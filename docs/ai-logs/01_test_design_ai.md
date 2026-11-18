# Nhật ký AI — Thiết kế kiểm thử Tuần 1 (AU Badminton)

## Mục tiêu
Cập nhật đặc tả và bộ kiểm thử ban đầu theo ứng dụng AU Badminton được tích hợp trong repo.

## Prompt sử dụng
> Bạn là QA Lead. Dựa trên `docs/SPECIFICATIONS.md` của website AU Badminton, hãy lập test plan và test cases cho Tuần 1, ưu tiên khởi chạy local, đăng ký/đăng nhập, tra cứu sân, đặt sân, kiểm tra validation và quyền quản trị. Đánh dấu mọi ca là “Chưa chạy” cho tới khi được thực thi.

## Kết quả hỗ trợ
AI hỗ trợ cấu trúc hóa phạm vi, điều kiện môi trường, luồng chính và ca kiểm thử trong `docs/TEST_PLAN.md` và `docs/TEST_CASES.md`. Nội dung được đối chiếu với giao diện, controller và API hiện có; ca kiểm thử chưa được chạy trong lần cập nhật này.

## Ghi chú
Database dùng SQL Server `AuDB`. Chạy `docs/database/migration_booking_detail.sql` trong SSMS trước khi khởi động ứng dụng. Không ghi thông tin đăng nhập cá nhân hoặc mật khẩu database vào tài liệu này.
