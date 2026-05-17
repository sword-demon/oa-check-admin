import request from '@/utils/request'
import type { ProcessTemplate } from '@/types'

export interface NodeConfig {
  id?: number
  templateId?: number
  nodeId: string
  nodeName: string
  nodeType: string
  assigneeType?: string
  assigneeConfig?: string
  multiInstanceType?: string
  completionRatio?: number | null
  sortOrder?: number
}

export function getTemplateXml(id: number) {
  return request.get(`/approval/template/${id}/xml`)
}

export function saveTemplateXml(id: number, bpmnXml: string) {
  return request.put(`/approval/template/${id}/xml`, { bpmnXml })
}

export function getNodeConfigs(id: number) {
  return request.get(`/approval/template/${id}/node-config`)
}

export function saveNodeConfigs(id: number, configs: NodeConfig[]) {
  return request.put(`/approval/template/${id}/node-config`, configs)
}

export function publishTemplate(id: number) {
  return request.post(`/approval/template/${id}/publish`)
}

export function createNewVersion(id: number) {
  return request.post(`/approval/template/${id}/new-version`)
}

export function getTemplate(id: number) {
  return request.get(`/approval/template/${id}`) as Promise<ProcessTemplate>
}

export function updateTemplate(id: number, data: Record<string, unknown>) {
  return request.put(`/approval/template/${id}`, data)
}

export function deleteTemplate(id: number) {
  return request.delete(`/approval/template/${id}`)
}
