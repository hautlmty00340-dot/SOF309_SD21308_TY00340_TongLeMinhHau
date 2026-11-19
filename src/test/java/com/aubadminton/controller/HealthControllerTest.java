package com.aubadminton.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho HealthController, đối chiếu docs/TEST_CASES.md (W1-ENV-03).
 */
class HealthControllerTest {

    private JdbcTemplate db;
    private HealthController controller;

    @BeforeEach
    void setUp() {
        db = mock(JdbcTemplate.class);
        controller = new HealthController(db);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("W1-ENV-03: /api/health trả OK khi DB kết nối được")
    void testHealthReturnsOkWhenDatabaseIsUp() {
        when(db.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        Map<String, Object> response = controller.health();
        assertEquals(Boolean.TRUE, response.get("success"));
        assertEquals("OK", response.get("status"));
        assertNotNull(response.get("database"));
        assertNotNull(response.get("timestamp"));
    }

    @Test
    @DisplayName("/api/health trả ERROR khi mất kết nối DB")
    void testHealthReturnsErrorWhenDatabaseFails() {
        when(db.queryForObject("SELECT 1", Integer.class)).thenThrow(new RuntimeException("connection refused"));
        Map<String, Object> response = controller.health();
        assertEquals(Boolean.FALSE, response.get("success"));
        assertEquals("ERROR", response.get("status"));
    }
}
