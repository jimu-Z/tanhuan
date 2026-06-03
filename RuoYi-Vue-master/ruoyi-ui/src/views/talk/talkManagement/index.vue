<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="谈话时间" prop="talkTime">
        <el-date-picker clearable
          v-model="queryParams.talkTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择谈话时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="谈话地点" prop="talkLocation">
        <el-input
          v-model="queryParams.talkLocation"
          placeholder="请输入谈话地点"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="谈话人" prop="talkPerson">
        <el-input
          v-model="queryParams.talkPerson"
          placeholder="请输入谈话人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAddSession"
          v-hasPermi="['talk:session:add']"
        >新增会话</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdateSession"
          v-hasPermi="['talk:session:edit']"
        >修改会话</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDeleteSession"
          v-hasPermi="['talk:session:remove']"
        >删除会话</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExportXls"
          v-hasPermi="['talk:session:export']"
        >导出Excel</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-download" size="mini" :disabled="multiple2"
          @click="handleBatchExport" v-hasPermi="['talk:session:export']">批量导出DOCX</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-tabs v-model="talkTypeFilter" @tab-click="handleQuery" style="margin-bottom:8px">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="个别谈话" name="individual" />
      <el-tab-pane label="集体谈话" name="group" />
    </el-tabs>

    <el-table v-loading="loading" :data="sessionList" @selection-change="handleSelectionChange"
      row-key="sessionId" @expand-change="handleExpand" ref="sessionTable">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column type="expand">
        <template slot-scope="scope">
          <div v-loading="scope.row._loadingRecords" style="padding:8px 20px">
            <div style="margin-bottom:6px;display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold;font-size:13px">谈话记录明细</span>
              <el-button type="primary" size="mini" icon="el-icon-plus"
                v-hasPermi="['talk:record:add']"
                @click="handleAddRecord(scope.row)">新增记录</el-button>
            </div>
            <el-table :data="scope.row._records || []" size="mini" border>
              <el-table-column label="学生姓名" prop="studentName" width="100" />
              <el-table-column label="学号" prop="studentCode" width="120" />
              <el-table-column label="学生反馈" prop="studentFeedback" min-width="140" show-overflow-tooltip />
              <el-table-column label="跟进计划" prop="followupPlan" min-width="140" show-overflow-tooltip />
              <el-table-column label="跟进状态" width="90" align="center">
                <template slot-scope="s2">
                  <el-tag v-if="s2.row.followupStatus==='pending'" type="info" size="mini">待跟进</el-tag>
                  <el-tag v-else-if="s2.row.followupStatus==='in_progress'" type="warning" size="mini">跟进中</el-tag>
                  <el-tag v-else-if="s2.row.followupStatus==='completed'" type="success" size="mini">已完成</el-tag>
                  <el-tag v-else-if="s2.row.followupStatus==='none'" type="info" size="mini">无需跟进</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center">
                <template slot-scope="s2">
                  <el-button size="mini" type="text" icon="el-icon-download"
                    @click="handleExportStudentRecord(s2.row)">导出</el-button>
                  <el-button size="mini" type="text" icon="el-icon-edit"
                    v-hasPermi="['talk:record:edit']"
                    @click="handleUpdateRecord(s2.row)">修改</el-button>
                  <el-button size="mini" type="text" icon="el-icon-delete" style="color:#f56c6c"
                    v-hasPermi="['talk:record:remove']"
                    @click="handleDeleteRecord(s2.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="scope.row._records===null" style="text-align:center;color:#c0c4cc;padding:12px">暂无记录，请点击"新增记录"添加</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="谈话类型" align="center" prop="talkType" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.talkType === 'individual'" type="primary">个别谈话</el-tag>
          <el-tag v-else-if="scope.row.talkType === 'group'" type="success">集体谈话</el-tag>
          <span v-else>{{ scope.row.talkType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="谈话时间" align="center" prop="talkTime" width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.talkTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="参与学生" align="center" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row._records">
            {{ scope.row._records.length > 0 ? scope.row._records.length + '人' : '0人' }}
          </span>
          <span v-else-if="recordCounts[scope.row.sessionId] !== undefined">
            {{ recordCounts[scope.row.sessionId] > 0 ? recordCounts[scope.row.sessionId] + '人' : '0人' }}
          </span>
          <span v-else style="color:#c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="谈话地点" align="center" prop="talkLocation" width="120" />
      <el-table-column label="谈话人" align="center" prop="talkPerson" width="100" />
      <el-table-column label="谈话内容" align="center" prop="talkContent" min-width="200" show-overflow-tooltip />
      <el-table-column label="内容标签" align="center" width="110">
        <template slot-scope="scope">
          <span v-if="tagMap[scope.row.sessionId]" style="font-size:12px;color:#666">
            {{ tagMap[scope.row.sessionId].map(t => getTagLabel(t.tagValue)).join('、') || '-' }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit"
            @click="handleUpdateSession(scope.row)"
            v-hasPermi="['talk:session:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#f56c6c"
            @click="handleDeleteSession(scope.row)"
            v-hasPermi="['talk:session:remove']">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-download"
            @click="handleExportDocx(scope.row)">导出</el-button>
          <el-button v-if="scope.row.talkType==='group'" size="mini" type="text" icon="el-icon-s-data"
            @click="handleExportGroupSummary(scope.row)">汇总</el-button>
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

    <!-- 会话增改对话框 -->
    <el-dialog :title="sessionDialogTitle" :visible.sync="sessionDialogOpen" width="500px" append-to-body>
      <el-form ref="sessionForm" :model="sessionForm" :rules="sessionRules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="谈话类型" prop="talkType">
              <el-select v-model="sessionForm.talkType" placeholder="请选择谈话类型" style="width:100%">
                <el-option label="个别谈话" value="individual"></el-option>
                <el-option label="集体谈话" value="group"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话时间" prop="talkTime">
              <el-date-picker clearable v-model="sessionForm.talkTime" type="date"
                value-format="yyyy-MM-dd" placeholder="请选择谈话时间" style="width:100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话地点" prop="talkLocation">
              <el-input v-model="sessionForm.talkLocation" placeholder="请输入谈话地点" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话人" prop="talkPerson">
              <el-input v-model="sessionForm.talkPerson" placeholder="请输入谈话人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话内容">
              <editor v-model="sessionForm.talkContent" :min-height="192" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSessionForm">确 定</el-button>
        <el-button @click="sessionDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 记录增改对话框 -->
    <el-dialog :title="recordDialogTitle" :visible.sync="recordDialogOpen" width="500px" append-to-body>
      <el-form ref="recordForm" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学生" prop="studentId">
              <el-input v-model="recordForm.studentId" placeholder="请输入学生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="学生反馈" prop="studentFeedback">
              <el-input v-model="recordForm.studentFeedback" type="textarea" :rows="3" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="跟进计划" prop="followupPlan">
              <el-input v-model="recordForm.followupPlan" type="textarea" :rows="3" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="跟进状态" prop="followupStatus">
              <el-select v-model="recordForm.followupStatus" placeholder="请选择" style="width:100%">
                <el-option label="待跟进" value="pending" />
                <el-option label="跟进中" value="in_progress" />
                <el-option label="已完成" value="completed" />
                <el-option label="无需跟进" value="none" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRecordForm">确 定</el-button>
        <el-button @click="recordDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTalksession, getTalksession, delTalksession, addTalksession, updateTalksession, getBatchTags, exportGroupSummary, exportDocx, exportDocxForStudent, exportDocxBatch, TAG_LABELS } from "@/api/talk/talkSession"
import { listTalkrecord, getTalkrecord, delTalkrecord, addTalkrecord, updateTalkrecord } from "@/api/talk/talkStudentRecord"
import { getTalk } from "@/api/talk/talkStudent"
import { getLabels } from "@/api/talk/talkTag"

export default {
  name: "TalkManagement",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      multiple2: true,
      showSearch: true,
      talkTypeFilter: '',
      total: 0,
      sessionList: [],
      tagMap: {},
      recordCounts: {},
      tagLabels: TAG_LABELS,
      sessionDialogTitle: "",
      sessionDialogOpen: false,
      recordDialogTitle: "",
      recordDialogOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        talkType: null,
        talkTime: null,
        talkLocation: null,
        talkPerson: null,
        talkContent: null,
      },
      sessionForm: {},
      sessionRules: {
        talkType: [{ required: true, message: "谈话类型不能为空", trigger: "change" }],
        talkTime: [{ required: true, message: "谈话时间不能为空", trigger: "blur" }],
        talkPerson: [{ required: true, message: "谈话人不能为空", trigger: "blur" }],
      },
      recordForm: {},
      recordRules: {
        studentId: [{ required: true, message: "学生ID不能为空", trigger: "blur" }],
        followupPlan: [{ required: true, message: "跟进计划不能为空", trigger: "blur" }],
        followupStatus: [{ required: true, message: "跟进状态不能为空", trigger: "change" }],
      },
      // Cache student lookups to avoid N+1 requests
      studentCache: {},
      currentSessionIdForRecord: null
    }
  },
  created() {
    this.loadTagLabels()
    this.getList()
  },
  methods: {
    loadTagLabels() {
      getLabels().then(res => {
        if (res.data && Object.keys(res.data).length > 0) {
          this.tagLabels = res.data
        }
      }).catch(() => { console.warn('标签映射加载失败，使用硬编码兜底') })
    },
    getList() {
      this.loading = true
      this.queryParams.talkType = this.talkTypeFilter || null
      listTalksession(this.queryParams).then(response => {
        this.sessionList = response.rows
        this.total = response.total
        this.loading = false
        this.loadTagsBatch()
        this.loadRecordCounts()
      }).catch(() => { this.loading = false; this.$modal.msgError('加载列表失败') })
    },
    loadTagsBatch() {
      const ids = this.sessionList.map(s => s.sessionId)
      if (ids.length === 0) return
      getBatchTags(ids).then(res => {
        const data = res.data || {}
        Object.keys(data).forEach(k => {
          this.$set(this.tagMap, Number(k), data[k] || [])
        })
      }).catch(() => { console.warn('批量加载标签失败') })
    },
    loadRecordCounts() {
      const ids = this.sessionList.map(s => s.sessionId)
      if (ids.length === 0) return
      Promise.all(ids.map(id =>
        listTalkrecord({ sessionId: id, pageSize: 1 }).then(res => {
          this.$set(this.recordCounts, id, res.total || 0)
        }).catch(() => {
          this.$set(this.recordCounts, id, 0)
        })
      ))
    },
    getTagLabel(value) {
      return this.tagLabels[value] || value
    },
    handleExpand(row, expanded) {
      if (!expanded) return
      if (row._records !== undefined) return
      this.$set(row, '_loadingRecords', true)
      listTalkrecord({ sessionId: row.sessionId, pageSize: 999 }).then(res => {
        const records = res.rows || []
        if (records.length === 0) {
          this.$set(row, '_records', null)
          this.$set(row, '_loadingRecords', false)
          return
        }
        // Collect unique student IDs to batch fetch
        const uniqueStudentIds = [...new Set(records.map(r => r.studentId))]
        // Filter out cached students
        const uncachedIds = uniqueStudentIds.filter(id => !this.studentCache[id])

        if (uncachedIds.length === 0) {
          // All students already cached
          const enriched = records.map(r => ({
            ...r,
            studentName: this.studentCache[r.studentId].studentName || '-',
            studentCode: this.studentCache[r.studentId].studentCode || '-',
          }))
          this.$set(row, '_records', enriched)
          this.$set(row, '_loadingRecords', false)
          return
        }

        // Batch fetch uncached students
        Promise.all(uncachedIds.map(id =>
          getTalk(id).then(stu => {
            if (stu.data) {
              this.$set(this.studentCache, id, { studentName: stu.data.studentName, studentCode: stu.data.studentCode })
            }
          }).catch(() => {
            this.$set(this.studentCache, id, { studentName: '-', studentCode: '-' })
          })
        )).then(() => {
          const enriched = records.map(r => {
            const cached = this.studentCache[r.studentId] || {}
            return {
              ...r,
              studentName: cached.studentName || '-',
              studentCode: cached.studentCode || '-',
            }
          })
          this.$set(row, '_records', enriched)
          this.$set(row, '_loadingRecords', false)
        }).catch(() => {
          this.$set(row, '_records', [])
          this.$set(row, '_loadingRecords', false)
          this.$modal.msgError('加载学生信息失败')
        })
      }).catch(() => {
        this.$set(row, '_records', [])
        this.$set(row, '_loadingRecords', false)
        this.$modal.msgError('加载记录失败')
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.sessionId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
      this.multiple2 = selection.length < 1
    },
    // ========== 会话 CRUD ==========
    resetSessionForm() {
      this.sessionForm = {
        sessionId: null,
        talkType: null,
        talkTime: null,
        talkLocation: null,
        talkPerson: null,
        talkContent: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      }
      if (this.$refs.sessionForm) this.resetForm("sessionForm")
    },
    handleAddSession() {
      this.resetSessionForm()
      this.sessionDialogOpen = true
      this.sessionDialogTitle = "添加谈话会话"
    },
    handleUpdateSession(row) {
      const sessionId = row.sessionId || (Array.isArray(this.ids) ? this.ids[0] : this.ids)
      if (!sessionId) {
        this.$modal.msgWarning('请选择一条记录进行编辑')
        return
      }
      this.resetSessionForm()
      getTalksession(sessionId).then(response => {
        this.sessionForm = response.data || response
        this.sessionDialogOpen = true
        this.sessionDialogTitle = "修改谈话会话"
      }).catch(() => { this.$modal.msgError('获取会话详情失败') })
    },
    submitSessionForm() {
      this.$refs.sessionForm.validate(valid => {
        if (valid) {
          if (this.sessionForm.sessionId != null) {
            updateTalksession(this.sessionForm).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.sessionDialogOpen = false
              this.getList()
            }).catch(() => { this.$modal.msgError('修改失败') })
          } else {
            addTalksession(this.sessionForm).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.sessionDialogOpen = false
              this.getList()
            }).catch(() => { this.$modal.msgError('新增失败') })
          }
        }
      })
    },
    handleDeleteSession(row) {
      const sessionIds = row.sessionId || this.ids
      this.$modal.confirm('是否确认删除选中的会话数据项？').then(() => {
        return delTalksession(sessionIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { this.$modal.msgError('删除失败') })
    },
    // ========== 记录 CRUD ==========
    resetRecordForm() {
      this.recordForm = {
        recordId: null,
        sessionId: this.currentSessionIdForRecord,
        studentId: null,
        studentFeedback: null,
        followupPlan: null,
        followupStatus: null,
        createTime: null
      }
      if (this.$refs.recordForm) this.resetForm("recordForm")
    },
    handleAddRecord(sessionRow) {
      this.currentSessionIdForRecord = sessionRow.sessionId
      this.resetRecordForm()
      this.recordDialogOpen = true
      this.recordDialogTitle = "添加谈话记录"
    },
    handleUpdateRecord(row) {
      this.resetRecordForm()
      getTalkrecord(row.recordId).then(response => {
        this.recordForm = response.data || response
        this.currentSessionIdForRecord = this.recordForm.sessionId
        this.recordDialogOpen = true
        this.recordDialogTitle = "修改谈话记录"
      }).catch(() => { this.$modal.msgError('获取记录详情失败') })
    },
    submitRecordForm() {
      this.$refs.recordForm.validate(valid => {
        if (valid) {
          this.recordForm.sessionId = this.currentSessionIdForRecord
          if (this.recordForm.recordId != null) {
            updateTalkrecord(this.recordForm).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.recordDialogOpen = false
              this.refreshExpandedRecords()
            }).catch(() => { this.$modal.msgError('修改失败') })
          } else {
            addTalkrecord(this.recordForm).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.recordDialogOpen = false
              this.refreshExpandedRecords()
            }).catch(() => { this.$modal.msgError('新增失败') })
          }
        }
      })
    },
    handleDeleteRecord(row) {
      this.$modal.confirm('是否确认删除该记录？').then(() => {
        return delTalkrecord(row.recordId)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        this.refreshExpandedRecords()
      }).catch(() => { this.$modal.msgError('删除失败') })
    },
    refreshExpandedRecords() {
      const sessionId = this.currentSessionIdForRecord
      this.sessionList.forEach(s => {
        if (s.sessionId === sessionId) {
          this.$delete(s, '_records')
          this.handleExpand(s, true)
        }
      })
    },
    // ========== 导出 ==========
    handleExportXls() {
      this.download('ruoyi-system/talksession/export', {
        ...this.queryParams
      }, `talksession_${new Date().getTime()}.xlsx`)
    },
    handleBatchExport() {
      if (this.ids.length === 0) { this.$modal.msgWarning('请至少选择一条记录'); return }
      this.$modal.confirm('确认导出选中的 ' + this.ids.length + ' 条会话记录？').then(() => {
        return exportDocxBatch(this.ids)
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url; a.download = '谈话记录批量导出.zip'; a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => { this.$modal.msgError('导出失败') })
    },
    handleExportDocx(row) {
      const isGroup = row.talkType === 'group'
      const ext = isGroup ? '.zip' : '.docx'
      this.$modal.confirm('确认导出' + (row.talkPerson || '') + '的谈话记录吗？' + (isGroup ? '（集体谈话将打包为zip）' : '')).then(() => {
        return exportDocx(row.sessionId)
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url
        a.download = '谈话记录_' + (row.talkPerson || row.sessionId) + ext
        a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => { this.$modal.msgError('导出失败') })
    },
    handleExportStudentRecord(row) {
      const name = row.studentName || row.studentCode || row.studentId
      this.$modal.confirm('确认导出' + name + '的个人谈话表吗？').then(() => {
        return exportDocxForStudent(row.sessionId, row.studentId)
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url
        a.download = '个人谈话表_' + name + '.docx'
        a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => { this.$modal.msgError('导出失败') })
    },
    handleExportGroupSummary(row) {
      if (row.talkType !== 'group') { this.$modal.msgWarning('仅支持集体谈话的汇总导出'); return }
      this.$modal.confirm('确认导出' + (row.talkPerson || '') + '的集体谈话汇总表吗？').then(() => {
        return exportGroupSummary(row.sessionId)
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url
        a.download = '集体谈话汇总表_' + (row.talkPerson || row.sessionId) + '.docx'
        a.click()
        window.URL.revokeObjectURL(url)
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

.el-form--inline {
  display: flex;
  flex-wrap: wrap;

  ::v-deep .el-form-item {
    margin-bottom: 16px;
    flex: 1;
    min-width: 220px;
  }

  ::v-deep .el-input__inner {
    width: 100%;
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

  ::v-deep .el-date-editor {
    .el-input__inner {
      border-radius: 6px;
      border: 1px solid #d4e0eb;
    }
  }
}

.el-form--inline > .el-form-item:last-child {
  flex: none;
}

::v-deep .search-btn-item {
  align-self: flex-end;
  flex: none;
  width: auto;
  
  .el-form-item__content {
    margin-left: 12px !important;
    display: flex;
    align-items: center;
    margin-top: 28px;
  }
  
  .el-form-item__label {
    display: none;
    width: 0 !important;
  }
}

.el-form--inline {
  ::v-deep .el-button--primary {
    background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
    border: none;
    box-shadow: 0 2px 8px rgba(26, 82, 118, 0.25);
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #1e5f8a 0%, #3080ba 100%);
      box-shadow: 0 4px 12px rgba(26, 82, 118, 0.35);
      transform: translateY(-1px);
    }
  }

  ::v-deep .el-button--default {
    border-color: #d4e0eb;
    color: #606266;
    background: #ffffff;

    &:hover {
      border-color: #1a5276;
      color: #1a5276;
      background: #f0f6fc;
    }
  }
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

  ::v-deep .el-button--success[plain] {
    border-color: #2a6fa8;
    color: #2a6fa8;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #2a6fa8 0%, #3a85c0 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(42, 111, 168, 0.3);
    }
  }

  ::v-deep .el-button--danger[plain] {
    border-color: #e64340;
    color: #e64340;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #e64340 0%, #f06060 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(230, 67, 64, 0.3);
    }
  }

  ::v-deep .el-button--warning[plain] {
    border-color: #f5a623;
    color: #f5a623;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #f5a623 0%, #f7c948 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(245, 166, 35, 0.3);
    }
  }

  ::v-deep .el-button--info[plain] {
    border-color: #37474f;
    color: #37474f;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #37474f 0%, #455a64 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(55, 71, 79, 0.3);
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

  .el-form--inline {
    ::v-deep .el-form-item {
      width: 100%;
      margin-bottom: 12px;
    }
  }
}
</style>