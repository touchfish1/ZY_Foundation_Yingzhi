import { request } from './http'

export interface ModelRouteConfig {
  id: number
  modelName: string
  provider: string
  providerModelName: string
  modelType: string
  status: string
  createdAt?: string
  updatedAt?: string
}

export interface ModelPricingConfig {
  id: number
  modelName: string
  inputPrice: number
  outputPrice: number
  currency: string
  effectiveFrom: string
  effectiveTo: string | null
  createdAt?: string
}

export interface PlanModelAccess {
  id: number
  planCode: string
  modelName: string
  maxConcurrency: number
  maxRpm: number
}

// Model Routes
export function listModelRoutes(): Promise<ModelRouteConfig[]> {
  return request<ModelRouteConfig[]>('/admin/ai/models')
}

export function createModelRoute(payload: Partial<ModelRouteConfig>): Promise<ModelRouteConfig> {
  return request<ModelRouteConfig>('/admin/ai/models', { method: 'POST', body: JSON.stringify(payload) })
}

export function updateModelRoute(id: number, payload: Partial<ModelRouteConfig>): Promise<ModelRouteConfig> {
  return request<ModelRouteConfig>(`/admin/ai/models/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
}

export function deleteModelRoute(id: number): Promise<void> {
  return request<void>(`/admin/ai/models/${id}`, { method: 'DELETE' })
}

// Model Pricing
export function listModelPricings(): Promise<ModelPricingConfig[]> {
  return request<ModelPricingConfig[]>('/admin/ai/pricing')
}

export function createModelPricing(payload: Partial<ModelPricingConfig>): Promise<ModelPricingConfig> {
  return request<ModelPricingConfig>('/admin/ai/pricing', { method: 'POST', body: JSON.stringify(payload) })
}

export function updateModelPricing(id: number, payload: Partial<ModelPricingConfig>): Promise<ModelPricingConfig> {
  return request<ModelPricingConfig>(`/admin/ai/pricing/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
}

// Plan Model Access
export function listPlanModelAccesses(): Promise<PlanModelAccess[]> {
  return request<PlanModelAccess[]>('/admin/ai/plan-access')
}

export function createPlanModelAccess(payload: Partial<PlanModelAccess>): Promise<PlanModelAccess> {
  return request<PlanModelAccess>('/admin/ai/plan-access', { method: 'POST', body: JSON.stringify(payload) })
}

export function deletePlanModelAccess(id: number): Promise<void> {
  return request<void>(`/admin/ai/plan-access/${id}`, { method: 'DELETE' })
}
