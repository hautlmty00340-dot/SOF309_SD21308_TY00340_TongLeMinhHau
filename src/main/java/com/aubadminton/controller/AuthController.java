package com.aubadminton.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 120000;
    private final JdbcTemplate db;

    public AuthController(JdbcTemplate db) { this.db = db; }

    private static Map<String, Object> result(boolean success, String message) {
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        r.put("message", message);
        return r;
    }

    private static String safe(String value) { return value == null ? "" : value.trim(); }

    private static byte[] derive(String password, byte[] salt, int rounds) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }

    private static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" +
                Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    private static boolean matchPassword(String password, String stored) throws Exception {
        if (stored == null) return false;
        if (!stored.startsWith("pbkdf2$")) { // Chỉ dùng cho các tài khoản demo cũ; nâng cấp hash sau khi đăng nhập.
            return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), stored.getBytes(StandardCharsets.UTF_8));
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4) return false;
        int rounds = Integer.parseInt(parts[1]);
        if (rounds < 10000 || rounds > 1000000) return false;
        return MessageDigest.isEqual(derive(password, Base64.getDecoder().decode(parts[2]), rounds),
                Base64.getDecoder().decode(parts[3]));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = safe(body.get("username"));
        String password = body.get("password");
        if (username.isEmpty() || password == null || password.isEmpty())
            return ResponseEntity.badRequest().body(result(false, "Vui lòng nhập tài khoản và mật khẩu"));
        try {
            List<Map<String, Object>> users = db.queryForList(
                    "SELECT id, fullname, username, password, role, phone, email, address, avatar_style, avatar_image FROM users WHERE username = ?", username);
            if (users.isEmpty() || !matchPassword(password, String.valueOf(users.get(0).get("password"))))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "Sai tài khoản hoặc mật khẩu"));
            Map<String, Object> user = new HashMap<>(users.get(0));
            String oldHash = String.valueOf(user.remove("password"));
            if (!oldHash.startsWith("pbkdf2$"))
                db.update("UPDATE users SET password = ? WHERE id = ? AND password = ?", hashPassword(password), user.get("id"), oldHash);
            HttpSession existing = request.getSession(false);
            if (existing != null) existing.invalidate();
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(60 * 60 * 4);
            session.setAttribute("user_id", ((Number) user.get("id")).intValue());
            session.setAttribute("role", user.get("role"));
            session.setAttribute("phone", user.get("phone"));
            session.setAttribute("fullname", user.get("fullname"));
            Map<String, Object> r = result(true, "Đăng nhập thành công");
            r.put("user", user);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(result(false, "Không thể đăng nhập, kiểm tra kết nối cơ sở dữ liệu"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s == null || s.getAttribute("user_id") == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "Phiên đăng nhập đã hết hạn"));
        List<Map<String, Object>> users = db.queryForList(
                "SELECT id, fullname, username, phone, role, email, address, avatar_style, avatar_image FROM users WHERE id = ?", s.getAttribute("user_id"));
        if (users.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "Tài khoản không tồn tại"));
        Map<String, Object> r = result(true, "OK");
        r.put("user", users.get(0));
        return ResponseEntity.ok(r);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        return result(true, "Đã đăng xuất");
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String fullname = safe(body.get("fullname"));
        String username = safe(body.get("username"));
        String password = body.get("password");
        String phone = safe(body.get("phone"));
        if (fullname.length() < 2 || fullname.length() > 100 || fullname.contains("<") || fullname.contains(">") ||
                !username.matches("[A-Za-z0-9_]{3,30}") || password == null || password.length() < 8 ||
                password.length() > 72 || !phone.matches("\\+?[0-9]{9,15}"))
            return ResponseEntity.badRequest().body(result(false, "Thông tin không hợp lệ (tên đăng nhập 3–30 ký tự, mật khẩu ít nhất 8 ký tự, SĐT 9–15 số)"));
        try {
            if (db.queryForObject("SELECT COUNT(*) FROM users WHERE username = ? OR phone = ?", Integer.class, username, phone) > 0)
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false, "Tên đăng nhập hoặc số điện thoại đã được sử dụng"));
            db.update("INSERT INTO users (fullname, username, password, role, phone) VALUES (?, ?, ?, 'user', ?)",
                    fullname, username, hashPassword(password), phone);
            return ResponseEntity.status(HttpStatus.CREATED).body(result(true, "Đăng ký thành công"));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false, "Tài khoản đã tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(result(false, "Đăng ký thất bại, thử lại sau"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(result(false,
                "Tính năng lấy lại mật khẩu cần xác minh OTP. Vui lòng liên hệ quản trị viên trong bản demo."));
    }

    private static ResponseEntity<Map<String, Object>> fail(HttpStatus code, String message) {
        return ResponseEntity.status(code).body(result(false, message));
    }

    private Map<String, Object> authenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) return null;
        List<Map<String, Object>> found = db.queryForList(
                "SELECT id, fullname, username, password, phone, role, email, address, avatar_style, avatar_image FROM users WHERE id=?",
                session.getAttribute("user_id"));
        return found.isEmpty() ? null : found.get(0);
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<Map<String, Object>> profile(@RequestBody Map<String, String> body, HttpServletRequest req) {
        Map<String, Object> user = authenticated(req);
        if (user == null) return fail(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        String fullname = safe(body.get("fullname"));
        String email = safe(body.get("email"));
        String avatar = safe(body.get("avatar_style"));
        boolean admin = "admin".equals(user.get("role"));
        if (fullname.length() < 2 || fullname.length() > 100 || fullname.contains("<") || fullname.contains(">") ||
                email.length() > 150 || (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) ||
                !List.of("forest", "ocean", "violet", "sunset", "graphite").contains(avatar))
            return ResponseEntity.badRequest().body(result(false, "Thông tin hồ sơ chưa hợp lệ"));
        int id = ((Number) user.get("id")).intValue();
        String phone = safe(body.get("phone"));
        if (admin) {
            db.update("UPDATE users SET fullname=?,email=?,avatar_style=? WHERE id=?", fullname, email, avatar, id);
        } else {
            String address = safe(body.get("address"));
            if (!phone.matches("\\+?[0-9]{9,15}") || address.length() > 250)
                return ResponseEntity.badRequest().body(result(false, "Số điện thoại hoặc địa chỉ chưa hợp lệ"));
            String oldPhone = String.valueOf(user.get("phone"));
            String oldName = String.valueOf(user.get("fullname"));
            Integer exists = db.queryForObject("SELECT COUNT(*) FROM users WHERE phone=? AND id<>?", Integer.class, phone, id);
            if (exists != null && exists > 0) return fail(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng");
            db.update("UPDATE users SET fullname=?,phone=?,email=?,address=?,avatar_style=? WHERE id=?",
                    fullname, phone, email, address, avatar, id);
            if (!oldPhone.equals(phone) || !oldName.equals(fullname))
                db.update("UPDATE bookings SET customer_name=?,customer_phone=? WHERE customer_phone=? AND customer_name=?",
                        fullname, phone, oldPhone, oldName);
        }
        HttpSession sess = req.getSession(false);
        if (!admin) sess.setAttribute("phone", phone);
        sess.setAttribute("fullname", fullname);
        Map<String, Object> response = result(true, "Đã cập nhật hồ sơ");
        response.put("user", me(req).getBody().get("user"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file,
            HttpServletRequest req) {
        Map<String, Object> user = authenticated(req);
        if (user == null) return fail(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        if (file.isEmpty() || file.getSize() > 2 * 1024 * 1024)
            return ResponseEntity.badRequest().body(result(false, "Ảnh đại diện không vượt quá 2 MB"));
        try {
            byte[] raw = file.getBytes();
            boolean png = raw.length > 8 && (raw[0] & 255) == 137 && raw[1] == 80 && raw[2] == 78 && raw[3] == 71;
            boolean jpeg = raw.length > 3 && (raw[0] & 255) == 255 && (raw[1] & 255) == 216 && (raw[2] & 255) == 255;
            boolean webp = raw.length > 12 && raw[0] == 'R' && raw[1] == 'I' && raw[2] == 'F' && raw[3] == 'F' &&
                    raw[8] == 'W' && raw[9] == 'E' && raw[10] == 'B' && raw[11] == 'P';
            if (!png && !jpeg && !webp)
                return ResponseEntity.badRequest().body(result(false, "Chỉ nhận PNG, JPG hoặc WebP"));
            Path dir = Paths.get("uploads", "avatars");
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + (png ? ".png" : (jpeg ? ".jpg" : ".webp"));
            Files.write(dir.resolve(name), raw, StandardOpenOption.CREATE_NEW);
            String image = "/uploads/avatars/" + name;
            db.update("UPDATE users SET avatar_image=? WHERE id=?", image, user.get("id"));
            Map<String, Object> result = result(true, "Đã thay ảnh đại diện");
            result.put("image", image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(result(false, "Không lưu được ảnh đại diện"));
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Map<String, Object>> resetAvatar(HttpServletRequest req) {
        Map<String, Object> user = authenticated(req);
        if (user == null) return fail(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        db.update("UPDATE users SET avatar_image='' WHERE id=?", user.get("id"));
        return ResponseEntity.ok(result(true, "Đã khôi phục avatar mặc định"));
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body,
            HttpServletRequest req) {
        Map<String, Object> user = authenticated(req);
        if (user == null) return fail(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        String current = body.get("currentPassword");
        String next = body.get("newPassword");
        String confirm = body.get("confirmPassword");
        if (next == null || next.isBlank() || next.length() < 8 || next.length() > 72 || !next.equals(confirm))
            return ResponseEntity.badRequest().body(result(false, "Mật khẩu mới cần từ 8–72 ký tự và phải trùng nhau"));
        try {
            if (current == null || !matchPassword(current, String.valueOf(user.get("password"))))
                return fail(HttpStatus.FORBIDDEN, "Mật khẩu hiện tại không đúng");
            db.update("UPDATE users SET password=? WHERE id=?", hashPassword(next), user.get("id"));
            req.getSession(false).invalidate();
            return ResponseEntity.ok(result(true, "Đã đổi mật khẩu. Vui lòng đăng nhập lại"));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(result(false, "Không đổi được mật khẩu")); }
    }

    @DeleteMapping("/account")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestBody Map<String, String> body,
            HttpServletRequest req) {
        Map<String, Object> user = authenticated(req);
        if (user == null) return fail(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        if (!"user".equals(user.get("role"))) return fail(HttpStatus.FORBIDDEN, "Không thể xóa tài khoản quản trị");
        if (!"XOA TAI KHOAN".equals(body.get("confirmation")))
            return ResponseEntity.badRequest().body(result(false, "Vui lòng nhập chính xác XOA TAI KHOAN"));
        try {
            if (!matchPassword(body.getOrDefault("currentPassword", ""), String.valueOf(user.get("password"))))
                return fail(HttpStatus.FORBIDDEN, "Mật khẩu xác nhận không đúng");
            Integer active = db.queryForObject("SELECT COUNT(*) FROM bookings WHERE customer_phone=? AND status IN ('PENDING','CONFIRMED') AND (date>CAST(GETDATE() AS DATE) OR (date=CAST(GETDATE() AS DATE) AND end_time>CAST(GETDATE() AS TIME)))",
                    Integer.class, user.get("phone"));
            if (active != null && active > 0)
                return fail(HttpStatus.CONFLICT, "Bạn còn lịch đặt chưa kết thúc. Hãy hủy hoặc hoàn tất lịch trước khi xóa tài khoản");
            String anonymous = "Tài khoản đã xóa";
            db.update("UPDATE bookings SET customer_name=?,customer_phone='' WHERE customer_phone=?", anonymous, user.get("phone"));
            db.update("DELETE FROM users WHERE id=?", user.get("id"));
            req.getSession(false).invalidate();
            return ResponseEntity.ok(result(true, "Tài khoản đã được xóa và lịch sử được ẩn danh"));
        } catch (Exception e) {
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(result(false, "Không xóa được tài khoản"));
        }
    }
}
