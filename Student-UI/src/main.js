import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App'
import store from './store'
import router from './router'

Vue.use(ElementUI, { size: 'medium' })

Vue.config.productionTip = false

const whiteList = ['/login']

router.beforeEach((to, from, next) => {
  if (whiteList.indexOf(to.path) !== -1) {
    next()
  } else {
    if (store.state.token) {
      if (!store.state.user.name) {
        store.dispatch('getUserInfo').then(() => {
          next()
        }).catch(() => {
          store.dispatch('logout').then(() => {
            next('/login')
          })
        })
      } else {
        next()
      }
    } else {
      next('/login')
    }
  }
})

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
