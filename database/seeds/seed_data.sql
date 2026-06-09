-- ============================================================
-- Seed Data for Development
-- Usage: psql -h localhost -U zhangyuan -d zhangyuan -f seed_data.sql
-- ============================================================

-- Admin users (password: admin123, BCrypt hash)
INSERT INTO admin_user (username, password, nickname, status)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Super Admin', 'enabled')
ON CONFLICT (username) DO NOTHING;

-- Admin roles
INSERT INTO admin_role (code, name) VALUES
    ('super_admin', 'Super Admin'),
    ('admin', 'Admin')
ON CONFLICT (code) DO NOTHING;

-- Assign super_admin role to admin user
INSERT INTO admin_user_role (user_id, role_id)
SELECT u.id, r.id FROM admin_user u, admin_role r
WHERE u.username = 'admin' AND r.code = 'super_admin'
ON CONFLICT DO NOTHING;

-- Permissions
INSERT INTO admin_permission (code, name, module) VALUES
    ('product:manage', 'Product Management', 'product'),
    ('order:list', 'Order Listing', 'order'),
    ('payment:list', 'Payment Listing', 'payment'),
    ('cms:manage', 'CMS Management', 'cms'),
    ('asset:manage', 'Asset Management', 'asset'),
    ('system:user:list', 'User List', 'system'),
    ('system:role:list', 'Role List', 'system'),
    ('system:permission:list', 'Permission List', 'system'),
    ('system:menu:list', 'Menu List', 'system'),
    ('system:log:list', 'Log List', 'system'),
    ('system:operation-log', 'Operation Log', 'system'),
    ('system:access-log', 'Access Log', 'system')
ON CONFLICT (code) DO NOTHING;

-- Assign all permissions to super_admin
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r, admin_permission p
WHERE r.code = 'super_admin'
ON CONFLICT DO NOTHING;

-- Product plan groups
INSERT INTO product_plan_group (code, name, description, sort_order) VALUES
    ('ai-api', 'AI API Plans', 'Access to various AI models via unified API', 1),
    ('dedicated', 'Dedicated Plans', 'Dedicated compute and private deployments', 2)
ON CONFLICT (code) DO NOTHING;

-- Plans in ai-api group
INSERT INTO product_plan (group_id, code, name, description, badge, sort_order)
SELECT pg.id, 'trial', 'Free Trial', 'Free trial with limited access', 'Free', 1
FROM product_plan_group pg WHERE pg.code = 'ai-api'
ON CONFLICT (code) DO NOTHING;

INSERT INTO product_plan (group_id, code, name, description, badge, sort_order)
SELECT pg.id, 'basic', 'Basic', 'Basic plan for individual developers', '¥99', 2
FROM product_plan_group pg WHERE pg.code = 'ai-api'
ON CONFLICT (code) DO NOTHING;

INSERT INTO product_plan (group_id, code, name, description, badge, sort_order)
SELECT pg.id, 'pro', 'Professional', 'Professional plan for teams', '¥299', 3
FROM product_plan_group pg WHERE pg.code = 'ai-api'
ON CONFLICT (code) DO NOTHING;

INSERT INTO product_plan (group_id, code, name, description, badge, sort_order)
SELECT pg.id, 'enterprise', 'Enterprise', 'Enterprise plan for large organizations', '¥999', 4
FROM product_plan_group pg WHERE pg.code = 'ai-api'
ON CONFLICT (code) DO NOTHING;

-- Prices for each plan
-- Trial: free
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'monthly', 0, 0 FROM product_plan p WHERE p.code = 'trial'
ON CONFLICT DO NOTHING;

-- Basic: ¥99/month or ¥999/year
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'monthly', 99, 99 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'yearly', 999, 1188 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;

-- Pro: ¥299/month or ¥2999/year
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'monthly', 299, 299 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'yearly', 2999, 3588 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;

-- Enterprise: ¥999/month or ¥9999/year
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'monthly', 999, 999 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_price (plan_id, currency, billing_cycle, amount, original_amount)
SELECT p.id, 'CNY', 'yearly', 9999, 11988 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;

-- Features per plan
-- Trial features
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Calls', '1,000 / month', true, 1 FROM product_plan p WHERE p.code = 'trial'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Models', 'GPT-3.5 only', true, 2 FROM product_plan p WHERE p.code = 'trial'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Concurrency', '1', true, 3 FROM product_plan p WHERE p.code = 'trial'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Rate Limit', '10 RPM', true, 4 FROM product_plan p WHERE p.code = 'trial'
ON CONFLICT DO NOTHING;

-- Basic features
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Calls', '100,000 / month', true, 1 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Models', 'GPT-3.5, GPT-4', true, 2 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Concurrency', '5', true, 3 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Rate Limit', '60 RPM', true, 4 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Key', '5 keys', true, 5 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Support', 'Email', true, 6 FROM product_plan p WHERE p.code = 'basic'
ON CONFLICT DO NOTHING;

-- Pro features
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Calls', '1,000,000 / month', true, 1 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Models', 'All available models', true, 2 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Concurrency', '20', true, 3 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Rate Limit', '300 RPM', true, 4 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Key', 'Unlimited keys', true, 5 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Support', 'Priority email + chat', true, 6 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Analytics', 'Advanced dashboard', true, 7 FROM product_plan p WHERE p.code = 'pro'
ON CONFLICT DO NOTHING;

-- Enterprise features
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Calls', '10,000,000 / month', true, 1 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Models', 'All models + custom models', true, 2 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Concurrency', '100', true, 3 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Rate Limit', '1000 RPM', true, 4 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'API Key', 'Unlimited keys', true, 5 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Support', '24/7 dedicated support', true, 6 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Analytics', 'Custom dashboard + reports', true, 7 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'SLA', '99.99% uptime guarantee', true, 8 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
INSERT INTO product_feature (plan_id, feature_name, feature_value, included, sort_order)
SELECT p.id, 'Deployment', 'Private deployment option', true, 9 FROM product_plan p WHERE p.code = 'enterprise'
ON CONFLICT DO NOTHING;
