import api from './api';
import type { ApiResponse, Game, GameResult, GameSessionStart } from '../types';

export interface AnswerSubmission {
  questionId?: number;
  selectedOptionId?: number;
  answerText?: string;
  isCorrect: boolean;
}

export const gameService = {
  async getGames(): Promise<Game[]> {
    const res = await api.get<ApiResponse<Game[]>>('/games');
    return res.data.data;
  },

  async startGame(code: string, topicId?: number): Promise<GameSessionStart> {
    const res = await api.post<ApiResponse<GameSessionStart>>(`/games/${code}/start`, null, {
      params: topicId ? { topicId } : undefined,
    });
    return res.data.data;
  },

  async submitAnswers(sessionId: number, answers: AnswerSubmission[]): Promise<GameResult> {
    const res = await api.post<ApiResponse<GameResult>>('/games/submit', {
      sessionId,
      answers,
    });
    return res.data.data;
  },
};
