import request from '@/utils/request'

export function getDashboardData() {
  return request({
    url: '/conversation/dashboard/data',
    method: 'get'
  })
}

export function getRecentRecords() {
  return request({
    url: '/conversation/dashboard/recent',
    method: 'get'
  })
}