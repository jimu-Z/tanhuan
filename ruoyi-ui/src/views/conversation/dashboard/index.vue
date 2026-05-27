<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon pending">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount }}</div>
              <div class="stat-label">待跟进谈话</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon monthly">
              <i class="el-icon-s-order"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.thisMonthCount }}</div>
              <div class="stat-label">本月谈话</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon student">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudentCount }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mt20 mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="medium" @click="handleAdd">新建谈话</el-button>
      </el-col>
    </el-row>

    <el-card class="mt10">
      <div slot="header">
        <span>最近谈话记录</span>
      </div>
      <el-table v-loading="loading" :data="recentList" border stripe>
        <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
        <el-table-column label="学号" align="center" prop="studentNo" width="120" />
        <el-table-column label="学院" align="center" prop="collegeName" :show-overflow-tooltip="true" />
        <el-table-column label="谈话主题" align="center" prop="topic" :show-overflow-tooltip="true" />
        <el-table-column label="谈话人" align="center" prop="speaker" width="100" />
        <el-table-column label="谈话时间" align="center" prop="conversationTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.conversationTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'warning'" size="small">
              {{ scope.row.status === '0' ? '已完成' : '待跟进' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="recentList.length === 0" style="text-align: center; padding: 40px; color: #909399">
        暂无谈话记录
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDashboardData, getRecentRecords } from "@/api/conversation/dashboard"

export default {
  name: "Dashboard",
  data() {
    return {
      loading: false,
      stats: {
        pendingCount: 0,
        thisMonthCount: 0,
        totalStudentCount: 0
      },
      recentList: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      getDashboardData().then(response => {
        this.stats = response.data || {}
      })
      this.loading = true
      getRecentRecords().then(response => {
        this.recentList = response.data || response.rows || []
        this.loading = false
      })
    },
    handleAdd() {
      this.$router.push({ path: '/conversation/record/add' })
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 16px;
}
.stat-icon.pending {
  background: linear-gradient(135deg, #F56C6C, #E6A23C);
}
.stat-icon.monthly {
  background: linear-gradient(135deg, #409EFF, #67C23A);
}
.stat-icon.student {
  background: linear-gradient(135deg, #9B59B6, #3498DB);
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.mt20 {
  margin-top: 20px;
}
.mt10 {
  margin-top: 10px;
}
.mb8 {
  margin-bottom: 8px;
}
</style>