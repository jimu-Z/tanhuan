import request from '@/utils/request'

export function listTemplate(query) {
  return request({ url: '/ruoyi-system/talktemplate/list', method: 'get', params: query })
}

export function listSystemTemplate() {
  return request({ url: '/ruoyi-system/talktemplate/system', method: 'get' })
}

export function getTemplate(id) {
  return request({ url: '/ruoyi-system/talktemplate/' + id, method: 'get' })
}

export function addTemplate(data) {
  return request({ url: '/ruoyi-system/talktemplate', method: 'post', data: data })
}

export function updateTemplate(data) {
  return request({ url: '/ruoyi-system/talktemplate', method: 'put', data: data })
}

export function delTemplate(ids) {
  return request({ url: '/ruoyi-system/talktemplate/' + ids, method: 'delete' })
}
