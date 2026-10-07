export interface HealthWorkoutPhoto {
  id: number;
  userId: number;
  logDate: string;
  imageUrl: string;
  caption?: string;
  weightKg?: number;
  createdAt: string;
  updatedAt: string;
}

export interface HealthWorkoutPhotoRequest {
  logDate: string;
  imageUrl: string;
  caption?: string;
  weightKg?: number;
}
