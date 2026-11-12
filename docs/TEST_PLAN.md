# Kế hoạch kiểm thử (Test Plan)

## Thông tin chung

- **Dự án:** ShopPulse
- **Sinh viên:** Tong Le Minh Hau — MSSV TY00340
- **Phiên bản:** 1.0, tuần 1
- **Tài liệu yêu cầu:** [SPECIFICATIONS.md](SPECIFICATIONS.md)
- **Trạng thái kế hoạch:** Soạn xong; test case chưa thực thi.

## 1. Mục tiêu

- Xác nhận ứng dụng Spring Boot phục vụ giao diện và API tại `http://localhost:8080`.
- Kiểm tra các luồng đăng ký, đăng nhập, session và quản lý sản phẩm qua REST API.
- Kiểm tra validation dữ liệu, phân quyền theo session, kết quả HTTP và tính nhất quán dữ liệu sau CRUD.
- Cung cấp test case nền cho tuần 2 (JUnit 5 Controller/Service) và các tuần automation sau.

## 2. Phạm vi

### Trong phạm vi

- Khởi động ứng dụng, trang tĩnh và API.
- Đăng ký email/điện thoại, validation, trùng liên hệ, mật khẩu hash.
- Đăng nhập đúng/sai; đọc tài khoản phiên; đăng xuất.
- Liệt kê sản phẩm công khai; tạo/cập nhật/xóa sản phẩm với session.
- Giá, số lượng, trường bắt buộc, tên sản phẩm trùng và ID không tồn tại.

### Ngoài phạm vi

- Hiệu năng/tải, kiểm thử xâm nhập, OAuth, gửi email, giỏ hàng/đặt hàng/thanh toán.
- Kiểm thử đầy đủ nhiều trình duyệt và thiết bị.
- Độ bền dữ liệu sau restart: H2 đang chạy in-memory nên dữ liệu được reset.

## 3. Chiến lược kiểm thử

1. **Smoke/manual:** khởi động bằng `mvn spring-boot:run`, mở trang chủ và gọi một endpoint đọc.
2. **API functional:** dùng Postman, IntelliJ HTTP Client hoặc `curl`; theo thứ tự đăng ký/đăng nhập → sản phẩm → đăng xuất. Giữ cookie session cho các request cần xác thực.
3. **Thiết kế dữ liệu:** phân vùng tương đương cho contact/password; giá trị biên 1, 0 và số âm cho giá/số lượng; kiểm tra trùng với khác chữ hoa/thường.
4. **Tự động hóa kế tiếp:** tuần 2 viết JUnit 5 cho service/controller; tuần 3 trở đi mới mở rộng Selenium UI.
5. Mỗi test case bắt đầu với database sạch hoặc dữ liệu định danh duy nhất (`contact`/tên sản phẩm có hậu tố riêng). Các test phụ thuộc session cần chạy cùng cookie jar.

## 4. Môi trường và dữ liệu

- JDK 17+, Maven 3.8+; cổng TCP 8080 còn trống.
- H2 in-memory; schema được tạo khi chạy ứng dụng; có ba sản phẩm demo ban đầu.
- Tài khoản test tự đăng ký trong phiên chạy, ví dụ `qa+<mã-lần-chạy>@example.com`; không có sẵn tài khoản `admin`.
- Gửi `Content-Type: application/json` cho các POST/PUT có body.

## 5. Tiêu chí bắt đầu và kết thúc

### Bắt đầu

- Maven build được, ứng dụng khởi động và log báo server chạy ở cổng 8080.
- Database H2 khởi tạo; `GET /api/products` trả HTTP 200.

### Kết thúc

- Toàn bộ test case mức P1 được chạy; không còn lỗi P1 mở.
- Ghi kết quả thực tế và bằng chứng vào cột trạng thái/ghi chú trong `TEST_CASES.md` khi chạy.
- Ở tuần 1 chỉ hoàn thành thiết kế test; chưa đánh dấu đạt cho đến khi có lần chạy thực tế.

## 6. Mức độ ưu tiên

- **P1:** khởi động, đăng ký, đăng nhập, session, validation căn bản, CRUD và phân quyền.
- **P2:** kiểm tra biên độ dài/định dạng, tên trùng khác hoa thường, các trường tùy chọn và lỗi ID.

## 7. Rủi ro và giả định

- Backend chưa có UI quản trị CRUD; các test CRUD gọi API.
- H2 in-memory không giữ tài khoản/sản phẩm qua lần khởi động lại.
- Thông báo validation mặc định của Spring có thể khác nội dung cụ thể trong test; ưu tiên kiểm tra HTTP status và trường/lỗi nghiệp vụ.

## 8. Sản phẩm bàn giao tuần 1

- Backend Spring Boot và cấu hình H2.
- Đặc tả nghiệp vụ, test plan và bộ test case.
- Cách khởi chạy tại README.
- Chưa bao gồm kết quả chạy test tự động; phần đó thuộc tuần tiếp theo.
