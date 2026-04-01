# LuminaBook — Ứng dụng bán sách (book-selling)

Một ứng dụng web bán sách mẫu xây dựng bằng Spring Boot, Thymeleaf và Spring Security. Dự án minh họa các tính năng cơ bản của một cửa hàng sách online: quản lý sách, tìm kiếm, đăng ký/đăng nhập, xác thực email, giỏ hàng và giao diện quản trị cơ bản.

---

## Tính năng chính

- **Đăng ký / Đăng nhập**: Người dùng có thể đăng ký, đăng nhập. Có gửi email xác thực sau khi đăng ký.
- **Danh mục & Sách**: Danh sách sách, trang chi tiết sách, tìm kiếm.
- **Giỏ hàng**: Trang giỏ hàng (cart page).
- **Quản trị**: Giao diện quản trị để quản lý sách/tác giả/thể loại (nếu bật các route admin).
- **Gửi email**: Hệ thống hỗ trợ gửi email (dùng Spring Mail) cho xác thực email.

---

## Công nghệ

- **Ngôn ngữ**: Java 21
- **Framework**: Spring Boot 4.x (pom.xml dùng 4.0.3)
- **View**: Thymeleaf (+ thymeleaf-extras-springsecurity6)
- **Bảo mật**: Spring Security
- **Persistence**: Spring Data JPA (H2 / MySQL)
- **Email**: Spring Boot Starter Mail
- **Tiện ích**: Lombok, Bootstrap, jQuery

---

## Yêu cầu môi trường

- Java 21
- Maven (sử dụng `mvn` hoặc wrapper `mvnw` / `mvnw.cmd`)
- MySQL (tùy chọn) hoặc H2 (được cấu hình runtime)

---

## Cấu hình (ví dụ)

Update file cấu hình tại [src/main/resources/application.properties](src/main/resources/application.properties#L1-L40). Không commit thông tin nhạy cảm (mật khẩu, token).

Ví dụ cấu hình cơ bản (thay thế giá trị bằng thông tin của bạn):

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bookdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_DB_PASSWORD

# Mail (sử dụng SMTP, ví dụ Gmail App Password)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-mail-password
spring.mail.from=Your Store <noreply@example.com>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Quan trọng**: Không lưu mật khẩu/credentials vào VCS. Dùng biến môi trường hoặc secret manager khi deploy.

---

## Chạy ứng dụng (local)

Sử dụng Maven wrapper (Windows):

```powershell
.\mvnw.cmd spring-boot:run
```

Hoặc (Unix/macOS):

```bash
./mvnw spring-boot:run
```

Build jar và chạy:

```bash
./mvnw package
java -jar target/book-selling-0.0.1-SNAPSHOT.jar
```

Chạy test:

```bash
./mvnw test
```

---

## Một số route / endpoint hữu ích

- Home: `/`
- Danh sách sách: `/books`
- Đăng nhập: `/login`
- Đăng ký: `/register`
- Xác thực email: `/verify-email?token=...` (đã có trong `AuthController`)
- Giỏ hàng: `/cart`

Xem mã nguồn controller liên quan: [src/main/java/com/group/book_selling/controllers/AuthController.java](src/main/java/com/group/book_selling/controllers/AuthController.java#L1-L200)

---

## Cấu trúc dự án (tóm tắt)

- `src/main/java` — mã nguồn Java (controllers, services, models, repository)
- `src/main/resources/templates` — Thymeleaf templates (giao diện)
- `src/main/resources/static` — tài nguyên tĩnh (css, js, images)
- `src/test/java` — unit / integration tests

---

## Ghi chú phát triển / TODO

- Hoàn thiện luồng thanh toán (`/checkout`) nếu cần tích hợp cổng thanh toán.
- Bảo mật: cấu hình CORS, CSP và các header bảo mật khi deploy.

---

## Đóng góp

1. Fork repository
2. Tạo branch feature/bugfix
3. Tạo pull request với mô tả rõ ràng

---

## Liên hệ

Nếu cần hỗ trợ nhanh, mở issue trong repo hoặc liên hệ người phát triển chính (xem thông tin trong pom.xml).

---
