<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="专业名称" prop="majorName">
        <el-input v-model="queryParams.majorName" placeholder="请输入专业名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="专业编码" prop="majorCode">
        <el-input v-model="queryParams.majorCode" placeholder="请输入专业编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="学院" prop="deptId">
        <treeselect v-model="queryParams.deptId" :options="deptOptions" :show-count="true" placeholder="请选择学院" style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['conversation:major:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['conversation:major:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['conversation:major:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="majorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="专业名称" align="center" prop="majorName" :show-overflow-tooltip="true" />
      <el-table-column label="专业编码" align="center" prop="majorCode" />
      <el-table-column label="所属学院" align="center" prop="deptName" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['conversation:major:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['conversation:major:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学院" prop="deptId">
          <treeselect v-model="form.deptId" :options="deptOptions" :show-count="true" placeholder="请选择学院" />
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="form.majorName" placeholder="请输入专业名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="专业编码" prop="majorCode">
          <el-input v-model="form.majorCode" placeholder="请输入专业编码" maxlength="30" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
import { listMajor, getMajor, delMajor, addMajor, updateMajor, deptTree } from "@/api/conversation/major"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "Major",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      majorList: [],
      title: "",
      deptOptions: [],
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        majorName: undefined,
        majorCode: undefined,
        deptId: undefined
      },
      form: {},
      rules: {
        majorName: [
          { required: true, message: "专业名称不能为空", trigger: "blur" }
        ],
        majorCode: [
          { required: true, message: "专业编码不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeptTree()
  },
  methods: {
    getList() {
      this.loading = true
      listMajor(this.queryParams).then(response => {
        this.majorList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDeptTree() {
      deptTree().then(response => {
        this.deptOptions = response.data
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        majorId: undefined,
        majorName: undefined,
        majorCode: undefined,
        deptId: undefined,
        remark: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.queryParams.deptId = undefined
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.majorId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加专业"
    },
    handleUpdate(row) {
      this.reset()
      const majorId = row.majorId || this.ids
      getMajor(majorId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改专业"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.majorId != undefined) {
            updateMajor(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addMajor(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const majorIds = row.majorId || this.ids
      this.$modal.confirm('是否确认删除专业编号为"' + majorIds + '"的数据项？').then(function() {
        return delMajor(majorIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>