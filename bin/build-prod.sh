#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

echo "[1/4] Building backend jar..."
mvn -pl ruoyi-admin -am clean package -Dmaven.test.skip=true

echo "[2/4] Installing frontend dependencies..."
npm --prefix ruoyi-ui ci

echo "[3/4] Building frontend dist..."
npm --prefix ruoyi-ui run build:prod

echo "[4/4] Build completed."
echo "Backend jar: $ROOT_DIR/ruoyi-admin/target/ruoyi-admin.jar"
echo "Frontend dist: $ROOT_DIR/ruoyi-ui/dist"
