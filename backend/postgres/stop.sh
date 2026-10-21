#!/usr/bin/env bash
set -euo pipefail

docker stop tennis-stats-postgres >/dev/null 2>&1 || true
docker rm tennis-stats-postgres >/dev/null 2>&1 || true

echo "tennis-stats-postgres stopped and removed."
