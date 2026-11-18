package com.poly.sof309.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.sof309.controller.AuthController;
import com.poly.sof309.dto.LoginRequest;
import com.poly.sof309.dto.RegisterRequest;
import com.poly.sof309.model.Account;
import com.poly.sof309.repository.AccountRepository;
import com.poly.sof309.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean AccountRepository accountRepository;

    @BeforeEach
    void resetMocks() { reset(authService, accountRepository); }

    @ParameterizedTest(name = "Từ chối đăng ký khi họ tên='{0}', mật khẩu='{1}'")
    @CsvSource({
            "' ', ShopPulse8",
            "A, ShopPulse8",
            "Nguyen Van An, ' '",
            "Nguyen Van An, short"
    })
    @DisplayName("TC_AUTH_REG_VALIDATION: Controller từ chối dữ liệu đăng ký ngoài biên")
    void registerRejectsInvalidNameOrPassword(String fullName, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(fullName, "qa@example.com", password, password, true);
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("TC_AUTH_REG_SUCCESS: Đăng ký thành công tạo session và không trả mật khẩu")
    void registerSuccessCreatesSession() throws Exception {
        Account account = account();
        when(authService.register(any(RegisterRequest.class))).thenReturn(account);
        RegisterRequest request = new RegisterRequest("Nguyen Van An", "qa@example.com", "ShopPulse8", "ShopPulse8", true);

        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.contact").value("qa@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(request().sessionAttribute("accountId", 17L));
    }

    @Test
    @DisplayName("TC_AUTH_LOGIN_SUCCESS: Đăng nhập thành công lưu ID tài khoản vào session")
    void loginSuccessCreatesSession() throws Exception {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(account());
        LoginRequest request = new LoginRequest("qa@example.com", "ShopPulse8");

        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van An"))
                .andExpect(request().sessionAttribute("accountId", 17L));
    }

    private Account account() {
        Account account = new Account("Nguyen Van An", "qa@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(account, "id", 17L);
        return account;
    }
}
