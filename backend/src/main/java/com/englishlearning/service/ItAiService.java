package com.englishlearning.service;

import com.englishlearning.dto.it.ItAiResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItAiService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    public ItAiResponseDto askArchitect(String prompt, String codeSnippet, String language, String topicCategory) {
        return askArchitect(prompt, codeSnippet, language, topicCategory, null);
    }

    public ItAiResponseDto askArchitect(String prompt, String codeSnippet, String language, String topicCategory, String requestApiKey) {
        String effectiveKey = resolveApiKey(requestApiKey);

        if (StringUtils.hasText(effectiveKey)) {
            try {
                ItAiResponseDto aiRes = callGeminiForIt(prompt, codeSnippet, language, topicCategory, effectiveKey);
                if (aiRes != null && StringUtils.hasText(aiRes.getAnswerMarkdown())) {
                    return aiRes;
                }
            } catch (Exception e) {
                log.warn("Gemini IT Architect call failed, falling back to smart engine: {}", e.getMessage());
            }
        }
        return generateSmartFallbackResponse(prompt, codeSnippet, language, topicCategory);
    }

    public ItAiResponseDto debugOrOptimizeCode(String language, String code, String action) {
        return debugOrOptimizeCode(language, code, action, null);
    }

    public ItAiResponseDto debugOrOptimizeCode(String language, String code, String action, String requestApiKey) {
        String prompt = "RUN".equalsIgnoreCase(action) || "EXPLAIN_AI".equalsIgnoreCase(action)
                ? "Giải thích và phân tích luồng thực thi của đoạn code này, chỉ ra độ phức tạp thời gian và không gian."
                : "DEBUG_AI".equalsIgnoreCase(action)
                ? "Hãy tìm tất cả các lỗi tiềm ẩn (Syntax error, Runtime Exception, Memory leak, NullPointerException, Race condition) trong đoạn code này và đưa ra bản sửa hoàn chỉnh."
                : "Hãy tối ưu hóa hiệu năng, giảm thời gian xử lý và refactor code này theo Clean Code chuẩn Senior.";

        return askArchitect(prompt, code, language, "CODE_OPTIMIZATION", requestApiKey);
    }

    private String resolveApiKey(String requestApiKey) {
        if (StringUtils.hasText(requestApiKey)) {
            return requestApiKey.trim();
        }
        if (StringUtils.hasText(geminiApiKey)) {
            return geminiApiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (StringUtils.hasText(envKey)) {
            return envKey.trim();
        }
        return null;
    }

    private ItAiResponseDto callGeminiForIt(String prompt, String codeSnippet, String language, String topicCategory, String apiKey) {
        try {
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("Bạn là một Chuyên gia Công nghệ cấp cao (Principal Software Architect & Lead Engineer) kiêm Cố vấn Lập trình.\n");
            systemPrompt.append("Bạn có hơn 15 năm kinh nghiệm chuyên sâu về:\n");
            systemPrompt.append("1. Kiến trúc hệ thống phân tán (Distributed Systems, Microservices, Event-Driven Architecture, Saga Pattern, Outbox Pattern, CQRS, Circuit Breaker, High Concurrency, Multithreading).\n");
            systemPrompt.append("2. Message Streaming & Queues (Apache Kafka - Partitioning, Offset, Consumer Groups, Idempotent Producer, DLQ, RabbitMQ).\n");
            systemPrompt.append("3. Database & SQL Chuyên sâu (Indexing B-Tree, Execution Plan, Locking, Isolation Levels, Sharding, Read-Write Replica, Caching Redis).\n");
            systemPrompt.append("4. Fullstack & Backend Engineering (Java Spring Boot, Vue.js, TypeScript, Python, Clean Architecture, Design Patterns).\n");
            systemPrompt.append("5. DevOps & Cloud (Docker, Kubernetes, CI/CD, Observability, Distributed Tracing).\n\n");

            systemPrompt.append("HÃY TRẢ LỜI CÂU HỎI HOẶC YÊU CẦU SAU CỦA DEVELOPER MỘT CÁCH CHUYÊN NGHIỆP, THỰC TẾ, CỤ THỂ VÀ DỄ HIỂU:\n");
            systemPrompt.append("- Chủ đề: ").append(topicCategory != null ? topicCategory : "GENERAL_IT").append("\n");
            systemPrompt.append("- Câu hỏi/Yêu cầu: ").append(prompt).append("\n");

            if (StringUtils.hasText(codeSnippet)) {
                systemPrompt.append("- Đoạn code đính kèm (Ngôn ngữ: ").append(language != null ? language : "General").append("):\n");
                systemPrompt.append("```").append(language != null ? language.toLowerCase() : "").append("\n");
                systemPrompt.append(codeSnippet).append("\n```\n");
            }

            systemPrompt.append("\nQUY TẮC PHẢN HỒI (RẤT QUAN TRỌNG):\n");
            systemPrompt.append("1. NẾU NGƯỜI DÙNG CHỈ CHÀO HỎI NGẮN GỌN (ví dụ: 'alo', 'chào bạn', 'hi', 'bạn là ai', 'giúp tôi với'...): Hãy chào lại thân thiện, tự giới thiệu bạn là Trợ lý Kiến trúc sư Hệ thống & Cố vấn Lập trình (Principal Architect AI), sẵn sàng giải đáp về Microservices, Kafka, Tối ưu SQL, Spring Boot, Vue, Refactor Code, debug lỗi... Trong trường hợp này, đặt mermaidDiagram, optimizedCode, language và keyTakeaways là null.\n");
            systemPrompt.append("2. NẾU LÀ CÂU HỎI KỸ THUẬT / KIẾN TRÚC: Hãy giải thích cặn kẽ bằng tiếng Việt, định dạng Markdown đẹp, phân tích ưu/nhược điểm, so sánh giải pháp và bẫy cần tránh trong thực tế production.\n");
            systemPrompt.append("3. NẾU CÂU HỎI CẦN SƠ ĐỒ HỆ THỐNG / LUỒNG XỬ LÝ: Hãy cung cấp code Mermaid hợp lệ (sequenceDiagram hoặc graph TD/LR) trong trường 'mermaidDiagram'. Nếu không cần vẽ thì để null.\n");
            systemPrompt.append("4. NẾU CÓ CODE MINH HỌA HOẶC CODE TỐI ƯU: Cung cấp trong 'optimizedCode' và điền 'language' (ví dụ: java, sql, typescript...). Nếu không có thì để null.\n");
            systemPrompt.append("5. LUÔN TRẢ VỀ JSON THUẦN (không dùng backticks ```json bao ngoài) theo đúng schema sau:\n");
            systemPrompt.append("{\n");
            systemPrompt.append("  \"answerMarkdown\": \"Nội dung giải thích Markdown chi tiết bằng tiếng Việt...\",\n");
            systemPrompt.append("  \"mermaidDiagram\": \"code mermaid hợp lệ hoặc null\",\n");
            systemPrompt.append("  \"optimizedCode\": \"code hoàn chỉnh sau khi sửa/tối ưu hoặc null\",\n");
            systemPrompt.append("  \"language\": \"java/sql/typescript/python... hoặc null\",\n");
            systemPrompt.append("  \"keyTakeaways\": \"3-5 ý cốt lõi quan trọng nhất hoặc null\"\n");
            systemPrompt.append("}\n");

            Map<String, Object> bodyMap = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", systemPrompt.toString())))),
                    "generationConfig", Map.of(
                            "temperature", 0.4,
                            "responseMimeType", "application/json"
                    )
            );

            String requestBody = objectMapper.writeValueAsString(bodyMap);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(25))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> respMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) respMap.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    String rawJson = (String) parts.get(0).get("text");
                    if (StringUtils.hasText(rawJson)) {
                        rawJson = rawJson.trim();
                        if (rawJson.startsWith("```json")) {
                            rawJson = rawJson.substring(7);
                        } else if (rawJson.startsWith("```")) {
                            rawJson = rawJson.substring(3);
                        }
                        if (rawJson.endsWith("```")) {
                            rawJson = rawJson.substring(0, rawJson.length() - 3);
                        }
                        rawJson = rawJson.trim();
                        try {
                            return objectMapper.readValue(rawJson, ItAiResponseDto.class);
                        } catch (Exception parseEx) {
                            return ItAiResponseDto.builder()
                                    .answerMarkdown(rawJson)
                                    .build();
                        }
                    }
                }
            } else {
                log.warn("Gemini IT API call returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Gemini IT API call error: {}", e.getMessage());
        }
        return null;
    }

    private ItAiResponseDto generateSmartFallbackResponse(String prompt, String codeSnippet, String language, String topicCategory) {
        String trimmed = prompt != null ? prompt.trim() : "";
        String promptLower = trimmed.toLowerCase();
        String cleanPrompt = promptLower.replaceAll("[!?,.;~`@#$%^&*()_+\\-=\\[\\]{}|\\\\:\"'<>/]", " ").trim().replaceAll("\\s+", " ");

        // 0. Greeting / Casual Conversation Check
        boolean isGreeting = cleanPrompt.isEmpty() ||
                cleanPrompt.contains("alo") ||
                cleanPrompt.contains("xin chào") ||
                cleanPrompt.contains("xin chao") ||
                cleanPrompt.contains("chào bạn") ||
                cleanPrompt.contains("chao ban") ||
                cleanPrompt.equals("chào") ||
                cleanPrompt.equals("chao") ||
                cleanPrompt.equals("hi") ||
                cleanPrompt.startsWith("hi ") ||
                cleanPrompt.equals("hello") ||
                cleanPrompt.startsWith("hello ") ||
                cleanPrompt.equals("hey") ||
                cleanPrompt.startsWith("hey ") ||
                cleanPrompt.contains("bạn là ai") ||
                cleanPrompt.contains("ban la ai") ||
                cleanPrompt.contains("who are you") ||
                cleanPrompt.equals("test") ||
                cleanPrompt.equals("help") ||
                cleanPrompt.equals("giúp") ||
                cleanPrompt.equals("ơi") ||
                (cleanPrompt.length() <= 4 && !cleanPrompt.contains("sql"));

        if (isGreeting) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### 👋 Xin chào bạn! Tôi là IT Architect & Coding Mentor AI\n\n" +
                            "Tôi là trợ lý AI chuyên sâu về **Kiến trúc Hệ thống Phân tán, Microservices, Tối ưu hóa Database và Lập trình Fullstack**.\n\n" +
                            "Tôi có thể đồng hành cùng bạn học tập và giải quyết mọi bài toán kỹ thuật:\n" +
                            "- 🏗️ **Kiến trúc Hệ thống & Microservices:** Phân rã Monolith, Saga Pattern, Transactional Outbox, CQRS, Circuit Breaker, High Concurrency.\n" +
                            "- 📨 **Message Streaming & Queues:** Apache Kafka (Partitioning, Offset, Consumer Group, Idempotency, DLQ), RabbitMQ.\n" +
                            "- 🗄️ **Database & Tối ưu SQL:** Bản chất B-Tree Index, Phân tích Execution Plan, Locking & Isolation Levels, Sharding, Caching Redis.\n" +
                            "- 💻 **Fullstack & Clean Code:** Java Spring Boot, Vue.js, TypeScript, Python, Design Patterns, Refactor & Debugging.\n" +
                            "- ⚡ **Code Playground & Trực quan hóa:** Viết code, test lỗi và xem sơ đồ luồng hệ thống bằng Mermaid.\n\n" +
                            "> 💡 **Mẹo:** Bạn có thể nhập bất kỳ câu hỏi kỹ thuật nào, hoặc cấu hình **Google Gemini API Key** trong phần **Cài đặt** để tôi giải đáp chi tiết theo thời gian thực!")
                    .mermaidDiagram(null)
                    .optimizedCode(null)
                    .language(language != null ? language : "java")
                    .keyTakeaways("1. Đặt câu hỏi cụ thể hoặc dán đoạn code cần phân tích/tối ưu.\n2. Cấu hình Google Gemini API Key trong Cài đặt để AI phân tích linh hoạt mọi ngữ cảnh.\n3. Nhấn 'Lưu vào Sổ tay' ở câu trả lời để lưu trữ kiến thức quan trọng.")
                    .build();
        }

        // 1. Kafka / Message Queue Fallback
        if (promptLower.contains("kafka") || promptLower.contains("queue") || promptLower.contains("message") || promptLower.contains("broker") || promptLower.contains("rabbitmq") || promptLower.contains("dlq") || promptLower.contains("event-driven")) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### 📨 Kiến trúc Apache Kafka & Xử lý Event-Driven chuyên sâu\n\n" +
                            "**1. Khái niệm cốt lõi:**\n" +
                            "- **Topic & Partition**: Topic được chia thành nhiều Partition để hỗ trợ xử lý song song (Horizontal Scalability).\n" +
                            "- **Consumer Group**: Mỗi partition trong một topic chỉ được đọc bởi duy nhất 1 consumer trong cùng 1 group tại một thời điểm.\n" +
                            "- **Offset Management**: Kafka lưu vết vị trí đọc của từng consumer thông qua `__consumer_offsets`.\n\n" +
                            "**2. Các bẫy kinh điển trong Production:**\n" +
                            "- **Trùng lặp tin nhắn (Duplication)**: Do mạng chập chờn khi commit offset -> Bắt buộc thiết kế Consumer theo nguyên tắc **Idempotent (Xử lý có tính lũy thừa)** bằng bảng Deduplication hoặc Redis Key.\n" +
                            "- **Thứ tự tin nhắn (Ordering)**: Kafka chỉ đảm bảo thứ tự trong cùng 1 Partition. Muốn giữ thứ tự cho cùng một khách hàng/đơn hàng, hãy truyền `Partition Key` (ví dụ `orderId` hoặc `userId`).\n" +
                            "- **Dead Letter Queue (DLQ)**: Khi một message bị lỗi parsing/logic nhiều lần, chuyển nó sang DLQ để không chặn toàn bộ luồng xử lý của topic.")
                    .mermaidDiagram("sequenceDiagram\n" +
                            "    autonumber\n" +
                            "    actor Client as Client App\n" +
                            "    participant Producer as Order Service (Producer)\n" +
                            "    participant Kafka as Apache Kafka (Topic: order-events)\n" +
                            "    participant Consumer as Payment Worker (Consumer Group)\n" +
                            "    participant DB as Postgres Database\n" +
                            "    Client->>Producer: Tạo đơn hàng\n" +
                            "    Producer->>Kafka: Gửi OrderCreatedEvent (Key: orderId)\n" +
                            "    Kafka-->>Consumer: Pull message theo Partition\n" +
                            "    Consumer->>DB: Kiểm tra Idempotency Key\n" +
                            "    Consumer->>DB: Trừ tiền & Cập nhật trạng thái\n" +
                            "    Consumer->>Kafka: Commit Offset thành công")
                    .language("java")
                    .keyTakeaways("1. Idempotency là bắt buộc trong mọi Consumer.\n2. Thứ tự tin nhắn chỉ được bảo toàn trong cùng 1 Partition (dùng Message Key).\n3. Luôn cấu hình Retry + Dead Letter Queue cho các tin nhắn lỗi.")
                    .build();
        }

        // 2. Microservices / Saga Pattern Fallback
        if (promptLower.contains("microservice") || promptLower.contains("saga") || promptLower.contains("outbox") || promptLower.contains("distributed") || promptLower.contains("cqrs") || promptLower.contains("circuit breaker")) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### 🏗️ Quản lý Transaction phân tán với Saga Pattern\n\n" +
                            "Trong kiến trúc Microservices, mỗi service sở hữu một Database riêng biệt (*Database per Service*). Do đó không thể dùng ACID Transaction cấp DB (`@Transactional` thông thường).\n\n" +
                            "**Có 2 dạng triển khai Saga Pattern:**\n" +
                            "1. **Choreography-based (Dựa trên Sự kiện)**: Các service tự lắng nghe Event của nhau và tự xử lý. Ưu điểm: Độc lập cao, không có điểm nghẽn tập trung. Nhược điểm: Khó debug luồng phức tạp.\n" +
                            "2. **Orchestration-based (Bộ điều phối trung tâm)**: Sử dụng một Orchestrator Service (như Camunda hoặc Saga Coordinator) điều khiển luồng từng bước. Ưu điểm: Dễ kiểm soát, dễ theo dõi trạng thái, dễ rollback.\n\n" +
                            "**Cơ chế Bù trừ (Compensating Transactions):**\n" +
                            "Nếu bước thanh toán thành công nhưng bước giao hàng thất bại, Orchestrator sẽ kích hoạt giao dịch bù trừ để hoàn tiền lại cho khách hàng.")
                    .mermaidDiagram("graph LR\n" +
                            "    A[Order Service] -->|1. Create Pending Order| B(Kafka Event Bus)\n" +
                            "    B -->|2. OrderCreated| C[Payment Service]\n" +
                            "    C -->|3. PaymentSuccess| B\n" +
                            "    B -->|4. Trigger Inventory| D[Inventory Service]\n" +
                            "    D -.->|5. Out of Stock - Rollback| C")
                    .language("java")
                    .keyTakeaways("1. Dùng Saga Pattern thay cho 2-Phase Commit (2PC) để tránh deadlock và suy giảm hiệu năng.\n2. Mọi bước phải có Compensating Action (hành động bù trừ) để rollback.\n3. Kết hợp Transactional Outbox Pattern để chống mất mát event giữa DB và Message Broker.")
                    .build();
        }

        // 3. SQL / Database Optimization Fallback
        if (promptLower.contains("sql") || promptLower.contains("index") || promptLower.contains("database") || promptLower.contains("query") || promptLower.contains("b-tree") || promptLower.contains("btree") || promptLower.contains("table scan")) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### 🗄️ Tối ưu hóa SQL Query & Bản chất B-Tree Index\n\n" +
                            "**1. Nguyên tắc sử dụng Index hiệu quả:**\n" +
                            "- **Leftmost Prefix Rule**: Với Composite Index `(user_id, status, created_at)`, query chỉ ăn Index nếu điều kiện lọc bắt đầu từ `user_id`.\n" +
                            "- **Tránh dùng hàm trên cột Index**: `WHERE YEAR(created_at) = 2026` sẽ làm mất Index (gây Table Scan). Hãy đổi thành `WHERE created_at >= '2026-01-01' AND created_at < '2027-01-01'`.\n" +
                            "- **Tránh wildcard đầu chuỗi**: `LIKE '%hieu'` không ăn Index; `LIKE 'hieu%'` vẫn ăn Index.\n\n" +
                            "**2. Phân biệt Clustered vs Non-Clustered Index:**\n" +
                            "- **Clustered Index**: Dữ liệu bảng được sắp xếp vật lý theo index này (mỗi bảng chỉ có 1 Clustered Index, thường là Primary Key).\n" +
                            "- **Non-Clustered Index**: Lưu trữ cây B-Tree riêng và trỏ về con trỏ bản ghi (hoặc Clustered Key). Dùng `Covering Index` (INCLUDE columns) để tránh bước `Key Lookup / Bookmark Lookup`.")
                    .mermaidDiagram("graph TD\n" +
                            "    Root[Root Node B-Tree] --> N1[Non-Leaf Node 1..500]\n" +
                            "    Root --> N2[Non-Leaf Node 501..1000]\n" +
                            "    N1 --> Leaf1[Leaf: Data Pointer 1..250]\n" +
                            "    N1 --> Leaf2[Leaf: Data Pointer 251..500]\n" +
                            "    Leaf1 --> DataRow[Physical Table Row in Disk]")
                    .language("sql")
                    .optimizedCode("-- Query tối ưu tránh Table Scan:\n" +
                            "CREATE NONCLUSTERED INDEX idx_orders_user_status_date\n" +
                            "ON orders (user_id, status, created_at)\n" +
                            "INCLUDE (total_amount);\n\n" +
                            "SELECT user_id, status, created_at, total_amount\n" +
                            "FROM orders WITH (INDEX(idx_orders_user_status_date))\n" +
                            "WHERE user_id = 123 AND status = 'COMPLETED'\n" +
                            "  AND created_at >= '2026-01-01';")
                    .keyTakeaways("1. Đọc Execution Plan (`EXPLAIN / SET STATISTICS IO ON`) để kiểm tra Index Seek vs Table Scan.\n2. Sử dụng Covering Index để tránh chi phí đắt đỏ của Key Lookup.\n3. Định kỳ Rebuild / Reorganize Index khi độ phân mảnh (Fragmentation) > 30%.")
                    .build();
        }

        // 4. Redis / Cache Fallback
        if (promptLower.contains("redis") || promptLower.contains("cache") || promptLower.contains("caching") || promptLower.contains("ttl") || promptLower.contains("stampede")) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### ⚡ Caching Chiến Lược & Chống Cache Breakdown / Avalanche\n\n" +
                            "**1. Các Pattern Caching phổ biến:**\n" +
                            "- **Cache-Aside (Lazy Loading)**: Ứng dụng đọc Cache trước -> Miss thì đọc DB và ghi ngược vào Cache. Đây là pattern phổ biến nhất.\n" +
                            "- **Write-Through**: Ghi đồng thời vào Cache và DB.\n" +
                            "- **Write-Behind (Write-Back)**: Ghi vào Cache trước, sau đó bất đồng bộ đẩy xuống DB theo batch (rất nhanh nhưng có rủi ro mất dữ liệu nếu Redis sập).\n\n" +
                            "**2. Các sự cố lớn trong Production và giải pháp:**\n" +
                            "- **Cache Avalanche (Tuyết lở)**: Hàng loạt key hết hạn cùng 1 thời điểm -> Mọi request ùa xuống DB làm sập DB. Giải pháp: Thêm giá trị ngẫu nhiên vào TTL (ví dụ `TTL = 3600 + random(0, 300)`).\n" +
                            "- **Cache Breakdown (Thủng cache / Hotspot Key)**: Một key cực 'hot' vừa hết hạn, hàng vạn request đồng thời đọc DB. Giải pháp: Dùng **Distributed Mutex Lock (Redisson / SETNX)** để chỉ cho 1 request tải từ DB, các request khác chờ.")
                    .mermaidDiagram("graph TD\n" +
                            "    Client -->|1. Get Data| App\n" +
                            "    App -->|2. Check Cache| Redis[(Redis Cache)]\n" +
                            "    Redis -->|3. Cache Hit| App\n" +
                            "    Redis -.->|3. Cache Miss| Mutex[Acquire Mutex Lock]\n" +
                            "    Mutex -->|4. Query DB| DB[(SQL Database)]\n" +
                            "    DB -->|5. Write Cache + TTL| Redis")
                    .language("java")
                    .keyTakeaways("1. Luôn gán Random TTL để chống Cache Avalanche.\n2. Dùng Distributed Lock khi reload Hotspot Key để chống Cache Breakdown.\n3. Dùng Bloom Filter ở trước Cache để chặn Cache Penetration (truy vấn key không hề tồn tại trong DB).")
                    .build();
        }

        // 5. Spring Boot / Java Fallback
        if (promptLower.contains("spring") || promptLower.contains("bean") || promptLower.contains("transactional") || promptLower.contains("multithread") || promptLower.contains("jvm") || promptLower.contains("gc")) {
            return ItAiResponseDto.builder()
                    .answerMarkdown("### ☕ Tối ưu Spring Boot & Xử lý Concurrency Chuyên sâu\n\n" +
                            "**1. Bẫy kinh điển với `@Transactional`:**\n" +
                            "- **Self-invocation trap**: Gọi method `@Transactional` từ một method khác trong cùng class sẽ KHÔNG kích hoạt Proxy -> Transaction không hoạt động.\n" +
                            "- **Exception rollback mặc định**: Spring chỉ rollback khi gặp `RuntimeException` hoặc `Error`. Muốn rollback cả Checked Exception, bắt buộc khai báo `@Transactional(rollbackFor = Exception.class)`.\n" +
                            "- **Tránh giữ Transaction quá lâu**: Không gọi API bên thứ ba hoặc gửi email bên trong `@Transactional` vì sẽ chiếm dụng DB Connection trong HikariCP Connection Pool quá lâu gây cạn kiệt connection.\n\n" +
                            "**2. Cấu hình ThreadPoolTaskExecutor chuẩn:**\n" +
                            "- `corePoolSize`: Số thread luôn sẵn sàng chạy.\n" +
                            "- `maxPoolSize`: Số thread tối đa khi queue đầy.\n" +
                            "- `queueCapacity`: Dung lượng hàng đợi chứa task đang chờ.")
                    .mermaidDiagram("graph LR\n" +
                            "    Task[Incoming Task] --> Core{Active < CorePoolSize?}\n" +
                            "    Core -->|Yes| Exec1[Run on New Thread]\n" +
                            "    Core -->|No| Q{Queue Full?}\n" +
                            "    Q -->|No| Queue[Add to BlockingQueue]\n" +
                            "    Q -->|Yes| Max{Active < MaxPoolSize?}\n" +
                            "    Max -->|Yes| Exec2[Spawn Additional Thread]\n" +
                            "    Max -->|No| Reject[CallerRunsPolicy / RejectionHandler]")
                    .language("java")
                    .keyTakeaways("1. Khai báo @Transactional(rollbackFor = Exception.class) và tránh self-invocation.\n2. Tách các tác vụ I/O nặng hoặc gọi API bên ngoài ra khỏi Transaction.\n3. Luôn cấu hình giới hạn kích thước ThreadPool với CallerRunsPolicy.")
                    .build();
        }

        // Generic Technical Guidance
        return ItAiResponseDto.builder()
                .answerMarkdown("### 💻 Phân tích Kỹ thuật & Định hướng Kiến trúc Chuyên sâu\n\n" +
                        "**Vấn đề:** " + trimmed + "\n\n" +
                        "**1. Phân tích giải pháp:**\n" +
                        "- Áp dụng nguyên lý **Clean Architecture & SOLID** để phân tách rõ ràng giữa tầng Domain Logic, Use Case và Tầng Cơ sở hạ tầng (Database / External APIs).\n" +
                        "- Đảm bảo khả năng mở rộng (Scalability) bằng cách thiết kế hệ thống Stateless, áp dụng Caching phân tán (Redis) và Asynchronous Processing (Message Broker).\n" +
                        "- Kiểm soát Concurrency bằng cách lựa chọn cơ chế khóa phù hợp: **Optimistic Locking** (dựa trên cột `@Version`) cho hệ thống đọc nhiều, hoặc **Pessimistic Locking / Distributed Lock** cho các giao dịch tài chính nhạy cảm.\n\n" +
                        "**2. Kiểm soát lỗi & Giám sát Production:**\n" +
                        "- Triển khai Global Exception Handler với mã lỗi phân loại rõ ràng.\n" +
                        "- Bổ sung Correlation ID (TraceId) xuyên suốt chuỗi gọi service để giám sát Distributed Tracing trên Zipkin / Jaeger / Grafana Loki.")
                .mermaidDiagram("graph LR\n" +
                        "    Client[Client App] --> GW[API Gateway / Load Balancer]\n" +
                        "    GW --> Auth[Security & Rate Limiter]\n" +
                        "    Auth --> Core[Core Business Service]\n" +
                        "    Core --> Cache[(Redis Cache)]\n" +
                        "    Core --> DB[(Database Cluster)]")
                .language(language != null ? language : "java")
                .keyTakeaways("1. Luôn đo đạc và benchmark thực tế trước khi tối ưu hóa.\n2. Ghi log chuẩn định dạng JSON kèm TraceId để dễ dàng phân tích và cảnh báo lỗi.\n3. Đảm bảo tính nhất quán dữ liệu bằng Idempotency và Distributed Transactions khi cần.")
                .build();
    }
}
