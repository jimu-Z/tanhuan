<template>
  <div class="dashboard-v2">
    <div class="kpi-row">
      <div class="kpi-card" v-for="(kpi, idx) in kpiData" :key="kpi.key" :style="{ animationDelay: idx * 0.06 + 's' }">
        <div class="kpi-accent" :style="{ background: kpi.accentColor }"></div>
        <div class="kpi-icon-wrap" :style="{ background: kpi.iconBg }">
          <i :class="kpi.icon" :style="{ color: kpi.iconColor }"></i>
        </div>
        <div class="kpi-body">
          <span class="kpi-label">{{ kpi.label }}</span>
          <div class="kpi-value-row">
            <span class="kpi-number">{{ kpi.displayValue }}</span>
            <span class="kpi-unit" v-if="kpi.unit">{{ kpi.unit }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <el-card class="chart-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">
            <i class="el-icon-collection-tag"></i> 谈话内容标签分布
          </span>
          <el-tag size="mini" type="info" effect="plain">{{ tagCount }}类</el-tag>
        </div>
        <div ref="tagPieChart" class="chart-box chart-box-medium"></div>
      </el-card>

      <el-card class="chart-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">
            <i class="el-icon-data-line"></i> 月度谈话趋势
          </span>
          <span class="card-subtitle">{{ currentYear }}年</span>
        </div>
        <div ref="monthlyBarChart" class="chart-box chart-box-medium"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboard, getCharts } from '@/api/talk/talkStatistics'

const KPI_CONFIG = [
  {
    key: 'totalStudents',
    label: '学生总数',
    unit: '人',
    icon: 'el-icon-user',
    iconBg: 'linear-gradient(135deg, rgba(64,158,255,0.12), rgba(103,194,58,0.12))',
    iconColor: '#409eff',
    accentColor: 'linear-gradient(180deg, #409eff 0%, #67c23a 100%)'
  },
  {
    key: 'totalSessions',
    label: '谈话场次',
    unit: '场',
    icon: 'el-icon-s-order',
    iconBg: 'linear-gradient(135deg, rgba(102,126,234,0.12), rgba(118,75,162,0.12))',
    iconColor: '#667eea',
    accentColor: 'linear-gradient(180deg, #667eea 0%, #764ba2 100%)'
  },
  {
    key: 'totalRecords',
    label: '记录总数',
    unit: '条',
    icon: 'el-icon-document',
    iconBg: 'linear-gradient(135deg, rgba(17,153,142,0.12), rgba(56,239,125,0.12))',
    iconColor: '#11998e',
    accentColor: 'linear-gradient(180deg, #11998e 0%, #38ef7d 100%)'
  },
  {
    key: 'avgRecordsPerStudent',
    label: '人均记录',
    unit: '条/人',
    icon: 'el-icon-data-analysis',
    iconBg: 'linear-gradient(135deg, rgba(238,90,111,0.12), rgba(247,186,42,0.12))',
    iconColor: '#ee5a6f',
    accentColor: 'linear-gradient(180deg, #ee5a6f 0%, #f7ba2a 100%)'
  },
  {
    key: 'individualCount',
    label: '个人谈话',
    unit: '场',
    icon: 'el-icon-user-solid',
    iconBg: 'linear-gradient(135deg, rgba(118,75,162,0.12), rgba(161,140,209,0.12))',
    iconColor: '#764ba2',
    accentColor: 'linear-gradient(180deg, #764ba2 0%, #a18cd1 100%)'
  },
  {
    key: 'groupCount',
    label: '集体谈话',
    unit: '场',
    icon: 'el-icon-s-grid',
    iconBg: 'linear-gradient(135deg, rgba(247,186,42,0.12), rgba(245,135,66,0.12))',
    iconColor: '#e6a23c',
    accentColor: 'linear-gradient(180deg, #e6a23c 0%, #f58742 100%)'
  },
  {
    key: 'pendingFeedback',
    label: '待查看反馈',
    unit: '条',
    icon: 'el-icon-bell',
    iconBg: 'linear-gradient(135deg, rgba(245,87,108,0.12), rgba(247,186,42,0.12))',
    iconColor: '#f5576c',
    accentColor: 'linear-gradient(180deg, #f5576c 0%, #f093fb 100%)'
  }
]

const TAG_COLORS = [
  '#667eea', '#764ba2', '#11998e', '#38ef7d', '#ee5a6f', '#f7ba2a', '#409eff',
  '#a18cd1', '#56d4a5', '#fcb69f'
]

export default {
  name: 'DashboardV2',
  data() {
    return {
      currentYear: new Date().getFullYear(),
      loading: false,
      chartInstances: {},
      dashboardData: {},
      tagDistribution: [],
      monthlyTrend: []
    }
  },
  computed: {
    kpiData() {
      const d = this.dashboardData
      return KPI_CONFIG.map((cfg) => ({
        ...cfg,
        displayValue: d[cfg.key] != null ? d[cfg.key] : 0
      }))
    },
    tagCount() {
      return this.tagDistribution.filter((t) => t.value > 0).length
    }
  },
  mounted() {
    this.fetchAllData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.disposeAllCharts()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    fetchAllData() {
      this.loading = true
      Promise.all([getDashboard(), getCharts()])
        .then(([dashRes, chartRes]) => {
          this.dashboardData = dashRes.data || {}

          const chartData = chartRes.data || {}
          this.tagDistribution = chartData.tagDistribution || []
          this.monthlyTrend = chartData.monthlyTrend || []

          this.$nextTick(() => {
            this.renderAllCharts()
          })
        })
        .catch((err) => {
          this.loading = false
          this.$message.error('数据加载失败')
        })
        .finally(() => {
          this.loading = false
        })
    },

    handleResize() {
      Object.values(this.chartInstances).forEach((chart) => {
        if (chart && !chart.isDisposed()) chart.resize()
      })
    },

    renderAllCharts() {
      this.renderTagPieChart()
      this.renderMonthlyBarChart()
    },

    initChart(refName) {
      if (this.chartInstances[refName]) {
        this.chartInstances[refName].dispose()
      }
      const dom = this.$refs[refName]
      if (!dom) return null
      const chart = echarts.init(dom)
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

    renderTagPieChart() {
      const chart = this.initChart('tagPieChart')
      if (!chart) return

      const data = this.tagDistribution.filter((t) => t.value > 0)
      if (data.length === 0) {
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } }
        })
        return
      }

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
        color: TAG_COLORS,
        series: [{
          name: '谈话内容标签',
          type: 'pie',
          radius: ['45%', '70%'],
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
          data
        }]
      })
    },

    renderMonthlyBarChart() {
      const chart = this.initChart('monthlyBarChart')
      if (!chart) return

      if (!this.monthlyTrend || this.monthlyTrend.length === 0) {
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } }
        })
        return
      }

      const months = this.monthlyTrend.map((d) => d.month)
      const counts = this.monthlyTrend.map((d) => d.count)

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: '#ebeef5',
          textStyle: { color: '#303133' },
          formatter(params) {
            return params[0].name + '<br/>谈话次数: <b>' + params[0].value + '</b> 次'
          }
        },
        grid: { top: 16, left: 8, right: 8, bottom: 12, containLabel: true },
        xAxis: {
          type: 'category',
          data: months,
          axisLabel: { fontSize: 11, color: '#909399' },
          axisTick: { show: false },
          axisLine: { lineStyle: { color: '#e4e7ed' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { fontSize: 10, color: '#909399' },
          splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
          axisLine: { show: false },
          axisTick: { show: false }
        },
        series: [{
          type: 'bar',
          barWidth: 18,
          data: counts,
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 0.5, color: '#764ba2' },
              { offset: 1, color: '#a18cd1' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#8e9efc' },
                { offset: 1, color: '#c471ed' }
              ])
            }
          }
        }]
      })
    }
  }
}
</script>

<style scoped>
.dashboard-v2 {
  padding: 24px;
  min-height: 100vh;
  background: #f0f4fc;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  box-sizing: border-box;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  position: relative;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1.2);
  animation: kpiSlideIn 0.45s ease both;
}

.kpi-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
}

.kpi-accent {
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 2px 2px 0;
}

.kpi-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.kpi-body {
  flex: 1;
  min-width: 0;
}

.kpi-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  display: block;
  margin-bottom: 4px;
}

.kpi-value-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.kpi-number {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1a1a2e;
  line-height: 1;
}

.kpi-unit {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

@keyframes kpiSlideIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid #ebeef5 !important;
  transition: box-shadow 0.3s ease;
  background: #ffffff;
}

.chart-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1) !important;
}

.chart-card ::v-deep .el-card__header {
  padding: 14px 20px;
  border-bottom-color: #f2f3f5;
  background: #fafbfc;
}

.chart-card ::v-deep .el-card__body {
  padding: 16px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-subtitle {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

.chart-box {
  width: 100%;
}

.chart-box-medium {
  height: 300px;
}

@media (max-width: 1400px) {
  .kpi-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1100px) {
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
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .kpi-card {
    padding: 14px 16px;
    gap: 10px;
  }
  .kpi-icon-wrap {
    width: 36px;
    height: 36px;
    font-size: 18px;
    border-radius: 10px;
  }
  .kpi-number {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
}
</style>