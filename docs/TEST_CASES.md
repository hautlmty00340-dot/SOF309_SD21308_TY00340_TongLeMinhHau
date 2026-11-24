# Test Cases — AU Badminton (Tuần 1)

**Trạng thái ban đầu:** Chưa chạy. Chỉ cập nhật trạng thái sau khi thực thi và lưu bằng chứng.

## Quy tắc và dữ liệu dùng chung

- Chạy trên SQL Server `AuDB` đã migrate bằng `docs/database/migration_booking_detail.sql`; ứng dụng đọc cấu hình từ `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- API tạo tài khoản: `POST /api/auth/register`; đăng nhập: `POST /api/auth/login`; xem phiên: `GET /api/auth/me`; đăng xuất: `POST /api/auth/logout`.
- Đăng ký yêu cầu `fullname` 2–100 ký tự, không chứa `<`/`>`; `username` khớp `[A-Za-z0-9_]{3,30}`; `password` 8–72 ký tự; `phone` khớp `\+?[0-9]{9,15}`. Username hoặc phone trùng trả HTTP 409.
- Đăng nhập thành công tạo HTTP session. Các request cần giữ nguyên cookie/session để kiểm tra xác thực.
- Tạo booking chỉ cho user đã đăng nhập (không phải admin); ngày/giờ bắt đầu phải ở tương lai theo `Asia/Ho_Chi_Minh`; thời lượng 30–720 phút; phương thức thanh toán thuộc `BANK_TRANSFER`, `VNPAY`, `MOMO`, `CASH`; giờ đặt nằm trong cấu hình mở cửa (mặc định 06:00–23:00). Sân không tồn tại trả 404. Booking `PENDING`/`CONFIRMED` có thời gian giao nhau trên cùng sân/ngày trả 409; `CANCELLED` không chặn.
- Khi `auto_approve=false` (mặc định), booking mới là `PENDING`; nếu `auto_approve=true`, là `CONFIRMED`. Số tiền bằng giá sân/giờ nhân thời lượng và được làm tròn 2 chữ số thập phân.
- `GET /api/bookings` với user chỉ trả booking theo số điện thoại trong session; tham số `phone` từ client không thay đổi bộ lọc. Chưa đăng nhập gọi API danh sách trả HTTP 403; chưa đăng nhập tạo booking trả HTTP 401.
- Trong mọi ca dùng dữ liệu mới hoặc booking fixture riêng, ghi lại ngày/giờ, ID và người dùng thực tế. Không phụ thuộc cố định vào dữ liệu demo có ngày dựa trên `GETDATE()`.
- Ở các ví dụ có `<suffix>`, thay placeholder bằng chuỗi số duy nhất (ví dụ `4821`); không gửi nguyên dấu ngoặc nhọn. Khi tạo phone, bảo đảm tổng số chữ số là 9–15.

## Test cases

| ID | Tính năng | Tiền điều kiện | Dữ liệu / thao tác | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|
| W1-ENV-01 | Khởi động | JDK 17, Maven 3.8+, SQL Server đang chạy; migration đã tạo `AuDB`; biến kết nối đúng | Chạy `mvn spring-boot:run` từ thư mục gốc | Spring Boot khởi động không lỗi và lắng nghe `127.0.0.1:8080` | Chưa chạy |
| W1-ENV-02 | Trang chủ và tài nguyên | Ứng dụng đang chạy | Mở `http://localhost:8080/`; kiểm tra trang và request HTML/CSS/JS/ảnh trong DevTools Network | Trang AU Badminton hiển thị; tài nguyên giao diện được tải thành công | Chưa chạy |
| W1-ENV-03 | Health API khi DB hoạt động | Ứng dụng và `AuDB` hoạt động | Gửi `GET /api/health` | HTTP 200; body `success=true`, `status="OK"`, có `database` và `timestamp` | Chưa chạy |
| W1-ENV-04 | Health API khi DB lỗi | Ứng dụng đang chạy nhưng cố ý cấu hình DB sai hoặc DB không khả dụng | Gửi `GET /api/health` | HTTP 200; body `success=false`, `status="ERROR"`, có thông báo không kết nối được DB. Không coi chỉ HTTP 200 là health thành công | Chưa chạy |
| W1-COURT-01 | Danh sách sân | Có ít nhất một sân trong DB | Gọi `GET /api/courts`; đối chiếu tên, loại sân, giá và `live_status` | HTTP 200; dữ liệu sân đầy đủ. `live_status="OCCUPIED"` chỉ khi có booking `CONFIRMED` bao phủ thời điểm hiện tại; booking `PENDING` không làm sân thành `OCCUPIED` | Chưa chạy |
| W1-COURT-02 | Chi tiết sân hợp lệ | Có sân ID `C` trong DB; biết booking của sân `C` trong ngày kiểm tra | Gọi `GET /api/courts/C` | HTTP 200; trả đúng sân; `bookingsToday` chỉ gồm lịch `PENDING`/`CONFIRMED` của sân `C` trong ngày theo SQL Server | Chưa chạy |
| W1-COURT-03 | Chi tiết sân không tồn tại | Xác nhận ID `C` không có trong DB trước khi chạy | Gọi `GET /api/courts/C` | HTTP 404; body `success=false`, thông báo `Không tìm thấy sân` | Chưa chạy |
| W1-AUTH-01 | Đăng ký hợp lệ | Username và phone chưa tồn tại; dùng suffix duy nhất cho lần chạy | Gửi form/API với `fullname="Test User"`, username `test_<suffix>`, password `TestPass123`, phone `+84912345678<suffix>` (đảm bảo tổng 9–15 chữ số) | HTTP 201; thông báo thành công; tài khoản được tạo với role `user` | Chưa chạy |
| W1-AUTH-02 | Đăng ký thiếu trường | Đang ở trang đăng ký | Bỏ trống lần lượt từng trường bắt buộc `fullname`, `username`, `password`, `phone`; mỗi lần chỉ thay một trường | HTTP 400 cho từng request; giao diện/API báo validation; không tạo tài khoản | Chưa chạy |
| W1-AUTH-03 | Biên fullname | Username/phone mới ở mỗi lần đăng ký | Thử riêng fullname 1, 2, 100, 101 ký tự và fullname chứa `<` hoặc `>`; các trường còn lại hợp lệ | Fullname 1/101 ký tự hoặc chứa `<`/`>` → HTTP 400; fullname 2/100 ký tự → HTTP 201 | Chưa chạy |
| W1-AUTH-04 | Biên username và password | Username/phone mới ở mỗi lần đăng ký | Thử username sai ký tự và độ dài 2/3/30/31; thử password độ dài 7/8/72/73; mỗi request chỉ có một giá trị cần kiểm tra | Username 3–30 ký tự chữ/số/`_` và password 8–72 ký tự → HTTP 201; ngoài giới hạn hoặc sai mẫu → HTTP 400 | Chưa chạy |
| W1-AUTH-05 | Định dạng phone | Username mới ở mỗi lần đăng ký | Thử phone 8/9/15/16 chữ số, có ký tự chữ, hoặc dấu `+` không ở đầu; mỗi request có các trường khác hợp lệ | `+` tùy chọn ở đầu và 9–15 chữ số → HTTP 201; các trường hợp còn lại → HTTP 400 | Chưa chạy |
| W1-AUTH-06 | Trùng username | Đã có tài khoản username `U`; phone lần thử là phone mới | Đăng ký lại với username `U` và phone khác | HTTP 409; thông báo tài khoản đã được sử dụng; không tạo bản ghi mới | Chưa chạy |
| W1-AUTH-07 | Trùng phone | Đã có tài khoản phone `P`; username lần thử là username mới | Đăng ký với phone `P` và username khác | HTTP 409; thông báo tài khoản/phone đã được sử dụng; không tạo bản ghi mới | Chưa chạy |
| W1-AUTH-08 | Đăng nhập hợp lệ và giữ session | Có tài khoản user hợp lệ, ví dụ tài khoản vừa tạo ở `W1-AUTH-01` | Đăng nhập đúng username/password; giữ cookie; gọi `GET /api/auth/me` bằng cùng cookie | Login HTTP 200, body có user; `/me` HTTP 200 và trả đúng user/role; nếu kiểm tra UI thì user được chuyển tới trang phù hợp | Chưa chạy |
| W1-AUTH-09 | Đăng nhập sai | Có một username hợp lệ | Chạy riêng: username đúng/password sai; username không tồn tại/password bất kỳ | Mỗi lần HTTP 401, thông báo `Sai tài khoản hoặc mật khẩu`; không tạo phiên xác thực | Chưa chạy |
| W1-AUTH-10 | Đăng xuất | Đã đăng nhập và có cookie session | Gọi `POST /api/auth/logout`, sau đó gọi `GET /api/auth/me` với cùng cookie jar | Logout HTTP 200; `/me` HTTP 401, thông báo `Phiên đăng nhập đã hết hạn` | Chưa chạy |
| W1-BOOK-01 | Đặt sân hợp lệ | Đã đăng nhập user; sân tồn tại và slot trống; giờ nằm trong giờ mở cửa; biết `auto_approve` | Gửi `POST /api/bookings` với `court_id` hợp lệ, ngày D+7, giờ tương lai 60 phút, payment `BANK_TRANSFER` | HTTP 201; có `booking_id`, `court_amount`; booking được lưu. Trạng thái là `PENDING` nếu `auto_approve=false`, hoặc `CONFIRMED` nếu true; amount khớp giá sân và thời lượng | Chưa chạy |
| W1-BOOK-02 | Đặt sân thiếu/sai trường | Đã đăng nhập user; sân và thời gian khác hợp lệ | Gửi riêng từng request thiếu/sai `court_id`, `date`, `start_time`, `end_time`, hoặc `payment_method` | HTTP 400; thông báo lỗi phù hợp; không tạo booking | Chưa chạy |
| W1-BOOK-03 | Thời lượng không hợp lệ | Đã đăng nhập user; sân tồn tại; thời điểm trong tương lai và trong giờ mở cửa | Thử riêng: end bằng start; end trước start; thời lượng 29 phút; 721 phút | Mỗi request HTTP 400; không tạo booking | Chưa chạy |
| W1-BOOK-04 | Thời lượng biên hợp lệ | Đã đăng nhập user; sân có slot trống; thời gian trong tương lai và giờ mở cửa | Thử đặt riêng 30 phút và 720 phút | HTTP 201 cho mỗi thời lượng; booking được lưu với amount tính đúng | Chưa chạy |
| W1-BOOK-05 | Đặt sân trong quá khứ | Đã đăng nhập user; sân tồn tại; các trường khác hợp lệ | Chọn ngày/giờ bắt đầu đã qua theo `Asia/Ho_Chi_Minh` | HTTP 400; thông báo yêu cầu chọn giờ tương lai; không tạo booking | Chưa chạy |
| W1-BOOK-06 | Ngoài giờ mở cửa | Đã đăng nhập user; biết giờ mở/đóng trong cấu hình | Chạy riêng: bắt đầu trước giờ mở cửa; kết thúc sau giờ đóng cửa | HTTP 400; thông báo khung giờ hoạt động; không tạo booking | Chưa chạy |
| W1-BOOK-07 | Sân không tồn tại khi đặt | Đã đăng nhập user; ID sân không có trong DB; các trường còn lại hợp lệ | Gửi `POST /api/bookings` với ID đó | HTTP 404; thông báo sân không tồn tại; không tạo booking | Chưa chạy |
| W1-BOOK-08 | Trùng lịch đang hiệu lực | Đã đăng nhập user; có fixture booking `PENDING` hoặc `CONFIRMED` trong tương lai | Gửi booking cùng sân/ngày với thời gian giao nhau với fixture | HTTP 409; thông báo slot đã có lịch; không tạo booking thứ hai | Chưa chạy |
| W1-BOOK-09 | Ranh giới slot và booking đã hủy | Đã đăng nhập user; có fixture booking trong tương lai | Thử slot bắt đầu đúng lúc booking trước kết thúc; thử slot giao với booking `CANCELLED` | Cả hai request không bị lỗi trùng lịch (các điều kiện khác hợp lệ); booking mới được tạo | Chưa chạy |
| W1-BOOK-10 | Admin không được đặt sân | Đã đăng nhập admin; payload đặt sân hợp lệ | Gửi `POST /api/bookings` với sân/slot/payment hợp lệ | HTTP 403; thông báo admin không thể đặt sân; không tạo booking | Chưa chạy |
| W1-BOOK-11 | Khách chưa đăng nhập tạo booking | Không có session đăng nhập; payload còn lại hợp lệ | Gửi `POST /api/bookings` | HTTP 401; yêu cầu đăng nhập; không tạo booking | Chưa chạy |
| W1-BOOK-12 | Lịch sử chỉ hiện booking của user hiện tại | Có hai user A/B với số điện thoại khác nhau và booking riêng | Đăng nhập A; gọi `GET /api/bookings`, sau đó thử `GET /api/bookings?phone=<phone-B>` | Cả hai response HTTP 200 nhưng chỉ chứa booking của A; tham số phone client không cho phép đọc lịch B | Chưa chạy |
| W1-BOOK-13 | Xem booking của user khác | Có booking thuộc user B; đăng nhập user A | Gọi `GET /api/bookings/{id-B}` | HTTP 403; không trả thông tin booking của B | Chưa chạy |
| W1-BOOK-14 | Khách chưa đăng nhập xem danh sách | Không có session | Gọi `GET /api/bookings` | HTTP 403; body báo tài khoản chưa có số điện thoại; không trả dữ liệu booking | Chưa chạy |
| W1-ADMIN-01 | User/khách bị chặn khỏi API quản lý sân | Chưa đăng nhập hoặc đăng nhập user thường; ghi lại dữ liệu sân trước test | Với mỗi trạng thái đăng nhập, gọi riêng POST thêm sân, PUT sửa sân, PATCH IoT và DELETE sân | Mỗi request HTTP 403; dữ liệu sân và trạng thái IoT không đổi | Chưa chạy |
| W1-ADMIN-02 | Admin quản lý sân | Đã đăng nhập admin; có sân fixture không ảnh hưởng dữ liệu demo | Tạo sân mới, sửa sân, đổi trạng thái IoT, rồi xóa sân fixture (mỗi thao tác xác minh riêng) | API trả thành công tương ứng; dữ liệu thay đổi đúng; xóa fixture không có booking thành công | Chưa chạy |
| W1-ADMIN-03 | Admin xác nhận booking | Đã đăng nhập admin; có booking fixture `PENDING` | PATCH `/api/bookings/{id}/status` với `status=CONFIRMED`; đọc lại booking | PATCH HTTP 200; booking chuyển thành `CONFIRMED` trong API/DB | Chưa chạy |
| W1-ADMIN-04 | User không tự xác nhận booking của mình | Đã đăng nhập user là chủ booking fixture `PENDING` | PATCH `/api/bookings/{id}/status` với `status=CONFIRMED`; đọc lại booking | HTTP 403; thông báo chỉ admin được xác nhận; trạng thái vẫn `PENDING` | Chưa chạy |
| W1-ADMIN-05 | User không sửa booking người khác | Đã đăng nhập user A; booking fixture `PENDING` thuộc user B | PATCH booking B sang `CANCELLED` | HTTP 403; trạng thái booking B không đổi | Chưa chạy |
| W1-ADMIN-06 | User hủy booking của mình | Đã đăng nhập user là chủ booking fixture trong tương lai, trạng thái `PENDING` hoặc `CONFIRMED` | PATCH trạng thái booking sang `CANCELLED`; đọc lại booking | HTTP 200; trạng thái chuyển thành `CANCELLED` | Chưa chạy |
| W1-ADMIN-07 | Chuyển trạng thái không hợp lệ/đơn đã hủy | Đã đăng nhập admin; có booking fixture `CANCELLED` | Thử đổi đơn đã hủy sang `CONFIRMED`; thử status không hỗ trợ như `PENDING` | Đổi từ `CANCELLED` trả HTTP 409; status không hỗ trợ trả HTTP 400; trạng thái không đổi | Chưa chạy |
| W1-ADMIN-08 | User hủy booking đã bắt đầu | Đã đăng nhập user là chủ booking đã bắt đầu | PATCH trạng thái sang `CANCELLED` | HTTP 409; thông báo không thể hủy lịch đã bắt đầu; trạng thái không đổi | Chưa chạy |

## Ghi nhận khi chạy

| Test ID | Ngày chạy | Người chạy | Kết quả thực tế | Bằng chứng / ghi chú |
|---|---|---|---|---|
| — | — | — | Chưa chạy | — |
