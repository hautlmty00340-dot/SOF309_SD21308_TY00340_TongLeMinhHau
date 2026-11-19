package com.aubadminton.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho CourtsController, đối chiếu docs/TEST_CASES.md (W1-COURT-01..03, W1-ADMIN-01).
 * JdbcTemplate được inject qua field @Autowired nên phải set bằng ReflectionTestUtils.
 */
class CourtsControllerTest {

    private JdbcTemplate db;
    private CourtsController controller;

    @BeforeEach
    void setUp() {
        db = mock(JdbcTemplate.class);
        controller = new CourtsController();
        ReflectionTestUtils.setField(controller, "jdbcTemplate", db);
    }

    private MockHttpServletRequest adminRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "admin");
        request.setSession(session);
        return request;
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("W1-COURT-01: Danh sách sân trả về đúng dữ liệu mock")
    void testGetCourtsReturnsList() {
        Map<String, Object> court = new HashMap<>();
        court.put("court_id", 1);
        court.put("name", "Sân 1");
        court.put("live_status", "AVAILABLE");
        when(db.queryForList(anyString())).thenReturn(List.of(court));

        ResponseEntity<Map<String, Object>> response = controller.getCourts();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("success"));
        assertEquals(1, ((List<?>) response.getBody().get("data")).size());
    }

    @Test
    @DisplayName("W1-COURT-03: Chi tiết sân không tồn tại -> 404")
    void testGetCourtByIdNotFound() {
        when(db.queryForList(anyString(), eq(999999))).thenReturn(List.of());
        ResponseEntity<Map<String, Object>> response = controller.getCourtById(999999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
    }

    @Test
    @DisplayName("W1-COURT-02: Chi tiết sân hợp lệ trả về court và bookingsToday")
    void testGetCourtByIdFound() {
        Map<String, Object> court = new HashMap<>();
        court.put("court_id", 1);
        court.put("name", "Sân 1");
        when(db.queryForList(anyString(), eq(1))).thenReturn(List.of(court));
        when(db.queryForList(contains("bookings"), eq(1))).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.getCourtById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertTrue(data.containsKey("court"));
        assertTrue(data.containsKey("bookingsToday"));
    }

    @Test
    @DisplayName("W1-ADMIN-01: Thêm sân khi chưa đăng nhập admin -> 403")
    void testAddCourtRequiresAdmin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.addCourt(
                Map.of("name", "Sân mới", "court_type", "Standard", "price_per_hour", "100000"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(db, never()).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any());
    }

    @ParameterizedTest(name = "[{index}] price=''{0}'' -> {1}")
    @DisplayName("Validation giá sân khi thêm sân (admin) với các giá trị biên")
    @CsvSource({
            "0, 400",          // không được bằng 0
            "-50000, 400",     // không được âm
            "abc, 400",        // không phải số
            "10000001, 400",   // vượt trần 10.000.000
            "100000, 201"      // hợp lệ
    })
    void testAddCourtPriceValidation(String price, int expectedStatus) {
        when(db.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any())).thenReturn(1L);
        ResponseEntity<Map<String, Object>> response = controller.addCourt(
                Map.of("name", "Sân mới", "court_type", "Standard", "price_per_hour", price), adminRequest());
        assertEquals(expectedStatus, response.getStatusCode().value());
    }

    @ParameterizedTest(name = "[{index}] iot_light_status={0} -> {1}")
    @DisplayName("Validation trạng thái đèn IoT (0, 1, 2 hợp lệ; ngoài khoảng là lỗi)")
    @CsvSource({
            "-1, 400",
            "3, 400",
            "0, 200",
            "1, 200",
            "2, 200"
    })
    void testUpdateIotValidation(int iotStatus, int expectedStatus) {
        when(db.update(anyString(), anyInt(), anyInt())).thenReturn(1);
        ResponseEntity<Map<String, Object>> response = controller.updateIot(
                1, Map.of("iot_light_status", iotStatus), adminRequest());
        assertEquals(expectedStatus, response.getStatusCode().value());
    }
}
