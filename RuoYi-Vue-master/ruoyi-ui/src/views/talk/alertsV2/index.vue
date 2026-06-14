<template>
  <div class="app-container">
    <!-- 标题区域 -->
    <div class="page-header">
      <div class="page-header-content">
        <h2 class="page-title">
          <i class="el-icon-warning-outline"></i>
          心理健康预警中心
        </h2>
        <p class="page-subtitle">实时监控学生心理健康状况，及时发现和干预潜在问题</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-card-total">
          <div class="stat-card-inner">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-body">
              <div class="stat-label">全部预警数</div>
              <div class="stat-value">{{ stats.totalAlerts }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-red">
          <div class="stat-card-inner">
            <div class="stat-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-body">
              <div class="stat-label">红色预警数</div>
              <div class="stat-value">{{ stats.redAlerts }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-pending">
          <div class="stat-card-inner">
            <div class="stat-icon">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-body">
              <div class="stat-label">待处理数</div>
              <div class="stat-value">{{ stats.pendingAlerts }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-today">
          <div class="stat-card-inner">
            <div class="stat-icon">
              <i class="el-icon-date"></i>
            </div>
            <div class="stat-body">
              <div class="stat-label">今日新增数</div>
              <div class="stat-value">{{ stats.todayAlerts }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预警等级" prop="alertLevel">
        <el-select v-model="queryParams.alertLevel" placeholder="请选择预警等级" clearable>
          <el-option
            v-for="dict in dict.type.alert_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="预警类型" prop="alertType">
        <el-select v-model="queryParams.alertType" placeholder="请选择预警类型" clearable>
          <el-option
            v-for="dict in dict.type.alert_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="alertStatus">
        <el-select v-model="queryParams.alertStatus" placeholder="请选择处理状态" clearable>
          <el-option
            v-for="dict in dict.type.alert_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="search-btn-item">
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['talk:alert:add']"
        >新增预警</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['talk:alert:edit']"
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
          v-hasPermi="['talk:alert:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="handleInitAlerts"
          v-hasPermi="['talk:alert:add']"
        >批量初始化</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="alertList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生姓名" align="center" prop="studentName" min-width="100" show-overflow-tooltip />
      <el-table-column label="学号" align="center" prop="studentCode" min-width="120" show-overflow-tooltip />
      <el-table-column label="预警等级" align="center" prop="alertLevel" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.alertLevel === 'red'" type="danger">红色预警</el-tag>
          <el-tag v-else-if="scope.row.alertLevel === 'orange'" type="warning">橙色预警</el-tag>
          <el-tag v-else-if="scope.row.alertLevel === 'yellow'">黄色预警</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="预警类型" align="center" prop="alertType" width="100">
        <template slot-scope="scope">
          <span>{{ selectDictLabel(dict.type.alert_type, scope.row.alertType) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预警原因" align="center" prop="alertReason" min-width="200" show-overflow-tooltip />
      <el-table-column label="处理状态" align="center" prop="alertStatus" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.alertStatus === 'pending'" type="info">待处理</el-tag>
          <el-tag v-else-if="scope.row.alertStatus === 'in_progress'" type="warning">处理中</el-tag>
          <el-tag v-else-if="scope.row.alertStatus === 'resolved'" type="success">已解除</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="handler" width="100" show-overflow-tooltip />
      <el-table-column label="处理时间" align="center" prop="handleTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.handleTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['talk:alert:handle']"
            v-if="scope.row.alertStatus === 'pending'"
            size="mini"
            type="text"
            icon="el-icon-s-tools"
            @click="handleHandle(scope.row)"
          >处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['talk:alert:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['talk:alert:remove']"
          >删除</el-button>
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

    <!-- 新增/修改预警对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择学生" prop="studentId">
          <el-select
            v-model="form.studentId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入学生姓名搜索"
            :remote-method="searchStudent"
            :loading="studentLoading"
            style="width: 100%"
            @change="handleStudentChange"
          >
            <el-option
              v-for="s in studentOptions"
              :key="s.studentId"
              :label="s.studentName + ' (' + s.studentCode + ')'"
              :value="s.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警等级" prop="alertLevel">
          <el-select v-model="form.alertLevel" placeholder="请选择预警等级" style="width: 100%">
            <el-option
              v-for="dict in dict.type.alert_level"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警原因" prop="alertReason">
          <el-input
            v-model="form.alertReason"
            type="textarea"
            :rows="4"
            placeholder="请输入预警原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 处理预警对话框 -->
    <el-dialog :title="'处理预警 - ' + currentAlert.studentName" :visible.sync="handleOpen" width="500px" append-to-body>
      <el-form ref="handleForm" :model="handleForm" :rules="handleRules" label-width="80px">
        <el-form-item label="处理状态" prop="alertStatus">
          <el-select v-model="handleForm.alertStatus" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="处理中" value="in_progress" />
            <el-option label="已解除" value="resolved" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitHandle">确 定</el-button>
        <el-button @click="handleOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAlert, getAlert, addAlert, updateAlert, delAlert, handleAlert, initAlerts } from "@/api/talk/alert"
import { listTalk } from "@/api/talk/talkStudent"

export default {
  name: "AlertsV2",
  dicts: ['alert_level', 'alert_type', 'alert_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      alertList: [],
      title: "",
      open: false,
      handleOpen: false,
      currentAlert: {},
      studentLoading: false,
      studentOptions: [],
      // 统计
      stats: {
        totalAlerts: 0,
        redAlerts: 0,
        pendingAlerts: 0,
        todayAlerts: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        alertLevel: undefined,
        alertType: undefined,
        alertStatus: undefined
      },
      form: {},
      rules: {
        studentId: [
          { required: true, message: "请选择学生", trigger: "change" }
        ],
        alertLevel: [
          { required: true, message: "请选择预警等级", trigger: "change" }
        ],
        alertReason: [
          { required: true, message: "请输入预警原因", trigger: "blur" }
        ]
      },
      handleForm: {
        alertStatus: "in_progress",
        handleRemark: undefined
      },
      handleRules: {
        alertStatus: [
          { required: true, message: "请选择处理状态", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.fetchStats()
  },
  methods: {
    /** 查询预警列表 */
    getList() {
      this.loading = true
      listAlert(this.queryParams).then(response => {
        this.alertList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    /** 获取统计数据
     *  注意：当前使用 pageSize:9999 拉取全量数据做前端统计，
     *  数据量较大时需改为后端提供统计接口 */
    fetchStats() {
      listAlert({ pageNum: 1, pageSize: 9999 }).then(response => {
        const rows = response.rows || []
        this.stats.totalAlerts = rows.length
        this.stats.redAlerts = rows.filter(r => r.alertLevel === 'red').length
        this.stats.pendingAlerts = rows.filter(r => r.alertStatus === 'pending').length
        const today = this.getTodayStr()
        this.stats.todayAlerts = rows.filter(r => {
          return r.createTime && r.createTime.indexOf(today) === 0
        }).length
      }).catch(() => { this.$modal.msgError('删除失败') })
    },
    /** 获取今日日期字符串 yyyy-MM-dd */
    getTodayStr() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      return y + '-' + m + '-' + d
    },
    /** 远程搜索学生 */
    searchStudent(query) {
      if (query !== '') {
        this.studentLoading = true
        listTalk({ pageNum: 1, pageSize: 20, studentName: query }).then(response => {
          this.studentOptions = response.rows || []
          this.studentLoading = false
        }).catch(() => { this.studentLoading = false })
      } else {
        this.studentOptions = []
      }
    },
    /** 学生选中回调 */
    handleStudentChange(val) {
      if (val) {
        const s = this.studentOptions.find(item => item.studentId === val)
        if (s) {
          this.form.studentName = s.studentName
          this.form.studentCode = s.studentCode
        }
      }
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
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.alertId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增预警"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      if (row && row.alertId) {
        this.form = JSON.parse(JSON.stringify(row))
      } else {
        const item = this.alertList.find(item => item.alertId === this.ids[0])
        if (item) {
          this.form = JSON.parse(JSON.stringify(item))
        }
      }
      this.open = true
      this.title = "修改预警"
    },
    /** 处理按钮操作 */
    handleHandle(row) {
      this.currentAlert = row
      this.handleForm = {
        alertStatus: "in_progress",
        handleRemark: undefined
      }
      this.handleOpen = true
    },
    /** 提交新增/修改 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.alertId != undefined) {
            updateAlert(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
              this.fetchStats()
            })
          } else {
            this.form.alertType = 'manual'
            addAlert(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
              this.fetchStats()
            })
          }
        }
      })
    },
    /** 提交处理 */
    submitHandle() {
      this.$refs["handleForm"].validate(valid => {
        if (valid) {
          handleAlert(this.currentAlert.alertId, this.handleForm.alertStatus, this.handleForm.handleRemark).then(() => {
            this.$modal.msgSuccess("处理成功")
            this.handleOpen = false
            this.getList()
            this.fetchStats()
          })
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const alertIds = row.alertId || this.ids.join(',')
      this.$modal.confirm('是否确认删除预警编号为"' + alertIds + '"的数据项？').then(function() {
        return delAlert(alertIds)
      }).then(() => {
        this.getList()
        this.fetchStats()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { this.$modal.msgError('删除失败') })
    },
    /** 批量初始化预警 */
    handleInitAlerts() {
      this.$modal.confirm('是否根据学生心理健康状态批量初始化预警？').then(() => {
        return initAlerts()
      }).then(res => {
        this.$modal.msgSuccess(res.msg || '初始化成功')
        this.getList()
        this.fetchStats()
      }).catch(() => {})
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        alertId: undefined,
        studentId: undefined,
        studentName: undefined,
        studentCode: undefined,
        alertLevel: undefined,
        alertType: undefined,
        alertReason: undefined
      }
      this.studentOptions = []
      this.resetForm("form")
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

/* 标题区域 */
.page-header {
  margin-bottom: 20px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(26, 82, 118, 0.25);

  .page-header-content {
    .page-title {
      color: #ffffff;
      font-size: 22px;
      font-weight: 700;
      margin: 0 0 8px 0;
      display: flex;
      align-items: center;
      gap: 10px;

      i {
        font-size: 26px;
      }
    }

    .page-subtitle {
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;
      margin: 0;
    }
  }
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(26, 82, 118, 0.08);
  transition: all 0.3s ease;
  cursor: default;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(26, 82, 118, 0.15);
  }

  .stat-card-inner {
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    i {
      font-size: 26px;
      color: #ffffff;
    }
  }

  .stat-body {
    flex: 1;
    min-width: 0;
  }

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 4px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.1;
  }
}

.stat-card-total {
  border-top: 3px solid #2a6fa8;
  .stat-icon { background: linear-gradient(135deg, #2a6fa8, #4a8fc7); }
}

.stat-card-red {
  border-top: 3px solid #e64340;
  .stat-icon { background: linear-gradient(135deg, #e64340, #f06060); }
  .stat-value { color: #e64340; }
}

.stat-card-pending {
  border-top: 3px solid #e6a23c;
  .stat-icon { background: linear-gradient(135deg, #e6a23c, #f0b446); }
  .stat-value { color: #e6a23c; }
}

.stat-card-today {
  border-top: 3px solid #67c23a;
  .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
  .stat-value { color: #67c23a; }
}

/* 搜索栏 */
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

  ::v-deep .el-select {
    .el-input__inner {
      width: 100%;
      border-radius: 6px;
      border: 1px solid #d4e0eb;

      &:hover { border-color: #2a6fa8; }
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

/* 操作按钮区 */
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
}

/* 表格 */
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
      color: #2a6fa8;
      &:hover { color: #3a85c0; text-decoration: underline; }
    }

    &:last-child {
      color: #e64340;
      &:hover { color: #f06060; text-decoration: underline; }
    }
  }
}

/* 分页 */
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

/* 对话框 */
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

  .stats-row .el-col {
    margin-bottom: 12px;
  }
}
</style>
