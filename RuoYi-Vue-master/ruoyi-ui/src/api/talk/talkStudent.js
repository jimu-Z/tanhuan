import request from '@/utils/request'

// 查询学生信息管理列表
export function listTalk(query) {
  return request({
    url: '/ruoyi-system/talk/list',
    method: 'get',
    params: query
  })
}

// 查询学生信息管理详细
export function getTalk(studentId) {
  return request({
    url: '/ruoyi-system/talk/' + studentId,
    method: 'get'
  })
}

// 新增学生信息管理
export function addTalk(data) {
  return request({
    url: '/ruoyi-system/talk',
    method: 'post',
    data: data
  })
}

// 修改学生信息管理
export function updateTalk(data) {
  return request({
    url: '/ruoyi-system/talk',
    method: 'put',
    data: data
  })
}

// 删除学生信息管理
export function delTalk(studentId) {
  return request({
    url: '/ruoyi-system/talk/' + studentId,
    method: 'delete'
  })
}

// 查询学生详情（含历史谈话记录）
export function getStudentDetail(studentId) {
  return request({
    url: '/ruoyi-system/talk/detail/' + studentId,
    method: 'get'
  })
}

// 导入预览
export function importPreview(data) {
  return request({
    url: '/ruoyi-system/talk/import/preview',
    method: 'post',
    data: data
  })
}

// 执行导入
export function importExecute(data) {
  return request({
    url: '/ruoyi-system/talk/import/execute',
    method: 'post',
    data: data
  })
}
