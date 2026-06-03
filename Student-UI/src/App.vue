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
      getPendingTalks({ hasNoFeedback: true, notified: 1, pageNum: 1, pageSize: 1 }).then(res => {
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
  background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px !important;
  line-height: 60px;
  box-shadow: 0 2px 8px rgba(26, 82, 118, 0.2);
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #fff;
  font-weight: 600;
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
  transition: transform 0.2s;
}

.header-badge .el-icon-bell:hover {
  transform: scale(1.1);
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
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.el-main {
  background: linear-gradient(160deg, #f0f5fa 0%, #e8edf2 100%);
  min-height: calc(100vh - 60px);
  padding: 0;
}

.el-container {
  height: 100%;
}
</style>
