import request from '@/utils/request'

// 查询谈话记录管理列表
export function listTalkrecord(query) {
  return request({
    url: '/ruoyi-system/talkrecord/list',
    method: 'get',
    params: query
  })
}

// 查询谈话记录管理详细
export function getTalkrecord(recordId) {
  return request({
    url: '/ruoyi-system/talkrecord/' + recordId,
    method: 'get'
  })
}

// 新增谈话记录管理
export function addTalkrecord(data) {
  return request({
    url: '/ruoyi-system/talkrecord',
    method: 'post',
    data: data
  })
}

// 修改谈话记录管理
export function updateTalkrecord(data) {
  return request({
    url: '/ruoyi-system/talkrecord',
    method: 'put',
    data: data
  })
}

// 删除谈话记录管理
export function delTalkrecord(recordId) {
  return request({
    url: '/ruoyi-system/talkrecord/' + recordId,
    method: 'delete'
  })
}
