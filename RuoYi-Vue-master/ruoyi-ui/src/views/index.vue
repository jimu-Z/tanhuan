<template>
  <div class="app-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-left">
        <h2>{{ greeting }}，{{ userName }}</h2>
        <p>{{ roleLabel }} · {{ dateStr }}</p>
      </div>
      <div class="welcome-right">
        <span>用心沟通，以情育人</span>
      </div>
    </div>

    <!-- 核心数据 -->
    <el-row :gutter="20" class="mb20">
      <el-col :xs="24" :sm="12" :md="8" :lg="4" v-for="kpi in kpiItems" :key="kpi.key">
        <div class="kpi-box">
          <div class="kpi-icon" :style="{ color: kpi.iconColor }">
            <i :class="kpi.icon"></i>
          </div>
          <div class="kpi-info">
            <div class="kpi-value">{{ kpi.value }}</div>
            <div class="kpi-label">{{ kpi.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷功能 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <h3 class="section-title">快捷功能</h3>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="card in cards" :key="card.title">
        <div class="func-box" @click="$router.push(card.path)">
          <div class="func-icon" :style="{ background: card.gradient }">
            <i :class="card.icon"></i>
          </div>
          <div class="func-content">
            <h4>{{ card.title }}</h4>
            <p>{{ card.desc }}</p>
          </div>
          <i class="el-icon-arrow-right func-arrow"></i>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import auth from '@/plugins/auth'
import { getDashboard } from '@/api/talk/talkStatistics'

const KPI_CONFIG = [
  { key: 'totalStudents', label: '学生总数', icon: 'el-icon-user', iconColor: '#409eff' },
  { key: 'totalSessions', label: '谈话场次', icon: 'el-icon-s-order', iconColor: '#67c23a' },
  { key: 'totalRecords', label: '记录总数', icon: 'el-icon-document', iconColor: '#e6a23c' },
  { key: 'individualCount', label: '个人谈话', icon: 'el-icon-user-solid', iconColor: '#f56c6c' },
  { key: 'groupCount', label: '集体谈话', icon: 'el-icon-s-grid', iconColor: '#909399' },
  { key: 'avgRecordsPerStudent', label: '人均记录', icon: 'el-icon-data-analysis', iconColor: '#409eff' }
]

export default {
  name: 'Index',
  data() {
    const isAdmin = auth.hasRole('admin')
    const isSecretary = auth.hasRole('talk_secretary')
    const roleLabel = isAdmin || isSecretary ? '书记工作台' : '辅导员工作台'

    const commonCards = [
      { title: '发起谈话', desc: '创建个别/集体谈话', path: '/talk/initiate', icon: 'el-icon-plus', gradient: 'linear-gradient(135deg, #409eff, #66b1ff)' },
      { title: '学生信息管理', desc: '查看与管理学生数据', path: '/talk/student', icon: 'el-icon-user', gradient: 'linear-gradient(135deg, #67c23a, #85ce61)' },
      { title: '谈话管理', desc: '查看全部谈话记录', path: '/talk/talksession', icon: 'el-icon-s-order', gradient: 'linear-gradient(135deg, #e6a23c, #ebb563)' }
    ]

    const secretaryCards = [
      ...commonCards,
      { title: '教师管理', desc: '管理教师信息', path: '/talk/teacher', icon: 'el-icon-s-custom', gradient: 'linear-gradient(135deg, #f56c6c, #f78989)' }
    ]

    const counselorCards = [
      ...commonCards,
      { title: '我的谈话记录', desc: '查看自己创建的谈话', path: '/talk/myrecords', icon: 'el-icon-document', gradient: 'linear-gradient(135deg, #909399, #a6a9ad)' }
    ]

    return {
      roleLabel,
      isAdmin,
      isSecretary,
      cards: isAdmin || isSecretary ? secretaryCards : counselorCards,
      dashboardData: {}
    }
  },
  computed: {
    userName() {
      return (this.$store.state.user && this.$store.state.user.name) || '老师'
    },
    dateStr() {
      const d = new Date()
      return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日'
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了'
      if (h < 9) return '早上好'
      if (h < 12) return '上午好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      return '晚上好'
    },
    kpiItems() {
      const d = this.dashboardData
      return KPI_CONFIG.map(cfg => ({
        ...cfg,
        value: d[cfg.key] != null ? d[cfg.key] : '--'
      }))
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      getDashboard()
        .then(res => {
          this.dashboardData = res.data || {}
        })
        .catch(() => {
          this.$message.error('数据加载失败')
        })
    }
  }
}
</script>

<style scoped>
.welcome-banner {
  background: #fff;
  border-radius: 4px;
  padding: 20px 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #409eff;
}
.welcome-banner h2 {
  margin: 0 0 6px;
  font-size: 20px;
  color: #303133;
}
.welcome-banner p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.welcome-right {
  font-size: 14px;
  color: #606266;
}

.mb20 {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.kpi-box {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.04);
  transition: all 0.3s;
  margin-bottom: 20px;
}
.kpi-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.08);
}
.kpi-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
}
.kpi-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}
.kpi-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.func-box {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.04);
  transition: all 0.3s;
  margin-bottom: 20px;
}
.func-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.08);
}
.func-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-size: 20px;
}
.func-content {
  flex: 1;
  min-width: 0;
}
.func-content h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #303133;
}
.func-content p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
.func-arrow {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
