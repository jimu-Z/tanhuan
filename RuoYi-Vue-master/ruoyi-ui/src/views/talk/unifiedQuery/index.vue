<template>
  <div class="app-container">
    <el-radio-group v-model="queryMode" @change="handleModeChange" size="small" style="margin-bottom:12px">
      <el-radio-button label="records">查已有记录</el-radio-button>
      <el-radio-button label="untalked">查未谈学生</el-radio-button>
    </el-radio-group>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="72px">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width:240px"
        />
      </el-form-item>
      <el-form-item label="学号/姓名">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入学号或姓名"
          clearable
          style="width:160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item v-if="queryMode === 'records'" label="谈话类型">
        <el-select v-model="queryParams.talkType" placeholder="全部" clearable style="width:120px">
          <el-option label="个别谈话" value="individual" />
          <el-option label="集体谈话" value="group" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="queryMode === 'records'" label="内容标签">
        <el-select v-model="queryParams.tags" multiple placeholder="全部" clearable collapse-tags style="width:220px">
          <el-option v-for="item in tagOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="queryMode === 'records'" label="谈话人">
        <el-input v-model="queryParams.talkPerson" placeholder="谈话人" clearable style="width:140px" />
      </el-form-item>
      <el-form-item v-if="queryMode === 'records'" label="跟进状态">
        <el-select v-model="queryParams.followupStatus" placeholder="全部" clearable style="width:130px">
          <el-option label="待跟进" value="pending" />
          <el-option label="跟进中" value="in_progress" />
          <el-option label="已完成" value="completed" />
          <el-option label="无需跟进" value="none" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="queryMode === 'untalked'" label="班级">
        <el-cascader
          v-model="selectedDeptPath"
          :options="deptTree"
          :props="{ value: 'deptId', label: 'deptName', children: 'children', checkStrictly: true }"
          placeholder="请选择班级"
          clearable
          style="width:180px"
          @change="handleDeptChange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row v-if="queryMode === 'records'" :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          :disabled="selected.length === 0"
          @click="handleBatchExport"
          v-hasPermi="['talk:session:export']"
        >批量导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-row v-if="queryMode === 'untalked'" :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-tabs v-if="queryMode === 'records'" v-model="talkTypeFilter" @tab-click="handleTabClick" style="margin-bottom:8px">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="个别谈话" name="individual" />
      <el-tab-pane label="集体谈话" name="group" />
    </el-tabs>

    <el-table v-loading="loading" :data="displayList" @selection-change="handleSelectionChange" :row-key="getRowKey" :key="queryMode">
      <el-table-column v-if="queryMode === 'records'" type="selection" width="50" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column v-if="queryMode === 'records'" label="会话ID" align="center" prop="sessionId" width="80" />
      <el-table-column v-if="queryMode === 'records'" label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column v-if="queryMode === 'records'" label="学号" align="center" prop="studentCode" width="130" />
      <el-table-column v-if="queryMode === 'records'" label="谈话类型" align="center" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.talkType === 'individual'" type="primary" size="small">个别谈话</el-tag>
          <el-tag v-else-if="scope.row.talkType === 'group'" type="success" size="small">集体谈话</el-tag>
          <span v-else>{{ scope.row.talkType }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="queryMode === 'records'" label="谈话时间" align="center" width="110">
        <template slot-scope="scope">
          <span>{{ scope.row.talkTime ? scope.row.talkTime.substring(0, 10) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="queryMode === 'records'" label="跟进状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.followupStatus === 'pending'" type="warning" size="mini">待跟进</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'in_progress'" type="primary" size="mini">跟进中</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'completed'" type="success" size="mini">已完成</el-tag>
          <el-tag v-else-if="scope.row.followupStatus === 'none'" type="info" size="mini">无需跟进</el-tag>
          <span v-else style="color:#c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="queryMode === 'records'" label="谈话内容" align="center" prop="talkContent" min-width="200" show-overflow-tooltip />
      <el-table-column v-if="queryMode === 'records'" label="内容标签" align="center" width="180">
        <template slot-scope="scope">
          <span v-if="tagDataMap[scope.row.sessionId] && tagDataMap[scope.row.sessionId].length > 0">
            <el-tag
              v-for="(tag, ti) in tagDataMap[scope.row.sessionId]"
              :key="ti"
              size="mini"
              type="info"
              style="margin:2px"
            >{{ tag }}</el-tag>
          </span>
          <span v-else style="color:#c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="queryMode === 'records'" label="操作" align="center" width="80">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExportRow(scope.row)"
          >导出</el-button>
        </template>
      </el-table-column>

      <el-table-column v-if="queryMode === 'untalked'" label="学号" align="center" prop="studentCode" width="130" />
      <el-table-column v-if="queryMode === 'untalked'" label="姓名" align="center" prop="studentName" width="120" />
      <el-table-column v-if="queryMode === 'untalked'" label="班级" align="center" prop="deptName" width="200" show-overflow-tooltip />
      <el-table-column v-if="queryMode === 'untalked'" label="上次谈话时间" align="center" width="140">
        <template slot-scope="scope">
          <span>{{ scope.row.lastTalkTime ? scope.row.lastTalkTime.substring(0, 10) : '-' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listTalksession, getSessionTags, getBatchTags, TAG_LABELS } from '@/api/talk/talkSession'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { getTalk, getDeptTree, listUntalked } from '@/api/talk/talkStudent'
import request from '@/utils/request'

export default {
  name: 'UnifiedQuery',
  data() {
    return {
      loading: false,
      showSearch: true,
      selected: [],
      total: 0,
      talkTypeFilter: 'all',
      allData: [],
      displayList: [],
      tagDataMap: {},

      queryMode: 'records',
      deptTree: [],
      selectedDeptPath: [],
      selectedDeptId: null,

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dateRange: null,
        keyword: '',
        talkType: '',
        tags: [],
        talkPerson: '',
        followupStatus: ''
      },

      tagOptions: Object.keys(TAG_LABELS).map(k => ({ value: k, label: TAG_LABELS[k] }))
    }
  },
  computed: {
    getRowKey() {
      if (this.queryMode === 'records') {
        return 'rowKey'
      }
      return 'studentId'
    }
  },
  created() {
    this.loadDeptTree()
    this.getList()
  },
  methods: {
    loadDeptTree() {
      getDeptTree().then(response => {
        this.deptTree = response.data || []
      }).catch(() => {
        this.deptTree = []
        this.$modal.msgError('加载部门树失败')
      })
    },

    handleModeChange() {
      this.queryParams.pageNum = 1
      this.selected = []
      this.allData = []
      this.displayList = []
      this.total = 0
      if (this.queryMode === 'untalked') {
        this.selectedDeptPath = []
        this.selectedDeptId = null
      }
      this.getList()
    },

    handleDeptChange(value) {
      if (value && value.length > 0) {
        this.selectedDeptId = value[value.length - 1]
      } else {
        this.selectedDeptId = null
      }
    },

    getList() {
      if (this.queryMode === 'untalked') {
        this.loadUntalkedList()
        return
      }
      this.loadRecordsList()
    },

    loadUntalkedList() {
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize
      }
      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        params.beginDate = this.queryParams.dateRange[0]
        params.endDate = this.queryParams.dateRange[1]
      }
      if (this.selectedDeptId) {
        params.deptId = this.selectedDeptId
      }
      if (this.queryParams.keyword) {
        params.keyword = this.queryParams.keyword
      }

      listUntalked(params).then(response => {
        this.displayList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.displayList = []
        this.total = 0
        this.loading = false
        this.$modal.msgError('查询未谈学生失败')
      })
    },

    loadRecordsList() {
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize
      }

      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        params.beginTime = this.queryParams.dateRange[0]
        params.endTime = this.queryParams.dateRange[1]
      }

      if (this.talkTypeFilter && this.talkTypeFilter !== 'all') {
         params.talkType = this.talkTypeFilter
       } else if (this.queryParams.talkType) {
         params.talkType = this.queryParams.talkType
       }

      const hasFrontendFilters = this.queryParams.keyword ||
        (this.queryParams.tags && this.queryParams.tags.length > 0) ||
        (this.queryParams.dateRange && this.queryParams.dateRange.length === 2)

      if (hasFrontendFilters) {
        params.pageNum = 1
        params.pageSize = 9999
      }

      listTalksession(params).then(response => {
        const sessions = response.rows || []
        if (sessions.length === 0) {
           this.allData = []
           this.tagDataMap = {}
           this.total = 0
           this.displayList = []
           this.loading = false
           return
         }

        this.loadTagsForSessions(sessions)

        const sessionIds = sessions.map(s => s.sessionId)
        listTalkrecord({ sessionIds: sessionIds.join(','), pageSize: 9999 }).then(recRes => {
          const records = recRes.rows || []
          const sessionMap = {}
          sessions.forEach(s => { sessionMap[s.sessionId] = s })

          const studentIds = [...new Set(records.map(r => r.studentId))]
          const studentMap = {}
          Promise.all(studentIds.map(id =>
            getTalk(id).then(stu => {
              if (stu.data) { studentMap[id] = stu.data }
            }).catch(() => {})
          )).then(() => {
            this.allData = records.map(record => {
              const session = sessionMap[record.sessionId] || {}
              const student = studentMap[record.studentId] || {}
              return {
                rowKey: record.sessionId + '_' + record.recordId,
                sessionId: record.sessionId,
                talkType: session.talkType || '',
                talkTime: session.talkTime || '',
                talkContent: session.talkContent || '',
                talkPerson: session.talkPerson || '',
                talkLocation: session.talkLocation || '',
                recordId: record.recordId,
                studentId: record.studentId,
                studentName: student.studentName || '',
                studentCode: student.studentCode || '',
                studentFeedback: record.studentFeedback,
                followupPlan: record.followupPlan,
                followupStatus: record.followupStatus
              }
            })

            if (hasFrontendFilters) {
              this.applyFilters()
            } else {
              this.total = response.total
              this.displayList = this.allData.slice()
            }
            this.loading = false
          })
        }).catch(() => {
          this.allData = []
          this.total = 0
          this.displayList = []
          this.loading = false
        })
      }).catch(() => {
        this.allData = []
        this.total = 0
        this.displayList = []
        this.loading = false
      })
    },

    loadTagsForSessions(sessions) {
      const sessionIds = sessions.map(s => s.sessionId)
      getBatchTags(sessionIds).then(res => {
        const data = res.data || {}
        const newTagMap = {}
        Object.keys(data).forEach(k => {
          newTagMap[Number(k)] = (data[k] || []).map(t => TAG_LABELS[t.tagValue] || t.tagValue)
        })
        this.tagDataMap = newTagMap
      }).catch(() => {
        this.tagDataMap = {}
      })
    },

    applyFilters() {
      let data = this.allData.slice()

      if (this.queryParams.keyword) {
        const kw = this.queryParams.keyword.toLowerCase()
        data = data.filter(row =>
          (row.studentName || '').toLowerCase().indexOf(kw) > -1 ||
          (row.studentCode || '').toLowerCase().indexOf(kw) > -1
        )
      }

      if (this.talkTypeFilter && this.talkTypeFilter !== 'all') {
        data = data.filter(row => row.talkType === this.talkTypeFilter)
      } else if (this.queryParams.talkType) {
        data = data.filter(row => row.talkType === this.queryParams.talkType)
      }

      if (this.queryParams.talkPerson) {
        const tp = this.queryParams.talkPerson.toLowerCase()
        data = data.filter(row => (row.talkPerson || '').toLowerCase().indexOf(tp) > -1)
      }

      if (this.queryParams.followupStatus) {
        data = data.filter(row => row.followupStatus === this.queryParams.followupStatus)
      }

      if (this.queryParams.tags && this.queryParams.tags.length > 0) {
        const selectedTags = this.queryParams.tags
        data = data.filter(row => {
          const rowTags = this.tagDataMap[row.sessionId] || []
          return selectedTags.some(t => rowTags.indexOf(TAG_LABELS[t] || t) > -1)
        })
      }

      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        const start = this.queryParams.dateRange[0]
        const end = this.queryParams.dateRange[1]
        data = data.filter(row => {
          if (!row.talkTime) return false
          const date = row.talkTime.substring(0, 10)
          return date >= start && date <= end
        })
      }

      this.total = data.length
      const startIdx = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      this.displayList = data.slice(startIdx, startIdx + this.queryParams.pageSize)
    },

    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        dateRange: null,
        keyword: '',
        talkType: '',
        tags: [],
        talkPerson: '',
        followupStatus: ''
      }
      this.talkTypeFilter = 'all'
      this.selectedDeptPath = []
      this.selectedDeptId = null
      this.handleQuery()
    },

    handleTabClick() {
      this.queryParams.pageNum = 1
      this.queryParams.talkType = ''
      this.getList()
    },

    handleSelectionChange(selection) {
      this.selected = selection
    },

    handleExportRow(row) {
      this.$modal.confirm('确认导出该条谈话记录吗？').then(() => {
        return request({
          url: '/ruoyi-system/talksession/exportDocx/' + row.sessionId,
          method: 'get',
          responseType: 'blob'
        })
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = '谈话记录_' + (row.studentName || row.sessionId) + '.docx'
        a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {
        this.$modal.msgError('导出失败')
      })
    },

    handleBatchExport() {
      if (this.selected.length === 0) {
        this.$modal.msgWarning('请至少选择一条记录')
        return
      }
      const sessionIds = [...new Set(this.selected.map(item => item.sessionId))]
      this.$modal.confirm('确认导出选中的 ' + sessionIds.length + ' 条会话记录吗？').then(() => {
        return request({
          url: '/ruoyi-system/talksession/exportDocx/batch',
          method: 'post',
          data: sessionIds,
          responseType: 'blob'
        })
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = '谈话记录批量导出.zip'
        a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {
        this.$modal.msgError('批量导出失败')
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

.search-section {
  background: #ffffff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(26, 82, 118, 0.08);
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;

  .el-input__inner {
    height: 36px;
    line-height: 36px;
    border-radius: 6px;
    border: 1px solid #d4e0eb;
    background: #ffffff;
    transition: all 0.3s ease;

    &:hover {
      border-color: #2a6fa8;
    }

    &:focus {
      border-color: #1a5276;
      box-shadow: 0 0 0 3px rgba(26, 82, 118, 0.08);
      background: #ffffff;
    }
  }

  ::v-deep .el-input__prefix {
    color: #2a6fa8;
  }

  ::v-deep .el-button--primary {
    background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
    border: none;
    box-shadow: 0 2px 8px rgba(26, 82, 118, 0.25);
    transition: all 0.3s ease;
    height: 36px;

    &:hover {
      background: linear-gradient(135deg, #1e5f8a 0%, #3080ba 100%);
      box-shadow: 0 4px 12px rgba(26, 82, 118, 0.35);
      transform: translateY(-1px);
    }
  }
}

::v-deep .el-tabs__item.is-active {
  color: #1a5276;
}
::v-deep .el-tabs__active-bar {
  background-color: #1a5276;
}
::v-deep .el-tabs__item:hover {
  color: #2a6fa8;
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

  ::v-deep .el-table__body tr.current-row > td {
    background: #e8f4f8 !important;
  }
}

.small-padding {
  ::v-deep .el-button--text {
    padding: 4px 8px;
    font-size: 13px;

    &:first-child {
      color: #2a6fa8;
      &:hover { color: #3a85c0; text-decoration: underline; }
    }

    &:nth-child(2) {
      color: #e64340;
      &:hover { color: #f06060; text-decoration: underline; }
    }
  }
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

    .el-pagination__sizes {
      .el-input__inner {
        border-color: #d4e0eb;
        &:hover { border-color: #1a5276; }
      }
    }
  }
}

::v-deep .el-dialog {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(10, 37, 64, 0.2);

  .el-dialog__header {
    background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
    padding: 16px 20px;
    margin: 0;

    .el-dialog__title {
      color: #ffffff;
      font-weight: 600;
      font-size: 16px;
    }

    .el-dialog__headerbtn {
      .el-dialog__close {
        color: rgba(255, 255, 255, 0.8);
        &:hover {
          color: #ffffff;
          transform: rotate(90deg);
        }
      }
    }
  }

  .el-dialog__body {
    padding: 24px 20px;
    background: #ffffff;

    .el-form-item__label {
      color: #1a5276;
      font-weight: 500;
    }

    .el-input__inner,
    .el-textarea__inner {
      border-radius: 6px;
      border: 1px solid #d4e0eb;

      &:hover { border-color: #2a6fa8; }

      &:focus {
        border-color: #1a5276;
        box-shadow: 0 0 0 3px rgba(26, 82, 118, 0.08);
      }
    }

    .el-select .el-input__inner {
      border-radius: 6px;
    }
  }

  .el-dialog__footer {
    padding: 12px 20px 20px;
    background: #fafbfd;
    border-top: 1px solid #e8edf2;
    text-align: center;

    .el-button--primary {
      background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(26, 82, 118, 0.3);
      min-width: 100px;

      &:hover {
        background: linear-gradient(135deg, #1e5f8a 0%, #3080ba 100%);
        box-shadow: 0 6px 20px rgba(26, 82, 118, 0.4);
        transform: translateY(-1px);
      }
    }

    .el-button--default {
      border-color: #d4e0eb;
      color: #606266;
      background: #ffffff;
      min-width: 100px;

      &:hover {
        border-color: #1a5276;
        color: #1a5276;
        background: #f0f6fc;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .app-container {
    padding: 12px;
  }

  .search-section {
    flex-direction: column;

    ::v-deep .el-input {
      width: 100% !important;
    }
  }
}
</style>
