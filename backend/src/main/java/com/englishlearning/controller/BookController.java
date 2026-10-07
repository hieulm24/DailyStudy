package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.book.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.createBook(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm sách thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> getBooks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {
        PageResponse<BookResponse> response = bookService.getBooks(userPrincipal.getId(), status, category, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BookDetailResponse response = bookService.getBookDetail(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.updateBook(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật sách thành công", response));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<BookResponse>> updateProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateBookProgressRequest request) {
        BookResponse response = bookService.updateProgress(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật tiến độ đọc thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        bookService.deleteBook(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa sách thành công", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReadingStatsResponse>> getReadingStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReadingStatsResponse response = bookService.getReadingStats(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // QUOTES & PHILOSOPHY APIS
    // ==========================================

    @PostMapping("/{bookId}/quotes")
    public ResponseEntity<ApiResponse<BookQuoteResponse>> addQuote(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookQuoteRequest request) {
        BookQuoteResponse response = bookService.addQuote(bookId, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu trích dẫn / triết lý thành công", response));
    }

    @PutMapping("/quotes/{quoteId}")
    public ResponseEntity<ApiResponse<BookQuoteResponse>> updateQuote(
            @PathVariable Long quoteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookQuoteRequest request) {
        BookQuoteResponse response = bookService.updateQuote(quoteId, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trích dẫn thành công", response));
    }

    @PatchMapping("/quotes/{quoteId}/favorite")
    public ResponseEntity<ApiResponse<BookQuoteResponse>> toggleFavoriteQuote(
            @PathVariable Long quoteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BookQuoteResponse response = bookService.toggleFavoriteQuote(quoteId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/quotes/{quoteId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuote(
            @PathVariable Long quoteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        bookService.deleteQuote(quoteId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa trích dẫn thành công", null));
    }

    @GetMapping("/quotes")
    public ResponseEntity<ApiResponse<PageResponse<BookQuoteResponse>>> getAllQuotes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "isFavorite", required = false) Boolean isFavorite,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResponse<BookQuoteResponse> response = bookService.getQuotes(userPrincipal.getId(), bookId, isFavorite, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
