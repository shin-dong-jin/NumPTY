#!/bin/bash

ansible-playbook playbooks/install-mysql.yml --limit numpty-mysql
ansible-playbook playbooks/install-mongo.yml --limit numpty-mongo
ansible-playbook playbooks/install-redis.yml --limit numpty-redis
ansible-playbook playbooks/install-worker.yml --limit numpty-worker
ansible-playbook playbooks/install-api.yml --limit numpty-api
ansible-playbook playbooks/install-bff.yml --limit numpty-bff
ansible-playbook playbooks/install-web.yml --limit numpty-web
ansible-playbook playbooks/configure-mysql.yml --limit numpty-mysql
ansible-playbook playbooks/configure-mongo.yml --limit numpty-mongo
ansible-playbook playbooks/configure-redis.yml --limit numpty-redis
ansible-playbook playbooks/configure-worker.yml --limit numpty-worker
ansible-playbook playbooks/configure-api.yml --limit numpty-api
ansible-playbook playbooks/configure-bff.yml --limit numpty-bff
ansible-playbook playbooks/configure-web.yml --limit numpty-web