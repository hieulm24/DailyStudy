# 🌟 PersonalManagement Hub - Hệ Thống Quản Lý Cá Nhân Toàn Diện & Học Tập (All-In-One Life OS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x%20Composition%20API-42b883.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-v4-38bdf8.svg)](https://tailwindcss.com/)
[![Database](https://img.shields.io/badge/Database-Microsoft%20SQL%20Server-CC292B.svg)](https://www.microsoft.com/sql-server)
[![Security](https://img.shields.io/badge/Security-AES--256--GCM%20%7C%20JWT%20%7C%20BCrypt-orange.svg)](#7-kiến-trúc-bảo-mật--mã-hóa-cao-cấp)

> **PersonalManagement Hub** là nền tảng quản trị cuộc sống và phát triển bản thân toàn diện (**Life Operating System**) chạy hoàn toàn **cục bộ (Local-first & Self-hosted)**. Khởi nguồn từ một ứng dụng học ngoại ngữ, hệ thống hiện đã mở rộng thành một hệ sinh thái mạnh mẽ quản lý toàn diện 12 phân hệ: **Kế hoạch hàng ngày**, **Tài chính cá nhân**, **Dinh dưỡng & Thể chất**, **Tủ sách**, **Ghi chú IT & Kiến trúc phần mềm**, **Kho tài liệu**, **Két mật khẩu mã hóa quân đội AES-256**, và **Trung tâm học tiếng Anh chuyên sâu kết hợp Spaced Repetition & Mini-Games**.

---

## 📑 MỤC LỤC
1. [Điểm Nổi Bật Của Hệ Thống](#1-điểm-nổi-bật-của-hệ-thống)
2. [Chi Tiết 12 Phân Hệ Chức Năng](#2-chi-tiết-12-phân-hệ-chức-năng)
3. [Công Nghệ Sử Dụng (Tech Stack)](#3-công-nghệ-sử-dụng-tech-stack)
4. [Yêu Cầu Môi Trường](#4-yêu-cầu-môi-trường)
5. [Cấu Hình & Biến Môi Trường (.env / application.yml)](#5-cấu-hình--biến-môi-trường)
6. [Hướng Dẫn Khởi Chạy Dự Án](#6-hướng-dẫn-khởi-chạy-dự-án)
7. [Kiến Trúc Bảo Mật & Mã Hóa Cao Cấp](#7-kiến-trúc-bảo-mật--mã-hóa-cao-cấp)
8. [Cấu Trúc Thư Mục Dự Án](#8-cấu-trúc-thư-mục-dự-án)
9. [Đóng Gói Sản Phẩm (Build Production)](#9-đóng-gói-sản-phẩm-build-production)
10. [Xử Lý Sự Cố Thường Gặp (Troubleshooting)](#10-xử-lý-sự-cố-thường-gặp-troubleshooting)

---

## 1. ĐIỂM NỔI BẬT CỦA HỆ THỐNG

* 🛡️ **Bảo Mật Cực Cao (AES-256-GCM)**: Két mật khẩu và tài liệu nhạy cảm được mã hóa cấp quân sự trước khi ghi vào Database. Mật khẩu chính lưu trữ an toàn bằng BCrypt.
* ⚡ **Local-First & Quyền Riêng Tư Tuyệt Đối**: Toàn bộ dữ liệu của bạn thuộc về bạn. Hệ thống hoạt động hoàn toàn độc lập trên máy tính của bạn, không phụ thuộc vào đám mây bên thứ ba.
* 🎯 **Thiết Kế Đồng Bộ Chuẩn Enterprise**: Giao diện người dùng xây dựng bằng Vue 3 + Tailwind CSS hiện đại, sử dụng bảng token tinh gọn (`rounded-md`, `border border-slate-200`, `shadow-xs`), tối ưu trên mọi độ phân giải màn hình.
* 🧠 **Thuật Toán Khoa Học**: Ứng dụng thuật toán lặp lại ngắt quãng **SuperMemo SM-2** trong ôn tập và tính toán chuẩn chỉ nhu cầu năng lượng **BMR / TDEE / Macro** dinh dưỡng.
* 💾 **Sao Lưu & Phục Hồi Toàn Diện**: Xuất/nhập toàn bộ dữ liệu chỉ với một cú click chuột thông qua file JSON tiêu chuẩn.

---

## 2. CHI TIẾT 12 PHÂN HỆ CHỨC NĂNG

### 1. 📅 Kế Hoạch & Việc Cần Làm Hàng Ngày (`/tasks`)
* **Quản lý nhiệm vụ (Daily Tasks)**: Thêm mới, chỉnh sửa, phân loại theo độ ưu tiên (*Thấp, Trung bình, Cao, Khẩn cấp*).
* **Trạng thái & Tiến độ**: Hoàn thành, Đang làm, Tạm hoãn. Đếm ngược hạn chót (Deadline) và gắn thẻ tag phân loại.
* **Bộ lọc thông minh**: Lọc việc theo ngày hôm nay, tuần này, theo độ ưu tiên hoặc trạng thái.

### 2. 💰 Quản Lý Tài Chính Cá Nhân (`/finance`)
* **Đa ví & Tài khoản**: Quản lý tiền mặt, thẻ ngân hàng, ví điện tử (MoMo, ZaloPay), tài khoản tiết kiệm và đầu tư.
* **Giao dịch Thu / Chi**: Ghi chép chi tiêu chi tiết theo danh mục (Ăn uống, Hóa đơn, Mua sắm, Lương, Thưởng...), tự động cập nhật số dư các ví.
* **Ngân sách theo tháng**: Thiết lập hạn mức chi tiêu cho từng nhóm danh mục, cảnh báo khi chi tiêu vượt ngưỡng.
* **Mục tiêu tích lũy (Savings Goals)**: Theo dõi tiến độ tiết kiệm cho các mục tiêu lớn (Mua xe, Quỹ khẩn cấp, Du lịch).
* **Báo cáo tài chính**: Biểu đồ trực quan cơ cấu chi tiêu, dòng tiền vào/ra và biến động tài sản ròng.

### 3. 🥗 Sức Khỏe, Dinh Dưỡng & Thể Chất (`/nutrition`)
* **Nhật ký thực phẩm**: Ghi lại bữa sáng, trưa, tối, bữa phụ với số lượng và khối lượng (gram).
* **Tự động tính Calo & Macro**: Tính toán chính xác Calo, Chất đạm (Protein), Tinh bột (Carbs), Chất béo (Fat).
* **Tra cứu thực phẩm**: Tích hợp cơ sở dữ liệu dinh dưỡng thông minh (hỗ trợ USDA Food Database).
* **Nhật ký thể chất & Tiêu hao Calo**: Ghi nhận hoạt động tập luyện (Gym, Chạy bộ, Bơi lội...) và số calo đốt cháy.
* **Thâm hụt Calo & Chỉ số cơ thể**: Tự động tính toán Calo nạp vào - Calo tiêu hao = Calo thực nhận so với mục tiêu TDEE. Nhật ký cân nặng và ảnh theo dõi vóc dáng.

### 4. 📚 Tủ Sách & Nhật Ký Đọc (`/reading`)
* **Quản lý kho sách**: Theo dõi sách đang đọc, đã đọc xong, hoặc dự định đọc.
* **Tiến độ đọc**: Cập nhật số trang đã đọc, phần trăm hoàn thành, số phút đọc mỗi ngày.
* **Trích dẫn & Ghi chú đắc ý**: Lưu lại những câu trích dẫn tâm đắc, bài học rút ra và cảm nhận cá nhân cho từng cuốn sách.

### 5. 💻 Sổ Tay IT & Kiến Trúc Phần Mềm (`/it/notes`)
* **Ghi chú kỹ thuật (Architecture Notebook)**: Ghi lại kiến trúc hệ thống, design patterns, giải pháp kỹ thuật, cơ sở dữ liệu.
* **Kho Snippets Code**: Lưu trữ các đoạn code mẫu, script hữu ích (Java, SQL, TypeScript, Bash, Docker...).
* **Hỗ trợ Markdown & Tag**: Trình hiển thị Markdown chuyên nghiệp với highlight cú pháp chuẩn.

### 6. 📁 Kho Tài Liệu & Liên Kết Học Tập (`/documents`)
* **Quản lý file đính kèm**: Upload và lưu trữ an toàn các loại tài liệu học tập (PDF, Word, Excel, Slide, ZIP...).
* **Kho liên kết hữu ích**: Phân loại và bookmark các website, bài viết công nghệ, podcast chất lượng.
* **Phân mục thư mục**: Quản lý gọn gàng theo thư mục và tìm kiếm tức thì.

### 7. 🔑 Két Mật Khẩu & Bảo Mật Tài Khoản (`/vault`)
* **Mã hóa cao cấp AES-256-GCM**: Mật khẩu và thông tin đăng nhập được mã hóa an toàn ở tầng ứng dụng trước khi lưu xuống SQL Server.
* **Lưu trữ toàn diện**:
  * Tên tài khoản, Email, Số điện thoại liên kết, Mật khẩu.
  * Mã bảo mật 2FA (Secret Seed), Mã dự phòng (Backup Codes).
  * Câu hỏi & Câu trả lời bảo mật.
  * Ghi chú bí mật.
* **Đính kèm tệp tin bảo mật**: Hỗ trợ đính kèm mọi loại tệp nhạy cảm (Ảnh CCCD, file Key/PEM, chứng chỉ số, hóa đơn bản quyền...).
* **Đánh giá độ mạnh mật khẩu**: Phân tích độ an toàn mật khẩu (Yếu, Trung bình, Mạnh) bằng thuật toán đo entropy.
* **Thùng rác an toàn**: Xóa tạm thời vào thùng rác, có thể khôi phục hoặc xóa vĩnh viễn.

### 8. 🎓 Trung Tâm Học Tiếng Anh Chuyên Sâu (English Mastery Hub)
* **Từ vựng (Vocabulary - `/vocabulary`)**:
  * Quản lý từ vựng đầy đủ: Nghĩa tiếng Việt, Phiên âm IPA, Từ loại, Cấp độ chuẩn CEFR (A1 - C2), Câu ví dụ thực tế.
  * Đánh dấu trạng thái thuộc lòng (Mastered) và lọc theo ngày/loại từ.
* **Ngữ pháp (Grammar - `/grammar`)**:
  * Lưu trữ cấu trúc ngữ pháp có hệ thống: Khẳng định (+), Phủ định (-), Nghi vấn (?), Cách dùng, Dấu hiệu nhận biết, Lỗi thường gặp và câu ví dụ minh họa.
* **Luyện nghe (Listening - `/listening`)**:
  * Ghi chép bài nghe podcast/audio, thời lượng, chủ đề và tóm tắt ý chính.
* **Luyện nói (Speaking - `/speaking`)**:
  * Nhật ký luyện nói, thời lượng thực hành, đính kèm link bản thu âm và tự nhận xét phát âm.

### 9. 🧠 Không Gian Ôn Tập Spaced Repetition & 5 Mini-Games (`/review` & `/games`)
* **Ôn theo chủ đề (Spaced Repetition)**:
  * Thuật toán ngắt quãng **SuperMemo SM-2**: Tự động tính toán chu kỳ ôn tập kế tiếp dựa trên đánh giá của bạn qua 4 mức độ: **Quên (Forgot)**, **Khó (Hard)**, **Nhớ (Good)**, **Rất dễ (Easy)**.
  * Badge thông minh trên thanh menu tự động cảnh báo số lượng thẻ đến hạn ôn hôm nay.
* **5 Mini-Games Ôn Luyện Hấp Dẫn**:
  1. 🎴 **Flashcard**: Lật thẻ 2 mặt ôn từ vựng & đánh giá trí nhớ.
  2. ❓ **Trắc nghiệm 4 lựa chọn**: Chọn nhanh nghĩa đúng trong thời gian quy định.
  3. ⌨️ **Gõ từ theo nghĩa**: Rèn luyện chính tả và phản xạ gõ từ vựng.
  4. 🧩 **Điền từ vào câu**: Lựa chọn từ vựng chính xác điền vào câu ngữ cảnh.
  5. 📝 **Trắc nghiệm ngữ pháp**: Luyện tập phản xạ cấu trúc câu.

### 10. 📊 Báo Cáo & Thống Kê Hoạt Động (`/statistics`)
* **Heatmap chuyên cần (GitHub style)**: Hiển thị mật độ học tập và rèn luyện trong suốt 90 ngày.
* **Chuỗi ngày học liên tục (Streak)**: Theo dõi chuỗi ngày kỷ lục (Best Streak) và chuỗi hiện tại (Current Streak).
* **Biểu đồ so sánh**: Đo lường sự tăng trưởng giữa các kỹ năng và danh mục theo tuần/tháng.

### 11. 👤 Hồ Sơ Cá Nhân & Khôi Phục Mật Khẩu OTP (`/profile`)
* **Hồ sơ học tập**: Đổi ảnh đại diện (chọn avatar mẫu hoặc upload ảnh cá nhân), cập nhật mục tiêu TOEIC (450 - 990), mục tiêu thời gian học mỗi ngày và Bio.
* **Đổi mật khẩu tài khoản**: Kiểm tra mật khẩu cũ và xác thực mật khẩu mới.
* **Khôi phục mật khẩu qua Email OTP**: Gửi mã xác thực 6 số về email đăng ký để đặt lại mật khẩu an toàn.

### 12. ⚙️ Cài Đặt Hệ Thống & Sao Lưu / Phục Hồi Dữ Liệu (`/settings`)
* **Sao lưu dữ liệu (Backup)**: Kết xuất (Export) toàn bộ dữ liệu sang tệp JSON tiêu chuẩn (`personal-management-backup.json`).
* **Phục hồi dữ liệu (Restore)**: Khôi phục toàn bộ trạng thái hệ thống từ file JSON an toàn.

---

## 3. CÔNG NGHỆ SỬ DỤNG (TECH STACK)

### Backend
| Công nghệ | Phiên bản / Chi tiết | Mục đích sử dụng |
| :--- | :--- | :--- |
| **Java** | 17+ (Tương thích JDK 21) | Ngôn ngữ lập trình chính |
| **Spring Boot** | 3.3.x | Framework backend nòng cốt |
| **Spring Data JPA / Hibernate** | 3.3.x | ORM quản trị hơn 45+ thực thể cơ sở dữ liệu |
| **Spring Security** | 6.x | Phân quyền, bảo vệ tài nguyên và API Endpoints |
| **JWT (JJWT)** | 0.12.6 | Cơ chế Stateless Token Authentication |
| **Microsoft SQL Server JDBC** | Latest | Driver kết nối cơ sở dữ liệu SQL Server |
| **HikariCP** | Tích hợp sẵn | Connection Pool hiệu năng cao |
| **Bcrypt & Java Cryptography** | `AES/GCM/NoPadding` | Mã hóa mật khẩu một chiều & mã hóa 2 chiều Két dữ liệu |
| **Lombok, Jackson** | Latest | Giảm thiểu boilerplate code và xử lý JSON |

### Frontend
| Công nghệ | Phiên bản / Chi tiết | Mục đích sử dụng |
| :--- | :--- | :--- |
| **Vue 3** | 3.5+ (Composition API) | Framework giao diện phản ứng nhanh (Reactive) |
| **Vite** | 5.4+ | Công cụ Build & Hot Module Replacement siêu tốc |
| **TypeScript** | 5.x | Kiểm soát kiểu dữ liệu tĩnh nghiêm ngặt |
| **Tailwind CSS** | v4 | Hệ thống styling linh hoạt với các token thiết kế chuẩn |
| **Pinia** | 2.x | Quản lý Global State (Auth, Toast, Review, Settings...) |
| **Vue Router** | 4.x | Điều hướng trang và Route Guard bảo vệ JWT |
| **Lucide Icons** | `lucide-vue-next` | Thư viện hơn 500+ biểu tượng vector hiện đại |
| **Chart.js & Vue-Chartjs** | 5.x | Trực quan hóa số liệu qua biểu đồ tài chính, dinh dưỡng, học tập |
| **Axios** | 1.x | HTTP Client với Interceptor tự động gắn Bearer Token |

---

## 4. YÊU CẦU MÔI TRƯỜNG

Trước khi cài đặt, hãy đảm bảo máy tính của bạn đã cài đặt các phần mềm sau:

1. **Java Development Kit (JDK)**: Phiên bản **17** hoặc **21**.
2. **Apache Maven**: Phiên bản **3.8+** (hoặc dùng `mvnw` đi kèm).
3. **Node.js**: Phiên bản **v18+** (khuyến nghị **v20 LTS**) & npm.
4. **Cơ sở dữ liệu**: **Microsoft SQL Server** (bản Developer hoặc Express).
   * Cổng mặc định: `1433`.
   * Tên database: `EnglishLearning` (hoặc tên tùy chọn của bạn).
   * Bật hỗ trợ xác thực **SQL Server and Windows Authentication mode**.
   * Đảm bảo giao thức **TCP/IP** được bật (Enable) trong *SQL Server Configuration Manager*.

---

## 5. CẤU HÌNH & BIẾN MÔI TRƯỜNG

Hệ thống có thể chạy ngay với cấu hình mặc định trong `backend/src/main/resources/application.yml` hoặc tùy chỉnh qua biến môi trường (Environment Variables):

```properties
# ==============================================================================
# CẤU HÌNH DATABASE SQL SERVER
# ==============================================================================
DB_HOST=localhost
DB_PORT=1433
DB_NAME=EnglishLearning
DB_USERNAME=sa
DB_PASSWORD=fsh@12345

# ==============================================================================
# BẢO MẬT JWT & TÀI KHOẢN MẶC ĐỊNH
# ==============================================================================
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

DEFAULT_USER_EMAIL=hieulm24@gmail.com
DEFAULT_USER_PASSWORD=L@nhminhhieudeptrai.1
DEFAULT_USER_NAME=Lãnh Minh Hiếu

# ==============================================================================
# CỔNG DỊCH VỤ LOCAL
# ==============================================================================
PORT=8080
FRONTEND_PORT=5173
```

---

## 6. HƯỚNG DẪN KHỞI CHẠY DỰ ÁN

### Bước 1: Khởi động Backend (Spring Boot)
Mở một cửa sổ Terminal tại thư mục `backend/`:

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

* Backend sẽ khởi động tại: `http://localhost:8080`
* Khi chạy lần đầu, ứng dụng sẽ tự động khởi tạo tài khoản quản trị mặc định và sẵn sàng tiếp nhận kết nối.

### Bước 2: Khởi động Frontend (Vue 3 + Vite)
Mở một cửa sổ Terminal khác tại thư mục `frontend/`:

```bash
cd frontend
npm install
npm run dev
```

* Frontend sẽ khởi chạy tại: `http://localhost:5173`
* Mở trình duyệt và truy cập `http://localhost:5173`.

### Thông Tin Đăng Nhập Mặc Định:
* **Email**: `hieulm24@gmail.com`
* **Mật khẩu**: `L@nhminhhieudeptrai.1`

---

## 7. KIẾN TRÚC BẢO MẬT & MÃ HÓA CAO CẤP

Hệ thống tuân thủ các tiêu chuẩn bảo mật nghiêm ngặt nhằm bảo vệ tối đa dữ liệu riêng tư:

```
[Người dùng nhập mật khẩu/tài khoản]
                │
                ▼
[AES-256-GCM Encryption Layer] (Tự động sinh IV 12 bytes ngẫu nhiên cho mỗi bản ghi)
                │
                ▼ (Mã hóa chuỗi CipherText kèm GCM Auth Tag)
[Cơ sở dữ liệu Microsoft SQL Server] (Dữ liệu lưu dạng mã hóa, an toàn tuyệt đối ngay cả khi lộ DB)
```

1. **Mã Hóa 2 Chiều AES-256-GCM (`VaultEncryptionService`)**:
   * Áp dụng cho toàn bộ thông tin đăng nhập, mật khẩu tài khoản trong Két mật khẩu.
   * GCM mode đảm bảo cả tính **bí mật (Confidentiality)** lẫn tính **toàn vẹn (Integrity)**; ngăn chặn hoàn toàn việc can thiệp sửa đổi trái phép dữ liệu mã hóa.
2. **Mã Hóa 1 Chiều Mật Khẩu Chính (BCrypt)**:
   * Mật khẩu đăng nhập vào ứng dụng được băm với salt ngẫu nhiên bằng thuật toán BCrypt cường độ cao, không thể dịch ngược.
3. **Stateless JWT Authentication**:
   * Token JWT ký số HMAC-SHA256, truyền qua header `Authorization: Bearer <token>`, tự động hết hạn sau 24h.
4. **Phòng Chống SQL Injection**:
   * Toàn bộ truy vấn thông qua Spring Data JPA và Hibernate Criteria / Parameterized Queries, loại bỏ hoàn toàn nguy cơ SQL Injection.

---

## 8. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
PersonalManagement/
├── backend/                               # Source code Backend (Spring Boot 3)
│   ├── src/main/java/com/englishlearning/
│   │   ├── config/                        # Cấu hình Security, JPA Naming, CORS, Swagger
│   │   ├── controller/                    # 31 REST Controllers xử lý API
│   │   ├── dto/                           # Data Transfer Objects (Request/Response)
│   │   ├── entity/                        # Hơn 45+ JPA Entities (Financial, Nutrition, Vault, Learning...)
│   │   ├── repository/                    # Spring Data JPA Repositories
│   │   ├── security/                      # JWT Filter, UserDetails, Authentication Provider
│   │   ├── service/                       # Business Logic Services & AES Encryption
│   │   └── util/                          # Các hàm tiện ích, thuật toán Spaced Repetition
│   ├── src/main/resources/
│   │   └── application.yml                # Cấu hình chính của Spring Boot
│   └── pom.xml                            # Quản lý thư viện Maven
│
├── frontend/                              # Source code Frontend (Vue 3 + Vite)
│   ├── src/
│   │   ├── components/                    # Reusable UI Components (Modal, Button, Input, Sidebar...)
│   │   ├── router/                        # Vue Router & Navigation Guards
│   │   ├── services/                      # Axios API Service Modules
│   │   ├── stores/                        # Pinia Global State Stores
│   │   ├── types/                         # TypeScript Type Definitions
│   │   ├── utils/                         # Formatter tiền tệ, tính toán dinh dưỡng, ngày tháng
│   │   ├── views/                         # 18 Thư mục màn hình chức năng chính:
│   │   │   ├── dashboard/                 # Tổng quan toàn hệ thống
│   │   │   ├── tasks/                     # Kế hoạch & Việc cần làm hàng ngày
│   │   │   ├── finance/                   # Quản lý tài chính cá nhân
│   │   │   ├── nutrition/                 # Quản lý dinh dưỡng & calo thể chất
│   │   │   ├── reading/                   # Tủ sách & Nhật ký đọc
│   │   │   ├── it/                        # Sổ tay kiến trúc & Snippets IT
│   │   │   ├── documents/                 # Kho tài liệu & Liên kết học tập
│   │   │   ├── vault/                     # Két mật khẩu bảo mật AES-256
│   │   │   ├── vocabulary/                # Từ vựng tiếng Anh
│   │   │   ├── grammar/                   # Ngữ pháp tiếng Anh
│   │   │   ├── listening/                 # Luyện nghe podcast
│   │   │   ├── speaking/                  # Luyện nói & ghi âm
│   │   │   ├── review/                    # Ôn tập theo chủ đề (Spaced Repetition)
│   │   │   ├── games/                     # 5 Mini-Games luyện tập
│   │   │   ├── statistics/                # Báo cáo thống kê & Heatmap
│   │   │   ├── profile/                   # Thông tin cá nhân & Quên mật khẩu OTP
│   │   │   └── settings/                  # Cài đặt & Backup/Restore
│   │   ├── App.vue                        # Component gốc
│   │   └── main.ts                        # Điểm khởi đầu ứng dụng Vue
│   ├── package.json                       # Danh sách dependencies npm
│   └── vite.config.ts                     # Cấu hình Vite & Tailwind
│
└── README.md                              # Tài liệu hướng dẫn toàn diện của dự án
```

---

## 9. ĐÓNG GÓI SẢN PHẨM (BUILD PRODUCTION)

### 1. Build Backend sang file JAR độc lập:
```bash
cd backend
mvn clean package -DskipTests
```
* File JAR sinh ra tại: `backend/target/english-learning-backend-1.0.0.jar`
* Khởi chạy độc lập trên máy chủ hoặc local:
```bash
java -jar backend/target/english-learning-backend-1.0.0.jar
```

### 2. Build Frontend sang Bundle tĩnh:
```bash
cd frontend
npm run build
```
* Toàn bộ mã nguồn tĩnh (HTML, CSS minified, JS chunks) sẽ nằm trong thư mục `frontend/dist/`.
* Có thể triển khai trực tiếp lên Nginx, Caddy hoặc chạy thông qua bất kỳ web server tĩnh nào.

---

## 10. XỬ LÝ SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

1. **Lỗi không kết nối được Database (`Connection refused: connect`)**:
   * Kiểm tra dịch vụ `SQL Server (MSSQLSERVER)` hoặc `SQL Server Browser` trong *Services.msc* có đang ở trạng thái `Running` hay không.
   * Mở *SQL Server Configuration Manager*, kiểm tra mục *Protocols for MSSQLSERVER* -> đảm bảo **TCP/IP** đang ở trạng thái `Enabled` và cổng `IPAll` là `1433`.
   * Kiểm tra tài khoản `sa` và mật khẩu trong `application.yml` đã khớp với mật khẩu SQL Server của bạn chưa.

2. **Lỗi `Introducing FOREIGN KEY constraint may cause cycles or multiple cascade paths`**:
   * Khi tạo các bảng có nhiều khóa ngoại tham chiếu chéo (như bảng `account_credentials` hay `financial_transactions`), SQL Server yêu cầu thiết lập `ON DELETE NO ACTION` để tránh xung đột chu trình cascade.

3. **Lỗi CORS khi gọi API**:
   * Backend mặc định mở CORS cho địa chỉ `http://localhost:5173`. Nếu bạn đổi cổng chạy frontend, hãy cập nhật cấu hình cho phép trong `WebMvcConfig.java` hoặc `SecurityConfig.java`.

4. **Kích thước file upload vượt quá giới hạn**:
   * Hệ thống cấu hình giới hạn tệp tối đa là **50MB** (`spring.servlet.multipart.max-file-size=50MB`). Nếu bạn cần upload các file ZIP hay tài liệu lớn hơn, hãy tăng giá trị này trong `application.yml`.

---

## 📄 BẢN QUYỀN & TÁC GIẢ
* **Tác giả**: Lãnh Minh Hiếu (`hieulm24@gmail.com`)
* **Dự án**: [PersonalManagement Repository trên GitHub](https://github.com/hieulm24/PersonalManagement)
* Được phát triển phục vụ mục đích học tập, tối ưu hóa năng suất cá nhân và quản trị cuộc sống hàng ngày.
