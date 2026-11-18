# Nhật ký AI: JUnit 5 cho backend tuần 2

## 1. Ngữ cảnh & mục tiêu

- Dự án ShopPulse dùng Spring Boot, có `AuthController`, `ProductController`, `AuthService` và `ProductService`.
- Mục tiêu là viết test JUnit 5 thực tế, dùng `@ParameterizedTest`/`@CsvSource` cho dữ liệu biên và `assertThrows()` cho lỗi nghiệp vụ.

## 2. Yêu cầu gửi cho AI

> Dựa trên controller, service, DTO và repository hiện có của ShopPulse, hãy tạo bộ kiểm thử tuần 2 bằng JUnit 5. Kiểm tra đăng ký/đăng nhập, phân vùng email và số điện thoại, validation giá/số lượng sản phẩm, yêu cầu session, dữ liệu trùng và ID không tồn tại. Dùng `@ParameterizedTest` cùng `@CsvSource` và `assertThrows()`; không giả lập kết quả test.

## 3. Nội dung được tạo

- `AuthControllerTest`: kiểm tra validation đăng ký, đăng ký thành công và đăng nhập tạo session.
- `ProductControllerTest`: kiểm tra biên giá/số lượng, yêu cầu đăng nhập và tạo sản phẩm.
- `AuthServiceTest`: kiểm tra phân vùng contact, contact trùng và mật khẩu xác nhận không khớp.
- `ProductServiceTest`: kiểm tra tên trùng và cập nhật ID không tồn tại.

## 4. Chỉnh sửa thủ công

- Chỉnh `AuthController` để inject `AccountRepository` qua constructor; repository không phải kiểu tham số HTTP mà Spring MVC tự phân giải.
- Dùng `ReflectionTestUtils` đặt ID cho entity giả trong controller test vì ID được sinh bởi JPA.
- Trạng thái chạy test được ghi nhận sau khi thực thi `mvn test`; file này chỉ mô tả thiết kế test, không khẳng định test đã pass.
