#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_NAME="${PROJECT_NAME:-daehyun-backend}"
PUBLIC_PORT="${PUBLIC_PORT:-8080}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
HEALTH_RETRIES="${HEALTH_RETRIES:-30}"
HEALTH_SLEEP_SECONDS="${HEALTH_SLEEP_SECONDS:-3}"
DRAIN_SECONDS="${DRAIN_SECONDS:-10}"
REQUESTED_APP_IMAGE="${APP_IMAGE:-}"
REQUESTED_APP_IMAGE_TAG="${APP_IMAGE_TAG:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-${PROJECT_ROOT}/docker-compose.bluegreen.yml}"
ENV_FILE="${ENV_FILE:-${PROJECT_ROOT}/deploy/.env.production}"
NGINX_TEMPLATE="${NGINX_TEMPLATE:-${PROJECT_ROOT}/deploy/nginx/default.conf.template}"
NGINX_RUNTIME_DIR="${NGINX_RUNTIME_DIR:-${PROJECT_ROOT}/deploy/runtime/nginx}"
NGINX_CONF="${NGINX_CONF:-${NGINX_RUNTIME_DIR}/default.conf}"

cd "${PROJECT_ROOT}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

APP_IMAGE="${REQUESTED_APP_IMAGE:-${APP_IMAGE:-ghcr.io/daehyuh/daehyun-backend}}"
if [[ -n "${REQUESTED_APP_IMAGE_TAG}" ]]; then
  APP_IMAGE_TAG="${REQUESTED_APP_IMAGE_TAG}"
else
  APP_IMAGE_TAG="${APP_IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
fi

export PROJECT_NAME
export PUBLIC_PORT
export APP_IMAGE
export APP_IMAGE_TAG

DOCKER=()
if docker ps >/dev/null 2>&1; then
  DOCKER=(docker)
elif command -v sudo >/dev/null 2>&1 && sudo -n docker ps >/dev/null 2>&1; then
  DOCKER=(sudo -n docker)
else
  echo "Cannot access Docker. Add this user to the docker group or allow passwordless sudo for docker." >&2
  exit 1
fi

COMPOSE=("${DOCKER[@]}" compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}")
COMPOSE_NETWORK="${PROJECT_NAME}_backend"

mkdir -p "${NGINX_RUNTIME_DIR}"

deployment_switched="false"
target_service=""

service_is_running() {
  local service="$1"
  local container_id
  container_id="$("${COMPOSE[@]}" ps -q "${service}" 2>/dev/null || true)"
  [[ -n "${container_id}" ]] && [[ "$("${DOCKER[@]}" inspect -f '{{.State.Running}}' "${container_id}" 2>/dev/null || true)" == "true" ]]
}

configured_active_service() {
  if [[ -f "${NGINX_CONF}" ]] && grep -q "app-green" "${NGINX_CONF}"; then
    echo "app-green"
  elif [[ -f "${NGINX_CONF}" ]] && grep -q "app-blue" "${NGINX_CONF}"; then
    echo "app-blue"
  elif service_is_running "app-blue"; then
    echo "app-blue"
  elif service_is_running "app-green"; then
    echo "app-green"
  else
    echo ""
  fi
}

opposite_service() {
  case "$1" in
    app-blue) echo "app-green" ;;
    app-green) echo "app-blue" ;;
    *) echo "app-blue" ;;
  esac
}

render_nginx_conf() {
  local active_service="$1"
  sed "s/{{ACTIVE_APP}}/${active_service}/g" "${NGINX_TEMPLATE}" > "${NGINX_CONF}"
}

wait_for_health() {
  local service="$1"
  local url="http://${service}:8080${HEALTH_PATH}"

  for ((attempt = 1; attempt <= HEALTH_RETRIES; attempt++)); do
    if "${DOCKER[@]}" run --rm --network "${COMPOSE_NETWORK}" curlimages/curl:8.10.1 -fsS "${url}" >/dev/null; then
      echo "Health check passed for ${service}"
      return 0
    fi

    echo "Waiting for ${service} health check (${attempt}/${HEALTH_RETRIES})"
    sleep "${HEALTH_SLEEP_SECONDS}"
  done

  echo "Health check failed for ${service}" >&2
  return 1
}

cleanup_failed_deployment() {
  if [[ -n "${target_service}" && "${deployment_switched}" != "true" ]]; then
    echo "Deployment failed before traffic switch. Cleaning up ${target_service}." >&2
    "${COMPOSE[@]}" stop "${target_service}" >/dev/null 2>&1 || true
    "${COMPOSE[@]}" rm -f "${target_service}" >/dev/null 2>&1 || true
  fi
}

trap cleanup_failed_deployment ERR

reload_or_start_nginx() {
  if service_is_running "nginx"; then
    "${COMPOSE[@]}" exec -T nginx nginx -s reload
  else
    "${COMPOSE[@]}" up -d nginx
  fi
}

login_to_registry_if_configured() {
  local registry="${CONTAINER_REGISTRY:-ghcr.io}"
  local username="${CONTAINER_REGISTRY_USERNAME:-${GHCR_USERNAME:-}}"
  local token="${CONTAINER_REGISTRY_TOKEN:-${GHCR_TOKEN:-}}"

  if [[ -n "${username}" && -n "${token}" ]]; then
    echo "Logging in to ${registry} as ${username}"
    printf '%s' "${token}" | "${DOCKER[@]}" login "${registry}" -u "${username}" --password-stdin >/dev/null
  else
    echo "Skipping registry login. Set GHCR_USERNAME and GHCR_TOKEN when pulling private images."
  fi
}

active_service="$(configured_active_service)"
target_service="$(opposite_service "${active_service}")"

echo "Active service: ${active_service:-none}"
echo "Target service: ${target_service}"
echo "Image: ${APP_IMAGE}:${APP_IMAGE_TAG}"

login_to_registry_if_configured
"${COMPOSE[@]}" up -d db
"${COMPOSE[@]}" pull "${target_service}"
"${COMPOSE[@]}" up -d --no-deps "${target_service}"

wait_for_health "${target_service}"
render_nginx_conf "${target_service}"
reload_or_start_nginx
deployment_switched="true"

if [[ -n "${active_service}" && "${active_service}" != "${target_service}" ]]; then
  echo "Draining ${active_service} for ${DRAIN_SECONDS}s"
  sleep "${DRAIN_SECONDS}"
  "${COMPOSE[@]}" stop "${active_service}" || true
  "${COMPOSE[@]}" rm -f "${active_service}" || true
fi

echo "Deployment complete. Active service is ${target_service}."
