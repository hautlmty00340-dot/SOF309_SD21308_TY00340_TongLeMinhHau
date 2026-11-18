# AU Badminton — SOF309

Ứng dụng đặt sân cầu lông gồm trang khách hàng, đăng ký/đăng nhập, danh sách và chi tiết sân, đặt sân, lịch sử đặt sân, trang tài khoản và khu vực quản trị. Giao diện được phục vụ trực tiếp bởi Spring Boot.

## Chạy local (Tuần 1)

Yêu cầu: JDK 17, Maven 3.8+, Microsoft SQL Server đang chạy.

1. Mở SQL Server Management Studio và chạy toàn bộ script [`docs/database/migration_booking_detail.sql`](docs/database/migration_booking_detail.sql). Script tạo database `AuDB`, các bảng cơ bản và dữ liệu demo nếu bảng đang trống. Không chạy script lặp lại trên dữ liệu quan trọng nếu bạn chưa sao lưu.
2. Nếu dùng SQL Server Authentication, cấu hình thông tin kết nối trong PowerShell (thay mật khẩu bằng mật khẩu SQL Server của bạn):

   ```powershell
   $env:DB_AUTH_MODE = 'sql'
   $env:DB_URL = 'jdbc:sqlserver://localhost;databaseName=AuDB;encrypt=false;trustServerCertificate=true'
   $env:DB_USER = 'sa'
   $env:DB_PASSWORD = 'mat-khau-SQL-Server'
   ```

   `START_AU_BADMINTON.bat` mặc định tự chọn Windows Authentication nếu file `target/auth/mssql-jdbc_auth-12.4.2.x64.dll` có sẵn; nếu không, script dùng SQL Authentication. Đặt `DB_AUTH_MODE=windows` để yêu cầu Windows Authentication rõ ràng. Không commit mật khẩu thật vào repo.

3. Khởi động ứng dụng:

   ```powershell
   mvn spring-boot:run
   ```

   Hoặc chạy `START_AU_BADMINTON.bat` trên Windows sau khi đã thiết lập biến môi trường database.
4. Mở <http://localhost:8080/>. Dùng `Ctrl+C` trong terminal để dừng server.

Cấu hình mặc định đọc `DB_URL`, `DB_USER`, `DB_PASSWORD`; xem [`application.properties`](src/main/resources/application.properties). Nếu kết nối thất bại, kiểm tra SQL Server đang chạy, database `AuDB` đã được tạo và thông tin đăng nhập đúng.

## API chính

- `GET /api/courts`, `GET /api/courts/{id}`: danh sách và chi tiết sân.
- `POST /api/bookings`, `GET /api/bookings`, `GET /api/bookings/{id}`: đặt sân và xem lịch.
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`: tài khoản và phiên đăng nhập.
- `GET /api/health`, `GET /api/version`: kiểm tra backend.

Các trang quản trị và API có phân quyền theo session. Mô tả nghiệp vụ ở [`docs/SPECIFICATIONS.md`](docs/SPECIFICATIONS.md); kế hoạch và ca kiểm thử ở [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md), [`docs/TEST_CASES.md`](docs/TEST_CASES.md).
