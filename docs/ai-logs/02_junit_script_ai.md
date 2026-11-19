# Nhật ký AI — Unit Test Controller với JUnit 5 (Tuần 2)

## Mục tiêu
Viết Unit Test cho toàn bộ Controller thật của AU Badminton (`AuthController`, `CourtsController`,
`BookingsController`, `HealthController`, `SettingsController`), áp dụng `@ParameterizedTest`/`@CsvSource`
cho kiểm tra giá trị biên, `assertThrows` cho kiểm tra ngoại lệ, và `@Timeout` cho các luồng không được treo.

## Prompt sử dụng
> Đọc mã nguồn thật trong `src/main/java/com/aubadminton/controller/`, đối chiếu với
> `docs/TEST_CASES.md` (Tuần 1), rồi viết Unit Test JUnit 5 cho các Controller bằng Mockito để giả lập
> `JdbcTemplate` (không cần SQL Server thật). Áp dụng Parameterized Test cho validate biên, Exception
> Testing bằng `assertThrows` cho các đường dẫn không được try-catch bắt, và `@Timeout` để đảm bảo không
> bị treo luồng. Chỉ coi là xong khi `mvn test` báo BUILD SUCCESS thật.

## Kết quả hỗ trợ
- AI đọc trực tiếp 5 class Controller (không có tầng Service riêng, Controller gọi thẳng `JdbcTemplate`),
  nên chiến lược test là mock `JdbcTemplate` bằng Mockito và dựng `HttpServletRequest`/`HttpSession` bằng
  `org.springframework.mock.web.MockHttpServletRequest/MockHttpSession` (có sẵn trong `spring-boot-starter-test`).
- `CourtsController` dùng field injection (`@Autowired private JdbcTemplate`), nên phải gán mock bằng
  `ReflectionTestUtils.setField(...)` thay vì qua constructor.
- Đã tạo 5 file test trong `src/test/java/com/aubadminton/controller/`:
  `AuthControllerTest`, `CourtsControllerTest`, `BookingsControllerTest`, `HealthControllerTest`,
  `SettingsControllerTest` — tổng cộng 45 test case, ánh xạ trực tiếp tới các ca trong `docs/TEST_CASES.md`
  (W1-AUTH-01..06, W1-COURT-01..03, W1-BOOK-01..06, W1-ADMIN-01..03, W1-ENV-03).
- Áp dụng `@ParameterizedTest` + `@CsvSource` cho: validate đăng ký (fullname/username/password/phone biên),
  validate giá sân khi thêm sân, validate trạng thái đèn IoT, validate thời lượng đặt sân.
- Áp dụng `@Timeout(2, SECONDS)` trên các test luồng chính (login, tạo booking, health check, lấy danh sách sân).

## Phát hiện quan trọng (AI phân tích, sinh viên xác nhận lại bằng cách chạy test thật)
Trong `BookingsController.createBooking()`, đoạn:
```java
Map<String, String> options = settings.values();
LocalTime opening = LocalTime.parse(options.get("open_time"));
LocalTime closing = LocalTime.parse(options.get("close_time"));
```
nằm **ngoài** khối `try { ... } catch (Exception e) { ... 500 có JSON }` phía dưới. Nếu dữ liệu
`open_time`/`close_time` trong bảng `app_settings` bị sai định dạng giờ (ví dụ do admin nhập nhầm qua
`SettingsController.update()` nếu validate đó bị bỏ qua, hoặc do sửa tay trong DB), request đặt sân sẽ
văng `DateTimeParseException` không được bắt, trả về trang lỗi Whitelabel mặc định của Spring Boot (500)
thay vì JSON lỗi nhất quán như các nhánh khác. Được viết thành test
`testCreateBookingThrowsWhenOpeningHoursAreMisconfigured` bằng `assertThrows(DateTimeParseException.class, ...)`
trong `BookingsControllerTest.java` để ghi nhận hành vi hiện tại; đây là một lỗ hổng xử lý lỗi thật,
chưa được sửa trong phạm vi Tuần 2 (chỉ viết test, không refactor code nghiệp vụ) — đề xuất đưa đoạn
parse giờ vào trong khối try-catch chung ở Tuần 6 (AI Code Review).

## Kết quả chạy thực tế
```
mvn test -Dtest=*ControllerTest
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
Chạy ngày 19/11/2026 trên máy local (JDK 17, Maven 3.10.0), không cần SQL Server đang chạy vì toàn bộ
`JdbcTemplate` đã được mock.

## Ghi chú
Không mock nghiệp vụ giả định ngoài những gì đọc được trong code thật; mọi quy tắc biên (độ dài username,
khoảng giá sân, khoảng thời lượng đặt sân...) được lấy trực tiếp từ điều kiện `if` trong Controller.
