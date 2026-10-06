export interface NutritionMicronutrient {
  id?: number;
  nutrientName: string;
  amount: number;
  unit: string;
}

export interface NutritionVariant {
  id: number;
  state: string;
  servingAmount: number;
  servingUnit: string;
  calories: number;
  protein: number;
  carbohydrate: number;
  fat: number;
  fiber: number;
  dataSource?: string;
  micronutrients: NutritionMicronutrient[];
}

export interface NutritionUnit {
  id: number;
  unit: string;
  gramValue?: number;
  mlValue?: number;
  description?: string;
}

export interface NutritionFoodSearchItem {
  foodId?: number;
  userFoodId?: number;
  name: string;
  category?: string;
  description?: string;
  dataSource?: string;
  isUserCustom: boolean;
  variants: NutritionVariant[];
  units: NutritionUnit[];
}

export interface NutritionDailyLogItem {
  id: number;
  logDate: string;
  foodVariantId?: number;
  userFoodId?: number;
  foodName: string;
  state: string;
  quantity: number;
  unit: string;
  calculatedGrams?: number;
  calculatedMl?: number;
  calories: number;
  protein: number;
  carbohydrate: number;
  fat: number;
  fiber: number;
  isUserCustom: boolean;
  dataSource?: string;
  micronutrients: NutritionMicronutrient[];
}

export interface TopMicronutrient {
  nutrientName: string;
  totalAmount: number;
  unit: string;
}

export interface NutritionDailySummary {
  logDate: string;
  totalCalories: number;
  totalProtein: number;
  totalCarbohydrate: number;
  totalFat: number;
  totalFiber: number;
  items: NutritionDailyLogItem[];
  topMicronutrients: TopMicronutrient[];
}

export interface NutritionDailyLogRequest {
  logDate: string;
  foodVariantId?: number;
  userFoodId?: number;
  quantity: number;
  unit: string;
}

export interface NutritionRecentFood {
  foodVariantId?: number;
  userFoodId?: number;
  name: string;
  state: string;
  defaultQuantity: number;
  defaultUnit: string;
  isUserCustom: boolean;
}

export interface NutritionUserFoodRequest {
  name: string;
  state?: string;
  servingAmount?: number;
  servingUnit?: string;
  calories: number;
  protein: number;
  carbohydrate: number;
  fat: number;
  fiber?: number;
  dataSource?: string;
  description?: string;
  micronutrients?: NutritionMicronutrient[];
}

export interface NutritionTargetSettings {
  weight?: number;
  height?: number;
  targetCalories: number;
  targetProtein: number;
  targetCarb: number;
  targetFat: number;
}

export interface UsdaFoodItem {
  fdcId: number;
  description: string;
  dataType?: string;
  brandOwner?: string;
  servingSize?: number;
  servingSizeUnit?: string;
  calories: number;
  protein: number;
  carbohydrate: number;
  fat: number;
  fiber: number;
  micronutrients: NutritionMicronutrient[];
}

export interface NutritionActivity {
  id: number;
  name: string;
  category: string;
  intensity: 'HIGH' | 'MODERATE' | 'LOW' | string;
  metValue: number;
  description?: string;
  isActive?: boolean;
}

export interface NutritionActivityLog {
  id: number;
  userId?: number;
  logDate: string;
  activityId?: number;
  activityName: string;
  category: string;
  intensity: string;
  durationMinutes: number;
  weightKg: number;
  metValue: number;
  caloriesBurned: number;
  createdAt?: string;
}

export interface NutritionActivityLogRequest {
  activityId: number;
  logDate: string;
  durationMinutes: number;
  weightKg: number;
}

export interface CalorieDeficitSummary {
  logDate: string;
  weightKg: number;
  tdee: number;
  foodCalories: number;
  activityCalories: number;
  totalCaloriesBurned: number;
  calorieBalance: number;
  status: 'DEFICIT' | 'SURPLUS' | 'MAINTENANCE' | string;
  activityLogs: NutritionActivityLog[];
  loggedFoodsCount: number;
}

export interface NutritionCategorySummary {
  category: string;
  categoryName: string;
  totalMinutes: number;
  totalCalories: number;
  sessionsCount: number;
  percentage: number;
}

export interface NutritionDailyTrendItem {
  date: string;
  dayOfWeek: string;
  foodCalories: number;
  activityCalories: number;
  totalBurned: number;
  tdee: number;
  calorieBalance: number;
  status: 'DEFICIT' | 'SURPLUS' | 'MAINTENANCE' | string;
  protein: number;
  carbohydrate: number;
  fat: number;
  workoutMinutes: number;
  foodCount: number;
  activityCount: number;
  hasGym?: boolean;
  activitySummary?: string;
}

export interface NutritionStatisticsResponse {
  startDate: string;
  endDate: string;
  daysCount: number;
  loggedDaysCount?: number;
  tdee: number;
  weightKg: number;
  totalFoodCalories: number;
  avgDailyFoodCalories: number;
  totalActivityCalories: number;
  avgDailyActivityCalories: number;
  totalBurnedCalories: number;
  avgDailyBurnedCalories: number;
  netCalorieBalance: number;
  avgDailyCalorieBalance: number;
  estimatedFatKgChange: number;
  totalWorkoutMinutes: number;
  avgDailyWorkoutMinutes: number;
  workoutSessionsCount: number;
  deficitDaysCount: number;
  surplusDaysCount: number;
  maintenanceDaysCount: number;
  deficitRatePercent: number;
  gymDaysCount?: number;
  nonGymWorkoutDaysCount?: number;
  restDaysCount?: number;
  totalProtein: number;
  totalCarbohydrate: number;
  totalFat: number;
  totalFiber: number;
  avgDailyProtein: number;
  avgDailyCarbohydrate: number;
  avgDailyFat: number;
  categoryBreakdown: NutritionCategorySummary[];
  dailyTrend: NutritionDailyTrendItem[];
}

export interface CalorieDeficitCalculationRequest {
  logDate?: string;
  tdee?: number;
  weightKg?: number;
  foodCalories?: number;
  activities: {
    activityId?: number;
    activityName?: string;
    durationMinutes: number;
    metValue?: number;
  }[];
}




