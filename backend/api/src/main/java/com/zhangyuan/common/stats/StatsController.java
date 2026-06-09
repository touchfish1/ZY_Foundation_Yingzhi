package com.zhangyuan.common.stats;

import com.zhangyuan.common.response.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    private final JdbcTemplate jdbcTemplate;

    public StatsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Long totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_user", Long.class);
        Long totalOrders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_main", Long.class);
        Long totalRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM order_main WHERE status = 'paid'", Long.class);
        Long totalSubscriptions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_subscription WHERE status = 'active'", Long.class);
        Long totalFiles = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM asset_file", Long.class);

        return ApiResponse.ok(Map.of(
                "totalUsers", totalUsers,
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue,
                "totalSubscriptions", totalSubscriptions,
                "totalFiles", totalFiles
        ));
    }

    @GetMapping("/revenue/daily")
    public ApiResponse<Map<String, Object>> dailyRevenue() {
        var rows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) as day, SUM(amount) as revenue, COUNT(*) as orders " +
                "FROM order_main WHERE status IN ('paid', 'fulfilled') " +
                "GROUP BY DATE(created_at) ORDER BY day DESC LIMIT 30");
        return ApiResponse.ok(Map.of("daily", rows));
    }

    @GetMapping("/usage/daily")
    public ApiResponse<Map<String, Object>> dailyUsage() {
        try {
            var rows = jdbcTemplate.queryForList(
                    "SELECT date, SUM(total_calls) as calls, SUM(total_tokens_in) as tokens_in, " +
                    "SUM(total_tokens_out) as tokens_out, SUM(total_cost) as cost " +
                    "FROM usage_daily_summary GROUP BY date ORDER BY date DESC LIMIT 30");
            return ApiResponse.ok(Map.of("daily", rows));
        } catch (Exception e) {
            return ApiResponse.ok(Map.of("daily", java.util.Collections.emptyList()));
        }
    }
}
