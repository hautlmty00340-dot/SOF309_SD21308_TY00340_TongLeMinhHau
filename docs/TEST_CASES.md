# Test Cases — AU Badminton (Tuần 1)

**Trạng thái ban đầu:** Chưa chạy. Chỉ cập nhật trạng thái sau khi thực thi và lưu bằng chứng.

**Quy tắc nghiệp vụ đối chiếu từ mã nguồn (áp dụng cho các ca dưới):**
- Đăng ký: `fullname` 2–100 ký tự (không chứa `<`/`>`), `username` khớp `[A-Za-z0-9_]{3,30}`, `password` 8–72 ký tự, `phone` khớp `\+?[0-9]{9,15}`; `username` hoặc `phone` đã tồn tại → `409 Conflict`.
- Đăng nhập sai tài khoản/mật khẩu → `401 Unauthorized`.
- Đặt sân (`POST /api/bookings`) chỉ chấp nhận tài khoản đã đăng nhập với **role = user**; tài khoản admin gọi API này bị từ chối `403`.
- Thời gian đặt sân phải **ở tương lai** (`ngày+giờ bắt đầu > thời điểm hiện tại`), độ dài 30–720 phút, và nằm trong giờ mở cửa cấu hình ở `app_settings`.
- Khung giờ trùng với booking `PENDING`/`CONFIRMED` cùng sân, cùng ngày → `409 Conflict`.
- Đổi trạng thái booking (`PATCH /api/bookings/{id}/status`): khách hàng chỉ được chuyển sang `CANCELLED` (không được tự xác nhận `CONFIRMED`); chỉ admin mới xác nhận được; không thể đổi trạng thái của đơn đã `CANCELLED`.
- Các API quản trị sân (`POST`/`PUT`/`PATCH .../iot`/`DELETE` trên `/api/courts`) yêu cầu session có `role = admin`, ngược lại trả `403`.

| ID | Tính năng | Tiền điều kiện | Dữ liệu / thao tác | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|
| W1-ENV-01 | Khởi động | JDK 17, Maven, SQL Server đã sẵn sàng; AuDB đã migrate | Chạy `mvn spring-boot:run` | Spring Boot khởi động không lỗi và lắng nghe cổng 8080 | Chưa chạy |
| W1-ENV-02 | Trang chủ | Ứng dụng chạy | Mở `http://localhost:8080/` | Trang chủ AU Badminton hiển thị, tài nguyên CSS/JS/ảnh tải được | Chưa chạy |
| W1-ENV-03 | Health API | Ứng dụng và DB chạy | Gửi `GET /api/health` | HTTP 200, body `success:true`, `status:"OK"`, có trường `database` và `timestamp` | Chưa chạy |
| W1-COURT-01 | Danh sách sân | Có dữ liệu sân | Mở trang danh sách hoặc `GET /api/courts` | HTTP 200; hiển thị các sân cùng loại sân, giá và trạng thái `live_status` (AVAILABLE/OCCUPIED) phù hợp | Chưa chạy |
| W1-COURT-02 | Chi tiết sân hợp lệ | Có sân với ID hợp lệ | Mở `GET /api/courts/{id}` | HTTP 200; trả đúng thông tin sân và danh sách `bookingsToday` | Chưa chạy |
| W1-COURT-03 | Chi tiết sân không tồn tại | Dùng ID không có trong DB (ví dụ `/api/courts/999999`) | Mở `GET /api/courts/999999` | HTTP 404; `success:false`, thông báo "Không tìm thấy sân" | Chưa chạy |
| W1-AUTH-01 | Đăng ký hợp lệ | Số điện thoại và tên đăng nhập chưa tồn tại | Điền form đăng ký đúng quy tắc (username 3-30 ký tự, password ≥8, phone hợp lệ) | HTTP 201; tạo tài khoản role `user`, hiển thị thông báo thành công | Chưa chạy |
| W1-AUTH-02 | Đăng ký thiếu/sai dữ liệu | Đang ở trang đăng ký | Bỏ trống trường bắt buộc, hoặc username <3 ký tự, hoặc password <8 ký tự, hoặc SĐT sai định dạng | HTTP 400; hiển thị lỗi validation; không tạo tài khoản | Chưa chạy |
| W1-AUTH-03 | Đăng ký trùng tài khoản/SĐT | Đã có user với `username` hoặc `phone` này | Đăng ký lại với cùng `username` hoặc cùng `phone` | HTTP 409; thông báo "Tên đăng nhập hoặc số điện thoại đã được sử dụng"; không tạo bản ghi mới | Chưa chạy |
| W1-AUTH-04 | Đăng nhập hợp lệ | Tài khoản tồn tại | Nhập đúng username/password | HTTP 200; tạo session, trả về thông tin `user`, chuyển tới trang phù hợp theo role | Chưa chạy |
| W1-AUTH-05 | Đăng nhập sai | Tài khoản tồn tại | Nhập sai mật khẩu hoặc username không tồn tại | HTTP 401; từ chối đăng nhập và hiển thị "Sai tài khoản hoặc mật khẩu" | Chưa chạy |
| W1-AUTH-06 | Đăng xuất | Đang đăng nhập | Gọi `POST /api/auth/logout`, sau đó gọi `GET /api/auth/me` | Session bị huỷ (invalidate); `/api/auth/me` trả HTTP 401 "Phiên đăng nhập đã hết hạn" | Chưa chạy |
| W1-BOOK-01 | Đặt sân hợp lệ | Đăng nhập với tài khoản **role = user**; sân còn trống ở khung giờ tương lai hợp lệ (30–720 phút, trong giờ mở cửa) | `POST /api/bookings` với court_id, date, start_time, end_time, payment_method hợp lệ | HTTP 201; booking được lưu với trạng thái `PENDING` (hoặc `CONFIRMED` nếu `auto_approve=true`), trả `booking_id` và `court_amount` | Chưa chạy |
| W1-BOOK-02 | Thời gian không hợp lệ | Đang đăng nhập user, ở form đặt sân | Chọn giờ kết thúc trước hoặc bằng giờ bắt đầu, hoặc thời lượng ngoài 30–720 phút | HTTP 400; request bị từ chối; không tạo booking | Chưa chạy |
| W1-BOOK-03 | Đặt sân trong quá khứ | Đăng nhập user | Chọn ngày/giờ đã qua so với thời điểm hiện tại | HTTP 400; thông báo yêu cầu chọn khung giờ trong tương lai; không tạo booking | Chưa chạy |
| W1-BOOK-04 | Trùng lịch | Cùng sân/ngày/giờ đã có booking `PENDING`/`CONFIRMED` | Gửi booking trùng slot (giờ giao nhau) | HTTP 409; hệ thống từ chối và không tạo booking thứ hai | Chưa chạy |
| W1-BOOK-05 | Admin đặt sân bị từ chối | Đăng nhập với tài khoản **role = admin** | Gọi `POST /api/bookings` như một booking hợp lệ | HTTP 403; thông báo tài khoản admin không thể đặt sân | Chưa chạy |
| W1-BOOK-06 | Lịch sử đặt sân | Người dùng đã có booking | Mở trang lịch sử hoặc `GET /api/bookings` khi đã đăng nhập | HTTP 200; chỉ hiển thị booking thuộc số điện thoại của người dùng hiện tại | Chưa chạy |
| W1-ADMIN-01 | Quyền quản trị sân | Chưa đăng nhập hoặc đăng nhập user thường | Gọi API quản trị thêm/sửa/xoá sân hoặc đổi đèn IoT (`/api/courts`, `.../iot`) | HTTP 403; yêu cầu bị từ chối; dữ liệu không đổi | Chưa chạy |
| W1-ADMIN-02 | Admin xác nhận booking | Đăng nhập admin; có booking trạng thái `PENDING` | `PATCH /api/bookings/{id}/status` với `status=CONFIRMED` | HTTP 200; trạng thái chuyển thành `CONFIRMED` và phản ánh ở danh sách | Chưa chạy |
| W1-ADMIN-03 | Khách hàng không tự xác nhận booking | Đăng nhập user, là chủ booking trạng thái `PENDING` | `PATCH /api/bookings/{id}/status` với `status=CONFIRMED` | HTTP 403; chỉ cho phép khách hàng hủy (`CANCELLED`), không cho tự xác nhận | Chưa chạy |

## Ghi nhận khi chạy

| Test ID | Ngày chạy | Người chạy | Kết quả thực tế | Bằng chứng / ghi chú |
|---|---|---|---|---|
| — | — | — | Chưa chạy | — |
