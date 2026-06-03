import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/',
    redirect: '/index',
    hidden: true
  },
  {
    path: '/index',
    component: () => import('@/views/index'),
    name: 'Index',
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/talks',
    component: () => import('@/views/talks/index'),
    name: 'Talks',
    meta: { title: '我的谈话记录', requiresAuth: true }
  },
  {
    path: '/pending',
    component: () => import('@/views/pending/index'),
    name: 'Pending',
    meta: { title: '待处理谈话', requiresAuth: true }
  }
]

const routerPush = Router.prototype.push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}

const createRouter = () => new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

export default router
