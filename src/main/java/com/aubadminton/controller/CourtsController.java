package com.aubadminton.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courts")
public class CourtsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && "admin".equals(session.getAttribute("role"));
    }
    private static ResponseEntity<Map<String,Object>> denied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Chỉ quản trị viên được thực hiện thao tác này"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCourts() {
        Map<String, Object> response = new HashMap<>();
        try {
            String sql = "SELECT " +
                    "  c.court_id, " +
                    "  c.name, " +
                    "  c.court_type, " +
                    "  c.iot_light_status, " +
                    "  c.price_per_hour, " +
                    "  CASE " +
                    "    WHEN EXISTS (" +
                    "      SELECT 1 FROM bookings b " +
                    "      WHERE b.court_id = c.court_id " +
                    "        AND b.date = CAST(GETDATE() AS DATE) " +
                    "        AND b.status = 'CONFIRMED' " +
                    "        AND b.start_time <= CAST(GETDATE() AS TIME) " +
                    "        AND b.end_time > CAST(GETDATE() AS TIME) " +
                    "    ) THEN 'OCCUPIED' " +
                    "    ELSE 'AVAILABLE' " +
                    "  END AS live_status " +
                    "FROM courts c " +
                    "ORDER BY c.court_id";
            List<Map<String, Object>> courts = jdbcTemplate.queryForList(sql);
            response.put("success", true);
            response.put("data", courts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý sân, vui lòng kiểm tra máy chủ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourtById(@PathVariable("id") int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> courts = jdbcTemplate.queryForList("SELECT * FROM courts WHERE court_id = ?", id);
            if (courts.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy sân");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<Map<String, Object>> bookingsToday = jdbcTemplate.queryForList(
                    "SELECT start_time, end_time, status FROM bookings " +
                            "WHERE court_id = ? AND date = CAST(GETDATE() AS DATE) AND status IN ('PENDING','CONFIRMED') " +
                            "ORDER BY start_time",
                    id);

            Map<String, Object> data = new HashMap<>();
            data.put("court", courts.get(0));
            data.put("bookingsToday", bookingsToday);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý sân, vui lòng kiểm tra máy chủ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private static BigDecimal parsePrice(Object price) {
        if (price == null) throw new IllegalArgumentException("Thiếu giá thuê");
        try {
            BigDecimal value = new BigDecimal(price.toString().trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("10000000")) > 0 || value.scale() > 2)
                throw new IllegalArgumentException("Giá thuê phải lớn hơn 0, không vượt quá 10.000.000đ/giờ");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá thuê phải là số hợp lệ");
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addCourt(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (!isAdmin(req)) return denied();
        Map<String, Object> response = new HashMap<>();
        String name = (String) body.get("name");
        String courtType = (String) body.get("court_type");
        Object iotStatusObj = body.get("iot_light_status");

        if (name == null || courtType == null) {
            response.put("success", false);
            response.put("message", "Thiếu tên sân hoặc loại sân");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            if (name.isBlank() || courtType.isBlank() || name.length() > 100 || courtType.length() > 50)
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên hoặc loại sân không hợp lệ"));
            BigDecimal price = parsePrice(body.get("price_per_hour"));
            int iotStatus = iotStatusObj != null ? Integer.parseInt(iotStatusObj.toString()) : 0;
            if (iotStatus < 0 || iotStatus > 2)
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Đèn phải là 0, 1 hoặc 2"));
            Long courtId = jdbcTemplate.queryForObject(
                    "INSERT INTO courts (name, court_type, iot_light_status, price_per_hour) OUTPUT INSERTED.court_id VALUES (?, ?, ?, ?)",
                    Long.class, name.trim(), courtType.trim(), iotStatus, price);

            response.put("success", true);
            response.put("court_id", courtId);
            response.put("message", "Thêm sân thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "Tên sân đã tồn tại"));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý sân, vui lòng kiểm tra máy chủ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourt(@PathVariable("id") int id,
            @RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (!isAdmin(req)) return denied();
        Map<String, Object> response = new HashMap<>();
        String name = (String) body.get("name");
        String courtType = (String) body.get("court_type");
        Object iotStatusObj = body.get("iot_light_status");

        try {
            if (name == null || name.isBlank() || name.length() > 100 ||
                    courtType == null || courtType.isBlank() || courtType.length() > 50 || iotStatusObj == null)
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thông tin sân không hợp lệ"));
            BigDecimal price = parsePrice(body.get("price_per_hour"));
            int iotStatus = Integer.parseInt(iotStatusObj.toString());
            if (iotStatus < 0 || iotStatus > 2) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Đèn phải là 0, 1 hoặc 2"));
            int updated = jdbcTemplate.update("UPDATE courts SET name=?, court_type=?, iot_light_status=?, price_per_hour=? WHERE court_id=?",
                    name.trim(), courtType.trim(), iotStatus, price, id);
            if (updated == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Không tìm thấy sân"));
            response.put("success", true);
            response.put("message", "Cập nhật sân thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", "Tên sân đã tồn tại"));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý sân, vui lòng kiểm tra máy chủ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{id}/iot")
    public ResponseEntity<Map<String, Object>> updateIot(@PathVariable("id") int id,
            @RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (!isAdmin(req)) return denied();
        Map<String, Object> response = new HashMap<>();
        Object iotStatusObj = body.get("iot_light_status");

        if (iotStatusObj == null) {
            response.put("success", false);
            response.put("message", "Giá trị IoT không hợp lệ (0,1,2)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            int iotStatus = Integer.parseInt(iotStatusObj.toString());
            if (iotStatus < 0 || iotStatus > 2)
                throw new IllegalArgumentException();

            int changed = jdbcTemplate.update("UPDATE courts SET iot_light_status = ? WHERE court_id = ?", iotStatus, id);
            if (changed == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy sân"));

            String[] labels = { "Tắt", "Bật thủ công", "Tự động theo lịch" };
            response.put("success", true);
            response.put("message", "Đã lưu trạng thái đèn sân " + id + ": " + labels[iotStatus]);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Giá trị IoT không hợp lệ hoặc lỗi server");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourt(@PathVariable("id") int id, HttpServletRequest req) {
        if (!isAdmin(req)) return denied();
        Map<String, Object> response = new HashMap<>();
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bookings WHERE court_id=? AND status IN ('PENDING','CONFIRMED')",
                    Integer.class, id);

            if (count != null && count > 0) {
                response.put("success", false);
                response.put("message", "Sân đang có lịch đặt. Hủy lịch trước khi xóa sân.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Integer anyBookings = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bookings WHERE court_id = ?", Integer.class, id);
            if (anyBookings != null && anyBookings > 0) {
                response.put("success", false);
                response.put("message", "Sân đã có lịch sử đặt, không thể xóa để tránh mất dữ liệu. Hãy ẩn sân khi bổ sung chức năng ngừng hoạt động.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            int deleted = jdbcTemplate.update("DELETE FROM courts WHERE court_id = ?", id);
            if (deleted == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Không tìm thấy sân"));
            response.put("success", true);
            response.put("message", "Xóa sân thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý sân, vui lòng kiểm tra máy chủ");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, Object>> suggestCourtsAI(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String prompt = body.get("prompt");

        if (prompt == null || prompt.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập nhu cầu của bạn!");
            return ResponseEntity.badRequest().body(response);
        }

        prompt = prompt.toLowerCase();
        String sql = "SELECT * FROM courts"; // Mặc định lấy tất cả
        String aiMessage = "Đây là các sân phù hợp với nhu cầu của bạn:";

        try {

            if (prompt.contains("vip") || prompt.contains("xịn") || prompt.contains("giải") || prompt.contains("chuyên nghiệp")) {
                sql = "SELECT * FROM courts WHERE court_type LIKE '%VIP%'";
                aiMessage = "Dựa vào yêu cầu của bạn, Hệ thống đã lọc những sân VIP phù hợp với nhu cầu của bạn.";
            } else if (prompt.contains("rẻ") || prompt.contains("tiết kiệm") || prompt.contains("sinh viên")) {
                sql = "SELECT * FROM courts ORDER BY price_per_hour ASC";
                aiMessage = "Danh sách sân được sắp xếp theo giá thuê, từ thấp đến cao.";
            } else if (prompt.contains("tự động") || prompt.contains("thông minh") || prompt.contains("iot")) {
                sql = "SELECT * FROM courts WHERE iot_light_status = 2";
                aiMessage = "Các sân có chế độ đèn tự động theo lịch.";
            }

            List<Map<String, Object>> suggestedCourts = jdbcTemplate.queryForList(sql);
            
            response.put("success", true);
            response.put("data", suggestedCourts);
            response.put("ai_message", aiMessage);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không xử lý được yêu cầu gợi ý sân");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}