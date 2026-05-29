import request from '@/utils/request'

export function createTalk(data) {
  return request({
    url: '/ruoyi-system/talksession/create',
    method: 'post',
    data: data
  })
}
