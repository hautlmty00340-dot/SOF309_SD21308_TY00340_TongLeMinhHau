package com.aubadminton.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingsController {
    private static final Logger LOG = LoggerFactory.getLogger(BookingsController.class);
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> PAYMENTS = Set.of("BANK_TRANSFER", "VNPAY", "MOMO", "CASH");
    private final JdbcTemplate db;
    private final SettingsController settings;

    public BookingsController(JdbcTemplate db, SettingsController settings) { this.db = db; this.settings = settings; }

    private static Map<String, Object> result(boolean success, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", msg);
        return result;
    }
    private static String str(Object o) { return o == null ? "" : o.toString().trim(); }
    private static boolean admin(HttpServletRequest req) { HttpSession session = req.getSession(false); return session != null && "admin".equals(session.getAttribute("role")); }
    private static String phone(HttpServletRequest req) { HttpSession session = req.getSession(false); return session == null ? "" : str(session.getAttribute("phone")); }
    private boolean owner(HttpServletRequest req, Map<String, Object> b) {
        return admin(req) || (!phone(req).isBlank() && phone(req).equals(str(b.get("customer_phone"))));
    }
    private List<Map<String, Object>> find(int id) {
        return db.queryForList("SELECT b.booking_id, b.court_id, c.name AS court_name, c.court_type, " +
                "b.date, b.start_time, b.end_time, b.customer_name, b.customer_phone, " +
                "b.status, b.court_amount, b.reward_points, b.payment_method, b.court_amount AS grand_total, " +
                "ISNULL(b.deposit_image, '') AS deposit_image, ISNULL(b.admin_notes, '') AS admin_notes, b.created_at " +
                "FROM bookings b LEFT JOIN courts c ON b.court_id = c.court_id WHERE b.booking_id=?", id);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBookings(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer court_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            HttpServletRequest req) {
        try {
            StringBuilder sql = new StringBuilder("SELECT b.booking_id, b.court_id, c.name AS court_name, c.court_type, " +
                    "b.date, b.start_time, b.end_time, b.customer_name, b.customer_phone, " +
                    "b.status, b.court_amount, b.reward_points, b.payment_method, b.court_amount AS grand_total " +
                    "FROM bookings b LEFT JOIN courts c ON b.court_id=c.court_id WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (!admin(req)) { // Không tin tham số phone do trình duyệt gửi.
                if (phone(req).isBlank()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Tài khoản chưa có số điện thoại"));
                sql.append(" AND b.customer_phone=?"); params.add(phone(req));
            } else if (phone != null && !phone.isBlank()) { sql.append(" AND b.customer_phone=?"); params.add(phone.trim()); }
            if (court_id != null) { sql.append(" AND b.court_id=?"); params.add(court_id); }
            if (status != null && !status.isBlank()) {
                String s = status.trim().toUpperCase();
                if (!Set.of("PENDING", "CONFIRMED", "CANCELLED").contains(s))
                    return ResponseEntity.badRequest().body(result(false, "Trạng thái không hợp lệ"));
                sql.append(" AND b.status=?"); params.add(s);
            }
            if (date != null && !date.isBlank()) {
                try { LocalDate.parse(date); } catch (Exception e) { return ResponseEntity.badRequest().body(result(false, "Ngày không hợp lệ")); }
                sql.append(" AND b.date=?"); params.add(date);
            }
            sql.append(" ORDER BY b.booking_id DESC");
            List<Map<String, Object>> data = db.queryForList(sql.toString(), params.toArray());
            Map<String, Object> r = result(true, "OK"); r.put("data", data); r.put("total", data.size());
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(result(false, "Không tải được lịch đặt sân"));
        }
    }

    @PostMapping
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body, HttpServletRequest req) {


        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "Vui lòng đăng nhập bằng tài khoản khách hàng"));
        if (!"user".equals(str(session.getAttribute("role"))))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Tài khoản Admin không thể đặt sân. Hãy đăng nhập bằng tài khoản khách hàng"));
        int courtId;
        LocalDate day;
        LocalTime start, end;
        String payment = str(body.get("payment_method")).toUpperCase();
        try {
            courtId = Integer.parseInt(str(body.get("court_id")));
            day = LocalDate.parse(str(body.get("date")));
            start = LocalTime.parse(str(body.get("start_time")));
            end = LocalTime.parse(str(body.get("end_time")));
        } catch (Exception e) { return ResponseEntity.badRequest().body(result(false, "Thiếu thông tin sân, ngày hoặc giờ hợp lệ")); }
        if (!PAYMENTS.contains(payment)) return ResponseEntity.badRequest().body(result(false, "Phương thức thanh toán không hợp lệ"));
        LocalDateTime now = LocalDateTime.now(LOCAL_ZONE);
        long minutes = Duration.between(start, end).toMinutes();
        if (courtId <= 0 || minutes < 30 || minutes > 720 || !day.atTime(start).isAfter(now))
            return ResponseEntity.badRequest().body(result(false, "Chọn khung giờ trong tương lai, thời lượng từ 30 phút đến 12 giờ"));



        Map<String, String> options = settings.values();
        LocalTime opening = LocalTime.parse(options.get("open_time"));
        LocalTime closing = LocalTime.parse(options.get("close_time"));
        if (start.isBefore(opening) || end.isAfter(closing))
            return ResponseEntity.badRequest().body(result(false, "Sân hoạt động từ " + opening + " đến " + closing));
        String sqlDay = day.toString();
        String sqlStart = start.toString();
        String sqlEnd = end.toString();

        String name = str(session.getAttribute("fullname"));
        String customerPhone = str(session.getAttribute("phone"));
        if (name.isBlank() || customerPhone.isBlank())
            return ResponseEntity.badRequest().body(result(false, "Tài khoản chưa có tên hoặc số điện thoại"));
        try {

            List<Map<String, Object>> courts = db.queryForList(
                    "SELECT court_id, price_per_hour FROM courts WITH (UPDLOCK, HOLDLOCK) WHERE court_id=?", courtId);
            if (courts.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result(false, "Sân không tồn tại"));
            Integer conflict = db.queryForObject(
                    "SELECT COUNT(*) FROM bookings WHERE court_id=? AND date=? " +
                    "AND status IN ('PENDING','CONFIRMED') AND start_time < ? AND end_time > ?",
                    Integer.class, courtId, sqlDay, sqlEnd, sqlStart);
            if (conflict != null && conflict > 0)
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false,
                        "Sân đã có lịch trong khung giờ này. Vui lòng chọn sân hoặc thời gian khác."));
            BigDecimal price = (BigDecimal) courts.get(0).get("price_per_hour");
            if (price == null || price.signum() < 0) return ResponseEntity.badRequest().body(result(false, "Giá sân không hợp lệ"));
            BigDecimal amount = price.multiply(BigDecimal.valueOf(minutes))
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            Long bookingId = db.queryForObject(
                    "INSERT INTO bookings (court_id,date,start_time,end_time,customer_name,customer_phone,status,payment_method,court_amount) " +
                    "OUTPUT INSERTED.booking_id VALUES (?,?,?,?,?,?,?,?,?)",
                    Long.class, courtId, sqlDay, sqlStart, sqlEnd, name, customerPhone,
                    "true".equals(options.get("auto_approve")) ? "CONFIRMED" : "PENDING", payment, amount);
            Map<String, Object> r = result(true, "true".equals(options.get("auto_approve")) ? "Đặt sân thành công, đã xác nhận" : "Đặt sân thành công, chờ quản trị viên duyệt");
            r.put("booking_id", bookingId); r.put("court_amount", amount);
            return ResponseEntity.status(HttpStatus.CREATED).body(r);
        } catch (Exception e) {

            LOG.error("Booking creation failed: courtId={}, date={}, from={}, to={}",
                    courtId, sqlDay, sqlStart, sqlEnd, e);

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(result(false,
                    "Không thể tạo đơn. Vui lòng kiểm tra log backend hoặc liên hệ quản trị viên."));
        }
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<Map<String, Object>> revenue(@RequestParam(defaultValue = "month") String mode,
            @RequestParam Integer year, @RequestParam(required = false) Integer month, HttpServletRequest req) {
        if (!admin(req)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Không có quyền xem báo cáo"));
        if (!Set.of("month", "year").contains(mode) || year < 2020 || year > 2100 ||
                ("month".equals(mode) && (month == null || month < 1 || month > 12)))
            return ResponseEntity.badRequest().body(result(false, "Khoảng thời gian không hợp lệ"));
        LocalDate from = "month".equals(mode) ? LocalDate.of(year, month, 1) : LocalDate.of(year, 1, 1);
        LocalDate to = "month".equals(mode) ? from.plusMonths(1) : from.plusYears(1);
        String bucket = "month".equals(mode) ? "DAY([date])" : "MONTH([date])";
        List<Map<String, Object>> rows = db.queryForList(
                "SELECT " + bucket + " AS bucket, COUNT(*) AS total, " +
                "SUM(CASE WHEN status='CONFIRMED' THEN 1 ELSE 0 END) AS confirmed, " +
                "SUM(CASE WHEN status='PENDING' THEN 1 ELSE 0 END) AS pending, " +
                "SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END) AS cancelled, " +
                "SUM(CASE WHEN status='CONFIRMED' THEN court_amount ELSE 0 END) AS revenue " +
                "FROM bookings WHERE [date]>=? AND [date]<? GROUP BY " + bucket + " ORDER BY bucket",
                from.toString(), to.toString());
        int count = "month".equals(mode) ? from.lengthOfMonth() : 12;
        List<Map<String, Object>> chart = new ArrayList<>();
        long total = 0, confirmed = 0, pending = 0, cancelled = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        for (int i = 1; i <= count; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("period", i); point.put("total", 0); point.put("confirmed", 0);
            point.put("pending", 0); point.put("cancelled", 0); point.put("revenue", BigDecimal.ZERO);
            chart.add(point);
        }
        for (Map<String, Object> row: rows) {
            int index = ((Number) row.get("bucket")).intValue() - 1;
            Map<String, Object> point = chart.get(index);
            for (String key: List.of("total", "confirmed", "pending", "cancelled", "revenue")) point.put(key, row.get(key));
            total += ((Number) row.get("total")).longValue();
            confirmed += ((Number) row.get("confirmed")).longValue();
            pending += ((Number) row.get("pending")).longValue();
            cancelled += ((Number) row.get("cancelled")).longValue();
            revenue = revenue.add((BigDecimal) row.get("revenue"));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", total); data.put("confirmed", confirmed); data.put("pending", pending);
        data.put("cancelled", cancelled); data.put("revenue", revenue); data.put("series", chart);
        Map<String, Object> response = result(true, "OK"); response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookingDetail(@PathVariable int id, HttpServletRequest req) {
        List<Map<String, Object>> rows = find(id);
        if (rows.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result(false, "Không tìm thấy đơn"));
        if (!owner(req, rows.get(0))) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Không có quyền xem đơn này"));
        Map<String, Object> r = result(true, "OK");
        Map<String, Object> b = new HashMap<>(rows.get(0));
        if (!admin(req)) b.remove("admin_notes");
        r.put("data", b);
        return ResponseEntity.ok(r);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        String next = str(body.get("status")).toUpperCase();
        if (!Set.of("CONFIRMED", "CANCELLED").contains(next))
            return ResponseEntity.badRequest().body(result(false, "Trạng thái không hợp lệ"));
        List<Map<String, Object>> rows = find(id);
        if (rows.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result(false, "Không tìm thấy đơn"));
        Map<String, Object> booking = rows.get(0);
        if (!owner(req, booking)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Không có quyền thay đổi đơn"));
        boolean isAdmin = admin(req);
        if (!isAdmin && !next.equals("CANCELLED"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Khách hàng chỉ có thể hủy đơn của mình"));
        String old = str(booking.get("status"));
        if (old.equals("CANCELLED") || (next.equals("CONFIRMED") && !old.equals("PENDING")))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false, "Không thể chuyển trạng thái từ " + old + " sang " + next));
        if (!isAdmin) {
            LocalDate d = ((java.sql.Date) booking.get("date")).toLocalDate();
            LocalTime t = ((java.sql.Time) booking.get("start_time")).toLocalTime();
            if (!d.atTime(t).isAfter(LocalDateTime.now(LOCAL_ZONE)))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false, "Không thể hủy lịch đã bắt đầu"));
        }
        int updated = db.update("UPDATE bookings SET status=? WHERE booking_id=? AND status=?", next, id, old);
        if (updated == 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(result(false, "Đơn đã thay đổi, vui lòng tải lại trang"));
        return ResponseEntity.ok(result(true, "Đã cập nhật trạng thái"));
    }

    @PostMapping("/{id}/deposit-image")
    public ResponseEntity<Map<String, Object>> uploadDepositImage(@PathVariable int id,
            @RequestParam("file") MultipartFile file, HttpServletRequest req) {
        List<Map<String, Object>> rows = find(id);
        if (rows.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result(false, "Không tìm thấy đơn"));
        if (!owner(req, rows.get(0))) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Không có quyền tải ảnh"));
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
            return ResponseEntity.badRequest().body(result(false, "Vui lòng chọn ảnh nhỏ hơn 5MB"));
        try {
            byte[] b = file.getBytes();
            boolean png = b.length >= 8 && (b[0] & 255)==137 && b[1]==80 && b[2]==78 && b[3]==71;
            boolean jpeg = b.length >= 3 && (b[0] & 255)==255 && (b[1] & 255)==216 && (b[2] & 255)==255;
            boolean webp = b.length >= 12 && b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' &&
                    b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P';
            if (!png && !jpeg && !webp) return ResponseEntity.badRequest().body(result(false, "Chỉ hỗ trợ PNG, JPG hoặc WebP"));
            String ext = png ? ".png" : jpeg ? ".jpg" : ".webp";
            Path dir = Paths.get("uploads", "deposits");
            Files.createDirectories(dir);
            String filename = "deposit_" + id + "_" + UUID.randomUUID() + ext;
            Files.write(dir.resolve(filename), b);
            String imageUrl = "/uploads/deposits/" + filename;
            db.update("UPDATE bookings SET deposit_image=? WHERE booking_id=?", imageUrl, id);
            Map<String, Object> r = result(true, "Đã tải ảnh lên"); r.put("image_url", imageUrl);
            return ResponseEntity.ok(r);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(result(false, "Lưu ảnh thất bại")); }
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<Map<String, Object>> updateNotes(@PathVariable int id,
            @RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (!admin(req)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Chỉ quản trị viên được ghi chú"));
        String notes = str(body.get("admin_notes"));
        if (notes.length() > 1000) return ResponseEntity.badRequest().body(result(false, "Ghi chú tối đa 1000 ký tự"));
        int n = db.update("UPDATE bookings SET admin_notes=? WHERE booking_id=?", notes, id);
        if (n == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result(false, "Không tìm thấy đơn"));
        return ResponseEntity.ok(result(true, "Đã lưu ghi chú"));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getSummary(HttpServletRequest req) {
        if (!admin(req)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result(false, "Không có quyền xem thống kê"));
        try {
            Map<String, Object> stats = db.queryForMap("SELECT COUNT(*) AS total_bookings, " +
                    "ISNULL(SUM(CASE WHEN status='CONFIRMED' THEN 1 ELSE 0 END),0) AS confirmed, " +
                    "ISNULL(SUM(CASE WHEN status='PENDING' THEN 1 ELSE 0 END),0) AS pending, " +
                    "ISNULL(SUM(CASE WHEN status='CANCELLED' THEN 1 ELSE 0 END),0) AS cancelled, " +
                    "ISNULL(SUM(CASE WHEN status='CONFIRMED' THEN court_amount ELSE 0 END), 0) AS total_revenue, " +
                    "COUNT(CASE WHEN CAST(date AS DATE)=CAST(GETDATE() AS DATE) THEN 1 END) AS today_bookings " +
                    "FROM bookings WHERE MONTH(date)=MONTH(GETDATE()) AND YEAR(date)=YEAR(GETDATE())");
            Map<String, Object> courts = db.queryForMap("SELECT COUNT(*) AS total_courts, " +
                    "ISNULL(SUM(CASE WHEN court_type LIKE '%VIP%' THEN 1 ELSE 0 END),0) AS vip_courts FROM courts");
            Map<String, Object> r = result(true, "OK");
            Map<String, Object> data = new HashMap<>(stats); data.putAll(courts);
            r.put("data", data); return ResponseEntity.ok(r);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(result(false, "Không thể tải thống kê")); }
    }
}
