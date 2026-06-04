CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '식별자 (UUID)',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '사용자 이메일 (로그인 ID)',
    name VARCHAR(50) NOT NULL COMMENT '사용자 이름',
    password VARCHAR(255) NOT NULL COMMENT 'Bcrypt 암호화된 비밀번호',
    status VARCHAR(20) NOT NULL COMMENT '계정 상태 (ACTIVE, INACTIVE, BANNED)',
    role VARCHAR(20) NOT NULL COMMENT '권한 (USER, ADMIN)',
    created_at TIMESTAMP NOT NULL COMMENT '생성 일시 (UTC)',
    updated_at TIMESTAMP NOT NULL COMMENT '마지막 수정 일시 (UTC)',
    deleted_at TIMESTAMP NULL COMMENT '탈퇴 일시 (Soft Delete 용도)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 테이블';