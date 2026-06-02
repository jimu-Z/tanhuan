<template>
  <div class="talks-page">
    <h3 class="page-title">我的谈话记录</h3>
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%;">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="createTime" label="谈话时间" width="180" align="center" />
      <el-table-column prop="studentName" label="谈话人" width="120" align="center" />
      <el-table-column prop="followupStatus" label="谈话类型" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.followupStatus === '1'" type="warning">首次谈话</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === '2'" type="success">跟进谈话</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === '0'" type="danger">紧急谈话</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="followupPlan" label="谈话内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="内容标签" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="small" type="info">{{ scope.row.followupStatus || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="反馈状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.studentFeedback === '无' || !scope.row.studentFeedback" type="info">未反馈</el-tag>
          <el-tag v-else type="success">已反馈</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > 0"
      style="margin-top: 20px; text-align: right;"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="queryParams.pageSize"
      :current-page.sync="queryParams.pageNum"
      @current-change="fetchList"
    />
  </div>
</template>

<script>
import { getMyRecords } from '@/api/talk/studentApi'

export default {
  name: 'TalksList',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    fetchList() {
      this.loading = true
      const params = {
        ...this.queryParams
      }
      getMyRecords(params).then(res => {
        this.tableData = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.talks-page {
  padding: 10px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
