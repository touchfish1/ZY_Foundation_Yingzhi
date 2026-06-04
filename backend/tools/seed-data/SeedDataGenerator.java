import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SeedDataGenerator {

    static final String DB_URL = "jdbc:postgresql://100.125.148.23:5432/zhangyuan";
    static final String DB_USER = "zhangyuan";
    static final String DB_PASS = "zhangyuan";

    static final int USER_COUNT = 50;
    static final int ORDER_COUNT = 1000;
    static final int USAGE_PER_USER = 1000;
    static final int AUDIT_PER_USER = 1000; // 1000/用户 = 5万条总日志

    static final ThreadLocalRandom RND = ThreadLocalRandom.current();

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        System.out.println("=== Seed Data Generator ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            // Clean existing data
            cleanData(conn);

            // Ensure all tables exist
            ensureTables(conn);
            conn.commit();
            System.out.println("Tables ensured");

            // Seed users
            long[] userIds = seedUsers(conn);
            conn.commit();
            System.out.println("Users created: " + userIds.length);

            // Seed orders (1000+)
            long[] orderIds = seedOrders(conn, userIds);
            conn.commit();
            System.out.println("Orders created: " + orderIds.length);

            // Payments + subscriptions
            seedPaymentsAndSubscriptions(conn, orderIds, userIds);
            conn.commit();
            System.out.println("Payments + Subscriptions seeded");

            // Balance transactions (x1~x10 per user)
            seedBalances(conn, userIds);
            conn.commit();
            System.out.println("Balance transactions seeded");

            // Usage records (10~1000 per user)
            seedUsageRecords(conn, userIds);
            conn.commit();
            System.out.println("Usage records seeded");

            // Audit logs (10000 per user)
            seedAuditLogs(conn, userIds);
            conn.commit();
            System.out.println("Audit logs seeded");

            System.out.println("\n=== ALL DONE ===");
        }
    }

    static void cleanData(Connection conn) throws SQLException {
        var s = conn.createStatement();
        s.execute("TRUNCATE TABLE audit_log, usage_record, user_subscription, payment_transaction, order_main, balance_transaction, saas_user RESTART IDENTITY CASCADE");
    }

    static void ensureTables(Connection conn) throws SQLException {
        var s = conn.createStatement();
        String[][] ddl = {
            {"saas_user", "id bigserial primary key,email varchar(128) not null unique,password_hash varchar(255) not null,nickname varchar(64),balance decimal(20,8) default 0,concurrency int default 5,rpm_limit int default 0,role varchar(20) default 'user',status varchar(32) default 'active',api_key varchar(64) unique,total_recharged decimal(20,8) default 0,quota_used bigint default 0,quota_limit bigint default 0,notes text,last_login_at timestamptz,created_at timestamptz default now(),updated_at timestamptz default now()"},
            {"balance_transaction", "id bigserial primary key,user_id bigint not null,amount decimal(20,2) not null,balance_after decimal(20,2) not null,transaction_type varchar(32) not null,description text,created_at timestamptz default now()"},
            {"audit_log", "id bigserial primary key,user_id bigint,action varchar(64) not null,resource_type varchar(64),resource_id varchar(128),detail text,ip_address varchar(45),user_agent text,created_at timestamptz default now()"},
            {"order_main", "id bigserial primary key,order_no varchar(64) not null unique,plan_id bigint,price_id bigint,amount decimal(12,2) not null,currency varchar(8) default 'CNY',status varchar(32) default 'PENDING',user_id bigint,snapshot_json jsonb,created_at timestamptz default now(),paid_at timestamptz"},
            {"payment_transaction", "id bigserial primary key,payment_no varchar(64) not null unique,order_id bigint not null,order_no varchar(64) not null,channel varchar(32) not null,amount decimal(12,2) not null,currency varchar(8) default 'CNY',status varchar(32) default 'PENDING',request_json jsonb,callback_json jsonb,created_at timestamptz default now(),paid_at timestamptz"},
            {"user_subscription", "id bigserial primary key,user_id bigint not null,plan_code varchar(64) not null,plan_name varchar(128),status varchar(32) default 'active',starts_at timestamptz default now(),expires_at timestamptz not null,created_at timestamptz default now(),updated_at timestamptz default now()"},
            {"usage_record", "id bigserial primary key,user_id bigint not null,api_key varchar(64),api_path varchar(255),tokens_in bigint default 0,tokens_out bigint default 0,cost decimal(20,8) default 0,duration_ms int default 0,status varchar(16) default 'success',created_at timestamptz default now()"}
        };
        for (String[] t : ddl) {
            s.execute("CREATE TABLE IF NOT EXISTS " + t[0] + " (" + t[1] + ")");
        }
    }

    static long[] seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO saas_user (email, password_hash, nickname, balance, concurrency, role, status, api_key, quota_limit) VALUES (?,?,?,?,?,?,?,?,?)";
        long[] ids = new long[USER_COUNT];
        try (var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < USER_COUNT; i++) {
                stmt.setString(1, "user" + i + "@test.com");
                stmt.setString(2, "$2a$10$dummyhash");
                stmt.setString(3, "测试用户" + i);
                stmt.setBigDecimal(4, BigDecimal.valueOf(RND.nextInt(100, 1000)));
                stmt.setInt(5, RND.nextInt(1, 20));
                stmt.setString(6, "user");
                stmt.setString(7, i == 0 ? "admin" : "active");
                stmt.setString(8, "sk-test-" + UUID.randomUUID().toString().substring(0, 8));
                stmt.setLong(9, RND.nextInt(10000, 100000));
                stmt.addBatch();
            }
            stmt.executeBatch();
            var rs = stmt.getGeneratedKeys();
            for (int i = 0; i < USER_COUNT; i++) { rs.next(); ids[i] = rs.getLong(1); }
        }
        return ids;
    }

    static long[] seedOrders(Connection conn, long[] userIds) throws SQLException {
        String sql = "INSERT INTO order_main (order_no, amount, currency, status, user_id, snapshot_json) VALUES (?,?,?,?,?,?::jsonb)";
        long[] ids = new long[ORDER_COUNT];
        String[] statuses = {"PENDING", "PAID", "PAID", "PAID", "CANCELLED", "PAID"};
        try (var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < ORDER_COUNT; i++) {
                stmt.setString(1, "ORD" + System.currentTimeMillis() + i);
                stmt.setBigDecimal(2, BigDecimal.valueOf(RND.nextInt(50, 500)));
                stmt.setString(3, "CNY");
                stmt.setString(4, statuses[RND.nextInt(statuses.length)]);
                stmt.setLong(5, userIds[RND.nextInt(userIds.length)]);
                stmt.setString(6, "{\"planCode\":\"premium-monthly\",\"planName\":\"高级版月付\",\"billingCycle\":\"monthly\",\"validityDays\":30}");
                stmt.addBatch();
            }
            stmt.executeBatch();
            var rs = stmt.getGeneratedKeys();
            for (int i = 0; i < ORDER_COUNT; i++) { rs.next(); ids[i] = rs.getLong(1); }
        }
        return ids;
    }

    static void seedPaymentsAndSubscriptions(Connection conn, long[] orderIds, long[] userIds) throws SQLException {
        try (var ps = conn.prepareStatement("SELECT id, user_id, order_no FROM order_main WHERE id = ? AND status = 'PAID'");
             var payStmt = conn.prepareStatement("INSERT INTO payment_transaction (payment_no, order_id, order_no, channel, amount, currency, status, paid_at) VALUES (?,?,?,?,?,?,'SUCCESS',now())");
             var subStmt = conn.prepareStatement("INSERT INTO user_subscription (user_id, plan_code, plan_name, status, expires_at) VALUES (?,?,?,'active',now()+interval'30 days')")) {
            for (int i = 0; i < orderIds.length; i++) {
                ps.setLong(1, orderIds[i]);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    long uid = rs.getLong("user_id");
                    String orderNo = rs.getString("order_no");
                    payStmt.setString(1, "PAY" + System.currentTimeMillis() + i);
                    payStmt.setLong(2, orderIds[i]);
                    payStmt.setString(3, orderNo);
                    payStmt.setString(4, "mock");
                    payStmt.setBigDecimal(5, BigDecimal.valueOf(RND.nextInt(50, 500)));
                    payStmt.setString(6, "CNY");
                    payStmt.addBatch();
                    subStmt.setLong(1, uid);
                    subStmt.setString(2, "premium-monthly");
                    subStmt.setString(3, "高级版月付");
                    subStmt.addBatch();
                }
            }
            payStmt.executeBatch();
            subStmt.executeBatch();
        }
    }

    static void seedBalances(Connection conn, long[] userIds) throws SQLException {
        String sql = "INSERT INTO balance_transaction (user_id, amount, balance_after, transaction_type, description) VALUES (?,?,?,?,?)";
        try (var stmt = conn.prepareStatement(sql)) {
            for (long uid : userIds) {
                int times = RND.nextInt(1, 11);
                BigDecimal bal = BigDecimal.ZERO;
                for (int t = 0; t < times; t++) {
                    BigDecimal amt = BigDecimal.valueOf(RND.nextInt(10, 500));
                    bal = bal.add(amt);
                    stmt.setLong(1, uid);
                    stmt.setBigDecimal(2, amt);
                    stmt.setBigDecimal(3, bal);
                    stmt.setString(4, "RECHARGE");
                    stmt.setString(5, "自动充值");
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    static void seedUsageRecords(Connection conn, long[] userIds) throws SQLException {
        String sql = "INSERT INTO usage_record (user_id, api_key, api_path, tokens_in, tokens_out, cost, duration_ms, status) VALUES (?,?,?,?,?,?,?,?)";
        try (var stmt = conn.prepareStatement(sql)) {
            for (long uid : userIds) {
                int count = RND.nextInt(10, 1001);
                for (int t = 0; t < count; t++) {
                    stmt.setLong(1, uid);
                    stmt.setString(2, "sk-test-" + uid);
                    stmt.setString(3, RND.nextBoolean() ? "/v1/chat/completions" : "/v1/embeddings");
                    stmt.setLong(4, (long)RND.nextInt(100, 5000));
                    stmt.setLong(5, (long)RND.nextInt(50, 2000));
                    stmt.setBigDecimal(6, BigDecimal.valueOf(RND.nextDouble() * 0.1));
                    stmt.setInt(7, RND.nextInt(100, 5000));
                    stmt.setString(8, RND.nextInt(20) == 0 ? "error" : "success");
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    static void seedAuditLogs(Connection conn, long[] userIds) throws SQLException {
        String sql = "INSERT INTO audit_log (user_id, action, resource_type, resource_id, detail, ip_address) VALUES (?,?,?,?,?,?)";
        String[] actions = {"LOGIN", "LOGOUT", "API_CALL", "ORDER_CREATED", "PAYMENT", "PROFILE_UPDATE", "API_KEY_REGENERATE"};
        try (var stmt = conn.prepareStatement(sql)) {
            int total = 0, batch = 0;
            for (long uid : userIds) {
                for (int t = 0; t < AUDIT_PER_USER; t++) {
                    String act = actions[RND.nextInt(actions.length)];
                    stmt.setLong(1, uid);
                    stmt.setString(2, act);
                    stmt.setString(3, act.equals("API_CALL") ? "api" : "user");
                    stmt.setString(4, uid + "-" + t);
                    stmt.setString(5, act + " from user " + uid);
                    stmt.setString(6, "192.168.1." + RND.nextInt(255));
                    stmt.addBatch();
                    batch++; total++;
                    if (batch >= 5000) { stmt.executeBatch(); batch = 0; if (total % 100000 == 0) System.out.println("  Audit: " + total); }
                }
            }
            if (batch > 0) stmt.executeBatch();
            System.out.println("Audit logs: " + total);
        }
    }
}
