# Đặc tả nghiệp vụ — AU Badminton

## 1. Thông tin hệ thống

- **Mục tiêu Tuần 1:** tích hợp website đặt sân cầu lông có sẵn, chạy được local với backend và SQL Server, xác định yêu cầu nghiệp vụ và luồng kiểm thử.
- **Stack:** Java 17, Spring Boot 3.2.3, Spring MVC/JDBC, Microsoft SQL Server; giao diện HTML, CSS, JavaScript, Bootstrap.
- **Địa chỉ local:** `http://localhost:8080/`.
- **Database:** `AuDB` trên SQL Server. Cần chạy `docs/database/migration_booking_detail.sql` trước lần khởi động đầu.
- **Phiên:** HTTP session; client gửi cookie session cùng request.

## 2. Vai trò

- **Khách:** xem trang chủ, danh sách/chi tiết sân, đăng ký/đăng nhập, gửi yêu cầu đặt sân.
- **Thành viên:** xem lịch sử đặt sân và cập nhật thông tin cá nhân/avatar, đổi mật khẩu, yêu cầu xoá tài khoản.
- **Quản trị viên:** xem thống kê và quản lý sân, trạng thái đèn IoT, đặt sân, ghi chú và cấu hình cơ sở.

## 3. Luồng nghiệp vụ Tuần 1

1. Khách truy cập trang chủ; danh sách sân và giá được lấy từ backend.
2. Khách có thể mở đăng ký hoặc đăng nhập. Đăng nhập thành công tạo session; thông tin phiên được lấy qua `/api/auth/me`.
3. Khách chọn sân, ngày, giờ bắt đầu/kết thúc, phương thức thanh toán và gửi đặt sân. Server kiểm tra dữ liệu/khung giờ và ghi nhận đặt sân.
4. Thành viên đã đăng nhập xem lịch sử của mình; quản trị viên xem và cập nhật danh sách booking.
5. Người dùng đăng xuất để huỷ session.

## 4. Quy tắc dữ liệu và kiểm tra

- Sân phải tồn tại và được chọn; thời gian bắt đầu phải trước thời gian kết thúc.
- Khung giờ không được trùng với booking đang hiệu lực trên cùng sân và ngày.
- Số điện thoại là thông tin liên hệ của người đặt; trường bắt buộc phải có giá trị hợp lệ theo kiểm tra ở giao diện và backend.
- Các thao tác quản trị yêu cầu session có role admin; request không đủ quyền bị từ chối.
- Trạng thái booking dùng các giá trị nghiệp vụ như `PENDING`, `CONFIRMED`, `CANCELLED`.
- Ảnh minh chứng thanh toán và avatar được tải lên theo giới hạn multipart trong cấu hình; file được truy cập qua `/uploads/**`.
- Thiết lập kết nối database dùng biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`; không commit mật khẩu thật vào repo.

## 5. API contract quan trọng

| Method | Endpoint | Mục đích |
|---|---|---|
| `GET` | `/api/courts` | Danh sách sân |
| `GET` | `/api/courts/{id}` | Chi tiết sân và gợi ý lịch/booking liên quan |
| `POST` | `/api/courts` | Thêm sân (admin) |
| `PUT` | `/api/courts/{id}` | Cập nhật sân (admin) |
| `PATCH` | `/api/courts/{id}/iot` | Cập nhật trạng thái IoT (admin) |
| `DELETE` | `/api/courts/{id}` | Xoá sân (admin; phụ thuộc ràng buộc booking) |
| `POST` | `/api/bookings` | Tạo đặt sân |
| `GET` | `/api/bookings` | Danh sách booking theo quyền |
| `GET` | `/api/bookings/{id}` | Chi tiết booking |
| `PATCH` | `/api/bookings/{id}/status` | Cập nhật trạng thái (admin) |
| `GET` | `/api/auth/me` | Người dùng hiện tại |
| `POST` | `/api/auth/register` | Đăng ký tài khoản |
| `POST` | `/api/auth/login` | Đăng nhập |
| `POST` | `/api/auth/logout` | Đăng xuất |
| `GET` | `/api/health` | Tình trạng ứng dụng và DB |

Endpoint chi tiết khác gồm cập nhật hồ sơ/avatar/mật khẩu, báo cáo doanh thu, ghi chú booking và cấu hình. Hợp đồng đầu vào/đầu ra hiện được định nghĩa trong controller và giao diện; tài liệu này mô tả phạm vi Tuần 1 để tạo test plan.

## 6. Ngoài phạm vi Tuần 1

Tích hợp cổng thanh toán thật, gửi SMS/email thật, điều khiển phần cứng IoT thật, triển khai production, kiểm thử tải và kiểm thử bảo mật chuyên sâu.
