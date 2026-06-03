<template>
  <div class="app-container">
    <el-form :model="q" ref="queryForm" size="small" :inline="true" label-width="80px">
      <el-form-item label="时间范围">
        <el-date-picker v-model="q.dateRange" type="daterange" range-separator="至"
          start-placeholder="开始" end-placeholder="结束" value-format="yyyy-MM-dd" style="width:240px" />
      </el-form-item>
      <el-form-item label="学号/姓名">
        <el-input v-model="q.keyword" placeholder="学号或姓名" clearable style="width:150px" @keyup.enter.native="search" />
      </el-form-item>
      <el-form-item label="谈话类型">
        <el-select v-model="q.talkType" placeholder="全部" clearable style="width:110px">
          <el-option label="个别谈话" value="individual" />
          <el-option label="集体谈话" value="group" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容标签">
        <el-select v-model="q.tags" multiple placeholder="全部" clearable style="width:200px">
          <el-option v-for="t in tagOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="谈话人">
        <el-input v-model="q.talkPerson" placeholder="谈话人" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="跟进状态">
        <el-select v-model="q.followupStatus" placeholder="全部" clearable style="width:110px">
          <el-option label="待跟进" value="pending" />
          <el-option label="跟进中" value="in_progress" />
          <el-option label="已完成" value="completed" />
          <el-option label="无需跟进" value="none" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="search">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" :disabled="selected.length === 0" @click="batchExport">批量导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="result" @selection-change="s => selected = s">
      <el-table-column type="selection" width="45" />
      <el-table-column type="index" label="序号" width="50" align="center" />
      <el-table-column label="类型" width="70" align="center">
        <template slot-scope="s"><el-tag :type="s.row.talkType==='individual'?'primary':'success'" size="mini">{{ s.row.talkType==='individual'?'个别':'集体' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="学生" prop="studentName" min-width="80" align="center" />
      <el-table-column label="学号" prop="studentCode" width="100" align="center" />
      <el-table-column label="谈话时间" width="110" align="center">
        <template slot-scope="s">{{ s.row.talkTime ? s.row.talkTime.substring(0,10) : '' }}</template>
      </el-table-column>
      <el-table-column label="谈话人" prop="talkPerson" width="80" align="center" />
      <el-table-column label="谈话内容" prop="talkContent" min-width="180" show-overflow-tooltip />
      <el-table-column label="学生反馈" prop="studentFeedback" min-width="120" show-overflow-tooltip />
      <el-table-column label="跟进计划" prop="followupPlan" min-width="120" show-overflow-tooltip />
      <el-table-column label="跟进状态" width="80" align="center">
        <template slot-scope="s">
          <el-tag v-if="s.row.followupStatus==='pending'" type="info" size="mini">待跟进</el-tag>
          <el-tag v-else-if="s.row.followupStatus==='in_progress'" type="warning" size="mini">跟进中</el-tag>
          <el-tag v-else-if="s.row.followupStatus==='completed'" type="success" size="mini">已完成</el-tag>
          <el-tag v-else-if="s.row.followupStatus==='none'" size="mini">无需跟进</el-tag>
          <span v-else style="font-size:12px">{{ s.row.followupStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70" align="center">
        <template slot-scope="s">
          <el-button size="mini" type="text" icon="el-icon-download" @click="exportOne(s.row)">导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="q.pageNum" :limit.sync="q.pageSize" @pagination="search" />
  </div>
</template>

<script>
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { getTalksession, TAG_LABELS } from '@/api/talk/talkSession'

export default {
  name: 'AdvancedQuery',
  data() {
    return {
      loading: false, total: 0, result: [], selected: [],
      q: { pageNum: 1, pageSize: 10, dateRange: null, keyword: '', talkType: '', tags: [], talkPerson: '', followupStatus: '' },
      tagOptions: Object.keys(TAG_LABELS).map(k => ({ value: k, label: TAG_LABELS[k] }))
    }
  },
  methods: {
    search() {
      this.loading = true
      const p = { ...this.q }
      const hasFrontendFilters = p.keyword || p.talkType || p.talkPerson ||
        (p.tags && p.tags.length > 0) || (p.dateRange && p.dateRange.length === 2)
      const apiParams = {
        pageNum: hasFrontendFilters ? 1 : p.pageNum,
        pageSize: hasFrontendFilters ? 9999 : p.pageSize,
        followupStatus: p.followupStatus || undefined
      }
      listTalkrecord(apiParams).then(res => {
        const rows = res.rows || []
        if (rows.length === 0) {
          this.result = []
          this.total = 0
          this.loading = false
          return
        }
        Promise.all(rows.map(r =>
          getTalksession(r.sessionId).then(sesRes => {
            const ses = sesRes.data || sesRes
            return {
              ...r,
              talkType: ses.talkType || '',
              talkPerson: ses.talkPerson || '',
              talkContent: ses.talkContent || '',
              talkTime: ses.talkTime || ''
            }
          }).catch(() => ({
            ...r,
            talkType: '',
            talkPerson: '',
            talkContent: '',
            talkTime: ''
          })))
        ).then(enriched => {
          let filtered = enriched
          if (p.keyword) {
            const kw = p.keyword.toLowerCase()
            filtered = filtered.filter(r =>
              (r.studentName || '').toLowerCase().includes(kw) ||
              (r.studentCode || '').toString().includes(kw)
            )
          }
          if (p.talkType) {
            filtered = filtered.filter(r => r.talkType === p.talkType)
          }
          if (p.talkPerson) {
            filtered = filtered.filter(r => (r.talkPerson || '').includes(p.talkPerson))
          }
          if (p.dateRange && p.dateRange.length === 2) {
            const start = p.dateRange[0]
            const end = p.dateRange[1]
            filtered = filtered.filter(r => {
              if (!r.talkTime) return false
              return r.talkTime >= start && r.talkTime <= end
            })
          }
          if (hasFrontendFilters) {
            this.total = filtered.length
            const startIdx = (p.pageNum - 1) * p.pageSize
            this.result = filtered.slice(startIdx, startIdx + p.pageSize)
          } else {
            this.result = filtered
            this.total = res.total
          }
          this.loading = false
        })
      }).catch(() => {
        this.$modal.msgError('操作失败')
        this.result = []
        this.total = 0
        this.loading = false
      })
    },
    reset() {
      this.q = { pageNum: 1, pageSize: 10, dateRange: null, keyword: '', talkType: '', tags: [], talkPerson: '', followupStatus: '' }
      this.search()
    },
    exportOne(row) {
      const sid = row.sessionId
      if (!sid) { this.$modal.msgError('未找到对应会话'); return }
      const fileName = '谈话记录_' + (row.studentName || 'unknown') + '.docx'
      import('@/utils/request').then(({ default: request }) => {
        request({ url: '/ruoyi-system/talksession/exportDocx/' + sid, method: 'get', responseType: 'blob' }).then(blob => {
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement('a'); a.href = url
          a.download = fileName; a.click(); window.URL.revokeObjectURL(url)
        }).catch(() => {
          this.$modal.msgError('导出失败')
        })
      }).catch(() => {
        this.$modal.msgError('导出失败')
      })
    },
    batchExport() {
      this.$modal.msgWarning('批量导出功能开发中，请逐条导出')
    }
  }
}
</script>
