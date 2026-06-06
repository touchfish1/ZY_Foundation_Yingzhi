#!/usr/bin/env bash
# tests/integration/L2-api/C-asset.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-C: 资产管理测试 ==="
login

R=$(get "/admin/assets/files" "$ADMIN_TOKEN")
assert_code "C01 文件列表" "0" "$R"

TMP_FILE="/tmp/e2e-asset-${RANDOM}.txt"
echo "E2E test upload $(date)" > "$TMP_FILE"
R=$(post_multipart "/admin/assets/files" "file" "$TMP_FILE" "$ADMIN_TOKEN")
assert_code "C02 文件上传" "0" "$R"
if ! echo "$R" | python3 -c "import sys,json; d=json.load(sys.stdin); print('has_url' if d.get('data',{}).get('url') else 'no_url')" 2>/dev/null | grep -q "has_url"; then
  echo "FAIL: C02 response missing url field: $R"
  exit 1
fi
rm -f "$TMP_FILE"

R=$(get "/api/ddd/assets" "$ADMIN_TOKEN")
assert_code "C03 DDD 资产列表" "0" "$R"

summary "L2-C 资产管理"
