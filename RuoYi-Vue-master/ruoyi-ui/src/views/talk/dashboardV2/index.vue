<template>
  <div class="dashboard-root" v-loading="loading">
    <div class="dashboard-hero">
      <h2 class="dashboard-hero-title">数据总览</h2>
      <p class="dashboard-hero-sub">一站式查看学生谈心谈话核心数据</p>
    </div>

    <div class="dashboard-kpi-row">
      <div v-for="kpi in kpiData" :key="kpi.key" class="dashboard-kpi-card" :style="{ borderTop: '3px solid ' + kpi.color }">
        <span class="dashboard-kpi-label">{{ kpi.label }}</span>
        <div class="dashboard-kpi-value">
          <span class="dashboard-kpi-number">{{ kpi.value }}</span>
          <span class="dashboard-kpi-unit">{{ kpi.unit }}</span>
        </div>
      </div>
    </div>

    <div class="dashboard-charts">
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-collection-tag"></i> 谈话内容标签分布
        </div>
        <div ref="tagPieChart" class="dashboard-chart-body"></div>
      </div>
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-data-line"></i> 月度谈话趋势
        </div>
        <div ref="monthlyBarChart" class="dashboard-chart-body"></div>
      </div>
    </div>

    <div class="dashboard-bottom">
      <!-- 工作提醒 -->
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-bell"></i> 工作提醒
        </div>
        <div class="dashboard-todo-wrap">
          <div v-if="todoItems.length > 0" class="dashboard-todo-list">
            <div v-for="item in todoItems" :key="item.type" class="dashboard-todo-item" @click="handleTodoClick(item)">
              <div class="dashboard-todo-icon" :style="{ background: item.color + '18', color: item.color }">
                <i :class="item.icon"></i>
              </div>
              <div class="dashboard-todo-info">
                <span class="dashboard-todo-label">{{ item.label }}</span>
                <span class="dashboard-todo-count" :style="{ color: item.color }">{{ item.count }}</span>
              </div>
              <i class="el-icon-arrow-right dashboard-todo-arrow"></i>
            </div>
          </div>
          <div v-else class="dashboard-empty">
            <i class="el-icon-circle-check" style="font-size:28px;color:#67c23a;margin-bottom:8px;display:block"></i>
            暂无待处理事项
          </div>
        </div>
      </div>

      <!-- 预警概览 -->
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-warning"></i> 预警概览
        </div>
        <div class="dashboard-alert-wrap">
          <div v-if="alertItems.length > 0" class="dashboard-alert-list">
            <div v-for="item in alertItems" :key="item.type" class="dashboard-alert-item">
              <div class="dashboard-alert-header">
                <span class="dashboard-alert-dot" :style="{ background: item.color }"></span>
                <span class="dashboard-alert-label">{{ item.label }}</span>
                <span class="dashboard-alert-count" :style="{ color: item.color }">{{ item.count }}人</span>
              </div>
              <div class="dashboard-alert-detail">
                <span v-for="d in formatAlertDetail(item)" :key="d.label" class="dashboard-alert-tag">
                  {{ d.label }} {{ d.count }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="dashboard-empty">
            <i class="el-icon-circle-check" style="font-size:28px;color:#67c23a;margin-bottom:8px;display:block"></i>
            暂无预警信息
          </div>
        </div>
      </div>

      <!-- 最近动态 -->
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-time"></i> 最近谈话动态
        </div>
        <div class="dashboard-recent-wrap">
          <div v-if="recentActivities.length > 0" class="dashboard-recent-list">
            <div v-for="(act, idx) in recentActivities" :key="idx" class="dashboard-recent-item">
              <div class="dashboard-recent-dot" :class="act.talkType === 'group' ? 'dot-group' : 'dot-individual'"></div>
              <div class="dashboard-recent-info">
                <div class="dashboard-recent-top">
                  <span class="dashboard-recent-name">{{ act.studentName }}</span>
                  <el-tag :type="act.talkType === 'group' ? 'warning' : 'primary'" size="mini" effect="plain">
                    {{ act.talkType === 'group' ? '集体' : '个别' }}
                  </el-tag>
                  <el-tag v-if="act.followupStatus === 'pending'" type="danger" size="mini" effect="plain">待跟进</el-tag>
                </div>
                <div class="dashboard-recent-meta">
                  <span v-if="act.talkPerson">{{ act.talkPerson }}</span>
                  <span v-if="act.talkTime">{{ formatDate(act.talkTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="dashboard-empty">暂无谈话记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboard } from '@/api/talk/talkStatistics'

const KPI_CONFIG = [
  { key: 'totalStudents', label: '学生总数', unit: '人', color: '#409eff' },
  { key: 'totalSessions', label: '谈话场次', unit: '场', color: '#667eea' },
  { key: 'totalRecords', label: '记录总数', unit: '条', color: '#11998e' },
  { key: 'avgRecordsPerStudent', label: '人均记录', unit: '条/人', color: '#ee5a6f' },
  { key: 'individualCount', label: '个人谈话', unit: '场', color: '#764ba2' },
  { key: 'groupCount', label: '集体谈话', unit: '场', color: '#e6a23c' },
  { key: 'coverageRate', label: '覆盖率', unit: '%', color: '#f5576c' },
  { key: 'pendingFeedback', label: '待反馈', unit: '条', color: '#f093fb' }
]

const TAG_COLORS = ['#667eea','#764ba2','#11998e','#38ef7d','#ee5a6f','#f7ba2a','#409eff','#a18cd1']

const MENTAL_LABELS = { 'weekly_track': '周跟踪', 'monthly_track': '月跟踪', '重点关注': '重点关注', '中度预警': '中度预警', '关注': '关注' }
const POVERTY_LABELS = { 'general': '一般困难', 'difficult': '困难', 'severe': '特别困难', '贫困': '贫困', '轻度贫困': '轻度贫困', '一般困难': '一般困难' }
const ENROLLMENT_LABELS = { 'suspended': '休学', 'withdrawn': '退学', '休学': '休学', '退学': '退学' }

export default {
  name: 'DashboardV2',
  data() {
    return {
      loading: false,
      chartInstances: {},
      dashboardData: {},
      tagDistribution: [],
      monthlyTrend: [],
      todoItems: [],
      alertItems: [],
      recentActivities: []
    }
  },
  computed: {
    kpiData() {
      const d = this.dashboardData
      return KPI_CONFIG.map(k => ({ ...k, value: d[k.key] != null ? d[k.key] : 0 }))
    }
  },
  mounted() {
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.disposeAllCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    fetchData() {
      this.loading = true
      getDashboard()
        .then(res => {
          const data = res.data || {}
          this.dashboardData = data
          this.tagDistribution = data.tagDistribution || []
          this.monthlyTrend = data.monthlyTrend || []
          this.todoItems = data.todoItems || []
          this.alertItems = data.alertItems || []
          this.recentActivities = data.recentActivities || []

          this.$nextTick(() => this.renderAllCharts())
        })
        .catch(() => {
          this.$message.error('数据加载失败')
        })
        .finally(() => {
          this.loading = false
        })
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      return m + '-' + day + ' ' + h + ':' + min
    },

    formatAlertDetail(item) {
      if (!item.detail) return []
      const labelMap = item.type === 'mental' ? MENTAL_LABELS : item.type === 'poverty' ? POVERTY_LABELS : ENROLLMENT_LABELS
      return item.detail.map(d => ({
        label: labelMap[d.status || d.level] || d.status || d.level,
        count: d.cnt + '人'
      }))
    },

    handleTodoClick(item) {
      if (item.type === 'feedback') {
        this.$router.push('/talk/record?hasNoFeedback=true')
      } else if (item.type === 'followup') {
        this.$router.push('/talk/record?followupStatus=pending')
      } else if (item.type === 'untalked') {
        this.$router.push('/talk/student?untalked=true')
      }
    },

    handleResize() {
      Object.values(this.chartInstances).forEach(chart => {
        if (chart && !chart.isDisposed()) chart.resize()
      })
    },

    initChart(refName) {
      if (this.chartInstances[refName] && !this.chartInstances[refName].isDisposed()) {
        this.chartInstances[refName].dispose()
      }
      const dom = this.$refs[refName]
      if (!dom) return null
      const chart = echarts.init(dom)
      this.chartInstances[refName] = chart
      return chart
    },

    disposeAllCharts() {
      Object.keys(this.chartInstances).forEach(k => {
        if (this.chartInstances[k] && !this.chartInstances[k].isDisposed()) {
          this.chartInstances[k].dispose()
        }
      })
      this.chartInstances = {}
    },

    renderAllCharts() {
      this.renderTagPieChart()
      this.renderMonthlyBarChart()
    },

    renderTagPieChart() {
      const chart = this.initChart('tagPieChart')
      if (!chart) return
      const data = this.tagDistribution.filter(t => t.value > 0)
      if (!data.length) {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } } }, true)
        return
      }
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
        legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
        color: TAG_COLORS,
        series: [{ type: 'pie', radius: ['48%','72%'], center: ['50%','45%'], itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }, label: { show: false }, emphasis: { label: { show: true } }, data }]
      }, true)
    },

    renderMonthlyBarChart() {
      const chart = this.initChart('monthlyBarChart')
      if (!chart) return
      const trend = this.monthlyTrend
      if (!trend.length) {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } } }, true)
        return
      }
      const months = trend.map(d => d.month)
      const counts = trend.map(d => d.count)
      chart.setOption({
        tooltip: { trigger: 'axis', formatter(p) { return p[0].name + '<br/>谈话次数: <b>' + p[0].value + '</b> 次' } },
        grid: { top: 16, left: 8, right: 8, bottom: 12, containLabel: true },
        xAxis: { type: 'category', data: months, axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', axisLabel: { fontSize: 10 }, splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } } },
        series: [{ type: 'bar', barWidth: 18, data: counts, itemStyle: { borderRadius: [6,6,0,0], color: new echarts.graphic.LinearGradient(0,0,0,1,[{ offset:0, color:'#667eea' },{ offset:0.5, color:'#764ba2' },{ offset:1, color:'#a18cd1' }]) } }]
      }, true)
    }
  }
}
</script>

<style scoped>
.dashboard-root { background: #f8f9fc; min-height: 100vh; padding: 0 0 40px; }

.dashboard-hero { text-align: center; padding: 36px 24px 24px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
.dashboard-hero-title { font-size: 24px; font-weight: 700; margin: 0 0 6px; }
.dashboard-hero-sub { font-size: 13px; opacity: 0.85; margin: 0; }

.dashboard-kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; padding: 20px 24px; margin-top: -20px; position: relative; z-index: 2; }
.dashboard-kpi-card { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); text-align: center; }
.dashboard-kpi-label { font-size: 12px; color: #909399; display: block; margin-bottom: 6px; }
.dashboard-kpi-value { display: flex; align-items: baseline; justify-content: center; gap: 3px; }
.dashboard-kpi-number { font-size: 26px; font-weight: 700; color: #1a1a2e; }
.dashboard-kpi-unit { font-size: 11px; color: #909399; }

.dashboard-charts { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; padding: 0 24px 20px; }
.dashboard-bottom { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; padding: 0 24px; }

.dashboard-chart-box { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden; }
.dashboard-chart-header { font-size: 14px; font-weight: 600; color: #1a1a2e; padding: 14px 20px; border-bottom: 1px solid #f0f2f5; display: flex; align-items: center; gap: 6px; }
.dashboard-chart-body { width: 100%; height: 340px; }

.dashboard-empty { text-align: center; color: #909399; padding: 40px; font-size: 13px; }

/* 工作提醒 */
.dashboard-todo-wrap { padding: 16px 20px; }
.dashboard-todo-list { display: flex; flex-direction: column; gap: 12px; }
.dashboard-todo-item { display: flex; align-items: center; gap: 14px; padding: 14px 16px; background: #f8f9fc; border-radius: 10px; cursor: pointer; transition: all 0.2s; }
.dashboard-todo-item:hover { background: #f0f2f5; transform: translateX(4px); }
.dashboard-todo-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.dashboard-todo-info { flex: 1; display: flex; align-items: center; justify-content: space-between; }
.dashboard-todo-label { font-size: 14px; color: #303133; }
.dashboard-todo-count { font-size: 20px; font-weight: 700; }
.dashboard-todo-arrow { color: #c0c4cc; font-size: 14px; }

/* 预警概览 */
.dashboard-alert-wrap { padding: 16px 20px; }
.dashboard-alert-list { display: flex; flex-direction: column; gap: 16px; }
.dashboard-alert-item { padding: 14px 16px; background: #f8f9fc; border-radius: 10px; }
.dashboard-alert-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.dashboard-alert-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dashboard-alert-label { font-size: 14px; color: #303133; flex: 1; }
.dashboard-alert-count { font-size: 16px; font-weight: 700; }
.dashboard-alert-detail { display: flex; flex-wrap: wrap; gap: 8px; padding-left: 16px; }
.dashboard-alert-tag { font-size: 12px; color: #606266; background: #fff; padding: 4px 10px; border-radius: 6px; border: 1px solid #ebeef5; }

/* 最近动态 */
.dashboard-recent-wrap { padding: 16px 20px; }
.dashboard-recent-list { display: flex; flex-direction: column; }
.dashboard-recent-item { display: flex; align-items: flex-start; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f7fa; }
.dashboard-recent-item:last-child { border-bottom: none; }
.dashboard-recent-dot { width: 8px; height: 8px; border-radius: 50%; margin-top: 7px; flex-shrink: 0; }
.dot-individual { background: #409eff; }
.dot-group { background: #e6a23c; }
.dashboard-recent-info { flex: 1; min-width: 0; }
.dashboard-recent-top { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.dashboard-recent-name { font-size: 13px; color: #303133; font-weight: 500; }
.dashboard-recent-meta { font-size: 12px; color: #909399; display: flex; gap: 12px; }
</style>
