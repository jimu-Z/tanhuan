<template>
  <div class="bigscreen-root" v-loading="loading">
    <div class="bs-header">
      <div class="bs-header-left">
        <div class="bs-header-deco"></div>
        <span class="bs-header-title">学生谈心谈话管理系统 · 数据大屏</span>
      </div>
      <div class="bs-header-right">
        <div class="bs-clock">{{ currentDateTime }}</div>
      </div>
    </div>

    <div class="bs-kpi-row">
      <div class="bs-kpi-card" v-for="(kpi, idx) in kpiCards" :key="idx">
        <div class="bs-kpi-icon" :style="{ background: kpi.iconBg }">
          <svg-icon :icon-class="kpi.icon" v-if="kpi.icon" />
          <span v-else class="bs-kpi-icon-text">{{ kpi.iconText }}</span>
        </div>
        <div class="bs-kpi-body">
          <div class="bs-kpi-label">{{ kpi.label }}</div>
          <div class="bs-kpi-val">
            <span class="bs-kpi-num">{{ displayValues[idx] }}</span>
            <span class="bs-kpi-unit" v-if="kpi.unit">{{ kpi.unit }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bs-content">
      <div class="bs-col bs-col-left">
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 各学院谈话排名
          </div>
          <div ref="collegeBarChart" class="bs-chart-box" style="height:340px"></div>
        </div>
      </div>

      <div class="bs-col bs-col-center">
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 月度谈话趋势
          </div>
          <div ref="monthlyLineChart" class="bs-chart-box" style="height:340px"></div>
        </div>
      </div>

      <div class="bs-col bs-col-right">
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 标签分布
          </div>
          <div ref="tagPieChart" class="bs-chart-box" style="height:340px"></div>
        </div>
      </div>
    </div>

    <div class="bs-bottom">
      <div class="bs-panel">
        <div class="bs-panel-title">
          <span class="bs-panel-dot"></span> 谈话类型占比
        </div>
        <div class="bs-ratio-wrap">
          <div class="bs-ratio-item" v-if="individualCount + groupCount > 0">
            <div class="bs-ratio-bar">
              <div class="bs-ratio-fill" :style="{ width: individualPercent + '%', background: 'linear-gradient(90deg, #00d4ff, #0ff)' }"></div>
            </div>
            <div class="bs-ratio-info">
              <span class="bs-ratio-label">个别谈话</span>
              <span class="bs-ratio-val">{{ individualCount }} 次（{{ individualPercent }}%）</span>
            </div>
          </div>
          <div class="bs-ratio-item" v-if="individualCount + groupCount > 0">
            <div class="bs-ratio-bar">
              <div class="bs-ratio-fill" :style="{ width: groupPercent + '%', background: 'linear-gradient(90deg, #7b5cff, #b44dff)' }"></div>
            </div>
            <div class="bs-ratio-info">
              <span class="bs-ratio-label">集体谈话</span>
              <span class="bs-ratio-val">{{ groupCount }} 次（{{ groupPercent }}%）</span>
            </div>
          </div>
          <div class="bs-ratio-empty" v-if="individualCount + groupCount === 0">暂无数据</div>
        </div>
      </div>
    </div>

    <div class="bs-footer">
      <span>数据每30秒自动刷新</span>
      <span class="bs-footer-sep">|</span>
      <span>上次更新：{{ lastUpdateTime }}</span>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBigscreen } from '@/api/talk/talkStatistics'

export default {
  name: 'BigscreenV2',
  data() {
    return {
      loading: false,
      refreshTimer: null,
      clockTimer: null,
      currentDateTime: '',
      lastUpdateTime: '--',

      totalStudents: 0,
      totalSessions: 0,
      totalRecords: 0,
      individualCount: 0,
      groupCount: 0,
      coverageRate: 0,
      monthlyTrend: [],
      tagDistribution: [],
      collegeRanking: [],

      chartInstances: {}
    }
  },
  computed: {
    displayValues() {
      return [
        this.formatNumber(this.totalStudents),
        this.formatNumber(this.totalSessions),
        this.formatNumber(this.totalRecords),
        this.formatNumber(this.individualCount),
        this.formatNumber(this.groupCount),
        this.coverageRate
      ]
    },
    individualPercent() {
      const total = this.individualCount + this.groupCount
      return total > 0 ? Math.round(this.individualCount / total * 100) : 0
    },
    groupPercent() {
      const total = this.individualCount + this.groupCount
      return total > 0 ? Math.round(this.groupCount / total * 100) : 0
    },
    kpiCards() {
      return [
        { label: '学生总数', iconText: 'S', iconBg: 'linear-gradient(135deg, #00d4ff, #0ff)' },
        { label: '谈话场次', iconText: 'T', iconBg: 'linear-gradient(135deg, #7b5cff, #b44dff)' },
        { label: '谈话记录', iconText: 'R', iconBg: 'linear-gradient(135deg, #00b8d4, #4facfe)' },
        { label: '个别谈话', iconText: 'I', iconBg: 'linear-gradient(135deg, #00c853, #38ef7d)' },
        { label: '集体谈话', iconText: 'G', iconBg: 'linear-gradient(135deg, #ff9100, #f7ba2a)' },
        { label: '覆盖率', iconText: '%', iconBg: 'linear-gradient(135deg, #ff5252, #f5576c)', unit: '%' }
      ]
    }
  },
  mounted() {
    this.startClock()
    this.fetchData()
    this.refreshTimer = setInterval(() => {
      this.fetchData()
    }, 30000)
  },
  beforeDestroy() {
    this.disposeAllCharts()
    this.stopClock()
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  },
  methods: {
    formatNumber(num) {
      if (num == null) return 0
      return num
    },

    fetchData() {
      var self = this
      getBigscreen().then(function(res) {
        var data = res.data || res
        self.totalStudents = data.totalStudents || 0
        self.totalSessions = data.totalSessions || 0
        self.totalRecords = data.totalRecords || 0
        self.individualCount = data.individualCount || 0
        self.groupCount = data.groupCount || 0
        self.coverageRate = data.coverageRate || 0
        self.monthlyTrend = data.monthlyTrend || []
        self.tagDistribution = data.tagDistribution || []
        self.collegeRanking = data.collegeRanking || []

        self.lastUpdateTime = self.formatTime(new Date())

        self.$nextTick(function() {
          self.initAllCharts()
        })
      }).catch(function() {})
    },

    formatTime(date) {
      var h = String(date.getHours()).padStart(2, '0')
      var m = String(date.getMinutes()).padStart(2, '0')
      var s = String(date.getSeconds()).padStart(2, '0')
      return h + ':' + m + ':' + s
    },

    initAllCharts() {
      this.initCollegeBarChart()
      this.initMonthlyLineChart()
      this.initTagPieChart()
    },

    createChart(refName) {
      if (this.chartInstances[refName]) {
        this.chartInstances[refName].dispose()
      }
      var dom = this.$refs[refName]
      if (!dom) return null
      var chart = echarts.init(dom)
      this.chartInstances[refName] = chart
      return chart
    },

    disposeAllCharts() {
      for (var key in this.chartInstances) {
        if (this.chartInstances[key]) {
          this.chartInstances[key].dispose()
        }
      }
      this.chartInstances = {}
    },

    initCollegeBarChart() {
      var chart = this.createChart('collegeBarChart')
      if (!chart) return
      var data = this.collegeRanking
      if (data.length === 0) {
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#667084', fontSize: 14 } }
        }, true)
        return
      }
      var names = data.map(function(d) { return d.name })
      var values = data.map(function(d) { return d.count })

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(10,14,44,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#b4c0d4', fontSize: 12 }
        },
        grid: { top: 10, left: 100, right: 40, bottom: 10, containLabel: true },
        xAxis: {
          type: 'value',
          axisLabel: { color: '#667084', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
          axisLine: { show: false }
        },
        yAxis: {
          type: 'category',
          data: names,
          axisLabel: { color: '#b4c0d4', fontSize: 11 },
          axisLine: { show: false },
          axisTick: { show: false },
          inverse: true
        },
        series: [{
          type: 'bar',
          barWidth: 16,
          data: values.map(function(v) {
            return {
              value: v,
              itemStyle: {
                borderRadius: [0, 8, 8, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#00d4ff' },
                  { offset: 1, color: '#7b5cff' }
                ])
              }
            }
          }),
          label: {
            show: true,
            position: 'right',
            color: '#0ff',
            fontSize: 11,
            fontWeight: 'bold'
          },
          animationDuration: 1500,
          animationEasing: 'cubicOut'
        }]
      }, true)
    },

    initMonthlyLineChart() {
      var chart = this.createChart('monthlyLineChart')
      if (!chart) return
      var data = this.monthlyTrend
      if (data.length === 0) {
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#667084', fontSize: 14 } }
        }, true)
        return
      }
      var months = data.map(function(d) { return d.month })
      var values = data.map(function(d) { return d.count })

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(10,14,44,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#b4c0d4', fontSize: 12 }
        },
        grid: { top: 20, left: 50, right: 30, bottom: 20, containLabel: true },
        xAxis: {
          type: 'category',
          data: months,
          boundaryGap: false,
          axisLabel: { color: '#667084', fontSize: 11 },
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
          axisLabel: { color: '#667084', fontSize: 11 }
        },
        series: [{
          type: 'line',
          data: values,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            color: '#0ff',
            width: 3,
            shadowBlur: 10,
            shadowColor: 'rgba(0,255,255,0.4)'
          },
          itemStyle: {
            color: '#0ff',
            borderColor: '#0a0e2c',
            borderWidth: 2
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,212,255,0.35)' },
              { offset: 1, color: 'rgba(0,212,255,0.02)' }
            ])
          },
          animationDuration: 1500,
          animationEasing: 'cubicOut'
        }]
      }, true)
    },

    initTagPieChart() {
      var chart = this.createChart('tagPieChart')
      if (!chart) return
      var data = this.tagDistribution
      if (data.length === 0) {
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#667084', fontSize: 14 } }
        }, true)
        return
      }

      var colors = ['#00d4ff', '#7b5cff', '#38ef7d', '#f7ba2a', '#f5576c', '#4facfe', '#ff9100', '#00c853']
      var pieData = data.map(function(d, i) {
        return {
          value: d.value,
          name: d.name,
          itemStyle: { color: colors[i % colors.length] }
        }
      })

      chart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(10,14,44,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#b4c0d4', fontSize: 12 },
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          type: 'scroll',
          bottom: 0,
          textStyle: { color: '#8892a4', fontSize: 10 },
          pageTextStyle: { color: '#667084' }
        },
        series: [{
          type: 'pie',
          radius: ['55%', '78%'],
          center: ['50%', '46%'],
          itemStyle: {
            borderRadius: 4,
            borderColor: '#0a0e2c',
            borderWidth: 4
          },
          label: {
            show: true,
            position: 'outside',
            color: '#b4c0d4',
            fontSize: 10,
            formatter: '{b}\n{d}%'
          },
          labelLine: {
            lineStyle: { color: 'rgba(255,255,255,0.15)' }
          },
          emphasis: {
            label: { fontSize: 14, fontWeight: 'bold' },
            scaleSize: 10
          },
          data: pieData,
          animationDuration: 1500,
          animationEasing: 'cubicOut'
        }]
      }, true)
    },

    startClock() {
      var self = this
      this.updateClock()
      this.clockTimer = setInterval(function() {
        self.updateClock()
      }, 1000)
    },

    stopClock() {
      if (this.clockTimer) {
        clearInterval(this.clockTimer)
        this.clockTimer = null
      }
    },

    updateClock() {
      var now = new Date()
      var y = now.getFullYear()
      var m = String(now.getMonth() + 1).padStart(2, '0')
      var d = String(now.getDate()).padStart(2, '0')
      var h = String(now.getHours()).padStart(2, '0')
      var min = String(now.getMinutes()).padStart(2, '0')
      var s = String(now.getSeconds()).padStart(2, '0')
      var weekdays = ['日', '一', '二', '三', '四', '五', '六']
      this.currentDateTime = y + '-' + m + '-' + d + ' 星期' + weekdays[now.getDay()] + ' ' + h + ':' + min + ':' + s
    }
  }
}
</script>

<style scoped>
.bigscreen-root {
  background: #0a0e2c;
  min-height: 100vh;
  color: #b4c0d4;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  overflow: auto;
}

/* ========== Header ========== */
.bs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 36px;
  background: linear-gradient(180deg, rgba(0,212,255,0.08), transparent);
  border-bottom: 1px solid rgba(0,212,255,0.12);
}

.bs-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bs-header-deco {
  width: 4px;
  height: 34px;
  background: linear-gradient(180deg, #00d4ff, #7b5cff);
  border-radius: 2px;
}

.bs-header-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #00d4ff, #7b5cff, #38ef7d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.bs-clock {
  font-size: 16px;
  font-family: 'Courier New', monospace;
  color: #0ff;
  letter-spacing: 1px;
  text-shadow: 0 0 12px rgba(0,255,255,0.5);
}

/* ========== KPI Row ========== */
.bs-kpi-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  padding: 20px 36px;
}

.bs-kpi-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 18px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.35s ease;
  position: relative;
  overflow: hidden;
}

.bs-kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0,212,255,0.04), rgba(123,92,255,0.04));
  opacity: 0;
  transition: opacity 0.35s ease;
}

.bs-kpi-card:hover {
  border-color: rgba(0,212,255,0.35);
  box-shadow: 0 4px 24px rgba(0,212,255,0.1);
  transform: translateY(-2px);
}

.bs-kpi-card:hover::before {
  opacity: 1;
}

.bs-kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  position: relative;
  z-index: 1;
}

.bs-kpi-icon-text {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 0 8px rgba(255,255,255,0.4);
}

.bs-kpi-body {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.bs-kpi-label {
  font-size: 13px;
  color: #667084;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.bs-kpi-val {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.bs-kpi-num {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, #0ff, #7b5cff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.1;
}

.bs-kpi-unit {
  font-size: 14px;
  font-weight: 500;
  color: #667084;
}

/* ========== 3-Column Content ========== */
.bs-content {
  display: grid;
  grid-template-columns: 1fr 1.1fr 1fr;
  gap: 20px;
  padding: 0 36px 20px;
}

.bs-col {
  display: flex;
  flex-direction: column;
}

/* ========== Panel ========== */
.bs-panel {
  background: rgba(255,255,255,0.025);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 12px;
  padding: 20px;
  height: 100%;
}

.bs-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #e8eaed;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.bs-panel-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0ff;
  box-shadow: 0 0 10px rgba(0,255,255,0.6);
}

.bs-chart-box {
  width: 100%;
}

/* ========== Bottom Ratio ========== */
.bs-bottom {
  padding: 0 36px 20px;
}

.bs-ratio-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 8px 0;
}

.bs-ratio-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bs-ratio-bar {
  flex: 1;
  height: 12px;
  background: rgba(255,255,255,0.04);
  border-radius: 6px;
  overflow: hidden;
  max-width: 500px;
}

.bs-ratio-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  min-width: 2px;
}

.bs-ratio-info {
  display: flex;
  gap: 16px;
  align-items: center;
  min-width: 280px;
}

.bs-ratio-label {
  font-size: 13px;
  color: #e8eaed;
  font-weight: 500;
  width: 70px;
}

.bs-ratio-val {
  font-size: 13px;
  color: #0ff;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.bs-ratio-empty {
  color: #667084;
  font-size: 13px;
  text-align: center;
  padding: 12px 0;
}

/* ========== Footer ========== */
.bs-footer {
  text-align: center;
  padding: 12px 36px 24px;
  font-size: 12px;
  color: #444d62;
  letter-spacing: 1px;
}

.bs-footer-sep {
  margin: 0 12px;
  color: rgba(255,255,255,0.06);
}

/* ========== Responsive ========== */
@media (max-width: 1400px) {
  .bs-kpi-row {
    grid-template-columns: repeat(3, 1fr);
  }
  .bs-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .bs-kpi-row {
    grid-template-columns: repeat(2, 1fr);
    padding: 16px;
  }
  .bs-content {
    padding: 0 16px 16px;
    grid-template-columns: 1fr;
  }
  .bs-header {
    padding: 16px 20px;
  }
  .bs-header-title {
    font-size: 16px;
  }
  .bs-bottom {
    padding: 0 16px 16px;
  }
}
</style>