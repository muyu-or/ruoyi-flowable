#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_HOST="${REMOTE_HOST:-8.136.186.107}"
REMOTE_PATH="${REMOTE_PATH:-/data/ruoyi-flowable}"

cd "$ROOT_DIR"

test -f ruoyi-admin/target/ruoyi-admin.jar || {
  echo "Missing backend jar. Run bin/build-prod.sh first."
  exit 1
}

test -d ruoyi-ui/dist || {
  echo "Missing frontend dist. Run bin/build-prod.sh first."
  exit 1
}

echo "Uploading deployment bundle to ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH} ..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p ${REMOTE_PATH}/ruoyi-admin/target ${REMOTE_PATH}/ruoyi-ui"
scp docker-compose.prod.yml "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"
scp -r docker "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"
scp ruoyi-admin/target/ruoyi-admin.jar "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/ruoyi-admin/target/ruoyi-admin.jar"
scp -r ruoyi-ui/dist "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/ruoyi-ui/dist"

echo "Remote bundle uploaded."
echo "On the server run:"
echo "  cd ${REMOTE_PATH}"
echo "  mkdir -p /data/ruoyi/uploadPath /data/ruoyi/logs"
echo "  docker compose -f docker-compose.prod.yml up -d --build"
