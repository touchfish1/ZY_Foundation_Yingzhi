-- Nacos 需要独立的数据库
SELECT 'CREATE DATABASE nacos' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'nacos')\gexec
