<template>
  <div class="app-container">
    <el-card class="mb20">
      <div slot="header">
        <span>各学院谈话统计</span>
      </div>
      <el-table v-loading="collegeLoading" :data="collegeData" border stripe>
        <el-table-column label="学院" align="center" prop="deptName" :show-overflow-tooltip="true" />
        <el-table-column label="谈话总数" align="center" prop="totalCount" sortable />
        <el-table-column label="已完成" align="center" prop="completedCount" sortable />
        <el-table-column label="待跟进" align="center" prop="pendingCount" sortable />
        <el-table-column label="辅导员数" align="center" prop="counselorCount" />
      </el-table>
    </el-card>

    <el-card class="mb20">
      <div slot="header">
        <span>辅导员谈话统计</span>
      </div>
      <el-table v-loading="counselorLoading" :data="counselorData" border stripe>
        <el-table-column label="辅导员" align="center" prop="counselorName" />
        <el-table-column label="所属学院" align="center" prop="deptName" :show-overflow-tooltip="true" />
        <el-table-column label="谈话总数" align="center" prop="totalCount" sortable />
        <el-table-column label="本月谈话" align="center" prop="monthCount" sortable />
        <el-table-column label="待跟进" align="center" prop="pendingCount" sortable />
      </el-table>
    </el-card>

    <el-card>
      <div slot="header">
        <el-row>
          <el-col :span="12">
            <span>待跟进谈话提醒</span>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <el-button type="text" icon="el-icon-refresh" @click="loadPendingFollowUps">刷新</el-button>
          </el-col>
        </el-row>
      </div>
      <el-table v-loading="pendingLoading" :data="pendingList" border stripe>
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
        <el-table-column label="最后跟进" align="center" prop="lastFollowUpTime" width="160">
          <template slot-scope="scope">
            <span>{{ scope.row.lastFollowUpTime ? parseTime(scope.row.lastFollowUpTime) : '未跟进' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template slot-scope="scope">
            <el-tag type="warning" size="small">待跟进</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pendingTotal > 0" :total="pendingTotal" :page.sync="pendingQuery.pageNum" :limit.sync="pendingQuery.pageSize" @pagination="loadPendingFollowUps" />
    </el-card>
  </div>
</template>

<script>
import { getCollegeStatistics, getCounselorStatistics, getPendingFollowUps } from "@/api/conversation/statistics"

export default {
  name: "StatisticsCollege",
  data() {
    return {
      collegeLoading: false,
      collegeData: [],
      counselorLoading: false,
      counselorData: [],
      pendingLoading: false,
      pendingList: [],
      pendingTotal: 0,
      pendingQuery: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.loadCollegeStats()
    this.loadCounselorStats()
    this.loadPendingFollowUps()
  },
  methods: {
    loadCollegeStats() {
      this.collegeLoading = true
      getCollegeStatistics().then(response => {
        this.collegeData = response.data || response.rows || []
        this.collegeLoading = false
      }).catch(() => {
        this.collegeLoading = false
      })
    },
    loadCounselorStats() {
      this.counselorLoading = true
      getCounselorStatistics().then(response => {
        this.counselorData = response.data || response.rows || []
        this.counselorLoading = false
      }).catch(() => {
        this.counselorLoading = false
      })
    },
    loadPendingFollowUps() {
      this.pendingLoading = true
      getPendingFollowUps(this.pendingQuery).then(response => {
        this.pendingList = response.rows || []
        this.pendingTotal = response.total || 0
        this.pendingLoading = false
      }).catch(() => {
        this.pendingLoading = false
      })
    }
  }
}
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
</style>