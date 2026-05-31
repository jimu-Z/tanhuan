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
      <el-form-item label="谈话人(默认当前班主任)" prop="talkPerson">
        <el-input
          v-model="queryParams.talkPerson"
          placeholder="请输入谈话人(默认当前班主任)"
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
          @click="handleAdd"
          v-hasPermi="['talk:session:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['talk:session:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['talk:session:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['talk:session:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-download" size="mini" :disabled="multiple2"
          @click="handleBatchExport" v-hasPermi="['talk:session:export']">批量导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-tabs v-model="talkTypeFilter" @tab-click="handleQuery" style="margin-bottom:8px">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="个别谈话" name="individual" />
      <el-tab-pane label="集体谈话" name="group" />
    </el-tabs>

    <el-table v-loading="loading" :data="talksessionList" @selection-change="handleSelectionChange" row-key="sessionId" @expand-change="handleExpand">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column type="expand" v-if="talkTypeFilter==='group'">
        <template slot-scope="scope">
          <div v-loading="scope.row._loadingStudents" style="padding:8px 20px">
            <el-table :data="scope.row._students || []" size="mini">
              <el-table-column label="学生" prop="studentName" width="120" />
              <el-table-column label="学号" prop="studentCode" width="120" />
              <el-table-column label="反馈" prop="studentFeedback" min-width="150" show-overflow-tooltip />
              <el-table-column label="跟进计划" prop="followupPlan" min-width="150" show-overflow-tooltip />
              <el-table-column label="跟进状态" width="80">
                <template slot-scope="s2">
                  <el-tag v-if="s2.row.followupStatus==='pending'" type="info" size="mini">待跟进</el-tag>
                  <el-tag v-else-if="s2.row.followupStatus==='in_progress'" type="warning" size="mini">跟进中</el-tag>
                  <el-tag v-else-if="s2.row.followupStatus==='completed'" type="success" size="mini">已完成</el-tag>
                  <el-tag v-else size="mini">无需跟进</el-tag>
                </template>
              </el-table-column>
            </el-table>
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
      <el-table-column label="谈话时间" align="center" prop="talkTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.talkTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="谈话地点" align="center" prop="talkLocation" />
      <el-table-column label="谈话人(默认当前班主任)" align="center" prop="talkPerson" />
      <el-table-column label="谈话内容" align="center" prop="talkContent" />
      <el-table-column label="内容标签" align="center" width="100">
        <template slot-scope="scope">
          <span v-if="tagMap[scope.row.sessionId]" style="font-size:12px;color:#666">
            {{ tagMap[scope.row.sessionId].map(t => getTagLabel(t.tagValue)).join('、') || '-' }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['talk:session:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['talk:session:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExportDocx(scope.row)"
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

    <!-- 添加或修改谈话会话管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="谈话时间" prop="talkTime">
              <el-date-picker clearable
                v-model="form.talkTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择谈话时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话地点" prop="talkLocation">
              <el-input v-model="form.talkLocation" placeholder="请输入谈话地点" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话人(默认当前班主任)" prop="talkPerson">
              <el-input v-model="form.talkPerson" placeholder="请输入谈话人(默认当前班主任)" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="谈话内容">
              <editor v-model="form.talkContent" :min-height="192"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTalksession, getTalksession, delTalksession, addTalksession, updateTalksession, getSessionTags, TAG_LABELS } from "@/api/talk/talkSession"
import { listTalkrecord } from "@/api/talk/talkStudentRecord"
import { getTalk } from "@/api/talk/talkStudent"
import request from '@/utils/request'

export default {
  name: "Talksession",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      multiple2: true,
      // 显示搜索条件
      showSearch: true,
      talkTypeFilter: '',
      // 总条数
      total: 0,
      // 谈话会话管理表格数据
      talksessionList: [],
      tagMap: {},
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        talkType: null,
        talkTime: null,
        talkLocation: null,
        talkPerson: null,
        talkContent: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        talkType: [
          { required: true, message: "谈话类型不能为空", trigger: "change" }
        ],
        talkTime: [
          { required: true, message: "谈话时间不能为空", trigger: "blur" }
        ],
        talkPerson: [
          { required: true, message: "谈话人(默认当前班主任)不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询谈话会话管理列表 */
    getList() {
      this.loading = true
      this.queryParams.talkType = this.talkTypeFilter || null
      listTalksession(this.queryParams).then(response => {
        this.talksessionList = response.rows
        this.total = response.total
        this.loading = false
        this.loadTags()
      }).catch(() => { this.loading = false })
    },
    loadTags() {
      const ids = this.talksessionList.map(s => s.sessionId)
      if (ids.length === 0) return
      ids.forEach(id => {
        getSessionTags(id).then(res => {
          this.$set(this.tagMap, id, res.data || [])
        }).catch(() => {})
      })
    },
    getTagLabel(value) {
      return TAG_LABELS[value] || value
    },
    handleExpand(row, expanded) {
      if (!expanded || row._students) return
      this.$set(row, '_loadingStudents', true)
      listTalkrecord({ sessionId: row.sessionId, pageSize: 999 }).then(res => {
        const records = res.rows || []
        Promise.all(records.map(r =>
          getTalk(r.studentId).then(stu => ({
            studentName: stu.data ? stu.data.studentName : '-',
            studentCode: stu.data ? stu.data.studentCode : '-',
            studentFeedback: r.studentFeedback,
            followupPlan: r.followupPlan,
            followupStatus: r.followupStatus
          }))
        )).then(students => {
          this.$set(row, '_students', students)
          this.$set(row, '_loadingStudents', false)
        }).catch(() => { this.$set(row, '_loadingStudents', false) })
      }).catch(() => { this.$set(row, '_loadingStudents', false) })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
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
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.sessionId)
      this.single = selection.length!==1
      this.multiple = !selection.length
      this.multiple2 = selection.length < 1
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加谈话会话管理"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      var sessionId = row.sessionId || (Array.isArray(this.ids) ? this.ids[0] : this.ids)
      if (!sessionId) {
        this.$modal.msgWarning('请选择一条记录进行编辑')
        return
      }
      getTalksession(sessionId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = "修改谈话会话管理"
      }).catch(() => { this.$modal.msgError('获取会话详情失败') })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.sessionId != null) {
            updateTalksession(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            }).catch(() => {})
          } else {
            addTalksession(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(() => {})
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const sessionIds = row.sessionId || this.ids
      this.$modal.confirm('是否确认删除谈话会话管理编号为"' + sessionIds + '"的数据项？').then(function() {
        return delTalksession(sessionIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('ruoyi-system/talksession/export', {
        ...this.queryParams
      }, `talksession_${new Date().getTime()}.xlsx`)
    },
    handleBatchExport() {
      if (this.ids.length === 0) { this.$modal.msgWarning('请至少选择一条记录'); return }
      this.$modal.confirm('确认导出选中的 ' + this.ids.length + ' 条会话记录？').then(() => {
        return request({ url: '/ruoyi-system/talksession/exportDocx/batch', method: 'post', data: this.ids, responseType: 'blob' }).catch(() => { this.$modal.msgError('导出失败') })
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url; a.download = '谈话记录批量导出.zip'; a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {})
    },
    handleExportDocx(row) {
      const isGroup = row.talkType === 'group'
      const ext = isGroup ? '.zip' : '.docx'
      this.$modal.confirm('确认导出' + row.talkPerson + '的谈话记录吗？' + (isGroup ? '（集体谈话将打包为zip）' : '')).then(() => {
        return request({ url: '/ruoyi-system/talksession/exportDocx/' + row.sessionId, method: 'get', responseType: 'blob' }).catch(() => { this.$modal.msgError('导出失败') })
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = '谈话记录_' + (row.talkPerson || row.sessionId) + ext
        a.click()
        window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {})
    }
  }
}
</script>
