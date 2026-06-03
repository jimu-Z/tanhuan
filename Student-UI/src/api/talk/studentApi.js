import request from '@/utils/request'

export function login(username, password, code, uuid) {
  const data = { username, password, code, uuid }
  return request({
    url: '/login',
    headers: { isToken: false },
    method: 'post',
    data: data
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: { isToken: false },
    method: 'get',
    timeout: 20000
  })
}

export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

export function getMyRecords(params) {
  return request({
    url: '/ruoyi-system/talkrecord/myRecords',
    method: 'get',
    params: params
  })
}

export function updateRecord(data) {
  return request({
    url: '/ruoyi-system/talkrecord',
    method: 'put',
    data: data
  })
}

export function submitFeedback(data) {
  return request({
    url: '/ruoyi-system/talkrecord/submitFeedback',
    method: 'put',
    data: data
  })
}

export function getPendingTalks(params) {
  return request({
    url: '/ruoyi-system/talkrecord/myRecords',
    method: 'get',
    params: params
  })
}
