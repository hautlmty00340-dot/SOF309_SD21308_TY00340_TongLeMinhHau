# Test Plan — AU Badminton (Tuần 1)

## Mục tiêu

Xác nhận ứng dụng khởi động local, giao diện được phục vụ, các luồng cốt lõi tài khoản, tra cứu sân và đặt sân có thể được đặc tả và kiểm thử ở các tuần tiếp theo.

## Phạm vi

- Khởi chạy Spring Boot và kết nối SQL Server `AuDB`.
- Trang chủ, danh sách sân, trang chi tiết (bao gồm trường hợp sân không tồn tại).
- Đăng ký (kèm ràng buộc trùng username/số điện thoại), đăng nhập, đăng xuất và kiểm tra session.
- Gửi đặt sân (chỉ tài khoản role `user`, khung giờ phải ở tương lai và không trùng lịch), kiểm tra lịch đặt và xem lịch sử.
- Quyền quản trị: quản lý sân/booking, thay đổi trạng thái IoT, xác nhận booking; phân biệt rõ hành vi được phép của `user` (chỉ hủy đơn của mình) và `admin` (xác nhận/hủy, quản lý sân).

## Ngoài phạm vi

Cổng thanh toán thật, SMS/email, phần cứng IoT, triển khai production, hiệu năng và bảo mật chuyên sâu.

## Môi trường kiểm thử dự kiến

- Windows 10/11, JDK 17, Maven 3.8+, Microsoft SQL Server và SQL Server Management Studio.
- Database `AuDB` được khởi tạo bằng `docs/database/migration_booking_detail.sql`.
- Ứng dụng tại `http://localhost:8080/`; trình duyệt Chrome/Edge hiện hành.
- Tài khoản và dữ liệu demo được tạo theo migration; không ghi mật khẩu thực vào test log.

## Chiến lược

1. **Smoke:** ứng dụng khởi động, trang chủ trả về, `/api/health` báo trạng thái.
2. **Functional:** đăng ký/đăng nhập, xem sân, đặt sân hợp lệ (role `user`), xem lịch sử, admin xác nhận booking.
3. **Validation/boundary:** thiếu trường, sai định dạng liên hệ, username/password/phone sai quy tắc, thời gian không hợp lệ hoặc trong quá khứ, sân không tồn tại, trùng username/số điện thoại khi đăng ký.
4. **Authorization:** khách chưa đăng nhập hoặc user thường không được thao tác admin; admin không được tự đặt sân; user chỉ được hủy (không tự xác nhận) booking của chính mình.
5. **Integration:** xác nhận request API ghi/đọc SQL Server đúng dữ liệu (ví dụ trùng khung giờ bị chặn ở DB) và session được giữ qua các request.

## Điều kiện bắt đầu và kết thúc

- **Bắt đầu:** JDK/Maven/SQL Server sẵn sàng, DB đã migrate, cấu hình kết nối hợp lệ.
- **Kết thúc:** ghi kết quả từng test case, lưu log lỗi và bằng chứng; chỉ đánh dấu đạt khi quan sát thực tế.
- Các test case trong file `TEST_CASES.md` hiện ở trạng thái **Chưa chạy**.
