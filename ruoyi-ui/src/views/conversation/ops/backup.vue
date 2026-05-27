<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-upload" size="mini" @click="handleManualBackup" v-hasPermi="['conversation:ops:backup']" :loading="backupLoading">手动备份</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="backupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="备份类型" align="center" prop="backupType">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.backupType === '0'" type="primary">手动</el-tag>
          <el-tag v-else type="success">自动</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件名" align="center" prop="fileName" :show-overflow-tooltip="true" />
      <el-table-column label="文件大小" align="center" prop="fileSize">
        <template slot-scope="scope">
          <span>{{ scope.row.fileSize ? (scope.row.fileSize / 1024 / 1024).toFixed(2) + ' MB' : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '1'" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备份时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === '1'" size="mini" type="text" icon="el-icon-download" @click="handleDownload(scope.row)">下载</el-button>
          <el-popconfirm title="确定要删除该备份记录吗？" @confirm="handleDelete(scope.row)">
            <el-button size="mini" type="text" icon="el-icon-delete" slot="reference">删除</el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listBackup, manualBackup, delBackup, downloadBackup } from "@/api/conversation/backup"

export default {
  name: "BackupLog",
  data() {
    return {
      loading: true,
      backupLoading: false,
      ids: [],
      backupList: [],
      total: 0,
      showSearch: true,
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listBackup(this.queryParams).then(response => {
        this.backupList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.backupId) },
    handleManualBackup() {
      this.$modal.confirm("确认执行手动备份？此操作可能需要一些时间。").then(() => {
        this.backupLoading = true
        manualBackup().then(() => {
          this.$modal.msgSuccess("备份成功")
          this.backupLoading = false
          this.getList()
        }).catch(() => { this.backupLoading = false })
      })
    },
    handleDownload(row) {
      downloadBackup(row.backupId).then(response => {
        const blob = new Blob([response])
        const link = document.createElement("a")
        link.href = URL.createObjectURL(blob)
        link.download = row.fileName
        link.click()
      })
    },
    handleDelete(row) {
      delBackup(row.backupId).then(() => { this.$modal.msgSuccess("删除成功"); this.getList() })
    }
  }
}
</script>