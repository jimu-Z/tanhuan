<template>
  <div class="analytics-page" v-loading="loading">
    <section class="analytics-hero">
      <div>
        <span class="eyebrow">TALKING DATA CENTER</span>
        <h1>统计分析</h1>
        <p>{{ scopeLabel }}谈心谈话数据概览，为日常工作安排提供清晰依据。</p>
      </div>
      <div class="hero-actions">
        <span v-if="lastUpdated"><i class="el-icon-time"></i> 更新于 {{ lastUpdated }}</span>
        <el-button icon="el-icon-refresh" @click="fetchData">刷新数据</el-button>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="item in kpiData" :key="item.key" class="kpi-card">
        <div class="kpi-icon" :style="{ color: item.color, background: item.tint }"><i :class="item.icon"></i></div>
        <div class="kpi-copy">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
          <em>{{ item.note }}</em>
        </div>
      </article>
    </section>

    <section class="chart-grid">
      <article class="data-card">
        <div class="card-heading">
          <div><span>趋势观察</span><h2>近 12 个月谈话趋势</h2></div>
          <div class="type-summary">
            <span><i class="dot individual"></i>个别 {{ dashboardData.individualCount || 0 }}</span>
            <span><i class="dot group"></i>集体 {{ dashboardData.groupCount || 0 }}</span>
          </div>
        </div>
        <div ref="monthlyChart" class="chart-body"></div>
      </article>

      <article class="data-card">
        <div class="card-heading"><div><span>主题洞察</span><h2>谈话内容标签分布</h2></div></div>
        <div ref="tagChart" class="chart-body"></div>
      </article>
    </section>

    <section class="lower-grid">
      <article class="data-card coverage-card">
        <div class="card-heading"><div><span>覆盖情况</span><h2>{{ coverageTitle }}</h2></div><b>{{ dashboardData.totalColleges || 0 }} 个学院</b></div>
        <div v-if="collegeRanking.length" class="coverage-list">
          <div v-for="college in collegeRanking" :key="college.name" class="coverage-item">
            <div class="coverage-copy"><strong>{{ college.name }}</strong><span>{{ college.coveredCount || 0 }} / {{ college.count || 0 }} 人</span></div>
            <el-progress :percentage="percentage(college.coverageRate)" :stroke-width="8" :show-text="false" color="#168294" />
            <em>{{ percentage(college.coverageRate) }}%</em>
          </div>
        </div>
        <div v-else class="empty-state"><i class="el-icon-data-analysis"></i><p>暂无学院覆盖数据</p></div>
      </article>

      <article class="data-card recent-card">
        <div class="card-heading"><div><span>最近动态</span><h2>最新谈话记录</h2></div><el-button type="text" @click="$router.push('/talk/query')">进入统一查询 <i class="el-icon-arrow-right"></i></el-button></div>
        <div v-if="recentActivities.length" class="recent-list">
          <div v-for="(activity, index) in recentActivities" :key="index" class="recent-item">
            <span class="timeline-dot" :class="activity.talkType"></span>
            <div><strong>{{ activity.studentName || '学生' }}</strong><p>{{ activity.talkPerson || '未填写谈话人' }} · {{ $talkLabel('talkType', activity.talkType) }}</p></div>
            <time>{{ formatDate(activity.talkTime) }}</time>
          </div>
        </div>
        <div v-else class="empty-state"><i class="el-icon-chat-line-round"></i><p>暂无谈话记录</p></div>
      </article>
    </section>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import auth from '@/plugins/auth'
import { getDashboard } from '@/api/talk/talkStatistics'

const KPI_CONFIG = [
  { key: 'totalStudents', label: '学生总数', unit: '人', note: '当前数据范围内', icon: 'el-icon-user', color: '#13798a', tint: '#e6f4f5' },
  { key: 'coveredStudents', label: '已谈学生', unit: '人', note: '至少有一条谈话记录', icon: 'el-icon-circle-check', color: '#2f8c68', tint: '#e9f6ef' },
  { key: 'coverageRate', label: '谈话覆盖率', unit: '%', note: '按去重学生计算', icon: 'el-icon-pie-chart', color: '#bd7a27', tint: '#fff4e5' },
  { key: 'totalSessions', label: '谈话场次', unit: '场', note: '个别与集体谈话', icon: 'el-icon-chat-line-square', color: '#536da8', tint: '#edf1fa' },
  { key: 'totalRecords', label: '学生记录', unit: '条', note: '会话关联学生记录', icon: 'el-icon-document', color: '#845f9e', tint: '#f3edf7' },
  { key: 'untalkedStudents', label: '未谈学生', unit: '人', note: '尚无谈话记录', icon: 'el-icon-bell', color: '#b45e54', tint: '#fbeeed' }
]

export default {
  name: 'DashboardV2',
  data() {
    return {
      loading: false,
      dashboardData: {},
      collegeRanking: [],
      recentActivities: [],
      charts: {},
      lastUpdated: ''
    }
  },
  computed: {
    scopeLabel() {
      return auth.hasRole('admin') || auth.hasRole('school_admin') ? '全校' : '本学院'
    },
    coverageTitle() {
      return this.scopeLabel === '全校' ? '各学院谈话覆盖率' : '本学院谈话覆盖率'
    },
    kpiData() {
      return KPI_CONFIG.map(item => ({ ...item, value: this.dashboardData[item.key] != null ? this.dashboardData[item.key] : 0 }))
    }
  },
  mounted() {
    this.fetchData()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.values(this.charts).forEach(chart => chart && chart.dispose())
  },
  methods: {
    fetchData() {
      this.loading = true
      getDashboard().then(res => {
        this.dashboardData = res.data || {}
        this.collegeRanking = this.dashboardData.collegeRanking || []
        this.recentActivities = this.dashboardData.recentActivities || []
        this.lastUpdated = this.formatClock(new Date())
        this.$nextTick(this.renderCharts)
      }).catch(() => {
        this.$modal.msgError('统计数据加载失败')
      }).finally(() => { this.loading = false })
    },
    percentage(value) {
      const number = Number(value) || 0
      return Math.max(0, Math.min(100, Number(number.toFixed(1))))
    },
    formatDate(value) {
      if (!value) return '-'
      return String(value).replace('T', ' ').substring(5, 16)
    },
    formatClock(date) {
      return [date.getHours(), date.getMinutes(), date.getSeconds()].map(value => String(value).padStart(2, '0')).join(':')
    },
    resizeCharts() {
      Object.values(this.charts).forEach(chart => chart && chart.resize())
    },
    chart(ref) {
      if (this.charts[ref]) this.charts[ref].dispose()
      this.charts[ref] = echarts.init(this.$refs[ref])
      return this.charts[ref]
    },
    renderCharts() {
      this.renderMonthlyChart()
      this.renderTagChart()
    },
    renderMonthlyChart() {
      const chart = this.chart('monthlyChart')
      const data = this.dashboardData.monthlyTrend || []
      if (!data.length) return this.renderEmpty(chart)
      chart.setOption({
        color: ['#168294'],
        tooltip: { trigger: 'axis', backgroundColor: '#17324d', borderWidth: 0, textStyle: { color: '#fff' } },
        grid: { top: 24, left: 18, right: 18, bottom: 12, containLabel: true },
        xAxis: { type: 'category', data: data.map(item => item.month), axisTick: { show: false }, axisLine: { lineStyle: { color: '#dce6eb' } }, axisLabel: { color: '#758997' } },
        yAxis: { type: 'value', minInterval: 1, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#90a0aa' }, splitLine: { lineStyle: { color: '#edf2f4', type: 'dashed' } } },
        series: [{ type: 'line', smooth: true, symbol: 'circle', symbolSize: 7, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(22,130,148,.30)' }, { offset: 1, color: 'rgba(22,130,148,.02)' }]) }, data: data.map(item => item.count) }]
      }, true)
    },
    renderTagChart() {
      const chart = this.chart('tagChart')
      const data = (this.dashboardData.tagDistribution || []).filter(item => Number(item.value) > 0)
      if (!data.length) return this.renderEmpty(chart)
      // ECharts 5.4 caches pie percentages before legend filtering. Calculate
      // against the visible values so labels and tooltips match the slices.
      const visiblePercent = value => {
        const legend = (chart.getOption().legend || [])[0]
        const selected = (legend && legend.selected) || {}
        const total = data.reduce((sum, item) => selected[item.name] === false ? sum : sum + Number(item.value), 0)
        return total > 0 ? Number((Number(value) * 100 / total).toFixed(2)) : 0
      }
      chart.setOption({
        color: ['#168294', '#51a487', '#d9a34a', '#627caf', '#8a689f', '#c66b5e', '#75a3ad'],
        tooltip: { trigger: 'item', renderMode: 'richText', formatter: params => params.name + '\n' + params.value + ' 次 · ' + visiblePercent(params.value) + '%' },
        legend: { type: 'scroll', bottom: 0, left: 'center', icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#6e8291', fontSize: 11 } },
        series: [{ type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'], avoidLabelOverlap: true, itemStyle: { borderColor: '#fff', borderWidth: 4, borderRadius: 5 }, label: { color: '#536b7c', formatter: params => params.name + '\n' + visiblePercent(params.value) + '%' }, data }]
      }, true)
    },
    renderEmpty(chart) {
      chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#a1b0ba', fontSize: 13, fontWeight: 'normal' } } }, true)
    }
  }
}
</script>

<style lang="scss" scoped>
.analytics-page { min-height: calc(100vh - 84px); padding: 24px; color: #17324d; background: #f3f6f9; }
.analytics-hero { position: relative; overflow: hidden; display: flex; align-items: center; justify-content: space-between; gap: 24px; min-height: 150px; padding: 28px 34px; border-radius: 18px; color: #fff; background: linear-gradient(120deg, #103f52 0%, #116879 58%, #3a8e7c 100%); box-shadow: 0 16px 36px rgba(19, 80, 92, .2); }
.analytics-hero::before { content: ''; position: absolute; right: 9%; bottom: -125px; width: 310px; height: 310px; border: 54px solid rgba(255,255,255,.06); border-radius: 50%; }
.eyebrow { font-size: 10px; letter-spacing: 3px; opacity: .68; }
.analytics-hero h1 { margin: 9px 0 7px; font-size: 29px; letter-spacing: 2px; }
.analytics-hero p { margin: 0; font-size: 14px; opacity: .8; }
.hero-actions { position: relative; z-index: 1; display: flex; align-items: center; gap: 16px; }
.hero-actions span { font-size: 12px; opacity: .72; }
.hero-actions .el-button { border-color: rgba(255,255,255,.25); color: #fff; background: rgba(255,255,255,.12); }
.kpi-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 14px; margin: 18px 0; }
.kpi-card { display: flex; align-items: center; gap: 13px; min-height: 112px; padding: 17px; border: 1px solid #e7edf0; border-radius: 15px; background: #fff; box-shadow: 0 7px 20px rgba(28,62,78,.05); }
.kpi-icon { display: flex; align-items: center; justify-content: center; flex: 0 0 42px; height: 42px; border-radius: 12px; font-size: 19px; }
.kpi-copy span, .kpi-copy em { display: block; }
.kpi-copy span { margin-bottom: 5px; color: #697f8e; font-size: 12px; }
.kpi-copy strong { color: #18374e; font-size: 25px; line-height: 1; }
.kpi-copy small { margin-left: 3px; color: #8496a3; font-size: 11px; font-weight: normal; }
.kpi-copy em { margin-top: 7px; color: #a2afb8; font-size: 10px; font-style: normal; white-space: nowrap; }
.chart-grid, .lower-grid { display: grid; gap: 18px; }
.chart-grid { grid-template-columns: minmax(0, 1.35fr) minmax(360px, .8fr); margin-bottom: 18px; }
.lower-grid { grid-template-columns: minmax(0, 1.1fr) minmax(360px, .9fr); }
.data-card { overflow: hidden; border: 1px solid #e5ecef; border-radius: 16px; background: #fff; box-shadow: 0 8px 24px rgba(28,62,78,.05); }
.card-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 19px 22px 10px; }
.card-heading span { color: #9aabb5; font-size: 10px; letter-spacing: 1.5px; }
.card-heading h2 { margin: 5px 0 0; color: #25445a; font-size: 16px; }
.card-heading b { color: #168294; font-size: 12px; font-weight: 500; }
.type-summary { display: flex; gap: 14px; }
.type-summary span { color: #6e8290; font-size: 11px; letter-spacing: 0; }
.dot { display: inline-block; width: 7px; height: 7px; margin-right: 5px; border-radius: 50%; }
.dot.individual { background: #168294; }
.dot.group { background: #d8a045; }
.chart-body { width: 100%; height: 330px; }
.coverage-list { max-height: 310px; overflow-y: auto; padding: 7px 22px 20px; }
.coverage-item { position: relative; display: grid; grid-template-columns: minmax(160px, 1fr) minmax(120px, 1.4fr) 48px; align-items: center; gap: 15px; padding: 12px 0; border-bottom: 1px solid #f0f3f5; }
.coverage-item:last-child { border-bottom: 0; }
.coverage-copy strong, .coverage-copy span { display: block; }
.coverage-copy strong { overflow: hidden; color: #405c6f; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.coverage-copy span { margin-top: 4px; color: #a1afb8; font-size: 10px; }
.coverage-item em { color: #168294; font-size: 12px; font-style: normal; text-align: right; }
.recent-list { padding: 8px 22px 20px; }
.recent-item { display: grid; grid-template-columns: 12px minmax(0, 1fr) auto; align-items: center; gap: 10px; padding: 11px 0; border-bottom: 1px solid #f0f3f5; }
.recent-item:last-child { border-bottom: 0; }
.timeline-dot { width: 8px; height: 8px; border: 2px solid #c2dce0; border-radius: 50%; background: #168294; }
.timeline-dot.group { border-color: #f1dcba; background: #d8a045; }
.recent-item strong { color: #38566a; font-size: 13px; }
.recent-item p { overflow: hidden; margin: 4px 0 0; color: #93a3ad; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.recent-item time { color: #8da0ad; font-size: 11px; }
.empty-state { padding: 70px 20px; color: #a0afb8; text-align: center; }
.empty-state i { font-size: 31px; color: #ccd7dd; }
.empty-state p { margin: 9px 0 0; font-size: 12px; }
@media (max-width: 1500px) { .kpi-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .chart-grid, .lower-grid { grid-template-columns: 1fr; } }
@media (max-width: 768px) {
  .analytics-page { padding: 14px; }
  .analytics-hero { align-items: flex-start; flex-direction: column; padding: 24px 20px; }
  .hero-actions { justify-content: space-between; width: 100%; }
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .kpi-card { min-height: 100px; padding: 13px; }
  .coverage-item { grid-template-columns: minmax(120px, 1fr) 1fr 42px; }
}
</style>
