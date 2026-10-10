import api from './api';
import type {
  ApiResponse,
  AuthResponse,
  UserProfile,
  UpdateProfileRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  VerifyOtpRequest,
  ResetPasswordWithOtpRequest,
} from '../types';

export const authService = {
  async login(credentials: { email: string; password: string }): Promise<AuthResponse> {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    return res.data.data;
  },

  async getCurrentUser(): Promise<UserProfile> {
    const res = await api.get<ApiResponse<UserProfile>>('/auth/me');
    return res.data.data;
  },

  async updateProfile(data: UpdateProfileRequest): Promise<UserProfile> {
    const res = await api.put<ApiResponse<UserProfile>>('/auth/profile', data);
    return res.data.data;
  },

  async changePassword(data: ChangePasswordRequest): Promise<string> {
    const res = await api.post<ApiResponse<any>>('/auth/change-password', data);
    return res.data.message || 'Đổi mật khẩu thành công';
  },

  async sendForgotPasswordOtp(data: ForgotPasswordRequest): Promise<{ email: string; devOtpPreview?: string }> {
    const res = await api.post<ApiResponse<{ email: string; devOtpPreview?: string }>>('/auth/forgot-password', data);
    return res.data.data;
  },

  async verifyOtp(data: VerifyOtpRequest): Promise<boolean> {
    const res = await api.post<ApiResponse<{ valid: boolean }>>('/auth/verify-otp', data);
    return res.data.data.valid;
  },

  async resetPasswordWithOtp(data: ResetPasswordWithOtpRequest): Promise<string> {
    const res = await api.post<ApiResponse<any>>('/auth/reset-password', data);
    return res.data.message || 'Đặt lại mật khẩu thành công';
  },

  async uploadAvatar(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    const res = await api.post<ApiResponse<{ avatarUrl: string }>>('/auth/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data.avatarUrl;
  },
};
