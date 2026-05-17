SHELL := /bin/sh

COMPOSE := docker compose
APP_SERVICES := backend frontend
ALL_SERVICES := mysql redis backend frontend

.PHONY: help build build-backend build-frontend restart restart-backend restart-frontend rebuild rebuild-backend rebuild-frontend up down ps logs logs-backend logs-frontend

help:
	@printf "%s\n" \
		"Available targets:" \
		"  make build            Build backend and frontend images" \
		"  make build-backend    Build backend image" \
		"  make build-frontend   Build frontend image" \
		"  make restart          Restart backend and frontend containers" \
		"  make restart-backend  Restart backend container" \
		"  make restart-frontend Restart frontend container" \
		"  make rebuild          Rebuild and restart backend/frontend" \
		"  make rebuild-backend  Rebuild and restart backend" \
		"  make rebuild-frontend Rebuild and restart frontend" \
		"  make up               Start all containers" \
		"  make down             Stop all containers" \
		"  make ps               Show container status" \
		"  make logs             Tail backend/frontend logs" \
		"  make logs-backend     Tail backend logs" \
		"  make logs-frontend    Tail frontend logs"

build:
	$(COMPOSE) build $(APP_SERVICES)

build-backend:
	$(COMPOSE) build backend

build-frontend:
	$(COMPOSE) build frontend

restart:
	$(COMPOSE) restart $(APP_SERVICES)

restart-backend:
	$(COMPOSE) restart backend

restart-frontend:
	$(COMPOSE) restart frontend

rebuild:
	$(COMPOSE) up -d --build $(APP_SERVICES)

rebuild-backend:
	$(COMPOSE) up -d --build backend

rebuild-frontend:
	$(COMPOSE) up -d --build frontend

up:
	$(COMPOSE) up -d $(ALL_SERVICES)

down:
	$(COMPOSE) down

ps:
	$(COMPOSE) ps

logs:
	$(COMPOSE) logs -f $(APP_SERVICES)

logs-backend:
	$(COMPOSE) logs -f backend

logs-frontend:
	$(COMPOSE) logs -f frontend
