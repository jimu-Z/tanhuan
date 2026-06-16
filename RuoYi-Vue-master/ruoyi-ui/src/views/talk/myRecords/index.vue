<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="myRecordList">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column label="谈话类型" align="center" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.talkType === 'individual'" type="primary" size="small">个别</el-tag>
          <el-tag v-else type="success" size="small">集体</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="谈话时间" align="center" width="120">
        <template slot-scope="scope">{{ scope.row.talkTime ? scope.row.talkTime.substring(0,10) : '' }}</template>
      </el-table-column>
      <el-table-column label="谈话地点" prop="talkLocation" min-width="120" align="center" show-overflow-tooltip />
      <el-table-column label="谈话内容" prop="talkContent" min-width="300" align="center" show-overflow-tooltip />
      <el-table-column label="谈话人" prop="talkPerson" width="100" align="center" />
      <el-table-column label="我的反馈" align="center" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.studentFeedback" style="color:#666">{{ scope.row.studentFeedback }}</span>
          <span v-else style="color:#ccc">未填写</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleFeedback(scope.row)">
            {{ scope.row.studentFeedback ? '修改反馈' : '提交反馈' }}
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-download" @click="handleExport(scope.row)">导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 学生提交/修改反馈对话框 -->
    <el-dialog :title="feedbackTitle" :visible.sync="feedbackOpen" width="600px" append-to-body>
      <el-form ref="feedbackForm" :model="feedbackForm" label-width="100px">
        <el-form-item label="谈话时间">
          <el-input :value="currentRecord.talkTime" disabled />
        </el-form-item>
        <el-form-item label="谈话内容">
          <el-input :value="currentRecord.talkContent" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-divider>我的反馈</el-divider>
        <el-form-item label="学生反馈" prop="studentFeedback">
          <el-input
            v-model="feedbackForm.studentFeedback"
            type="textarea"
            :rows="6"
            placeholder="请输入您的反馈意见..."
          />
        </el-form-item>
        <el-alert
          title="提示：提交反馈后，教师将收到通知"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="feedbackOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitFeedbackForm" :loading="feedbackLoading">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyRecords, submitFeedback } from '@/api/talk/talkStudentRecord'
import { exportDocx } from '@/api/talk/talkSession'

export default {
  name: 'MyRecords',
  data() {
    return {
      loading: false,
      showSearch: false,
      total: 0,
      myRecordList: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      // 反馈相关
      feedbackOpen: false,
      feedbackTitle: '提交反馈',
      feedbackForm: {},
      feedbackLoading: false,
      currentRecord: {}
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      getMyRecords(this.queryParams).then(res => {
        this.myRecordList = res.rows || []
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false; this.$modal.msgError('加载我的谈话记录失败') })
    },
    handleFeedback(row) {
      this.currentRecord = row
      this.feedbackTitle = row.studentFeedback ? '修改反馈' : '提交反馈'
      this.feedbackForm = {
        recordId: row.recordId,
        studentFeedback: row.studentFeedback || ''
      }
      this.feedbackOpen = true
    },
    submitFeedbackForm() {
      if (!this.feedbackForm.studentFeedback) {
        this.$modal.msgWarning('请输入反馈内容')
        return
      }
      this.feedbackLoading = true
      submitFeedback(this.feedbackForm).then(response => {
        this.$modal.msgSuccess('反馈提交成功，教师将收到通知')
        this.feedbackOpen = false
        this.getList()
      }).catch(() => {
        this.$modal.msgError('反馈提交失败')
      }).finally(() => {
        this.feedbackLoading = false
      })
    },
    handleExport(row) {
      const personName = row.talkPerson || '未知'
      this.$modal.confirm('导出' + personName + '的谈话记录？').then(() => {
        return exportDocx(row.sessionId)
      }).then(blob => {
        const blobUrl = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = blobUrl
        const contentType = blob.type || ''
        const actualExt = contentType.includes('zip') ? '.zip' : '.docx'
        a.download = '谈话记录_' + personName + actualExt
        a.click()
        window.URL.revokeObjectURL(blobUrl)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {
        this.$modal.msgError('导出失败')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/assets/styles/variables.scss";

.app-container {
  padding: 20px;
  background: linear-gradient(160deg, #f0f5fa 0%, #e8edf2 100%);
  min-height: calc(100vh - 84px);
}

.mb8 {
  ::v-deep .el-button--primary[plain] {
    border-color: #1a5276;
    color: #1a5276;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(26, 82, 118, 0.3);
      transform: translateY(-1px);
    }
  }
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(26, 82, 118, 0.08);

  ::v-deep th.el-table__cell {
    background: linear-gradient(135deg, #2a6fa8 0%, #4a8fc7 100%) !important;
    color: #ffffff;
    font-weight: 600;
    font-size: 14px;
    border-color: #e8edf2;
    padding: 12px 0;
  }

  ::v-deep td.el-table__cell {
    border-color: #e8edf2;
    font-size: 13px;
    padding: 10px 0;
  }

  ::v-deep .el-table__body tr {
    transition: background 0.2s ease;

    &:hover > td {
      background: #f0f6fc !important;
    }
  }

  ::v-deep .el-table__body tr:nth-child(even) {
    background: #fafbfd;
  }

  ::v-deep .el-table__body tr:nth-child(odd) {
    background: #ffffff;
  }
}

::v-deep .el-tag--primary {
  background: rgba(42, 111, 168, 0.1);
  border-color: #2a6fa8;
  color: #1a5276;
}

::v-deep .el-tag--success {
  background: rgba(103, 194, 58, 0.1);
  border-color: #67c23a;
  color: #529b2e;
}

::v-deep .pagination-container {
  margin-top: 16px;
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(26, 82, 118, 0.06);
  display: flex;
  justify-content: center;

  .el-pagination {
    .btn-prev,
    .btn-next,
    .el-pager li {
      background: #ffffff;
      color: #1a5276;
      border-radius: 4px;

      &:hover {
        background: #f0f6fc;
        color: #1a5276;
      }

      &.active {
        background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
        color: #ffffff;
      }
    }
  }
}
</style>
