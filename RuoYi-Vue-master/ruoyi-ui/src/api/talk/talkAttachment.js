import request from '@/utils/request'

export function listAttachment(sessionId) {
  return request({ url: '/ruoyi-system/talkattachment/list/' + sessionId, method: 'get' })
}

export function uploadAttachment(formData) {
  return request({
    url: '/ruoyi-system/talkattachment/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function delAttachment(ids) {
  return request({ url: '/ruoyi-system/talkattachment/' + ids, method: 'delete' })
}
