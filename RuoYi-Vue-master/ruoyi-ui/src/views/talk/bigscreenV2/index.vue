<!--
  ================================================================================
  THROWAWAY PROTOTYPE — bigscreenV2/index.vue
  学生谈心谈话管理系统 · 数据大屏
  目的：独立全屏数据可视化大屏，暗色主题，ECharts动态加载
  所有数据均为模拟数据，无API调用
  ================================================================================
-->
<template>
  <div class="bigscreen-root">
    <!-- Header -->
    <div class="bs-header">
      <div class="bs-header-left">
        <div class="bs-header-deco"></div>
        <span class="bs-header-title">学生谈心谈话管理系统 · 数据大屏</span>
      </div>
      <div class="bs-header-right">
        <div class="bs-clock">{{ currentDateTime }}</div>
      </div>
    </div>

    <!-- 3 Columns -->
    <div class="bs-content">
      <!-- Left Column -->
      <div class="bs-col bs-col-left">
        <!-- 各学院覆盖率排名 -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 各学院覆盖率排名
          </div>
          <div ref="coverageBarChart" class="bs-chart-box" style="height:260px"></div>
        </div>

        <!-- 辅导员工作量TOP10 -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 辅导员工作量TOP10
          </div>
          <div class="bs-rank-list">
            <div class="bs-rank-item" v-for="(item, idx) in topCounselors" :key="idx">
              <div class="bs-rank-num" :class="{ 'top3': idx < 3 }">{{ idx + 1 }}</div>
              <div class="bs-rank-name">{{ item.name }}</div>
              <div class="bs-rank-dept">{{ item.dept }}</div>
              <div class="bs-rank-bar-wrap">
                <div class="bs-rank-bar" :style="{ width: (item.count / topCounselorMax * 100) + '%' }"></div>
              </div>
              <div class="bs-rank-val">{{ item.count }}次</div>
            </div>
          </div>
        </div>

        <!-- 学院数据表格 -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 学院谈话数据明细
          </div>
          <el-table :data="collegeDetailData" size="mini" class="bs-table" :header-cell-style="headerCellStyle" :cell-style="cellStyle">
            <el-table-column prop="name" label="学院" min-width="100" />
            <el-table-column prop="total" label="谈话总数" align="center" width="80" />
            <el-table-column prop="rate" label="覆盖率" align="center" width="80">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.rate >= 85 ? '#0ff' : scope.row.rate >= 70 ? '#b44dff' : '#f7ba2a' }">{{ scope.row.rate }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="进度" min-width="120">
              <template slot-scope="scope">
                <div class="bs-table-bar-wrap">
                  <div class="bs-table-bar" :style="{ width: scope.row.rate + '%', background: scope.row.rate >= 85 ? 'linear-gradient(90deg, #0ff, #4facfe)' : scope.row.rate >= 70 ? 'linear-gradient(90deg, #b44dff, #f093fb)' : 'linear-gradient(90deg, #f7ba2a, #f5576c)' }"></div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Center Column -->
      <div class="bs-col bs-col-center">
        <!-- Province Data Table -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 各省份谈话数据一览
          </div>
          <div class="bs-province-table">
            <div class="bs-province-header">
              <span>省份</span><span>谈话总数</span><span>覆盖率</span>
            </div>
            <div class="bs-province-row" v-for="(p, idx) in provinceData" :key="idx">
              <span>{{ p.name }}</span>
              <span>{{ p.count }}</span>
              <span>
                <div class="bs-province-bar-wrap">
                  <div class="bs-province-bar" :style="{ width: p.coverage + '%', background: provinceColors[idx % 3] }"></div>
                </div>
                {{ p.coverage }}%
              </span>
            </div>
          </div>
        </div>

        <!-- Core KPI -->
        <div class="bs-kpi-grid">
          <div class="bs-kpi-item" v-for="(kpi, idx) in bsKpiData" :key="idx">
            <div class="bs-kpi-val">{{ kpi.value }}<span v-if="kpi.unit" class="bs-kpi-unit">{{ kpi.unit }}</span></div>
            <div class="bs-kpi-label">{{ kpi.label }}</div>
          </div>
        </div>

        <!-- 实时谈话流 -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 实时谈话动态
          </div>
          <div class="bs-feed">
            <div class="bs-feed-item" v-for="(item, idx) in recentFeed" :key="idx" :style="{ animationDelay: idx * 0.15 + 's' }">
              <div class="bs-feed-dot" :class="item.type === 'individual' ? 'dot-blue' : 'dot-purple'"></div>
              <div class="bs-feed-info">
                <div class="bs-feed-title">{{ item.studentName }} · {{ item.college }}</div>
                <div class="bs-feed-meta">{{ item.talkPerson }} · {{ item.talkType === 'individual' ? '个别谈话' : '集体谈话' }}</div>
              </div>
              <div class="bs-feed-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="bs-col bs-col-right">
        <!-- 谈话类型分布 Pie -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 谈话类型分布
          </div>
          <div ref="bsPieChart" class="bs-chart-box" style="height:260px"></div>
        </div>

        <!-- 近7天趋势 Line -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 近7天谈话趋势
          </div>
          <div ref="bsLineChart" class="bs-chart-box" style="height:260px"></div>
        </div>

        <!-- 内容标签单词云模拟 -->
        <div class="bs-panel">
          <div class="bs-panel-title">
            <span class="bs-panel-dot"></span> 内容标签热度
          </div>
          <div class="bs-tag-cloud">
            <span v-for="(tag, idx) in tagCloud" :key="idx"
              :style="{ fontSize: tag.size + 'px', color: tag.color, opacity: tag.opacity }">
              {{ tag.label }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Scrolling Bar -->
    <div class="bs-scroll-bar">
      <div class="bs-scroll-wrap">
        <div class="bs-scroll-inner" :style="{ animationDuration: scrollDuration + 's' }">
          <template v-for="(item, idx) in recentScrollRecords">
            <span class="bs-scroll-item" :key="idx">
              <span class="bs-scroll-time">{{ item.time }}</span>
              <span class="bs-scroll-name">{{ item.studentName }}</span>
              <span class="bs-scroll-type">{{ item.talkType }}</span>
              <span class="bs-scroll-sep">|</span>
            </span>
            <span class="bs-scroll-item" :key="'r'+idx">
              <span class="bs-scroll-time">{{ item.time }}</span>
              <span class="bs-scroll-name">{{ item.studentName }}</span>
              <span class="bs-scroll-type">{{ item.talkType }}</span>
              <span class="bs-scroll-sep">|</span>
            </span>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listTalksession, getSessionTags } from '@/api/talk/talkSession'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { listTalk } from '@/api/talk/talkStudent'
import request from '@/utils/request'

export default {
  name: 'BigscreenV2',
  data() {
    return {
      echartsReady: false,
      chartInstances: {},
      currentDateTime: '',
      clockTimer: null,

      topCounselors: [
        { name: '张老师', dept: '计算机学院', count: 68 },
        { name: '刘老师', dept: '软件工程学院', count: 62 },
        { name: '陈老师', dept: '数据科学学院', count: 55 },
        { name: '王老师', dept: '人工智能学院', count: 48 },
        { name: '李老师', dept: '电子工程学院', count: 45 },
        { name: '赵老师', dept: '经济管理学院', count: 42 },
        { name: '孙老师', dept: '外国语学院', count: 38 },
        { name: '周老师', dept: '数学学院', count: 35 },
        { name: '吴老师', dept: '计算机学院', count: 32 },
        { name: '郑老师', dept: '软件工程学院', count: 28 }
      ],

      collegeDetailData: [
        { name: '计算机学院', total: 156, rate: 92 },
        { name: '软件工程学院', total: 132, rate: 88 },
        { name: '数据科学学院', total: 118, rate: 85 },
        { name: '人工智能学院', total: 98, rate: 80 },
        { name: '电子工程学院', total: 89, rate: 76 },
        { name: '经济管理学院', total: 76, rate: 72 },
        { name: '外国语学院', total: 65, rate: 68 },
        { name: '数学学院', total: 52, rate: 65 }
      ],

      provinceData: [
        { name: '北京', count: 1280, coverage: 82 },
        { name: '上海', count: 1150, coverage: 78 },
        { name: '广东', count: 1420, coverage: 85 },
        { name: '江苏', count: 1100, coverage: 75 },
        { name: '浙江', count: 980, coverage: 72 },
        { name: '山东', count: 860, coverage: 68 },
        { name: '河南', count: 790, coverage: 65 },
        { name: '四川', count: 720, coverage: 62 }
      ],

      bsKpiData: [
        { label: '本月谈话', value: 0 },
        { label: '待跟进', value: 0 },
        { label: '覆盖率', value: 0, unit: '%' },
        { label: '在职辅导员', value: 0, unit: '人' }
      ],

      recentFeed: [
        { studentName: '张晓明', college: '计算机学院', talkPerson: '张老师', talkType: 'individual', time: '14:30:22' },
        { studentName: '李芳华', college: '软件工程学院', talkPerson: '刘老师', talkType: 'group', time: '14:28:15' },
        { studentName: '王建国', college: '数据科学学院', talkPerson: '陈老师', talkType: 'individual', time: '14:15:08' },
        { studentName: '陈小丽', college: '计算机学院', talkPerson: '王老师', talkType: 'individual', time: '14:02:45' },
        { studentName: '赵文博', college: '软件工程学院', talkPerson: '赵老师', talkType: 'group', time: '13:55:30' }
      ],

      recentScrollRecords: [
        { time: '2026-05-29 14:30', studentName: '张晓明', talkType: '个别谈话' },
        { time: '2026-05-29 11:15', studentName: '李芳华', talkType: '集体谈话' },
        { time: '2026-05-29 09:00', studentName: '王建国', talkType: '个别谈话' },
        { time: '2026-05-28 16:45', studentName: '陈小丽', talkType: '个别谈话' },
        { time: '2026-05-28 14:00', studentName: '赵文博', talkType: '集体谈话' }
      ],

      tagCloud: [
        { label: '学风建设', size: 22, color: '#0ff', opacity: 1 },
        { label: '思想教育', size: 18, color: '#b44dff', opacity: 0.9 },
        { label: '心理健康', size: 20, color: '#f5576c', opacity: 0.85 },
        { label: '日常事务', size: 16, color: '#4facfe', opacity: 0.8 },
        { label: '职业规划', size: 14, color: '#38ef7d', opacity: 0.75 },
        { label: '党团建设', size: 15, color: '#f7ba2a', opacity: 0.7 },
        { label: '危机应对', size: 12, color: '#f093fb', opacity: 0.65 },
        { label: '学业警示', size: 13, color: '#ff6b6b', opacity: 0.6 },
        { label: '贫困帮扶', size: 11, color: '#ffd93d', opacity: 0.55 },
        { label: '就业指导', size: 14, color: '#6bcb77', opacity: 0.7 }
      ],

      provinceColors: [
        'linear-gradient(90deg, #00f2fe, #4facfe)',
        'linear-gradient(90deg, #0ff, #b44dff)',
        'linear-gradient(90deg, #f093fb, #f5576c)'
      ]
    }
  },
  computed: {
    topCounselorMax: function() {
      return this.topCounselors.length > 0 ? this.topCounselors[0].count : 1
    },
    scrollDuration: function() {
      return Math.max(15, this.recentScrollRecords.length * 3)
    }
  },
  mounted: function() {
    this.startClock()
    this.loadEcharts()
    this.fetchData()
  },
  beforeDestroy: function() {
    this.disposeAllCharts()
    this.stopClock()
  },
  methods: {
    fetchData: function() {
      var self = this
      listTalksession({ pageSize: 9999 }).then(function(res) {
        self.bsKpiData[0].value = (res.rows || []).length
      }).catch(function() {})
      listTalk({ pageSize: 9999 }).then(function(r) {
        self.bsKpiData[1].value = (r.rows || []).length
      }).catch(function() {})
      listTalkrecord({ followupStatus: 'pending', pageSize: 9999 }).then(function(r) {
        self.bsKpiData[2].value = (r.rows || []).length
      }).catch(function() {})
      listTalkrecord({ pageSize: 9999 }).then(function(r) {
        var done = (r.rows || []).filter(function(x) { return x.followupStatus === 'completed' }).length
        self.bsKpiData[3].value = (r.rows || []).length > 0 ? Math.round(done / r.rows.length * 100) : 0
      }).catch(function() {})
    },

    headerCellStyle: function() {
      return {
        background: 'rgba(255,255,255,0.04)',
        borderBottom: '1px solid rgba(255,255,255,0.06)',
        color: '#8892a4',
        fontSize: '12px',
        padding: '8px 0'
      }
    },
    cellStyle: function() {
      return {
        background: 'transparent',
        borderBottom: '1px solid rgba(255,255,255,0.03)',
        color: '#b4c0d4',
        fontSize: '12px',
        padding: '6px 0'
      }
    },

    loadEcharts: function() {
      var self = this
      if (window.echarts) {
        this.echartsReady = true
        this.$nextTick(function() {
          self.initAllCharts()
        })
        return
      }
      var script = document.createElement('script')
      script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js'
      script.onload = function() {
        self.echartsReady = true
        self.$nextTick(function() {
          self.initAllCharts()
        })
      }
      script.onerror = function() {
        console.warn('[BigscreenV2] ECharts CDN加载失败')
      }
      document.head.appendChild(script)
    },

    initAllCharts: function() {
      this.initCoverageChart()
      this.initBsPieChart()
      this.initBsLineChart()
    },

    createChart: function(refName) {
      if (this.chartInstances[refName]) {
        this.chartInstances[refName].dispose()
      }
      var dom = this.$refs[refName]
      if (!dom) return null
      var chart = window.echarts.init(dom)
      this.chartInstances[refName] = chart
      return chart
    },

    disposeAllCharts: function() {
      for (var key in this.chartInstances) {
        if (this.chartInstances[key]) {
          this.chartInstances[key].dispose()
        }
      }
      this.chartInstances = {}
    },

    initCoverageChart: function() {
      var chart = this.createChart('coverageBarChart')
      if (!chart) return
      var colleges = this.collegeDetailData.slice().reverse()
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 5, left: 90, right: 40, bottom: 5, containLabel: true },
        xAxis: { type: 'value', max: 100, axisLabel: { color: '#8892a4', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
        yAxis: {
          type: 'category',
          data: colleges.map(function(d) { return d.name }),
          axisLabel: { color: '#b4c0d4', fontSize: 10 },
          axisLine: { show: false },
          axisTick: { show: false }
        },
        series: [{
          type: 'bar',
          barWidth: 14,
          data: colleges.map(function(d) {
            return {
              value: d.rate,
              itemStyle: {
                borderRadius: [0, 6, 6, 0],
                color: new window.echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#0ff' },
                  { offset: 1, color: '#b44dff' }
                ])
              }
            }
          }),
          label: { show: true, position: 'right', color: '#0ff', fontSize: 10, formatter: '{c}%' }
        }]
      })
    },

    initBsPieChart: function() {
      var chart = this.createChart('bsPieChart')
      if (!chart) return
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, textStyle: { color: '#8892a4', fontSize: 10 } },
        series: [{
          type: 'pie',
          radius: ['45%', '75%'],
          center: ['50%', '43%'],
          itemStyle: { borderRadius: 4, borderColor: '#0f1119', borderWidth: 3 },
          label: { show: false },
          emphasis: { label: { show: true, color: '#e8eaed' } },
          data: [
            { value: 180, name: '学业辅导', itemStyle: { color: '#0ff' } },
            { value: 120, name: '心理关怀', itemStyle: { color: '#b44dff' } },
            { value: 86, name: '生活指导', itemStyle: { color: '#f5576c' } },
            { value: 60, name: '纪律教育', itemStyle: { color: '#f7ba2a' } },
            { value: 42, name: '就业指导', itemStyle: { color: '#38ef7d' } }
          ]
        }]
      })
    },

    initBsLineChart: function() {
      var chart = this.createChart('bsLineChart')
      if (!chart) return
      var days = ['05-23', '05-24', '05-25', '05-26', '05-27', '05-28', '05-29']
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { top: 20, left: 40, right: 20, bottom: 15, containLabel: true },
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: { color: '#8892a4', fontSize: 10 },
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          axisLabel: { color: '#8892a4', fontSize: 10 }
        },
        series: [{
          type: 'line',
          data: [48, 52, 45, 58, 62, 55, 68],
          smooth: true,
          lineStyle: { color: '#0ff', width: 2 },
          itemStyle: { color: '#0ff' },
          areaStyle: { color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,255,255,0.3)' },
            { offset: 1, color: 'rgba(0,255,255,0.02)' }
          ]) },
          symbol: 'circle',
          symbolSize: 6
        }]
      })
    },

    startClock: function() {
      var self = this
      this.updateClock()
      this.clockTimer = setInterval(function() {
        self.updateClock()
      }, 1000)
    },

    stopClock: function() {
      if (this.clockTimer) {
        clearInterval(this.clockTimer)
        this.clockTimer = null
      }
    },

    updateClock: function() {
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
  background: #0a0c14;
  min-height: 100vh;
  color: #b4c0d4;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
}

/* Header */
.bs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 32px;
  background: linear-gradient(180deg, rgba(0,255,255,0.08), transparent);
  border-bottom: 1px solid rgba(0,255,255,0.12);
}

.bs-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bs-header-deco {
  width: 4px;
  height: 32px;
  background: linear-gradient(180deg, #0ff, #b44dff);
  border-radius: 2px;
}

.bs-header-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #0ff, #b44dff, #f093fb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.bs-clock {
  font-size: 15px;
  font-family: 'Courier New', monospace;
  color: #0ff;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(0,255,255,0.4);
}

/* 3-Column Layout */
.bs-content {
  display: grid;
  grid-template-columns: 1fr 1.1fr 1fr;
  gap: 16px;
  padding: 16px 20px;
}

.bs-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Glass Panels */
.bs-panel {
  background: rgba(255,255,255,0.03);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 10px;
  padding: 16px;
}

.bs-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #e8eaed;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.bs-panel-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0ff;
  box-shadow: 0 0 8px rgba(0,255,255,0.6);
}

.bs-chart-box {
  width: 100%;
}

/* Rank List */
.bs-rank-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bs-rank-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.bs-rank-num {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #8892a4;
  background: rgba(255,255,255,0.04);
  flex-shrink: 0;
}

.bs-rank-num.top3 {
  background: linear-gradient(135deg, #0ff, #b44dff);
  color: #0a0c14;
  box-shadow: 0 0 12px rgba(0,255,255,0.3);
}

.bs-rank-name {
  font-size: 12px;
  font-weight: 500;
  color: #e8eaed;
  width: 60px;
  flex-shrink: 0;
}

.bs-rank-dept {
  font-size: 11px;
  color: #667084;
  width: 80px;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bs-rank-bar-wrap {
  flex: 1;
  height: 6px;
  background: rgba(255,255,255,0.04);
  border-radius: 3px;
  overflow: hidden;
}

.bs-rank-bar {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #0ff, #b44dff);
  transition: width 0.6s ease;
}

.bs-rank-val {
  font-size: 12px;
  font-weight: 600;
  color: #0ff;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

/* Dark Table */
.bs-table {
  background: transparent !important;
}

.bs-table >>> .el-table__body-wrapper {
  background: transparent;
}

.bs-table >>> .el-table__empty-block {
  background: transparent;
}

.bs-table-bar-wrap {
  width: 100%;
  height: 6px;
  background: rgba(255,255,255,0.04);
  border-radius: 3px;
  overflow: hidden;
}

.bs-table-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

/* Province Table */
.bs-province-table {
  font-size: 12px;
}

.bs-province-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr;
  padding: 8px 0;
  color: #667084;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  margin-bottom: 4px;
}

.bs-province-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr;
  padding: 7px 0;
  color: #b4c0d4;
  align-items: center;
  transition: background 0.2s;
}

.bs-province-row:hover {
  background: rgba(255,255,255,0.03);
}

.bs-province-bar-wrap {
  display: inline-flex;
  width: 60px;
  height: 6px;
  background: rgba(255,255,255,0.04);
  border-radius: 3px;
  overflow: hidden;
  margin-right: 6px;
  vertical-align: middle;
}

.bs-province-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

/* KPI Grid */
.bs-kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.bs-kpi-item {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 10px;
  padding: 18px 16px;
  text-align: center;
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
}

.bs-kpi-item:hover {
  border-color: rgba(0,255,255,0.3);
  box-shadow: 0 0 20px rgba(0,255,255,0.08);
}

.bs-kpi-val {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #0ff, #b44dff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.bs-kpi-unit {
  font-size: 14px;
  font-weight: 500;
  -webkit-text-fill-color: #667084;
}

.bs-kpi-label {
  font-size: 12px;
  color: #667084;
  margin-top: 4px;
}

/* Feed */
.bs-feed {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bs-feed-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: rgba(255,255,255,0.02);
  border-radius: 6px;
  border: 1px solid rgba(255,255,255,0.03);
  animation: feedSlideIn 0.4s ease both;
}

@keyframes feedSlideIn {
  from { opacity: 0; transform: translateX(-12px); }
  to { opacity: 1; transform: translateX(0); }
}

.bs-feed-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.bs-feed-dot.dot-blue {
  background: #0ff;
  box-shadow: 0 0 6px rgba(0,255,255,0.5);
}

.bs-feed-dot.dot-purple {
  background: #b44dff;
  box-shadow: 0 0 6px rgba(180,77,255,0.5);
}

.bs-feed-info {
  flex: 1;
  min-width: 0;
}

.bs-feed-title {
  font-size: 12px;
  font-weight: 500;
  color: #e8eaed;
}

.bs-feed-meta {
  font-size: 11px;
  color: #667084;
  margin-top: 2px;
}

.bs-feed-time {
  font-size: 11px;
  color: #0ff;
  font-family: 'Courier New', monospace;
  flex-shrink: 0;
}

/* Tag Cloud */
.bs-tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  justify-content: center;
  padding: 8px 0;
  min-height: 100px;
  align-items: center;
}

.bs-tag-cloud span {
  font-weight: 600;
  cursor: default;
  transition: transform 0.2s;
}

.bs-tag-cloud span:hover {
  transform: scale(1.15);
}

/* Bottom Scrolling Bar */
.bs-scroll-bar {
  margin: 0 20px 16px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 8px;
  padding: 10px 16px;
  overflow: hidden;
}

.bs-scroll-wrap {
  overflow: hidden;
  white-space: nowrap;
}

.bs-scroll-inner {
  display: inline-flex;
  gap: 32px;
  animation: scrollLeft linear infinite;
}

@keyframes scrollLeft {
  0%   { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.bs-scroll-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  flex-shrink: 0;
}

.bs-scroll-time { color: #667084; }
.bs-scroll-name { color: #0ff; font-weight: 600; }
.bs-scroll-type { color: #b4c0d4; }
.bs-scroll-sep  { color: rgba(255,255,255,0.1); }

@media (max-width: 1200px) {
  .bs-content { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .bs-content { grid-template-columns: 1fr; }
  .bs-header-title { font-size: 16px; }
  .bs-kpi-grid { grid-template-columns: 1fr 1fr; }
}
</style>