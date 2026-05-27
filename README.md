# Quiz Web - BackEnd

Đây là mã nguồn phía máy chủ (BackEnd) cho ứng dụng trắc nghiệm (Quiz Web). Dự án được xây dựng bằng Java và Spring Boot, cung cấp các API để quản lý người dùng và bài thi trắc nghiệm.

## Yêu cầu hệ thống
- Java 17
- Maven
- MySQL (có thể dùng phần mềm XAMPP)

## Các công nghệ sử dụng
- **Spring Boot 3**: Framework chính để chạy ứng dụng.
- **Spring Security & JJWT**: Xử lý xác thực người dùng và phân quyền thông qua JSON Web Token (JWT).
- **Spring Data JPA**: Kết nối và thao tác với cơ sở dữ liệu MySQL (tự động tạo bảng).
- **Google API Client**: Hỗ trợ xác thực đăng nhập qua Google (Firebase).

## Hướng dẫn cài đặt và chạy dự án

1. **Cấu hình Database**:
   - Khởi động MySQL (qua XAMPP hoặc MySQL Workbench).
   - Mở file `src/main/resources/application.yml` và kiểm tra lại thông tin `username`, `password`, cũng như tên database (`url`) cho khớp với cấu hình máy của bạn.

2. **Khởi động ứng dụng**:
   - Mở terminal tại thư mục `BackEnd`.
   - Chạy lệnh sau để tải thư viện và khởi động máy chủ:
     ```bash
     mvn spring-boot:run
     ```
   - Nếu không có lỗi, server sẽ chạy ở địa chỉ mặc định là `http://localhost:8080`.

## Cấu trúc thư mục chính
- `auth/`: Chức năng đăng nhập, đăng ký, tạo JWT và đăng nhập Google.
- `user/`: Quản lý thông tin tài khoản, hồ sơ (Profile) của người dùng.
- `config/`: Các cấu hình chung như Security, phân quyền các đường dẫn API.
