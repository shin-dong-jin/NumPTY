# NumPTY

[![Deploy](https://github.com/shin-dong-jin/NumPTY/actions/workflows/deploy.yml/badge.svg)](https://github.com/shin-dong-jin/NumPTY/actions/workflows/deploy.yml)

```
Numerical PTY
A number theory calculator with terminal interface
```

웹 브라우저에서 실제 터미널처럼 명령을 입력해 정수론 연산(최대공약수, 소인수분해, 소수판정 등)을 수행하는 서비스

https://numpty.kr

<br/>

# Demo

<img width="1580" height="1173" alt="Image" src="https://github.com/user-attachments/assets/e558c7d2-70dd-4b1f-9f8d-33be544d798c" />

<br/>

<img width="1580" height="460" alt="Image" src="https://github.com/user-attachments/assets/a7d5edad-a338-4b79-a6c3-0cb515e3f05f" />

<br/>

# Architecture

<img width="1160" height="1760" alt="Image" src="https://github.com/user-attachments/assets/ba28f624-a1fb-4b36-b513-6577ff1e37eb" />

- 동기 경로: client -> web -> bff -> api -> MySQL/MongoDB
- 비동기 경로: api -> redis(request) -> worker -> redis(response) -> api/bff (팬아웃)
- 인프라
  - 단일 VPC
  - public/private subnet 분리
  - 외부 진입은 web만, 나머지는 private
  - private egress는 nat 인스턴스 경유
  - 배포 시에만 bastion 일시적으로 생성

<br/>

# Tech Stack

| 영역      | 스택                                                                 |
| --------- | -------------------------------------------------------------------- |
| Infra/IaC | AWS, CloudFormation                                                  |
| CI/CD     | GitHub Actions (OIDC), Ansible                                       |
| Web       | HTML/CSS/JS, xterm.js, nginx                                         |
| BFF       | Node.js, node-pty, PM2, Redis, zsh                                   |
| API       | Java 17, Tomcat, JDBC(Connector/J) + HikariCP, Jedis, MongoDB Driver |
| Worker    | C, GMP, hiredis, CMake                                               |
| Data      | MySQL 8.0, MongoDB 8.0, Redis 6                                      |

<br/>

# Components

## [Web](web/README.md)

```
[Stack] html/css/js, xterm.js, nginx
브라우저에서 동작하는 웹 터미널 클라이언트
```

웹페이지에서 실제 터미널처럼 입력하면 WebSocket을 통해 서버(BFF)의 PTY 세션과 실시간 연결

[xterm.js](https://xtermjs.org/) 기반 SPA (nginx로 정적 파일 서빙)

### 주요 특징

- WebSocket
  - 웹소켓 활용한 webserver와 bff 양방향 통신
  - `[web -> bff]` init, stdin, resize(JSON)
  - `[bff -> web]` PTY 출력(raw), 테마 변경 및 JWT 토큰(OSC 9999)

- Finite State Machine
  - 연결과 세션을 두 개의 상태 기계로 나눠 관리
  - Connection FSM: 소켓 상태(연결/끊김/재연결)
  - Session FSM: 세션 생명주기(부팅/동작/복구/종료)
  - 허용된 전이만 통과시켜 잘못된 상태 전이 방지

- 연결이 끊기면 지수 백오프로 자동 재연결, 복구 시 세션 재생성

<br/>

## [BFF](bff/README.md)

```
[Stack] Node.js, node-pty, PM2, Redis, zsh
웹 터미널의 백엔드 - PTY 세션과 비동기 작업 중계
```

web의 WebSocket을 받아 격리된 셸 세션 생성

셸에서 발생한 연산 작업을 API / Redis 스트림을 통해 worker에 전달하고 작업 결과 반환

PM2가 관리하는 두 개의 독립적인 프로세스로 구성

- **app**: WebSocket으로 web과 통신, PTY 세션 관리
- **daemon**: Redis 스트림 구독, Unix domain socket으로 셸 명령과 통신

※ 세션(휘발성, 클라이언트별 독립적)과 Redis 작업 처리(상시 리스너)는 생명 주기가 다르기 때문에 프로세스 분리하여 독립적으로 동작하고 관리

### 주요 특징

- 터미널 세션 관리 (app)
  - init 수신 시 node-pty로 세션마다 격리된 restricted zsh를 spawn하고 uuid로 추적, 연결 종료 시 정리
  - workspace, PATH, HOME을 전용 os/ 디렉터리로 묶어 샌드박스된 셸 환경을 구성
- Redis 스트림 + IPC (daemon)
  - 요청/응답 두 스트림을 단일 blocking xread로 구독
  - 셸 명령이 Unix domain socket으로 작업 \_id 전송(app) -> 대기 목록에 작업 등록 후 대기(daemon) -> worker 응답이 스트림에 도착하면 \_id로 매칭해 해당 셸로 결과 반환(daemon)
- 커스텀 명령어
  - 샌드박스 셸의 연산 명령(gcd, factorize 등): C wrapper + Node script 2단 구조, 단일 실행파일 binary PATH에 노출
  - login: api 인증 후 받은 JWT 토큰을 OSC 시퀀스로 전송
  - theme: 테마 변경을 OSC 시퀀스로 전송 (제어 시퀀스라 터미널에 출력되지 않음)
- 시스템 바이너리를 화이트리스트로 격리한 샌드박스 파일시스템 구성 (setup_os.sh)
- SIGTERM 시 모든 PTY 세션을 정리하는 graceful shutdown

<br/>

## [API](api/README.md)

```
[Stack] Java 17, Tomcat, JDBC(Connector/J) + HikariCP, Jedis, MongoDB Driver
프레임워크부터 직접 구현한 비동기 작업 처리 서버
```

연산 작업 요청을 받아 Redis 스트림으로 worker에 넘기고, 결과를 수신해 영속화하는 서버

Spring 같은 기존 프레임워크를 쓰지 않고, DI 컨테이너부터
디스패처·시큐리티·데이터 접근 계층까지 직접 구현

커스텀 프레임워크 위에 도메인을 얹고, Redis 스트림으로 worker와 비동기로 연결

### A. 자체 프레임워크 (framework/)

기존 프레임워크의 핵심 메커니즘을 직접 구현해 동작 원리를 이해한 위에서 사용

- DI 컨테이너 + 부트스트랩
  - 빈을 등록·조립하는 ApplicationContext
  - 임베디드 Tomcat을 직접 띄우는 부트 시퀀스
  - 생성자 주입으로 의존성 명시적으로 연결
- DispatcherServlet + 함수형 라우팅
  - 요청 라우팅 -> 핸들러 어댑터 선택 -> 처리 -> 응답 변환으로 잇는 디스패처 서블릿 구현
  - 함수형 라우팅 RouterFunction / HandlerFunction 등록, 핸들러 디스패치에 리플렉션을 쓰지 않아, 요청 경로가 명시적이고 추적 가능
- Security Filter Chain
  - JWT 인증/인가를 필터 체인으로 구현
  - DelegatingFilterProxy -> FilterChainProxy -> 인증/인가 필터
  - BCrypt 비밀번호 해싱, CORS, SecurityContext, CSRF 필터(구현 후 stateless JWT 정책에 따라 비활성)
- Template method를 활용한 데이터 접근
  - JDBCTemplate, MongoTemplate으로 연결 획득, 해제와 예외 처리를 캡슐화
  - TransactionTemplate, TransactionManager를 활용한 ThreadLocal 컨텍스트 커밋 / 롤백 관리
- Exception resolver chain
  - 계층별 예외를 ExceptionResolver 후보들이 순회하며 적절한 HTTP 응답으로 변환
  - 프레임워크 예외와 비즈니스 예외를 분리

### B. 애플리케이션 설계 (app/)

프레임워크 위에 올린 도메인

5종 연산 작업(gcd, lcm, factorize 등)을 하나의 일관된 구조로 처리

- 제네릭 + sealed 기반 Task 엔티티
  - Task<P extends TaskPayload<?,?>>, TaskPayload<I,O>(sealed, 5종으로 고정)로 작업 종류가 달라도 같은 구조 공유
  - static factory(create/reconstitute), 상태 전이 가드를 엔티티가 스스로 지키는 rich domain
  - TaskLifecycle이 작업 상태(PENDING -> WORKING -> COMPLETED/FAILED)를 상태 머신으로 관리
- 계층 흐름
  - controller -> service -> repository(Jedis/MongoDB/MySQL)
  - repository는 용도에 따라 Jedis(작업 큐잉), MongoDB(작업 결과 영속화), JDBC + Connector/J(회원 인증, MySQL)로 분리
- Strategy pattern
  - worker가 보내는 상태(WORKING/COMPLETED/FAILED/UNKNOWN)별 핸들러를 supports()로 선택해 분기
  - 작업 종류마다 핸들러 셋을 갖춰, 새 연산이나 상태를 추가해도 동일한 패턴으로 확장

### C. 비동기 처리 & 운영

- Redis Streams consumer (cg-was)
  - worker의 결과 스트림(numpty:worker:response)을 consumer group으로 구독
  - 별도 워커 스레드에서 xreadGroup(blocking)으로 수신하고, 처리 후 xack. 연결이 끊기면 재연결 후 stream 처리 재시도
  - Redis stream 데이터는 상태별 핸들러로 분기해 MongoDB tasks collection에 영속화
- Graceful shutdown
  - 서버 종료 시 컨테이너가 모든 AutoCloseable 빈(커넥션 풀, 스트림 리스너 등)을 순회하며 정리
  - 셧다운 훅으로 SIGTERM에 연결되고, 멱등하게 동작

<br/>

## [Worker](worker/README.md)

```
[Stack] C, GMP, hiredis, CMake
임의정밀도 정수론 연산을 수행하는 네이티브 워커
```

api가 Redis request 스트림에 추가한 연산 작업을 읽어 처리하고 연산 결과를 result 스트림에 추가하는 C 네이티브 서버

큰 수 연산이 핵심이라 GMP로 임의정밀도를 다루며, 배포 환경인 Amazon Linux 2023과 동일한 환경에서 빌드해 동적 링크된 바이너리로 동작

### A. 파이프라인 구조

수신/연산/송신의 계층적 분리, 메인루프로 통합

- ingress / calculate / egress 3계층
  - transport/(수신, 송신)와 calculator/(연산)로 책임 분리
  - 메인루프는 fetch -> exec -> send로 진행
- 함수 포인터 디스패치
  - task 종류를 exec_table[](함수 포인터 배열)로 매핑
  - 분기 없이 해당 task 연산 함수로 디스패치
- 시작 시 사전 계산
  - 에라토스테네스 체, GMP 난수 상태를 시작 시 1회 초기화 (요청마다 재생성 X)

### B. Redis streams (hiredis)

작업 큐를 Redis Streams로 소비하며, 결과 손실이 없도록 ack를 명시적으로 관리

- consumer group 소비
  - 요청 스트림(numpty:was:request)을 XREADGROUP BLOCK 으로 consume (blocking, 동기 hiredis)
  - 타임아웃이면(수신 메시지 없음) 루프를 계속 돌며 다시 대기
- at-least-once 보장
  - 연산 결과를 스트림에 전송하는 데 성공한 뒤에만 XACK
  - 전송이 실패하면 메시지는 PEL(Pending Entries List)에 남아 재시도 대상이
    되어, 결과 유실 방지
- 상태 보고
  - 작업 진행 상태에 따라 WORKING -> COMPLETED/FAILED를 결과 스트림 (numpty:worker:response)에 단계적으로 전송
  - 손상된 payload도 FAILED로 보고 후 정리 (FAILED Task는 API가 리스너로 받아 처리)

### C. 정수론 연산 (GMP)

모든 연산은 GMP mpz_t(임의정밀도)로 처리해 자릿수 제한 없이 큰 수 처리 가능

| Task            | 알고리즘                               | 비고            |
| --------------- | -------------------------------------- | --------------- |
| GCD             | 유클리드 호제법                        |                 |
| LCM             | 유클리드 기반 (a \* b / gcd)           | gcd 결과 재사용 |
| Extended GCD    | 확장 유클리드                          | 베주 계수       |
| Primality Test  | Miller-Rabin (k회 반복)                | 확률적 소수판정 |
| Prime Factorize | Trial division -> Pollard's rho -> ECM | 아래 다단 전략  |

※ Prime Factorize 다단 전략: 자릿수로 분기하지 않고, 알고리즘 특성에 따라 단계적으로 처리

1. Trial division: 작은 소인수를 먼저 제거 (수 크기와 무관)
2. Pollard's rho: 남은 합성수의 인수를 탐색 (중간 크기 인수에 효율적)
3. ECM: Pollard's rho가 정해진 시도(POLLARD_RHO_MAX_STEPS) 안에 실패하면 폴백

- 찾은 인수는 재귀적으로 다시 분해
- 각 인수의 소수성은 Miller-Rabin으로 검증
- 최종 결과는 퀵정렬 뒤 반환
- 복잡도는 합성수의 크기가 아니라 소인수 크기에 따라 결정됨

### D. 운영

- Graceful shutdown
  - SIGTERM 수신 시 volatile sig_atomic_t 플래그로 메인루프를 안전하게 탈출
  - 탈출 뒤 진행 중 작업의 자원 해제
- 런타임 로그 레벨 변경
  - 시그널로 동작 중 로그 레벨을 조정
  - 재시작 없이 디버깅 수준 변경 가능
- 명시적 자원 관리
  - task, Redis 클라이언트, sieve, 난수 상태를 일관된 생명주기로 생성 및 해제
  - 사용한 Redis 비밀번호는 메모리에서 즉시 제거 (explicit_bzero)

<br/>

# Deploy & Infra

main push가 github actions deploy workflow 트리거

인프라 프로비저닝부터 7개 컴포넌트 빌드, 배포까지 자동 수행

<br/>

## CI/CD 파이프라인

```
build -> deploy-infra -> deploy-app
```

- build
  - 모든 컴포넌트 산출물을 생성해 아티팩트로 업로드
  - 유효성 게이트 역할 수행, 빌드가 실패하면 인프라/앱 배포 중단
- deploy-infra
  - network / data / app CloudFormation 스택 배포
- deploy-app
  - 일회성 bastion 생성 -> Ansible로 빌드 산출물 배포, 서비스 구성 -> bastion 삭제 (if: always()로 정리 보장)

<br/>

## 빌드 전략

산출물이 환경에 종속적인지에 따라 빌드 방식을 달리하되, runner에서 빌드하고 필요한
파일만 묶어 전송한다는 원칙 공유

- Web(Vite): dist를 아카이브로 묶어 단일 전송
- BFF(Node): 소스만 deterministic 아카이브로 전송하고 서버에서 install (네이티브
  모듈을 타깃 환경에서 컴파일)
- API(Java): jar는 매번, 의존성 라이브러리는 결정적 tar로 묶어 내용이
  바뀔 때만 재전송
- Worker(C): 네이티브 바이너리가 glibc에 동적 링크되므로, 배포 환경과 동일한
  AL2023 컨테이너에서 빌드해 ABI를 일치시킴 (EC2에는 빌드 도구 없이 런타임
  라이브러리만)
- 캐싱: setup-java/setup-node 캐시로 의존성 다운로드를 줄임

<br/>

## 비용 최적화

- NAT Gateway -> NAT 인스턴스: NAT Gateway를 NAT 인스턴스로 교체 (상시 ~$42/월 -> 가변)
- 일회성 bastion: SSH 진입점은 배포 중에만 생성하고 끝나면 파기, 상시 공격
  표면과 비용을 함께 제거

<br/>

# Run

## Cloud

GitHub Actions 파이프라인으로 배포 및 실행. main push가 진입점

### Dependencies

- AWS 계정 + OIDC 연동용 IAM Role
- GitHub Actions Secrets: SSH 키(bastion/data/app), Ansible Vault 비밀번호
- Route 53에 위임된 도메인 (TLS 인증서 발급용)

### Deploy and run

```bash
# main 브랜치 push 또는 Actions에서 수동 실행으로 전체 파이프라인 트리거
git push origin main
```

파이프라인이 인프라 프로비저닝 -> 빌드 -> 배포까지 자동 수행

인프라 템플릿은 infra/, 컴포넌트별 배포 플레이북은 ansible/playbooks/

<br/>

## Local

의존성 설치 후 각 쉘에서 명령어 실행

### Dependencies

- Runtime: Java 17, Node.js 24, GCC/CMake, gmp-devel, hiredis-devel, jq, zsh, starship
- Data: MySQL 8.0, MongoDB 8.0, Redis 6

### Run

```bash
# web
cd ./web
npm install
npm run dev

# bff
cd bff
npm install
bash setup_os.sh           # 샌드박스 셸 환경 구성 (최초 1회)

## app과 daemon을 각각 띄움 (터미널 2개)
npm run dev:app            # WebSocket + PTY 세션
npm run dev:daemon         # Redis 스트림 구독 + IPC

# api
cd api
./mvnw -B package -DskipTests
# DB_URL, MONGODB_URI, REDIS_HOST, JWT_SECRET 등 주입 후 (export)
java -jar target/api-1.0-SNAPSHOT.jar

# worker
cd worker
cmake -B build && cmake --build build
# REDIS_HOST, STREAM_REQUEST, STREAM_RESPONSE, CONSUMER_GROUP 등 주입 후 (export)
./build/worker
```

### export env 예시

```bash
# web: web/vite.config.js 참조

# bff: bff/src/appProperties.js, bff/src/daemonProperties.js 참조

# api: api/src/main/resources/application.properties 참조
export DB_USERNAME='...'
export DB_PASSWORD='...'
export MONGODB_CREDENTIAL_USERNAME='...'
export MONGODB_CREDENTIAL_PASSWORD='...'
export REDIS_PASSWORD='...'
export JWT_SECRET='...'

# worker: worker/Makefile 참조
export REDIS_PASSWORD='...'
```
