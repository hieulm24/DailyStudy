# English Tracker - Ứng Dụng Tự Học & Theo Dõi Tiến Độ Tiếng Anh Cá Nhân (Chạy LOCAL)

Ứng dụng web toàn diện chạy **hoàn toàn cục bộ (LOCAL)** giúp ghi chép, theo dõi và nâng cao 4 kỹ năng tiếng Anh: **Từ vựng (Vocabulary)**, **Ngữ pháp (Grammar)**, **Luyện nghe (Listening)**, **Luyện nói (Speaking)** kết hợp cùng thuật toán ôn tập **Spaced Repetition** và hệ thống **5 Mini-Games** thông minh.

---

## 1. YÊU CẦU MÔI TRƯỜNG

* **Java**: JDK 17 hoặc 21
* **Build Tool**: Apache Maven 3.8+
* **Node.js**: v18+ (khuyến nghị v20+) & npm
* **Cơ sở dữ liệu**: Microsoft SQL Server (LOCAL)
  * Tên Database đã có sẵn: `EnglishLearning`
  * Cổng kết nối mặc định: `1433`

---

## 2. CÔNG NGHỆ SỬ DỤNG (TECH STACK)

### Backend
* **Spring Boot 3.3.x** / **Java 17+**
* **Spring Web**, **Spring Data JPA**, **Spring Security**
* **JWT Authentication** (jjwt `0.12.6`)
* **Microsoft SQL Server JDBC Driver**
* **BCrypt Password Encoder**
* **Lombok**, **Hibernate Validator**, **Jackson**
* Cấu hình JPA: `spring.jpa.hibernate.ddl-auto=validate` (mapping chính xác 100% vào 22 bảng database thực tế).

### Frontend
* **Vue 3** (Composition API `<script setup>`)
* **Vite** & **TypeScript**
* **Tailwind CSS** (với design tokens chuyên nghiệp, `border-radius` tối đa 7px)
* **Lucide Icons** (`lucide-vue-next`) đồng bộ toàn diện
* **Pinia** (State Management)
* **Vue Router** (Route Guards & JWT protection)
* **Axios** & **Chart.js** (`vue-chartjs`)

---

## 3. THÔNG TIN ĐĂNG NHẬP MẶC ĐỊNH

Hệ thống tự động khởi tạo (seed) tài khoản quản trị khi khởi chạy lần đầu:

* **Email**: `hieulm24@gmail.com`
* **Mật khẩu**: `L@nhminhhieudeptrai.1`
* *(Password được mã hóa một chiều bằng BCrypt trong database)*

---

## 4. BIẾN MÔI TRƯỜNG (.env.example)

Tạo file `.env` hoặc cấu hình qua `application.yml` khi cần thay đổi kết nối database:

```properties
# Kết nối SQL Server
DB_HOST=localhost
DB_PORT=1433
DB_NAME=EnglishLearning
DB_USERNAME=sa
DB_PASSWORD=fsh@12345

# Bảo mật JWT
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

# Cổng dịch vụ
BACKEND_PORT=8080
FRONTEND_PORT=5173
```

---

## 5. HƯỚNG DẪN KHỞI CHẠY DỰ ÁN

### Bước 1: Khởi động Backend (Spring Boot)

Mở terminal tại thư mục `backend/`:

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

Backend sẽ khởi chạy tại: `http://localhost:8080`
*(Tự động seed tài khoản mặc định `hieulm24@gmail.com` và nạp dữ liệu mẫu ban đầu nếu database còn trống).*

### Bước 2: Khởi động Frontend (Vue 3 + Vite)

Mở một terminal khác tại thư mục `frontend/`:

```bash
cd frontend
npm install
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:5173`

Truy cập trình duyệt tại `http://localhost:5173` và đăng nhập với tài khoản trên.

---

## 6. DANH SÁCH CHỨC NĂNG CHÍNH

1. **Dashboard Tổng Quan**:
   * Thống kê hôm nay: Số từ vựng, ngữ pháp, bài nghe, bài nói, tổng cộng và số mục cần ôn.
   * Cảnh báo thông minh: Nhắc nhở danh sách nội dung đến hạn ôn tập hôm nay.
   * Thanh tiến độ học theo mục tiêu ngày.
   * Biểu đồ học tập tương tác (7 ngày, 30 ngày, tháng này).
   * Timeline lịch sử hoạt động trong ngày.
2. **Quản Lý Từ Vựng (Vocabulary)**:
   * CRUD đầy đủ: Word, Meaning, Pronunciation, Part of speech, Level, Example, Note.
   * Bộ lọc đa chiều: Theo khoảng ngày (Hôm nay, Hôm qua, 7 ngày, 30 ngày, Tùy chọn), Cấp độ (A1-C2), Loại từ, Trạng thái.
   * Đánh dấu thuộc lòng (Mastered) hoặc xem chi tiết.
3. **Quản Lý Ngữ Pháp (Grammar)**:
   * Form linh hoạt: Topic, Level, Cấu trúc tổng quát, Khẳng định (+), Phủ định (-), Nghi vấn (?), Cách dùng, Dấu hiệu nhận biết, Lỗi thường gặp, Câu ví dụ.
   * Bộ lọc theo ngày, cấp độ và tìm kiếm nội dung.
4. **Nhật Ký Luyện Nghe (Listening)**:
   * Ghi lại bài nghe: Tiêu đề, link audio/podcast, cấp độ, thời lượng, ghi chú tóm tắt.
   * Mở liên kết trực tiếp bài nghe.
5. **Nhật Ký Luyện Nói (Speaking)**:
   * Ghi lại chủ đề luyện nói: Topic, thời lượng, link bản thu âm, nhận xét phát âm.
6. **Hệ Thống Ôn Tập Spaced Repetition (`/review`)**:
   * Thuật toán ngắt quãng: Đánh giá theo 4 mức độ **Forgot (Quên)**, **Hard (Khó)**, **Good (Nhớ)**, **Easy (Rất dễ)** để tự động điều chỉnh thời điểm ôn tập kế tiếp.
   * Tự động đồng bộ tiến độ vào database (`review_items`, `review_histories`, `study_streaks`, `daily_learning_statistics`).
7. **5 Mini-Games Ôn Luyện (`/games`)**:
   * **Flashcard**: Lật thẻ ôn từ vựng & đánh giá ghi nhớ.
   * **Trắc nghiệm 4 lựa chọn**: Chọn nghĩa tiếng Việt đúng cho từ vựng.
   * **Gõ từ theo nghĩa**: Luyện chính tả và phản xạ từ vựng.
   * **Điền từ vào câu**: Chọn từ phù hợp điền vào câu ví dụ thực tế.
   * **Trắc nghiệm ngữ pháp**: Luyện tập cấu trúc ngữ pháp.
   * Tính điểm, tỷ lệ chính xác (%), lưu lịch sử phiên chơi.
8. **Báo Cáo & Thống Kê (`/statistics`)**:
   * Heatmap chuyên cần (90 ngày) trực quan.
   * Chuỗi ngày học liên tục (Current Streak & Best Streak).
   * Biểu đồ so sánh tăng trưởng giữa các kỹ năng.
9. **Cài Đặt & Sao Lưu / Khôi Phục (`/settings`)**:
   * Cài đặt mục tiêu học tập hàng ngày.
   * Xuất toàn bộ dữ liệu ra tệp JSON (`english-learning-backup.json`).
   * Khôi phục dữ liệu từ tệp JSON an toàn.

---

## 7. ĐÓNG GÓI SẢN PHẨM (BUILD PRODUCTION)

* **Backend Jar**:
  ```bash
  cd backend
  mvn clean package -DskipTests
  # File thực thi: backend/target/english-learning-backend-1.0.0.jar
  java -jar target/english-learning-backend-1.0.0.jar
  ```
* **Frontend Bundle**:
  ```bash
  cd frontend
  npm run build
  # Thư mục tĩnh: frontend/dist/
  ```

---

## 8. XỬ LÝ SỰ CỐ (TROUBLESHOOTING)

* **Lỗi không kết nối được SQL Server**:
  * Đảm bảo dịch vụ `SQL Server (MSSQLSERVER)` hoặc `SQL Server Browser` đang chạy.
  * Kiểm tra xem TCP/IP đã được Enable trong *SQL Server Configuration Manager*.
  * Kiểm tra mật khẩu `sa` trong `application.yml` có đúng là `fsh@12345` không.
* **Lỗi CORS khi gọi API**:
  * Backend đã mở CORS cho `http://localhost:5173`. Nếu chạy frontend ở cổng khác, cập nhật trong `WebMvcConfig.java`.
* **Dữ liệu database**:
  * Ứng dụng mapping trực tiếp vào database `EnglishLearning` đã có sẵn, không tự động sửa đổi hoặc xóa bảng của bạn.
