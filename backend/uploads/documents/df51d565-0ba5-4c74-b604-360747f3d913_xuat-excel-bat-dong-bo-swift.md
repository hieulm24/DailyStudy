# Xuất Excel bất đồng bộ — màn "Danh sách điện swift về phân hệ"

> Phạm vi: **chỉ** tính năng nút "Xuất file" chạy bất đồng bộ của màn swift (dự án `pmh_gw_bidv_international`).
> Tài liệu đi theo 4 lớp:
> - **Phần A** — bức tranh hệ thống: có những thành phần nào, dữ liệu nằm ở đâu (RAM / đĩa / phân vùng nào / trình duyệt).
> - **Phần B** — giải thích **từng file, từng hàm, từng dòng quan trọng** của BE: code làm gì → để làm gì → bên dưới máy làm gì.
> - **Phần C** — như vậy với FE.
> - **Phần D–G** — chạy thử một lượt từ đầu đến cuối theo thời gian, vòng đời, các tình huống lỗi, các giới hạn.

---

## Mục lục

- [Phần A. Bức tranh hệ thống](#phần-a-bức-tranh-hệ-thống)
  - [A1. Vì sao phải bất đồng bộ](#a1-vì-sao-phải-bất-đồng-bộ)
  - [A2. Các thành phần tham gia](#a2-các-thành-phần-tham-gia)
  - [A3. Dữ liệu nằm ở đâu: RAM, đĩa, phân vùng](#a3-dữ-liệu-nằm-ở-đâu-ram-đĩa-phân-vùng)
  - [A4. Heap, Stack, ThreadLocal — 3 khái niệm phải nắm](#a4-heap-stack-threadlocal--3-khái-niệm-phải-nắm)
  - [A5. Luồng (thread): ai làm việc gì](#a5-luồng-thread-ai-làm-việc-gì)
  - [A6. Danh sách file của tính năng](#a6-danh-sách-file-của-tính-năng)
- [Phần B. Giải thích code BE](#phần-b-giải-thích-code-be)
- [Phần C. Giải thích code FE](#phần-c-giải-thích-code-fe)
- [Phần D. Chạy một lượt từ đầu đến cuối](#phần-d-chạy-một-lượt-từ-đầu-đến-cuối)
- [Phần E. Vòng đời](#phần-e-vòng-đời)
- [Phần F. Tình huống và mã lỗi](#phần-f-tình-huống-và-mã-lỗi)
- [Phần G. Giới hạn và điểm cần lưu ý](#phần-g-giới-hạn-và-điểm-cần-lưu-ý)

---

# Phần A. Bức tranh hệ thống

## A1. Vì sao phải bất đồng bộ

### Cách cũ (đồng bộ): `POST /swift-msg/in/export` — vẫn còn trong code

```
Trình duyệt ──POST──▶ BE: gọi procedure → map dòng → dựng Excel → byte[] ──▶ trả file
            ◀──────── (trình duyệt đứng chờ suốt, có thể vài chục giây) ──────
```

Chuyện gì xảy ra với máy chủ trong lúc chờ:

| Tài nguyên | Bị chiếm thế nào |
|---|---|
| **Luồng Tomcat** | 1 luồng bị giữ suốt thời gian dựng file. Tomcat chỉ có số luồng hữu hạn (mặc định Spring Boot là 200). Nhiều người cùng xuất → các luồng bị giữ hết → các API khác (tìm kiếm, xem chi tiết…) của **cả service** phải xếp hàng. |
| **Kết nối HTTP** | Mở lâu. Gateway (Kong), proxy hoặc trình duyệt có thời gian chờ tối đa; quá là cắt → FE nhận lỗi 504 dù BE vẫn đang làm (làm xong cũng không ai nhận). |
| **RAM** | Cả file nằm trong `byte[]` trên heap cho tới khi gửi xong. |
| **Người dùng** | Màn hình bị overlay loading, không làm gì khác được. |
| **Số việc nặng cùng lúc** | Không giới hạn: 30 người bấm là 30 procedure nặng + 30 file đang dựng cùng lúc. |

### Cách mới (bất đồng bộ): tách 1 request dài thành 3 request ngắn + 1 việc chạy nền

```
① POST /in/export/init     ── vài ms ──▶ nhận "số phiếu" taskId
                                          (việc nặng chuyển sang luồng nền, chạy độc lập)
② GET  /export-status      ── vài ms ──▶ "xong chưa?" (lặp mỗi 3 giây)
③ GET  /export-download    ── stream ──▶ lấy file
```

Hình dung giống **gọi món ở quán**: gọi món → nhận phiếu có số ngay → về chỗ ngồi → nhìn bảng số → thấy số mình thì lên lấy món.

| Quán ăn | Hệ thống |
|---|---|
| Quầy nhận order | Endpoint `export/init` |
| Tờ phiếu có số | `taskId` (UUID) |
| Sổ ghi các phiếu | `Map<String, ExportTask> tasks` trong RAM |
| Bếp có 2 đầu bếp | 2 luồng nền `swift-export-1`, `swift-export-2` |
| Chỗ xếp phiếu chờ nấu, tối đa 20 | `LinkedBlockingQueue` sức chứa 20 |
| Bảng số "đã xong" | Endpoint `export-status` |
| Quầy trả món | Endpoint `export-download` |
| Món để quá 30 phút thì dọn | `retention-minutes: 30` |

## A2. Các thành phần tham gia

```
┌──────────────────────┐   HTTP    ┌────────────┐   HTTP    ┌──────────────────────────── Tiến trình Java BE (gwi-fe-manual-api) ─────────────────────────────┐
│ Trình duyệt          │ ────────▶ │ Kong /     │ ────────▶ │                                                                                                 │
│  Tab Angular         │ ◀──────── │ proxy      │ ◀──────── │  Tomcat (nhận HTTP) ──▶ SwiftSeachController ──▶ ExportTaskServiceImpl ──▶ ThreadPoolExecutor   │
│  - biến exportTaskId │           └────────────┘           │                                                  │                         │ luồng nền            │
│  - popup trạng thái  │                                    │                                                  │ Map tasks (RAM)         ▼                      │
│  - Blob file tải về  │                                    │                                                  │              SwiftListServiceImpl ──▶ Oracle   │
└──────────────────────┘                                    │                                                  │              ExcelExportUtil ──▶ đĩa           │
                                                            └──────────────────────────────────────────────────│───────────────────────────────────────────────│
                                                                                                               ▼                                               ▼
                                                                                           Ổ đĩa: {java.io.tmpdir}/pmh-export/{taskId}.export     Oracle: PCK_PMH_SWIFT_LIST.EXPORT_SWIFT_IN
```

- **Trình duyệt**: giữ **số phiếu** (`exportTaskId`) và hiển thị popup.
- **Tomcat**: web server nhúng trong Spring Boot, nhận request HTTP, mượn một luồng trong pool của nó để chạy controller.
- **`ExportTaskServiceImpl`**: "quầy + sổ + bếp": giữ bảng phiếu, đội luồng nền, thư mục file.
- **`SwiftListServiceImpl` + `ExcelExportUtil`**: phần nghiệp vụ, tức là **món ăn**: lấy dữ liệu swift và dựng file Excel.
- **Oracle**: chạy procedure trả danh sách điện.
- **Ổ đĩa**: nơi cất file kết quả.

## A3. Dữ liệu nằm ở đâu: RAM, đĩa, phân vùng

Câu hỏi then chốt: **3 request tách rời nhau, HTTP không nhớ gì giữa các lần gọi, vậy request ③ lấy đâu ra file mà request ① đặt làm?**

Trả lời: vì có 2 chỗ lưu **sống lâu hơn một request**.

| Thứ được lưu | Ở đâu | Tại sao sống được qua nhiều request |
|---|---|---|
| **Phiếu** `ExportTask` (trạng thái, tên file, đường dẫn file, lỗi, chủ phiếu) | **RAM**, vùng **heap** của JVM, bên trong `Map tasks` của object `ExportTaskServiceImpl` | Tiến trình Java chạy liên tục, không tắt giữa các request. `ExportTaskServiceImpl` là **singleton** (cả ứng dụng có đúng 1 object) → request nào cũng đụng vào **cùng một** Map |
| **File Excel** | **Ổ đĩa** | File trên đĩa không phụ thuộc request hay luồng nào; nằm đó tới khi bị xóa |
| **Số phiếu** `taskId` | **RAM của tab trình duyệt** (biến `exportTaskId`) | Tab còn mở, component còn sống thì biến còn |

`taskId` là **chìa khóa**: FE giữ chìa, BE giữ ổ khóa. Request sau đưa chìa lên → BE tra Map ra phiếu → phiếu chứa đường dẫn file trên đĩa.

### File nằm ở phân vùng / thư mục nào?

Code quyết định:
```yaml
# application.yml
export-task:
  dir:            # ← để trống
```
```java
// ExportTaskServiceImpl.resolveDir
String configured = props.getDir();
return configured == null || configured.isBlank()
        ? Paths.get(System.getProperty("java.io.tmpdir"), "pmh-export")   // ← trống thì dùng thư mục tạm của OS
        : Paths.get(configured);
```

`java.io.tmpdir` là **thư mục tạm mà hệ điều hành cấp cho tiến trình**, JVM tự đọc lúc khởi động:

| Chạy ở đâu | `java.io.tmpdir` | File kết quả nằm ở | Thuộc phân vùng |
|---|---|---|---|
| Máy dev Windows | `%TEMP%` = `C:\Users\<user>\AppData\Local\Temp\` | `C:\Users\<user>\AppData\Local\Temp\pmh-export\<taskId>.export` | **Ổ C** |
| Container deploy (image `ubi9/openjdk-17`, `ENTRYPOINT` không truyền `-Djava.io.tmpdir`) | `/tmp` | `/tmp/pmh-export/<taskId>.export` **bên trong container** | Lớp ghi tạm (writable layer) của container, thực chất là **đĩa của máy node** chạy pod, nằm trong thư mục dữ liệu của container runtime. **Không** phải ổ đĩa riêng; pod bị tạo lại là mất |

Trong lúc dựng file còn có **2 loại file tạm khác** cùng nằm trong `java.io.tmpdir`:

| File | Ai tạo | Để làm gì | Bị xóa khi nào |
|---|---|---|---|
| `pmh-export-<số ngẫu nhiên>.xlsx` | `TempFileUtil.create` → `Files.createTempFile` | Nơi ghi file Excel lần đầu | Được `Files.move` (đổi tên) thành `pmh-export/<taskId>.export` ngay sau khi ghi xong; lỗi thì xóa |
| `poifiles/poi-sxssf-sheet-*.xml` | Thư viện Apache POI (`SXSSFWorkbook`) | Đẩy bớt các dòng Excel đã ghi xuống đĩa cho nhẹ RAM (xem B13) | `wb.dispose()` ngay sau khi ghi file xong |

→ Muốn đổi sang phân vùng khác (ví dụ một volume riêng mount vào container) thì **chỉ cần cấu hình** `export-task.dir: /duong/dan`, không phải sửa code.

## A4. Heap, Stack, ThreadLocal — 3 khái niệm phải nắm

```
                         Tiến trình Java
┌──────────────────────────────────────────────────────────────────────────────┐
│ HEAP — 1 vùng DUY NHẤT, MỌI luồng cùng thấy                                   │
│   mọi object tạo bằng `new` đều nằm ở đây:                                    │
│   ExportTaskServiceImpl ─┬─ tasks (ConcurrentHashMap)                          │
│                          │    "a1b2…" ──▶ ExportTask{SUCCESS, file=/tmp/…}     │
│                          │    "c3d4…" ──▶ ExportTask{PROCESSING}               │
│                          └─ executor ─┬─ hàng chờ [lambda, lambda, …]          │
│                                       └─ 2 Worker (mỗi cái bọc 1 luồng)        │
│   SwiftSearchRequest (bộ lọc), List dòng DB, List dòng Excel, …               │
├──────────────────────────────────────────────────────────────────────────────┤
│ STACK — MỖI luồng 1 cái RIÊNG, chứa biến cục bộ + thứ tự gọi hàm              │
│   [http-nio-exec-3]: exportInit() → submit() → biến task, work …              │
│   [swift-export-1]:  run() → buildExportInFile() → biến rows, file …          │
│   Hàm return → phần stack của hàm đó bị bỏ đi NGAY                            │
├──────────────────────────────────────────────────────────────────────────────┤
│ THREADLOCAL — "ngăn kéo riêng" gắn với TỪNG luồng (object nằm trên heap,      │
│   nhưng luồng nào chỉ mở được ngăn của luồng đó)                              │
│   [http-nio-exec-3].SecurityContext = user "hieunm"                           │
│   [swift-export-1].SecurityContext  = (trống)                                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

- **Heap**: object còn **được ai đó tham chiếu tới** (một field, một phần tử Map, một biến trên stack…) thì còn sống. Không ai tham chiếu nữa → **GC** (bộ dọn rác) thu hồi ở lần dọn tiếp theo.
- **Stack**: biến cục bộ `task`, `rows`… nằm ở đây. Hàm return là mất. Luồng này không nhìn được stack của luồng khác.
- **ThreadLocal**: Spring Security cất user đăng nhập vào ThreadLocal **của luồng Tomcat đang xử lý request đó**. Luồng nền không có ngăn này → gọi lấy user trong luồng nền sẽ ra `null`.

→ Vì vậy:
1. Muốn dữ liệu sống qua nhiều request, nhiều luồng cùng thấy → đặt vào **field của singleton trên heap**. Đó chính là `tasks`.
2. Muốn biết user trong việc chạy nền → phải lấy **ở luồng Tomcat** rồi cất vào object trên heap. Đó chính là `task.owner`.

## A5. Luồng (thread): ai làm việc gì

| Luồng | Tên trong log | Ai tạo | Làm gì trong tính năng này | Sống bao lâu |
|---|---|---|---|---|
| Luồng Tomcat | `http-nio-18191-exec-N` | Tomcat, có sẵn một pool | Chạy controller cho **cả 3 endpoint**. Mỗi request mượn 1 luồng vài ms rồi trả lại | Suốt đời ứng dụng (Tomcat quản lý) |
| Luồng nền | `swift-export-1`, `swift-export-2` | `ThreadPoolExecutor` qua `namedThreadFactory()` | Chạy `run()`: gọi procedure, dựng Excel, cất file | Tạo khi có việc; rảnh 60 giây thì tự kết thúc |
| Luồng JS trình duyệt | — | Trình duyệt | Chạy toàn bộ code Angular: bấm nút, timer, popup | Theo tab |

Mỗi luồng Java là một **luồng thật của hệ điều hành**: có stack riêng (mặc định khoảng 512KB–1MB) và được OS xếp lịch chạy trên các nhân CPU. Máy có nhiều nhân thì luồng Tomcat và luồng nền **chạy song song thật sự**. Vì vậy mới cần `volatile` và `ConcurrentHashMap` (xem B4, B10).

## A6. Danh sách file của tính năng

**BE** — `BE/gwi-fe-manual-api/src/main/`

| # | File | Vai trò |
|---|---|---|
| B1 | `resources/application.yml` (khối `export-task`) | Tham số: thư mục, số luồng, sức chứa hàng chờ, thời gian giữ file |
| B2 | `java/.../config/ExportTaskProperties.java` | Đọc khối cấu hình trên thành object Java |
| B3 | `java/.../enums/ExportTaskStatus.java` | 3 trạng thái của phiếu |
| B4 | `java/.../entity/ExportTask.java` | **Phiếu**: object trong RAM (không phải bảng DB) |
| B5 | `java/.../dto/response/ExportedFile.java` | Kết quả của việc dựng file: đường dẫn + tên + kiểu |
| B6 | `java/.../dto/response/ExportTaskResDTO.java` | Dữ liệu trả cho FE |
| B7 | `java/.../enums/ResponseCode.java` (`EXP001`–`EXP004`) | Mã lỗi riêng của tính năng |
| B8 | `java/.../exceptions/BusinessException.java` + `config/GlobalExceptionHandlerConfig.java` | Ném lỗi → HTTP 400 + JSON |
| B9 | `java/.../service/ExportTaskService.java` | Interface 3 hàm `submit`, `status`, `download` |
| B10 | `java/.../service/impl/ExportTaskServiceImpl.java` | **Lõi** cơ chế bất đồng bộ (dùng chung được cho mọi màn) |
| B11 | `java/.../controller/SwiftSeachController.java` | 3 endpoint |
| B12 | `java/.../service/impl/SwiftListServiceImpl.java` | Validate bộ lọc, lấy dữ liệu, dựng file (nghiệp vụ swift) |
| B13 | `java/.../utils/ExcelExportUtil.java` | Ghi Excel từ template ra file tạm |
| B14 | `java/.../utils/TempFileUtil.java` | Tạo / xóa file tạm |
| B15 | `java/.../utils/FileResponseUtil.java` | Dựng response trả file (stream + tên file UTF-8) |
| — | `resources/templates/swift_msg_template.xlsx` | Template Excel (logo, tiêu đề, style) |

**FE** — `FE/gwi-manual-fe/apps/pmh-gwd-manual/src/app/`

| # | File | Vai trò |
|---|---|---|
| C1 | `models/export-task/index.ts` | Kiểu dữ liệu `ExportTask`, `ExportTaskStatus` |
| C2 | `utils/api.ts` | 3 URL `EXPORT_INIT`, `EXPORT_STATUS`, `EXPORT_DOWNLOAD` |
| C3 | `service/swift-message-list/swift-message-list.service.ts` | 3 hàm gọi API + đọc lỗi blob |
| C4 | `utils/common.ts` (`readBlobErrorMessage`) | Đọc message lỗi từ body dạng Blob |
| C5 | `pages/swift-message-list/list/swift-message-list.component.ts` | Toàn bộ logic: đặt hàng, polling, tải, popup |
| C6 | `pages/swift-message-list/list/swift-message-list.component.html` + `.scss` | Nút "Xuất file" + popup góc phải dưới |

---

# Phần B. Giải thích code BE

Mỗi mục có 3 ý: **Code** → **Để làm gì** → **Bên dưới máy làm gì**.

## B1. `application.yml` — khối `export-task`

```yaml
export-task:
  dir:                    # thư mục cất file; trống = {java.io.tmpdir}/pmh-export
  pool-size: 2            # số file được dựng CÙNG LÚC
  queue-capacity: 20      # số yêu cầu được xếp hàng chờ
  retention-minutes: 30   # giữ file bao lâu sau khi xong
  max-run-minutes: 60     # PROCESSING quá lâu thế này thì coi như treo
```

**Để làm gì**: cho phép chỉnh sức chịu tải và thời gian giữ file theo môi trường mà không phải sửa code.

**Tác dụng từng tham số lên máy**:
- `pool-size: 2` → tối đa **2** lệnh `EXPORT_SWIFT_IN` chạy trên Oracle cùng lúc, tối đa **2** file đang dựng trên RAM. Tăng lên thì xuất song song nhanh hơn nhưng DB và RAM chịu nặng hơn.
- `queue-capacity: 20` → tối đa 20 yêu cầu **đang chờ** (mỗi yêu cầu chỉ tốn vài KB RAM cho lambda + bộ lọc). Người thứ 23 (2 chạy + 20 chờ + 1) bị từ chối `EXP003`.
- `retention-minutes: 30` → quyết định **dung lượng đĩa** bị chiếm: tối đa khoảng (số file xuất trong 30 phút) × (kích thước 1 file).
- `max-run-minutes: 60` → lưới an toàn cho phiếu kẹt `PROCESSING` (ví dụ luồng nền bị treo khi gọi DB).

## B2. `ExportTaskProperties.java`

```java
@Getter @Setter
@Component                                    // app này KHÔNG có @ConfigurationPropertiesScan -> phải có dòng này
@ConfigurationProperties(prefix = "export-task")
public class ExportTaskProperties {
    private String dir;
    private int poolSize = 2;
    private int queueCapacity = 20;
    private int retentionMinutes = 30;
    private int maxRunMinutes = 60;
}
```

**Để làm gì**: biến khối `export-task` trong yml thành một object Java có kiểu rõ ràng để service dùng.

**Bên dưới**:
- `@ConfigurationProperties(prefix = "export-task")`: lúc khởi động Spring đọc yml, tìm key bắt đầu bằng `export-task.`, rồi gọi setter tương ứng. `pool-size` tự khớp với `poolSize` (quy tắc "relaxed binding").
- `@Component`: bắt Spring **tạo bean** từ class này. Thiếu nó (app không bật `@ConfigurationPropertiesScan`) thì class không được nạp; khi đó constructor `ExportTaskServiceImpl(ExportTaskProperties props)` sẽ không tìm được bean và app không khởi động được.
- Giá trị gán sẵn (`= 2`, `= 20`…) là **mặc định** khi yml không khai báo key đó.

## B3. `ExportTaskStatus.java`

```java
public enum ExportTaskStatus {
    PROCESSING, // Đang chạy, FE tiếp tục hỏi trạng thái
    SUCCESS,    // Đã chạy xong, FE gọi API tải file được
    FAILED      // Lỗi
}
```

**Để làm gì**: 3 trạng thái duy nhất của một phiếu. FE dựa vào đây để quyết định hỏi tiếp, hiện nút tải hay hiện lỗi.

**Bên dưới**: Jackson chuyển enum thành chuỗi `"PROCESSING"` / `"SUCCESS"` / `"FAILED"` trong JSON. Phía FE khai báo đúng 3 chuỗi đó (`models/export-task`).

Chỉ có 2 hướng chuyển: `PROCESSING → SUCCESS` hoặc `PROCESSING → FAILED`. Không bao giờ quay ngược.

## B4. `ExportTask.java` — "tờ phiếu"

```java
@Getter @Setter
public class ExportTask {
    private final String taskId;     // số phiếu
    private final String owner;      // ai đặt (username)
    private final String name;       // loại việc, vd "swiftExportIn" (để ghi log)

    private volatile ExportTaskStatus status = ExportTaskStatus.PROCESSING;
    private final Instant createdAt = Instant.now();
    private volatile Instant finishedAt;
    private volatile Path file;              // đường dẫn file trên đĩa
    private volatile String fileName;        // tên hiển thị cho người dùng
    private volatile MediaType contentType;  // kiểu file (xlsx)
    private volatile ResponseCode errorCode; // mã lỗi nếu FAILED
    private volatile String errorDetail;     // giá trị điền vào {0} của mã lỗi
    private volatile boolean success;        // (hiện không dùng tới)
    ...
}
```

**Để làm gì**: nơi **duy nhất** lưu mọi thông tin về một lần xuất file. Luồng nền ghi kết quả vào đây, các request status/download đọc ra từ đây.

**Không phải entity DB**: dù nằm trong package `entity`, class này **không có `@Entity`**, không map bảng nào. Nó chỉ là object Java thường nằm trên heap.

**Từng field**:

| Field | Ai ghi | Ai đọc | Tác dụng |
|---|---|---|---|
| `taskId` (`final`) | constructor (luồng Tomcat) | Map key, log, tên file trên đĩa | Chìa khóa tra phiếu |
| `owner` (`final`) | constructor, lấy từ `currentUser()` | `require()` | Chặn người khác xem hoặc tải phiếu của mình |
| `name` (`final`) | constructor | log | Biết phiếu này là việc gì khi đọc log |
| `status` | constructor (`PROCESSING`), `run()`/`fail()` (luồng nền) | `status()`, `download()`, `toResponse()` | Trạng thái |
| `createdAt` (`final`) | lúc `new` | `sweepExpired()` | Tính mốc 60 phút "coi như treo" |
| `finishedAt` | `finally` trong `run()` | `sweepExpired()` | Tính mốc 30 phút "hết hạn giữ file" |
| `file` | `run()` sau `store()` | `download()`, `sweepExpired()`, `shutdown()` | Đường dẫn file trên đĩa |
| `fileName` | `run()` | `toResponse()`, `download()` | Tên hiện cho người dùng, ví dụ `DanhSachDienSwiftDen_28092026_101530.xlsx` |
| `contentType` | `run()` | `download()` | Header `Content-Type` |
| `errorCode`, `errorDetail` | `fail()` | `toResponse()`, `download()` | Báo lỗi lại cho FE |

**Vì sao `final` ở một số field**: `final` gán 1 lần trong constructor và không bao giờ đổi. Java đảm bảo luồng nào nhìn thấy object cũng thấy đúng giá trị `final` đã gán.

**Vì sao `volatile` ở các field còn lại**: vì **2 luồng khác nhau** cùng đụng vào một phiếu: luồng nền **ghi**, luồng Tomcat (request status) **đọc**. Hai luồng có thể chạy trên hai nhân CPU khác nhau:

```
    Nhân CPU A (luồng nền swift-export-1)          Nhân CPU B (luồng Tomcat - request status)
    ┌──────────────────────┐                        ┌──────────────────────┐
    │ cache L1/L2 riêng    │                        │ cache L1/L2 riêng    │
    │ status = SUCCESS  ✍  │                        │ status = PROCESSING  │ ← giá trị CŨ còn trong cache
    └─────────┬────────────┘                        └─────────┬────────────┘
              │  (chưa chắc đã ghi ra RAM)                    │  (chưa chắc đã đọc lại từ RAM)
              ▼                                               ▼
    ┌─────────────────────────────── RAM chung (heap) ───────────────────────────────┐
```

- **Không có `volatile`**: mỗi nhân CPU có bộ nhớ đệm riêng. Giá trị luồng nền ghi có thể chưa ra RAM chung, và luồng đọc có thể cứ đọc bản cũ trong cache của nó. Thêm nữa, trình biên dịch và CPU được phép **đảo thứ tự** các lệnh ghi. Hậu quả là FE hỏi mãi vẫn thấy `PROCESSING`, hoặc thấy `SUCCESS` mà `file` vẫn `null`.
- **Có `volatile`**:
  1. Ghi field `volatile` → bắt buộc đẩy ra RAM chung. Đọc field `volatile` → bắt buộc đọc từ RAM chung.
  2. Quy tắc **happens-before** của Java: mọi thứ luồng A ghi **trước** khi ghi field `volatile` X, luồng B **thấy đủ** khi B đọc X. Đây là lý do `run()` đặt `status = SUCCESS` **cuối cùng** (xem B10.8).

## B5. `ExportedFile.java`

```java
public record ExportedFile(Path file, String fileName, MediaType contentType) { }
```

**Để làm gì**: giá trị trả về của "công việc" (`Supplier<ExportedFile>`). Nó là **hợp đồng** giữa lõi `ExportTaskServiceImpl` và phần nghiệp vụ: nghiệp vụ nào cũng chỉ cần trả về 3 thứ này.

**Vì sao mang `Path` mà không mang `byte[]`**:
- `byte[]` = cả file nằm trên heap. File được giữ tới 30 phút; 20 file × vài chục MB nằm lì trên heap chừng ấy thời gian thì dễ `OutOfMemoryError`.
- `Path` = chỉ là **chuỗi đường dẫn** (vài chục byte). Nội dung nằm trên đĩa.

`record`: kiểu dữ liệu bất biến của Java 16+, tự sinh constructor, getter `file()`, `fileName()`, `contentType()`, `equals`, `hashCode`.

## B6. `ExportTaskResDTO.java`

```java
@Getter @Setter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)   // field null thì không xuất hiện trong JSON
public class ExportTaskResDTO {
    private String taskId;
    private ExportTaskStatus status;
    private String fileName;     // chỉ có khi SUCCESS
    private String errorCode;    // chỉ có khi FAILED
    private String errorMessage; // chỉ có khi FAILED
}
```

**Để làm gì**: **bản sao để gửi ra ngoài** của phiếu. Không trả thẳng `ExportTask` vì nó chứa những thứ FE không được thấy: `owner`, đường dẫn `file` trên máy chủ, `createdAt`…

**`@JsonInclude(NON_NULL)`**: bỏ field null khỏi JSON cho gọn. Ví dụ 3 dạng response:

```json
{ "taskId": "a1b2c3...", "status": "PROCESSING" }
{ "taskId": "a1b2c3...", "status": "SUCCESS", "fileName": "DanhSachDienSwiftDen_28092026_101530.xlsx" }
{ "taskId": "a1b2c3...", "status": "FAILED", "errorCode": "EXP004", "errorMessage": "Lưu file kết quả không thành công (a1b2c3...)" }
```
Các object này nằm trong field `data` của `BaseResponseParam` (`status: "0"`, `message`, `data`, …).

## B7. `ResponseCode.java` — 4 mã lỗi của tính năng

```java
EXP001("EXP001", "Yêu cầu xuất file không tồn tại hoặc đã hết hạn", "Export task not found or expired"),
EXP002("EXP002", "File đang được tạo, vui lòng chờ",                "Export is still processing"),
EXP003("EXP003", "Hệ thống đang bận xuất file, vui lòng thử lại sau", "Export queue is full"),
EXP004("EXP004", "Lưu file kết quả không thành công ({0})",          "Cannot store export file ({0})"),
```

| Mã | Ném ở | Khi nào |
|---|---|---|
| `EXP001` | `require()`, `download()` | taskId sai / không phải chủ / đã bị dọn / service đã restart; hoặc file không còn trên đĩa |
| `EXP002` | `download()` | Gọi tải khi phiếu còn `PROCESSING` |
| `EXP003` | `submit()` | Hàng chờ đầy |
| `EXP004` | `TempFileUtil.create`, `store()`, `download()` | Không tạo / chuyển / đọc được file trên đĩa (đĩa đầy, không có quyền ghi…) |

`{0}` là chỗ để điền giá trị: `getDescVi(Object... args)` gọi `MessageFormat.format(descVi, args)`, nên `EXP004.getDescVi("abc")` ra "Lưu file kết quả không thành công (abc)".

## B8. `BusinessException` + `GlobalExceptionHandlerConfig` — lỗi biến thành HTTP như thế nào

```java
// BusinessException
public BusinessException(ResponseCode responseCode, String fieldError, Throwable cause) { ... }

// GlobalExceptionHandlerConfig
@ExceptionHandler(BusinessException.class)
public ResponseEntity<CommonResDTO> handleBusiness(BusinessException ex, HttpServletRequest request) {
    logError(ex, request);
    return build(HttpStatus.BAD_REQUEST, ex.getResponseCode(), ex.getFieldError());
}
// build(): tranDesc = code.getDescVi(fieldError) -> CommonResDTO { ..., tranDesc, ... } với HTTP 400
```

**Để làm gì**: service chỉ cần `throw new BusinessException(ResponseCode.EXPxxx)`. `@RestControllerAdvice` bắt lại ở một chỗ chung và trả JSON đúng định dạng FE đang đọc (`tranDesc`).

**Điểm quan trọng với bất đồng bộ**: cơ chế này **chỉ hoạt động trên luồng Tomcat**, vì nó bắt exception bay ra khỏi controller **trong lúc đang xử lý một request**.
- Exception ném trong `submit()`, `status()`, `download()` (chạy trên luồng Tomcat) → handler bắt → HTTP 400.
- Exception ném trong **luồng nền** (`run()`) → **không có request nào để trả về**, handler không bao giờ thấy. Vì vậy `run()` phải tự bắt và **ghi lỗi vào phiếu** (B10.8).

## B9. `ExportTaskService.java` — interface

```java
public interface ExportTaskService {
    ExportTaskResDTO submit(String taskName, Supplier<ExportedFile> work);
    ExportTaskResDTO status(String taskId);
    ResponseEntity<Resource> download(String taskId);
}
```

**Để làm gì**: 3 hàm tương ứng 3 bước. `submit` nhận **`Supplier<ExportedFile>`**, tức là "một đoạn code mà khi gọi `.get()` sẽ trả về `ExportedFile`". Nhờ vậy lõi này **không biết gì về swift**: màn nào muốn xuất bất đồng bộ chỉ cần đưa vào một `Supplier` của mình.

## B10. `ExportTaskServiceImpl.java` — lõi của cơ chế

### B10.1. Hằng số và field

```java
private static final String DEFAULT_DIR_NAME = "pmh-export";
private static final String TEMP_SUFFIX = ".export";

private final ExportTaskProperties props;
private final Path dir;
private final ThreadPoolExecutor executor;
private final Map<String, ExportTask> tasks = new ConcurrentHashMap<>();
```

| Thứ | Tác dụng |
|---|---|
| `DEFAULT_DIR_NAME` | Tên thư mục con trong `java.io.tmpdir`, gom file của tính năng vào một chỗ để dễ nhận ra và dọn |
| `TEMP_SUFFIX = ".export"` | Đuôi file trên đĩa. Không dùng `.xlsx` vì tên file thật cho người dùng nằm ở `task.fileName`, tên trên đĩa chỉ để máy quản lý |
| `props` | Cấu hình B2 |
| `dir` | Đường dẫn thư mục cất file (xem A3) |
| `executor` | Đội luồng nền + hàng chờ |
| `tasks` | **Sổ phiếu**: `taskId → ExportTask` |

**Vì sao `ConcurrentHashMap` mà không phải `HashMap`**: nhiều luồng Tomcat cùng `put` / `get` / `remove` một lúc (nhiều người xuất và hỏi cùng lúc), cộng thêm vòng lặp dọn rác. `HashMap` thường bị ghi đồng thời có thể **hỏng cấu trúc bên trong** (mất phần tử, lặp vô hạn khi resize). `ConcurrentHashMap` khóa theo từng ngăn nhỏ, đọc gần như không cần khóa → an toàn mà vẫn nhanh. Duyệt nó bằng iterator trong lúc luồng khác sửa cũng không ném `ConcurrentModificationException`.

**Vì sao Map sống mãi**: `ExportTaskServiceImpl` là bean `@Service` (singleton). Spring giữ tham chiếu tới nó suốt đời ứng dụng → nó và mọi field (Map, executor) **không bao giờ bị GC**.

### B10.2. Constructor — dựng quầy lúc khởi động

```java
public ExportTaskServiceImpl(ExportTaskProperties props) {
    this.props = props;
    this.dir = resolveDir(props);
    this.executor = new ThreadPoolExecutor(
            props.getPoolSize(), props.getPoolSize(),                // corePoolSize = maxPoolSize = 2
            60L, TimeUnit.SECONDS,                                    // keepAliveTime
            new LinkedBlockingQueue<>(props.getQueueCapacity()),      // hàng chờ tối đa 20
            namedThreadFactory(),                                     // cách tạo luồng
            new ThreadPoolExecutor.AbortPolicy());                    // đầy -> ném RejectedExecutionException
    this.executor.allowCoreThreadTimeOut(true);
    log.info("Hang doi xuat file | dir={} | poolSize={} | queueCapacity={}", dir, ...);
}
```

**Để làm gì**: chạy **1 lần** khi ứng dụng khởi động, dựng sẵn đội luồng và hàng chờ.

**Từng tham số của `ThreadPoolExecutor`**:

| Tham số | Giá trị | Tác dụng |
|---|---|---|
| `corePoolSize` | 2 | Số luồng "nòng cốt" |
| `maximumPoolSize` | 2 | Số luồng tối đa. Bằng core → **đội luồng cố định 2**, không bao giờ nở thêm |
| `keepAliveTime` | 60 giây | Luồng rảnh quá thời gian này thì kết thúc |
| `workQueue` | `LinkedBlockingQueue(20)` | Hàng chờ **có giới hạn** 20. Nếu dùng hàng chờ không giới hạn, lúc quá tải việc sẽ dồn mãi, ăn hết RAM |
| `threadFactory` | `namedThreadFactory()` | Đặt tên luồng + đặt daemon (B10.13) |
| `handler` | `AbortPolicy` | Hết luồng + hết chỗ chờ → ném `RejectedExecutionException` ngay, **không chặn** luồng Tomcat |
| `allowCoreThreadTimeOut(true)` | — | Cho phép cả 2 luồng nòng cốt tự kết thúc khi rảnh 60s → không ai xuất file thì không giữ luồng nào |

**Bên dưới**: tạo `ThreadPoolExecutor` **chưa tạo luồng nào**. Luồng chỉ được tạo khi có việc đầu tiên. Dòng log ghi ra `dir` thực tế, đọc log lúc khởi động là biết file sẽ nằm ở đâu.

### B10.3. `submit()` — nhận việc

```java
@Override
public ExportTaskResDTO submit(String taskName, Supplier<ExportedFile> work) {
    sweepExpired();                                                            // (1)
    ExportTask task = new ExportTask(newTaskId(), currentUser(), taskName);    // (2)
    tasks.put(task.getTaskId(), task);                                         // (3)
    try {
        executor.execute(() -> run(task, work));                               // (4)
    } catch (RejectedExecutionException e) {
        tasks.remove(task.getTaskId());                                        // (5)
        log.warn("Hang doi xuat file day | task={} | queue={}", taskName, executor.getQueue().size());
        throw new BusinessException(ResponseCode.EXP003, null, e);
    }
    log.info("[{}] >>> NHAN {} | user={} | dang cho={}", ...);
    return toResponse(task);                                                   // (6)
}
```

Chạy trên **luồng Tomcat** của request `init`.

**(1) `sweepExpired()`** — dọn phiếu và file hết hạn trước khi nhận việc mới (chi tiết B10.10). Đặt ở đây để khỏi phải có luồng hẹn giờ riêng: mỗi lần có việc mới là một lần dọn.

**(2) `new ExportTask(newTaskId(), currentUser(), taskName)`**
- `newTaskId()` → UUID 32 ký tự hex, ngẫu nhiên, không đoán được.
- `currentUser()` → **phải gọi ở đây**, vì đây là luồng Tomcat nên ThreadLocal còn user (A4). Giá trị được chép vào `task.owner` trên heap.
- Object phiếu được tạo trên heap với `status = PROCESSING`, `createdAt = bây giờ`.

**(3) `tasks.put(...)`** — Map lưu **tham chiếu** tới phiếu, không copy. Map và luồng nền sau này cùng trỏ vào **một** object → luồng nền sửa phiếu thì request status tra Map sẽ thấy ngay.
Phải `put` **trước** `execute`: nếu làm ngược lại, luồng nền có thể chạy xong trước cả khi phiếu được cất vào Map.

**(4) `executor.execute(() -> run(task, work))`**
- `() -> run(task, work)` là một **lambda** (`Runnable`): Java tạo một object trên heap, **chụp lại** (capture) tham chiếu tới `task` và `work`. Chưa chạy gì cả.
- `execute()` bên trong `ThreadPoolExecutor` chạy đúng 3 nhánh:

```
 số luồng đang có < corePoolSize (2)?
     ├─ có  → addWorker(): tạo Thread mới, Thread.start(), giao luôn việc này cho nó
     └─ không
          ↓
 workQueue.offer(việc)  — hàng chờ còn chỗ (< 20)?
     ├─ có  → việc nằm cuối hàng, một luồng rảnh sẽ lấy ra chạy
     └─ không
          ↓
 số luồng < maximumPoolSize (2)?   → không (max = core)
          ↓
 AbortPolicy → throw RejectedExecutionException
```
- `Thread.start()` nhờ **hệ điều hành** tạo luồng thật (cấp stack riêng) và xếp lịch cho nó chạy trên CPU. Cả `execute()` chỉ tốn **micro-giây**: luồng Tomcat **không chờ** việc chạy xong.

**(5) Hàng chờ đầy** → gỡ phiếu vừa tạo khỏi Map (không để phiếu "ma" nằm đó mãi ở `PROCESSING`) → ném `EXP003` → handler B8 trả HTTP 400 → FE hiện "Hệ thống đang bận xuất file, vui lòng thử lại sau".

**(6) `return toResponse(task)`** → `{taskId, PROCESSING}`. Luồng Tomcat ghi JSON ra socket rồi **quay về pool Tomcat**. Request HTTP đã **kết thúc**, nhưng phiếu (Map giữ) và lambda (hàng chờ hoặc luồng nền giữ) vẫn sống trên heap.

### B10.4. `status()` — trả lời "xong chưa"

```java
@Override
public ExportTaskResDTO status(String taskId) {
    return toResponse(require(taskId));
}
```
Chạy trên **một luồng Tomcat bất kỳ** (thường khác luồng lúc `init`).

**Bên dưới**: `require` → `tasks.get(taskId)` là một lần tra bảng băm trong RAM: **không đụng DB, không đụng đĩa**, xong trong micro-giây. Vì vậy FE hỏi mỗi 3 giây gần như không tốn gì cho BE. Đọc `status` là đọc field `volatile` → luôn thấy giá trị mới nhất luồng nền vừa ghi.

### B10.5. `require()` — tìm phiếu + kiểm tra chủ

```java
private ExportTask require(String taskId) {
    ExportTask task = taskId == null ? null : tasks.get(taskId);
    if (task == null) {
        throw new BusinessException(ResponseCode.EXP001);
    }
    String user = currentUser();
    if (task.getOwner() != null && !task.getOwner().equals(user)) {
        log.warn("[{}] Tu choi truy cap | owner={} | nguoi goi={}", taskId, task.getOwner(), user);
        throw new BusinessException(ResponseCode.EXP001);
    }
    return task;
}
```

**Để làm gì**: dùng chung cho `status` và `download`.
- Không có phiếu → `EXP001`.
- Có phiếu nhưng người gọi **không phải chủ** → **cũng `EXP001`**. Cố ý dùng cùng một mã để người dò taskId không phân biệt được "có tồn tại nhưng không phải của tôi" với "không tồn tại".
- `currentUser()` ở đây chạy trên luồng Tomcat của request hiện tại → lấy đúng user đang gọi rồi so với `owner` đã cất lúc `init`.
- `owner != null &&`: nếu lúc `init` không lấy được user (ra `null`) thì bỏ qua kiểm tra chủ (xem G).

### B10.6. `download()` — trả file

```java
@Override
public ResponseEntity<Resource> download(String taskId) {
    ExportTask task = require(taskId);
    if (task.getStatus() == ExportTaskStatus.PROCESSING) throw new BusinessException(ResponseCode.EXP002);
    if (task.getStatus() == ExportTaskStatus.FAILED)     throw new BusinessException(task.getErrorCode(), task.getErrorDetail());
    Path file = task.getFile();
    if (file == null || !Files.isReadable(file)) {
        log.warn("[{}] File ket qua khong con tren dia | path={}", taskId, file);
        throw new BusinessException(ResponseCode.EXP001);
    }
    long size;
    try {
        size = Files.size(file);
    } catch (IOException e) {
        throw new BusinessException(ResponseCode.EXP004, task.getFileName(), e);
    }
    log.info("[{}] <<< TAI VE {} | bytes={}", taskId, task.getFileName(), size);
    return FileResponseUtil.buildFileResponse(new FileSystemResource(file), task.getFileName(), task.getContentType(), size);
}
```

**Từng bước**:
1. `require` → phiếu của đúng người.
2. Còn `PROCESSING` → `EXP002` (chặn trường hợp gọi tải thẳng mà chưa hỏi status).
3. `FAILED` → ném lại **đúng mã lỗi lúc dựng file**.
4. `Files.isReadable(file)` → hỏi OS: file còn trên đĩa không, tiến trình có quyền đọc không. File có thể đã mất (bị dọn, thư mục tạm bị xóa…).
5. `Files.size(file)` → hỏi OS kích thước để đặt header `Content-Length`; trình duyệt biết trước dung lượng nên hiện được tiến độ tải.
6. `new FileSystemResource(file)` → **chưa đọc file**, chỉ là object bọc đường dẫn.

**Bên dưới — file được gửi đi thế nào (stream)**: sau khi controller return, Spring (`ResourceHttpMessageConverter`) mới mở file và chạy vòng lặp:
```
 ĐĨA ──đọc 1 khúc (vài KB)──▶ bộ đệm nhỏ trên heap ──ghi──▶ socket ──mạng──▶ trình duyệt
      (lặp lại tới hết file, xong thì đóng file)
```
RAM chỉ tốn **một khúc**, dù file lớn bao nhiêu. Ngược với bản đồng bộ cũ nạp cả file vào `byte[]` rồi mới gửi.

File **không bị xóa sau khi tải** → trong 30 phút tải lại bao nhiêu lần cũng được.

### B10.7. `run()` — việc chạy nền

```java
/** KHONG de exception thoat ra: thoat ra = task treo PROCESSING mai mai. */
private void run(ExportTask task, Supplier<ExportedFile> work) {
    long start = System.currentTimeMillis();
    try {
        ExportedFile result = work.get();                              // (a)
        task.setFile(store(task.getTaskId(), result.file()));          // (b)
        task.setFileName(result.fileName());                           // (c)
        task.setContentType(result.contentType());                     // (c)
        task.setStatus(ExportTaskStatus.SUCCESS);                      // (d) ĐẶT CUỐI CÙNG
        log.info("[{}] <<< XONG {} | fileName={} | bytes={} | duration={}ms", ...);
    } catch (BusinessException e) {
        fail(task, e.getResponseCode(), e.getFieldError(), e, start);  // (e)
    } catch (Exception e) {
        fail(task, ResponseCode.TEC999, null, e, start);               // (e)
    } finally {
        task.setFinishedAt(Instant.now());                             // (f)
    }
}
```

**Ai chạy**: luồng `swift-export-1` hoặc `swift-export-2`. Mỗi luồng nền chạy vòng lặp có sẵn trong `ThreadPoolExecutor` (hàm `runWorker`):
```
while (việc = getTask()) != null:     // getTask(): lấy việc từ hàng chờ, CHỜ tối đa 60s
    việc.run()                         //   hàng chờ trống → luồng NGỦ (không tốn CPU)
                                       //   60s không có việc → getTask() trả null → luồng kết thúc
```

**(a) `work.get()`** — lúc này lambda ở controller **mới thật sự chạy**, tức là `swiftListService.buildExportInFile(request)` (B12). Đây là phần tốn thời gian nhất: gọi DB, dựng Excel.

**(b) `store(...)`** — chuyển file từ chỗ tạm vào thư mục `pmh-export`, đặt tên theo taskId (B10.9).

**(c)** Ghi tên hiển thị và kiểu file vào phiếu.

**(d) `setStatus(SUCCESS)` đặt cuối cùng** — kết hợp với `volatile` (B4): luồng status nào đã đọc thấy `SUCCESS` thì chắc chắn thấy `file`, `fileName`, `contentType` đã có giá trị. Nếu đặt `SUCCESS` lên đầu, sẽ có khoảnh khắc FE thấy xong, bấm tải, nhưng `file` còn `null` → lỗi.

**(e) Bắt mọi exception** — đây là điểm **khác biệt cốt lõi** giữa bất đồng bộ và đồng bộ:
- Ở luồng nền **không có request HTTP nào đang mở**, không có handler B8. Exception thoát khỏi `run()` thì executor chỉ in stack trace rồi bỏ việc: phiếu **kẹt `PROCESSING` mãi mãi** và FE hỏi vô tận (tới khi quá `max-run-minutes`).
- Nên lỗi được **ghi vào phiếu**. FE biết lỗi ở lần hỏi status tiếp theo. Lỗi không được "ném về" mà được **"gửi lại" qua phiếu**.
- `BusinessException` (lỗi đã biết: procedure trả lỗi, `EXP004`…) → giữ đúng mã. `Exception` còn lại (NullPointer, SQL…) → `TEC999`.

**(f) `finally`** — dù thành công hay lỗi đều ghi mốc kết thúc, dùng để tính 30 phút giữ file.

Sau `run()`, luồng nền quay lại vòng lặp lấy việc tiếp. Lambda `work` (cùng bộ lọc `request` nó chụp) không còn ai tham chiếu nên GC thu hồi. **Chỉ còn lại** phiếu (trong Map) và file (trên đĩa).

### B10.8. `fail()`

```java
private void fail(ExportTask task, ResponseCode code, String detail, Exception e, long start) {
    task.setErrorCode(code);
    task.setErrorDetail(detail);
    task.setStatus(ExportTaskStatus.FAILED);        // cũng đặt SAU errorCode/errorDetail, cùng lý do như SUCCESS
    log.error("[{}] !!! LOI {} | code={} | duration={}ms | error={}", ..., e);   // kèm stack trace để điều tra
}
```

### B10.9. `store()` — cất file

```java
private Path store(String taskId, Path source) {
    try {
        Files.createDirectories(dir);                                        // tạo thư mục nếu chưa có
        Path target = dir.resolve(taskId + TEMP_SUFFIX);                     // .../pmh-export/<taskId>.export
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    } catch (IOException e) {
        TempFileUtil.deleteQuietly(source);                                  // không để file mồ côi
        log.error("Khong chuyen duoc file ket qua vao {}", dir, e);
        throw new BusinessException(ResponseCode.EXP004, taskId, e);
    }
}
```

**Để làm gì**: gom file vào thư mục riêng, **đặt tên theo taskId** → từ phiếu tìm ra file, và lúc dọn biết chắc file nào của phiếu nào.

**Bên dưới**:
- `Files.createDirectories(dir)`: đã có thì không làm gì; chưa có thì OS tạo. Lần xuất đầu tiên sau khi khởi động sẽ tạo thư mục.
- `Files.move`: file nguồn (`{tmpdir}/pmh-export-123.xlsx`) và đích (`{tmpdir}/pmh-export/…`) nằm **cùng một phân vùng**, nên OS chỉ **đổi mục trong bảng thư mục** của hệ thống file (rename). **Không chép byte nào** → file 100MB cũng xong tức thì. (Nếu `dir` được cấu hình sang phân vùng khác, Java tự chuyển thành chép rồi xóa, chậm hơn nhưng vẫn đúng.)
- Lỗi → xóa file nguồn rồi ném `EXP004`; `run()` bắt và ghi vào phiếu.

### B10.10. `sweepExpired()` — dọn rác

```java
private void sweepExpired() {
    Instant now = Instant.now();
    Duration retention = Duration.ofMinutes(props.getRetentionMinutes());   // 30'
    Duration maxRun    = Duration.ofMinutes(props.getMaxRunMinutes());      // 60'
    for (Iterator<Map.Entry<String, ExportTask>> it = tasks.entrySet().iterator(); it.hasNext(); ) {
        ExportTask task = it.next().getValue();
        boolean expired = task.getFinishedAt() != null
                ? task.getFinishedAt().plus(retention).isBefore(now)   // đã xong: quá 30' kể từ lúc xong
                : task.getCreatedAt().plus(maxRun).isBefore(now);      // chưa xong: quá 60' kể từ lúc tạo
        if (expired) {
            it.remove();                                   // gỡ khỏi Map
            TempFileUtil.deleteQuietly(task.getFile());    // xóa file trên đĩa
        }
    }
}
```

**Để làm gì**: không để phiếu tích mãi trên RAM, không để file tích mãi trên đĩa.

**Bên dưới**:
- `it.remove()`: Map là nơi **cuối cùng** tham chiếu tới phiếu → phiếu thành rác → GC thu hồi vùng heap ở lần dọn sau. Đó là cách một phiếu **chết trong RAM**.
- `deleteQuietly`: nhờ OS xóa file khỏi đĩa, trả lại dung lượng.
- Dùng `Iterator.remove()` trên `ConcurrentHashMap`: an toàn cả khi luồng khác đang `put` hay `get`.
- **Chỉ chạy trong `submit()`**: không ai bấm xuất mới thì phiếu và file cũ vẫn nằm đó, tới lần xuất sau hoặc tới khi tắt service.

### B10.11. `toResponse()`

```java
private ExportTaskResDTO toResponse(ExportTask task) {
    ExportTaskResDTO.ExportTaskResDTOBuilder builder = ExportTaskResDTO.builder()
            .taskId(task.getTaskId())
            .status(task.getStatus());
    if (task.getStatus() == ExportTaskStatus.SUCCESS) {
        builder.fileName(task.getFileName());
    }
    if (task.getStatus() == ExportTaskStatus.FAILED && task.getErrorCode() != null) {
        builder.errorCode(task.getErrorCode().getCode())
               .errorMessage(task.getErrorDetail() == null
                       ? task.getErrorCode().getDescVi()
                       : task.getErrorCode().getDescVi(task.getErrorDetail()));
    }
    return builder.build();
}
```
**Để làm gì**: chép phiếu ra DTO, **chỉ những gì FE được thấy**, tùy theo trạng thái. Message lỗi được dịch sẵn sang tiếng Việt ở BE nên FE chỉ việc hiện `errorMessage`.

### B10.12. `currentUser()`, `newTaskId()`, `resolveDir()`

```java
private String currentUser() {
    try { return SecurityContextUtils.getUserName(); }       // đọc SecurityContext (ThreadLocal)
    catch (Exception e) { log.debug(...); return null; }       // không có user thì null, không làm hỏng luồng
}
private static String newTaskId() {
    return UUID.randomUUID().toString().replace("-", "");     // 128 bit ngẫu nhiên → 32 ký tự hex
}
private static Path resolveDir(ExportTaskProperties props) { ... }   // xem A3
```

### B10.13. `namedThreadFactory()`

```java
private static ThreadFactory namedThreadFactory() {
    AtomicInteger seq = new AtomicInteger();
    return runnable -> {
        Thread thread = new Thread(runnable, "swift-export-" + seq.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    };
}
```
- **Tên `swift-export-N`**: log in ra `[swift-export-1]`, dump luồng cũng thấy tên này → nhìn là biết luồng xuất file. `AtomicInteger` để đánh số không trùng dù nhiều luồng cùng tạo.
- **`setDaemon(true)`**: JVM chỉ tự thoát khi hết luồng **không-daemon**. Luồng nền là daemon nên **không giữ** JVM sống lúc tắt service.

### B10.14. `shutdown()` — `@PreDestroy`

```java
@PreDestroy
void shutdown() {
    executor.shutdownNow();                                             // ngắt luồng đang chạy, bỏ việc đang chờ
    tasks.values().forEach(t -> TempFileUtil.deleteQuietly(t.getFile()));
    tasks.clear();
}
```
- Spring gọi hàm này khi **tắt ứng dụng bình thường** (Ctrl+C, K8s gửi SIGTERM…).
- Xóa hết file vì khởi động lại thì Map mới **rỗng**, không còn ai biết file nào thuộc phiếu nào để mà xóa.
- Tiến trình bị **giết đột ngột** (kill -9, bị hết RAM và OS giết, mất điện) thì hàm này **không chạy**: heap mất sạch (phiếu mất), file vẫn nằm lại trên đĩa làm rác (trong container thì mất theo container khi pod bị tạo lại).

## B11. `SwiftSeachController.java` — 3 endpoint

```java
@RestController
@RequestMapping(Constants.API_PATH.CONTEXT_PATH + Constants.API_PATH.SWIFT_MSG)
@RequiredArgsConstructor
public class SwiftSeachController {
    private final SwiftListService swiftListService;
    private final ExportTaskService exportTaskService;

    @PostMapping("/in/export/init")
    public ResponseEntity<BaseResponseParam> exportInit(@RequestBody SwiftSearchRequest request) {
        swiftListService.validateExportIn(request);                                        // (1)
        ExportTaskResDTO task = exportTaskService.submit("swiftExportIn",
                () -> swiftListService.buildExportInFile(request));                        // (2)
        return ResponseEntity.ok(new BaseResponseParam().success(task));                   // (3)
    }

    @GetMapping("/export-status")
    public ResponseEntity<BaseResponseParam> exportStatus(@RequestParam("taskId") String taskId) {
        return ResponseEntity.ok(new BaseResponseParam().success(exportTaskService.status(taskId)));
    }

    @GetMapping("/export-download")
    public ResponseEntity<Resource> exportDownload(@RequestParam("taskId") String taskId) {
        return exportTaskService.download(taskId);
    }
}
```

**`exportInit`**
- **(1) Validate trên luồng Tomcat, trước khi giao việc**: bộ lọc sai (ngày sai định dạng, từ ngày > đến ngày, số tiền âm… — B12) thì ném `BusinessException` → HTTP 400 **ngay lập tức**. Người dùng thấy lỗi tức thì thay vì đợi một vòng polling, và không tốn một chỗ trong hàng chờ.
- **(2) `() -> swiftListService.buildExportInFile(request)`**: dòng quan trọng nhất của controller.
  - Đây **không phải** lời gọi hàm. Java tạo ra một object lambda (`Supplier<ExportedFile>`) trên heap, **chụp** tham chiếu tới `swiftListService` và `request`.
  - Nhờ lambda giữ tham chiếu, object `request` (bộ lọc người dùng nhập) **không bị GC** khi request HTTP kết thúc. Nó sống tiếp trên heap cho tới khi luồng nền chạy xong.
  - Hình dung như **tờ công thức kèm sẵn nguyên liệu**, đưa cho bếp nấu sau.
  - `"swiftExportIn"` chỉ là tên để ghi log.
- **(3)** Bọc DTO vào `BaseResponseParam` (`status: "0"`, `message`, `data`) như các API khác của service.

**`exportStatus`, `exportDownload`**: dùng `GET` + `taskId` trên query string (`?taskId=...`) vì chỉ **đọc**, không đổi dữ liệu. Không gắn với swift → màn khác dùng lại được luôn.

`exportDownload` trả `ResponseEntity<Resource>` (không phải `BaseResponseParam`): thành công thì body là **file**; lỗi thì exception đi qua handler B8 thành JSON 400 (FE phải đọc riêng — C4).

## B12. `SwiftListServiceImpl.java` — phần nghiệp vụ swift

### `validateExportIn` / `validateFilter`

```java
public void validateExportIn(SwiftSearchRequest request) {
    ResponseCode invalid = validateFilter(request);
    if (invalid != null) throw new BusinessException(invalid);
}
```
`validateFilter` kiểm tra (dùng chung với API tìm kiếm):

| Điều kiện | Mã |
|---|---|
| `trxDateFrom` / `trxDateTo` sai định dạng | `VAL015` |
| Từ ngày > hôm nay | `VAL021` |
| Đến ngày > hôm nay | `VAL022` |
| Từ ngày > đến ngày | `VAL023` |
| Số tiền từ < 0 | `ACC029` |
| Số tiền đến < 0 | `ACC030` |
| Số tiền từ > số tiền đến | `ACC031` |

### `buildExportInFile` — món ăn mà luồng nền nấu

```java
public ExportedFile buildExportInFile(SwiftSearchRequest request) {
    long t0 = System.currentTimeMillis();
    List<SwiftExportRow> rows = loadExportInRows(request);                               // (1) lấy dữ liệu
    Path file = ExcelExportUtil.exportToTempFile(
            Constants.EXCEL_TEMPLATE.SWIFT_MSG_TEMPLATE,     // "templates/swift_msg_template.xlsx"
            EXPORT_IN_SHEET,                                 // "Dien den"
            EXPORT_IN_TITLE,                                 // "DANH SÁCH ĐIỆN SWIFT ĐẾN"
            buildTradeDate(request),                         // "dd/MM/yyyy - dd/MM/yyyy"
            rows, SwiftExportRow.class, EXPORT_COLUMNS);                                  // (2) ghi Excel ra đĩa
    log.info("buildExportInFile | rows={} | duration={}ms", rows.size(), System.currentTimeMillis() - t0);
    return new ExportedFile(file, buildExportInFileName(), FileResponseUtil.EXCEL_MEDIA_TYPE);   // (3)
}
```
Chạy trên **luồng nền** (được gọi từ `work.get()`).

### `loadExportInRows` — lấy dữ liệu từ Oracle

```java
private List<SwiftExportRow> loadExportInRows(SwiftSearchRequest request) {
    ProcedureResult result = baseProcedureRepository.callProcedures(
            PACKAGE_NAME + ".EXPORT_SWIFT_IN", mapper.mapToFilterParams(request));
    if (!result.isSuccess()) {
        log.error("EXPORT_SWIFT_IN tra loi: errCode={}, errDesc={}", ...);
        throw new BusinessException(toResponseCode(result.getErrCode()));     // → run() bắt → FAILED
    }
    SwiftReferenceData refData = swiftReferenceDataService.load();
    List<Map<String, Object>> data = result.getData();
    List<SwiftExportRow> rows = new ArrayList<>(data.size());
    for (int i = 0; i < data.size(); i++) {
        rows.add(resultMapper.mapToExportRow(resultMapper.mapToRespone(data.get(i), refData), i + 1));
    }
    return rows;
}
```

**Bên dưới máy làm gì**:
1. `mapper.mapToFilterParams(request)` → xếp bộ lọc thành danh sách tham số đúng **thứ tự** của procedure (repository bind tham số theo vị trí).
2. `callProcedures` → **mượn một kết nối DB** từ connection pool → gửi lệnh gọi `PCK_PMH_SWIFT_LIST.EXPORT_SWIFT_IN` qua mạng tới Oracle → Oracle chạy câu truy vấn → trả cursor → JDBC đọc từng lô dòng qua mạng → mỗi dòng thành một `Map<String,Object>` trên **heap** → trả kết nối về pool.
   Trong lúc chờ Oracle, luồng nền **đứng chờ mạng** (không tốn CPU). Đây là lý do việc này phải ở luồng nền chứ không ở luồng Tomcat.
3. Procedure báo lỗi → `BusinessException` → bay lên `run()` → phiếu `FAILED` với đúng mã.
4. `swiftReferenceDataService.load()` → danh mục (phân hệ, kênh, trạng thái…) để đổi **mã → tên hiển thị**.
5. Vòng lặp: `Map` dòng DB → `SwiftMessage` (qua `mapToRespone`) → `SwiftExportRow` (qua `mapToExportRow`, đánh STT `i + 1`).
   Lúc này trên heap có **cả** `List<Map>` lẫn `List<SwiftExportRow>`: đây là đỉnh RAM của bước lấy dữ liệu. Khi hàm return, `data` hết ai tham chiếu và được GC thu hồi.

### `buildExportInFileName`, `buildTradeDate`

```java
public String buildExportInFileName() {
    return EXPORT_IN_FILE_PREFIX + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
    // "DanhSachDienSwiftDen_" + "28092026_101530" + ".xlsx"
}
```
- Tên này là **tên người dùng thấy khi tải**, được lưu vào `task.fileName`. Nó khác với tên trên đĩa (`<taskId>.export`).
- Thời điểm lấy là lúc **dựng xong** (chạy trong luồng nền), không phải lúc bấm nút.
- `buildTradeDate`: dựng chuỗi dòng "Ngày giao dịch: …" trên file (khoảng từ–đến, 1 ngày nếu trùng, trống nếu không lọc ngày).

## B13. `ExcelExportUtil.java` — ghi Excel ra đĩa

### `exportToTempFile`

```java
public static <T> Path exportToTempFile(String templatePath, String sheetName, String title, String tradeDate,
                                        List<T> rows, Class<T> rowClass, List<String> fieldOrder) {
    Path tmp = TempFileUtil.create(".xlsx");                       // (1) OS tạo file rỗng
    try (OutputStream out = Files.newOutputStream(tmp)) {          // (2) mở file để ghi
        writeTo(out, templatePath, sheetName, title, tradeDate, rows, rowClass, fieldOrder);   // (3)
        return tmp;
    } catch (IOException e) {
        TempFileUtil.deleteQuietly(tmp);                           // (4) lỗi → xóa file dở dang
        throw new UncheckedIOException("Lỗi ghi file Excel tạm", e);
    } catch (RuntimeException e) {
        TempFileUtil.deleteQuietly(tmp);
        throw e;
    }
}
```
- **(1)** `Files.createTempFile("pmh-export-", ".xlsx")` → OS tạo **file rỗng**, tên ngẫu nhiên không trùng, trong `java.io.tmpdir`.
- **(2)** `try (...)` = try-with-resources: ra khỏi khối (dù lỗi hay không) thì `out.close()` tự chạy → OS **đẩy nốt bộ đệm xuống đĩa và đóng file**. Không có nó, file có thể bị cụt hoặc bị khóa.
- **(4)** Lỗi giữa chừng → xóa file dở, không để rác, rồi ném tiếp lên `run()` → `FAILED`.
- Bản cũ `export(...)` dùng chung `writeTo` nhưng ghi vào `ByteArrayOutputStream` (RAM) → ra `byte[]`. **Cùng một hàm dựng Excel, chỉ khác đích ghi**: bản đồng bộ ghi vào RAM, bản bất đồng bộ ghi ra đĩa.

### `writeTo` — dựng Excel từ template

```java
try (InputStream is = new ClassPathResource(templatePath).getInputStream();       // (1)
     XSSFWorkbook template = new XSSFWorkbook(is)) {                               // (2)
    Sheet templateSheet = template.getSheetAt(0);
    template.setSheetName(0, sheetName);                                           // "Dien den"
    List<Field> fields = resolveFields(rowClass, fieldOrder);                      // (3)
    int lastColIndex = FIRST_FIELD_COL_INDEX + fields.size() - 1;

    writeMergedText(templateSheet, TITLE_ROW_INDEX, TITLE_COL_INDEX, lastColIndex, title);          // dòng 5
    writeMergedText(templateSheet, DATE_ROW_INDEX, DATE_COL_INDEX, lastColIndex, DATE_LABEL + tradeDate); // dòng 6
    writeHeader(templateSheet, fields, templatePath);                                               // dòng 8

    CellStyle dataStyle = referenceStyle(templateSheet, STYLE_ROW_INDEX, ...);     // (4) style mẫu dòng 9
    float dataRowHeight = templateSheet.getRow(STYLE_ROW_INDEX).getHeightInPoints();

    for (int i = 0; i < fields.size(); i++) templateSheet.autoSizeColumn(FIRST_FIELD_COL_INDEX + i);  // (5)

    for (int r = lastRow; r >= STYLE_ROW_INDEX; r--) templateSheet.removeRow(...);  // (6)

    try (SXSSFWorkbook wb = new SXSSFWorkbook(template, 200)) {                    // (7)
        Sheet sheet = wb.getSheetAt(0);
        writeData(sheet, rows, fields, resolveColumnStyles(wb, fields, dataStyle), dataRowHeight);  // (8)
        wb.write(out);                                                             // (9)
        wb.dispose();                                                              // (10)
    }
}
```

| Bước | Làm gì | Bên dưới |
|---|---|---|
| (1) | Mở template `templates/swift_msg_template.xlsx` | File nằm **bên trong file jar** (classpath), không phải trên đĩa rời. `ClassPathResource` đọc nó như một luồng byte |
| (2) | `XSSFWorkbook` nạp template | Template nhỏ (logo, tiêu đề, style) → nạp **toàn bộ** vào heap cũng nhẹ |
| (3) | `resolveFields` | Dùng **reflection**: đọc các field có `@ExcelColumn` của `SwiftExportRow`, xếp theo `EXPORT_COLUMNS`. Sai tên field → lỗi ngay (lỗi cấu hình) |
| (4) | Lấy style mẫu ở dòng 9 | Để mọi dòng dữ liệu dùng chung một bộ style (viền, font…) |
| (5) | `autoSizeColumn` | Làm **trước** khi ghi dữ liệu, vì SXSSF đã đẩy dòng xuống đĩa thì không đo lại được. Hiện chỉ đo theo tiêu đề và header |
| (6) | Xóa các dòng mẫu từ dòng 9 trở xuống | SXSSF **không cho ghi** vào dòng đã tồn tại trong phần XSSF gốc → phải dọn chỗ trước |
| (7) | `new SXSSFWorkbook(template, 200)` | **Chế độ ghi luồng (streaming)**: chỉ giữ **200 dòng gần nhất** trên heap; dòng thứ 201 được ghi thì dòng cũ nhất bị **đẩy xuống file XML tạm trên đĩa** (`{tmpdir}/poifiles/poi-sxssf-sheet-*.xml`) và xóa khỏi RAM |
| (8) | `writeData` ghi từng dòng | Mỗi `SwiftExportRow` → 1 dòng Excel; `writeCell` chọn kiểu ô theo kiểu dữ liệu (chuỗi, ngày giờ, ngày, số tiền `BigDecimal`, số) |
| (9) | `wb.write(out)` | Ghép các phần (template + XML tạm các dòng) thành file `.xlsx` (thực chất là **file zip** chứa nhiều XML), nén và ghi ra `out` = file tạm ở (1) |
| (10) | `wb.dispose()` | Xóa các file XML tạm của POI trên đĩa |

**Vì sao SXSSF quan trọng**: với XSSF thường, 50.000 dòng × 20 cột = 1 triệu object ô nằm trên heap. Với SXSSF, trên heap chỉ có 200 dòng; phần còn lại nằm trên đĩa. Lưu ý là `List<SwiftExportRow> rows` (toàn bộ dữ liệu) **vẫn nằm trọn trên heap** trong suốt quá trình ghi, vì nó được truyền vào đầy đủ.

**`resolveColumnStyles`**: tạo style theo định dạng (`dd/MM/yyyy HH:mm:ss`, `dd/MM/yyyy`, `#,##0.00`) **một lần cho cả workbook** rồi dùng chung. Tạo style trong vòng lặp từng ô sẽ vượt giới hạn 64.000 style của Excel và làm hỏng file.

## B14. `TempFileUtil.java`

```java
public static Path create(String suffix) {
    try { return Files.createTempFile("pmh-export-", suffix); }
    catch (IOException e) { throw new BusinessException(ResponseCode.EXP004, suffix, e); }   // đĩa đầy / không có quyền
}
public static void deleteQuietly(Path file) {
    if (file == null) return;
    try { Files.deleteIfExists(file); }
    catch (IOException e) { log.warn("Khong xoa duoc file tam {}", file, e); }              // NUỐT lỗi
}
public static long sizeQuietly(Path file) { ... }   // chỉ dùng để ghi log kích thước
```
- **`deleteQuietly` nuốt lỗi**: hàm này thường được gọi **khi đang có một lỗi khác** (trong `catch`). Nếu việc xóa cũng ném lỗi thì lỗi mới sẽ **che mất nguyên nhân thật**. Không xóa được thì chỉ ghi log cảnh báo.
- `deleteIfExists`: file không còn thì thôi, không coi là lỗi.

## B15. `FileResponseUtil.java`

```java
public static ResponseEntity<Resource> buildFileResponse(Resource file, String fileName,
                                                         MediaType contentType, long contentLength) {
    ContentDisposition disposition = ContentDisposition.attachment()
            .filename(fileName, StandardCharsets.UTF_8)
            .build();
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .contentType(contentType)
            .contentLength(contentLength)
            .body(file);
}
```

| Header | Giá trị ví dụ | Tác dụng |
|---|---|---|
| `Content-Disposition` | `attachment; filename="=?UTF-8?Q?...?="; filename*=UTF-8''DanhSachDienSwiftDen_28092026_101530.xlsx` | `attachment` = bảo trình duyệt đây là file để tải. `filename*=UTF-8''…` giữ được **tên có dấu tiếng Việt** |
| `Access-Control-Expose-Headers` | `Content-Disposition` | Mặc định, khi FE gọi khác nguồn (CORS), JS **không đọc được** header này. Dòng này cho phép FE đọc để lấy tên file |
| `Content-Type` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | Kiểu file xlsx |
| `Content-Length` | số byte | Trình duyệt biết trước dung lượng, hiện được tiến độ |

Body là `Resource` → Spring **stream** từ đĩa (B10.6).

---

# Phần C. Giải thích code FE

## C1. `models/export-task/index.ts`

```ts
export type ExportTaskStatus = 'PROCESSING' | 'SUCCESS' | 'FAILED';

export interface ExportTask {
  taskId: string;
  status: ExportTaskStatus;
  fileName?: string;      // có khi SUCCESS
  errorCode?: string;     // có khi FAILED
  errorMessage?: string;
}
```
Khớp 1–1 với `ExportTaskResDTO` (B6). Dấu `?` vì BE bỏ field null (`NON_NULL`).

## C2. `utils/api.ts`

```ts
EXPORT_INIT:     UrlConstant.SWIFT_SERVICE.concat(UrlConstant.SERVICE_VERSION).concat('/swift-msg/in/export/init'),
EXPORT_STATUS:   UrlConstant.SWIFT_SERVICE.concat(UrlConstant.SERVICE_VERSION).concat('/swift-msg/export-status'),
EXPORT_DOWNLOAD: UrlConstant.SWIFT_SERVICE.concat(UrlConstant.SERVICE_VERSION).concat('/swift-msg/export-download'),
```
`EXPORT_INIT` có `/in` vì là việc riêng của **điện đến**. `STATUS` và `DOWNLOAD` không có `/in` vì chỉ cần `taskId` và dùng chung cho mọi loại xuất.

## C3. `swift-message-list.service.ts`

```ts
initExportIn(params: SwiftMessageSearchForm) {
  const url = UrlConstant.NX_ENDPOINT_URL.concat(UrlConstant.SWIFT_MESSAGE.EXPORT_INIT);
  return this.#httpClient.post<any>(url, this.buildRequest(params));          // body giống hệt API tìm kiếm
}

getExportStatus(taskId: string) {
  const url = UrlConstant.NX_ENDPOINT_URL.concat(UrlConstant.SWIFT_MESSAGE.EXPORT_STATUS);
  return this.#httpClient.get<any>(url, { params: { taskId } });              // → ?taskId=...
}

downloadExport(taskId: string) {
  const url = UrlConstant.NX_ENDPOINT_URL.concat(UrlConstant.SWIFT_MESSAGE.EXPORT_DOWNLOAD);
  return this.#httpClient.get(url, {
    params: { taskId },
    responseType: 'blob',       // body là dữ liệu nhị phân, không parse JSON
    observe: 'response',        // lấy cả header (cần Content-Disposition để có tên file)
  });
}

readBlobError(err: any): Promise<string | null> {
  return readBlobErrorMessage(err);
}
```
- `buildRequest(params)`: dựng body từ form, **dùng chung với tìm kiếm**, nên file xuất ra đúng với những gì đang lọc trên màn.
- `HttpClient` trả `Observable`, là **"lời hứa sẽ gọi"**: request chỉ thật sự được gửi khi có người `subscribe`.

## C4. `utils/common.ts` — `readBlobErrorMessage`

```ts
export async function readBlobErrorMessage(err: any): Promise<string | null> {
  if (!(err?.error instanceof Blob)) {
    return err?.error?.tranDesc || err?.error?.message || null;
  }
  try {
    const body = JSON.parse(await err.error.text());
    return body?.tranDesc || body?.message || null;
  } catch {
    return null;
  }
}
```
**Để làm gì**: API download khai báo `responseType: 'blob'`, nên khi BE trả lỗi (JSON 400 từ handler B8), Angular **vẫn gói body lỗi thành Blob**. `err.error` lúc này là Blob chứ không phải object. Phải đọc Blob thành chữ (`.text()`, bất đồng bộ nên cần `await`) rồi `JSON.parse` mới lấy được `tranDesc`.

## C5. `swift-message-list.component.ts`

### Hằng số và biến

```ts
import { exhaustMap, finalize, map, takeWhile, timer } from 'rxjs';
import { saveAs } from 'file-saver';
const EXPORT_POLL_MS = 3_000;                              // hỏi status mỗi 3 giây

protected exportStatus: ExportTaskStatus | null = null;    // null = không có popup
protected exportFileName = '';                             // tên file hiện trên popup
protected exportError = '';                                // message lỗi hiện trên popup
private exportTaskId: string | null = null;                // SỐ PHIẾU — chìa khóa cho status/download
```
Các biến này nằm trong **RAM của tab trình duyệt**, gắn với object component. Rời màn (component bị hủy) hoặc F5 là mất.

`exportStatus` điều khiển toàn bộ giao diện (C6):

| `exportStatus` | Nút "Xuất file" | Popup |
|---|---|---|
| `null` | Bấm được, chữ "Xuất file" | Ẩn |
| `'PROCESSING'` | **Disabled**, chữ "Đang xuất file..." | Thanh chạy + spinner, "Đang xuất file — Vui lòng chờ trong giây lát..." |
| `'SUCCESS'` | Bấm được | Dấu tick, "Xuất file thành công", tên file, nút **Tải về** |
| `'FAILED'` | Bấm được | Dấu chấm than, "Xuất file thất bại", message lỗi |

### `exportFile()` — B1: đặt hàng

```ts
public exportFile(): void {
  checkValidForm(this.formGroup);                         // (1) đánh dấu các ô lỗi
  if (this.formGroup.invalid) {
    handleFormGroupErrors(this.formGroup, this.alertService);
    return;
  }
  if (this.exportStatus === 'PROCESSING') {
    return;                                               // (2) đang xuất rồi, chặn bấm lần 2
  }

  this.isLoading = true;                                  // (3) overlay chỉ trong lúc đặt hàng, vài ms
  this.#swiftMessageService
    .initExportIn({ ...this.formGroup.getRawValue(), pagination: this.pagination })   // (4)
    .pipe(
      takeUntilDestroyed(this.destroyRef),                // (5)
      finalize(() => (this.isLoading = false))            // (6)
    )
    .subscribe({
      next: (res) => this.onTaskAccepted(res?.data),      // (7)
      error: (e) => this.handleJsonError(e),              // (8)
    });
}
```
1. Validate ở FE trước để khỏi gọi BE vô ích.
2. Lớp chặn thứ 2 (lớp 1 là `[disabled]` trên nút): phòng trường hợp nút chưa kịp disable mà bấm nhanh 2 lần.
3. Overlay chỉ hiện **vài ms** trong lúc chờ BE trả `taskId`, không phải suốt thời gian dựng file.
4. `getRawValue()` lấy cả giá trị các ô đang disabled. Body giống tìm kiếm.
5. Rời màn trước khi BE trả lời thì bỏ luôn kết quả.
6. `finalize` chạy khi Observable kết thúc, dù thành công hay lỗi → luôn tắt overlay.
7. `res.data` = `{taskId, status: 'PROCESSING'}`.
8. Lỗi validate BE (`VAL0xx`, `ACC0xx`) hoặc `EXP003` → popup đỏ.

**Bên dưới trình duyệt**: `subscribe` **không chặn**. JS gửi request, đăng ký "có kết quả thì gọi `next`", rồi chạy tiếp ngay. Người dùng vẫn thao tác được.

### `onTaskAccepted()` — nhận phiếu

```ts
private onTaskAccepted(task?: ExportTask): void {
  if (!task?.taskId) {
    this.showExportError('Có lỗi xảy ra, vui lòng thử lại.');   // response lạ
    return;
  }
  this.exportTaskId = task.taskId;     // cất số phiếu
  this.exportFileName = '';            // xóa dấu vết lần xuất trước
  this.exportError = '';
  this.exportStatus = 'PROCESSING';    // popup hiện, nút disabled
  this.pollExport(task.taskId);        // bắt đầu hỏi
}
```

### `pollExport()` — B2: hỏi định kỳ

```ts
private pollExport(taskId: string): void {
  timer(0, EXPORT_POLL_MS)                                                // (1)
    .pipe(
      exhaustMap(() => this.#swiftMessageService.getExportStatus(taskId)), // (2)
      map((res) => res?.data as ExportTask),                              // (3)
      takeWhile((task) => task?.status === 'PROCESSING', true),           // (4)
      takeUntilDestroyed(this.destroyRef)                                 // (5)
    )
    .subscribe({
      next: (task) => {
        if (task?.status !== 'PROCESSING') {
          this.onTaskFinished(task);                                      // (6)
        }
      },
      error: (e) => this.handleJsonError(e),                              // (7)
    });
}
```

**Bên dưới trình duyệt**: JS chạy trên **một luồng duy nhất** kèm **vòng lặp sự kiện** (event loop). `timer` không phải vòng `while` chặn giao diện; nó nhờ trình duyệt "3 giây nữa nhắc tôi". Giữa các lần nhắc, luồng JS rảnh để xử lý thao tác khác nên màn hình không đơ.

| # | Toán tử | Tác dụng | Nếu thiếu |
|---|---|---|---|
| (1) | `timer(0, 3000)` | Phát tín hiệu 0 **ngay lập tức**, rồi 1, 2, 3… mỗi 3 giây | Dùng `interval(3000)` thì phải đợi 3 giây mới hỏi lần đầu |
| (2) | `exhaustMap` | Mỗi tín hiệu → gọi API status. Nếu **lần gọi trước chưa về** thì **bỏ qua** tín hiệu mới | `mergeMap`: mạng chậm là request chồng request. `switchMap`: hủy request cũ và có thể không bao giờ nhận được kết quả nếu mạng luôn chậm hơn 3 giây |
| (3) | `map` | Lấy `data` ra khỏi `BaseResponseParam` | — |
| (4) | `takeWhile(cond, true)` | Còn `PROCESSING` thì cho đi tiếp; gặp giá trị khác thì **kết thúc** Observable (timer dừng). `true` = **vẫn cho giá trị cuối** (SUCCESS/FAILED) đi xuống `next` | Không có `takeWhile`: hỏi mãi. Thiếu `true`: dừng mà `next` không nhận được SUCCESS, popup kẹt "Đang xuất" |
| (5) | `takeUntilDestroyed` | Component bị hủy (rời màn) → hủy đăng ký → timer ngừng | Rời màn mà vẫn gọi API ngầm mỗi 3 giây (rò rỉ) |
| (6) | `next` | Chỉ xử lý khi đã hết `PROCESSING` | — |
| (7) | `error` | API status lỗi HTTP (ví dụ `EXP001` do service restart) → popup đỏ; Observable tự dừng | — |

Chuỗi tín hiệu ví dụ:
```
timer:     0 ─────── 1 ─────── 2 ─────── 3
API:       └▶PROC    └▶PROC    └▶PROC    └▶SUCCESS
takeWhile: cho qua   cho qua   cho qua   cho qua (true) rồi KẾT THÚC → timer dừng
next:      bỏ qua    bỏ qua    bỏ qua    onTaskFinished(SUCCESS)
```

### `onTaskFinished()`

```ts
private onTaskFinished(task?: ExportTask): void {
  if (task?.status === 'SUCCESS') {
    this.exportFileName = task.fileName ?? '';
    this.exportStatus = 'SUCCESS';                          // popup đổi sang "thành công" + nút Tải về
  } else {
    // FAILED hoặc response lạ -> luôn thoát trạng thái PROCESSING
    this.showExportError(task?.errorMessage ?? 'Có lỗi xảy ra, vui lòng thử lại.');
  }
}
```
Không tự tải file: người dùng tự bấm "Tải về". Lý do là trình duyệt thường **chặn tự tải** nếu việc tải không xuất phát trực tiếp từ một cú bấm.

### `onDownload()` — B3: tải file

```ts
protected onDownload(): void {
  if (!this.exportTaskId) return;
  this.#swiftMessageService
    .downloadExport(this.exportTaskId)
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: (res) => saveAs(res.body as Blob, this.resolveExportFileName(res)),
      error: async (e) => {
        const message = await this.#swiftMessageService.readBlobError(e);   // body lỗi là Blob (C4)
        this.alertService.showErrorAlert('Thông báo', message || 'Có lỗi xảy ra, vui lòng thử lại.');
      },
    });
}
```
**Bên dưới trình duyệt**:
- Byte file về tới nơi được gom thành một **Blob** (khối nhị phân trong RAM của tab).
- `saveAs` (thư viện `file-saver`) tạo một URL tạm `blob:…` trỏ tới Blob, tạo thẻ `<a download="tên">` rồi tự "bấm". Trình duyệt lưu file vào thư mục Downloads, sau đó URL tạm được giải phóng.
- Không đổi `exportStatus` → popup vẫn ở "thành công", **bấm Tải về lại được** (BE còn giữ file 30 phút).

### `closeExportPanel()`, `showExportError()`, `handleJsonError()`

```ts
protected closeExportPanel(): void {           // nút X trên popup
  this.exportStatus = null;                    // ẩn popup, mở lại nút Xuất file
  this.exportFileName = '';
  this.exportError = '';
}

private showExportError(message: string): void {
  this.exportError = message;
  this.exportStatus = 'FAILED';
}

/** Lỗi của API trả JSON (init, status) - err.error đã là object, đọc thẳng */
private handleJsonError(e: any): void {
  this.showExportError(e?.error?.tranDesc ?? e?.error?.message ?? 'Có lỗi xảy ra, vui lòng thử lại.');
}
```
- Bấm X khi đang `PROCESSING`: popup ẩn **nhưng polling vẫn chạy**, khi xong `onTaskFinished` sẽ đặt `exportStatus` và popup hiện lại. Phía BE không biết gì về cú bấm X, vẫn dựng file.
- Có **2 hàm đọc lỗi** vì 2 loại API: init/status trả JSON (`err.error` đã là object) → `handleJsonError`; download trả blob (`err.error` là Blob) → `readBlobError`.

### `resolveExportFileName()`, `fileTimestamp()`

```ts
private resolveExportFileName(res: HttpResponse<Blob>): string {
  const cd = res.headers.get('content-disposition') ?? '';
  const encoded = cd.match(/filename\*=UTF-8''([^;]+)/i);            // 1. tên UTF-8 (có dấu)
  if (encoded) return decodeURIComponent(encoded[1]);
  const plain = cd.match(/filename="?([^";]+)"?/i);                   // 2. tên thường
  return plain?.[1] ?? `DanhSachDienSwiftDen_${this.fileTimestamp()}.xlsx`;   // 3. tự dựng
}
```
Ưu tiên tên BE đặt. Đọc được header này là nhờ BE có `Access-Control-Expose-Headers` (B15). Nếu thiếu header, FE tự dựng tên cùng mẫu với BE (`ddMMyyyy_HHmmss`).

## C6. HTML + SCSS

**Nút "Xuất file"**:
```html
<button bidvButton (click)="exportFile()" [disabled]="exportStatus === 'PROCESSING'">
  {{ exportStatus === 'PROCESSING' ? 'Đang xuất file...' : 'Xuất file' }}
</button>
```

**Popup** (góc phải dưới):
```html
<!-- Nằm NGOÀI bidv-loader: để trong thì bị overlay làm mờ theo.
     Không tự đóng - popup biến mất là mất đường tải file. -->
<div *ngIf="exportStatus" class="export-popup"
     [class.export-popup--done]="exportStatus === 'SUCCESS'"
     [class.export-popup--failed]="exportStatus === 'FAILED'" role="status">
  <div *ngIf="exportStatus === 'PROCESSING'" class="export-popup__progress"></div>   <!-- thanh chạy -->
  <button (click)="closeExportPanel()" class="export-popup__close">X</button>
  ... spinner / tick / chấm than theo exportStatus ...
  <div [ngSwitch]="exportStatus">
    <ng-container *ngSwitchCase="'PROCESSING'"> Đang xuất file / Vui lòng chờ... </ng-container>
    <ng-container *ngSwitchCase="'SUCCESS'"> Xuất file thành công / {{ exportFileName }} / [Tải về] </ng-container>
    <ng-container *ngSwitchCase="'FAILED'"> Xuất file thất bại / {{ exportError }} </ng-container>
  </div>
</div>
```
- `*ngIf="exportStatus"`: `null` thì popup **không tồn tại** trong DOM.
- Popup nằm **ngoài** `bidv-loader`: overlay của các thao tác khác (tìm kiếm…) không làm mờ popup.
- **Không tự đóng**: nếu popup tự biến mất, người dùng mất nút "Tải về", tức mất đường lấy file (FE không có chỗ nào khác giữ `taskId`).
- `role="status"`: trình đọc màn hình sẽ đọc thông báo khi nội dung đổi.
- SCSS (`.export-popup`): animation trượt vào (`export-popup-in`), thanh chạy (`export-popup-progress`), spinner xoay (`export-popup-spin`); đổi màu theo `--done` / `--failed`.

---

# Phần D. Chạy một lượt từ đầu đến cuối

Ví dụ: người dùng `hieunm` xuất khoảng 20.000 điện, bộ đôi luồng nền đang rảnh.

```
 t        TRÌNH DUYỆT                        LUỒNG TOMCAT                         LUỒNG NỀN swift-export-1                HEAP BE                                  ĐĨA ({tmpdir})
 ───────  ─────────────────────────────────  ───────────────────────────────────  ──────────────────────────────────────  ───────────────────────────────────────  ─────────────────────────────────
 0ms      bấm "Xuất file"
          validate form OK
          isLoading = true
          POST /in/export/init ────────────▶ [exec-3] đọc JSON                                                             + SwiftSearchRequest
                                             validateExportIn: OK
                                             tạo lambda work ─────────────────────────────────────────────────────────────▶ + Supplier(work) chụp request
                                             submit():
                                              sweepExpired (không có gì)
                                              currentUser() = "hieunm"  (ThreadLocal)
                                              new ExportTask ──────────────────────────────────────────────────────────────▶ + ExportTask{a1b2, hieunm, PROCESSING}
                                              tasks.put ───────────────────────────────────────────────────────────────────▶ Map["a1b2"] → phiếu
                                              execute(): chưa có luồng → Thread.start() ──▶ OS tạo luồng swift-export-1
                                             trả {a1b2, PROCESSING}, về pool
 ~5ms     nhận taskId                                                               run(): work.get()
          isLoading = false                                                         buildExportInFile():
          exportStatus = PROCESSING                                                  loadExportInRows:
          popup "Đang xuất file"                                                      mượn kết nối DB → gọi EXPORT_SWIFT_IN
          timer(0): GET status ────────────▶ [exec-7] Map.get("a1b2")                 ...chờ Oracle (không tốn CPU)...
                                             owner == hieunm ✓ → PROCESSING
          ◀── PROCESSING
 3s       GET status ──────────────────────▶ [exec-1] → PROCESSING                    Oracle trả cursor, đọc 20.000 dòng ──▶ + List<Map> 20.000 phần tử
 4s                                                                                   map mã→tên, STT ─────────────────────▶ + List<SwiftExportRow> 20.000
                                                                                                                              − List<Map> (hết tham chiếu, chờ GC)
                                                                                     exportToTempFile:
                                                                                      createTempFile ───────────────────────────────────────────────────────────────────▶ + pmh-export-8812.xlsx (rỗng)
                                                                                      nạp template (từ jar) ───────────────▶ + XSSFWorkbook template (nhỏ)
                                                                                      SXSSF ghi dòng, giữ 200 dòng ────────▶ ≤ 200 dòng Excel                        + poifiles/poi-sxssf-sheet-*.xml (lớn dần)
 6s       GET status ──────────────────────▶ [exec-4] → PROCESSING
                                                                                      wb.write → nén zip ─────────────────────────────────────────────────────────────────▶ pmh-export-8812.xlsx (đầy đủ)
                                                                                      wb.dispose ─────────────────────────────────────────────────────────────────────────▶ − poifiles/*.xml
                                                                                      close file
                                                                                     return ExportedFile ──────────────────▶ − rows, workbook (chờ GC)
                                                                                    store(): createDirectories ───────────────────────────────────────────────────────────▶ + thư mục pmh-export/
                                                                                     Files.move (rename) ─────────────────────────────────────────────────────────────────▶ pmh-export/a1b2.export
                                                                                    set file, fileName, contentType
                                                                                    set status = SUCCESS (volatile) ──────▶ phiếu{SUCCESS, file, fileName}
                                                                                    finally: finishedAt = 7.2s
                                                                                    về vòng lặp chờ việc (ngủ)
 9s       GET status ──────────────────────▶ [exec-2] → SUCCESS + fileName
          takeWhile dừng timer
          exportStatus = SUCCESS
          popup "Xuất file thành công" + [Tải về]
 25s      bấm "Tải về"
          GET export-download ─────────────▶ [exec-6] require OK, SUCCESS
                                             isReadable, size = 1.8MB
                                             trả FileSystemResource
                                             Spring stream từng khúc ◀────────────────────────────────────────────────────────────────────────────────────────────────── đọc a1b2.export
          ◀── byte file (Content-Disposition)
          gom Blob → saveAs → Downloads/DanhSachDienSwiftDen_28092026_101507.xlsx
 67s                                                                                getTask() hết 60s không có việc → luồng kết thúc
 7.2s+30' (ai đó bấm xuất mới) → submit → sweepExpired ──────────────────────────────────────────────────────────────────────▶ − phiếu a1b2 (chờ GC)                  − pmh-export/a1b2.export
```

Điểm cần nhìn ra:
- **5 request HTTP** (1 init + 3 status + 1 download) chạy trên **5 luồng Tomcat khác nhau**, mỗi luồng chỉ bận vài ms (riêng download bận theo thời gian truyền file). Chúng "gặp nhau" nhờ **cùng một Map trên heap**.
- Việc nặng (DB + Excel) chỉ chạy trên **luồng nền**, không đụng luồng Tomcat.
- RAM tăng mạnh nhất ở khoảng 3–6s (danh sách dòng) rồi giảm về. Sau đó RAM chỉ còn giữ **1 phiếu nhỏ**; dữ liệu thật nằm trên đĩa.

---

# Phần E. Vòng đời

## E1. Phiếu `ExportTask` (heap BE)

```
                   submit()                             run() xong không lỗi
   (chưa có) ─────────────────▶ PROCESSING ─────────────────────────────────────▶ SUCCESS ─┐
        │                          │                                                        │ sweepExpired():
        │ hàng chờ đầy             │ run() gặp exception                                    │ finishedAt + 30' < now
        │ → remove, EXP003         └─────────────────────────────────────────────▶ FAILED ──┤
        ▼                                                                                   ▼
   (không tồn tại)          PROCESSING mà createdAt + 60' < now ───────────────────▶ gỡ khỏi Map + xóa file ──▶ GC thu hồi
                            Tắt service (@PreDestroy) ─────────────────────────────▶ xóa HẾT
                            Tiến trình chết đột ngột ─────────────────────────────▶ heap mất sạch (file còn lại trên đĩa)
```

## E2. File trên đĩa

```
 {tmpdir}/pmh-export-8812.xlsx      ← TempFileUtil.create + ghi Excel
          │  lỗi → deleteQuietly
          │  xong → Files.move (rename, cùng phân vùng)
          ▼
 {tmpdir}/pmh-export/a1b2.export    ← được tải nhiều lần
          │  sweepExpired (30' sau khi xong, lúc có yêu cầu mới) / @PreDestroy
          ▼
        (xóa)

 {tmpdir}/poifiles/poi-sxssf-*.xml  ← chỉ tồn tại trong lúc SXSSF ghi, wb.dispose() xóa
```

## E3. Luồng nền

```
 (chưa có) ──có việc, < 2 luồng──▶ chạy run() ──xong──▶ chờ việc (ngủ) ──có việc──▶ chạy run() ...
                                                           │
                                                           └─ 60s không có việc ──▶ kết thúc
 Tắt service ──▶ shutdownNow() ngắt
```

## E4. Trạng thái FE

```
 null ──bấm Xuất file, init OK──▶ PROCESSING ──status SUCCESS──▶ SUCCESS ──bấm X──▶ null
  │                                  │   ▲                         │ bấm Tải về: giữ nguyên, tải lại được
  │                                  │   └ poll mỗi 3s             │
  │                                  └──status FAILED / lỗi HTTP──▶ FAILED ──bấm X──▶ null
  └──init lỗi (VAL/ACC/EXP003)───────────────────────────────────▶ FAILED
 Rời màn / F5 bất cứ lúc nào ──▶ mất hết (exportTaskId, polling dừng)
```

## E5. Bảng tổng hợp: cái gì nằm ở đâu, sống bao lâu

| Thứ | Nằm ở | Sinh ra | Chết |
|---|---|---|---|
| `ExportTaskServiceImpl`, `tasks`, `executor`, hàng chờ | Heap BE | Khởi động service | Tắt service |
| `SwiftSearchRequest` (bộ lọc) | Heap BE | Request init | Luồng nền chạy xong (lambda hết ai giữ) |
| Lambda `work` / `Runnable` | Heap BE (trong hàng chờ, rồi được luồng nền giữ) | `init` / `submit` | `run()` xong |
| `ExportTask` (phiếu) | Heap BE, trong Map | `submit` | Bị sweep gỡ khỏi Map rồi GC; hoặc tắt service |
| User lúc gọi | ThreadLocal luồng Tomcat → chép vào `task.owner` | Mỗi request | Hết request (bản chép sống theo phiếu) |
| `List<Map>` dòng DB, `List<SwiftExportRow>` | Heap BE | Trong `work.get()` | Ngay sau khi ghi xong file |
| ≤ 200 dòng Excel của SXSSF | Heap BE | Lúc ghi | `wb.close()` |
| `poifiles/*.xml` | Đĩa | Lúc SXSSF ghi | `wb.dispose()` |
| `pmh-export-xxx.xlsx` | Đĩa | `createTempFile` | Bị `move` thành file dưới |
| `pmh-export/<taskId>.export` | Đĩa | `store()` | Sweep (30' sau khi xong) / `@PreDestroy` |
| Luồng `swift-export-N` | OS | Có việc mà chưa đủ 2 luồng | Rảnh 60s / tắt service |
| Luồng Tomcat | OS, pool của Tomcat | Tomcat quản lý | Mỗi request chỉ **mượn** |
| `exportTaskId`, `exportStatus`… | RAM tab trình duyệt | Nhận phiếu | Rời màn / F5 / bấm X (trừ `exportTaskId`) |
| Subscription polling | RAM tab | `pollExport` | Hết PROCESSING / lỗi / rời màn |
| Blob file | RAM tab | Tải về | Sau khi `saveAs` lưu xong |

---

# Phần F. Tình huống và mã lỗi

| Tình huống | Chuyện gì xảy ra | Người dùng thấy |
|---|---|---|
| Form FE sai | `exportFile` dừng trước khi gọi BE | Alert lỗi form |
| Bộ lọc sai theo BE (ngày, số tiền) | `validateExportIn` ném trên luồng Tomcat → 400 | Popup đỏ, message `VAL0xx` / `ACC0xx` |
| Bấm "Xuất file" 2 lần liên tục | Nút disabled + `if PROCESSING return` | Không có gì xảy ra |
| 23 người bấm cùng lúc | 2 chạy, 20 chờ, người thứ 23 → `RejectedExecutionException` → `EXP003` | "Hệ thống đang bận xuất file, vui lòng thử lại sau" |
| Procedure trả lỗi | `loadExportInRows` ném → `run()` bắt → FAILED | Popup đỏ ở lần hỏi status kế tiếp |
| Lỗi bất ngờ (NullPointer, SQL…) | `run()` bắt `Exception` → `TEC999` | Popup đỏ, message của TEC999 |
| Đĩa đầy / không có quyền ghi | `createTempFile` hoặc `move` lỗi → `EXP004` | "Lưu file kết quả không thành công (…)" |
| Mạng chậm, 1 lần hỏi status > 3s | `exhaustMap` bỏ qua các nhịp trong lúc chờ | Không ảnh hưởng |
| Rời màn khi đang PROCESSING | Polling dừng; BE vẫn dựng xong, file nằm chờ bị dọn | Quay lại màn không thấy popup, phải xuất lại |
| F5 | Như rời màn | Như trên |
| Bấm X khi đang PROCESSING | Popup ẩn, polling vẫn chạy → xong thì popup hiện lại | Popup hiện lại khi xong |
| Người khác cầm taskId của mình gọi status/download | `require` so owner → `EXP001` | "Yêu cầu xuất file không tồn tại hoặc đã hết hạn" |
| Tải lại sau hơn 30 phút | Nếu đã có lượt xuất mới kích sweep → phiếu mất → `EXP001`; nếu chưa → vẫn tải được | Tùy |
| Service restart khi đang xuất | `@PreDestroy` dọn hết; lần hỏi status sau → `EXP001` | Popup đỏ "không tồn tại hoặc đã hết hạn" |
| Gọi download khi còn PROCESSING (gọi tay API) | `EXP002` | "File đang được tạo, vui lòng chờ" |

---

# Phần G. Giới hạn và điểm cần lưu ý

1. **Phiếu chỉ sống trong heap của một tiến trình.**
   - Restart service là mất phiếu.
   - Chạy **nhiều bản** service song song (nhiều pod sau load balancer): mỗi bản có Map riêng và `/tmp` riêng. `init` vào bản A mà `status` rơi vào bản B thì B không có phiếu → `EXP001`.
   - Cách xử lý nếu cần: bật sticky session, hoặc chuyển phiếu sang Redis / bảng DB và file sang kho dùng chung.
2. **File nằm ở thư mục tạm của OS** (`/tmp/pmh-export` trong container, xem A3): chung đĩa với hệ thống, pod tạo lại là mất. Muốn tách phân vùng thì cấu hình `export-task.dir` trỏ vào volume riêng.
3. **Dọn rác chỉ chạy khi có yêu cầu xuất mới** (`sweepExpired` trong `submit`). Không ai xuất nữa thì file cũ nằm tới lần xuất sau hoặc tới khi restart. Muốn dọn đúng giờ thì thêm một `@Scheduled` gọi `sweepExpired()`.
4. **RAM lúc dựng vẫn tỉ lệ với số dòng**: SXSSF chỉ giữ 200 dòng Excel, nhưng `List<SwiftExportRow>` (toàn bộ dữ liệu) và `List<Map>` từ procedure vẫn nằm trọn trên heap. Giới hạn `pool-size: 2` đảm bảo tối đa 2 khối như vậy tồn tại cùng lúc.
5. **Code trong lambda chạy ở luồng nền**: không có user đăng nhập (ThreadLocal trống), không có `HttpServletRequest`, không có `auditorAware`. Cái gì cần từ request phải lấy trước ở luồng Tomcat rồi truyền vào lambda.
6. **`owner == null` thì bỏ qua kiểm tra chủ**: nếu lúc `init` không lấy được user, ai có taskId cũng xem hoặc tải được.
7. **Không có API hủy**: bấm X chỉ ẩn popup, BE vẫn dựng file tới xong.
8. **FE không nhớ phiếu qua F5 / rời màn** (biến component). Muốn giữ thì cất `exportTaskId` vào `sessionStorage` và khôi phục polling khi vào lại màn.
9. **`autoSizeColumn` chỉ đo theo tiêu đề và header** (đo trước khi ghi dữ liệu), nên độ rộng cột không theo dữ liệu thật.
10. Endpoint đồng bộ cũ `/swift-msg/in/export` và `ExcelExportUtil.export(...)` (trả `byte[]`) vẫn còn. Comment ghi "giữ tạm đến khi FE chuyển xong thì xóa".
