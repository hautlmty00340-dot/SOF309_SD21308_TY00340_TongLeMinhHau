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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho BookingsController, đối chiếu docs/TEST_CASES.md (W1-BOOK-01..06, W1-ADMIN-02/03).
 */
class BookingsControllerTest {

    private static final String FUTURE_DATE = LocalDate.now().plusYears(1).toString();

    private JdbcTemplate db;
    private SettingsController settings;
    private BookingsController controller;

    @BeforeEach
    void setUp() {
        db = mock(JdbcTemplate.class);
        settings = mock(SettingsController.class);
        controller = new BookingsController(db, settings);

        when(settings.values()).thenReturn(Map.of(
                "open_time", "06:00", "close_time", "23:00", "auto_approve", "false"));
        Map<String, Object> courtRow = new HashMap<>();
        courtRow.put("court_id", 1);
        courtRow.put("price_per_hour", new BigDecimal("100000"));
        when(db.queryForList(anyString(), eq(1))).thenReturn(List.of(courtRow));
        when(db.queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any())).thenReturn(0);
        when(db.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(100L);
    }

    private MockHttpServletRequest userRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_id", 1);
        session.setAttribute("role", "user");
        session.setAttribute("phone", "0987654321");
        session.setAttribute("fullname", "Nguyen Van A");
        request.setSession(session);
        return request;
    }

    private MockHttpServletRequest adminRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_id", 2);
        session.setAttribute("role", "admin");
        request.setSession(session);
        return request;
    }

    private Map<String, Object> bookingBody(String courtId, String date, String start, String end) {
        Map<String, Object> body = new HashMap<>();
        body.put("court_id", courtId);
        body.put("date", date);
        body.put("start_time", start);
        body.put("end_time", end);
        body.put("payment_method", "CASH");
        return body;
    }

    @Test
    @DisplayName("W1-BOOK: Chưa đăng nhập không được đặt sân -> 401")
    void testCreateBookingRequiresLogin() {
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", FUTURE_DATE, "10:00", "11:00"), new MockHttpServletRequest());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("W1-BOOK-05: Tài khoản admin đặt sân bị từ chối -> 403")
    void testCreateBookingAdminForbidden() {
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", FUTURE_DATE, "10:00", "11:00"), adminRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @ParameterizedTest(name = "[{index}] {0} {1}-{2} -> {3}")
    @DisplayName("W1-BOOK-02/03: Validation thời gian đặt sân với các giá trị biên")
    @CsvSource({
            "10:00, 09:00, 400",  // giờ kết thúc trước giờ bắt đầu
            "10:00, 10:10, 400",  // 10 phút: dưới biên dưới (30 phút)
            "08:00, 21:00, 400",  // 780 phút: vượt biên trên (720 phút)
    })
    void testCreateBookingInvalidDuration(String start, String end, int expectedStatus) {
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", FUTURE_DATE, start, end), userRequest());
        assertEquals(expectedStatus, response.getStatusCode().value());
    }

    @Test
    @DisplayName("W1-BOOK-03: Đặt sân trong quá khứ -> 400")
    void testCreateBookingInThePast() {
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", "2020-01-01", "10:00", "11:00"), userRequest());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("W1-BOOK-01: Đặt sân hợp lệ -> 201, trả booking_id và court_amount")
    void testCreateBookingSuccess() {
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", FUTURE_DATE, "10:00", "11:00"), userRequest());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(100L, response.getBody().get("booking_id"));
        assertNotNull(response.getBody().get("court_amount"));
    }

    @Test
    @DisplayName("W1-BOOK-04: Trùng lịch -> 409 Conflict")
    void testCreateBookingConflict() {
        when(db.queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any())).thenReturn(1);
        ResponseEntity<Map<String, Object>> response = controller.createBooking(
                bookingBody("1", FUTURE_DATE, "10:00", "11:00"), userRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Exception testing: giờ mở cửa cấu hình sai định dạng làm lộ lỗi chưa được bắt (bug thật)")
    void testCreateBookingThrowsWhenOpeningHoursAreMisconfigured() {
        // Phát hiện AI: đoạn parse open_time/close_time trong BookingsController nằm NGOÀI khối try-catch,
        // nên nếu app_settings bị cấu hình sai định dạng giờ, request sẽ văng lỗi 500 không kiểm soát
        // thay vì trả JSON lỗi như các nhánh khác. Ghi nhận trong docs/ai-logs/02_junit_script_ai.md.
        when(settings.values()).thenReturn(Map.of(
                "open_time", "not-a-time", "close_time", "23:00", "auto_approve", "false"));
        assertThrows(DateTimeParseException.class, () -> controller.createBooking(
                bookingBody("1", FUTURE_DATE, "10:00", "11:00"), userRequest()));
    }

    @Test
    @DisplayName("Validation trạng thái booking không hợp lệ -> 400")
    void testUpdateStatusInvalidValue() {
        ResponseEntity<Map<String, Object>> response = controller.updateStatus(
                1, Map.of("status", "DONE"), adminRequest());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(db, never()).queryForList(anyString(), eq(1));
    }

    @Test
    @DisplayName("Cập nhật trạng thái đơn không tồn tại -> 404")
    void testUpdateStatusNotFound() {
        when(db.queryForList(anyString(), eq(999))).thenReturn(List.of());
        ResponseEntity<Map<String, Object>> response = controller.updateStatus(
                999, Map.of("status", "CANCELLED"), adminRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("W1-ADMIN-03: Khách hàng không được tự xác nhận booking của mình -> 403")
    void testCustomerCannotConfirmOwnBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("booking_id", 1);
        booking.put("status", "PENDING");
        booking.put("customer_phone", "0987654321");
        when(db.queryForList(anyString(), eq(1))).thenReturn(List.of(booking));

        ResponseEntity<Map<String, Object>> response = controller.updateStatus(
                1, Map.of("status", "CONFIRMED"), userRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(db, never()).update(anyString(), eq("CONFIRMED"), eq(1), eq("PENDING"));
    }

    @Test
    @DisplayName("W1-ADMIN-02: Admin xác nhận booking PENDING -> CONFIRMED -> 200")
    void testAdminConfirmsBooking() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("booking_id", 1);
        booking.put("status", "PENDING");
        booking.put("customer_phone", "0987654321");
        when(db.queryForList(anyString(), eq(1))).thenReturn(List.of(booking));
        when(db.update(anyString(), eq("CONFIRMED"), eq(1), eq("PENDING"))).thenReturn(1);

        ResponseEntity<Map<String, Object>> response = controller.updateStatus(
                1, Map.of("status", "CONFIRMED"), adminRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
