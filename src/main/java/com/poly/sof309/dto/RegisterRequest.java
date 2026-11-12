package com.poly.sof309.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự") String fullName,
        @NotBlank(message = "Email hoặc số điện thoại không được để trống") String contact,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") String password,
        @NotBlank(message = "Vui lòng xác nhận mật khẩu") String confirmPassword,
        boolean acceptTerms) { }
