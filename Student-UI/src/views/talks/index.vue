<template>
  <div class="talks-page">
    <h3 class="page-title">我的谈话记录</h3>
    <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%;">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="talkTime" label="谈话时间" width="180" align="center" />
      <el-table-column prop="talkPerson" label="谈话人" width="120" align="center" />
      <el-table-column prop="followupStatus" label="跟进状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.followupStatus === 'none' || scope.row.followupStatus === null" type="info">无需跟进</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'pending'" type="warning">待跟进</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'in_progress'" type="primary">跟进中</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'completed'" type="success">已完成</el-tag>
          <span v-else>{{ scope.row.followupStatus || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="followupPlan" label="跟进计划" min-width="200" show-overflow-tooltip />
      <el-table-column label="反馈状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag v-if="!scope.row.studentFeedback || scope.row.studentFeedback === '无'" type="info">未反馈</el-tag>
          <el-tag v-else type="success">已反馈</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template slot-scope="scope">
          <el-button
            size="small"
            :type="!scope.row.studentFeedback || scope.row.studentFeedback === '无' ? 'primary' : 'text'"
            @click="openFeedback(scope.row)">
            {{ !scope.row.studentFeedback || scope.row.studentFeedback === '无' ? '填写反馈' : '修改反馈' }}
          </el-button>
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

    <el-dialog :title="feedbackDialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
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
import { getMyRecords, submitFeedback } from '@/api/talk/studentApi'

export default {
  name: 'TalksList',
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
  computed: {
    feedbackDialogTitle() {
      return this.feedbackForm._isEdit ? '修改反馈' : '填写反馈'
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
    },
    openFeedback(row) {
      this.feedbackForm = {
        recordId: row.recordId,
        studentFeedback: row.studentFeedback || '',
        _isEdit: !!row.studentFeedback && row.studentFeedback !== '无'
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.feedbackForm && this.$refs.feedbackForm.clearValidate()
      })
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (valid) {
          if (!this.feedbackForm.studentFeedback) {
            this.$message.warning('请输入反馈内容')
            return
          }
          this.submitLoading = true
          submitFeedback({
            recordId: this.feedbackForm.recordId,
            studentFeedback: this.feedbackForm.studentFeedback
          }).then(() => {
            this.$message.success('反馈提交成功')
            this.dialogVisible = false
            this.submitLoading = false
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
.talks-page {
  padding: 10px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
