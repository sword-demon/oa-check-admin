import request from '@/utils/request'

<#list ctx.entity.allEntities as entity>
// ${entity.comment} APIs
export function get${entity.name}List(params?: Record<string, any>) {
  return request.get('/${ctx.config.module}/${entity.resourcePath}', { params })
}

export function get${entity.name}Detail(id: number) {
  return request.get(`/${ctx.config.module}/${entity.resourcePath}/${r'${id}'}`)
}

export function create${entity.name}(data: Record<string, any>) {
  return request.post('/${ctx.config.module}/${entity.resourcePath}', data)
}

export function update${entity.name}(id: number, data: Record<string, any>) {
  return request.put(`/${ctx.config.module}/${entity.resourcePath}/${r'${id}'}`, data)
}

export function delete${entity.name}(id: number) {
  return request.delete(`/${ctx.config.module}/${entity.resourcePath}/${r'${id}'}`)
}

</#list>
