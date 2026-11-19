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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AuthController, đối chiếu quy tắc nghiệp vụ mô tả trong docs/TEST_CASES.md
 * (W1-AUTH-01..06). Dùng Mockito để giả lập JdbcTemplate, không cần SQL Server thật.
 */
class AuthControllerTest {

    private JdbcTemplate db;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        db = mock(JdbcTemplate.class);
        controller = new AuthController(db);
        // Mặc định: chưa có username/phone trùng, mọi lệnh ghi đều thành công.
        when(db.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(0);
        when(db.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    private Map<String, String> registerBody(String fullname, String username, String password, String phone) {
        Map<String, String> body = new HashMap<>();
        body.put("fullname", fullname);
        body.put("username", username);
        body.put("password", password);
        body.put("phone", phone);
        return body;
    }

    @ParameterizedTest(name = "[{index}] fullname=''{0}'' username=''{1}'' password=''{2}'' phone=''{3}'' -> {4}")
    @DisplayName("W1-AUTH-02: Validation đăng ký với các bộ dữ liệu biên")
    @CsvSource({
            "A, validUser, Pass@1234, 0987654321, 400",          // fullname 1 ký tự: dưới biên dưới (2)
            "Valid Name, ab, Pass@1234, 0987654321, 400",        // username 2 ký tự: dưới biên dưới (3)
            "Valid Name, validUser, short1, 0987654321, 400",    // password 6 ký tự: dưới biên dưới (8)
            "Valid Name, validUser, Pass@1234, 123, 400",        // phone 3 số: dưới biên dưới (9)
            "Valid Name, validUser, Pass@1234, 0987654321, 201"  // tất cả hợp lệ: đúng biên dưới username (10 ký tự) -> tạo thành công
    })
    void testRegisterValidation(String fullname, String username, String password, String phone, int expectedStatus) {
        ResponseEntity<Map<String, Object>> response = controller.register(registerBody(fullname, username, password, phone));
        assertEquals(expectedStatus, response.getStatusCode().value());
        assertEquals(expectedStatus == 201, response.getBody().get("success"));
    }

    @Test
    @DisplayName("W1-AUTH-03: Đăng ký trùng username/số điện thoại -> 409 Conflict")
    void testRegisterDuplicateReturnsConflict() {
        when(db.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(1);
        ResponseEntity<Map<String, Object>> response = controller.register(
                registerBody("Valid Name", "validUser", "Pass@1234", "0987654321"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
        verify(db, never()).update(contains("INSERT INTO users"), any(Object[].class));
    }

    @Test
    @DisplayName("W1-AUTH-01: Đăng ký hợp lệ -> 201 và không bị từ chối trùng lặp")
    void testRegisterSuccess() {
        ResponseEntity<Map<String, Object>> response = controller.register(
                registerBody("Nguyen Van A", "nguyenvana", "Pass@1234", "0987654321"));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("success"));
    }

    @Test
    @DisplayName("Đăng nhập thiếu username hoặc mật khẩu -> 400")
    void testLoginMissingFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.login(Map.of("username", ""), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("W1-AUTH-05: Đăng nhập với tài khoản không tồn tại -> 401")
    void testLoginUserNotFound() {
        when(db.queryForList(anyString(), eq("ghost"))).thenReturn(List.of());
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.login(
                Map.of("username", "ghost", "password", "whatever1"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("W1-AUTH-05: Đăng nhập sai mật khẩu -> 401")
    void testLoginWrongPassword() {
        Map<String, Object> stored = new HashMap<>();
        stored.put("id", 1);
        stored.put("fullname", "Nguyen Van A");
        stored.put("username", "nguyenvana");
        stored.put("password", "secret123"); // hash kiểu cũ (không tiền tố pbkdf2$) để so khớp trực tiếp
        stored.put("role", "user");
        stored.put("phone", "0987654321");
        when(db.queryForList(anyString(), eq("nguyenvana"))).thenReturn(List.of(stored));
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.login(
                Map.of("username", "nguyenvana", "password", "wrongpass"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("W1-AUTH-04: Đăng nhập đúng mật khẩu -> 200 và tạo session")
    void testLoginSuccessCreatesSession() {
        Map<String, Object> stored = new HashMap<>();
        stored.put("id", 1);
        stored.put("fullname", "Nguyen Van A");
        stored.put("username", "nguyenvana");
        stored.put("password", "secret123");
        stored.put("role", "user");
        stored.put("phone", "0987654321");
        when(db.queryForList(anyString(), eq("nguyenvana"))).thenReturn(List.of(stored));
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.login(
                Map.of("username", "nguyenvana", "password", "secret123"), request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(request.getSession(false));
        assertEquals("user", request.getSession(false).getAttribute("role"));
    }

    @Test
    @DisplayName("W1-AUTH-06: Chưa đăng nhập gọi /api/auth/me -> 401")
    void testMeWithoutSessionReturnsUnauthorized() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<Map<String, Object>> response = controller.me(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("W1-AUTH-06: Đăng xuất hủy session, /api/auth/me sau đó trả 401")
    void testLogoutInvalidatesSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_id", 1);
        request.setSession(session);

        Map<String, Object> result = controller.logout(request);
        assertEquals(Boolean.TRUE, result.get("success"));
        assertTrue(session.isInvalid());

        ResponseEntity<Map<String, Object>> me = controller.me(request);
        assertEquals(HttpStatus.UNAUTHORIZED, me.getStatusCode());
    }
}
