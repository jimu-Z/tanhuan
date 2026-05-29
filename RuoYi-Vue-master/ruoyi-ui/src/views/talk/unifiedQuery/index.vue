<!--
  ================================================================================
  THROWAWAY PROTOTYPE — unifiedQuery/index.vue
  学生谈心谈话管理系统 · 统一查询管理页面
  目的：将高级查询、谈话会话管理、谈话记录管理三个页面合并为一个统一界面
  所有数据均为模拟数据，无API调用
  ================================================================================
-->
<template>
  <div class="unified-root">
    <!-- Search Bar -->
    <div class="search-card">
      <el-form :model="query" ref="queryForm" size="small" :inline="true" class="search-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="query.dateRange"
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
            v-model="query.keyword"
            placeholder="请输入学号或姓名"
            clearable
            style="width:160px"
            @keyup.enter.native="doSearch"
          />
        </el-form-item>
        <el-form-item label="谈话类型">
          <el-select v-model="query.talkType" placeholder="全部" clearable style="width:120px">
            <el-option label="个别谈话" value="individual" />
            <el-option label="集体谈话" value="group" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容标签">
          <el-select v-model="query.tags" multiple placeholder="全部" clearable collapse-tags style="width:200px">
            <el-option v-for="t in tagOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="谈话人">
          <el-input
            v-model="query.talkPerson"
            placeholder="请输入谈话人"
            clearable
            style="width:140px"
          />
        </el-form-item>
        <el-form-item label="跟进状态">
          <el-select v-model="query.followupStatus" placeholder="全部" clearable style="width:120px">
            <el-option label="待跟进" value="pending" />
            <el-option label="跟进中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="无需跟进" value="none" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="doSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Tabs + Action Bar -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <div class="custom-tabs">
          <span
            v-for="tab in tabs"
            :key="tab.value"
            :class="['custom-tab', { active: activeTab === tab.value }]"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.count !== null" class="tab-count">{{ tab.count }}</span>
          </span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openCreateDialog">发起谈话</el-button>
        <el-button type="success" size="small" plain icon="el-icon-download" :disabled="selected.length === 0" @click="batchExport">批量导出</el-button>
      </div>
    </div>

    <!-- Unified Table -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="filteredData"
        @selection-change="s => selected = s"
        row-key="id"
        @expand-change="handleExpand"
        size="small"
        class="unified-table"
        :header-cell-style="{ background: '#fafbfc', color: '#303133', fontWeight: 600, fontSize: '13px' }"
      >
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column type="index" label="序号" width="55" align="center" />

        <el-table-column type="expand" v-if="activeTab === 'group' || activeTab === 'all'">
          <template slot-scope="scope">
            <div v-if="scope.row.talkType === 'group'" v-loading="scope.row._expanding" style="padding: 12px 24px">
              <el-table :data="scope.row.students || []" size="mini" class="expand-table">
                <el-table-column label="学生姓名" prop="studentName" width="100" />
                <el-table-column label="学号" prop="studentCode" width="120" />
                <el-table-column label="学生反馈" prop="studentFeedback" min-width="160" show-overflow-tooltip />
                <el-table-column label="跟进计划" prop="followupPlan" min-width="160" show-overflow-tooltip />
                <el-table-column label="跟进状态" width="90" align="center">
                  <template slot-scope="s2">
                    <el-tag v-if="s2.row.followupStatus === 'pending'" type="info" size="mini">待跟进</el-tag>
                    <el-tag v-else-if="s2.row.followupStatus === 'in_progress'" type="warning" size="mini">跟进中</el-tag>
                    <el-tag v-else-if="s2.row.followupStatus === 'completed'" type="success" size="mini">已完成</el-tag>
                    <el-tag v-else-if="s2.row.followupStatus === 'none'" size="mini">无需跟进</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else style="padding: 12px 24px; color: #909399; font-size: 13px">
              个别谈话无展开数据，可直接在行内查看
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.talkType === 'individual'" type="primary" size="small" effect="dark">个别</el-tag>
            <el-tag v-else type="success" size="small" effect="dark">集体</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="学生姓名" prop="studentName" min-width="90" align="center" />

        <el-table-column label="学号" prop="studentCode" width="110" align="center" />

        <el-table-column label="谈话时间" width="120" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.talkTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="谈话人" prop="talkPerson" width="90" align="center" />

        <el-table-column label="谈话地点" prop="talkLocation" width="110" align="center" />

        <el-table-column label="谈话内容" prop="talkContent" min-width="180" show-overflow-tooltip />

        <el-table-column label="内容标签" width="140" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.tags && scope.row.tags.length > 0">
              <el-tag
                v-for="(tag, ti) in scope.row.tags.slice(0, 2)"
                :key="ti"
                size="mini"
                type="info"
                style="margin:1px"
              >{{ tag }}</el-tag>
              <el-tag v-if="scope.row.tags.length > 2" size="mini" type="info" style="margin:1px">+{{ scope.row.tags.length - 2 }}</el-tag>
            </span>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>

        <el-table-column label="学生反馈" prop="studentFeedback" min-width="150" show-overflow-tooltip />

        <el-table-column label="跟进计划" prop="followupPlan" min-width="150" show-overflow-tooltip />

        <el-table-column label="跟进状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.followupStatus === 'pending'" type="info" size="small">待跟进</el-tag>
            <el-tag v-else-if="scope.row.followupStatus === 'in_progress'" type="warning" size="small">跟进中</el-tag>
            <el-tag v-else-if="scope.row.followupStatus === 'completed'" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="scope.row.followupStatus === 'none'" size="small">无需跟进</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-s-management" @click="openFollowupDialog(scope.row)">跟进</el-button>
            <el-button size="mini" type="text" icon="el-icon-download" @click="exportOne(scope.row)">导出</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <span class="table-summary">共 {{ filteredData.length }} 条记录</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="filteredData.length"
          :page-size="10"
          :current-page.sync="currentPage"
          small
        />
      </div>
    </div>

    <!-- Dialog: Edit Session -->
    <el-dialog
      :title="sessionDialogTitle"
      :visible.sync="sessionDialogVisible"
      width="560px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="sessionForm" :model="sessionForm" :rules="sessionRules" label-width="90px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="谈话时间" prop="talkTime">
              <el-date-picker
                v-model="sessionForm.talkTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="选择日期"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话类型" prop="talkType">
              <el-select v-model="sessionForm.talkType" placeholder="选择类型" style="width:100%">
                <el-option label="个别谈话" value="individual" />
                <el-option label="集体谈话" value="group" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="谈话人" prop="talkPerson">
              <el-input v-model="sessionForm.talkPerson" placeholder="请输入谈话人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话地点" prop="talkLocation">
              <el-input v-model="sessionForm.talkLocation" placeholder="请输入谈话地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="谈话内容" prop="talkContent">
          <el-input
            v-model="sessionForm.talkContent"
            type="textarea"
            :rows="5"
            placeholder="请输入谈话内容"
          />
        </el-form-item>
        <el-form-item label="内容标签">
          <el-select v-model="sessionForm.tags" multiple placeholder="选择标签" style="width:100%">
            <el-option v-for="t in tagOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sessionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSession">确定</el-button>
      </div>
    </el-dialog>

    <!-- Dialog: Update Followup -->
    <el-dialog
      :title="followupDialogTitle"
      :visible.sync="followupDialogVisible"
      width="480px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="followupForm" :model="followupForm" :rules="followupRules" label-width="90px" size="small">
        <el-form-item label="学生反馈" prop="studentFeedback">
          <el-input
            v-model="followupForm.studentFeedback"
            type="textarea"
            :rows="3"
            placeholder="请输入学生反馈"
          />
        </el-form-item>
        <el-form-item label="跟进计划" prop="followupPlan">
          <el-input
            v-model="followupForm.followupPlan"
            type="textarea"
            :rows="3"
            placeholder="请输入跟进计划"
          />
        </el-form-item>
        <el-form-item label="跟进状态" prop="followupStatus">
          <el-select v-model="followupForm.followupStatus" placeholder="选择状态" style="width:100%">
            <el-option label="待跟进" value="pending" />
            <el-option label="跟进中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="无需跟进" value="none" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="followupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFollowup">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTalksession, getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'
import { listTalkrecord, updateTalkrecord } from '@/api/talk/talkStudentRecord'
import { listTalk } from '@/api/talk/talkStudent'
import request from '@/utils/request'

export default {
  name: 'UnifiedQuery',
  data() {
    return {
      loading: false,
      currentPage: 1,
      activeTab: 'all',
      selected: [],

      query: {
        dateRange: null,
        keyword: '',
        talkType: '',
        tags: [],
        talkPerson: '',
        followupStatus: ''
      },

      tabs: [
        { label: '全部', value: 'all', count: 0 },
        { label: '个别谈话', value: 'individual', count: 0 },
        { label: '集体谈话', value: 'group', count: 0 }
      ],

      tagOptions: [
        { value: '学风建设', label: '学风建设' },
        { value: '思想教育', label: '思想教育' },
        { value: '心理健康', label: '心理健康' },
        { value: '日常事务', label: '日常事务' },
        { value: '职业规划', label: '职业规划' },
        { value: '党团建设', label: '党团建设' },
        { value: '危机应对', label: '危机应对' },
        { value: '就业指导', label: '就业指导' }
      ],

      simulatedData: [],

      sessionDialogVisible: false,
      sessionDialogTitle: '编辑谈话会话',
      sessionForm: {
        id: '',
        talkTime: '',
        talkType: 'individual',
        talkPerson: '',
        talkLocation: '',
        talkContent: '',
        tags: []
      },
      sessionRules: {
        talkTime: [{ required: true, message: '请选择谈话时间', trigger: 'change' }],
        talkType: [{ required: true, message: '请选择谈话类型', trigger: 'change' }],
        talkPerson: [{ required: true, message: '请输入谈话人', trigger: 'blur' }],
        talkContent: [{ required: true, message: '请输入谈话内容', trigger: 'blur' }]
      },

      followupDialogVisible: false,
      followupDialogTitle: '更新跟进信息',
      followupForm: {
        id: '',
        studentFeedback: '',
        followupPlan: '',
        followupStatus: 'pending'
      },
      followupRules: {
        followupStatus: [{ required: true, message: '请选择跟进状态', trigger: 'change' }]
      },

      editingId: null,
      followupTargetId: null
    }
  },
  mounted: function() {
    this.fetchData()
  },
  computed: {
    filteredData: function() {
      var self = this
      var data = this.simulatedData.slice()

      if (this.activeTab !== 'all') {
        data = data.filter(function(row) {
          return row.talkType === self.activeTab
        })
      }

      if (this.query.talkType && this.query.talkType !== 'all') {
        data = data.filter(function(row) {
          return row.talkType === self.query.talkType
        })
      }

      if (this.query.keyword) {
        var kw = this.query.keyword.toLowerCase()
        data = data.filter(function(row) {
          return row.studentName.toLowerCase().indexOf(kw) > -1 ||
                 row.studentCode.toLowerCase().indexOf(kw) > -1
        })
      }

      if (this.query.talkPerson) {
        data = data.filter(function(row) {
          return row.talkPerson.indexOf(self.query.talkPerson) > -1
        })
      }

      if (this.query.followupStatus) {
        data = data.filter(function(row) {
          return row.followupStatus === self.query.followupStatus
        })
      }

      if (this.query.tags && this.query.tags.length > 0) {
        data = data.filter(function(row) {
          if (!row.tags || row.tags.length === 0) return false
          return self.query.tags.some(function(t) {
            return row.tags.indexOf(t) > -1
          })
        })
      }

      if (this.query.dateRange && this.query.dateRange.length === 2) {
        var start = this.query.dateRange[0]
        var end = this.query.dateRange[1]
        data = data.filter(function(row) {
          return row.talkTime >= start && row.talkTime <= end
        })
      }

      this.tabs[0].count = this.simulatedData.length
      this.tabs[1].count = this.simulatedData.filter(function(r) { return r.talkType === 'individual' }).length
      this.tabs[2].count = this.simulatedData.filter(function(r) { return r.talkType === 'group' }).length

      return data
    }
  },
  methods: {
    fetchData: function() {
      var self = this
      this.loading = true
      listTalksession({ pageSize: 999 }).then(function(res) {
        var sessions = res.rows || []
        Promise.all(sessions.map(function(s) {
          return listTalkrecord({ sessionId: s.sessionId, pageSize: 999 }).then(function(rr) {
            return Promise.all((rr.rows || []).map(function(rec) {
              return listTalk({ pageSize: 1, studentId: rec.studentId }).then(function(stu) {
                return {
                  ...rec, ...s,
                  studentName: (stu.rows || [])[0]?.studentName || '',
                  studentCode: (stu.rows || [])[0]?.studentCode || '',
                  records: rr.rows
                }
              }).catch(function() { return { ...rec, ...s, studentName: '', studentCode: '' } })
            }))
          }).catch(function() { return [] })
        })).then(function(allRows) {
          self.simulatedData = allRows.flat()
          self.loading = false
        }).catch(function() { self.loading = false })
      }).catch(function() { self.loading = false })
    },

    doSearch: function() {
      this.currentPage = 1
      this.$message.success('搜索完成，共 ' + this.filteredData.length + ' 条结果')
    },
    resetSearch: function() {
      this.query = {
        dateRange: null,
        keyword: '',
        talkType: '',
        tags: [],
        talkPerson: '',
        followupStatus: ''
      }
      this.activeTab = 'all'
      this.currentPage = 1
      this.$message.info('已重置搜索条件')
    },
    switchTab: function(tab) {
      this.activeTab = tab
      this.currentPage = 1
      if (tab !== 'all') {
        this.query.talkType = ''
      }
    },
    handleExpand: function(row, expanded) {
      if (!expanded) return
      if (row.talkType !== 'group') return
      this.$set(row, '_expanding', true)
      var self = this
      setTimeout(function() {
        self.$set(row, '_expanding', false)
      }, 300)
    },

    openCreateDialog: function() {
      this.sessionDialogTitle = '发起新谈话'
      this.editingId = null
      this.sessionForm = {
        id: '',
        talkTime: '',
        talkType: 'individual',
        talkPerson: '',
        talkLocation: '',
        talkContent: '',
        tags: []
      }
      this.sessionDialogVisible = true
      this.$nextTick(function() {
        if (this.$refs.sessionForm) {
          this.$refs.sessionForm.clearValidate()
        }
      })
    },

    openEditDialog: function(row) {
      this.sessionDialogTitle = '编辑谈话会话'
      this.editingId = row.id
      this.sessionForm = {
        id: row.id,
        talkTime: row.talkTime,
        talkType: row.talkType,
        talkPerson: row.talkPerson,
        talkLocation: row.talkLocation,
        talkContent: row.talkContent,
        tags: (row.tags || []).slice()
      }
      this.sessionDialogVisible = true
      this.$nextTick(function() {
        if (this.$refs.sessionForm) {
          this.$refs.sessionForm.clearValidate()
        }
      })
    },

    submitSession: function() {
      var self = this
      this.$refs.sessionForm.validate(function(valid) {
        if (!valid) return
        var form = self.sessionForm

        if (self.editingId) {
          var row = self.simulatedData.find(function(r) { return r.id === self.editingId })
          if (row) {
            row.talkTime = form.talkTime
            row.talkType = form.talkType
            row.talkPerson = form.talkPerson
            row.talkLocation = form.talkLocation
            row.talkContent = form.talkContent
            row.tags = (form.tags || []).slice()
          }
          self.$message.success('编辑成功')
        } else {
          var newId = 'IND-' + String(self.simulatedData.length + 1).padStart(3, '0')
          self.simulatedData.unshift({
            id: newId,
            talkType: form.talkType,
            studentName: '新学生',
            studentCode: '2024xxxxxx',
            talkTime: form.talkTime,
            talkPerson: form.talkPerson,
            talkLocation: form.talkLocation,
            talkContent: form.talkContent,
            tags: (form.tags || []).slice(),
            studentFeedback: '',
            followupPlan: '',
            followupStatus: 'pending',
            students: null
          })
          self.$message.success('新谈话已创建')
        }
        self.sessionDialogVisible = false
      })
    },

    openFollowupDialog: function(row) {
      this.followupDialogTitle = '更新跟进 - ' + row.studentName
      this.followupTargetId = row.id
      this.followupForm = {
        id: row.id,
        studentFeedback: row.studentFeedback || '',
        followupPlan: row.followupPlan || '',
        followupStatus: row.followupStatus || 'pending'
      }
      this.followupDialogVisible = true
      this.$nextTick(function() {
        if (this.$refs.followupForm) {
          this.$refs.followupForm.clearValidate()
        }
      })
    },

    submitFollowup: function() {
      var self = this
      this.$refs.followupForm.validate(function(valid) {
        if (!valid) return
        var row = self.simulatedData.find(function(r) { return r.id === self.followupTargetId })
        if (row) {
          row.studentFeedback = self.followupForm.studentFeedback
          row.followupPlan = self.followupForm.followupPlan
          row.followupStatus = self.followupForm.followupStatus
        }
        self.$message.success('跟进信息已更新')
        self.followupDialogVisible = false
      })
    },

    exportOne: function(row) {
      var self = this
      this.$confirm('确认导出 ' + row.studentName + ' 的谈话记录吗？', '导出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(function() {
        var content = '=== 谈话记录导出 ===\n'
        content += '学生: ' + row.studentName + '\n'
        content += '学号: ' + row.studentCode + '\n'
        content += '类型: ' + (row.talkType === 'individual' ? '个别谈话' : '集体谈话') + '\n'
        content += '时间: ' + row.talkTime + '\n'
        content += '地点: ' + row.talkLocation + '\n'
        content += '谈话人: ' + row.talkPerson + '\n'
        content += '内容: ' + row.talkContent + '\n'
        content += '反馈: ' + (row.studentFeedback || '无') + '\n'
        content += '跟进计划: ' + (row.followupPlan || '无') + '\n'
        var blob = new Blob([content], { type: 'application/msword' })
        var url = window.URL.createObjectURL(blob)
        var a = document.createElement('a')
        a.href = url
        a.download = '谈话记录_' + row.studentName + '.docx'
        a.click()
        window.URL.revokeObjectURL(url)
        self.$message.success('导出成功')
      }).catch(function() {})
    },

    batchExport: function() {
      var self = this
      if (this.selected.length === 0) {
        this.$message.warning('请至少选择一条记录')
        return
      }
      this.$confirm('确认导出选中的 ' + this.selected.length + ' 条记录？', '批量导出', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(function() {
        var content = '=== 谈心谈话批量导出 ===\n'
        content += '导出时间: ' + new Date().toLocaleString() + '\n'
        content += '共 ' + self.selected.length + ' 条记录\n\n'
        self.selected.forEach(function(row, i) {
          content += '--- 记录 ' + (i + 1) + ' ---\n'
          content += '学生: ' + row.studentName + ' | 学号: ' + row.studentCode + '\n'
          content += '类型: ' + (row.talkType === 'individual' ? '个别' : '集体') + ' | 时间: ' + row.talkTime + '\n'
          content += '地点: ' + row.talkLocation + ' | 谈话人: ' + row.talkPerson + '\n'
          content += '内容: ' + row.talkContent + '\n\n'
        })
        var blob = new Blob([content], { type: 'application/msword' })
        var url = window.URL.createObjectURL(blob)
        var a = document.createElement('a')
        a.href = url
        a.download = '谈话记录_批量导出.docx'
        a.click()
        window.URL.revokeObjectURL(url)
        self.$message.success('批量导出成功')
      }).catch(function() {})
    }
  }
}
</script>

<style scoped>
.unified-root {
  padding: 16px;
  background: #f0f2f5;
  min-height: 100vh;
}

/* Search Card */
.search-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 20px 4px;
  margin-bottom: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #ebeef5;
}

.search-form {
  margin-bottom: 0;
}

.search-form >>> .el-form-item {
  margin-bottom: 16px;
}

/* Toolbar Card */
.toolbar-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px 20px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #ebeef5;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.custom-tabs {
  display: flex;
  gap: 4px;
}

.custom-tab {
  padding: 7px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  transition: all 0.25s ease;
  position: relative;
  user-select: none;
}

.custom-tab:hover {
  background: #ecf5ff;
  color: #409eff;
}

.custom-tab.active {
  background: #409eff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

.tab-count {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  background: rgba(255,255,255,0.25);
  text-align: center;
  font-size: 11px;
  margin-left: 4px;
  padding: 0 5px;
}

.custom-tab:not(.active) .tab-count {
  background: #f0f2f5;
  color: #909399;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #ebeef5;
}

.unified-table >>> .el-table__body tr:hover > td {
  background: #f5f7fa !important;
}

.unified-table >>> .el-table__expand-icon {
  color: #409eff;
}

.expand-table >>> th {
  background: #f5f7fa !important;
  font-size: 12px !important;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid #f0f2f5;
}

.table-summary {
  font-size: 13px;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 1200px) {
  .search-form >>> .el-form-item {
    margin-bottom: 12px;
  }
}
</style>