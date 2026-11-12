# Bộ kịch bản kiểm thử ShopPulse

**Trạng thái mặc định:** Chưa chạy. Cập nhật thành Pass/Fail sau khi thực thi; không coi dữ liệu mong đợi là kết quả thực tế.

| ID | Ưu tiên | Chức năng | Tiền điều kiện | Các bước / dữ liệu | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|---|
| TC_SMOKE_01 | P1 | Khởi động | JDK 17, Maven, cổng 8080 trống | Chạy `mvn spring-boot:run`; mở `http://localhost:8080` | Ứng dụng khởi động, trang ShopPulse trả HTTP 200 | Chưa chạy |
| TC_SMOKE_02 | P1 | Danh sách sản phẩm | Ứng dụng đang chạy | `GET /api/products` | HTTP 200, JSON array; lần đầu có 3 sản phẩm demo | Chưa chạy |
| TC_AUTH_REG_01 | P1 | Đăng ký email | DB sạch hoặc email chưa dùng | `POST /api/auth/register`: tên `Nguyen Van An`, contact `qa1@example.com`, password/confirm `ShopPulse8`, acceptTerms `true` | HTTP 201; trả id/tên/contact, không có password/hash; có cookie session | Chưa chạy |
| TC_AUTH_REG_02 | P1 | Đăng ký số điện thoại | Số chưa dùng | Cùng body hợp lệ, contact `0912345678` | HTTP 201; contact được lưu và trả về | Chưa chạy |
| TC_AUTH_REG_03 | P1 | Contact rỗng | — | Register với contact ` ` | HTTP 400; tài khoản không được tạo | Chưa chạy |
| TC_AUTH_REG_04 | P1 | Email sai định dạng | — | Register với `an@invalid` | HTTP 400; tài khoản không được tạo | Chưa chạy |
| TC_AUTH_REG_05 | P2 | Phone sai định dạng | — | Register với `0123456789` | HTTP 400 vì đầu số không hợp lệ | Chưa chạy |
| TC_AUTH_REG_06 | P1 | Họ tên dưới biên | — | Register với fullName `A` | HTTP 400; không tạo tài khoản | Chưa chạy |
| TC_AUTH_REG_07 | P1 | Mật khẩu dưới biên | — | Register với password/confirm `Abc1234` (7 ký tự) | HTTP 400; không tạo tài khoản | Chưa chạy |
| TC_AUTH_REG_08 | P2 | Mật khẩu trên biên | — | Register với mật khẩu dài 73 ký tự giống nhau | HTTP 400; không tạo tài khoản | Chưa chạy |
| TC_AUTH_REG_09 | P1 | Xác nhận mật khẩu không khớp | — | password `ShopPulse8`, confirm `ShopPulse9` | HTTP 400; không tạo tài khoản | Chưa chạy |
| TC_AUTH_REG_10 | P1 | Không đồng ý điều khoản | — | Body hợp lệ nhưng `acceptTerms:false` | HTTP 400; không tạo tài khoản | Chưa chạy |
| TC_AUTH_REG_11 | P1 | Trùng contact | Tài khoản `qa1@example.com` đã tồn tại | Đăng ký lại cùng contact | HTTP 409; không tạo tài khoản thứ hai | Chưa chạy |
| TC_AUTH_REG_12 | P2 | Trùng contact khác hoa thường | Email `qa1@example.com` đã tồn tại | Đăng ký `QA1@EXAMPLE.COM` | HTTP 409 | Chưa chạy |
| TC_AUTH_LOGIN_01 | P1 | Đăng nhập email thành công | Đã đăng ký tài khoản | `POST /api/auth/login` với identifier email và password đúng | HTTP 200; thông tin công khai; session cookie được thiết lập | Chưa chạy |
| TC_AUTH_LOGIN_02 | P1 | Đăng nhập điện thoại thành công | Đã đăng ký bằng phone | Login với identifier phone/password đúng | HTTP 200; thông tin tài khoản đúng | Chưa chạy |
| TC_AUTH_LOGIN_03 | P1 | Sai mật khẩu | Tài khoản tồn tại | Login contact đúng, password sai | HTTP 401; thông báo chung, không tiết lộ contact có tồn tại hay không | Chưa chạy |
| TC_AUTH_LOGIN_04 | P1 | Tài khoản không tồn tại | — | Login với contact chưa đăng ký | HTTP 401; cùng thông báo như sai mật khẩu | Chưa chạy |
| TC_AUTH_SESSION_01 | P1 | Đọc session | Login/đăng ký thành công, giữ cookie | `GET /api/auth/me` cùng cookie | HTTP 200; id/tên/contact đúng | Chưa chạy |
| TC_AUTH_SESSION_02 | P1 | Không có session | Xóa cookie hoặc cửa sổ riêng | `GET /api/auth/me` | HTTP 401 | Chưa chạy |
| TC_AUTH_SESSION_03 | P1 | Đăng xuất | Đã đăng nhập | `POST /api/auth/logout`, sau đó gọi `/api/auth/me` bằng cookie cũ | Logout HTTP 204; `/me` trả HTTP 401 | Chưa chạy |
| TC_PRODUCT_01 | P1 | Tạo sản phẩm hợp lệ | Đã đăng nhập; tên chưa tồn tại | POST product `QA Phone`, `Công nghệ`, price `100`, oldPrice `120`, quantity `0` | HTTP 201; dữ liệu lưu chính xác; giá 100 và tồn kho 0 được chấp nhận | Chưa chạy |
| TC_PRODUCT_02 | P1 | Tạo sản phẩm chưa đăng nhập | Không có session | POST body hợp lệ | HTTP 401; không có sản phẩm mới | Chưa chạy |
| TC_PRODUCT_03 | P1 | Tên sản phẩm rỗng | Đã đăng nhập | POST với name ` ` | HTTP 400; không lưu | Chưa chạy |
| TC_PRODUCT_04 | P1 | Giá bằng 0 | Đã đăng nhập | POST với price `0` | HTTP 400; không lưu | Chưa chạy |
| TC_PRODUCT_05 | P1 | Giá âm | Đã đăng nhập | POST với price `-1` | HTTP 400; không lưu | Chưa chạy |
| TC_PRODUCT_06 | P1 | Số lượng âm | Đã đăng nhập | POST với quantity `-1` | HTTP 400; không lưu | Chưa chạy |
| TC_PRODUCT_07 | P2 | Giá gốc âm | Đã đăng nhập | POST với oldPrice `-1` | HTTP 400; không lưu | Chưa chạy |
| TC_PRODUCT_08 | P1 | Tên trùng | Đã tạo `QA Phone` | POST sản phẩm mới tên `QA Phone` | HTTP 409; số bản ghi không tăng | Chưa chạy |
| TC_PRODUCT_09 | P2 | Tên trùng khác hoa thường | Đã tạo `QA Phone` | POST tên `qa phone` | HTTP 409 | Chưa chạy |
| TC_PRODUCT_10 | P1 | Cập nhật sản phẩm | Đã đăng nhập; có ID hợp lệ | PUT `/api/products/{id}` thay price thành `200`, quantity thành `3` | HTTP 200; GET danh sách phản ánh giá và số lượng mới | Chưa chạy |
| TC_PRODUCT_11 | P1 | Cập nhật chưa đăng nhập | Không có session | PUT ID hợp lệ với body hợp lệ | HTTP 401; dữ liệu không đổi | Chưa chạy |
| TC_PRODUCT_12 | P1 | Cập nhật ID không tồn tại | Đã đăng nhập | PUT ID `999999` | HTTP 404 | Chưa chạy |
| TC_PRODUCT_13 | P1 | Xóa sản phẩm | Đã đăng nhập; có sản phẩm kiểm thử | DELETE ID vừa tạo, sau đó GET danh sách | DELETE HTTP 204; sản phẩm không còn trong danh sách | Chưa chạy |
| TC_PRODUCT_14 | P1 | Xóa chưa đăng nhập | Không có session | DELETE ID hợp lệ | HTTP 401; sản phẩm vẫn còn | Chưa chạy |
| TC_PRODUCT_15 | P2 | Xóa ID không tồn tại | Đã đăng nhập | DELETE ID `999999` | HTTP 404 | Chưa chạy |

## Ghi chú thực thi

- Trước khi chạy lại, thay `qa1@example.com` và tên `QA Phone` bằng dữ liệu duy nhất hoặc khởi động lại ứng dụng để reset H2.
- Với API cần đăng nhập, bật cookie jar để giữ `JSESSIONID` giữa các request.
- Lưu thời điểm, người chạy, phiên bản commit và bằng chứng (response/log) trong phần này sau khi bắt đầu thực thi.
