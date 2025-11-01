// src/api/aiApi.ts
import apiClient from './apiClient'

export interface GeneratePlanRequest {
  userInput: string
  destination: string
  duration: number
  budget: number
  travelers: number
  preferences?: string
  additionalRequirements?: string
}

export interface ParsedUserInput {
  destination: string
  duration: number | null
  budget: number | null
  travelers: number | null
  preferences: string[]
  additionalRequirements: string
}

export interface Activity {
  time: string
  type: 'transport' | 'attraction' | 'restaurant' | 'accommodation'
  title: string
  description: string
  location: string
  address?: string  // 地点的详细地址
  estimatedCost: number
  coordinate?: {
    latitude: number
    longitude: number
  }
  editing?: boolean  // 编辑状态标记
  originalData?: Activity  // 编辑前的原始数据备份
  isPlaceholder?: boolean  // 是否是占位符状态(用于首次聚焦清空)
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
 * 智能解析用户输入
 */
export const parseUserInput = async (userInput: string): Promise<ParsedUserInput> => {
  const response = await apiClient.post('/ai/parse-input', { userInput })
  return response.data
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
