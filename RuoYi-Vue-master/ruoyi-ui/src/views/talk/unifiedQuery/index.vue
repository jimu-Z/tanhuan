<template>
  <div class="query-page">
    <section class="query-hero">
      <div>
        <span class="eyebrow">DATA SEARCH</span>
        <h1>统一查询</h1>
        <p>集中检索谈话记录与未谈学生，查询结果自动遵循当前账号的数据范围。</p>
      </div>
      <div class="mode-switch">
        <button :class="{ active: queryMode === 'records' }" @click="switchMode('records')"><i class="el-icon-document"></i><span>谈话记录</span></button>
        <button :class="{ active: queryMode === 'untalked' }" @click="switchMode('untalked')"><i class="el-icon-user"></i><span>未谈学生</span></button>
      </div>
    </section>

    <section class="panel filter-panel">
      <div class="panel-heading">
        <div>
          <h2>{{ queryMode === 'records' ? '记录筛选' : '学生筛选' }}</h2>
          <span>{{ queryMode === 'records' ? '可按学生、时间、类型、标签和谈话人组合查询' : '查找指定时间段内尚未开展谈话的学生' }}</span>
        </div>
        <el-button type="text" icon="el-icon-refresh" @click="resetQuery">重置条件</el-button>
      </div>

      <el-form ref="queryForm" :model="queryParams" label-position="top" class="query-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :lg="6">
            <el-form-item label="学号 / 姓名">
              <el-input v-model.trim="queryParams.keyword" clearable placeholder="输入学号或学生姓名" @keyup.enter.native="handleQuery">
                <i slot="prefix" class="el-input__icon el-icon-search"></i>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="7">
            <el-form-item label="时间范围">
              <el-date-picker v-model="dateRange" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <template v-if="queryMode === 'records'">
            <el-col :xs="24" :sm="12" :lg="4">
              <el-form-item label="谈话类型">
                <el-select v-model="queryParams.talkType" clearable placeholder="全部类型" style="width: 100%">
                  <el-option label="个别谈话" value="individual" />
                  <el-option label="集体谈话" value="group" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="7">
              <el-form-item label="内容标签">
                <el-select v-model="queryParams.tags" multiple collapse-tags clearable placeholder="全部标签" style="width: 100%">
                  <el-option v-for="tag in tagOptions" :key="tag.value" :label="tag.label" :value="tag.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="谈话人">
                <el-input v-model.trim="queryParams.talkPerson" clearable placeholder="输入教师姓名" @keyup.enter.native="handleQuery" />
              </el-form-item>
            </el-col>
          </template>
          <el-col v-else :xs="24" :sm="12" :lg="7">
            <el-form-item label="学院 / 班级">
              <el-cascader v-model="selectedDeptPath" :options="deptTree" :props="deptProps" clearable placeholder="全部班级" style="width: 100%" @change="handleDeptChange" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="4" class="search-action-col">
            <el-button type="primary" icon="el-icon-search" class="search-button" @click="handleQuery">开始查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </section>

    <section class="panel result-panel">
      <div class="panel-heading result-heading">
        <div>
          <h2>{{ queryMode === 'records' ? '谈话记录' : '未谈学生' }}</h2>
          <span>共找到 <strong>{{ total }}</strong> 条结果</span>
        </div>
        <el-button v-if="queryMode === 'records'" type="primary" plain icon="el-icon-download" :disabled="selectedRows.length === 0" v-hasPermi="['talk:session:export']" @click="handleBatchExport">批量导出</el-button>
      </div>

      <el-table :key="queryMode" v-loading="loading" :data="rows" :row-key="rowKey" class="result-table" @selection-change="selectedRows = $event">
        <el-table-column v-if="queryMode === 'records'" type="selection" width="48" align="center" />
        <el-table-column type="index" label="序号" width="64" align="center" :index="tableIndex" />
        <template v-if="queryMode === 'records'">
          <el-table-column label="学生" min-width="150">
            <template slot-scope="scope">
              <div class="student-cell"><span class="student-avatar">{{ (scope.row.studentName || '?').slice(0, 1) }}</span><div><strong>{{ scope.row.studentName || '-' }}</strong><small>{{ scope.row.studentCode || '-' }}</small></div></div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="96" align="center">
            <template slot-scope="scope"><el-tag :type="scope.row.talkType === 'group' ? 'success' : 'primary'" size="small" effect="plain">{{ $talkLabel('talkType', scope.row.talkType) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="谈话时间" prop="talkTime" width="164"><template slot-scope="scope">{{ formatDateTime(scope.row.talkTime) }}</template></el-table-column>
          <el-table-column label="谈话人" prop="talkPerson" width="120" show-overflow-tooltip />
          <el-table-column label="谈话内容" prop="talkContent" :formatter="formatTalkContent" min-width="240" show-overflow-tooltip />
          <el-table-column label="标签" min-width="190">
            <template slot-scope="scope"><div v-if="tagsFor(scope.row.sessionId).length" class="tag-list"><el-tag v-for="tag in tagsFor(scope.row.sessionId)" :key="tag" size="mini" type="info">{{ tag }}</el-tag></div><span v-else class="muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="82" fixed="right" align="center">
            <template slot-scope="scope"><el-button type="text" icon="el-icon-download" v-hasPermi="['talk:session:export']" @click="handleExportRow(scope.row)">导出</el-button></template>
          </el-table-column>
        </template>
        <template v-else>
          <el-table-column label="学生" min-width="180">
            <template slot-scope="scope"><div class="student-cell"><span class="student-avatar warm">{{ (scope.row.studentName || '?').slice(0, 1) }}</span><div><strong>{{ scope.row.studentName || '-' }}</strong><small>{{ scope.row.studentCode || '-' }}</small></div></div></template>
          </el-table-column>
          <el-table-column label="班级" prop="deptName" min-width="220" show-overflow-tooltip />
          <el-table-column label="最近谈话时间" width="170"><template slot-scope="scope">{{ formatDateTime(scope.row.lastTalkTime) }}</template></el-table-column>
          <el-table-column label="操作" width="110" align="center">
            <template slot-scope="scope"><el-button type="text" icon="el-icon-chat-line-round" @click="$router.push({ path: '/talk/initiate', query: { studentId: scope.row.studentId } })">发起谈话</el-button></template>
          </el-table-column>
        </template>
        <template slot="empty"><div class="empty-state"><i :class="queryMode === 'records' ? 'el-icon-document' : 'el-icon-user'"></i><p>没有找到符合条件的数据</p><span>调整筛选条件后再试试</span></div></template>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </section>
  </div>
</template>

<script>
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { getDeptTree, listUntalked } from '@/api/talk/talkStudent'
import { getBatchTags, exportDocxForRecord, exportDocxRecordsBatch, TAG_LABELS } from '@/api/talk/talkSession'
import { getActiveTags } from '@/api/talk/talkTag'
import { html2Text } from '@/utils'

export default {
  name: 'UnifiedQuery',
  data() {
    return {
      loading: false,
      queryMode: 'records',
      rows: [],
      total: 0,
      selectedRows: [],
      dateRange: [],
      deptTree: [],
      selectedDeptPath: [],
      selectedDeptId: null,
      deptProps: { value: 'deptId', label: 'deptName', children: 'children', checkStrictly: true, emitPath: true },
      tagOptions: Object.keys(TAG_LABELS).map(value => ({ value, label: TAG_LABELS[value] })),
      tagLabelMap: { ...TAG_LABELS },
      tagDataMap: {},
      requestSeq: 0,
      queryParams: this.defaultQuery()
    }
  },
  computed: {
    rowKey() {
      return this.queryMode === 'records' ? 'recordId' : 'studentId'
    }
  },
  created() {
    this.loadSupportData()
    this.getList()
  },
  methods: {
    formatTalkContent(row) {
      return html2Text(row.talkContent || '')
    },
    defaultQuery() {
      return { pageNum: 1, pageSize: 10, keyword: '', talkType: '', tags: [], talkPerson: '' }
    },
    loadSupportData() {
      getDeptTree().then(res => { this.deptTree = res.data || [] }).catch(() => { this.deptTree = [] })
      getActiveTags().then(res => {
        const tags = res.data || []
        if (!tags.length) return
        this.tagOptions = tags.map(tag => ({ value: tag.tagKey, label: tag.tagName }))
        this.tagLabelMap = tags.reduce((map, tag) => { map[tag.tagKey] = tag.tagName; return map }, { ...TAG_LABELS })
      }).catch(() => {})
    },
    switchMode(mode) {
      if (mode === this.queryMode) return
      this.queryMode = mode
      this.resetQuery()
    },
    handleDeptChange(path) {
      this.selectedDeptId = path && path.length ? path[path.length - 1] : null
    },
    getList() {
      if (this.queryMode === 'untalked') this.loadUntalked()
      else this.loadRecords()
    },
    loadRecords() {
      const requestId = ++this.requestSeq
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        keyword: this.queryParams.keyword || undefined,
        talkType: this.queryParams.talkType || undefined,
        talkPerson: this.queryParams.talkPerson || undefined,
        tagValues: this.queryParams.tags.length ? this.queryParams.tags.join(',') : undefined,
        beginTalkTime: this.dateRange && this.dateRange[0],
        endTalkTime: this.dateRange && this.dateRange[1]
      }
      listTalkrecord(params).then(res => {
        if (requestId !== this.requestSeq || this.queryMode !== 'records') return
        this.rows = res.rows || []
        this.total = Number(res.total) || 0
        this.loadRowTags(this.rows)
      }).catch(() => {
        if (requestId !== this.requestSeq || this.queryMode !== 'records') return
        this.rows = []
        this.total = 0
        this.$modal.msgError('谈话记录查询失败')
      }).finally(() => {
        if (requestId === this.requestSeq) this.loading = false
      })
    },
    loadUntalked() {
      const requestId = ++this.requestSeq
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        keyword: this.queryParams.keyword || undefined,
        deptId: this.selectedDeptId || undefined,
        beginDate: this.dateRange && this.dateRange[0],
        endDate: this.dateRange && this.dateRange[1]
      }
      listUntalked(params).then(res => {
        if (requestId !== this.requestSeq || this.queryMode !== 'untalked') return
        this.rows = res.rows || []
        this.total = Number(res.total) || 0
      }).catch(() => {
        if (requestId !== this.requestSeq || this.queryMode !== 'untalked') return
        this.rows = []
        this.total = 0
        this.$modal.msgError('未谈学生查询失败')
      }).finally(() => {
        if (requestId === this.requestSeq) this.loading = false
      })
    },
    loadRowTags(rows) {
      const ids = [...new Set(rows.map(row => row.sessionId).filter(Boolean))]
      if (!ids.length) { this.tagDataMap = {}; return }
      getBatchTags(ids).then(res => { this.tagDataMap = res.data || {} }).catch(() => { this.tagDataMap = {} })
    },
    tagsFor(sessionId) {
      const entries = this.tagDataMap[sessionId] || this.tagDataMap[String(sessionId)] || []
      const values = []
      entries.forEach(entry => {
        let parsed = entry.tagValue
        try { parsed = JSON.parse(entry.tagValue) } catch (e) { /* 兼容历史普通字符串 */ }
        const items = Array.isArray(parsed) ? parsed : [parsed]
        items.filter(Boolean).forEach(value => values.push(this.$talkTags(value, this.tagLabelMap)))
      })
      return [...new Set(values)]
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.selectedRows = []
      this.getList()
    },
    resetQuery() {
      this.queryParams = this.defaultQuery()
      this.dateRange = []
      this.selectedDeptPath = []
      this.selectedDeptId = null
      this.rows = []
      this.total = 0
      this.selectedRows = []
      this.getList()
    },
    tableIndex(index) {
      return (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
    },
    formatDateTime(value) {
      return value ? String(value).replace('T', ' ').substring(0, 16) : '暂无记录'
    },
    async saveBlob(blob, fileName) {
      // DOCX and ZIP exports both use ZIP headers. Authentication/error JSON is not a document.
      const header = new Uint8Array(await blob.slice(0, 4).arrayBuffer())
      if (header.length < 4 || header[0] !== 0x50 || header[1] !== 0x4b) {
        throw new Error('导出未返回有效文件')
      }
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    },
    handleExportRow(row) {
      exportDocxForRecord(row.recordId).then(blob => this.saveBlob(blob, '谈话记录_' + (row.studentName || row.studentId) + '.docx')).catch(() => this.$modal.msgError('导出失败'))
    },
    handleBatchExport() {
      const recordIds = [...new Set(this.selectedRows.map(row => row.recordId))]
      if (!recordIds.length) return
      exportDocxRecordsBatch(recordIds).then(blob => this.saveBlob(blob, '谈话记录批量导出.zip')).catch(() => this.$modal.msgError('批量导出失败'))
    }
  }
}
</script>

<style lang="scss" scoped>
.query-page { min-height: calc(100vh - 84px); padding: 24px; background: #f3f6f9; color: #17324d; }
.query-hero { position: relative; overflow: hidden; display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 20px; padding: 30px 34px; border-radius: 18px; color: #fff; background: linear-gradient(125deg, #0e5b72 0%, #147c88 52%, #54a790 100%); box-shadow: 0 16px 34px rgba(24, 95, 109, .2); }
.query-hero::after { content: ''; position: absolute; right: -60px; top: -95px; width: 260px; height: 260px; border: 48px solid rgba(255, 255, 255, .08); border-radius: 50%; }
.eyebrow { font-size: 11px; letter-spacing: 3px; opacity: .72; }
.query-hero h1 { margin: 8px 0 7px; font-family: "Microsoft YaHei", sans-serif; font-size: 30px; letter-spacing: 2px; }
.query-hero p { margin: 0; font-size: 14px; opacity: .82; }
.mode-switch { position: relative; z-index: 1; display: flex; padding: 5px; border: 1px solid rgba(255, 255, 255, .25); border-radius: 13px; background: rgba(7, 54, 67, .28); backdrop-filter: blur(8px); }
.mode-switch button { border: 0; padding: 11px 18px; border-radius: 9px; color: rgba(255, 255, 255, .72); background: transparent; cursor: pointer; transition: .2s ease; }
.mode-switch button i { margin-right: 7px; }
.mode-switch button.active { color: #155b68; background: #fff; box-shadow: 0 5px 14px rgba(4, 40, 49, .18); }
.panel { margin-bottom: 18px; padding: 22px 24px; border: 1px solid #e6edf1; border-radius: 16px; background: #fff; box-shadow: 0 8px 24px rgba(28, 62, 78, .055); }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.panel-heading h2 { margin: 0 0 5px; font-size: 17px; color: #17324d; }
.panel-heading span { font-size: 12px; color: #8496a6; }
.panel-heading strong { color: #147c88; font-size: 15px; }
.result-heading { margin-bottom: 12px; }
.query-form ::v-deep .el-form-item { margin-bottom: 0; }
.query-form ::v-deep .el-form-item__label { padding-bottom: 7px; color: #536b7c; font-weight: 500; line-height: 20px; }
.query-form ::v-deep .el-input__inner { border-color: #dce6eb; border-radius: 8px; }
.query-form ::v-deep .el-input__inner:focus { border-color: #168294; }
.search-action-col { padding-top: 27px; }
.search-button { width: 100%; border-color: #147c88; background: #147c88; border-radius: 8px; }
.search-button:hover { border-color: #0e6877; background: #0e6877; }
.result-table::before { display: none; }
.result-table ::v-deep th { color: #506879; background: #f6f9fa; }
.result-table ::v-deep td { padding: 12px 0; }
.student-cell { display: flex; align-items: center; gap: 10px; }
.student-cell strong, .student-cell small { display: block; }
.student-cell strong { margin-bottom: 3px; color: #213e54; font-size: 14px; }
.student-cell small { color: #96a5b1; font-size: 11px; }
.student-avatar { display: inline-flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 10px; color: #147c88; background: #e6f3f3; font-weight: 700; }
.student-avatar.warm { color: #a26a19; background: #fff2dc; }
.tag-list { display: flex; flex-wrap: wrap; gap: 5px; }
.muted { color: #b5c0c8; }
.empty-state { padding: 40px 0; color: #9aaab6; text-align: center; }
.empty-state i { font-size: 34px; color: #cad5dc; }
.empty-state p { margin: 10px 0 4px; color: #60798b; }
.empty-state span { font-size: 12px; }
@media (max-width: 768px) {
  .query-page { padding: 14px; }
  .query-hero { align-items: flex-start; flex-direction: column; padding: 24px 20px; }
  .mode-switch { width: 100%; }
  .mode-switch button { flex: 1; }
  .panel { padding: 18px 15px; }
  .query-form ::v-deep .el-form-item { margin-bottom: 14px; }
  .search-action-col { padding-top: 0; }
}
</style>
