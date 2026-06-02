<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学号" prop="studentCode">
        <el-input
          v-model="queryParams.studentCode"
          placeholder="请输入学号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="部门" prop="deptId">
        <el-cascader v-model="queryParams.deptId" :options="deptTree"
          :props="{ checkStrictly:true, label:'label', value:'id' }"
          placeholder="请选择部门" clearable style="width:240px" @change="handleQuery" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-input
          v-model="queryParams.gender"
          placeholder="请输入性别"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="民族" prop="nation">
        <el-input
          v-model="queryParams.nation"
          placeholder="请输入民族"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="本人联系电话" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入本人联系电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="queryParams.idCard"
          placeholder="请输入身份证号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="家庭住址" prop="address">
        <el-input
          v-model="queryParams.address"
          placeholder="请输入家庭住址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="父亲姓名" prop="fatherName">
        <el-input
          v-model="queryParams.fatherName"
          placeholder="请输入父亲姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="父亲电话" prop="fatherPhone">
        <el-input
          v-model="queryParams.fatherPhone"
          placeholder="请输入父亲电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="母亲姓名" prop="motherName">
        <el-input
          v-model="queryParams.motherName"
          placeholder="请输入母亲姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="母亲电话" prop="motherPhone">
        <el-input
          v-model="queryParams.motherPhone"
          placeholder="请输入母亲电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="班长" prop="classMonitor">
        <el-input
          v-model="queryParams.classMonitor"
          placeholder="请输入班长"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="舍长" prop="dormLeader">
        <el-input
          v-model="queryParams.dormLeader"
          placeholder="请输入舍长"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="宿舍楼" prop="dormBuilding">
        <el-input
          v-model="queryParams.dormBuilding"
          placeholder="请输入宿舍楼"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="宿舍号" prop="dormRoom">
        <el-input
          v-model="queryParams.dormRoom"
          placeholder="请输入宿舍号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="贫困等级认定" prop="povertyLevel">
        <el-input
          v-model="queryParams.povertyLevel"
          placeholder="请输入贫困等级认定"
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
          v-hasPermi="['talk:student:add']"
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
          v-hasPermi="['talk:student:edit']"
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
          v-hasPermi="['talk:student:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['talk:student:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImportOpen"
          v-hasPermi="['talk:student:import']"
        >导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="talkList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生ID" align="center" prop="studentId" />
      <el-table-column label="学号" align="center" prop="studentCode" />
      <el-table-column label="姓名" align="center" prop="studentName" />
      <el-table-column label="部门ID(班级)" align="center" prop="deptId" />
      <el-table-column label="性别" align="center" prop="gender">
        <template slot-scope="scope">
          <span>{{ scope.row.gender === '1' ? '女' : '男' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="政治面貌" align="center" prop="politicalStatus" />
      <el-table-column label="民族" align="center" prop="nation" />
      <el-table-column label="本人联系电话" align="center" prop="phone">
        <template slot-scope="scope">{{ maskPhone(scope.row.phone) }}</template>
      </el-table-column>
      <el-table-column label="身份证号" align="center" prop="idCard">
        <template slot-scope="scope">{{ maskIdCard(scope.row.idCard) }}</template>
      </el-table-column>
      <el-table-column label="家庭住址" align="center" prop="address" />
      <el-table-column label="父亲姓名" align="center" prop="fatherName" />
      <el-table-column label="父亲电话" align="center" prop="fatherPhone" />
      <el-table-column label="母亲姓名" align="center" prop="motherName" />
      <el-table-column label="母亲电话" align="center" prop="motherPhone" />
      <el-table-column label="班长" align="center" prop="classMonitor" />
      <el-table-column label="舍长" align="center" prop="dormLeader" />
      <el-table-column label="宿舍楼" align="center" prop="dormBuilding" />
      <el-table-column label="宿舍号" align="center" prop="dormRoom" />
      <el-table-column label="学籍状态" align="center" prop="enrollmentStatus" />
      <el-table-column label="心理健康状态" align="center" prop="mentalHealthStatus" />
      <el-table-column label="贫困等级认定" align="center" prop="povertyLevel" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['talk:student:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['talk:student:remove']"
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

    <!-- 添加或修改学生信息管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学号" prop="studentCode">
              <el-input v-model="form.studentCode" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="部门ID(班级)" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入部门ID(班级)" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="性别" prop="gender">
              <el-input v-model="form.gender" placeholder="请输入性别" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="民族" prop="nation">
              <el-input v-model="form.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="本人联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入本人联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭住址" prop="address">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="父亲姓名" prop="fatherName">
              <el-input v-model="form.fatherName" placeholder="请输入父亲姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="父亲电话" prop="fatherPhone">
              <el-input v-model="form.fatherPhone" placeholder="请输入父亲电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="母亲姓名" prop="motherName">
              <el-input v-model="form.motherName" placeholder="请输入母亲姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="母亲电话" prop="motherPhone">
              <el-input v-model="form.motherPhone" placeholder="请输入母亲电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="班长" prop="classMonitor">
              <el-input v-model="form.classMonitor" placeholder="请输入班长" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="舍长" prop="dormLeader">
              <el-input v-model="form.dormLeader" placeholder="请输入舍长" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="宿舍楼" prop="dormBuilding">
              <el-input v-model="form.dormBuilding" placeholder="请输入宿舍楼" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="宿舍号" prop="dormRoom">
              <el-input v-model="form.dormRoom" placeholder="请输入宿舍号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="贫困等级认定" prop="povertyLevel">
              <el-input v-model="form.povertyLevel" placeholder="请输入贫困等级认定" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog :title="importTitle" :visible.sync="importOpen" width="800px" append-to-body>
      <div v-if="importStep === 1">
        <el-upload
          ref="upload"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :file-list="fileList"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">支持 .xlsx / .xls 格式，第1行为标题行将自动跳过</div>
        </el-upload>
        <div style="margin-top:12px">
          <span style="font-size:13px;color:#666;margin-right:8px">重复处理：</span>
          <el-radio-group v-model="importMode">
            <el-radio label="skip">跳过重复学号</el-radio>
            <el-radio label="update">覆盖已有数据</el-radio>
          </el-radio-group>
        </div>
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="primary" :loading="importLoading" @click="handleImportPreview" :disabled="!uploadFile">开始预览</el-button>
        </div>
      </div>
      <div v-else-if="importStep === 2">
        <el-alert
          :title="'共 ' + importResult.totalRows + ' 行数据，正确 ' + (importResult.totalRows - importResult.errorCount - importResult.warnCount) + ' 行，警告 ' + importResult.warnCount + ' 行，错误 ' + importResult.errorCount + ' 行'"
          :type="importResult.errorCount > 0 ? 'warning' : 'success'"
          :closable="false"
          show-icon
        />
        <div style="overflow-x: auto; margin-top: 10px;">
        <el-table :data="importResult.previewRows" max-height="400" border size="mini">
          <el-table-column label="行号" prop="rowNum" width="60" fixed="left" />
          <el-table-column label="学号" prop="data.student_code" width="130" />
          <el-table-column label="姓名" prop="data.student_name" width="80" />
          <el-table-column label="学院" prop="data.college" width="150" />
          <el-table-column label="年级" prop="data.grade" width="80" />
          <el-table-column label="班级" prop="data.class" width="120" />
          <el-table-column label="电话" prop="data.phone" width="120" />
          <el-table-column label="身份证号" prop="data.id_card" width="180" />
          <el-table-column label="家庭住址" prop="data.address" min-width="150" show-overflow-tooltip />
          <el-table-column label="状态" width="70" fixed="right">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.status === 'ok'" type="success">正常</el-tag>
              <el-tag v-else-if="scope.row.status === 'warn'" type="warning">警告</el-tag>
              <el-tag v-else type="danger">错误</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提示" prop="message" min-width="120" show-overflow-tooltip />
        </el-table>
        </div>
        <div slot="footer" class="dialog-footer" style="text-align: center; margin-top: 15px;">
          <el-button @click="importStep = 1">返回</el-button>
          <el-button type="primary" :loading="importLoading" @click="handleImportExecute" :disabled="importResult.errorCount >= importResult.totalRows">确认导入</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTalk, getTalk, delTalk, addTalk, updateTalk, importPreview, importExecute } from "@/api/talk/talkStudent"
import { listDept } from "@/api/system/dept"

export default {
  name: "Talk",
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
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 学生信息管理表格数据
      talkList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentCode: null,
        studentName: null,
        deptId: null,
        gender: null,
        politicalStatus: null,
        nation: null,
        phone: null,
        idCard: null,
        address: null,
        fatherName: null,
        fatherPhone: null,
        motherName: null,
        motherPhone: null,
        classMonitor: null,
        dormLeader: null,
        dormBuilding: null,
        dormRoom: null,
        enrollmentStatus: null,
        mentalHealthStatus: null,
        povertyLevel: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        studentCode: [
          { required: true, message: "学号不能为空", trigger: "blur" }
        ],
        studentName: [
          { required: true, message: "姓名不能为空", trigger: "blur" }
        ],
        deptId: [
          { required: true, message: "部门ID(班级)不能为空", trigger: "blur" }
        ],
      },
      // 导入相关
      importOpen: false,
      importTitle: "导入学生数据",
      importStep: 1,
      importLoading: false,
      uploadFile: null,
      fileList: [],
      importResult: { totalRows: 0, errorCount: 0, warnCount: 0, previewRows: [] },
      importMode: 'skip',
      deptTree: []
    }
  },
  created() {
    this.getList()
    this.loadDeptTree()
  },
  methods: {
    loadDeptTree() {
      listDept().then(res => {
        this.deptTree = this.buildTree(res.data || [])
      }).catch(() => { this.$modal.msgError('操作失败') })
    },
    buildTree(list) {
      const map = {}, tree = []
      list.forEach(d => { map[d.deptId] = { id:d.deptId, label:d.deptName, children:[], deptType:d.deptType, parentId:d.parentId } })
      list.forEach(d => {
        const node = map[d.deptId]
        if (d.parentId && map[d.parentId]) { map[d.parentId].children.push(node) }
        else if (!d.parentId || d.parentId === 0 || d.parentId === 100) { tree.push(node) }
      })
      return tree
    },
    /** 查询学生信息管理列表 */
    maskPhone(phone) {
      if (!phone || phone.length < 7) return phone || '-'
      return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4)
    },
    maskIdCard(idCard) {
      if (!idCard || idCard.length < 8) return idCard || '-'
      return idCard.substring(0, 3) + '***********' + idCard.substring(idCard.length - 4)
    },
    getList() {
      this.loading = true
      listTalk(this.queryParams).then(response => {
        this.talkList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        studentId: null,
        studentCode: null,
        studentName: null,
        deptId: null,
        gender: null,
        politicalStatus: null,
        nation: null,
        phone: null,
        idCard: null,
        address: null,
        fatherName: null,
        fatherPhone: null,
        motherName: null,
        motherPhone: null,
        classMonitor: null,
        dormLeader: null,
        dormBuilding: null,
        dormRoom: null,
        enrollmentStatus: null,
        mentalHealthStatus: null,
        povertyLevel: null,
        remark: null,
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
      this.ids = selection.map(item => item.studentId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加学生信息管理"
    },
    /** 查看详情 */
    handleDetail(row) {
      this.$router.push({ path: '/talk/student-detail/' + row.studentId })
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const studentId = row.studentId || this.ids
      getTalk(studentId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = "修改学生信息管理"
      }).catch(() => { this.$modal.msgError('获取学生详情失败') })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.studentId != null) {
            updateTalk(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            }).catch(() => { this.$modal.msgError('修改失败') })
          } else {
            addTalk(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(() => { this.$modal.msgError('新增失败') })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const studentIds = row.studentId || this.ids
      this.$modal.confirm('是否确认删除学生信息管理编号为"' + studentIds + '"的数据项？').then(function() {
        return delTalk(studentIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { this.$modal.msgError('删除失败') })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('ruoyi-system/talk/export', {
        ...this.queryParams
      }, `talk_${new Date().getTime()}.xlsx`)
    },
    /** 打开导入对话框 */
    handleImportOpen() {
      this.importStep = 1
      this.importOpen = true
      this.uploadFile = null
      this.fileList = []
    },
    /** 文件选择变化 */
    handleFileChange(file) {
      this.uploadFile = file.raw
      this.fileList = [file]
    },
    /** 导入预览 */
    handleImportPreview() {
      if (!this.uploadFile) {
        this.$modal.msgError("请先选择文件")
        return
      }
      this.importLoading = true
      const formData = new FormData()
      formData.append('file', this.uploadFile)
      importPreview(formData).then(response => {
        this.importResult = response.data
        this.importStep = 2
        this.importLoading = false
      }).catch(() => {
        this.importLoading = false
      })
    },
    /** 执行导入 */
    handleImportExecute() {
      this.importLoading = true
      const importData = this.importResult.previewRows.map(row => row.data)
      importExecute(importData, this.importMode).then(response => {
        this.$modal.msgSuccess("导入成功，共导入 " + response.data.successCount + " 条记录")
        this.importOpen = false
        this.importLoading = false
        this.getList()
      }).catch(() => {
        this.importLoading = false
      })
    }
  }
}
</script>
