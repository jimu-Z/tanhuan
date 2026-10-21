import request from '@/utils/request'

export function listAttachment(sessionId) {
  return request({ url: '/ruoyi-system/talkattachment/list/' + sessionId, method: 'get' })
}

export function uploadAttachment(formData) {
  return request({
    url: '/ruoyi-system/talkattachment/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

export function delAttachment(ids) {
  return request({ url: '/ruoyi-system/talkattachment/' + ids, method: 'delete' })
}

export function downloadAttachmentFile(attachmentId) {
  return request({ url: '/ruoyi-system/talkattachment/download/' + attachmentId, method: 'get', responseType: 'blob', timeout: 0 })
}
