package com.englishlearning.service;

import com.englishlearning.dto.it.ItAiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItAiServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItAiService itAiService = new ItAiService(objectMapper);

    @Test
    void testGreetingAlo() {
        ItAiResponseDto res = itAiService.askArchitect("alo", null, null, "GENERAL_IT");
        assertNotNull(res);
        assertNotNull(res.getAnswerMarkdown());
        assertTrue(res.getAnswerMarkdown().contains("Xin chào bạn"), "Should contain greeting");
        assertNull(res.getMermaidDiagram(), "Mermaid diagram should be null for greetings");
    }

    @Test
    void testGreetingXinChao() {
        ItAiResponseDto res = itAiService.askArchitect("xin chào, bạn là ai", null, null, "GENERAL_IT");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("Xin chào bạn"));
        assertNull(res.getMermaidDiagram());
    }

    @Test
    void testGreetingAloBanOi() {
        ItAiResponseDto res = itAiService.askArchitect("alo bạn ơi", null, null, "GENERAL_IT");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("Xin chào bạn"));
        assertNull(res.getMermaidDiagram());
    }

    @Test
    void testKafkaQuery() {
        ItAiResponseDto res = itAiService.askArchitect("Tìm hiểu về Apache Kafka and Consumer Group", null, "java", "KAFKA_DISTRIBUTED");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("Apache Kafka"));
        assertNotNull(res.getMermaidDiagram());
    }

    @Test
    void testSqlQuery() {
        ItAiResponseDto res = itAiService.askArchitect("Tối ưu hóa B-Tree Index cho câu lệnh SQL", null, "sql", "DATABASE_OPTIMIZATION");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("B-Tree"));
        assertNotNull(res.getOptimizedCode());
    }

    @Test
    void testRedisQuery() {
        ItAiResponseDto res = itAiService.askArchitect("Chiến lược Redis Cache Avalanche và Mutex Lock", null, "java", "SYSTEM_DESIGN");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("Redis"));
        assertNotNull(res.getMermaidDiagram());
    }

    @Test
    void testSpringBootQuery() {
        ItAiResponseDto res = itAiService.askArchitect("Giải thích @Transactional trong Spring Boot", null, "java", "JAVA_SPRING");
        assertNotNull(res);
        assertTrue(res.getAnswerMarkdown().contains("Spring Boot"));
    }

    @Test
    void testDebugAction() {
        ItAiResponseDto res = itAiService.debugOrOptimizeCode("JAVA", "public void test() { String s = null; s.length(); }", "DEBUG_AI");
        assertNotNull(res);
        assertNotNull(res.getAnswerMarkdown());
    }
}
