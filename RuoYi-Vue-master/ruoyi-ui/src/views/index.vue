<template>
  <div class="home-wrapper">
    <!-- 1. 欢迎横幅 -->
    <div class="banner">
      <div class="banner-inner">
        <div class="banner-left">
          <div class="banner-avatar">
            <span class="banner-avatar-text">{{ userName.charAt(0) }}</span>
          </div>
          <div class="banner-greeting">
            <h1 class="banner-hi">{{ greeting }}，{{ userName }}</h1>
            <p class="banner-sub">{{ roleLabel }} · {{ dateStr }} · 用心沟通，以情育人</p>
          </div>
        </div>
        <div class="banner-right">
          <div class="banner-motto">
            <span class="banner-motto-icon">💬</span>
            <span>每一次谈话，都是一次心灵的靠近</span>
          </div>
        </div>
      </div>
      <div class="banner-wave">
        <svg viewBox="0 0 1200 80" preserveAspectRatio="none">
          <path d="M0,40 C200,80 400,0 600,40 C800,80 1000,0 1200,40 L1200,80 L0,80 Z" fill="#fffcf7" opacity="0.6"/>
          <path d="M0,50 C200,70 400,20 600,50 C800,80 1000,20 1200,50 L1200,80 L0,80 Z" fill="#fffcf7" opacity="0.3"/>
        </svg>
      </div>
    </div>

    <!-- 2. KPI 数据胶囊 -->
    <div class="kpi-strip">
      <div class="kpi-capsule" v-for="(kpi, idx) in kpiItems" :key="kpi.key" :style="{ animationDelay: idx * 0.08 + 's' }">
        <div class="kpi-capsule-icon" :style="{ background: kpi.iconBg }">
          <i :class="kpi.icon" :style="{ color: kpi.iconColor }"></i>
        </div>
        <div class="kpi-capsule-body">
          <span class="kpi-capsule-val">{{ kpi.value }}</span>
          <span class="kpi-capsule-unit">{{ kpi.unit }}</span>
        </div>
        <span class="kpi-capsule-label">{{ kpi.label }}</span>
      </div>
    </div>

    <!-- 3. 待办提醒区 -->
    <div class="alerts-section">
      <div class="section-header">
        <h3 class="section-title">
          <i class="el-icon-bell" style="color: #e6a23c; margin-right: 8px;"></i>待办提醒
        </h3>
        <el-tag v-if="totalAlerts > 0" type="warning" effect="plain" size="small">{{ totalAlerts }} 项待处理</el-tag>
        <el-tag v-else type="success" effect="plain" size="small">全部完成</el-tag>
      </div>
      <div class="alerts-cards">
        <div class="alert-card alert-pending">
          <div class="alert-card-num">{{ alertsData.pendingFollowups || 0 }}</div>
          <div class="alert-card-text">
            <span class="alert-card-title">待跟进谈话</span>
            <span class="alert-card-desc">需要安排后续跟进</span>
          </div>
          <div class="alert-card-dot" style="background: #e6a23c;"></div>
        </div>
        <div class="alert-card alert-progress">
          <div class="alert-card-num">{{ alertsData.inProgressFollowups || 0 }}</div>
          <div class="alert-card-text">
            <span class="alert-card-title">跟进中</span>
            <span class="alert-card-desc">正在处理中的记录</span>
          </div>
          <div class="alert-card-dot" style="background: #409eff;"></div>
        </div>
        <div class="alert-card alert-urgent" v-if="alertsData.pendingFollowups > 0">
          <div class="alert-card-icon-wrap">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="alert-card-text">
            <span class="alert-card-title">建议优先处理</span>
            <span class="alert-card-desc">有 {{ alertsData.pendingFollowups }} 位学生等待跟进</span>
          </div>
          <div class="alert-card-dot" style="background: #f56c6c;"></div>
        </div>
      </div>
    </div>

    <!-- 5. 功能快捷入口 -->
    <div class="func-section">
      <div class="section-header">
        <h3 class="section-title">
          <i class="el-icon-s-grid" style="color: #7b9e87; margin-right: 8px;"></i>功能入口
        </h3>
      </div>
      <div class="func-grid">
        <div class="func-card" v-for="(card, i) in cards" :key="card.title"
             @click="$router.push(card.path)">
          <div class="func-card-icon" :style="{ background: card.gradient }">
            <span class="func-card-num">{{ '0' + (i + 1) }}</span>
          </div>
          <div class="func-card-content">
            <h4 class="func-card-title">{{ card.title }}</h4>
            <p class="func-card-desc">{{ card.desc }}</p>
          </div>
          <i class="el-icon-arrow-right func-card-arrow"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import auth from '@/plugins/auth'
import { getDashboard, getAlerts } from '@/api/talk/talkStatistics'

const KPI_CONFIG = [
  { key: 'totalStudents', label: '学生总数', unit: '人', icon: 'el-icon-user', iconBg: 'rgba(200,150,102,0.15)', iconColor: '#c89666' },
  { key: 'totalSessions', label: '谈话场次', unit: '场', icon: 'el-icon-s-order', iconBg: 'rgba(123,158,135,0.15)', iconColor: '#7b9e87' },
  { key: 'totalRecords', label: '记录总数', unit: '条', icon: 'el-icon-document', iconBg: 'rgba(143,121,100,0.15)', iconColor: '#8f7964' },
  { key: 'avgRecordsPerStudent', label: '人均记录', unit: '条/人', icon: 'el-icon-data-analysis', iconBg: 'rgba(230,162,60,0.15)', iconColor: '#e6a23c' },
  { key: 'individualCount', label: '个人谈话', unit: '场', icon: 'el-icon-user-solid', iconBg: 'rgba(164,133,109,0.15)', iconColor: '#a4856d' },
  { key: 'groupCount', label: '集体谈话', unit: '场', icon: 'el-icon-s-grid', iconBg: 'rgba(64,158,255,0.15)', iconColor: '#409eff' }
]

export default {
  name: 'Index',
  data() {
    const isSecretary = auth.hasRole('admin') || auth.hasRole('talk_secretary')
    const roleLabel = isSecretary ? '书记工作台' : '辅导员工作台'

    const secretaryCards = [
      { title: '学生信息管理', desc: '导入台账、查看学生信息、管理学生数据', path: '/talk/talkStudent/index', accent: '#c89666', gradient: 'linear-gradient(135deg, #c89666, #e0b88a)' },
      { title: '发起谈话', desc: '创建个别/集体谈话，选择谈话类型和内容标签', path: '/talk/talkInitiate/index', accent: '#7b9e87', gradient: 'linear-gradient(135deg, #7b9e87, #a3c4a9)' },
      { title: '统计分析', desc: '仪表盘视图，查看谈话数据分布与趋势', path: '/talk/dashboardV2/index', accent: '#5a7d6c', gradient: 'linear-gradient(135deg, #5a7d6c, #7b9e87)' },
      { title: '预警提醒', desc: '超期未谈话、心理健康跟踪等自动告警', path: '/talk/alertsV2/index', accent: '#e6a23c', gradient: 'linear-gradient(135deg, #f0b44d, #f08080)' },
      { title: '数据大屏', desc: '全屏数据可视化，适用于大屏幕投放', path: '/talk/bigscreenV2/index', accent: '#667eea', gradient: 'linear-gradient(135deg, #667eea, #8b7fcf)' },
      { title: '统一查询', desc: '合并会话/记录的多维度条件查询', path: '/talk/unifiedQuery/index', accent: '#8f7964', gradient: 'linear-gradient(135deg, #8f7964, #a4856d)' }
    ]

    const counselorCards = [
      { title: '发起谈话', desc: '创建个别/集体谈话，选择谈话类型和内容标签', path: '/talk/talkInitiate/index', accent: '#7b9e87', gradient: 'linear-gradient(135deg, #7b9e87, #a3c4a9)' },
      { title: '我的记录', desc: '快速查看自己创建的谈话历史与状态', path: '/talk/my-records', accent: '#5a7d6c', gradient: 'linear-gradient(135deg, #5a7d6c, #7b9e87)' },
      { title: '学生管理', desc: '查看管辖范围内的学生基本信息', path: '/talk/talkStudent/index', accent: '#c89666', gradient: 'linear-gradient(135deg, #c89666, #e0b88a)' },
      { title: '预警提醒', desc: '待跟进谈话、异常情况的自动提醒', path: '/talk/alertsV2/index', accent: '#e6a23c', gradient: 'linear-gradient(135deg, #f0b44d, #f08080)' },
      { title: '谈话模板库', desc: '使用系统模板快速填充谈话内容', path: '/talk/templatesV2/index', accent: '#667eea', gradient: 'linear-gradient(135deg, #667eea, #8b7fcf)' },
      { title: '会话管理', desc: '查看、编辑、导出历史谈话会话', path: '/talk/talkSession/index', accent: '#8f7964', gradient: 'linear-gradient(135deg, #8f7964, #a4856d)' }
    ]

    return {
      roleLabel,
      isSecretary,
      cards: isSecretary ? secretaryCards : counselorCards,
      dashboardData: {},
      alertsData: {},
      loading: false
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
    },
    totalAlerts() {
      return (this.alertsData.pendingFollowups || 0) + (this.alertsData.inProgressFollowups || 0)
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
      Promise.all([getDashboard(), getAlerts()])
        .then(([dashRes, alertsRes]) => {
          this.dashboardData = dashRes.data || {}
          this.alertsData = alertsRes.data || {}
        })
        .catch(() => {
          this.$message.error('数据加载失败')
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-wrapper {
  min-height: calc(100vh - 100px);
  padding: 0 24px 40px;
  background: linear-gradient(180deg, #fef8f0 0%, #fffcf7 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  box-sizing: border-box;
}

/* ==================== 1. 欢迎横幅 ==================== */
.banner {
  background: linear-gradient(135deg, #fef8f0 0%, #fdf2e4 40%, #faf0e0 100%);
  border-radius: 20px;
  margin: 20px 0 24px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 24px rgba(139, 109, 82, 0.08);
}
.banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32px 36px 24px;
  position: relative;
  z-index: 1;
}
.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.banner-avatar {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: linear-gradient(135deg, #c89666, #d4a574);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(200, 150, 102, 0.25);
  flex-shrink: 0;
}
.banner-avatar-text {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}
.banner-hi {
  font-size: 26px;
  font-weight: 700;
  color: #3d3027;
  margin: 0 0 6px;
  letter-spacing: 1px;
}
.banner-sub {
  font-size: 13px;
  color: #8c7b6b;
  margin: 0;
  letter-spacing: 1px;
}
.banner-right {
  text-align: right;
}
.banner-motto {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 12px 20px;
  font-size: 14px;
  color: #8c7b6b;
  border: 1px solid rgba(200, 150, 102, 0.15);
  display: flex;
  align-items: center;
  gap: 8px;
}
.banner-motto-icon {
  font-size: 18px;
}
.banner-wave {
  height: 40px;
  position: relative;
}
.banner-wave svg {
  width: 100%;
  height: 100%;
}

/* ==================== 2. KPI 数据胶囊 ==================== */
.kpi-strip {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}
.kpi-capsule {
  background: #fffdf9;
  border-radius: 16px;
  padding: 18px 16px;
  text-align: center;
  border: 1px solid #efe4d6;
  box-shadow: 0 2px 12px rgba(139, 109, 82, 0.05);
  transition: all 0.3s ease;
  animation: fadeUp 0.5s ease both;
  position: relative;
  overflow: hidden;
}
.kpi-capsule:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(139, 109, 82, 0.1);
}
.kpi-capsule-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 18px;
}
.kpi-capsule-body {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
}
.kpi-capsule-val {
  font-size: 26px;
  font-weight: 700;
  color: #3d3027;
  line-height: 1;
}
.kpi-capsule-unit {
  font-size: 11px;
  color: #8c7b6b;
}
.kpi-capsule-label {
  display: block;
  font-size: 11px;
  color: #8c7b6b;
  margin-top: 4px;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ==================== 3. 待办提醒区 ==================== */
.alerts-section {
  margin-bottom: 24px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #3d3027;
  margin: 0;
  display: flex;
  align-items: center;
}
.alerts-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.alert-card {
  background: #fffdf9;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #efe4d6;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(139, 109, 82, 0.05);
}
.alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 109, 82, 0.1);
}
.alert-card-dot {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.alert-card-num {
  font-size: 36px;
  font-weight: 800;
  color: #3d3027;
  line-height: 1;
  min-width: 50px;
}
.alert-card-icon-wrap {
  font-size: 28px;
  color: #f56c6c;
  min-width: 40px;
  text-align: center;
}
.alert-card-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.alert-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #3d3027;
}
.alert-card-desc {
  font-size: 12px;
  color: #8c7b6b;
}

/* ==================== 5. 功能快捷入口 ==================== */
.func-section {
  margin-bottom: 24px;
}
.func-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.func-card {
  background: #fffdf9;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  border: 1px solid #efe4d6;
  box-shadow: 0 2px 12px rgba(139, 109, 82, 0.05);
  transition: all 0.3s ease;
  position: relative;
}
.func-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(139, 109, 82, 0.1);
}
.func-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.func-card-num {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
}
.func-card-content {
  flex: 1;
  min-width: 0;
}
.func-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #3d3027;
  margin: 0 0 4px;
}
.func-card-desc {
  font-size: 12px;
  color: #8c7b6b;
  margin: 0;
  line-height: 1.5;
}
.func-card-arrow {
  color: #c0b5a8;
  font-size: 14px;
  flex-shrink: 0;
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 1100px) {
  .kpi-strip { grid-template-columns: repeat(3, 1fr); }
  .alerts-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .banner-inner { flex-direction: column; text-align: center; gap: 16px; }
  .banner-left { flex-direction: column; }
  .kpi-strip { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .alerts-cards { grid-template-columns: 1fr; }
  .func-grid { grid-template-columns: 1fr; }
}
</style>
