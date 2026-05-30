import request from '@/utils/request'

export function getDashboard() {
  return request({ url: '/ruoyi-system/talk/statistics/dashboard', method: 'get' })
}

export function getCharts() {
  return request({ url: '/ruoyi-system/talk/statistics/charts', method: 'get' })
}

export function getAlerts() {
  return request({ url: '/ruoyi-system/talk/statistics/alerts', method: 'get' })
}

export function getBigscreen() {
  return request({ url: '/ruoyi-system/talk/statistics/bigscreen', method: 'get' })
}