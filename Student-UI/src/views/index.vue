<template>
  <div class="home-page">
    <h3 class="page-title">欢迎回来，{{ user.nickName || user.name || '同学' }}</h3>
    <el-row :gutter="20" class="card-row">
      <el-col :span="8">
        <el-card class="home-card" shadow="hover" @click.native="$router.push('/talks')">
          <div class="card-content">
            <i class="el-icon-document card-icon" style="color: #409EFF;"></i>
            <h4 class="card-title">我的谈话记录</h4>
            <p class="card-desc">查看所有历史谈话记录，了解谈话详情</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="home-card" shadow="hover" @click.native="$router.push('/pending')">
          <div class="card-content">
            <i class="el-icon-bell card-icon" style="color: #E6A23C;"></i>
            <h4 class="card-title">待处理谈话</h4>
            <p class="card-desc">
              你有
              <span class="pending-num">{{ pendingCount }}</span>
              条待处理的谈话记录
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="home-card" shadow="hover">
          <div class="card-content">
            <i class="el-icon-user-solid card-icon" style="color: #67C23A;"></i>
            <h4 class="card-title">个人中心</h4>
            <p class="card-desc">查看和管理个人基本信息</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getPendingTalks } from '@/api/talk/studentApi'

export default {
  name: 'StudentHome',
  computed: {
    user() {
      return this.$store.state.user
    },
    pendingCount() {
      return this.$store.state.pendingCount
    }
  },
  created() {
  },
  methods: {
    fetchPendingCount() {
      getPendingTalks({
        studentFeedback: '无',
        notified: 1,
        pageNum: 1,
        pageSize: 1
      }).then(res => {
        this.$store.commit('SET_PENDING_COUNT', res.total || 0)
      }).catch(err => {
        console.error('获取待处理数量失败:', err)
      })
    }
  }
}
</script>

<style scoped>
.home-page {
  padding: 10px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 24px;
}

.card-row {
  margin-top: 10px;
}

.home-card {
  cursor: pointer;
  text-align: center;
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.home-card:hover {
  transform: translateY(-4px);
}

.card-content {
  padding: 20px 0;
}

.card-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 10px 0;
}

.card-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.pending-num {
  color: #F56C6C;
  font-weight: bold;
  font-size: 18px;
}
</style>
