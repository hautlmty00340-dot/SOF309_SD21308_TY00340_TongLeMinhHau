package com.aubadminton.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho SettingsController (cấu hình giờ mở cửa dùng chung trong luồng đặt sân).
 */
class SettingsControllerTest {

    private JdbcTemplate db;
    private SettingsController controller;

    @BeforeEach
    void setUp() {
        db = mock(JdbcTemplate.class);
        controller = new SettingsController(db);
    }

    private MockHttpServletRequest adminRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "admin");
        request.setSession(session);
        return request;
    }

    @Test
    @DisplayName("Chỉ admin được đọc/sửa cấu hình -> 403 nếu không có quyền")
    void testGetRequiresAdmin() {
        ResponseEntity<Map<String, Object>> response = controller.get(new MockHttpServletRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Giờ đóng cửa phải sau giờ mở cửa, nếu không -> 400")
    void testUpdateRejectsClosingBeforeOpening() {
        ResponseEntity<Map<String, Object>> response = controller.update(
                Map.of("open_time", "23:00", "close_time", "06:00"), adminRequest());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Cập nhật cấu hình hợp lệ -> 200")
    void testUpdateSuccess() {
        ResponseEntity<Map<String, Object>> response = controller.update(
                Map.of("open_time", "06:00", "close_time", "22:00", "auto_approve", "true"), adminRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
