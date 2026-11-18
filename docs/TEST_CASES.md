# Test Cases — AU Badminton (Tuần 1)

**Trạng thái ban đầu:** Chưa chạy. Chỉ cập nhật trạng thái sau khi thực thi và lưu bằng chứng.

| ID | Tính năng | Tiền điều kiện | Dữ liệu / thao tác | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|
| W1-ENV-01 | Khởi động | JDK 17, Maven, SQL Server đã sẵn sàng; AuDB đã migrate | Chạy `mvn spring-boot:run` | Spring Boot khởi động không lỗi và lắng nghe cổng 8080 | Chưa chạy |
| W1-ENV-02 | Trang chủ | Ứng dụng chạy | Mở `http://localhost:8080/` | Trang chủ AU Badminton hiển thị, tài nguyên CSS/JS/ảnh tải được | Chưa chạy |
| W1-ENV-03 | Health API | Ứng dụng và DB chạy | Gửi `GET /api/health` | Trả phản hồi trạng thái ứng dụng/DB thành công | Chưa chạy |
| W1-COURT-01 | Danh sách sân | Có dữ liệu sân | Mở trang danh sách hoặc `GET /api/courts` | Hiển thị các sân cùng loại sân, giá và trạng thái phù hợp | Chưa chạy |
| W1-COURT-02 | Chi tiết sân | Có sân với ID hợp lệ | Mở chi tiết sân | Hiển thị đúng thông tin sân và luồng chọn lịch | Chưa chạy |
| W1-AUTH-01 | Đăng ký hợp lệ | Số điện thoại chưa đăng ký | Điền form đăng ký hợp lệ | Tạo tài khoản, hiển thị thông báo thành công | Chưa chạy |
| W1-AUTH-02 | Đăng ký thiếu dữ liệu | Đang ở trang đăng ký | Bỏ trống trường bắt buộc và gửi | Hiển thị lỗi validation; không tạo tài khoản | Chưa chạy |
| W1-AUTH-03 | Đăng nhập hợp lệ | Tài khoản tồn tại | Nhập thông tin hợp lệ | Tạo session và chuyển tới trang phù hợp | Chưa chạy |
| W1-AUTH-04 | Đăng nhập sai | Tài khoản tồn tại | Nhập sai mật khẩu | Từ chối đăng nhập và hiển thị thông báo lỗi | Chưa chạy |
| W1-AUTH-05 | Đăng xuất | Đang đăng nhập | Chọn đăng xuất, sau đó gọi `/api/auth/me` | Session bị huỷ; endpoint trả về trạng thái chưa đăng nhập | Chưa chạy |
| W1-BOOK-01 | Đặt sân hợp lệ | Sân còn trống ở thời gian chọn | Chọn ngày/giờ hợp lệ, nhập thông tin liên hệ, gửi đặt | Booking được lưu và người dùng nhận xác nhận | Chưa chạy |
| W1-BOOK-02 | Thời gian không hợp lệ | Đang ở form đặt sân | Chọn giờ kết thúc trước hoặc bằng giờ bắt đầu | Request bị từ chối; không tạo booking | Chưa chạy |
| W1-BOOK-03 | Trùng lịch | Cùng sân/ngày/giờ đã có booking hiệu lực | Gửi booking trùng slot | Hệ thống từ chối và không tạo booking thứ hai | Chưa chạy |
| W1-BOOK-04 | Lịch sử đặt sân | Người dùng đã có booking | Mở trang lịch sử | Hiển thị booking thuộc người dùng hiện tại | Chưa chạy |
| W1-ADMIN-01 | Quyền quản trị | Chưa đăng nhập hoặc đăng nhập user thường | Gọi API quản trị thêm/sửa/xoá sân | Yêu cầu bị từ chối; dữ liệu không đổi | Chưa chạy |
| W1-ADMIN-02 | Cập nhật trạng thái booking | Đăng nhập admin, có booking | Đổi trạng thái booking | Trạng thái được cập nhật và phản ánh ở danh sách | Chưa chạy |

## Ghi nhận khi chạy

| Test ID | Ngày chạy | Người chạy | Kết quả thực tế | Bằng chứng / ghi chú |
|---|---|---|---|---|
| — | — | — | Chưa chạy | — |
