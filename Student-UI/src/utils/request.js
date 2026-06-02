import axios from 'axios'
import { MessageBox, Message } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 10000
})

service.interceptors.request.use(config => {
  if (getToken()) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }
  return config
}, error => {
  console.log(error)
  return Promise.reject(error)
})

service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = res.data.msg || '系统错误'

  if (code === 401) {
    MessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      store.dispatch('logout').then(() => {
        location.reload()
      })
    }).catch(() => {})
    return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
  } else if (code === 500) {
    Message({ message: msg, type: 'error' })
    return Promise.reject(new Error(msg))
  } else if (code !== 200) {
    Message({ message: msg, type: 'error' })
    return Promise.reject('error')
  } else {
    return res.data
  }
}, error => {
  console.log('err' + error)
  let { message } = error
  if (message === 'Network Error') {
    message = '后端接口连接异常'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时'
  } else if (message.includes('Request failed with status code')) {
    message = '系统接口' + message.slice(-3) + '异常'
  }
  Message({ message: message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

export default service
