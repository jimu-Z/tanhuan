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
      <div class="dashboard-chart-box dashboard-chart-full">
        <div class="dashboard-chart-header">
          <i class="el-icon-s-data"></i> 各学院谈话排名
        </div>
        <div ref="collegeBarChart" class="dashboard-chart-body" style="height:360px"></div>
      </div>
      <div class="dashboard-chart-box">
        <div class="dashboard-chart-header">
          <i class="el-icon-pie-chart"></i> 谈话类型占比
        </div>
        <div class="dashboard-ratio-wrap">
          <div v-if="individualCount + groupCount > 0" class="dashboard-ratio-content">
            <div class="dashboard-ratio-item">
              <div class="dashboard-ratio-label">个别谈话</div>
              <el-progress :percentage="individualPercent" color="#667eea" :stroke-width="18" :text-inside="true">
                {{ individualCount }} 次
              </el-progress>
            </div>
            <div class="dashboard-ratio-item">
              <div class="dashboard-ratio-label">集体谈话</div>
              <el-progress :percentage="groupPercent" color="#e6a23c" :stroke-width="18" :text-inside="true">
                {{ groupCount }} 次
              </el-progress>
            </div>
          </div>
          <div v-else class="dashboard-empty">暂无数据</div>
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

export default {
  name: 'DashboardV2',
  data() {
    return {
      loading: false,
      chartInstances: {},
      dashboardData: {},
      tagDistribution: [],
      monthlyTrend: [],
      collegeRanking: [],
      individualCount: 0,
      groupCount: 0
    }
  },
  computed: {
    kpiData() {
      const d = this.dashboardData
      return KPI_CONFIG.map(k => ({ ...k, value: d[k.key] != null ? d[k.key] : 0 }))
    },
    individualPercent() {
      const t = this.individualCount + this.groupCount
      return t > 0 ? Math.round(this.individualCount / t * 100) : 0
    },
    groupPercent() {
      const t = this.individualCount + this.groupCount
      return t > 0 ? Math.round(this.groupCount / t * 100) : 0
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
          this.collegeRanking = data.collegeRanking || []
          this.individualCount = data.individualCount || 0
          this.groupCount = data.groupCount || 0

          this.$nextTick(() => this.renderAllCharts())
        })
        .catch(() => {
          this.$message.error('数据加载失败')
        })
        .finally(() => {
          this.loading = false
        })
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
      this.renderCollegeBarChart()
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
    },

    renderCollegeBarChart() {
      const chart = this.initChart('collegeBarChart')
      if (!chart) return
      const data = this.collegeRanking
      if (!data.length) {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } } }, true)
        return
      }
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 10, left: 120, right: 50, bottom: 10, containLabel: true },
        xAxis: { type: 'value', axisLabel: { fontSize: 11 } },
        yAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { fontSize: 11 }, inverse: true },
        series: [{ type: 'bar', barWidth: 16, data: data.map(v => ({ value: v.count, itemStyle: { borderRadius: [0,6,6,0], color: new echarts.graphic.LinearGradient(0,0,1,0,[{ offset:0, color:'#667eea' },{ offset:1, color:'#764ba2' }]) } })), label: { show: true, position: 'right', fontSize: 11, fontWeight: 'bold' } }]
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
.dashboard-bottom { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; padding: 0 24px; }

.dashboard-chart-box { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); overflow: hidden; }
.dashboard-chart-full { grid-column: 1 / -1; }
.dashboard-chart-header { font-size: 14px; font-weight: 600; color: #1a1a2e; padding: 14px 20px; border-bottom: 1px solid #f0f2f5; display: flex; align-items: center; gap: 6px; }
.dashboard-chart-body { width: 100%; height: 340px; }

.dashboard-ratio-wrap { padding: 20px 24px; }
.dashboard-ratio-content { display: flex; flex-direction: column; gap: 24px; }
.dashboard-ratio-item { display: flex; align-items: center; gap: 16px; }
.dashboard-ratio-label { font-size: 13px; color: #606266; white-space: nowrap; width: 70px; }
.dashboard-ratio-item ::v-deep .el-progress { flex: 1; }

.dashboard-empty { text-align: center; color: #909399; padding: 40px; font-size: 13px; }
</style>
