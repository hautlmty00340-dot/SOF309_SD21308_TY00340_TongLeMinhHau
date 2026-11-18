package com.aubadminton.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final JdbcTemplate db;
    private static final Map<String, String> DEFAULTS = Map.of(
            "business_name", "AU Badminton", "business_address", "123 Đường Thể Thao, Q.1",
            "support_phone", "", "open_time", "06:00", "close_time", "23:00", "auto_approve", "false");
    public SettingsController(JdbcTemplate db) { this.db = db; }
    public Map<String, String> values() {
        Map<String, String> data = new HashMap<>(DEFAULTS);
        db.query("SELECT setting_key,setting_value FROM app_settings", rs -> {
            if (data.containsKey(rs.getString(1))) data.put(rs.getString(1), rs.getString(2));
        });
        return data;
    }
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicInfo() {
        Map<String, String> all = values();
        Map<String, String> data = new HashMap<>();
        for (String key : Set.of("business_name", "business_address", "support_phone", "open_time", "close_time"))
            data.put(key, all.get(key));
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> get(HttpServletRequest req) {
        if (!admin(req)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không có quyền quản trị"));
        return ResponseEntity.ok(Map.of("success", true, "data", values()));
    }
    @PutMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, String> body, HttpServletRequest req) {
        if (!admin(req)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không có quyền quản trị"));
        Map<String, String> data = values();
        for (String key: DEFAULTS.keySet()) if (body.containsKey(key)) data.put(key, body.get(key) == null ? "" : body.get(key).trim());
        try {
            LocalTime open = LocalTime.parse(data.get("open_time"));
            LocalTime close = LocalTime.parse(data.get("close_time"));
            if (!close.isAfter(open) || !data.get("auto_approve").matches("true|false") ||
                    data.get("business_name").isBlank() || data.get("business_name").length() > 100 ||
                    data.get("business_address").length() > 250 || data.get("support_phone").length() > 24)
                throw new IllegalArgumentException();
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cấu hình chưa hợp lệ")); }
        for (String key: DEFAULTS.keySet()) db.update("UPDATE app_settings SET setting_value=? WHERE setting_key=?", data.get(key), key);
        for (String key: DEFAULTS.keySet()) db.update("IF NOT EXISTS(SELECT 1 FROM app_settings WHERE setting_key=?) INSERT INTO app_settings(setting_key,setting_value) VALUES (?,?)",
                    key, key, data.get(key));
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã lưu cấu hình", "data", data));
    }
    private static boolean admin(HttpServletRequest req) {
        jakarta.servlet.http.HttpSession s = req.getSession(false);
        return s != null && "admin".equals(s.getAttribute("role"));
    }
}
