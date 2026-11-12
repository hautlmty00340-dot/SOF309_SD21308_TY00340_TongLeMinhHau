package com.poly.sof309.service;

import com.poly.sof309.dto.LoginRequest;
import com.poly.sof309.dto.RegisterRequest;
import com.poly.sof309.model.Account;
import com.poly.sof309.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {
    private static final String EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String PHONE = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$";
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    public Account register(RegisterRequest request) {
        String contact = normalizeContact(request.contact());
        if (!isValidContact(contact)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email hoặc số điện thoại chưa đúng định dạng");
        if (!request.acceptTerms()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng đồng ý với điều khoản để tiếp tục");
        if (!request.password().equals(request.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận chưa khớp");
        if (accounts.existsByContactIgnoreCase(contact)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email hoặc số điện thoại đã được đăng ký");
        return accounts.save(new Account(request.fullName().trim(), contact, passwordEncoder.encode(request.password())));
    }

    public Account authenticate(LoginRequest request) {
        String contact = normalizeContact(request.identifier());
        Account account = accounts.findByContactIgnoreCase(contact)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không chính xác"));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không chính xác");
        }
        return account;
    }

    public static String normalizeContact(String contact) { return contact.trim().toLowerCase(Locale.ROOT); }
    public static boolean isValidContact(String contact) { return contact.matches(EMAIL) || contact.matches(PHONE); }
    public static Map<String, Object> publicAccount(Account account) {
        return Map.of("id", account.getId(), "fullName", account.getFullName(), "contact", account.getContact());
    }
}
