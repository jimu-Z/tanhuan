import request from '@/utils/request'

// 查询预约列表
export function listAppointment(query) {
  return request({ url: '/talk/appointment/list', method: 'get', params: query })
}

// 查询预约详情
export function getAppointment(appointmentId) {
  return request({ url: '/talk/appointment/' + appointmentId, method: 'get' })
}

// 新增预约（学生发起）
export function addAppointment(data) {
  return request({ url: '/talk/appointment', method: 'post', data: data })
}

// 修改预约
export function updateAppointment(data) {
  return request({ url: '/talk/appointment', method: 'put', data: data })
}

// 删除预约
export function delAppointment(appointmentIds) {
  return request({ url: '/talk/appointment/' + appointmentIds, method: 'delete' })
}

// 确认预约（教师）
export function confirmAppointment(appointmentId) {
  return request({ url: '/talk/appointment/confirm/' + appointmentId, method: 'put' })
}

// 拒绝预约（教师）
export function rejectAppointment(appointmentId, rejectReason) {
  return request({ url: '/talk/appointment/reject/' + appointmentId, method: 'put', params: { rejectReason } })
}

// 取消预约（学生）
export function cancelAppointment(appointmentId) {
  return request({ url: '/talk/appointment/cancel/' + appointmentId, method: 'put' })
}

// 完成预约
export function completeAppointment(appointmentId) {
  return request({ url: '/talk/appointment/complete/' + appointmentId, method: 'put' })
}
