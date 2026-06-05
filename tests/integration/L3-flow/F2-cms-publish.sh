#!/usr/bin/env bash
# tests/integration/L3-flow/F2-cms-publish.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== F2: CMS 发布流程 ==="
login

SLUG="/flow-e2e-$RANDOM"

echo "Step 1: 创建页面"
R=$(post_json "/admin/cms/pages" '{"slug":"'$SLUG'","pageType":"custom","title":"Flow E2E Page","defaultLocale":"zh-CN"}' "$ADMIN_TOKEN")
assert_code "F2.1 创建页面" "0" "$R"
PAGE_ID=$(get_field "$R" "id")
echo "  Page ID: $PAGE_ID"

if [ -n "$PAGE_ID" ]; then
  echo "Step 2: 保存草稿"
  R=$(put_json "/admin/cms/pages/$PAGE_ID/translations/zh-CN/draft" '{"title":"Flow Draft","content":{"blocks":[{"type":"text","content":"Hello"}]}}' "$ADMIN_TOKEN")
  assert_code "F2.2 保存草稿" "0" "$R"

  echo "Step 3: 发布"
  R=$(post_json "/admin/cms/pages/$PAGE_ID/translations/zh-CN/publish" '{}' "$ADMIN_TOKEN")
  assert_code "F2.3 发布" "0" "$R"

  echo "Step 4: 前台渲染"
  R=$(get "/api/cms/pages/render?path=$SLUG" "")
  assert_code "F2.4 前台渲染" "0" "$R"

  echo "Step 5: 清理"
  R=$(delete "/admin/cms/pages/$PAGE_ID" "$ADMIN_TOKEN")
  assert_code "F2.5 清理" "0" "$R"
fi

summary "F2 CMS 发布流程"
