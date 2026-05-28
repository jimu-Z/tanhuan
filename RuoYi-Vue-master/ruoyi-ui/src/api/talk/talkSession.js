import request from '@/utils/request'

// 查询谈话会话管理列表
export function listTalksession(query) {
  return request({
    url: '/ruoyi-system/talksession/list',
    method: 'get',
    params: query
  })
}

// 查询谈话会话管理详细
export function getTalksession(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/' + sessionId,
    method: 'get'
  })
}

// 新增谈话会话管理
export function addTalksession(data) {
  return request({
    url: '/ruoyi-system/talksession',
    method: 'post',
    data: data
  })
}

// 修改谈话会话管理
export function updateTalksession(data) {
  return request({
    url: '/ruoyi-system/talksession',
    method: 'put',
    data: data
  })
}

// 删除谈话会话管理
export function delTalksession(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/' + sessionId,
    method: 'delete'
  })
}
