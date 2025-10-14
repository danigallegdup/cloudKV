# -----------------------------------------
# cloudKV — Dev Makefile (WSL-friendly)
# -----------------------------------------

SHELL := /bin/bash
.DEFAULT_GOAL := help

# ---- Config (override with: make VAR=value) ----
JDK17      ?= /usr/lib/jvm/java-17-openjdk-amd64
GRADLEW    ?= ./gradlew
APP_NAME   ?= api-rest
INSTALL_DIR := modules/$(APP_NAME)/build/install/$(APP_NAME)
PID_FILE   ?= server.pid
LOG_FILE   ?= server.log
BASE_URL   ?= http://localhost:8080
TIMEOUT_S  ?= 30

# Compose JAVA env for one-shot commands
ENVVARS := JAVA_HOME=$(JDK17) PATH=$(JDK17)/bin:$$PATH

# ---- Phony targets ----
.PHONY: help check-java build dist run-gradle run-dist run stop logs test it clean deepclean

help:
	@echo "cloudKV Make targets:"
	@echo "  make build        - Gradle build (api-rest module)"
	@echo "  make dist         - Create runnable distribution (installDist)"
	@echo "  make run-gradle   - Run via Gradle (foreground)"
	@echo "  make run-dist     - Run installed dist in background + wait for /health"
	@echo "  make run          - Alias to run-dist"
	@echo "  make stop         - Stop background server from run-dist"
	@echo "  make logs         - Tail server.log (CTRL+C to stop)"
	@echo "  make test         - Run unit tests for core and api-rest"
	@echo "  make it           - Integration test (starts dist, runs scripts/it_http_mvp.sh)"
	@echo "  make clean        - Clean module builds"
	@echo "  Vars you can override: JDK17, BASE_URL, TIMEOUT_S"

check-java:
	@echo "# Using JDK at: $(JDK17)"
	@test -x "$(JDK17)/bin/java" || (echo "❌ JDK17 not found at $(JDK17). Set JDK17=/path/to/jdk17" && exit 1)
	@env $(ENVVARS) java -version

build: check-java
	@echo "# Build (api-rest)"
	@env $(ENVVARS) $(GRADLEW) clean :modules:$(APP_NAME):build --no-daemon --console=plain --stacktrace

dist: check-java
	@echo "# installDist (api-rest)"
	@env $(ENVVARS) $(GRADLEW) :modules:$(APP_NAME):installDist --no-daemon --console=plain --stacktrace
	@ls -la $(INSTALL_DIR)/bin || true

run-gradle: check-java
	@echo "# Running via Gradle (foreground)"
	@env $(ENVVARS) $(GRADLEW) :modules:$(APP_NAME):run --no-daemon --console=plain --stacktrace

run-dist: dist
	@echo "# Starting $(APP_NAME) from $(INSTALL_DIR) -> $(LOG_FILE)"
	@nohup $(INSTALL_DIR)/bin/$(APP_NAME) > $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@sleep 1
	@echo "# Waiting for health at $(BASE_URL)/health (timeout $(TIMEOUT_S)s)..."
	@for i in $$(seq 1 $(TIMEOUT_S)); do \
	  if curl -fsS $(BASE_URL)/health | grep -q '"UP"'; then \
	    echo "# Server healthy."; exit 0; \
	  fi; \
	  sleep 1; \
	done; \
	echo "❌ Server did not become healthy in time"; \
	[ -f $(LOG_FILE) ] && tail -n 200 $(LOG_FILE) || true; \
	exit 1

# alias
run: run-dist

stop:
	@if [ -f $(PID_FILE) ]; then \
	  echo "# Stopping PID $$(cat $(PID_FILE))"; \
	  kill $$(cat $(PID_FILE)) || true; \
	  rm -f $(PID_FILE); \
	else \
	  echo "# No $(PID_FILE) found (already stopped?)"; \
	fi

logs:
	@echo "# Tailing $(LOG_FILE) — CTRL+C to stop"
	@tail -f $(LOG_FILE)

test: check-java
	@echo "# Running unit tests"
	@env $(ENVVARS) $(GRADLEW) :modules:core:test :modules:api-rest:test --no-daemon --console=plain --stacktrace

it: run-dist
	@echo "# Running HTTP MVP integration tests"
	@./scripts/it_http_mvp.sh || (echo "❌ Integration tests failed"; cat $(LOG_FILE); $(MAKE) stop; exit 1)
	@$(MAKE) stop
	@echo "✅ Integration tests passed"

clean:
	@env $(ENVVARS) $(GRADLEW) clean --no-daemon --console=plain

deepclean: clean
	@rm -f $(PID_FILE) $(LOG_FILE)
