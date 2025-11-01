// src/api/travelPlanApi.ts
import apiClient from './apiClient'

export interface TravelPlan {
  id: string
  destination: string
  startDate: string
  endDate: string
  duration: number
  budget: number
  travelers: number
  status: 'planning' | 'ongoing' | 'completed'
  preferences: string[]
  createdAt: string
  updatedAt: string
}

export interface CreateTravelPlanRequest {
  destination: string
  startDate: string
  endDate: string
  duration: number
  budget: number
  travelers: number
  preferences?: string[]
  userInput?: string
  aiGeneratedPlan?: string
}

/**
 * 获取用户的所有旅行计划
 */
export const getTravelPlans = async (): Promise<TravelPlan[]> => {
  const response = await apiClient.get('/travel-plans')
  return response.data
}

/**
 * 根据ID获取旅行计划详情
 */
export const getTravelPlanById = async (planId: string): Promise<TravelPlan> => {
  const response = await apiClient.get(`/travel-plans/${planId}`)
  return response.data
}

/**
 * 根据ID获取旅行计划详情(包含每日计划)
 */
export const getTravelPlanDetail = async (planId: string): Promise<any> => {
  const response = await apiClient.get(`/travel-plans/${planId}/detail`)
  return response.data
}

/**
 * 创建旅行计划
 */
export const createTravelPlan = async (
  request: CreateTravelPlanRequest
): Promise<TravelPlan> => {
  const response = await apiClient.post('/travel-plans', request)
  return response.data
}

/**
 * 更新旅行计划
 */
export const updateTravelPlan = async (
  planId: string,
  request: CreateTravelPlanRequest
): Promise<TravelPlan> => {
  const response = await apiClient.put(`/travel-plans/${planId}`, request)
  return response.data
}

/**
 * 删除旅行计划
 */
export const deleteTravelPlan = async (planId: string): Promise<void> => {
  await apiClient.delete(`/travel-plans/${planId}`)
}

/**
 * 手动标记计划为已完成
 */
export const markTravelPlanAsCompleted = async (
  planId: string
): Promise<TravelPlan> => {
  const response = await apiClient.patch(`/travel-plans/${planId}/complete`)
  return response.data
}
