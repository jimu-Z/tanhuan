import request from '@/utils/request'

// 获取学院的辅导员/班主任列表（供预约选择）
export function getCounselors(deptId) {
  return request({ url: '/talk/teacher/counselors/' + deptId, method: 'get' })
}

// 学生发起预约
export function addAppointment(data) {
  return request({ url: '/talk/appointment', method: 'post', data: data })
}

// 查询我的预约列表
export function listMyAppointments(params) {
  return request({ url: '/talk/appointment/list', method: 'get', params })
}

// 取消预约
export function cancelAppointment(appointmentId) {
  return request({ url: '/talk/appointment/cancel/' + appointmentId, method: 'put' })
}
