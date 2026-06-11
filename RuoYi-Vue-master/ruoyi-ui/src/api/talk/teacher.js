import request from '@/utils/request'

// 查询教师列表
export function listTeacher(query) {
  return request({ url: '/talk/teacher/list', method: 'get', params: query })
}

// 查询教师详情
export function getTeacher(teacherId) {
  return request({ url: '/talk/teacher/' + teacherId, method: 'get' })
}

// 新增教师
export function addTeacher(data) {
  return request({ url: '/talk/teacher', method: 'post', data: data })
}

// 修改教师
export function updateTeacher(data) {
  return request({ url: '/talk/teacher', method: 'put', data: data })
}

// 删除教师
export function delTeacher(teacherIds) {
  return request({ url: '/talk/teacher/' + teacherIds, method: 'delete' })
}

// 获取学院下的辅导员/班主任列表
export function getCounselors(deptId) {
  return request({ url: '/talk/teacher/counselors/' + deptId, method: 'get' })
}

// 获取教师管理的所有学生（按学院查询）
export function getTeacherStudents(teacherId) {
  return request({ url: '/ruoyi-system/talk/byTeacher/' + teacherId, method: 'get' })
}

// 获取教师管理的班级名列表
export function getTeacherClasses(teacherId) {
  return request({ url: '/talk/teacher/' + teacherId + '/classes', method: 'get' })
}

// 保存教师管理的班级
export function saveTeacherClasses(teacherId, classNames) {
  return request({ url: '/talk/teacher/' + teacherId + '/classes', method: 'put', data: classNames })
}

// 获取全校所有班级名
export function getAllClassNames() {
  return request({ url: '/talk/teacher/allClassNames', method: 'get' })
}
