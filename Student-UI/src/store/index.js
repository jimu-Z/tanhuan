import Vue from 'vue'
import Vuex from 'vuex'
import { login, getInfo } from '@/api/talk/studentApi'
import { getToken, setToken, removeToken } from '@/utils/auth'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    token: getToken(),
    pendingCount: 0,
    user: {
      name: '',
      nickName: '',
      userId: null,
      studentId: null,
      studentCode: '',
      deptName: '',
      phonenumber: '',
      politicalStatus: ''
    }
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
    },
    SET_USER(state, user) {
      state.user = user
    },
    SET_PENDING_COUNT(state, count) {
      state.pendingCount = count
    }
  },

  actions: {
    login({ commit }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const code = userInfo.code || ''
      const uuid = userInfo.uuid || ''
      return new Promise((resolve, reject) => {
        login(username, password, code, uuid).then(res => {
          setToken(res.token)
          commit('SET_TOKEN', res.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    getUserInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user
          const params = user.params || {}
          commit('SET_USER', {
            name: user.userName,
            nickName: user.nickName || user.userName,
            userId: user.userId,
            studentId: params.studentId || user.studentId || null,
            studentCode: params.studentCode || user.studentCode || user.userName,
            deptName: params.deptName || user.deptName || '',
            phonenumber: user.phonenumber || params.phonenumber || '',
            politicalStatus: params.politicalStatus || user.politicalStatus || ''
          })
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    logout({ commit }) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_USER', {
          name: '',
          nickName: '',
          userId: null,
          studentId: null,
          studentCode: '',
          deptName: '',
          phonenumber: '',
          politicalStatus: ''
        })
        removeToken()
        resolve()
      })
    }
  }
})

export default store
