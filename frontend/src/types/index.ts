export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
}

export interface User {
  id: number;
  email: string;
  displayName: string;
  avatarUrl?: string;
  isActive?: boolean;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: number;
  email: string;
  displayName: string;
  avatarUrl?: string;
}

export interface VocabularyExample {
  id?: number;
  exampleSentence: string;
  meaning?: string;
  isPrimary?: boolean;
}

export interface Vocabulary {
  id: number;
  word: string;
  meaning: string;
  pronunciation?: string;
  partOfSpeech?: string;
  level?: string;
  note?: string;
  status: 'NEW' | 'LEARNING' | 'REVIEW' | 'MASTERED' | string;
  masteryLevel: number;
  reviewCount: number;
  lastReviewedAt?: string;
  nextReviewAt?: string;
  createdAt: string;
  updatedAt: string;
  examples: VocabularyExample[];
}

export interface GrammarExample {
  id?: number;
  exampleSentence: string;
  meaning?: string;
  isPrimary?: boolean;
}

export interface Grammar {
  id: number;
  topic: string;
  level?: string;
  structure?: string;
  positiveStructure?: string;
  negativeStructure?: string;
  questionStructure?: string;
  usage?: string;
  signalWords?: string;
  commonMistakes?: string;
  note?: string;
  status: 'NEW' | 'LEARNING' | 'REVIEW' | 'MASTERED' | string;
  masteryLevel: number;
  reviewCount: number;
  lastReviewedAt?: string;
  nextReviewAt?: string;
  createdAt: string;
  updatedAt: string;
  examples: GrammarExample[];
}

export interface ListeningLesson {
  id: number;
  title: string;
  description?: string;
  url?: string;
  durationSeconds?: number;
  level?: string;
  note?: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | string;
  listenedCount: number;
  learnedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SpeakingLesson {
  id: number;
  title: string;
  topic?: string;
  description?: string;
  url?: string;
  durationSeconds?: number;
  level?: string;
  note?: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | string;
  practiceCount: number;
  practicedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewItem {
  id: number;
  contentType: 'VOCABULARY' | 'GRAMMAR';
  contentId: number;
  masteryLevel: number;
  reviewCount: number;
  currentIntervalDays: number;
  lastReviewedAt?: string;
  nextReviewAt?: string;
  title: string;
  subtitle?: string;
  primaryMeaning?: string;
  structure?: string;
  exampleSentence?: string;
  exampleMeaning?: string;
  note?: string;
}

export interface ReviewDueSummary {
  totalDue: number;
  vocabularyDue: number;
  grammarDue: number;
  totalItems: number;
  totalMastered: number;
}

export interface Game {
  id: number;
  code: string;
  name: string;
  description?: string;
  gameType: string;
  isActive: boolean;
}

export interface GameOption {
  id: number;
  optionText: string;
  isCorrect?: boolean;
  displayOrder: number;
}

export interface GameQuestion {
  id: number;
  gameId: number;
  contentType?: string;
  contentId?: number;
  questionText: string;
  prompt?: string;
  targetAnswer?: string;
  explanation?: string;
  difficulty?: string;
  options: GameOption[];
}

export interface GameSessionStart {
  sessionId: number;
  gameId: number;
  gameCode: string;
  gameName: string;
  totalQuestions: number;
  startedAt: string;
  questions: GameQuestion[];
}

export interface GameResult {
  sessionId: number;
  gameCode: string;
  gameName: string;
  totalQuestions: number;
  correctAnswers: number;
  wrongAnswers: number;
  score: number;
  startedAt: string;
  completedAt: string;
}

export interface Activity {
  id: number;
  activityType: string;
  contentType?: string;
  contentId?: number;
  title: string;
  description?: string;
  activityDate: string;
  durationSeconds?: number;
}

export interface DashboardSummary {
  todayVocabulary: number;
  todayGrammar: number;
  todayListening: number;
  todaySpeaking: number;
  todayTotal: number;
  todayNeedReview: number;
  todayCompletedReview: number;
  todayProgressPercent: number;
  totalVocabulary: number;
  totalGrammar: number;
  totalListening: number;
  totalSpeaking: number;
  totalReviews: number;
  totalGameSessions: number;
  currentStreak: number;
  longestStreak: number;
  lastStudyDate?: string;
  dailyLearningTarget: number;
  recentActivities: Activity[];
}

export interface ChartData {
  labels: string[];
  vocabularyData: number[];
  grammarData: number[];
  listeningData: number[];
  speakingData: number[];
  reviewData: number[];
  totalData: number[];
}

export interface HeatmapDay {
  date: string;
  count: number;
  level: number;
}

export interface StreakHeatmap {
  currentStreak: number;
  longestStreak: number;
  lastStudyDate?: string;
  totalStudyDays: number;
  heatmapDays: HeatmapDay[];
}

export interface UserSettings {
  id?: number;
  theme: string;
  language: string;
  timezone: string;
  dailyLearningTarget: number;
  reviewEnabled: boolean;
  displayName?: string;
  email?: string;
}

export type DocumentType = 'EXCEL' | 'WORD' | 'PDF' | 'POWERPOINT' | 'IMAGE' | 'TEXT' | 'OTHER' | string;
export type DocumentCategory = 'TOEIC' | 'VOCABULARY' | 'GRAMMAR' | 'LISTENING' | 'SPEAKING' | 'TEST_EXAM' | 'GENERAL' | 'OTHER' | string;

export interface LearningDocument {
  id: number;
  title: string;
  fileName: string;
  storedFileName: string;
  filePath: string;
  fileType: DocumentType;
  mimeType?: string;
  fileSize: number;
  formattedFileSize: string;
  category?: DocumentCategory;
  description?: string;
  downloadCount: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentFilter {
  search?: string;
  category?: string;
  group?: string;
  fileType?: string;
  isFavorite?: boolean;
  dateRange?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface DocumentStatistics {
  totalDocuments: number;
  totalEnglish?: number;
  totalIt?: number;
  totalFileSizeBytes: number;
  formattedTotalSize: string;
  totalExcel: number;
  totalWord: number;
  totalPdf: number;
  totalPowerPoint: number;
  totalOther: number;
  totalFavorites: number;
  countByCategory: Record<string, number>;
}

export interface StudyLink {
  id: number;
  title: string;
  url: string;
  category?: string;
  description?: string;
  clickCount: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StudyLinkFilter {
  search?: string;
  category?: string;
  isFavorite?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | string;
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | string;
export type TaskCategory = 'ENGLISH' | 'WORK' | 'STUDY' | 'PERSONAL' | 'HEALTH' | 'PROJECT' | 'OTHER' | string;

export interface DailyTask {
  id: number;
  userId?: number;
  taskDate: string; // YYYY-MM-DD
  title: string;
  description?: string;
  category?: string;
  priority: TaskPriority;
  status: TaskStatus;
  isCompleted: boolean;
  displayOrder: number;
  estimatedTime?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DailyTaskRequest {
  taskDate: string;
  title: string;
  description?: string;
  category?: string;
  priority?: TaskPriority;
  status?: TaskStatus;
  isCompleted?: boolean;
  displayOrder?: number;
  estimatedTime?: string;
}

export interface DailyTaskFilter {
  taskDate?: string;
  fromDate?: string;
  toDate?: string;
  category?: string;
  priority?: string;
  status?: string;
  isCompleted?: boolean;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface DailyTaskSummary {
  taskDate: string;
  dayOfWeek: string;
  formattedDate?: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  pendingTasks: number;
  completionRate: number;
  statusEvaluation?: string;
  tasks?: DailyTask[];
}

export interface DailyTaskStats {
  todayTotal: number;
  todayCompleted: number;
  todayPending: number;
  todayInProgress: number;
  todayCompletionRate: number;
  totalTasksAllTime: number;
  totalCompletedAllTime: number;
  overallCompletionRate: number;
  totalDaysWithTasks: number;
  totalDaysCompletedFull: number;
  countByCategory?: Record<string, number>;
  countByPriority?: Record<string, number>;
}

export interface DailyTaskReorderItem {
  id: number;
  displayOrder: number;
}

