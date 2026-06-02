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
      <el-tab-pane label="全部" name="" />
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
import { listTalksession, getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'
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
      talkTypeFilter: '',
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
      const params = { ...this.queryParams }
      if (params.dateRange && params.dateRange.length === 2) {
        params.beginTime = params.dateRange[0]
        params.endTime = params.dateRange[1]
      }
      delete params.dateRange
      delete params.keyword
      delete params.tags

      const hasFrontendFilters = this.queryParams.keyword || this.queryParams.talkType ||
        (this.queryParams.tags && this.queryParams.tags.length > 0) ||
        (this.queryParams.dateRange && this.queryParams.dateRange.length === 2)

      if (this.talkTypeFilter) {
        params.talkType = this.talkTypeFilter
      }

      if (hasFrontendFilters) {
        params.pageNum = 1
        params.pageSize = 9999
      }

      listTalksession(params).then(response => {
        const sessions = response.rows || []
        if (sessions.length === 0) {
          this.allData = []
          this.tagDataMap = {}
          this.applyFilters()
          this.loading = false
          return
        }

        this.loadTagsForSessions(sessions)

        const recordPromises = sessions.map(async session => {
          try {
            const recRes = await listTalkrecord({ sessionId: session.sessionId, pageSize: 999 })
            const records = recRes.rows || []
            const enriched = await Promise.all(records.map(async record => {
              try {
                const stuRes = await getTalk(record.studentId)
                return {
                  rowKey: session.sessionId + '_' + record.recordId,
                  sessionId: session.sessionId,
                  talkType: session.talkType,
                  talkTime: session.talkTime,
                  talkContent: session.talkContent,
                  talkPerson: session.talkPerson,
                  talkLocation: session.talkLocation,
                  recordId: record.recordId,
                  studentId: record.studentId,
                  studentName: stuRes.data ? stuRes.data.studentName : '',
                  studentCode: stuRes.data ? stuRes.data.studentCode : '',
                  studentFeedback: record.studentFeedback,
                  followupPlan: record.followupPlan,
                  followupStatus: record.followupStatus
                }
              } catch (e) {
                return {
                  rowKey: session.sessionId + '_' + record.recordId,
                  sessionId: session.sessionId,
                  talkType: session.talkType,
                  talkTime: session.talkTime,
                  talkContent: session.talkContent,
                  talkPerson: session.talkPerson,
                  talkLocation: session.talkLocation,
                  recordId: record.recordId,
                  studentId: record.studentId,
                  studentName: '',
                  studentCode: '',
                  studentFeedback: record.studentFeedback,
                  followupPlan: record.followupPlan,
                  followupStatus: record.followupStatus
                }
              }
            }))
            return enriched
          } catch (e) {
            this.$modal.msgError('加载学生信息失败')
            return []
          }
        })

        Promise.all(recordPromises).then(results => {
          this.allData = results.flat()
          if (hasFrontendFilters) {
            this.applyFilters()
          } else {
            this.total = response.total
            this.displayList = this.allData.slice()
          }
          this.loading = false
        }).catch(() => {
          this.allData = []
          this.applyFilters()
          this.loading = false
          this.$modal.msgError('加载记录失败')
        })
      }).catch(() => {
        this.$modal.msgError('加载会话失败')
        this.allData = []
        this.applyFilters()
        this.loading = false
      })
    },

    loadTagsForSessions(sessions) {
      const newTagMap = {}
      const tagPromises = sessions.map(session =>
        getSessionTags(session.sessionId).then(res => {
          const tags = res.data || []
          newTagMap[session.sessionId] = tags.map(t => TAG_LABELS[t.tagValue] || t.tagValue)
        }).catch(() => {
          newTagMap[session.sessionId] = []
          this.$modal.msgError('加载标签失败')
        })
      )
      Promise.all(tagPromises).then(() => {
        this.tagDataMap = newTagMap
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

      if (this.queryParams.talkType) {
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
      this.talkTypeFilter = ''
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
