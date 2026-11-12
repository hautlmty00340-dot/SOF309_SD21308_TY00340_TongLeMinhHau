# Đặc tả nghiệp vụ ShopPulse

## 1. Thông tin dự án

- **Tên đề tài:** ShopPulse — cửa hàng thương mại điện tử.
- **Mục tiêu tuần 1:** Khởi chạy web local; có backend cho đăng ký, đăng nhập và CRUD sản phẩm; hoàn thiện đặc tả, test plan và test case.
- **Nền tảng:** Java 17, Spring Boot 3, Spring MVC, Spring Data JPA, H2, HTML/CSS/JavaScript.
- **URL local:** `http://localhost:8080`.
- **API prefix:** `/api`.
- **Database:** H2 in-memory. Dữ liệu bị xóa khi ứng dụng dừng; đây là cấu hình học tập/dev, chưa phải cấu hình triển khai thật.

## 2. Vai trò và quy tắc nghiệp vụ

### 2.1 Tài khoản

1. Người dùng đăng ký với họ tên, email hoặc số điện thoại Việt Nam, mật khẩu, xác nhận mật khẩu và đồng ý điều khoản.
2. Họ tên được trim, bắt buộc từ 2 đến 100 ký tự.
3. Email phải có dạng cơ bản `ten@mien.tld`. Số điện thoại chấp nhận `0` hoặc `+84`, theo sau là đầu số di động 3/5/7/8/9 và 8 chữ số.
4. Mật khẩu dài 8–72 ký tự. Mật khẩu được lưu dưới dạng BCrypt hash; API không trả hash về client.
5. Mật khẩu xác nhận phải trùng mật khẩu. Người đăng ký phải chọn đồng ý điều khoản.
6. Email/số điện thoại đã tồn tại không thể đăng ký lần nữa (so sánh không phân biệt hoa thường).
7. Đăng ký thành công tạo phiên đăng nhập. Đăng nhập nhận `identifier` (email hoặc điện thoại) và mật khẩu. Sai tài khoản hoặc mật khẩu trả cùng thông báo chung.
8. `/api/auth/me` trả thông tin tài khoản của phiên hiện tại; chưa đăng nhập trả HTTP 401. `/api/auth/logout` hủy phiên.

### 2.2 Sản phẩm

1. Danh sách sản phẩm đọc công khai qua API.
2. Tạo, cập nhật và xóa sản phẩm yêu cầu phiên đăng nhập.
3. Tên sản phẩm bắt buộc, tối đa 160 ký tự và không trùng (không phân biệt hoa thường). Danh mục bắt buộc, tối đa 60 ký tự.
4. Giá bán bắt buộc và lớn hơn 0; giá gốc bắt buộc và không âm; số lượng bắt buộc và không âm.
5. Không tìm thấy sản phẩm khi cập nhật/xóa trả HTTP 404. Tên trùng trả HTTP 409; dữ liệu không hợp lệ trả HTTP 400.
6. Khi chạy lần đầu, ứng dụng thêm ba sản phẩm mẫu nếu bảng sản phẩm đang trống.

## 3. API contract

Các request/response dùng JSON, trừ `204 No Content`. Phiên dùng cookie HTTP session do Spring quản lý.

| Method | Endpoint | Quyền | Thành công |
|---|---|---|---|
| `POST` | `/api/auth/register` | Công khai | `201`, `{id, fullName, contact}` và tạo session |
| `POST` | `/api/auth/login` | Công khai | `200`, `{id, fullName, contact}` và tạo session |
| `GET` | `/api/auth/me` | Đã đăng nhập | `200` tài khoản hiện tại; chưa đăng nhập `401` |
| `POST` | `/api/auth/logout` | Công khai | `204`, hủy session |
| `GET` | `/api/products` | Công khai | `200`, mảng sản phẩm |
| `POST` | `/api/products` | Đã đăng nhập | `201`, sản phẩm vừa tạo |
| `PUT` | `/api/products/{id}` | Đã đăng nhập | `200`, sản phẩm đã cập nhật |
| `DELETE` | `/api/products/{id}` | Đã đăng nhập | `204` |

### Ví dụ body

```json
{
  "fullName": "Nguyen Van An",
  "contact": "an@example.com",
  "password": "ShopPulse8",
  "confirmPassword": "ShopPulse8",
  "acceptTerms": true
}
```

```json
{
  "name": "Tai nghe PulseTune",
  "category": "Công nghệ",
  "price": 780000,
  "oldPrice": 950000,
  "image": "https://example.com/pulse.jpg",
  "quantity": 5
}
```

## 4. Ngoài phạm vi tuần 1

Thanh toán, đơn hàng, gửi email khôi phục mật khẩu, đăng nhập mạng xã hội, vai trò quản trị, bảo mật triển khai production, hiệu năng và kiểm thử bảo mật chuyên sâu chưa được triển khai. Giao diện danh mục trang chủ hiện có một số dữ liệu trình bày mẫu; CRUD sản phẩm được cung cấp qua REST API.
