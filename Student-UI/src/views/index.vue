<template>
  <div class="home-page">
    <!-- 欢迎区 -->
    <div class="welcome-banner">
      <div class="welcome-left">
        <div class="welcome-avatar">
          <i class="el-icon-user"></i>
        </div>
        <div class="welcome-text">
          <h2 class="welcome-title">你好，{{ user.nickName || user.name || '同学' }} 👋</h2>
          <p class="welcome-subtitle">欢迎来到谈心谈话个人中心</p>
        </div>
      </div>
      <div class="welcome-right">
        <el-button class="quick-btn" @click="$router.push('/pending')">
          <i class="el-icon-bell"></i>
          <span>待处理</span>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-dot" />
        </el-button>
      </div>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-blue">
          <div class="stat-left">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
          </div>
          <div class="stat-right">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">总谈话次数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-orange">
          <div class="stat-left">
            <div class="stat-icon">
              <i class="el-icon-bell"></i>
            </div>
          </div>
          <div class="stat-right">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待处理谈话</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card stat-green">
          <div class="stat-left">
            <div class="stat-icon">
              <i class="el-icon-circle-check"></i>
            </div>
          </div>
          <div class="stat-right">
            <div class="stat-value">{{ doneCount }}</div>
            <div class="stat-label">已完成谈话</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷功能入口 -->
    <div class="section-title">快捷功能</div>
    <el-row :gutter="20" class="action-row">
      <el-col :xs="24" :sm="8">
        <div class="action-card" @click="$router.push('/talks')">
          <div class="action-icon action-icon-blue">
            <i class="el-icon-document"></i>
          </div>
          <div class="action-info">
            <h4>我的谈话记录</h4>
            <p>查看全部历史谈话记录</p>
          </div>
          <i class="el-icon-arrow-right action-arrow"></i>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="action-card" @click="$router.push('/pending')">
          <div class="action-icon action-icon-orange">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="action-info">
            <h4>待处理谈话</h4>
            <p>{{ pendingCount }} 条待反馈</p>
          </div>
          <i class="el-icon-arrow-right action-arrow"></i>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="action-card" @click="showProfile = true">
          <div class="action-icon action-icon-green">
            <i class="el-icon-user"></i>
          </div>
          <div class="action-info">
            <h4>个人中心</h4>
            <p>管理个人信息</p>
          </div>
          <i class="el-icon-arrow-right action-arrow"></i>
        </div>
      </el-col>
    </el-row>

    <!-- 最近谈话 -->
    <div class="section-title">最近谈话</div>
    <el-card class="recent-card" shadow="never">
      <div class="recent-list" v-loading="loading">
        <div v-for="(item, idx) in recentTalks" :key="item.recordId || idx" class="recent-item">
          <div class="recent-time">{{ formatTime(item.talkTime) }}</div>
          <div class="recent-dot"></div>
          <div class="recent-content">
            <div class="recent-header">
              <el-tag size="mini" :type="item.talkType === 'group' ? 'success' : ''">
                {{ getTalkTypeText(item.talkType) }}
              </el-tag>
              <span class="recent-location">
                <i class="el-icon-location"></i> {{ item.talkLocation || '-' }}
              </span>
              <span v-if="item.talkPerson" class="recent-person">
                <i class="el-icon-user"></i> {{ item.talkPerson }}
              </span>
            </div>
            <p class="recent-text">{{ truncateText(item.talkContent, 50) }}</p>
            <div class="recent-footer">
              <el-tag size="mini" :type="getStatusType(item.followupStatus)">
                {{ getStatusText(item.followupStatus) }}
              </el-tag>
              <span v-if="item.studentFeedback" class="feedback-hint">
                <i class="el-icon-chat-dot-round"></i> 已反馈
              </span>
            </div>
          </div>
        </div>
        <div v-if="recentTalks.length === 0 && !loading" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-chat-line-square"></i>
          </div>
          <p>暂无谈话记录</p>
        </div>
      </div>
    </el-card>

    <!-- 个人中心弹窗 -->
    <el-dialog title="个人信息" :visible.sync="showProfile" width="500px" center>
      <el-form label-width="100px">
        <el-form-item label="学号">
          <span>{{ user.studentCode || user.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="姓名">
          <span>{{ user.nickName || user.name || '-' }}</span>
        </el-form-item>
        <el-form-item label="班级">
          <span>{{ user.deptName || '-' }}</span>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { getPendingTalks, getMyRecords } from '@/api/talk/studentApi'

export default {
  name: 'StudentHome',
  data() {
    return {
      totalCount: 0,
      doneCount: 0,
      recentTalks: [],
      loading: false,
      showProfile: false
    }
  },
  computed: {
    user() {
      return this.$store.state.user
    },
    pendingCount() {
      return this.$store.state.pendingCount
    }
  },
  created() {
    this.fetchStats()
    this.fetchRecentTalks()
  },
  methods: {
    fetchStats() {
      getMyRecords({ pageNum: 1, pageSize: 1 }).then(res => {
        this.totalCount = res.total || 0
        this.doneCount = Math.max(0, this.totalCount - this.pendingCount)
      }).catch(() => {
        this.totalCount = 0
        this.doneCount = 0
      })
    },
    fetchRecentTalks() {
      this.loading = true
      getMyRecords({ pageNum: 1, pageSize: 5 }).then(res => {
        const rows = res.rows || []
        this.recentTalks = rows.slice(0, 5)
      }).catch(() => {
        this.recentTalks = []
      }).finally(() => {
        this.loading = false
      })
    },
    getStatusType(status) {
      const map = { pending: 'warning', in_progress: '', completed: 'success', none: 'info' }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = { pending: '待跟进', in_progress: '跟进中', completed: '已完成', none: '无需跟进' }
      return map[status] || '待跟进'
    },
    formatTime(time) {
      if (!time) return '-'
      if (typeof time === 'string' && time.length >= 10) {
        return time.substring(0, 10)
      }
      return time
    },
    getTalkTypeText(type) {
      const map = { individual: '个别谈话', group: '集体谈话' }
      return map[type] || type || '个别谈话'
    },
    truncateText(text, maxLen) {
      if (!text) return '暂无内容'
      return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
    }
  }
}
</script>

<style scoped>
.home-page {
  padding: 24px;
}

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 60%, #3a85c0 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(26, 82, 118, 0.18);
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.welcome-title {
  margin: 0 0 4px 0;
  font-size: 22px;
  color: #fff;
  font-weight: 600;
}

.welcome-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.quick-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #fff;
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.quick-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}

.badge-dot ::v-deep .el-badge__content {
  border: none;
  background: #f5a623;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-left {
  margin-right: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.stat-blue .stat-icon {
  background: linear-gradient(135deg, #e8f4fd 0%, #d1ecf9 100%);
  color: #2a6fa8;
}

.stat-orange .stat-icon {
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
  color: #f5a623;
}

.stat-green .stat-icon {
  background: linear-gradient(135deg, #e8f8f0 0%, #d1f2e1 100%);
  color: #52c41a;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a5276;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

/* 区块标题 */
.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid #2a6fa8;
  line-height: 1;
}

/* 快捷功能 */
.action-row {
  margin-bottom: 28px;
}

.action-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

@media (min-width: 768px) {
  .action-card {
    margin-bottom: 0;
  }
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-right: 16px;
  flex-shrink: 0;
}

.action-icon-blue {
  background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
  color: #fff;
}

.action-icon-orange {
  background: linear-gradient(135deg, #f5a623 0%, #f7c948 100%);
  color: #fff;
}

.action-icon-green {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.action-info {
  flex: 1;
}

.action-info h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.action-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.action-arrow {
  color: #c0c4cc;
  font-size: 18px;
  transition: all 0.3s;
}

.action-card:hover .action-arrow {
  color: #2a6fa8;
  transform: translateX(4px);
}

/* 最近谈话 */
.recent-card {
  border-radius: 16px;
  border: none;
  overflow: hidden;
}

.recent-list {
  padding: 8px 0;
}

.recent-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-time {
  font-size: 12px;
  color: #909399;
  min-width: 100px;
  flex-shrink: 0;
  padding-top: 2px;
}

.recent-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2a6fa8;
  margin: 8px 16px 0 0;
  flex-shrink: 0;
}

.recent-content {
  flex: 1;
}

.recent-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.recent-location {
  font-size: 12px;
  color: #909399;
}

.recent-location i {
  margin-right: 2px;
}

.recent-person {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.recent-person i {
  margin-right: 2px;
}

.recent-text {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.recent-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback-hint {
  font-size: 12px;
  color: #2a6fa8;
  margin-left: 8px;
}

.feedback-hint i {
  margin-right: 2px;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .home-page {
    padding: 16px;
  }

  .welcome-banner {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }

  .welcome-right {
    width: 100%;
  }

  .quick-btn {
    width: 100%;
    justify-content: center;
  }

  .stat-card {
    margin-bottom: 12px;
  }

  .recent-time {
    min-width: 70px;
    font-size: 11px;
  }
}
</style>
