create table cms_page (
  id bigserial primary key,
  slug varchar(255) not null unique,
  page_type varchar(64) not null default 'custom',
  default_locale varchar(16) not null default 'zh-CN',
  status varchar(32) not null default 'enabled',
  created_by bigint references admin_user(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table cms_page_version (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  version_no integer not null,
  content_json jsonb not null,
  snapshot_json jsonb,
  created_by bigint references admin_user(id),
  created_at timestamptz not null default now(),
  remark varchar(255),
  unique (page_id, locale, version_no)
);

create table cms_page_translation (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  title varchar(255) not null,
  seo_title varchar(255),
  seo_description text,
  seo_keywords varchar(512),
  draft_version_id bigint references cms_page_version(id),
  published_version_id bigint references cms_page_version(id),
  status varchar(32) not null default 'draft',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (page_id, locale)
);

create table cms_block_definition (
  id bigserial primary key,
  type varchar(64) not null unique,
  name varchar(64) not null,
  schema_json jsonb not null,
  default_props_json jsonb not null default '{}'::jsonb,
  enabled boolean not null default true,
  sort_order integer not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table cms_publish_record (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  version_id bigint not null references cms_page_version(id),
  operator_id bigint references admin_user(id),
  published_at timestamptz not null default now(),
  remark varchar(255)
);

create index idx_cms_page_slug on cms_page(slug);
create index idx_cms_page_translation_locale on cms_page_translation(locale);
create index idx_cms_page_version_page_locale on cms_page_version(page_id, locale);
