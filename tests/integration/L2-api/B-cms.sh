#!/usr/bin/env bash
# tests/integration/L2-api/B-cms.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

echo "=== L2-B: CMS 测试 ==="
login

SLUG="/e2e-test-$RANDOM"

echo "--- B01-B05: 页面管理 ---"
R=$(get "/admin/cms/pages" "$ADMIN_TOKEN")
assert_code "B01 页面列表" "0" "$R"

R=$(post_json "/admin/cms/pages" '{"slug":"'$SLUG'","pageType":"custom","title":"E2E Test Page","defaultLocale":"zh-CN"}' "$ADMIN_TOKEN")
assert_code "B02 创建页面" "0" "$R"
PAGE_ID=$(get_field "$R" "id")
echo "  Page ID: $PAGE_ID"

if [ -n "$PAGE_ID" ]; then
  R=$(get "/admin/cms/pages/$PAGE_ID" "$ADMIN_TOKEN")
  assert_code "B03 页面详情" "0" "$R"

  R=$(put_json "/admin/cms/pages/$PAGE_ID" '{"slug":"'$SLUG'","title":"E2E Updated Title"}' "$ADMIN_TOKEN")
  assert_code "B04 更新页面" "0" "$R"

  echo "--- B06-B07: 草稿 ---"
  R=$(put_json "/admin/cms/pages/$PAGE_ID/translations/zh-CN/draft" '{"title":"E2E Draft","content":{"blocks":[]}}' "$ADMIN_TOKEN")
  assert_code "B06 保存草稿" "0" "$R"

  R=$(get "/admin/cms/pages/$PAGE_ID/translations/zh-CN/draft" "$ADMIN_TOKEN")
  assert_code "B07 获取草稿" "0" "$R"

  echo "--- B08: 版本列表 ---"
  R=$(get "/admin/cms/pages/$PAGE_ID/translations/zh-CN/versions" "$ADMIN_TOKEN")
  assert_code "B08 版本列表" "0" "$R"

  echo "--- B09: 发布 ---"
  R=$(post_json "/admin/cms/pages/$PAGE_ID/translations/zh-CN/publish" '{}' "$ADMIN_TOKEN")
  assert_code "B09 发布页面" "0" "$R"

  R=$(get "/admin/cms/pages/$PAGE_ID/preview" "$ADMIN_TOKEN")
  assert_code "B11 预览页面" "0" "$R"

  R=$(delete "/admin/cms/pages/$PAGE_ID" "$ADMIN_TOKEN")
  assert_code "B05 删除页面" "0" "$R"
fi

echo "--- B10: 回滚 ---"
EXISTING=$(get "/admin/cms/pages" "$ADMIN_TOKEN")
FIRST_ID=$(echo "$EXISTING" | python3 -c "
import sys,json
d=json.load(sys.stdin)['data']
items = d if isinstance(d, list) else d.get('items', [])
print(items[0]['id'] if items else '')
" 2>/dev/null || true)
if [ -n "$FIRST_ID" ]; then
  R=$(post_json "/admin/cms/pages/$FIRST_ID/translations/zh-CN/publish" '{}' "$ADMIN_TOKEN")
  VERSION_ID=$(get_field "$R" "versionNumber")
  if [ -n "$VERSION_ID" ]; then
    R=$(post_json "/admin/cms/pages/$FIRST_ID/translations/zh-CN/rollback" "{\"versionNumber\":$VERSION_ID}" "$ADMIN_TOKEN")
    assert_code "B10 回滚版本" "0" "$R"
  fi
fi

echo "--- B12: 区块定义 ---"
R=$(get "/admin/cms/block-definitions" "$ADMIN_TOKEN")
assert_code "B12 区块定义" "0" "$R"

echo "--- B13-B14: 公开 CMS API ---"
R=$(get "/api/cms/pages/render?path=/" "")
assert_code "B13 前台渲染" "0" "$R"

R=$(get "/api/cms/pages/list" "")
assert_code "B14 前台页面列表" "0" "$R"

echo "--- B15-B18: DDD CMS API ---"
R=$(get "/api/ddd/cms/pages" "$ADMIN_TOKEN")
assert_code "B15 DDD 列表" "0" "$R"

DDD_SLUG="/ddd-e2e-$RANDOM"
R=$(post_json "/api/ddd/cms/pages" '{"slug":"'$DDD_SLUG'","pageType":"custom","title":"DDD E2E","defaultLocale":"zh-CN"}' "$ADMIN_TOKEN")
assert_code "B16 DDD 创建" "0" "$R"
DDD_ID=$(get_field "$R" "id")

if [ -n "$DDD_ID" ]; then
  R=$(get "/api/ddd/cms/pages/$DDD_ID" "$ADMIN_TOKEN")
  assert_code "B17 DDD 详情" "0" "$R"

  R=$(delete "/api/ddd/cms/pages/$DDD_ID" "$ADMIN_TOKEN")
  assert_code "B18 DDD 删除" "0" "$R"
fi

summary "L2-B CMS"
