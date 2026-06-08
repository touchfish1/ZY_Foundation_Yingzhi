-- Performance indexes for common query patterns

-- CMS: filter by page_type + status (used in public rendering and admin listing)
CREATE INDEX IF NOT EXISTS idx_cms_page_type_status ON cms_page(page_type, status);

-- Orders: sort by created_at (used in admin order listing)
CREATE INDEX IF NOT EXISTS idx_order_main_created_at ON order_main(created_at DESC);

-- CMS versions: find latest version by page + locale (used in version management)
CREATE INDEX IF NOT EXISTS idx_cms_page_version_page_locale_no
    ON cms_page_version(page_id, locale, version_no DESC);

-- Payment transactions: sort by created_at (used in admin payment listing)
CREATE INDEX IF NOT EXISTS idx_payment_transaction_created_at ON payment_transaction(created_at DESC);
