<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="预约状态" clearable>
          <el-option
            v-for="dict in dict.type.appointment_status"
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['talk:appointment:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="appointmentList">
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="学号" align="center" prop="studentCode" />
      <el-table-column label="预约教师" align="center" prop="teacherName" />
      <el-table-column label="预约时间" align="center" prop="appointmentTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.appointmentTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预约地点" align="center" prop="location" show-overflow-tooltip />
      <el-table-column label="预约原因" align="center" prop="reason" show-overflow-tooltip min-width="150" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="info">待确认</el-tag>
          <el-tag v-else-if="scope.row.status === 'confirmed'" type="success">已确认</el-tag>
          <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
          <el-tag v-else-if="scope.row.status === 'completed'">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 'cancelled'" type="warning">已取消</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status === 'pending' && !isCurrentStudent(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleConfirm(scope.row)"
          >确认</el-button>
          <el-button
            v-if="scope.row.status === 'pending' && !isCurrentStudent(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
          >拒绝</el-button>
          <el-button
            v-if="scope.row.status === 'pending' && isCurrentStudent(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-circle-close"
            @click="handleCancel(scope.row)"
          >取消</el-button>
          <el-button
            v-if="scope.row.status === 'confirmed'"
            size="mini"
            type="text"
            icon="el-icon-circle-check"
            @click="handleComplete(scope.row)"
          >完成</el-button>
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

    <!-- 新增/修改预约对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentCode">
          <el-input v-model="form.studentCode" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="预约教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择预约教师" filterable style="width:100%">
            <el-option
              v-for="t in teacherOptions"
              :key="t.teacherId"
              :label="t.teacherName + ' (' + t.position + ')'"
              :value="t.teacherId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="form.appointmentTime"
            type="datetime"
            placeholder="选择预约时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="预约地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入预约地点" />
        </el-form-item>
        <el-form-item label="预约原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入预约原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog :title="'拒绝预约'" :visible.sync="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAppointment, addAppointment, updateAppointment, confirmAppointment, rejectAppointment, cancelAppointment, completeAppointment } from "@/api/talk/appointment"
import { getCounselors } from "@/api/talk/teacher"

export default {
  name: "TalkAppointment",
  dicts: ['appointment_status'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      appointmentList: [],
      title: "",
      open: false,
      rejectOpen: false,
      currentRow: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        status: undefined
      },
      form: {},
      rejectForm: {
        rejectReason: ""
      },
      // 教师列表（用于预约选择）
      teacherOptions: [],
      rules: {
        studentName: [
          { required: true, message: "学生姓名不能为空", trigger: "blur" }
        ],
        studentCode: [
          { required: true, message: "学号不能为空", trigger: "blur" }
        ],
        teacherId: [
          { required: true, message: "请选择预约教师", trigger: "change" }
        ],
        appointmentTime: [
          { required: true, message: "预约时间不能为空", trigger: "change" }
        ],
        location: [
          { required: true, message: "预约地点不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadTeacherOptions()
  },
  methods: {
    /** 加载可选教师列表（从学生所属学院获取） */
    loadTeacherOptions() {
      // 获取当前用户部门ID，查询该学院的教师
      const deptId = this.$store.state.user.dept?.deptId
      if (deptId) {
        getCounselors(deptId).then(res => {
          this.teacherOptions = res.data || []
        }).catch(() => {})
      }
    },
    getList() {
      this.loading = true
      listAppointment(this.queryParams).then(response => {
        this.appointmentList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        appointmentId: undefined,
        studentName: undefined,
        studentCode: undefined,
        teacherId: undefined,
        appointmentTime: undefined,
        location: undefined,
        reason: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增预约"
    },
    handleUpdate(row) {
      this.reset()
      if (row && row.appointmentId) {
        this.form = JSON.parse(JSON.stringify(row))
      }
      this.open = true
      this.title = "修改预约"
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.appointmentId != undefined) {
            updateAppointment(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addAppointment(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    isCurrentStudent(row) {
      return String(this.$store.getters.id) === String(row.studentUserId)
    },
    handleConfirm(row) {
      this.$modal.confirm('确认通过"' + row.studentName + '"的预约申请？').then(() => {
        return confirmAppointment(row.appointmentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("确认成功")
      }).catch(() => {})
    },
    handleReject(row) {
      this.currentRow = row
      this.rejectForm.rejectReason = ""
      this.rejectOpen = true
    },
    submitReject() {
      if (!this.rejectForm.rejectReason) {
        this.$modal.msgWarning("请输入拒绝原因")
        return
      }
      rejectAppointment(this.currentRow.appointmentId, this.rejectForm.rejectReason).then(() => {
        this.$modal.msgSuccess("已拒绝该预约")
        this.rejectOpen = false
        this.getList()
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$modal.confirm('确认取消"' + row.studentName + '"的预约？').then(() => {
        return cancelAppointment(row.appointmentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("取消成功")
      }).catch(() => { this.$modal.msgError('操作失败') })
    },
    handleComplete(row) {
      this.$modal.confirm('确认完成"' + row.studentName + '"的预约？').then(() => {
        return completeAppointment(row.appointmentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已完成")
      }).catch(() => { this.$modal.msgError('操作失败') })
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

    &:last-child {
      color: #2a6fa8;
      &:hover { color: #3a85c0; text-decoration: underline; }
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
