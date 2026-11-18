package com.poly.sof309.services;

import com.poly.sof309.dto.RegisterRequest;
import com.poly.sof309.repository.AccountRepository;
import com.poly.sof309.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthService authService = new AuthService(accounts, encoder);

    @ParameterizedTest(name = "Contact {0} hợp lệ={1}")
    @CsvSource({
            "qa@example.com, true",
            "0912345678, true",
            "+84912345678, true",
            "qa-invalid, false",
            "0123456789, false",
            "' ', false"
    })
    @DisplayName("TC_AUTH_CONTACT_PARTITION: Phân vùng email và số điện thoại")
    void contactValidation(String contact, boolean expected) {
        assertEquals(expected, AuthService.isValidContact(contact));
    }

    @Test
    @DisplayName("TC_AUTH_DUPLICATE: Ném 409 khi contact đã được đăng ký")
    void registerDuplicateContactThrowsConflict() {
        when(accounts.existsByContactIgnoreCase("qa@example.com")).thenReturn(true);
        RegisterRequest request = validRequest("qa@example.com");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(accounts, never()).save(any());
    }

    @Test
    @DisplayName("TC_AUTH_PASSWORD_MISMATCH: Ném 400 khi mật khẩu xác nhận không khớp")
    void registerMismatchPasswordThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest("Nguyen Van An", "qa@example.com", "ShopPulse8", "OtherPass8", true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(accounts, encoder);
    }

    private RegisterRequest validRequest(String contact) {
        return new RegisterRequest("Nguyen Van An", contact, "ShopPulse8", "ShopPulse8", true);
    }
}
