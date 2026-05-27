<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px" style="max-width: 800px">
      <el-card class="mb20">
        <div slot="header">
          <span>谈话基本信息</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="谈话时间" prop="conversationTime">
              <el-date-picker v-model="form.conversationTime" type="datetime" placeholder="选择谈话时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话地点" prop="conversationPlace">
              <el-input v-model="form.conversationPlace" placeholder="请输入谈话地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="谈话人" prop="speaker">
              <el-input v-model="form.speaker" placeholder="请输入谈话人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话主题" prop="topic">
              <el-input v-model="form.topic" placeholder="请输入谈话主题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="谈话内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入谈话内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="后续跟进事项" prop="followUpItems">
              <el-input v-model="form.followUpItems" type="textarea" :rows="3" placeholder="请输入后续跟进事项" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card>
        <div slot="header">
          <span>已选学生</span>
          <el-button type="text" icon="el-icon-plus" style="float: right" @click="selectStudentOpen">选择学生</el-button>
        </div>
        <el-table :data="selectedStudents" border stripe>
          <el-table-column label="学号" prop="studentNo" width="120" />
          <el-table-column label="姓名" prop="studentName" width="100" />
          <el-table-column label="学院" prop="collegeName" />
          <el-table-column label="专业" prop="majorName" />
          <el-table-column label="班级" prop="className" />
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C" @click="removeStudent(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="selectedStudents.length === 0" style="text-align: center; padding: 40px 0; color: #909399">
          暂未选择学生，请点击"选择学生"按钮添加
        </div>
      </el-card>

      <div style="text-align: center; margin-top: 20px">
        <el-button type="primary" icon="el-icon-s-promotion" size="medium" @click="submitForm">提 交</el-button>
        <el-button icon="el-icon-back" size="medium" @click="goBack">返 回</el-button>
      </div>
    </el-form>

    <el-dialog title="选择学生" :visible.sync="studentDialogVisible" width="1000px" append-to-body @opened="loadStudentList">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="tree-title">学院/专业/班级</div>
          <el-input v-model="treeFilter" placeholder="输入关键字过滤" size="small" clearable style="margin-bottom: 10px" />
          <el-tree ref="tree" :data="treeData" :props="treeProps" :filter-node-method="filterNode" node-key="id" highlight-current @node-click="handleTreeClick" />
        </el-col>
        <el-col :span="18">
          <el-form :model="studentQuery" :inline="true" size="small">
            <el-form-item label="学生姓名">
              <el-input v-model="studentQuery.studentName" placeholder="请输入姓名" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model="studentQuery.studentNo" placeholder="请输入学号" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="loadStudentList">搜索</el-button>
            </el-form-item>
          </el-form>
          <el-table ref="studentTable" :data="studentTableData" @selection-change="handleStudentSelect" max-height="400" stripe>
            <el-table-column type="selection" width="50" />
            <el-table-column label="学号" prop="studentNo" width="120" />
            <el-table-column label="姓名" prop="studentName" width="100" />
            <el-table-column label="学院" prop="collegeName" />
            <el-table-column label="专业" prop="majorName" />
            <el-table-column label="班级" prop="className" />
          </el-table>
          <pagination v-show="studentTotal > 0" :total="studentTotal" :page.sync="studentQuery.pageNum" :limit.sync="studentQuery.pageSize" @pagination="loadStudentList" />
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmStudentSelection">确定选择</el-button>
        <el-button @click="studentDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addRecord } from "@/api/conversation/record"
import { listStudent } from "@/api/conversation/student"
import { deptTree, optionSelect as majorOptionSelect } from "@/api/conversation/major"
import { optionSelect as classOptionSelect } from "@/api/conversation/class"
import { getCurrentInstance } from 'vue'

export default {
  name: "RecordAdd",
  data() {
    return {
      form: {
        conversationTime: '',
        conversationPlace: '',
        speaker: '',
        topic: '',
        content: '',
        followUpItems: ''
      },
      rules: {
        conversationTime: [
          { required: true, message: "谈话时间不能为空", trigger: "blur" }
        ],
        conversationPlace: [
          { required: true, message: "谈话地点不能为空", trigger: "blur" }
        ],
        speaker: [
          { required: true, message: "谈话人不能为空", trigger: "blur" }
        ],
        topic: [
          { required: true, message: "谈话主题不能为空", trigger: "blur" }
        ],
        content: [
          { required: true, message: "谈话内容不能为空", trigger: "blur" }
        ]
      },
      selectedStudents: [],
      studentDialogVisible: false,
      treeFilter: '',
      treeData: [],
      treeProps: {
        children: 'children',
        label: 'label'
      },
      studentQuery: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        studentNo: undefined,
        deptId: undefined,
        majorId: undefined,
        classId: undefined
      },
      studentTableData: [],
      studentTotal: 0,
      tempSelectedStudents: []
    }
  },
  created() {
    this.loadTreeData()
    const instance = getCurrentInstance()
    if (instance && instance.proxy) {
      this.form.speaker = instance.proxy.$store.state.user.name || ''
    }
  },
  methods: {
    loadTreeData() {
      deptTree().then(response => {
        this.treeData = response.data || []
        this.expandTreeData(this.treeData)
      })
    },
    expandTreeData(tree, deptId) {
      tree.forEach(node => {
        if (deptId) {
          node.deptId = deptId
        }
        if (!deptId) {
          majorOptionSelect({ deptId: node.id }).then(res => {
            let children = (res.data || []).map(m => ({
              id: 'major_' + m.majorId,
              label: m.majorName,
              majorId: m.majorId,
              children: []
            }))
            children.forEach(major => {
              classOptionSelect({ majorId: major.majorId }).then(clRes => {
                major.children = (clRes.data || []).map(c => ({
                  id: 'class_' + c.classId,
                  label: c.className,
                  classId: c.classId
                }))
              })
            })
            node.children = children
          })
        }
      })
    },
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    handleTreeClick(data) {
      this.studentQuery.pageNum = 1
      this.studentQuery.deptId = undefined
      this.studentQuery.majorId = undefined
      this.studentQuery.classId = undefined
      if (data.deptId) {
        this.studentQuery.deptId = data.deptId
      } else if (data.majorId) {
        this.studentQuery.majorId = data.majorId
      } else if (data.classId) {
        this.studentQuery.classId = data.classId
      }
      this.loadStudentList()
    },
    selectStudentOpen() {
      this.tempSelectedStudents = JSON.parse(JSON.stringify(this.selectedStudents))
      this.studentDialogVisible = true
    },
    loadStudentList() {
      listStudent(this.studentQuery).then(response => {
        this.studentTableData = response.rows || []
        this.studentTotal = response.total || 0
        this.$nextTick(() => {
          if (this.tempSelectedStudents.length > 0) {
            const selectedIds = this.tempSelectedStudents.map(s => s.studentId)
            this.studentTableData.forEach(row => {
              if (selectedIds.includes(row.studentId)) {
                this.$refs.studentTable.toggleRowSelection(row, true)
              }
            })
          }
        })
      })
    },
    handleStudentSelect(selection) {
      this.tempSelectedStudents = selection
    },
    confirmStudentSelection() {
      this.selectedStudents = JSON.parse(JSON.stringify(this.tempSelectedStudents))
      this.studentDialogVisible = false
    },
    removeStudent(index) {
      this.selectedStudents.splice(index, 1)
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.selectedStudents.length === 0) {
            this.$modal.msgWarning("请选择至少一名学生")
            return
          }
          const data = {
            ...this.form,
            studentIds: this.selectedStudents.map(s => s.studentId).join(',')
          }
          addRecord(data).then(() => {
            this.$modal.msgSuccess("新增成功")
            this.$router.push({ path: '/conversation/record/list' })
          })
        }
      })
    },
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
.tree-title {
  font-weight: bold;
  margin-bottom: 10px;
  padding: 5px 0;
  border-bottom: 1px solid #EBEEF5;
}
</style>