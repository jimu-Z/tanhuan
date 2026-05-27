<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="queryParams.studentNo" placeholder="请输入学号" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="谈话主题" prop="topic">
        <el-input v-model="queryParams.topic" placeholder="请输入主题" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="已完成" value="0" />
          <el-option label="待跟进" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="谈话时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新建谈话</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange" @row-click="handleRowClick" highlight-current-row style="cursor: pointer">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="学号" align="center" prop="studentNo" width="120" />
      <el-table-column label="学院" align="center" prop="collegeName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="专业" align="center" prop="majorName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="班级" align="center" prop="className" width="100" :show-overflow-tooltip="true" />
      <el-table-column label="谈话主题" align="center" prop="topic" :show-overflow-tooltip="true" />
      <el-table-column label="谈话时间" align="center" prop="conversationTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.conversationTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'warning'" size="small">
            {{ scope.row.status === '0' ? '已完成' : '待跟进' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="'谈话详情'" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border size="medium" v-if="currentRecord">
        <el-descriptions-item label="学生姓名">{{ currentRecord.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentRecord.collegeName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentRecord.majorName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentRecord.className }}</el-descriptions-item>
        <el-descriptions-item label="谈话人">{{ currentRecord.speaker }}</el-descriptions-item>
        <el-descriptions-item label="谈话时间">{{ currentRecord.conversationTime }}</el-descriptions-item>
        <el-descriptions-item label="谈话地点">{{ currentRecord.conversationPlace }}</el-descriptions-item>
        <el-descriptions-item label="谈话主题" :span="2">{{ currentRecord.topic }}</el-descriptions-item>
        <el-descriptions-item label="谈话内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto">{{ currentRecord.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="后续跟进事项" :span="2">
          <div style="white-space: pre-wrap; max-height: 150px; overflow-y: auto">{{ currentRecord.followUpItems || '无' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">跟进记录</el-divider>
      <el-table :data="followUpList" border stripe max-height="300">
        <el-table-column label="跟进时间" prop="createTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="跟进人" prop="createBy" width="100" />
        <el-table-column label="跟进内容" prop="content" :show-overflow-tooltip="true" />
      </el-table>
      <div v-if="followUpList.length === 0" style="text-align: center; padding: 20px; color: #909399">
        暂无跟进记录
      </div>

      <el-divider content-position="left">添加跟进</el-divider>
      <el-form ref="followUpForm" :model="followUpForm" label-width="80px" size="small">
        <el-form-item label="跟进内容" prop="content" :rules="[{ required: true, message: '跟进内容不能为空', trigger: 'blur' }]">
          <el-input v-model="followUpForm.content" type="textarea" :rows="3" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFollowUp">提交跟进</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { listMyRecord, getRecord, delRecord, addFollowUp, getFollowUpList } from "@/api/conversation/record"

export default {
  name: "RecordMy",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      recordList: [],
      dateRange: [],
      detailOpen: false,
      currentRecord: null,
      followUpList: [],
      followUpForm: {
        content: ''
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        studentNo: undefined,
        topic: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMyRecord(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleRowClick(row) {
      this.handleDetail(row)
    },
    handleAdd() {
      this.$router.push({ path: '/conversation/record/add' })
    },
    handleDetail(row) {
      const recordId = row.recordId || row
      getRecord(recordId).then(response => {
        this.currentRecord = response.data
        this.detailOpen = true
        this.followUpForm.content = ''
        this.loadFollowUpList(recordId)
      })
    },
    loadFollowUpList(recordId) {
      getFollowUpList(recordId).then(response => {
        this.followUpList = response.data || response.rows || []
      })
    },
    submitFollowUp() {
      if (!this.followUpForm.content) {
        this.$modal.msgWarning("请输入跟进内容")
        return
      }
      addFollowUp({
        recordId: this.currentRecord.recordId,
        content: this.followUpForm.content
      }).then(() => {
        this.$modal.msgSuccess("跟进记录添加成功")
        this.followUpForm.content = ''
        this.loadFollowUpList(this.currentRecord.recordId)
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal.confirm('是否确认删除谈话记录编号为"' + recordIds + '"的数据项？').then(function() {
        return delRecord(recordIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>