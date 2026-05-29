<!--
  ================================================================================
  THROWAWAY PROTOTYPE — alertsV2/index.vue
  学生谈心谈话管理系统 · 预警与提醒系统 V2
  所有数据均为模拟数据，无API调用
  ================================================================================
-->
<template>
  <div class="alerts-root">
    <div class="alerts-container">

      <!-- ==================== Top Banner ==================== -->
      <div class="banner-wrap">
        <div class="banner-card">
          <div class="banner-icon">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="banner-body">
            <div class="banner-title">共 <strong class="banner-count">{{ summaryCount }}</strong> 条预警信息需处理</div>
            <div class="banner-sub">请及时处理以下预警，避免影响学生工作进度</div>
          </div>
          <div class="banner-progress">
            <el-progress
              type="circle"
              :percentage="summaryResolved ? Math.round(summaryResolved / (summaryCount + summaryResolved) * 100) : 0"
              :width="72"
              :stroke-width="6"
              color="#f56c6c"
            ></el-progress>
          </div>
        </div>
      </div>

      <!-- ==================== Alert Cards Grid ==================== -->
      <div class="alert-grid">

        <!-- 🔴 RED: 心理异常 -->
        <div class="alert-block alert-block-red">
          <div class="block-header">
            <div class="block-header-left">
              <span class="block-indicator red-bg"></span>
              <span class="block-title">心理异常预警</span>
              <el-tag size="small" type="danger" effect="dark">{{ mentalAlerts.length }}人</el-tag>
            </div>
            <span class="block-badge">需立即关注</span>
          </div>
          <div class="block-cards">
            <div
              class="alert-item alert-item-red"
              v-for="(item, idx) in mentalAlerts"
              :key="'m' + idx"
            >
              <div class="item-left">
                <div class="item-avatar" :style="{ background: avatarColors[idx % avatarColors.length] }">
                  {{ item.studentName.charAt(0) }}
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.studentName }}</div>
                  <div class="item-meta">
                    <span>{{ item.studentCode }}</span>
                    <span class="meta-sep">·</span>
                    <span>{{ item.className }}</span>
                  </div>
                  <div class="item-detail">
                    <el-tag size="mini" type="danger" effect="plain">{{ item.mentalStatus }}</el-tag>
                    <span class="detail-text">上次谈话：{{ item.lastTalkDate }}</span>
                  </div>
                </div>
              </div>
              <div class="item-right">
                <el-button size="small" type="danger" plain round @click="handleTalk(item)">
                  发起谈话
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 🟠 ORANGE: 长期未谈 >30天 -->
        <div class="alert-block alert-block-orange">
          <div class="block-header">
            <div class="block-header-left">
              <span class="block-indicator orange-bg"></span>
              <span class="block-title">长期未谈话预警</span>
              <el-tag size="small" type="warning" effect="dark">{{ longNoTalkAlerts.length }}人</el-tag>
            </div>
            <span class="block-badge">超过30天</span>
          </div>
          <div class="block-cards">
            <div
              class="alert-item alert-item-orange"
              v-for="(item, idx) in longNoTalkAlerts"
              :key="'o' + idx"
            >
              <div class="item-left">
                <div class="item-avatar" :style="{ background: avatarColors[(idx + 1) % avatarColors.length] }">
                  {{ item.studentName.charAt(0) }}
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.studentName }}</div>
                  <div class="item-meta">
                    <span>{{ item.studentCode }}</span>
                    <span class="meta-sep">·</span>
                    <span>{{ item.className }}</span>
                  </div>
                  <div class="item-detail">
                    <el-tag size="mini" type="warning" effect="plain">{{ item.days }}天未谈话</el-tag>
                    <span class="detail-text">上次谈话：{{ item.lastTalkDate }}</span>
                  </div>
                </div>
              </div>
              <div class="item-right">
                <el-button size="small" type="warning" plain round @click="handleTalk(item)">
                  发起谈话
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 🟡 YELLOW: 待跟进到期 -->
        <div class="alert-block alert-block-yellow">
          <div class="block-header">
            <div class="block-header-left">
              <span class="block-indicator yellow-bg"></span>
              <span class="block-title">待跟进事项到期</span>
              <el-tag size="small" type="warning" effect="dark">{{ followupAlerts.length }}项</el-tag>
            </div>
            <span class="block-badge">即将到期</span>
          </div>
          <div class="block-cards">
            <div
              class="alert-item alert-item-yellow"
              v-for="(item, idx) in followupAlerts"
              :key="'y' + idx"
            >
              <div class="item-left">
                <div class="item-avatar" :style="{ background: avatarColors[(idx + 2) % avatarColors.length] }">
                  {{ item.studentName.charAt(0) }}
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.studentName }}</div>
                  <div class="item-meta">
                    <span>{{ item.plan }}</span>
                  </div>
                  <div class="item-detail">
                    <el-tag size="mini" type="warning" effect="plain">
                      截止 {{ item.deadline }}
                    </el-tag>
                    <span class="detail-text urgency">{{ item.days }}天后到期</span>
                  </div>
                </div>
              </div>
              <div class="item-right">
                <el-dropdown trigger="click" @command="function(cmd) { handleFollowupStatus(cmd, item) }">
                  <el-button size="small" type="warning" plain round>
                    更新状态 <i class="el-icon-arrow-down el-icon--right"></i>
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="done">
                      <i class="el-icon-success" style="color:#67c23a"></i> 已完成
                    </el-dropdown-item>
                    <el-dropdown-item command="progress">
                      <i class="el-icon-loading" style="color:#409eff"></i> 进行中
                    </el-dropdown-item>
                    <el-dropdown-item command="delay">
                      <i class="el-icon-time" style="color:#e6a23c"></i> 延期处理
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 🔵 BLUE: 贫困关注 -->
        <div class="alert-block alert-block-blue">
          <div class="block-header">
            <div class="block-header-left">
              <span class="block-indicator blue-bg"></span>
              <span class="block-title">贫困学生关注</span>
              <el-tag size="small" effect="dark" color="#409eff">{{ poorAlerts.length }}人</el-tag>
            </div>
            <span class="block-badge">特别困难</span>
          </div>
          <div class="block-cards">
            <div
              class="alert-item alert-item-blue"
              v-for="(item, idx) in poorAlerts"
              :key="'b' + idx"
            >
              <div class="item-left">
                <div class="item-avatar" :style="{ background: avatarColors[(idx + 3) % avatarColors.length] }">
                  {{ item.studentName.charAt(0) }}
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.studentName }}</div>
                  <div class="item-meta">
                    <span>{{ item.studentCode }}</span>
                    <span class="meta-sep">·</span>
                    <span>{{ item.className }}</span>
                  </div>
                  <div class="item-detail">
                    <el-tag size="mini" type="info" effect="plain">{{ item.povertyLevel }}</el-tag>
                    <el-tag size="mini" type="info" effect="plain" v-if="item.subsidyStatus">
                      {{ item.subsidyStatus }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="item-right">
                <el-button size="small" type="primary" plain round @click="handleTalk(item)">
                  安排关怀
                </el-button>
              </div>
            </div>
          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script>
import { listTalk } from '@/api/talk/talkStudent'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import request from '@/utils/request'

export default {
  name: 'AlertsV2',

  data: function() {
    return {
      avatarColors: ['#f56c6c', '#e6a23c', '#409eff', '#67c23a'],

      mentalAlerts: [],
      longNoTalkAlerts: [],
      followupAlerts: [],
      poorAlerts: [],
      summaryCount: 0,
      summaryResolved: 0
    }
  },

  mounted: function() {
    this.fetchData()
  },

  methods: {
    fetchData: function() {
      var self = this

      listTalk({ pageSize: 9999 }).then(function(res) {
        var students = res.rows || []
        self.mentalAlerts = students
          .filter(function(s) { return s.mentalHealthStatus && s.mentalHealthStatus !== 'normal' })
          .map(function(s) { return { studentName: s.studentName, studentCode: s.studentCode, className: '', mentalStatus: s.mentalHealthStatus, lastTalkDate: '' } })
      })

      listTalk({ pageSize: 9999 }).then(function(r) {
        var students = r.rows || []
        self.poorAlerts = students
          .filter(function(s) { return s.povertyLevel === 'severe' || s.povertyLevel === 'difficult' })
          .map(function(s) { return { studentName: s.studentName, studentCode: s.studentCode, className: '', povertyLevel: s.povertyLevel } })
      })

      listTalkrecord({ pageSize: 9999 }).then(function(res) {
        var records = res.rows || []
        var lastTalkMap = {}
        var now = new Date()
        records.forEach(function(r) {
          if (!lastTalkMap[r.studentId] || new Date(r.createTime) > new Date(lastTalkMap[r.studentId])) {
            lastTalkMap[r.studentId] = r.createTime
          }
        })
        listTalk({ pageSize: 9999 }).then(function(r2) {
          var allStudents = r2.rows || []
          self.longNoTalkAlerts = allStudents
            .filter(function(s) {
              var last = lastTalkMap[s.studentId]
              if (!last) return true
              return (now - new Date(last)) / (1000 * 86400) > 30
            })
            .slice(0, 10)
            .map(function(s) {
              var last = lastTalkMap[s.studentId]
              var days = last ? Math.floor((now - new Date(last)) / (1000 * 86400)) : 999
              return { studentName: s.studentName, studentCode: s.studentCode, className: '', days: days, lastTalkDate: last || '从未' }
            })
        })
      })

      listTalkrecord({ followupStatus: 'pending', pageSize: 9999 }).then(function(res) {
        var records = res.rows || []
        Promise.all(records.map(function(r) {
          return listTalk({ pageSize: 1 }).then(function(r2) {
            return { ...r, studentName: (r2.rows || [])[0]?.studentName || '', studentCode: (r2.rows || [])[0]?.studentCode || '' }
          }).catch(function() { return r })
        })).then(function(enriched) {
          self.followupAlerts = enriched.map(function(r) {
            var days = r.createTime ? Math.floor((new Date() - new Date(r.createTime)) / (1000 * 86400)) : 0
            return { studentName: r.studentName, studentCode: r.studentCode, plan: r.followupPlan || '', days: days, deadline: r.createTime || '' }
          })
        })
      })

      this.$watch(function() {
        self.summaryCount = self.mentalAlerts.length + self.longNoTalkAlerts.length + self.followupAlerts.length + self.poorAlerts.length
        self.summaryResolved = 2
      })
    },

    handleTalk: function(item) {
      this.$message.success('已跳转至谈话发起页面：' + (item.studentName || item.name))
    },

    handleFollowupStatus: function(cmd, item) {
      var statusMap = {
        done: '已完成',
        progress: '进行中',
        delay: '延期处理'
      }
      item.status = cmd
      this.$message.success(
        '跟进事项【' + item.studentName + '】状态已更新为：' + statusMap[cmd]
      )
    }
  }
}
</script>

<style scoped>
/* ============================================================
   Root & Container
   ============================================================ */
.alerts-root {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 24px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  box-sizing: border-box;
}

.alerts-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* ============================================================
   Top Banner
   ============================================================ */
.banner-wrap {
  margin-bottom: 28px;
}

.banner-card {
  background: linear-gradient(135deg, #fff5f5, #fff);
  border: 1px solid #fde2e2;
  border-radius: 16px;
  padding: 20px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 16px rgba(245, 108, 108, 0.08);
  animation: bannerPulse 2.4s ease-in-out infinite;
}

@keyframes bannerPulse {
  0%, 100% {
    box-shadow: 0 2px 16px rgba(245, 108, 108, 0.08), 0 0 0 0 rgba(245, 108, 108, 0.2);
  }
  50% {
    box-shadow: 0 2px 16px rgba(245, 108, 108, 0.08), 0 0 0 12px rgba(245, 108, 108, 0);
  }
}

.banner-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f56c6c, #e04040);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.banner-icon .el-icon-warning-outline {
  font-size: 28px;
  color: #fff;
}

.banner-body {
  flex: 1;
}

.banner-title {
  font-size: 18px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
}

.banner-count {
  font-size: 26px;
  color: #f56c6c;
  font-weight: 800;
}

.banner-sub {
  font-size: 13px;
  color: #909399;
}

.banner-progress {
  flex-shrink: 0;
}

/* ============================================================
   Alert Grid
   ============================================================ */
.alert-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* ============================================================
   Alert Block (Colored Section Card)
   ============================================================ */
.alert-block {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  border-left: 4px solid transparent;
}

.alert-block:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.alert-block-red    { border-left-color: #f56c6c; }
.alert-block-orange { border-left-color: #e6a23c; }
.alert-block-yellow { border-left-color: #f2c811; }
.alert-block-blue   { border-left-color: #409eff; }

/* ============================================================
   Block Header
   ============================================================ */
.block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0f0f0;
}

.block-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.block-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.red-bg    { background: #f56c6c; }
.orange-bg { background: #e6a23c; }
.yellow-bg { background: #f2c811; }
.blue-bg   { background: #409eff; }

.block-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.block-badge {
  font-size: 11px;
  color: #909399;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
}

/* ============================================================
   Block Cards (Alert Items)
   ============================================================ */
.block-cards {
  padding: 8px 0;
}

.alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  transition: background 0.2s ease;
  border-bottom: 1px solid #fafafa;
  border-left: 3px solid transparent;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-item:hover {
  background: #fafbfc;
}

.alert-item-red:hover    { border-left-color: #f56c6c; background: #fef8f8; }
.alert-item-orange:hover { border-left-color: #e6a23c; background: #fef9f0; }
.alert-item-yellow:hover { border-left-color: #f2c811; background: #fffef5; }
.alert-item-blue:hover   { border-left-color: #409eff; background: #f5f9ff; }

/* ============================================================
   Item Left
   ============================================================ */
.item-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.item-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.item-info {
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.item-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-sep {
  margin: 0 4px;
  color: #c0c4cc;
}

.item-detail {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-text {
  font-size: 12px;
  color: #909399;
}

.detail-text.urgency {
  color: #e6a23c;
  font-weight: 600;
}

/* ============================================================
   Item Right
   ============================================================ */
.item-right {
  flex-shrink: 0;
  margin-left: 16px;
}

/* ============================================================
   Responsive
   ============================================================ */
@media (max-width: 900px) {
  .alert-grid {
    grid-template-columns: 1fr;
  }

  .banner-card {
    flex-direction: column;
    text-align: center;
  }

  .alert-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-right {
    margin-left: 0;
    align-self: flex-end;
  }
}
</style>