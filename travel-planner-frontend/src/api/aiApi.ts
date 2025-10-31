// src/api/aiApi.ts
import apiClient from './apiClient'

export interface GeneratePlanRequest {
  userInput: string
  destination: string
  duration: number
  budget: number
  travelers: number
  preferences?: string
}

export interface Activity {
  time: string
  type: 'transport' | 'attraction' | 'restaurant' | 'accommodation'
  title: string
  description: string
  location: string
  estimatedCost: number
  coordinate?: {
    latitude: number
    longitude: number
  }
}

export interface DayPlan {
  day: number
  date?: string
  activities: Activity[]
}

export interface BudgetBreakdown {
  accommodation: number
  food: number
  transport: number
  attraction: number
  shopping: number
  other: number
}

export interface GeneratedPlanResponse {
  destination: string
  summary: string
  dailyPlans: DayPlan[]
  budgetBreakdown: BudgetBreakdown
  tips: string[]
}

/**
 * 调用 AI 生成旅行计划
 */
export const generateTravelPlan = async (
  request: GeneratePlanRequest
): Promise<GeneratedPlanResponse> => {
  const response = await apiClient.post('/ai/generate-plan', request)
  return response.data
}
