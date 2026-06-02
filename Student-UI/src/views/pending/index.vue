<template>
  <div class="pending-page">
    <h3 class="page-title">待处理谈话</h3>
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%;">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="createTime" label="谈话时间" width="180" align="center" />
      <el-table-column prop="studentName" label="谈话人" width="120" align="center" />
      <el-table-column prop="followupPlan" label="谈话内容" min-width="250" show-overflow-tooltip />
      <el-table-column label="反馈状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.studentFeedback === '无' || !scope.row.studentFeedback" type="danger">待反馈</el-tag>
          <el-tag v-else type="success">已反馈</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template slot-scope="scope">
          <el-button v-if="scope.row.studentFeedback === '无' || !scope.row.studentFeedback"
            type="primary" size="small" @click="openFeedback(scope.row)">
            填写反馈
          </el-button>
          <span v-else style="color: #67C23A;">已完成</span>
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

    <el-dialog title="填写反馈" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="反馈内容" prop="studentFeedback">
          <el-input v-model="feedbackForm.studentFeedback" type="textarea" :rows="5"
            placeholder="请输入您的反馈意见" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitFeedback">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPendingTalks, submitFeedback } from '@/api/talk/studentApi'

export default {
  name: 'PendingTalks',
  data() {
    return {
      loading: false,
      submitLoading: false,
      dialogVisible: false,
      tableData: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      feedbackForm: {
        recordId: null,
        studentFeedback: ''
      },
      feedbackRules: {
        studentFeedback: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
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
        ...this.queryParams,
        studentFeedback: '无',
        notified: 1
      }
      getPendingTalks(params).then(res => {
        this.records = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => { this.loading = false; this.$message.error('加载失败') })
    },
    openFeedback(row) {
      this.feedbackForm = {
        recordId: row.recordId,
        studentFeedback: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.feedbackForm && this.$refs.feedbackForm.clearValidate()
      })
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (valid) {
          this.submitLoading = true
          submitFeedback({
            recordId: this.feedbackForm.recordId,
            studentFeedback: this.feedbackForm.studentFeedback
          }).then(() => {
            this.$message.success('反馈提交成功')
            this.dialogVisible = false
            this.submitLoading = false
            this.$store.commit('SET_PENDING_COUNT', Math.max(0, (this.$store.state.pendingCount || 0) - 1))
            this.fetchList()
          }).catch(() => {
            this.submitLoading = false
            this.$message.error('提交失败')
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.pending-page {
  padding: 10px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
