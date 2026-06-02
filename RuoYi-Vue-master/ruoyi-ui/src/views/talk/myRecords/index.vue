<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="$router.push('/talk/talkInitiate/index')">发起谈话</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="talksessionList">
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
      <el-table-column label="谈话内容" prop="talkContent" min-width="200" align="center" show-overflow-tooltip />
      <el-table-column label="参与学生" min-width="120" align="center">
        <template slot-scope="scope">
          <span v-if="studentMap[scope.row.sessionId]">
            {{ studentMap[scope.row.sessionId].join('、') }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="100" align="center">
        <template slot-scope="scope">
          <span v-if="tagMap[scope.row.sessionId]" style="font-size:12px;color:#666">
            {{ tagMap[scope.row.sessionId].map(t => getTagLabel(t.tagValue)).join('、') || '-' }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-download" @click="handleExport(scope.row)">导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listTalksession, getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'
import request from '@/utils/request'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { getTalk } from '@/api/talk/talkStudent'

export default {
  name: 'MyRecords',
  data() {
    return {
      loading: false,
      showSearch: false,
      total: 0,
      talksessionList: [],
      tagMap: {},
      studentMap: {},
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTalksession(this.queryParams).then(res => {
        this.talksessionList = res.rows || []
        this.total = res.total
        this.loadTags()
        this.loadStudents()
      }).catch(() => { this.loading = false; this.$modal.msgError('加载失败') }).finally(() => { this.loading = false })
    },
    loadTags() {
      this.talksessionList.forEach(s => {
        getSessionTags(s.sessionId).then(r => this.$set(this.tagMap, s.sessionId, r.data || [])).catch(() => { this.$modal.msgError('加载标签失败') })
      })
    },
    loadStudents() {
      this.talksessionList.forEach(s => {
        listTalkrecord({ sessionId: s.sessionId, pageSize: 999 }).then(r => {
          const ids = (r.rows || []).map(rec => rec.studentId)
          Promise.all(ids.map(id => getTalk(id))).then(students => {
            this.$set(this.studentMap, s.sessionId, students.map(st => st.data ? st.data.studentName : '-'))
          }).catch(() => { this.$modal.msgError('操作失败') })
        }).catch(() => { this.$modal.msgError('操作失败') })
      })
    },
    getTagLabel(v) { return TAG_LABELS[v] || v },
    handleExport(row) {
      let url = '/ruoyi-system/talksession/exportDocx/' + row.sessionId;
      this.$modal.confirm('导出' + row.talkPerson + '的谈话记录？').then(() => {
        return request({ url: url, method: 'get', responseType: 'blob' }).catch(() => { this.$modal.msgError('导出失败') })
      }).then(blob => {
        const blobUrl = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = blobUrl
        const contentType = blob.type || ''
        const actualExt = contentType.includes('zip') ? '.zip' : '.docx'
        a.download = '谈话记录_' + (row.talkPerson || row.sessionId) + actualExt
        a.click(); window.URL.revokeObjectURL(blobUrl)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => { this.$modal.msgError('导出失败') })
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
