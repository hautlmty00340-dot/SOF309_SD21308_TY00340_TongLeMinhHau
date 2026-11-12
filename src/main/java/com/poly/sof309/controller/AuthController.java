package com.poly.sof309.controller;

import com.poly.sof309.dto.LoginRequest;
import com.poly.sof309.dto.RegisterRequest;
import com.poly.sof309.model.Account;
import com.poly.sof309.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        Account account = authService.register(request);
        session.setAttribute("accountId", account.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthService.publicAccount(account));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Account account = authService.authenticate(request);
        session.setAttribute("accountId", account.getId());
        return AuthService.publicAccount(account);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpSession session, com.poly.sof309.repository.AccountRepository accounts) {
        Object id = session.getAttribute("accountId");
        if (!(id instanceof Long accountId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accounts.findById(accountId).map(account -> ResponseEntity.ok(AuthService.publicAccount(account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
