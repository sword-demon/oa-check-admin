export enum ApprovalInstanceStatus {
  PENDING = 1,
  APPROVED = 2,
  REJECTED = 3,
  WITHDRAWN = 4,
  CANCELLED = 5,
}

export enum ApprovalTaskType {
  NORMAL = 1,
  COUNTERSIGN = 2,
  OR_SIGN = 3,
}

export enum ApprovalTaskResult {
  APPROVED = 1,
  REJECTED = 2,
  TRANSFERRED = 3,
  CANCELLED = 4,
}

export enum TemplateStatus {
  DRAFT = 1,
  PUBLISHED = 2,
}

export enum CommonStatus {
  DISABLED = 0,
  ACTIVE = 1,
}

export interface ApprovalInstance {
  id: number
  processTemplateId: number
  instanceTitle: string
  flowableProcessInstanceId: string
  initiatorUserId: number
  status: ApprovalInstanceStatus
  formData: string
  endAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ApprovalTask {
  id: number
  approvalInstanceId: number
  flowableTaskId: string
  assigneeUserId: number
  taskName: string
  taskType: ApprovalTaskType
  taskResult: ApprovalTaskResult | null
  taskComment: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ApprovalCc {
  id: number
  approvalInstanceId: number
  instanceTitle?: string
  instanceStatus?: number
  ccUserId: number
  ccReason: string
  readAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ProcessTemplate {
  id: number
  templateName: string
  templateKey: string
  flowableProcessDefinitionId: string | null
  formConfig: string | null
  bpmnXml: string | null
  publishedBpmnXml: string | null
  flowableDeploymentId: string | null
  version: number
  status: TemplateStatus
  createdAt: string
  updatedAt: string
}

export interface ProcessNodeConfig {
  id: number
  templateId: number
  nodeId: string
  nodeName: string
  nodeType: string
  assigneeType: string | null
  assigneeConfig: string | null
  multiInstanceType: string
  completionRatio: number | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface InstanceDiagram {
  bpmnXml: string
  completedNodeIds: string[]
  currentNodeIds: string[]
}

export interface DashboardStats {
  todoCount: number
  doneCount: number
  templateCount: number
  unreadCcCount: number
  recentActivities: ApprovalTask[]
}

export interface TaskVO {
  id: number
  approvalInstanceId: number
  flowableTaskId: string
  assigneeUserId: number
  taskName: string
  taskType: ApprovalTaskType
  taskResult: ApprovalTaskResult | null
  taskComment: string | null
  completedAt: string | null
  createdAt: string
  instanceTitle: string | null
  initiatorUserId: number | null
  instanceStatus: number | null
  formDataSummary: string | null
}
