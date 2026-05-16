#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_NAME="${PROJECT_NAME:-daehyun-backend}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
PRODUCTION_ENV_FILE="${PRODUCTION_ENV_FILE:-${PROJECT_ROOT}/deploy/.env.production}"
MONITORING_ENV_FILE="${MONITORING_ENV_FILE:-${PROJECT_ROOT}/deploy/.env.monitoring}"
APP_COMPOSE_FILE="${APP_COMPOSE_FILE:-${PROJECT_ROOT}/docker-compose.bluegreen.yml}"
MONITORING_COMPOSE_FILE="${MONITORING_COMPOSE_FILE:-${PROJECT_ROOT}/docker-compose.monitoring.yml}"

cd "${PROJECT_ROOT}"

if [[ -f "${PRODUCTION_ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${PRODUCTION_ENV_FILE}"
  set +a
fi

if [[ -f "${MONITORING_ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${MONITORING_ENV_FILE}"
  set +a
fi

ENABLE_MONITORING="${ENABLE_MONITORING:-false}"
if [[ "${ENABLE_MONITORING}" != "true" ]]; then
  echo "Skipping monitoring stack. Set ENABLE_MONITORING=true in deploy/.env.monitoring to enable it."
  exit 0
fi

required_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required environment variable: ${name}" >&2
    exit 1
  fi
}

required_file() {
  local path="$1"
  if [[ ! -f "${path}" ]]; then
    echo "Missing required file: ${path}" >&2
    exit 1
  fi
}

required_file "${APP_COMPOSE_FILE}"
required_file "${MONITORING_COMPOSE_FILE}"
required_env "SPRING_DATASOURCE_PASSWORD"
required_env "MARIADB_ROOT_PASSWORD"
required_env "GRAFANA_ADMIN_PASSWORD"

COMPOSE=(
  docker compose
  -p "${PROJECT_NAME}"
  -f "${APP_COMPOSE_FILE}"
  -f "${MONITORING_COMPOSE_FILE}"
)

echo "Starting monitoring stack for ${PROJECT_NAME}"
"${COMPOSE[@]}" up -d prometheus grafana alertmanager node-exporter cadvisor

echo "Monitoring stack is active."
echo "Grafana: http://${GRAFANA_BIND_ADDRESS:-127.0.0.1}:${GRAFANA_PORT:-3000}"
echo "Prometheus: http://${PROMETHEUS_BIND_ADDRESS:-127.0.0.1}:${PROMETHEUS_PORT:-9090}"
