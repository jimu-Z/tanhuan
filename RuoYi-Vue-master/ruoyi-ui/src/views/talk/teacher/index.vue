<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="工号" prop="teacherCode">
        <el-input
          v-model="queryParams.teacherCode"
          placeholder="请输入工号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="teacherName">
        <el-input
          v-model="queryParams.teacherName"
          placeholder="请输入姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="岗位" prop="position">
        <el-select v-model="queryParams.position" placeholder="请选择岗位" clearable>
          <el-option
            v-for="item in positionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学院" prop="deptId">
        <treeselect v-model="queryParams.deptId" :options="collegeDeptTree"
          :normalizer="normalizer" placeholder="请选择学院" clearable
          @input="handleQuery" />
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
          v-hasPermi="['talk:teacher:add']"
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
          v-hasPermi="['talk:teacher:edit']"
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
          v-hasPermi="['talk:teacher:remove']"
        >删除</el-button>
      </el-col>
      <!-- 导出功能暂未实现
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['talk:teacher:export']"
        >导出</el-button>
      </el-col>
      -->
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['talk:teacher:import']"
        >导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="teacherList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="工号" align="center" prop="teacherCode" />
      <el-table-column label="姓名" align="center" prop="teacherName" />
      <el-table-column label="所属学院" align="center" prop="deptName" />
      <el-table-column label="岗位" align="center" prop="position">
        <template slot-scope="scope">
          <dict-tag :options="positionOptions" :value="scope.row.position" />
        </template>
      </el-table-column>
      <el-table-column label="手机号码" align="center" prop="phone" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="danger">停用</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['talk:teacher:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['talk:teacher:remove']"
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

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工号" prop="teacherCode">
          <el-input v-model="form.teacherCode" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="所属学院" prop="deptId">
          <treeselect v-model="form.deptId" :options="collegeDeptTree"
            :normalizer="normalizer" placeholder="请选择学院" />
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-select v-model="form.position" placeholder="请选择岗位">
            <el-option
              v-for="item in filteredPositionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTeacher, getTeacher, addTeacher, updateTeacher, delTeacher } from "@/api/talk/teacher"
import { listDept } from "@/api/system/dept"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "TalkTeacher",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      teacherList: [],
      title: "",
      open: false,
      collegeDeptTree: [],
      positionOptions: [
        { label: "辅导员", value: "counselor" },
        { label: "班主任", value: "head_teacher" },
        { label: "副书记", value: "vice_secretary" },
        { label: "书记", value: "secretary" }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        teacherCode: undefined,
        teacherName: undefined,
        position: undefined,
        deptId: undefined
      },
      form: {},
      rules: {
        teacherCode: [
          { required: true, message: "工号不能为空", trigger: "blur" }
        ],
        teacherName: [
          { required: true, message: "姓名不能为空", trigger: "blur" }
        ],
        deptId: [
          { required: true, message: "所属学院不能为空", trigger: "change" }
        ],
        position: [
          { required: true, message: "岗位不能为空", trigger: "change" }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: "请输入有效的手机号码", trigger: "blur" }
        ]
      }
    }
  },
  computed: {
    filteredPositionOptions() {
      const roles = this.$store.state.user?.roles || []
      const isSecretary = roles.some(r => r.roleKey === 'secretary' || r.roleName === '书记')
      if (isSecretary) {
        return this.positionOptions.filter(
          item => item.value === 'counselor' || item.value === 'head_teacher'
        )
      }
      return this.positionOptions
    }
  },
  created() {
    this.getList()
    this.loadCollegeDeptTree()
  },
  methods: {
    loadCollegeDeptTree() {
      listDept().then(res => {
        const allList = res.data || []
        const collegeList = allList.filter(d => d.deptType === 'college')
        const map = {}
        const tree = []
        allList.forEach(d => {
          map[d.deptId] = { id: d.deptId, label: d.deptName, children: [], deptType: d.deptType, parentId: d.parentId }
        })
        allList.forEach(d => {
          const node = map[d.deptId]
          if (node && d.parentId && map[d.parentId]) {
            map[d.parentId].children.push(node)
          }
          if (node && (!d.parentId || d.parentId === 0 || d.parentId === 100)) {
            tree.push(node)
          }
        })
        // 只保留根到college的路径
        this.collegeDeptTree = this.filterCollegeBranches(tree, collegeList.map(d => d.deptId))
      }).catch(() => { this.$modal.msgError('获取学院列表失败') })
    },
    filterCollegeBranches(nodes, collegeIds) {
      if (!nodes || nodes.length === 0) return []
      const result = []
      nodes.forEach(node => {
        const children = node.children ? this.filterCollegeBranches(node.children, collegeIds) : []
        if (collegeIds.includes(node.id) || children.length > 0) {
          result.push({ ...node, children })
        }
      })
      return result
    },
    normalizer(node) {
      return {
        id: node.id,
        label: node.label,
        children: node.children && node.children.length ? node.children : undefined
      }
    },
    getList() {
      this.loading = true
      listTeacher(this.queryParams).then(response => {
        this.teacherList = response.rows
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
        teacherId: undefined,
        teacherCode: undefined,
        teacherName: undefined,
        deptId: undefined,
        position: undefined,
        phone: undefined,
        status: "0"
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.teacherId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加教师"
    },
    handleUpdate(row) {
      this.reset()
      if (row && row.teacherId) {
        this.form = JSON.parse(JSON.stringify(row))
      } else {
        const teacherId = this.ids[0]
        getTeacher(teacherId).then(response => {
          this.form = response.data || {}
          this.open = true
          this.title = "修改教师"
        }).catch(() => { this.$modal.msgError('获取教师详情失败') })
        return
      }
      this.open = true
      this.title = "修改教师"
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.teacherId != undefined) {
            updateTeacher(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTeacher(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const teacherIds = row.teacherId || this.ids.join(',')
      this.$modal.confirm('是否确认删除教师编号为"' + teacherIds + '"的数据项？').then(function() {
        return delTeacher(teacherIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { this.$modal.msgError('操作失败') })
    },
    // 后端未实现导出接口，暂时注释
    // handleExport() {
    //   this.download('/talk/teacher/export', {
    //     ...this.queryParams
    //   }, `teacher_${new Date().getTime()}.xlsx`)
    // },
    handleImport() {
      this.$modal.msgInfo("导入功能待实现")
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

  ::v-deep .vue-treeselect__control {
    height: 36px;
    border-radius: 6px;
    border: 1px solid #d4e0eb;

    &:hover { border-color: #2a6fa8; }

    .vue-treeselect__value-container {
      .vue-treeselect__placeholder {
        color: #c0c4cc;
        line-height: 34px;
      }
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
    border-color: #e6a23c;
    color: #e6a23c;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #e6a23c 0%, #f0b856 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
    }
  }

  ::v-deep .el-button--info[plain] {
    border-color: #909399;
    color: #909399;
    background: rgba(255, 255, 255, 0.9);

    &:hover {
      background: linear-gradient(135deg, #909399 0%, #b0b3b8 100%);
      color: #ffffff;
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
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

    &:last-child {
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

    .vue-treeselect__control {
      border-radius: 6px;
      border: 1px solid #d4e0eb;

      &:hover { border-color: #2a6fa8; }
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
