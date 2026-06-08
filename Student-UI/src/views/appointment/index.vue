<template>
  <div class="appointment-list-page">
    <h3 class="page-title">我的预约</h3>

    <!-- 统计卡片 -->
    <el-row :gutter="12" class="stats-row">
      <el-col :xs="8" :sm="8">
        <div class="stat-card stat-blue">
          <div class="stat-value">{{ statPending }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8">
        <div class="stat-card stat-green">
          <div class="stat-value">{{ statConfirmed }}</div>
          <div class="stat-label">已确认</div>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8">
        <div class="stat-card stat-gray">
          <div class="stat-value">{{ statCompleted }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-col>
    </el-row>

    <!-- 预约列表 -->
    <div v-loading="loading" class="appointment-list">
      <div v-for="item in list" :key="item.id || item.appointmentId" class="appointment-card">
        <div class="card-top">
          <div class="card-teacher">
            <i class="el-icon-user-solid"></i>
            <span>{{ item.counselorName || item.teacherName || '-' }}</span>
          </div>
          <el-tag :type="statusTagType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
        </div>
        <div class="card-body">
          <div class="card-row">
            <i class="el-icon-time"></i>
            <span>{{ formatTime(item.appointmentTime) }}</span>
          </div>
          <div v-if="item.location" class="card-row">
            <i class="el-icon-location-outline"></i>
            <span>{{ item.location }}</span>
          </div>
          <div v-if="item.reason" class="card-row reason-row">
            <i class="el-icon-edit-outline"></i>
            <span>{{ item.reason }}</span>
          </div>
        </div>
        <div v-if="item.status === 'pending'" class="card-footer">
          <el-button type="danger" size="small" plain @click="handleCancel(item)">取消预约</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-chat-line-square"></i>
        </div>
        <p>暂无预约记录</p>
      </div>
    </div>
  </div>
</template>

<script>
import { listMyAppointments, cancelAppointment } from '@/api/talk/appointmentApi'

export default {
  name: 'AppointmentList',
  data() {
    return {
      loading: false,
      list: []
    }
  },
  computed: {
    statPending() {
      return this.list.filter(i => i.status === 'pending').length
    },
    statConfirmed() {
      return this.list.filter(i => i.status === 'confirmed').length
    },
    statCompleted() {
      return this.list.filter(i => i.status === 'completed').length
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    fetchList() {
      this.loading = true
      listMyAppointments().then(res => {
        this.list = res.data || res.rows || res || []
      }).catch(() => {
        this.list = []
      }).finally(() => {
        this.loading = false
      })
    },
    formatTime(time) {
      if (!time) return '-'
      if (typeof time === 'string' && time.length >= 16) {
        return time.substring(0, 16)
      }
      return time
    },
    statusTagType(status) {
      const map = {
        'pending': '',
        'confirmed': 'success',
        'rejected': 'danger',
        'completed': 'info',
        'cancelled': 'warning'
      }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = {
        'pending': '待确认',
        'confirmed': '已确认',
        'rejected': '已拒绝',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return map[status] || status || '未知'
    },
    handleCancel(item) {
      const id = item.appointmentId
      this.$confirm('确定要取消该预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelAppointment(id).then(() => {
          this.$message.success('已取消预约')
          this.fetchList()
        }).catch(() => {
          this.$message.error('取消失败')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.appointment-list-page {
  padding: 16px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-left: 12px;
  border-left: 3px solid #2a6fa8;
  line-height: 1;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 16px 8px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-blue .stat-value {
  color: #2a6fa8;
}

.stat-green .stat-value {
  color: #52c41a;
}

.stat-gray .stat-value {
  color: #909399;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 预约卡片 */
.appointment-list {
  min-height: 200px;
}

.appointment-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-teacher {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.card-teacher i {
  color: #2a6fa8;
  font-size: 16px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.card-row i {
  color: #909399;
  font-size: 14px;
  width: 16px;
  text-align: center;
}

.reason-row {
  color: #909399;
  font-size: 12px;
}

.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
  text-align: right;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .appointment-list-page {
    padding: 10px;
  }
}
</style>
