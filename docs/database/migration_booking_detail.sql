-- AU Badminton - Chạy trong SQL Server Management Studio (F5).
-- AN TOÀN: KHÔNG DROP DATABASE/BẢNG, KHÔNG XÓA DỮ LIỆU CŨ.
-- Chỉ tạo AuDB/bảng thiếu; nạp dữ liệu demo nếu bảng trống.
USE master;
GO
IF DB_ID(N'AuDB') IS NULL CREATE DATABASE AuDB;
GO
USE AuDB;
GO
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
       id INT IDENTITY(1,1) PRIMARY KEY,
       fullname NVARCHAR(255) NOT NULL,
       username NVARCHAR(100) NOT NULL UNIQUE,
       password NVARCHAR(255) NOT NULL,
       role NVARCHAR(50) NOT NULL DEFAULT N'user',
       phone VARCHAR(20) NOT NULL DEFAULT ''
    );
END;
IF OBJECT_ID(N'dbo.courts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.courts (
       court_id INT IDENTITY(1,1) PRIMARY KEY,
       name NVARCHAR(100) NOT NULL UNIQUE,
       court_type NVARCHAR(50) NOT NULL DEFAULT N'Standard',
       iot_light_status TINYINT NOT NULL DEFAULT 0,
       price_per_hour DECIMAL(10,2) NOT NULL DEFAULT 100000
    );
END;
IF OBJECT_ID(N'dbo.bookings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.bookings (
       booking_id INT IDENTITY(1,1) PRIMARY KEY,
       court_id INT NOT NULL FOREIGN KEY REFERENCES dbo.courts(court_id),
       [date] DATE NOT NULL,
       start_time TIME NOT NULL,
       end_time TIME NOT NULL,
       customer_name NVARCHAR(100) DEFAULT N'Khách Lẻ',
       customer_phone VARCHAR(20) DEFAULT '',
       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
       court_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
       reward_points INT NOT NULL DEFAULT 0,
       payment_method NVARCHAR(50) DEFAULT N'BANK_TRANSFER',
       deposit_image NVARCHAR(500) NOT NULL DEFAULT '',
       admin_notes NVARCHAR(1000) NOT NULL DEFAULT '',
       created_at DATETIME NOT NULL DEFAULT GETDATE()
    );
END;
GO
-- Di trú database cũ khi thiếu một số cột, không thay đổi lịch đã đặt.
IF COL_LENGTH('dbo.bookings','reward_points') IS NULL ALTER TABLE dbo.bookings ADD reward_points INT DEFAULT 0;
IF COL_LENGTH('dbo.bookings','payment_method') IS NULL ALTER TABLE dbo.bookings ADD payment_method NVARCHAR(50) DEFAULT N'BANK_TRANSFER';
IF COL_LENGTH('dbo.bookings','court_amount') IS NULL ALTER TABLE dbo.bookings ADD court_amount DECIMAL(10,2) DEFAULT 0;
IF COL_LENGTH('dbo.bookings','deposit_image') IS NULL ALTER TABLE dbo.bookings ADD deposit_image NVARCHAR(500) DEFAULT '';
IF COL_LENGTH('dbo.bookings','admin_notes') IS NULL ALTER TABLE dbo.bookings ADD admin_notes NVARCHAR(1000) DEFAULT '';
IF COL_LENGTH('dbo.bookings','created_at') IS NULL ALTER TABLE dbo.bookings ADD created_at DATETIME DEFAULT GETDATE();
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_bookings_phone' AND object_id=OBJECT_ID('dbo.bookings'))
    CREATE INDEX idx_bookings_phone ON dbo.bookings(customer_phone);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_bookings_date' AND object_id=OBJECT_ID('dbo.bookings'))
    CREATE INDEX idx_bookings_date ON dbo.bookings([date]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_bookings_slot' AND object_id=OBJECT_ID('dbo.bookings'))
    CREATE INDEX idx_bookings_slot ON dbo.bookings(court_id,[date],status,start_time,end_time);
GO
-- Khi có dữ liệu trùng số điện thoại từ bản cũ, không ép UNIQUE để tránh làm hỏng migration.
-- Khi cài mới, index này bảo đảm một số điện thoại chỉ thuộc một tài khoản.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_users_phone_unique' AND object_id=OBJECT_ID('dbo.users'))
 AND NOT EXISTS (SELECT phone FROM dbo.users WHERE phone <> '' GROUP BY phone HAVING COUNT(*) > 1)
BEGIN
    CREATE UNIQUE INDEX idx_users_phone_unique ON dbo.users(phone) WHERE phone <> '';
END;
GO
-- Các tài khoản DEMO được nạp ở dạng PBKDF2, không lưu mật khẩu rõ.
-- Tài khoản cũ cũng được nâng cấp hash tự động tại lần đăng nhập tiếp theo.
IF NOT EXISTS (SELECT 1 FROM dbo.users)
BEGIN
INSERT INTO users (fullname, username, password, role, phone) VALUES 
(N'System Admin', 'admin', 'pbkdf2$120000$ztM6Jo7/PmOF6dPljJSyqg==$AHeVUy3VjTc+ixu5RRViq9DvwEjSER1T6xHTEmhFdG4=', 'admin', '0999888777'),
(N'Nguyễn Văn Khách', 'khach01', 'pbkdf2$120000$UK9PIJxMbZkph0+UpKLafw==$cKOBmRM4U8eJUv4YEgCzD6MQLY1yHx8eg6o3QIoR58Q=', 'user', '0901234567'),
(N'Trần Thu Hà', 'thuha99', 'pbkdf2$120000$1gTc0o4T7GvnFsEx1bnLQQ==$/C+qhibrFTmO2XtHgIL4I23oG+RXNBgCzgX+j77SPgE=', 'user', '0988111222'),
(N'Lê Minh Hoàng', 'hoangle', 'pbkdf2$120000$vBNUMEk2TMz+O3Y48vIhiA==$OiEErhlw2JYKM2hxoDe+f2ks0p7xm4rrqsjAbUFG7u4=', 'user', '0912333444'),
(N'Phạm Quang Dũng', 'dungpq', 'pbkdf2$120000$49PYPIzPXM15XfKU/dUKHw==$bf+LrrU8U0OxwliCWjKALlxCHgvr6/GaZMXmjYNCECE=', 'user', '0977555666'),
(N'Đội Cầu Lông FPT', 'teama', 'pbkdf2$120000$Rjnl/4FPLtUsKTS7smzPVQ==$MCd78moHF6sl9YgBKUaolH+I0i6IyODVtAdK71GsIeU=', 'user', '0944999888'),
(N'Khách Vãng Lai', 'guest01', 'pbkdf2$120000$YV2Mcg9IBmrJW1WYW8yvKw==$PVQ+mOjQOuKCXgRZJGzBF4iYhx669J32B8CyXCpCUi0=', 'user', '0900000000');
END;
GO
IF NOT EXISTS (SELECT 1 FROM dbo.courts)
BEGIN
INSERT INTO courts (name, court_type, iot_light_status, price_per_hour) VALUES 
(N'Sân Thảm 1 VIP', N'Yonex VIP', 2, 150000),
(N'Sân Thảm 2 VIP', N'Yonex VIP', 2, 150000),
(N'Sân Thảm 3 Standard', N'Standard', 0, 100000),
(N'Sân Thảm 4 Standard', N'Standard', 0, 100000),
(N'Sân Thảm 5 Standard', N'Standard', 2, 100000),
(N'Sân Thảm 6 Khởi Động', N'Standard', 1, 80000);
END;
GO
IF NOT EXISTS (SELECT 1 FROM dbo.bookings)
BEGIN
INSERT INTO bookings (court_id, date, start_time, end_time, customer_name, customer_phone, status, court_amount, payment_method, admin_notes) VALUES 
(1, GETDATE(), '17:00', '19:00', N'Nguyễn Văn Khách', '0901234567', 'CONFIRMED', 300000, 'BANK_TRANSFER', N'Khách quen, đã cọc 50%'),
(2, GETDATE(), '19:00', '20:30', N'Nguyễn Văn Khách', '0901234567', 'PENDING', 225000, 'BANK_TRANSFER', ''),
(3, GETDATE(), '08:00', '10:00', N'Lê Minh Hoàng', '0912333444', 'CANCELLED', 200000, 'MOMO', N'Khách báo bận đột xuất'),
(1, GETDATE(), '20:00', '22:00', N'Trần Thu Hà', '0988111222', 'CONFIRMED', 300000, 'VNPAY', ''),
(4, GETDATE(), '15:00', '17:00', N'Đội Cầu Lông FPT', '0944999888', 'CONFIRMED', 200000, 'BANK_TRANSFER', N'Thuê cố định hàng tuần'),
(5, DATEADD(day, -1, GETDATE()), '18:00', '20:00', N'Phạm Quang Dũng', '0977555666', 'CONFIRMED', 200000, 'MOMO', ''),
(2, DATEADD(day, -2, GETDATE()), '09:00', '11:00', N'Nguyễn Văn Khách', '0901234567', 'CONFIRMED', 300000, 'BANK_TRANSFER', ''),
(3, DATEADD(day, -3, GETDATE()), '14:00', '16:00', N'Khách Vãng Lai', '0900000000', 'CANCELLED', 200000, 'CASH', N'Khách không đến'),
(1, DATEADD(day, 1, GETDATE()), '19:00', '21:00', N'Trần Thu Hà', '0988111222', 'PENDING', 300000, 'VNPAY', N'Chờ khách chuyển khoản'),
(4, DATEADD(day, 2, GETDATE()), '18:00', '20:00', N'Đội Cầu Lông FPT', '0944999888', 'PENDING', 200000, 'BANK_TRANSFER', ''),
(2, DATEADD(day, 3, GETDATE()), '20:00', '22:00', N'Lê Minh Hoàng', '0912333444', 'PENDING', 300000, 'MOMO', '');
END;
GO

IF COL_LENGTH('dbo.users','email') IS NULL ALTER TABLE dbo.users ADD email NVARCHAR(150) NOT NULL DEFAULT '';
IF COL_LENGTH('dbo.users','address') IS NULL ALTER TABLE dbo.users ADD address NVARCHAR(250) NOT NULL DEFAULT '';
IF COL_LENGTH('dbo.users','avatar_style') IS NULL ALTER TABLE dbo.users ADD avatar_style VARCHAR(24) NOT NULL DEFAULT 'forest';
IF COL_LENGTH('dbo.users','avatar_image') IS NULL ALTER TABLE dbo.users ADD avatar_image NVARCHAR(500) NOT NULL DEFAULT '';
IF OBJECT_ID('dbo.app_settings','U') IS NULL
    CREATE TABLE dbo.app_settings(setting_key VARCHAR(60) PRIMARY KEY, setting_value NVARCHAR(500) NOT NULL);
GO
SELECT DB_NAME() AS database_name, (SELECT COUNT(*) FROM dbo.users) AS users_count,
       (SELECT COUNT(*) FROM dbo.courts) AS courts_count,
       (SELECT COUNT(*) FROM dbo.bookings) AS bookings_count;
GO
