export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
  timestamp: number
}

export interface PageData<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
