# Oracle Index — Tất Tần Tật

## 1. Index là gì, tại sao cần?

Index (chỉ mục) là structure (cấu trúc dữ liệu) phụ, trỏ đến vị trí row trong table, giúp optimizer (bộ tối ưu truy vấn) khỏi phải Full Table Scan (quét toàn bộ bảng) mà nhảy thẳng đến data cần tìm.

Trade-off: đọc nhanh hơn nhưng ghi (INSERT/UPDATE/DELETE) chậm hơn vì mỗi lần DML phải update luôn index. Đây là lý do vì sao **không phải cứ đánh index bừa bãi là ngon** — vi phạm nguyên tắc performance nếu lạm dụng.

---

## 2. Các loại Index chính

### B-tree Index (default)
- Cấu trúc cây cân bằng (balanced tree), branch node dẫn tới leaf node chứa rowid.
- Ngon cho: cột có **high selectivity** (nhiều giá trị phân biệt), dùng trong `WHERE`, `JOIN`, `ORDER BY`.
- Đây là loại mặc định khi bạn `CREATE INDEX` không chỉ định gì.

```sql
CREATE INDEX idx_emp_lastname ON employees(last_name);
```

### Bitmap Index
- Dùng bitmap (chuỗi bit 0/1) đại diện cho mỗi giá trị distinct.
- Ngon cho: cột **low cardinality** (ít giá trị, VD: giới tính, trạng thái, loại sản phẩm), môi trường **OLAP/reporting**, ít DML.
- **Cực kỵ dùng cho OLTP** — vì lock ở mức bitmap segment, DML nhiều là dễ deadlock, degrade performance nặng.

```sql
CREATE BITMAP INDEX idx_status ON orders(status);
```

### Composite (Concatenated) Index
- Gộp nhiều cột vào 1 index.
- Nguyên tắc **column order** quan trọng nhất: cột hay filter/equality trước, cột range/order sau. Đặt theo thứ tự selectivity giảm dần hoặc theo pattern query thực tế.
- Index chỉ dùng được hiệu quả nếu query filter theo **leading column** (cột đầu tiên) — leftmost prefix rule.

```sql
CREATE INDEX idx_ord_cust_date ON orders(customer_id, order_date);
```

### Function-Based Index (FBI)
- Index trên biểu thức/hàm thay vì cột thô.
- Dùng khi query hay wrap hàm quanh cột (`UPPER()`, `TRUNC()`, tính toán...) — không có FBI thì optimizer bó tay, phải Full Scan dù có index thường trên cột đó.

```sql
CREATE INDEX idx_upper_email ON users(UPPER(email));
```

### Unique vs Non-Unique Index
- `UNIQUE INDEX`: đảm bảo giá trị không trùng, thường tự sinh khi có `PRIMARY KEY`/`UNIQUE constraint`.
- Non-unique: index thường, cho phép trùng giá trị.

### Reverse Key Index
- Đảo ngược byte của giá trị trước khi lập chỉ mục.
- Dùng để giải quyết **hot block contention** khi insert giá trị tăng dần liên tục (VD: sequence) — tránh mọi session cùng ghi vào 1 leaf block cuối cây.
- Đánh đổi: mất khả năng dùng cho range scan (`BETWEEN`, `>`, `<`).

### Index-Organized Table (IOT)
- Table mà data được lưu trực tiếp trong cấu trúc B-tree của primary key, không có heap table riêng.
- Ngon cho bảng lookup nhỏ, truy vấn chủ yếu theo PK, tiết kiệm I/O vì không cần join thêm.

### Partitioned Index
- **Local**: mỗi partition của table có index riêng tương ứng — dễ maintain, gắn liền partition table.
- **Global**: 1 index bao trùm toàn bộ các partition — tốt cho query không theo partition key, nhưng maintain cực hơn (partition table bị drop/split phải rebuild global index).

### Invisible Index
- Index vẫn tồn tại, vẫn được maintain khi DML, nhưng optimizer **không thấy** để dùng — trừ khi set `OPTIMIZER_USE_INVISIBLE_INDEXES=TRUE`.
- Dùng để test tác động trước khi drop hẳn index, an toàn hơn cho production — không cần rebuild lại nếu cần dùng lại.

```sql
ALTER INDEX idx_emp_lastname INVISIBLE;
```

---

## 3. Covering Index & Index-Only Access

Khi tất cả cột cần trong `SELECT`/`WHERE` đều nằm trong index → Oracle đọc thẳng từ index, khỏi động vào table (tránh **table access by rowid**). Đây là kỹ thuật tăng performance mạnh cho query đọc nhiều.

---

## 4. Những khái niệm cần nắm khi tune

| Khái niệm | Ý nghĩa | Vì sao quan trọng |
|---|---|---|
| **Selectivity** | Tỷ lệ giá trị distinct / tổng số row | Selectivity cao → B-tree ngon; thấp → nên xét Bitmap hoặc bỏ index |
| **Clustering Factor** | Đo mức độ data trên table có "gần" nhau theo thứ tự index hay không | CF thấp → data đã gần sắp xếp theo index → đọc rẻ; CF cao gần bằng số row → optimizer có thể chê index, chọn Full Scan |
| **Statistics** | Thông tin optimizer dùng để ước lượng cost | Stats cũ/sai → optimizer chọn execution plan (kế hoạch thực thi) sai toét, phải `DBMS_STATS.GATHER_TABLE_STATS` định kỳ |
| **Index Skip Scan** | Optimizer vẫn dùng được composite index dù query không filter leading column, nếu leading column ít giá trị | Không phải lúc nào thiếu leading column là toang |

---

## 5. Cách đánh index — nguyên tắc thực chiến

1. **Đánh theo query pattern thật, không đánh theo cảm tính** — profile query chạy nhiều/chậm trước (dùng `AWR`, `SQL Trace`, `EXPLAIN PLAN`) rồi mới quyết đánh cột nào.
2. **Cột trong WHERE/JOIN/ORDER BY** là ứng viên số 1.
3. **Composite index đặt cột equality trước, range sau.**
4. **Đừng đánh index lên cột low-cardinality bằng B-tree** (VD: boolean, giới tính) — vô dụng, tốn không gian, làm chậm DML. Xét Bitmap nếu là OLAP.
5. **Foreign key nên đánh index** — tránh full table scan khi lock cha-con hoặc join.
6. **Tránh over-indexing**: mỗi index thêm là DML chậm thêm. Review định kỳ index không dùng (`V$OBJECT_USAGE`, `DBA_INDEX_USAGE` từ 12c+) rồi drop hoặc set invisible.
7. **Rebuild vs Coalesce**: index bị phân mảnh (fragmentation) nhiều do DML → `ALTER INDEX ... REBUILD` (tốn resource, khóa lâu hơn) hoặc `COALESCE` (nhẹ hơn, gộp leaf block trống). Từ Oracle hiện đại, B-tree tự cân bằng khá tốt, rebuild định kỳ **không còn là best practice mặc định** như xưa — chỉ rebuild khi có lý do rõ (fragmentation nặng, đổi tablespace...).
8. **Update statistics thường xuyên**, đặc biệt sau bulk load, để optimizer không "mù" cost.

---

## 6. Ví dụ thực chiến — SELECT dùng index kiểu gì

Giả sử table:
```sql
CREATE TABLE orders (
  order_id     NUMBER PRIMARY KEY,
  customer_id  NUMBER,
  order_date   DATE,
  status       VARCHAR2(10),
  email        VARCHAR2(100)
);

CREATE INDEX idx_ord_cust_date ON orders(customer_id, order_date);
CREATE INDEX idx_upper_email ON orders(UPPER(email));
```

### Case 1: Filter đúng leading column → dùng index ngon
```sql
SELECT * FROM orders WHERE customer_id = 1001;
```
→ Optimizer dùng `idx_ord_cust_date`, chỉ scan đúng nhánh cây có `customer_id=1001` (INDEX RANGE SCAN), không đụng row khác.

### Case 2: Filter cả leading + trailing column → càng ngon
```sql
SELECT * FROM orders 
WHERE customer_id = 1001 AND order_date > SYSDATE - 30;
```
→ Vẫn dùng `idx_ord_cust_date`, thu hẹp thêm theo `order_date` ngay trong cùng 1 lần range scan.

### Case 3: Chỉ filter cột thứ 2, bỏ qua leading column → INDEX VÔ DỤNG
```sql
SELECT * FROM orders WHERE order_date > SYSDATE - 30;
```
→ Vi phạm **leftmost prefix rule**. Optimizer thường **không dùng được** `idx_ord_cust_date` hiệu quả (trừ trường hợp `customer_id` cực ít giá trị thì có thể Index Skip Scan, nhưng đừng trông cậy vào đó) → dễ bị Full Table Scan.
→ Fix: tạo thêm index riêng `CREATE INDEX idx_ord_date ON orders(order_date);` nếu query này chạy thường xuyên.

### Case 4: Wrap hàm quanh cột có index thường → INDEX VÔ DỤNG
```sql
SELECT * FROM orders WHERE UPPER(email) = 'ABC@GMAIL.COM';
```
→ Nếu chỉ có index thường trên `email` (không phải FBI) thì bó tay, phải Full Scan vì optimizer không biết `UPPER(email)` = gì trước khi đọc row.
→ Đây là lý do có `idx_upper_email` ở trên — query này sẽ dùng đúng index đó.

### Case 5: So sánh khác kiểu dữ liệu (implicit conversion) → INDEX VÔ DỤNG
```sql
-- customer_id là NUMBER nhưng lỡ query kiểu này
SELECT * FROM orders WHERE customer_id = '1001';
```
→ Oracle tự convert ngầm, nhiều trường hợp làm optimizer bỏ qua index trên `customer_id`. Luôn compare đúng kiểu dữ liệu.

### Case 6: LIKE với wildcard đầu/giữa chuỗi → INDEX VÔ DỤNG

```sql
SELECT * FROM orders WHERE email LIKE '%gmail.com';   -- wildcard đầu
SELECT * FROM orders WHERE email LIKE '%a%';           -- wildcard cả 2 đầu
```

**Vì sao toang:** B-tree index sắp giá trị theo thứ tự ký tự tính từ đầu chuỗi (giống mục lục từ điển tra theo chữ cái đầu). Muốn cây nhảy vào đúng nhánh thì phải biết chuỗi **bắt đầu bằng gì**. `%a%` hay `%gmail.com` không cho biết ký tự đầu là gì → cây không có điểm neo để nhảy vào, Oracle buộc phải quét hết leaf block, tức là quy về Full Table Scan (hoặc Full Index Scan cũng chẳng nhanh hơn full table bao nhiêu).

| Pattern | Dùng được B-tree index? |
|---|---|
| `'abc%'` | Có — INDEX RANGE SCAN |
| `'%abc'` | Không |
| `'%abc%'` | Không |
| `'a_c'` (underscore = 1 ký tự bất kỳ) | Có, nếu ký tự trước `_` cố định (VD `'ab_'`) |

**Giải pháp khi bắt buộc search kiểu chứa chuỗi ở giữa:**

#### 1. Oracle Text Index (CTXSYS.CONTEXT) — full-text search chuyên dụng
Đây là index type riêng, build ngược (inverted index) theo từng "token" trong chuỗi, không phụ thuộc thứ tự ký tự đầu-cuối như B-tree.

```sql
-- Tạo index
CREATE INDEX idx_email_text ON orders(email)
INDEXTYPE IS CTXSYS.CONTEXT;

-- Query dùng CONTAINS thay vì LIKE
SELECT * FROM orders
WHERE CONTAINS(email, 'gmail', 1) > 0;
```

Ưu điểm: search cực nhanh dù chuỗi ở đâu trong text, hỗ trợ cả fuzzy search, stemming, ranking theo độ liên quan (score).
Nhược điểm:
- Index không tự đồng bộ real-time — mặc định cần `CTX_DDL.SYNC_INDEX()` hoặc set `PARAMETERS ('SYNC (ON COMMIT)')` khi tạo, nếu không data mới insert sẽ không search thấy ngay.
- Nặng hơn B-tree về storage và maintenance, không hợp cho cột update liên tục.
- Cú pháp `CONTAINS()` khác hẳn `LIKE`, phải sửa lại query code.

#### 2. Reverse toàn bộ giá trị + index thường (mẹo cho case đặc biệt)
Nếu chỉ cần match **hậu tố** (suffix, kiểu `'%.com'`), có thể lưu thêm cột `REVERSE(email)` và đánh B-tree lên đó, query bằng `REVERSE(email) LIKE 'moc.%'`. Chỉ áp dụng được cho suffix match, không giải quyết được `%a%` (chứa ở giữa).

#### 3. Đổi kiến trúc: tách bảng lookup / dùng search engine ngoài
Nếu tần suất `%a%` cao và data lớn (nhiều triệu row), cách bền nhất thực ra là đẩy ra ngoài Oracle — dùng Elasticsearch/OpenSearch làm search layer riêng, Oracle chỉ giữ vai trò nguồn dữ liệu gốc. Đây là hướng đúng nguyên tắc **scalable** — không ép DB làm việc nó không sinh ra để làm.

**Tóm 1 câu:** `%a%` = B-tree chịu thua chắc chắn, muốn nhanh phải đổi loại index (Oracle Text) hoặc đổi kiến trúc, không có mẹo nào "vá" B-tree để chạy được kiểu search này.

### Case 7: OR nhiều điều kiện khác cột → có thể vẫn ổn nhờ INDEX RANGE SCAN + CONCATENATION
```sql
SELECT * FROM orders WHERE customer_id = 1001 OR status = 'PAID';
```
→ Nếu cả 2 cột đều có index riêng, optimizer có thể gộp 2 lần range scan lại (`CONCATENATION`). Nhưng nếu 1 trong 2 cột không có index tốt → cả câu dễ bị kéo về Full Scan.

### Cách kiểm chứng thực tế (đừng đoán mò)
```sql
EXPLAIN PLAN FOR
SELECT * FROM orders WHERE customer_id = 1001 AND order_date > SYSDATE - 30;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
```
→ Nhìn cột `Operation`: thấy `INDEX RANGE SCAN` là index có chạy, thấy `TABLE ACCESS FULL` là toang, index không được dùng — quay lại check Case 3-6 xem dính lỗi nào.

---

## 7. Checklist tune nhanh khi query chậm

- [ ] `EXPLAIN PLAN` xem có Full Table Scan không đáng có không
- [ ] Check Selectivity + Clustering Factor của cột định đánh index
- [ ] Check leading column của composite index có match query không
- [ ] Check stats có stale (cũ) không
- [ ] Check có đang bị implicit conversion (kiểu dữ liệu lệch) làm optimizer bỏ qua index không
- [ ] Cân nhắc covering index nếu query đọc nhiều, ít cột

---

*Nguồn: tổng hợp kiến thức chuẩn Oracle Database Concepts & Performance Tuning Guide (Oracle Docs).*
