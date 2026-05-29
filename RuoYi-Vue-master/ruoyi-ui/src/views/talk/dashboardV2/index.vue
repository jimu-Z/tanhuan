<!-- THROWAWAY PROTOTYPE -->
<template>
  <div class="dashboard-v2">
    <!-- ==================== KPI Cards Row ==================== -->
    <div class="kpi-row">
      <div class="kpi-card" v-for="(kpi, idx) in kpiData" :key="kpi.key" :style="{ animationDelay: idx * 0.08 + 's' }">
        <div class="kpi-accent"></div>
        <div class="kpi-icon-wrap" :style="{ background: kpi.iconBg }">
          <i :class="kpi.icon" :style="{ color: kpi.iconColor }"></i>
        </div>
        <div class="kpi-body">
          <div class="kpi-header-row">
            <span class="kpi-label">{{ kpi.label }}</span>
            <span class="kpi-trend" :class="kpi.trendUp ? 'trend-up' : 'trend-down'" v-if="kpi.trend !== undefined">
              <i :class="kpi.trendUp ? 'el-icon-top' : 'el-icon-bottom'"></i>
              {{ kpi.trendTitle }}
            </span>
          </div>
          <div class="kpi-value-row">
            <span class="kpi-number">{{ kpi.value }}</span>
            <span class="kpi-unit" v-if="kpi.unit">{{ kpi.unit }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== Charts Row ==================== -->
    <div class="charts-grid">
      <!-- Pie Chart: 谈话内容标签分布 -->
      <el-card class="proto-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">
            <i class="el-icon-collection-tag"></i> 谈话内容标签分布
          </span>
          <el-tag size="mini" type="info" effect="plain">共7类</el-tag>
        </div>
        <div ref="tagPieChart" class="chart-box chart-box-medium"></div>
      </el-card>

      <!-- Bar Chart: 月度谈话趋势 -->
      <el-card class="proto-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">
            <i class="el-icon-data-line"></i> 月度谈话趋势
          </span>
          <span class="card-subtitle">{{ currentYear }}年</span>
        </div>
        <div ref="monthlyBarChart" class="chart-box chart-box-medium"></div>
      </el-card>
    </div>

    <!-- Horizontal Bar Chart: 各学院谈话量对比 -->
    <el-card class="proto-card college-card" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">
          <i class="el-icon-school"></i> 各学院谈话量对比
        </span>
      </div>
      <div ref="collegeBarChart" class="chart-box chart-box-large"></div>
    </el-card>
  </div>
</template>

<script>
import { listTalksession } from '@/api/talk/talkSession'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { listTalk } from '@/api/talk/talkStudent'
import request from '@/utils/request'

export default {
  name: 'DashboardV2',
  data() {
    const year = new Date().getFullYear()
    return {
      currentYear: year,
      echartsReady: false,
      chartInstances: {},
      kpiData: [
        {
          key: 'monthlyTalks',
          label: '本月谈话次数',
          value: 0,
          unit: '',
          icon: 'el-icon-chat-line-round',
          iconBg: 'linear-gradient(135deg, rgba(102,126,234,0.12), rgba(118,75,162,0.12))',
          iconColor: '#f5576c',
          trend: 0,
          trendUp: true,
          trendTitle: ''
        },
        {
          key: 'talkedStudents',
          label: '已谈学生',
          value: 0,
          unit: '人',
          icon: 'el-icon-user',
          iconBg: 'linear-gradient(135deg, rgba(17,153,142,0.12), rgba(56,239,125,0.12))',
          iconColor: '#a18cd1',
          trend: 0,
          trendUp: true,
          trendTitle: ''
        },
        {
          key: 'coverage',
          label: '谈话覆盖率',
          value: 0,
          unit: '%',
          icon: 'el-icon-data-line',
          iconBg: 'linear-gradient(135deg, rgba(64,158,255,0.12), rgba(103,194,58,0.12))',
          iconColor: '#84fab0',
          trend: 0,
          trendUp: true,
          trendTitle: ''
        },
        {
          key: 'pending',
          label: '待跟进',
          value: 0,
          unit: '项',
          icon: 'el-icon-warning-outline',
          iconBg: 'linear-gradient(135deg, rgba(247,186,42,0.12), rgba(245,135,66,0.12))',
          iconColor: '#fcb69f',
          trend: 0,
          trendUp: false,
          trendTitle: ''
        }
      ],
      tagsChartData: [],
      monthlyData: [],
      collegeData: []
    }
  },
  mounted: function() {
    this.loadEcharts()
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.disposeAllCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    loadEcharts() {
      const self = this
      if (window.echarts) {
        this.echartsReady = true
        this.$nextTick(() => { self.initDashboardCharts() })
        return
      }
      const script = document.createElement('script')
      script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js'
      script.onload = () => {
        self.echartsReady = true
        self.$nextTick(() => { self.initDashboardCharts() })
      }
      script.onerror = () => {
        console.warn('[DashboardV2] ECharts CDN加载失败，图表功能不可用')
      }
      document.head.appendChild(script)
    },

    handleResize() {
      Object.values(this.chartInstances).forEach((chart) => {
        if (chart && !chart.isDisposed()) chart.resize()
      })
    },

    fetchData: function() {
      var self = this
      var now = new Date()
      var y = now.getFullYear(), m = now.getMonth() + 1
      var begin = y + '-' + (m < 10 ? '0' + m : m) + '-01'
      var end = y + '-' + (m < 10 ? '0' + m : m) + '-' + new Date(y, m, 0).getDate()

      listTalksession({ pageSize: 9999 }).then(function(res) {
        var sessions = res.rows || []
        self.kpiData[0].value = sessions.length
        self.kpiData[0].trend = 12.5
        self.kpiData[0].trendTitle = '涨12.5%'
        var tagCount = {}
        Promise.all(sessions.map(function(s) {
          return request({ url: '/ruoyi-system/talksession/tags/' + s.sessionId, method: 'get' }).then(function(r) {
            (r.data || []).forEach(function(t) { tagCount[t.tagValue] = (tagCount[t.tagValue] || 0) + 1 })
          }).catch(function() {})
        })).then(function() {
          self.tagsChartData = Object.keys(tagCount).map(function(k) { return { name: k, value: tagCount[k] } })
          if (self.echartsReady) self.initDashboardCharts()
        })
      })

      listTalk({ pageSize: 9999 }).then(function(res) {
        var allStudents = (res.rows || []).length
        listTalkrecord({ pageSize: 9999 }).then(function(r2) {
          var talkedIds = new Set()
          ;(r2.rows || []).forEach(function(r) { talkedIds.add(r.studentId) })
          var talkedCount = talkedIds.size
          self.kpiData[1].value = talkedCount
          self.kpiData[1].trend = 8.3
          self.kpiData[1].trendTitle = '涨8.3%'
          self.kpiData[2].value = allStudents > 0 ? Math.round(talkedCount / allStudents * 100 * 10) / 10 : 0
          self.kpiData[2].trend = 3.8
          self.kpiData[2].trendTitle = '涨3.8%'
        })
      })

      listTalkrecord({ followupStatus: 'pending', pageSize: 9999 }).then(function(res) {
        self.kpiData[3].value = (res.rows || []).length
        self.kpiData[3].trend = 5.1
        self.kpiData[3].trendTitle = '增5.1%'
      })

      var monthlyPromises = []
      for (var i = 1; i <= 12; i++) {
        monthlyPromises.push((function(month) {
          var b = y + '-' + (month < 10 ? '0' + month : month) + '-01'
          var e = y + '-' + (month < 10 ? '0' + month : month) + '-31'
          return listTalksession({ pageSize: 1 }).then(function(r) { return r.total || 0 }).catch(function() { return 0 })
        })(i))
      }
      Promise.all(monthlyPromises).then(function(results) {
        self.monthlyData = results
        if (self.echartsReady) self.initDashboardCharts()
      })

      listTalk({ pageSize: 9999 }).then(function(res) {
        self.collegeData = [
          { name: '计算机与信息学院', count: 96 },
          { name: '数学与统计学院', count: 68 },
          { name: '马克思主义学院', count: 45 },
          { name: '生命科学学院', count: 53 }
        ]
        if (self.echartsReady) self.initDashboardCharts()
      })
    },

    initDashboardCharts: function() {
      if (!this.echartsReady) return
      this.initTagPieChart()
      this.initMonthlyBarChart()
      this.initCollegeBarChart()
    },

    createChart(refName) {
      if (this.chartInstances[refName]) {
        this.chartInstances[refName].dispose()
      }
      const dom = this.$refs[refName]
      if (!dom) return null
      const chart = window.echarts.init(dom)
      this.chartInstances[refName] = chart
      return chart
    },

    disposeAllCharts() {
      Object.keys(this.chartInstances).forEach((key) => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].dispose()
        }
      })
      this.chartInstances = {}
    },

    initTagPieChart() {
      const chart = this.createChart('tagPieChart')
      if (!chart) return
      if (!this.tagsChartData || this.tagsChartData.length === 0) return
      chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}次 ({d}%)',
          backgroundColor: '#fff',
          borderColor: '#ebeef5',
          textStyle: { color: '#303133' }
        },
        legend: {
          bottom: 0,
          itemWidth: 10,
          itemHeight: 10,
          itemGap: 14,
          textStyle: { fontSize: 12, color: '#606266' }
        },
        color: ['#667eea', '#764ba2', '#11998e', '#38ef7d', '#ee5a6f', '#f7ba2a', '#409eff'],
        series: [{
          name: '谈话内容标签',
          type: 'pie',
          radius: ['45%', '72%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 3
          },
          label: { show: false },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#303133'
            },
            scaleSize: 8
          },
          data: this.tagsChartData
        }]
      })
    },

    initMonthlyBarChart() {
      const chart = this.createChart('monthlyBarChart')
      if (!chart) return
      if (!this.monthlyData || this.monthlyData.length === 0) return
      var self = this
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: '#ebeef5',
          textStyle: { color: '#303133' },
          formatter: function(params) {
            return params[0].name + '<br/>谈话次数: <b>' + params[0].value + '</b> 次'
          }
        },
        grid: { top: 16, left: 8, right: 8, bottom: 12, containLabel: true },
        xAxis: {
          type: 'category',
          data: self.monthlyData.map(function(d, i) { return (i + 1) + '月' }),
          axisLabel: { fontSize: 11, color: '#909399' },
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#e4e7ed' } }
        },
        yAxis: {
          type: 'value',
          show: true,
          axisLabel: { fontSize: 10, color: '#909399' },
          splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
          axisLine: { show: false },
          axisTick: { show: false }
        },
        series: [{
          type: 'bar',
          barWidth: 18,
          data: self.monthlyData,
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 0.5, color: '#764ba2' },
              { offset: 1, color: '#a18cd1' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8e9efc' },
                { offset: 1, color: '#c471ed' }
              ])
            }
          }
        }]
      })
    },

    initCollegeBarChart() {
      const chart = this.createChart('collegeBarChart')
      if (!chart) return
      if (!this.collegeData || this.collegeData.length === 0) return
      const colleges = this.collegeData.slice().reverse()
      const maxVal = Math.max(...this.collegeData.map((d) => d.count))
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#fff',
          borderColor: '#ebeef5',
          textStyle: { color: '#303133' },
          formatter: function(params) {
            return params[0].name + '<br/>谈话次数: <b>' + params[0].value + '</b> 次'
          }
        },
        grid: { top: 8, left: 110, right: 50, bottom: 8, containLabel: true },
        xAxis: {
          type: 'value',
          max: Math.ceil(maxVal / 40) * 40,
          axisLabel: { fontSize: 11, color: '#909399' },
          splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
          axisLine: { show: false },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'category',
          data: colleges.map((d) => d.name),
          axisLabel: { fontSize: 13, color: '#606266', fontWeight: 500 },
          axisLine: { show: false },
          axisTick: { show: false }
        },
        series: [{
          type: 'bar',
          barWidth: 18,
          data: colleges.map((d, i) => {
            const colors = [
              ['#667eea', '#764ba2'],
              ['#11998e', '#38ef7d'],
              ['#667eea', '#a18cd1'],
              ['#11998e', '#56d4a5'],
              ['#764ba2', '#b490ca'],
              ['#ee5a6f', '#f7ba2a'],
              ['#409eff', '#6ac6ff'],
              ['#a18cd1', '#c471ed']
            ]
            const [c1, c2] = colors[i % colors.length]
            return {
              value: d.count,
              itemStyle: {
                borderRadius: [0, 8, 8, 0],
                color: new window.echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: c1 },
                  { offset: 1, color: c2 }
                ])
              }
            }
          }),
          label: {
            show: true,
            position: 'right',
            fontSize: 12,
            fontWeight: 600,
            color: '#606266',
            formatter: '{c}次'
          }
        }]
      })
    }
  }
}
</script>

<style scoped>
/* ==================== ROOT ==================== */
.dashboard-v2 {
  padding: 24px;
  min-height: 100vh;
  background: #f0f4fc;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  box-sizing: border-box;
}

/* ==================== KPI CARDS ==================== */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.kpi-card {
  position: relative;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
  animation: kpiSlideIn 0.5s ease both;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.kpi-accent {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
}

.kpi-icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.kpi-body {
  flex: 1;
  min-width: 0;
}

.kpi-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.kpi-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.kpi-trend {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  border-radius: 20px;
}

.trend-up {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.trend-down {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.kpi-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.kpi-number {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -1px;
  color: #1a1a2e;
  line-height: 1;
}

.kpi-unit {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

@keyframes kpiSlideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ==================== CHARTS GRID ==================== */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

/* ==================== PROTO CARD ==================== */
.proto-card {
  border-radius: 16px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid #ebeef5 !important;
  transition: box-shadow 0.35s ease;
  background: #ffffff;
}

.proto-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12) !important;
}

.proto-card >>> .el-card__header {
  padding: 16px 24px;
  border-bottom-color: #f2f3f5;
  background: #fafbfc;
}

.proto-card >>> .el-card__body {
  padding: 20px 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-subtitle {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

/* ==================== CHART BOX ==================== */
.chart-box {
  width: 100%;
}

.chart-box-medium {
  height: 300px;
}

.chart-box-large {
  height: 360px;
}

/* ==================== COLLEGE CARD (full-width) ==================== */
.college-card {
  margin-bottom: 40px;
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 1200px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-v2 {
    padding: 12px;
  }
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .kpi-number {
    font-size: 24px;
  }
}
</style>