import request from '@/utils/request'

export function getCollegeStatistics(query) {
  return request({
    url: '/conversation/statistics/college',
    method: 'get',
    params: query
  })
}

export function getCounselorStatistics(query) {
  return request({
    url: '/conversation/statistics/counselor',
    method: 'get',
    params: query
  })
}

export function getPendingFollowUps() {
  return request({
    url: '/conversation/statistics/pending',
    method: 'get'
  })
}