import request from '@/utils/request'

// 查询预警列表
export function listAlert(query) {
  return request({ url: '/talk/alert/list', method: 'get', params: query })
}

// 查询预警详情
export function getAlert(alertId) {
  return request({ url: '/talk/alert/' + alertId, method: 'get' })
}

// 新增预警（手动标记）
export function addAlert(data) {
  return request({ url: '/talk/alert', method: 'post', data: data })
}

// 修改预警
export function updateAlert(data) {
  return request({ url: '/talk/alert', method: 'put', data: data })
}

// 删除预警
export function delAlert(alertIds) {
  return request({ url: '/talk/alert/' + alertIds, method: 'delete' })
}

// 处理预警
export function handleAlert(alertId, alertStatus, handleRemark) {
  return request({ url: '/talk/alert/handle/' + alertId, method: 'put', data: { alertStatus, handleRemark } })
}

// 批量初始化预警（根据学生心理健康状态）
export function initAlerts() {
  return request({ url: '/talk/alert/batch-init', method: 'post' })
}
