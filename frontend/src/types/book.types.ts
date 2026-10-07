export type BookStatus = 'WANT_TO_READ' | 'READING' | 'COMPLETED' | 'ON_HOLD';

export interface Book {
  id: number;
  userId: number;
  title: string;
  author?: string;
  category?: string;
  description?: string;
  totalPages: number;
  currentPage: number;
  progressPercentage: number;
  status: BookStatus;
  rating?: number;
  reviewNotes?: string;
  startDate?: string;
  completedDate?: string;
  coverUrl?: string;
  quoteCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface BookDetail extends Book {
  quotes: BookQuote[];
  readingLogs: ReadingLog[];
}

export interface BookQuote {
  id: number;
  bookId: number;
  bookTitle?: string;
  bookAuthor?: string;
  bookCategory?: string;
  quoteText: string;
  pageNumber?: number;
  chapter?: string;
  note?: string;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ReadingLog {
  id: number;
  bookId: number;
  bookTitle?: string;
  logDate: string;
  pagesRead: number;
  minutesRead?: number;
  note?: string;
  createdAt: string;
}

export interface BookRequest {
  title: string;
  author?: string;
  category?: string;
  description?: string;
  totalPages?: number;
  currentPage?: number;
  status?: BookStatus;
  rating?: number;
  reviewNotes?: string;
  startDate?: string;
  completedDate?: string;
  coverUrl?: string;
}

export interface UpdateBookProgressRequest {
  currentPage: number;
  status?: BookStatus;
  rating?: number;
  reviewNotes?: string;
  pagesReadToday?: number;
  minutesSpent?: number;
}

export interface BookQuoteRequest {
  quoteText: string;
  pageNumber?: number;
  chapter?: string;
  note?: string;
  isFavorite?: boolean;
}

export interface ReadingStats {
  totalBooks: number;
  completedBooks: number;
  readingBooks: number;
  wantToReadBooks: number;
  onHoldBooks: number;
  totalPagesRead: number;
  totalQuotes: number;
  favoriteQuotes: number;
  categories: string[];
  booksByCategory: Record<string, number>;
  recentBooks: Book[];
}
