#!/bin/bash
# Publish configurations to Nacos config center
# Usage: ./publish-config.sh <nacos-host> <nacos-port> <username> <password>

NACOS_HOST=${1:-localhost}
NACOS_PORT=${2:-8080}
NACOS_USER=${3:-nacos}
NACOS_PASS=${4:-chengccn}

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Logging in to Nacos..."
TOKEN=$(curl -s -X POST "http://${NACOS_HOST}:${NACOS_PORT}/v1/auth/users/login" \
  -d "username=${NACOS_USER}&password=${NACOS_PASS}" | \
  python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Failed to authenticate with Nacos"
  exit 1
fi
echo "Authentication successful"

for config_file in "$SCRIPT_DIR"/configs/*.yaml; do
  data_id=$(basename "$config_file" .yaml)
  content=$(cat "$config_file")

  echo "Publishing $data_id..."

  RESPONSE=$(curl -s -X POST "http://${NACOS_HOST}:${NACOS_PORT}/v1/cs/configs" \
    -d "dataId=${data_id}&group=DEFAULT_GROUP&content=${content}&accessToken=${TOKEN}" 2>&1)

  if [ "$RESPONSE" = "true" ]; then
    echo "  ✓ $data_id published successfully"
  else
    echo "  ✗ Failed to publish $data_id: $RESPONSE"
  fi
done

echo "Done!"
