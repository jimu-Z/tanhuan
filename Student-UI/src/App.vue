<template>
  <div id="app">
    <el-container v-if="$route.path !== '/login'">
      <el-header class="student-header">
        <div class="header-left">
          <h2>学生谈心谈话系统</h2>
        </div>
        <div class="header-right">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="header-badge">
            <i class="el-icon-bell" style="font-size: 22px; cursor: pointer;" @click="$router.push('/pending')"></i>
          </el-badge>
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <i class="el-icon-user-solid"></i>
              {{ user.nickName || user.name || '学生' }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="index">首页</el-dropdown-item>
              <el-dropdown-item command="talks">我的谈话记录</el-dropdown-item>
              <el-dropdown-item command="pending">待处理谈话</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script>
import { getPendingTalks } from '@/api/talk/studentApi'

export default {
  name: 'App',
  computed: {
    user() {
      return this.$store.state.user
    },
    pendingCount() {
      return this.$store.state.pendingCount
    }
  },
  watch: {
    '$store.state.token': {
      handler(val) {
        if (val) {
          this.fetchPendingCount()
        }
      },
      immediate: true
    }
  },
  methods: {
    fetchPendingCount() {
      getPendingTalks({ studentFeedback: '无', notified: 1, pageNum: 1, pageSize: 1 }).then(res => {
        this.$store.commit('SET_PENDING_COUNT', res.total || 0)
      }).catch(err => {
        console.error('获取待处理数量失败:', err)
      })
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('logout').then(() => {
          this.$router.push('/login')
        }).catch(err => {
          console.error('登出失败:', err)
          this.$router.push('/login')
        })
      } else {
        this.$router.push('/' + command)
      }
    }
  }
}
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.student-header {
  background-color: #409EFF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px !important;
  line-height: 60px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-badge {
  margin-right: 10px;
}

.header-badge .el-icon-bell {
  color: #fff;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.el-main {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.el-container {
  height: 100%;
}
</style>
