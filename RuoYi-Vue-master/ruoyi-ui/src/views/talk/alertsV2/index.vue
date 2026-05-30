<template>
  <div class="alerts-container">
    <el-row :gutter="16" class="summary-row">
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card summary-card-danger">
          <div class="summary-body">
            <div class="summary-icon danger-bg">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="summary-info">
              <div class="summary-label">待跟进</div>
              <div class="summary-value" :style="{ color: '#f56c6c' }">{{ alertData.pendingFollowups }}</div>
            </div>
          </div>
          <el-divider class="summary-divider"></el-divider>
          <div class="summary-footer">
            <span class="summary-tag">
              <el-tag size="small" type="danger" effect="dark">需处理</el-tag>
            </span>
            <span class="summary-desc">待跟进预警事项</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="summary-card summary-card-primary">
          <div class="summary-body">
            <div class="summary-icon primary-bg">
              <i class="el-icon-loading"></i>
            </div>
            <div class="summary-info">
              <div class="summary-label">跟进中</div>
              <div class="summary-value" :style="{ color: '#409eff' }">{{ alertData.inProgressFollowups }}</div>
            </div>
          </div>
          <el-divider class="summary-divider"></el-divider>
          <div class="summary-footer">
            <span class="summary-tag">
              <el-tag size="small" type="primary" effect="dark">处理中</el-tag>
            </span>
            <span class="summary-desc">正在跟进的事项</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="summary-card" :class="totalAlertsCardClass">
          <div class="summary-body">
            <div class="summary-icon" :class="totalAlertsIconClass">
              <i :class="totalAlertsIcon"></i>
            </div>
            <div class="summary-info">
              <div class="summary-label">总预警数</div>
              <div class="summary-value" :style="{ color: totalAlertsColor }">{{ alertData.totalAlerts }}</div>
            </div>
          </div>
          <el-divider class="summary-divider"></el-divider>
          <div class="summary-footer">
            <el-progress
              :percentage="totalAlertsProgress"
              :color="totalAlertsProgressColor"
              :stroke-width="6"
            ></el-progress>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="dept-card">
      <div slot="header" class="dept-card-header">
        <span class="dept-card-title">
          <i class="el-icon-school"></i>
          部门/班级预警覆盖
        </span>
        <el-tag size="small" type="info" effect="plain">
          共 {{ alertData.deptCoverage.length }} 个部门/班级
        </el-tag>
      </div>

      <el-table
        :data="alertData.deptCoverage"
        stripe
        style="width: 100%"
        :default-sort="{ prop: 'percentage', order: 'descending' }"
      >
        <el-table-column
          prop="deptName"
          label="部门/班级名称"
          min-width="200"
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <span class="dept-name-cell">{{ scope.row.deptName }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="studentCount"
          label="学生人数"
          width="140"
          align="center"
          sortable
        >
          <template slot-scope="scope">
            <el-tag
              size="small"
              :type="getStudentCountTagType(scope.row.studentCount)"
              effect="plain"
            >
              {{ scope.row.studentCount }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="percentage"
          label="占比"
          width="160"
          align="center"
          sortable
        >
          <template slot-scope="scope">
            <div class="percentage-cell">
              <el-progress
                :percentage="parseFloat(scope.row.percentage)"
                :color="getPercentageColor(scope.row.percentage)"
                :stroke-width="8"
              ></el-progress>
              <span class="percentage-text" :style="{ color: getPercentageColor(scope.row.percentage) }">
                {{ scope.row.percentage }}%
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="预警等级"
          width="120"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag
              size="small"
              :type="getAlertLevelTagType(scope.row.percentage)"
              effect="dark"
            >
              {{ getAlertLevelText(scope.row.percentage) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAlerts } from '@/api/talk/talkStatistics'

export default {
  name: 'AlertsV2',

  data() {
    return {
      alertData: {
        pendingFollowups: 0,
        inProgressFollowups: 0,
        totalAlerts: 0,
        deptCoverage: []
      },
      loading: false
    }
  },

  computed: {
    totalAlertsProgress() {
      var pending = this.alertData.pendingFollowups
      var inProgress = this.alertData.inProgressFollowups
      var total = this.alertData.totalAlerts
      if (total === 0) return 100
      return Math.round((inProgress / total) * 100)
    },

    totalAlertsProgressColor() {
      var pending = this.alertData.pendingFollowups
      var total = this.alertData.totalAlerts
      if (total === 0) return '#67c23a'
      if (pending === 0) return '#67c23a'
      var ratio = pending / total
      if (ratio > 0.6) return '#f56c6c'
      if (ratio > 0.3) return '#e6a23c'
      return '#67c23a'
    },

    totalAlertsColor() {
      var total = this.alertData.totalAlerts
      if (total === 0) return '#67c23a'
      if (total >= 20) return '#f56c6c'
      if (total >= 10) return '#e6a23c'
      return '#67c23a'
    },

    totalAlertsIcon() {
      var total = this.alertData.totalAlerts
      if (total >= 20) return 'el-icon-warning'
      if (total >= 10) return 'el-icon-warning-outline'
      return 'el-icon-success'
    },

    totalAlertsIconClass() {
      var total = this.alertData.totalAlerts
      if (total >= 20) return 'danger-bg'
      if (total >= 10) return 'warning-bg'
      return 'success-bg'
    },

    totalAlertsCardClass() {
      var total = this.alertData.totalAlerts
      if (total >= 20) return 'summary-card-danger-border'
      if (total >= 10) return 'summary-card-warning-border'
      return 'summary-card-success-border'
    }
  },

  mounted() {
    this.fetchAlerts()
  },

  methods: {
    fetchAlerts() {
      var self = this
      self.loading = true
      getAlerts().then(function(res) {
        var data = res.data
        self.alertData.pendingFollowups = data.pendingFollowups || 0
        self.alertData.inProgressFollowups = data.inProgressFollowups || 0
        self.alertData.totalAlerts = data.totalAlerts || 0
        self.alertData.deptCoverage = data.deptCoverage || []
        self.loading = false
      }).catch(function() {
        self.loading = false
      })
    },

    getPercentageColor(percentage) {
      var val = parseFloat(percentage)
      if (val >= 30) return '#f56c6c'
      if (val >= 15) return '#e6a23c'
      return '#67c23a'
    },

    getAlertLevelText(percentage) {
      var val = parseFloat(percentage)
      if (val >= 30) return '高'
      if (val >= 15) return '中'
      return '低'
    },

    getAlertLevelTagType(percentage) {
      var val = parseFloat(percentage)
      if (val >= 30) return 'danger'
      if (val >= 15) return 'warning'
      return 'success'
    },

    getStudentCountTagType(count) {
      if (count >= 50) return 'danger'
      if (count >= 30) return 'warning'
      if (count >= 10) return 'primary'
      return 'success'
    }
  }
}
</script>

<style scoped>
.alerts-container {
  padding: 16px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  border-radius: 8px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-3px);
}

.summary-card-danger {
  border-top: 3px solid #f56c6c;
}

.summary-card-primary {
  border-top: 3px solid #409eff;
}

.summary-card-danger-border {
  border-top: 3px solid #f56c6c;
}

.summary-card-warning-border {
  border-top: 3px solid #e6a23c;
}

.summary-card-success-border {
  border-top: 3px solid #67c23a;
}

.summary-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.summary-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-icon i {
  font-size: 28px;
  color: #fff;
}

.danger-bg {
  background: linear-gradient(135deg, #f56c6c, #e04040);
}

.primary-bg {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
}

.warning-bg {
  background: linear-gradient(135deg, #e6a23c, #d4932e);
}

.success-bg {
  background: linear-gradient(135deg, #67c23a, #5daf34);
}

.summary-info {
  flex: 1;
  min-width: 0;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.summary-divider {
  margin: 12px 0;
}

.summary-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-tag {
  flex-shrink: 0;
}

.summary-desc {
  font-size: 13px;
  color: #909399;
}

.dept-card {
  border-radius: 8px;
}

.dept-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dept-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.dept-card-title i {
  margin-right: 6px;
  color: #409eff;
}

.dept-name-cell {
  font-weight: 500;
  color: #303133;
}

.percentage-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.percentage-cell .el-progress {
  flex: 1;
}

.percentage-text {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
</style>