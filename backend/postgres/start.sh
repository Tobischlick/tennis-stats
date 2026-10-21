#!/usr/bin/env bash
set -euo pipefail

# Matches the defaults in backend/src/main/resources/application.properties
docker rm -f tennis-stats-postgres >/dev/null 2>&1 || true

docker run -d \
  --name tennis-stats-postgres \
  -e POSTGRES_USER=tennis_stats \
  -e POSTGRES_PASSWORD=tennis_stats \
  -e POSTGRES_DB=tennis_stats \
  -p 5432:5432 \
  postgres:16

echo "Postgres running on localhost:5432 (db=tennis_stats, user=tennis_stats, password=tennis_stats)."
