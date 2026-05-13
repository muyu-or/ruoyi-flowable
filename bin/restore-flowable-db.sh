#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL_FILE="${1:-$ROOT_DIR/flowable_full.sql}"
COMPOSE_FILE="${COMPOSE_FILE:-$ROOT_DIR/docker-compose.prod.yml}"
MYSQL_SERVICE="${MYSQL_SERVICE:-mysql}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-}"
MYSQL_DATABASE="${MYSQL_DATABASE:-flowable}"
MYSQL_USERNAME="${MYSQL_USERNAME:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"

if [[ ! -f "$SQL_FILE" ]]; then
  echo "SQL file not found: $SQL_FILE"
  exit 1
fi

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Compose file not found: $COMPOSE_FILE"
  exit 1
fi

run_mysql() {
  if [[ -n "$MYSQL_CONTAINER" ]]; then
    docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USERNAME" -p"$MYSQL_PASSWORD" "$@"
  else
    docker compose -f "$COMPOSE_FILE" exec -T "$MYSQL_SERVICE" \
      mysql -u"$MYSQL_USERNAME" -p"$MYSQL_PASSWORD" "$@"
  fi
}

wait_for_mysql() {
  echo "[1/5] Waiting for MySQL to become ready..."
  for _ in $(seq 1 60); do
    if run_mysql -N -e "SELECT 1;" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  echo "MySQL is not ready. Start it first, for example:"
  echo "  docker compose -f $COMPOSE_FILE up -d $MYSQL_SERVICE"
  exit 1
}

check_lower_case_table_names() {
  local lower_case
  lower_case="$(run_mysql -N -e "SHOW VARIABLES LIKE 'lower_case_table_names';" | awk '{print $2}')"

  if [[ "$lower_case" != "1" ]]; then
    echo "Unexpected lower_case_table_names=$lower_case"
    echo "This project expects lower_case_table_names=1 before importing Flowable data."
    echo "If this MySQL data directory was initialized with another value, recreate the MySQL volume/data directory first."
    exit 1
  fi
}

check_flowable_config() {
  local config_file="$ROOT_DIR/ruoyi-admin/src/main/resources/application.yml"
  if ! rg -n "database-schema-update:\\s*false" "$config_file" >/dev/null 2>&1; then
    echo "Expected flowable.database-schema-update=false in $config_file"
    echo "Set it to false before importing a full Flowable backup."
    exit 1
  fi
}

restore_database() {
  echo "[2/5] Recreating database $MYSQL_DATABASE ..."
  run_mysql -e "DROP DATABASE IF EXISTS \`$MYSQL_DATABASE\`; CREATE DATABASE \`$MYSQL_DATABASE\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

  echo "[3/5] Importing $SQL_FILE ..."
  if [[ -n "$MYSQL_CONTAINER" ]]; then
    docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USERNAME" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < "$SQL_FILE"
  else
    docker compose -f "$COMPOSE_FILE" exec -T "$MYSQL_SERVICE" \
      mysql -u"$MYSQL_USERNAME" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < "$SQL_FILE"
  fi
}

verify_restore() {
  echo "[4/5] Verifying Flowable metadata ..."
  local schema_version
  local lower_case
  schema_version="$(run_mysql -N -D "$MYSQL_DATABASE" -e "SELECT VALUE_ FROM ACT_GE_PROPERTY WHERE NAME_='schema.version';")"
  lower_case="$(run_mysql -N -e "SHOW VARIABLES LIKE 'lower_case_table_names';" | awk '{print $2}')"

  if [[ "$schema_version" != "6.8.0.0" ]]; then
    echo "Unexpected ACT_GE_PROPERTY schema.version: ${schema_version:-<empty>}"
    exit 1
  fi

  run_mysql -D "$MYSQL_DATABASE" -e "SELECT COUNT(*) AS flowable_tables FROM information_schema.tables WHERE table_schema='$MYSQL_DATABASE' AND (table_name LIKE 'ACT_%' OR table_name LIKE 'FLW_%');"

  echo "[5/5] Restore finished."
  echo "lower_case_table_names=$lower_case"
  echo "schema.version=$schema_version"
  echo "Next step: restart the backend service."
}

wait_for_mysql
check_lower_case_table_names
check_flowable_config
restore_database
verify_restore
