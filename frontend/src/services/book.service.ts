import api from './api';
import type { ApiResponse, PageResponse } from '../types';
import type {
  Book,
  BookDetail,
  BookRequest,
  UpdateBookProgressRequest,
  BookQuote,
  BookQuoteRequest,
  ReadingStats,
} from '../types/book.types';

export const bookService = {
  async getBooks(params?: {
    status?: string;
    category?: string;
    keyword?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<Book>> {
    const res = await api.get<ApiResponse<PageResponse<Book>>>('/books', { params });
    return res.data.data;
  },

  async getBookDetail(id: number): Promise<BookDetail> {
    const res = await api.get<ApiResponse<BookDetail>>(`/books/${id}`);
    return res.data.data;
  },

  async createBook(data: BookRequest): Promise<Book> {
    const res = await api.post<ApiResponse<Book>>('/books', data);
    return res.data.data;
  },

  async updateBook(id: number, data: BookRequest): Promise<Book> {
    const res = await api.put<ApiResponse<Book>>(`/books/${id}`, data);
    return res.data.data;
  },

  async updateProgress(id: number, data: UpdateBookProgressRequest): Promise<Book> {
    const res = await api.patch<ApiResponse<Book>>(`/books/${id}/progress`, data);
    return res.data.data;
  },

  async deleteBook(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/books/${id}`);
  },

  async getStats(): Promise<ReadingStats> {
    const res = await api.get<ApiResponse<ReadingStats>>('/books/stats');
    return res.data.data;
  },

  // Quotes
  async addQuote(bookId: number, data: BookQuoteRequest): Promise<BookQuote> {
    const res = await api.post<ApiResponse<BookQuote>>(`/books/${bookId}/quotes`, data);
    return res.data.data;
  },

  async updateQuote(quoteId: number, data: BookQuoteRequest): Promise<BookQuote> {
    const res = await api.put<ApiResponse<BookQuote>>(`/books/quotes/${quoteId}`, data);
    return res.data.data;
  },

  async toggleFavoriteQuote(quoteId: number): Promise<BookQuote> {
    const res = await api.patch<ApiResponse<BookQuote>>(`/books/quotes/${quoteId}/favorite`);
    return res.data.data;
  },

  async deleteQuote(quoteId: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/books/quotes/${quoteId}`);
  },

  async getAllQuotes(params?: {
    bookId?: number;
    isFavorite?: boolean;
    keyword?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<BookQuote>> {
    const res = await api.get<ApiResponse<PageResponse<BookQuote>>>('/books/quotes', { params });
    return res.data.data;
  },
};
