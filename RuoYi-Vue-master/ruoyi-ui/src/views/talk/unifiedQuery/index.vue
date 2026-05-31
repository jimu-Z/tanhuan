<template>
  <div class="app-container">
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
      <el-form-item label="谈话类型">
        <el-select v-model="queryParams.talkType" placeholder="全部" clearable style="width:120px">
          <el-option label="个别谈话" value="individual" />
          <el-option label="集体谈话" value="group" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容标签">
        <el-select v-model="queryParams.tags" multiple placeholder="全部" clearable collapse-tags style="width:220px">
          <el-option v-for="item in tagOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
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

    <el-tabs v-model="talkTypeFilter" @tab-click="handleTabClick" style="margin-bottom:8px">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="个别谈话" name="individual" />
      <el-tab-pane label="集体谈话" name="group" />
    </el-tabs>

    <el-table v-loading="loading" :data="displayList" @selection-change="handleSelectionChange" row-key="rowKey">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="index" label="序号" width="55" align="center" />
      <el-table-column label="会话ID" align="center" prop="sessionId" width="80" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="学号" align="center" prop="studentCode" width="120" />
      <el-table-column label="谈话类型" align="center" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.talkType === 'individual'" type="primary" size="small">个别谈话</el-tag>
          <el-tag v-else-if="scope.row.talkType === 'group'" type="success" size="small">集体谈话</el-tag>
          <span v-else>{{ scope.row.talkType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="谈话时间" align="center" width="110">
        <template slot-scope="scope">
          <span>{{ scope.row.talkTime ? scope.row.talkTime.substring(0, 10) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="谈话内容" align="center" prop="talkContent" min-width="220" show-overflow-tooltip />
      <el-table-column label="内容标签" align="center" width="180">
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
      <el-table-column label="操作" align="center" width="80" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExportRow(scope.row)"
          >导出</el-button>
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
import { getTalk } from '@/api/talk/talkStudent'
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

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dateRange: null,
        keyword: '',
        talkType: '',
        tags: []
      },

      tagOptions: Object.keys(TAG_LABELS).map(k => ({ value: k, label: TAG_LABELS[k] }))
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
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

        const recordPromises = sessions.map(session =>
          listTalkrecord({ sessionId: session.sessionId, pageSize: 999 }).then(recRes => {
            const records = recRes.rows || []
            return Promise.all(records.map(record =>
              getTalk(record.studentId).then(stuRes => ({
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
              })).catch(() => ({
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
              }))
            ))
          }).catch(() => [])
        )

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
        })
      }).catch(() => {
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
        tags: []
      }
      this.talkTypeFilter = ''
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