import request from '@/utils/request'

export function listTag(query) {
  return request({ url: '/ruoyi-system/talktag/list', method: 'get', params: query })
}

export function getActiveTags() {
  return request({ url: '/ruoyi-system/talktag/active', method: 'get' })
}

export function getTag(id) {
  return request({ url: '/ruoyi-system/talktag/' + id, method: 'get' })
}

export function addTag(data) {
  return request({ url: '/ruoyi-system/talktag', method: 'post', data: data })
}

export function updateTag(data) {
  return request({ url: '/ruoyi-system/talktag', method: 'put', data: data })
}

export function delTag(ids) {
  return request({ url: '/ruoyi-system/talktag/' + ids, method: 'delete' })
}

export function getLabels() {
  return request({ url: '/ruoyi-system/talktag/labels', method: 'get' })
}
