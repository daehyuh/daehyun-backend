#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${PROJECT_ROOT}/.env}"

cd "${PROJECT_ROOT}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

APPLY_HOST_NGINX="${APPLY_HOST_NGINX:-false}"
if [[ "${APPLY_HOST_NGINX}" != "true" ]]; then
  echo "Skipping host Nginx apply. Set APPLY_HOST_NGINX=true to enable it."
  exit 0
fi

TEMPLATE_FILE="${HOST_NGINX_TEMPLATE:-${PROJECT_ROOT}/deploy/nginx/host-api.conf.template}"
PUBLIC_PORT="${PUBLIC_PORT:-18080}"
HOST_NGINX_SERVER_NAME="${HOST_NGINX_SERVER_NAME:-api.xn--vk1b177d.com}"
HOST_NGINX_UPSTREAM="${HOST_NGINX_UPSTREAM:-http://127.0.0.1:${PUBLIC_PORT}}"
HOST_NGINX_SSL_CERT="${HOST_NGINX_SSL_CERT:-/etc/letsencrypt/live/${HOST_NGINX_SERVER_NAME}/fullchain.pem}"
HOST_NGINX_SSL_KEY="${HOST_NGINX_SSL_KEY:-/etc/letsencrypt/live/${HOST_NGINX_SERVER_NAME}/privkey.pem}"
HOST_NGINX_CONF_PATH="${HOST_NGINX_CONF_PATH:-/etc/nginx/conf.d/${HOST_NGINX_SERVER_NAME}.conf}"
HOST_NGINX_TEST_CMD="${HOST_NGINX_TEST_CMD:-nginx -t}"
HOST_NGINX_RELOAD_CMD="${HOST_NGINX_RELOAD_CMD:-systemctl reload nginx}"

require_file() {
  local path="$1"
  local label="$2"
  if [[ ! -f "${path}" ]]; then
    echo "${label} does not exist: ${path}" >&2
    exit 1
  fi
}

escape_sed() {
  printf '%s' "$1" | sed -e 's/[\/&|]/\\&/g'
}

render_template() {
  sed \
    -e "s|{{SERVER_NAME}}|$(escape_sed "${HOST_NGINX_SERVER_NAME}")|g" \
    -e "s|{{UPSTREAM}}|$(escape_sed "${HOST_NGINX_UPSTREAM}")|g" \
    -e "s|{{SSL_CERT}}|$(escape_sed "${HOST_NGINX_SSL_CERT}")|g" \
    -e "s|{{SSL_KEY}}|$(escape_sed "${HOST_NGINX_SSL_KEY}")|g" \
    "${TEMPLATE_FILE}"
}

run_privileged() {
  if [[ "$(id -u)" -eq 0 ]]; then
    "$@"
  else
    sudo "$@"
  fi
}

run_privileged_shell() {
  if [[ "$(id -u)" -eq 0 ]]; then
    bash -c "$1"
  else
    sudo bash -c "$1"
  fi
}

require_file "${TEMPLATE_FILE}" "Host Nginx template"
require_file "${HOST_NGINX_SSL_CERT}" "SSL certificate"
require_file "${HOST_NGINX_SSL_KEY}" "SSL certificate key"

tmp_file="$(mktemp)"
backup_file=""
render_template > "${tmp_file}"

if run_privileged test -f "${HOST_NGINX_CONF_PATH}"; then
  backup_file="${HOST_NGINX_CONF_PATH}.$(date +%Y%m%d%H%M%S).bak"
  run_privileged cp "${HOST_NGINX_CONF_PATH}" "${backup_file}"
fi

run_privileged install -D -m 0644 "${tmp_file}" "${HOST_NGINX_CONF_PATH}"
rm -f "${tmp_file}"

if ! run_privileged_shell "${HOST_NGINX_TEST_CMD}"; then
  echo "nginx config test failed. Restoring previous config." >&2
  if [[ -n "${backup_file}" ]]; then
    run_privileged cp "${backup_file}" "${HOST_NGINX_CONF_PATH}"
    run_privileged_shell "${HOST_NGINX_TEST_CMD}" || true
  fi
  exit 1
fi

run_privileged_shell "${HOST_NGINX_RELOAD_CMD}"
echo "Host Nginx config applied: ${HOST_NGINX_CONF_PATH} -> ${HOST_NGINX_UPSTREAM}"
