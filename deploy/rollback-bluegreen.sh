#!/usr/bin/env bash
set -Eeuo pipefail

if [[ $# -lt 1 && -z "${APP_IMAGE_TAG:-}" ]]; then
  echo "Usage: $0 <previous-image-tag>" >&2
  echo "Or set APP_IMAGE_TAG=<previous-image-tag>." >&2
  exit 1
fi

export APP_IMAGE_TAG="${APP_IMAGE_TAG:-$1}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Rolling back to image tag: ${APP_IMAGE_TAG}"
bash "${SCRIPT_DIR}/deploy-bluegreen.sh"
