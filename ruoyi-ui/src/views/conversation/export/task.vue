<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-download" size="mini" @click="handleSubmit" v-hasPermi="['conversation:export:task']">新建导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="任务名称" align="center" prop="taskName" :show-overflow-tooltip="true" />
      <el-table-column label="导出类型" align="center" prop="exportType">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.exportType === 'xls'" type="success">XLS</el-tag>
          <el-tag v-else type="warning">PDF</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '1'" type="warning">处理中</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="danger">失败</el-tag>
          <el-tag v-else type="info">待处理</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="记录数" align="center" prop="recordCount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === '2'" size="mini" type="text" icon="el-icon-download" @click="handleDownload(scope.row)">下载</el-button>
          <el-popconfirm title="确定要删除该任务吗？" @confirm="handleDelete(scope.row)">
            <el-button size="mini" type="text" icon="el-icon-delete" slot="reference">删除</el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="导出类型" prop="exportType">
          <el-select v-model="form.exportType" placeholder="请选择导出类型" style="width: 100%">
            <el-option label="Excel(.xls)" value="xls" />
            <el-option label="PDF" value="pdf" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTask, submitTask, delTask, downloadTask } from "@/api/conversation/export"

export default {
  name: "ExportTask",
  data() {
    return {
      loading: true,
      ids: [],
      taskList: [],
      total: 0,
      showSearch: true,
      title: "",
      open: false,
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      form: { taskName: "", exportType: "xls" },
      rules: { taskName: [{ required: true, message: "请输入任务名称", trigger: "blur" }], exportType: [{ required: true, message: "请选择导出类型", trigger: "change" }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTask(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.taskId) },
    handleSubmit() {
      this.reset()
      this.open = true
      this.title = "新建导出任务"
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const data = { ...this.form }
          if (this.dateRange && this.dateRange.length === 2) {
            data.beginTime = this.dateRange[0]
            data.endTime = this.dateRange[1]
          }
          submitTask(data).then(() => {
            this.$modal.msgSuccess("导出任务已提交")
            this.open = false
            this.getList()
          })
        }
      })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { taskName: "", exportType: "xls" }
      this.dateRange = []
    },
    handleDownload(row) {
      downloadTask(row.taskId).then(response => {
        const blob = new Blob([response])
        const link = document.createElement("a")
        link.href = URL.createObjectURL(blob)
        link.download = row.fileName || "导出文件"
        link.click()
      })
    },
    handleDelete(row) {
      delTask(row.taskId).then(() => { this.$modal.msgSuccess("删除成功"); this.getList() })
    }
  }
}
</script>