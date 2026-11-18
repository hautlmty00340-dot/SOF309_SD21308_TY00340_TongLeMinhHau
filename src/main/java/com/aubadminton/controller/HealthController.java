package com.aubadminton.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> apiRoot() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "AU Badminton API");
        response.put("version", "2.8.0");
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "GET  /api/health");
        endpoints.put("courts", "GET  /api/courts            — Danh sách sân");
        endpoints.put("court", "GET  /api/courts/:id        — Chi tiết sân");
        endpoints.put("addCourt", "POST /api/courts            — Thêm sân (Admin)");
        endpoints.put("iotCtrl", "PATCH /api/courts/:id/iot  — Điều khiển đèn");
        endpoints.put("bookings", "GET  /api/bookings          — Danh sách đặt sân");
        endpoints.put("book", "POST /api/bookings          — Đặt sân mới");
        endpoints.put("status", "PATCH /api/bookings/:id/status");
        endpoints.put("stats", "GET  /api/bookings/stats/summary");
        endpoints.put("settings", "GET  /api/settings/public — Thông tin cơ sở");
        response.put("endpoints", endpoints);
        return response;
    }

    @GetMapping("/version")
    public Map<String, Object> version() {
        return Map.of("success", true, "version", "2.8.0", "site", "AU Badminton");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("success", true);
            response.put("status", "OK");
            response.put("database", "AuDB connected (Java JDBC)");
            response.put("timestamp", LocalDateTime.now().toString());
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "ERROR");
            response.put("message", "Không kết nối được cơ sở dữ liệu");
        }
        return response;
    }
}