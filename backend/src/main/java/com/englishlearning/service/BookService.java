package com.englishlearning.service;

import com.englishlearning.common.PageResponse;
import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.book.*;
import com.englishlearning.entity.Book;
import com.englishlearning.entity.BookQuote;
import com.englishlearning.entity.ReadingLog;
import com.englishlearning.entity.User;
import com.englishlearning.repository.BookQuoteRepository;
import com.englishlearning.repository.BookRepository;
import com.englishlearning.repository.ReadingLogRepository;
import com.englishlearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookResponse createBook(Long userId, BookRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int totalPages = request.getTotalPages() != null ? request.getTotalPages() : 0;
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : 0;
        String status = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "WANT_TO_READ";

        if (currentPage > 0 && "WANT_TO_READ".equals(status)) {
            status = "READING";
        }
        if (totalPages > 0 && currentPage >= totalPages) {
            status = "COMPLETED";
        }

        LocalDateTime startDate = request.getStartDate();
        if ("READING".equals(status) && startDate == null) {
            startDate = LocalDateTime.now();
        }

        LocalDateTime completedDate = request.getCompletedDate();
        if ("COMPLETED".equals(status) && completedDate == null) {
            completedDate = LocalDateTime.now();
        }

        Book book = Book.builder()
                .user(user)
                .title(request.getTitle().trim())
                .author(request.getAuthor() != null ? request.getAuthor().trim() : null)
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .description(request.getDescription())
                .totalPages(totalPages)
                .currentPage(currentPage)
                .status(status)
                .rating(request.getRating())
                .reviewNotes(request.getReviewNotes())
                .startDate(startDate)
                .completedDate(completedDate)
                .coverUrl(request.getCoverUrl())
                .build();

        Book saved = bookRepository.save(book);

        if (currentPage > 0) {
            ReadingLog log = ReadingLog.builder()
                    .user(user)
                    .book(saved)
                    .logDate(LocalDate.now())
                    .pagesRead(currentPage)
                    .note("Bắt đầu đọc")
                    .build();
            readingLogRepository.save(log);
        }

        return mapToBookResponse(saved);
    }

    @Transactional
    public BookResponse updateBook(Long id, Long userId, BookRequest request) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        int totalPages = request.getTotalPages() != null ? request.getTotalPages() : book.getTotalPages();
        int currentPage = request.getCurrentPage() != null ? request.getCurrentPage() : book.getCurrentPage();
        String status = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : book.getStatus();

        if (totalPages > 0 && currentPage >= totalPages) {
            status = "COMPLETED";
            if (book.getCompletedDate() == null) {
                book.setCompletedDate(LocalDateTime.now());
            }
        }

        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor() != null ? request.getAuthor().trim() : null);
        book.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);
        book.setDescription(request.getDescription());
        book.setTotalPages(totalPages);
        book.setCurrentPage(currentPage);
        book.setStatus(status);
        book.setRating(request.getRating());
        book.setReviewNotes(request.getReviewNotes());
        if (request.getStartDate() != null) {
            book.setStartDate(request.getStartDate());
        }
        if (request.getCompletedDate() != null) {
            book.setCompletedDate(request.getCompletedDate());
        }
        if (request.getCoverUrl() != null) {
            book.setCoverUrl(request.getCoverUrl());
        }

        Book saved = bookRepository.save(book);
        return mapToBookResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getBooks(
            Long userId,
            String status,
            String category,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.searchBooks(
                userId,
                status != null && !status.isBlank() ? status.trim().toUpperCase() : null,
                category != null && !category.isBlank() ? category.trim() : null,
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                pageable
        );

        List<BookResponse> content = bookPage.getContent().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());

        return PageResponse.from(bookPage, content);
    }

    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetail(Long id, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        List<BookQuoteResponse> quoteResponses = book.getQuotes().stream()
                .map(this::mapToQuoteResponse)
                .sorted(Comparator.comparing(BookQuoteResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        List<ReadingLogResponse> logResponses = book.getReadingLogs().stream()
                .map(this::mapToReadingLogResponse)
                .sorted(Comparator.comparing(ReadingLogResponse::getLogDate).reversed())
                .collect(Collectors.toList());

        return BookDetailResponse.builder()
                .id(book.getId())
                .userId(userId)
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .description(book.getDescription())
                .totalPages(book.getTotalPages())
                .currentPage(book.getCurrentPage())
                .progressPercentage(calculateProgress(book.getCurrentPage(), book.getTotalPages()))
                .status(book.getStatus())
                .rating(book.getRating())
                .reviewNotes(book.getReviewNotes())
                .startDate(book.getStartDate())
                .completedDate(book.getCompletedDate())
                .coverUrl(book.getCoverUrl())
                .quotes(quoteResponses)
                .readingLogs(logResponses)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    @Transactional
    public BookResponse updateProgress(Long id, Long userId, UpdateBookProgressRequest request) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        int prevPage = book.getCurrentPage() != null ? book.getCurrentPage() : 0;
        int newPage = request.getCurrentPage();
        book.setCurrentPage(newPage);

        if (book.getTotalPages() != null && book.getTotalPages() > 0 && newPage >= book.getTotalPages()) {
            book.setStatus("COMPLETED");
            if (book.getCompletedDate() == null) {
                book.setCompletedDate(LocalDateTime.now());
            }
        } else if (request.getStatus() != null && !request.getStatus().isBlank()) {
            book.setStatus(request.getStatus().trim().toUpperCase());
        } else if ("WANT_TO_READ".equals(book.getStatus()) && newPage > 0) {
            book.setStatus("READING");
            if (book.getStartDate() == null) {
                book.setStartDate(LocalDateTime.now());
            }
        }

        if (request.getRating() != null) {
            book.setRating(request.getRating());
        }
        if (request.getReviewNotes() != null) {
            book.setReviewNotes(request.getReviewNotes());
        }

        // Log reading session
        int pagesRead = request.getPagesReadToday() != null ? request.getPagesReadToday() : Math.max(0, newPage - prevPage);
        if (pagesRead > 0) {
            ReadingLog log = ReadingLog.builder()
                    .user(book.getUser())
                    .book(book)
                    .logDate(LocalDate.now())
                    .pagesRead(pagesRead)
                    .minutesRead(request.getMinutesSpent())
                    .note("Cập nhật tiến độ: trang " + newPage)
                    .build();
            readingLogRepository.save(log);
        }

        Book saved = bookRepository.save(book);
        return mapToBookResponse(saved);
    }

    @Transactional
    public void deleteBook(Long id, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        bookRepository.delete(book);
    }

    // ==========================================
    // QUOTES MANAGEMENT
    // ==========================================

    @Transactional
    public BookQuoteResponse addQuote(Long bookId, Long userId, BookQuoteRequest request) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        BookQuote quote = BookQuote.builder()
                .book(book)
                .quoteText(request.getQuoteText().trim())
                .pageNumber(request.getPageNumber())
                .chapter(request.getChapter() != null ? request.getChapter().trim() : null)
                .note(request.getNote())
                .isFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false)
                .build();

        BookQuote saved = bookQuoteRepository.save(quote);
        return mapToQuoteResponse(saved);
    }

    @Transactional
    public BookQuoteResponse updateQuote(Long quoteId, Long userId, BookQuoteRequest request) {
        BookQuote quote = bookQuoteRepository.findByIdAndBookUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with id: " + quoteId));

        quote.setQuoteText(request.getQuoteText().trim());
        quote.setPageNumber(request.getPageNumber());
        quote.setChapter(request.getChapter() != null ? request.getChapter().trim() : null);
        quote.setNote(request.getNote());
        if (request.getIsFavorite() != null) {
            quote.setIsFavorite(request.getIsFavorite());
        }

        BookQuote saved = bookQuoteRepository.save(quote);
        return mapToQuoteResponse(saved);
    }

    @Transactional
    public BookQuoteResponse toggleFavoriteQuote(Long quoteId, Long userId) {
        BookQuote quote = bookQuoteRepository.findByIdAndBookUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with id: " + quoteId));

        quote.setIsFavorite(!Boolean.TRUE.equals(quote.getIsFavorite()));
        BookQuote saved = bookQuoteRepository.save(quote);
        return mapToQuoteResponse(saved);
    }

    @Transactional
    public void deleteQuote(Long quoteId, Long userId) {
        BookQuote quote = bookQuoteRepository.findByIdAndBookUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with id: " + quoteId));
        bookQuoteRepository.delete(quote);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookQuoteResponse> getQuotes(
            Long userId,
            Long bookId,
            Boolean isFavorite,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookQuote> quotePage = bookQuoteRepository.searchQuotes(
                userId,
                bookId,
                isFavorite,
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                pageable
        );

        List<BookQuoteResponse> content = quotePage.getContent().stream()
                .map(this::mapToQuoteResponse)
                .collect(Collectors.toList());

        return PageResponse.from(quotePage, content);
    }

    // ==========================================
    // READING STATISTICS
    // ==========================================

    @Transactional(readOnly = true)
    public ReadingStatsResponse getReadingStats(Long userId) {
        long totalBooks = bookRepository.countByUserId(userId);
        long completedBooks = bookRepository.countByUserIdAndStatus(userId, "COMPLETED");
        long readingBooks = bookRepository.countByUserIdAndStatus(userId, "READING");
        long wantToReadBooks = bookRepository.countByUserIdAndStatus(userId, "WANT_TO_READ");
        long onHoldBooks = bookRepository.countByUserIdAndStatus(userId, "ON_HOLD");

        Long totalPagesRead = bookRepository.sumTotalPagesReadByUserId(userId);
        if (totalPagesRead == null) {
            totalPagesRead = 0L;
        }

        long totalQuotes = bookQuoteRepository.countByBookUserId(userId);
        long favoriteQuotes = bookQuoteRepository.countByBookUserIdAndIsFavoriteTrue(userId);

        List<String> categories = bookRepository.findDistinctCategoriesByUserId(userId);

        List<Book> allBooks = bookRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        Map<String, Long> booksByCategory = allBooks.stream()
                .filter(b -> b.getCategory() != null && !b.getCategory().isBlank())
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));

        List<BookResponse> recentBooks = allBooks.stream()
                .limit(5)
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());

        return ReadingStatsResponse.builder()
                .totalBooks(totalBooks)
                .completedBooks(completedBooks)
                .readingBooks(readingBooks)
                .wantToReadBooks(wantToReadBooks)
                .onHoldBooks(onHoldBooks)
                .totalPagesRead(totalPagesRead)
                .totalQuotes(totalQuotes)
                .favoriteQuotes(favoriteQuotes)
                .categories(categories)
                .booksByCategory(booksByCategory)
                .recentBooks(recentBooks)
                .build();
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private BookResponse mapToBookResponse(Book book) {
        int quoteCount = book.getQuotes() != null ? book.getQuotes().size() : 0;
        return BookResponse.builder()
                .id(book.getId())
                .userId(book.getUser() != null ? book.getUser().getId() : null)
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .description(book.getDescription())
                .totalPages(book.getTotalPages())
                .currentPage(book.getCurrentPage())
                .progressPercentage(calculateProgress(book.getCurrentPage(), book.getTotalPages()))
                .status(book.getStatus())
                .rating(book.getRating())
                .reviewNotes(book.getReviewNotes())
                .startDate(book.getStartDate())
                .completedDate(book.getCompletedDate())
                .coverUrl(book.getCoverUrl())
                .quoteCount(quoteCount)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    private BookQuoteResponse mapToQuoteResponse(BookQuote quote) {
        return BookQuoteResponse.builder()
                .id(quote.getId())
                .bookId(quote.getBook() != null ? quote.getBook().getId() : null)
                .bookTitle(quote.getBook() != null ? quote.getBook().getTitle() : "")
                .bookAuthor(quote.getBook() != null ? quote.getBook().getAuthor() : "")
                .bookCategory(quote.getBook() != null ? quote.getBook().getCategory() : "")
                .quoteText(quote.getQuoteText())
                .pageNumber(quote.getPageNumber())
                .chapter(quote.getChapter())
                .note(quote.getNote())
                .isFavorite(quote.getIsFavorite())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }

    private ReadingLogResponse mapToReadingLogResponse(ReadingLog log) {
        return ReadingLogResponse.builder()
                .id(log.getId())
                .bookId(log.getBook() != null ? log.getBook().getId() : null)
                .bookTitle(log.getBook() != null ? log.getBook().getTitle() : "")
                .logDate(log.getLogDate())
                .pagesRead(log.getPagesRead())
                .minutesRead(log.getMinutesRead())
                .note(log.getNote())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private Double calculateProgress(Integer currentPage, Integer totalPages) {
        if (totalPages == null || totalPages <= 0 || currentPage == null || currentPage <= 0) {
            return 0.0;
        }
        double percentage = ((double) currentPage / totalPages) * 100.0;
        return Math.min(100.0, Math.round(percentage * 10.0) / 10.0);
    }
}
