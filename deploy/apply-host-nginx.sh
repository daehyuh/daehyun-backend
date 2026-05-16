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

detect_existing_conf_path() {
  run_privileged_shell \
    "grep -R -l -F 'server_name ${HOST_NGINX_SERVER_NAME};' /etc/nginx/sites-enabled /etc/nginx/conf.d 2>/dev/null | grep -v '\\.bak$' | head -n 1" \
    || true
}

TEMPLATE_FILE="${HOST_NGINX_TEMPLATE:-${PROJECT_ROOT}/deploy/nginx/host-api.conf.template}"
PUBLIC_PORT="${PUBLIC_PORT:-18080}"
HOST_NGINX_SERVER_NAME="${HOST_NGINX_SERVER_NAME:-api.xn--vk1b177d.com}"
HOST_NGINX_UPSTREAM="${HOST_NGINX_UPSTREAM:-http://127.0.0.1:${PUBLIC_PORT}}"
HOST_NGINX_SSL_CERT="${HOST_NGINX_SSL_CERT:-/etc/letsencrypt/live/${HOST_NGINX_SERVER_NAME}/fullchain.pem}"
HOST_NGINX_SSL_KEY="${HOST_NGINX_SSL_KEY:-/etc/letsencrypt/live/${HOST_NGINX_SERVER_NAME}/privkey.pem}"
HOST_NGINX_CONF_PATH="${HOST_NGINX_CONF_PATH:-}"
if [[ -z "${HOST_NGINX_CONF_PATH}" ]]; then
  HOST_NGINX_CONF_PATH="$(detect_existing_conf_path)"
fi
HOST_NGINX_CONF_PATH="${HOST_NGINX_CONF_PATH:-/etc/nginx/conf.d/${HOST_NGINX_SERVER_NAME}.conf}"
HOST_NGINX_BACKUP_DIR="${HOST_NGINX_BACKUP_DIR:-/etc/nginx/codex-backups}"
HOST_NGINX_TEST_CMD="${HOST_NGINX_TEST_CMD:-nginx -t}"
HOST_NGINX_RELOAD_CMD="${HOST_NGINX_RELOAD_CMD:-systemctl reload nginx}"
EXPOSE_GRAFANA="${EXPOSE_GRAFANA:-false}"
GRAFANA_PUBLIC_PATH="${GRAFANA_PUBLIC_PATH:-/grafana}"
GRAFANA_UPSTREAM="${GRAFANA_UPSTREAM:-http://127.0.0.1:${GRAFANA_PORT:-3000}}"

require_file() {
  local path="$1"
  local label="$2"
  if ! run_privileged test -f "${path}"; then
    echo "${label} does not exist: ${path}" >&2
    exit 1
  fi
}

normalize_public_path() {
  local path="$1"
  [[ "${path}" == /* ]] || path="/${path}"
  while [[ "${path}" != "/" && "${path}" == */ ]]; do
    path="${path%/}"
  done
  printf '%s' "${path}"
}

render_grafana_location() {
  if [[ "${EXPOSE_GRAFANA}" != "true" ]]; then
    return 0
  fi

  local path
  path="$(normalize_public_path "${GRAFANA_PUBLIC_PATH}")"
  if [[ "${path}" == "/" ]]; then
    echo "GRAFANA_PUBLIC_PATH cannot be /. It would shadow the API proxy." >&2
    exit 1
  fi

  cat <<EOF
    location = ${path} {
        return 301 ${path}/;
    }

    location ${path}/ {
        proxy_pass ${GRAFANA_UPSTREAM};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Prefix ${path};
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 3s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
EOF
}

render_template() {
  local grafana_location
  local line
  grafana_location="$(render_grafana_location)"

  while IFS= read -r line || [[ -n "${line}" ]]; do
    if [[ "${line}" == *"{{GRAFANA_LOCATION}}"* ]]; then
      if [[ -n "${grafana_location}" ]]; then
        printf '%s\n' "${grafana_location}"
      fi
      continue
    fi

    line="${line//'{{SERVER_NAME}}'/${HOST_NGINX_SERVER_NAME}}"
    line="${line//'{{UPSTREAM}}'/${HOST_NGINX_UPSTREAM}}"
    line="${line//'{{SSL_CERT}}'/${HOST_NGINX_SSL_CERT}}"
    line="${line//'{{SSL_KEY}}'/${HOST_NGINX_SSL_KEY}}"
    printf '%s\n' "${line}"
  done < "${TEMPLATE_FILE}"
}

require_file "${TEMPLATE_FILE}" "Host Nginx template"
require_file "${HOST_NGINX_SSL_CERT}" "SSL certificate"
require_file "${HOST_NGINX_SSL_KEY}" "SSL certificate key"

tmp_file="$(mktemp)"
backup_file=""
render_template > "${tmp_file}"

if run_privileged test -f "${HOST_NGINX_CONF_PATH}"; then
  run_privileged mkdir -p "${HOST_NGINX_BACKUP_DIR}"
  backup_file="${HOST_NGINX_BACKUP_DIR}/$(basename "${HOST_NGINX_CONF_PATH}").$(date +%Y%m%d%H%M%S).bak"
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
