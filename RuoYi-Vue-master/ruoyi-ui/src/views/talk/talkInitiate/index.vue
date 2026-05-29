<template>
  <div class="app-container">
    <div class="ti-header">
      <h3>发起谈话</h3>
      <el-steps :active="step" align-center finish-status="success">
        <el-step title="选择类型" description="个别/集体" />
        <el-step title="选择学生" :description="form.talkType === 'group' ? '支持多选' : '单选'" />
        <el-step title="填写内容" description="谈话详情" />
      </el-steps>
    </div>

    <div class="ti-body">
      <!-- Step 1: 选择谈话类型 -->
      <div v-if="step === 0" class="ti-step-content">
        <div class="ti-type-cards">
          <div
            class="ti-type-card"
            :class="{ active: form.talkType === 'individual' }"
            @click="form.talkType = 'individual'"
          >
            <i class="el-icon-user-solid ti-type-icon" />
            <div class="ti-type-title">个别谈话</div>
            <div class="ti-type-desc">与单个学生一对一谈话<br>选择 1 名学生</div>
          </div>
          <div
            class="ti-type-card"
            :class="{ active: form.talkType === 'group' }"
            @click="form.talkType = 'group'"
          >
            <i class="el-icon-s-cooperation ti-type-icon" />
            <div class="ti-type-title">集体谈话</div>
            <div class="ti-type-desc">与多名学生同时谈话<br>共享内容，分别反馈</div>
          </div>
        </div>
        <div class="ti-step-actions">
          <el-button type="primary" @click="step = 1">下一步</el-button>
        </div>
      </div>

      <!-- Step 2: 选择学生 -->
      <div v-if="step === 1" class="ti-step-content ti-step-select">
        <div class="ti-transfer">
          <div class="ti-transfer-panel">
            <div class="ti-panel-title">
              可选学生 ({{ availableStudents.length }})
              <el-button type="text" size="mini" @click="loadStudents" :loading="studentLoading">
                <i class="el-icon-refresh" />
              </el-button>
            </div>
            <el-input v-model="searchKey" placeholder="搜索学号/姓名" size="small" clearable class="ti-transfer-search" />
            <div class="ti-transfer-list" v-loading="studentLoading">
              <div
                v-for="stu in filteredAvailable"
                :key="stu.studentId"
                class="ti-transfer-item"
                @click="selectStudent(stu)"
              >
                <span>{{ stu.studentName }}</span>
                <span class="ti-transfer-code">{{ stu.studentCode }}</span>
              </div>
              <div v-if="filteredAvailable.length === 0 && !studentLoading" class="ti-empty">无匹配学生</div>
            </div>
          </div>
          <div class="ti-transfer-arrows">
            <el-button icon="el-icon-arrow-right" circle size="small"
                       @click="selectAllAvailable" :disabled="filteredAvailable.length === 0" />
            <el-button icon="el-icon-arrow-left" circle size="small"
                       @click="removeAllSelected" :disabled="selectedStudents.length === 0" />
          </div>
          <div class="ti-transfer-panel">
            <div class="ti-panel-title">已选学生 ({{ selectedStudents.length }})</div>
            <div class="ti-transfer-list">
              <div
                v-for="stu in selectedStudents"
                :key="stu.studentId"
                class="ti-transfer-item"
                @click="removeStudent(stu)"
              >
                <el-tag size="mini" type="success" closable @close="removeStudent(stu)">
                  {{ stu.studentName }}
                </el-tag>
                <span class="ti-transfer-code">{{ stu.studentCode }}</span>
              </div>
              <div v-if="selectedStudents.length === 0" class="ti-empty">
                {{ form.talkType === 'group' ? '请从左边选择参与学生' : '请选择1名学生' }}
              </div>
            </div>
          </div>
        </div>
        <div class="ti-step-actions">
          <el-button @click="step = 0">上一步</el-button>
          <el-button
            type="primary"
            @click="step = 2"
            :disabled="form.talkType === 'group' ? selectedStudents.length === 0 : selectedStudents.length !== 1"
          >
            下一步
          </el-button>
        </div>
      </div>

      <!-- Step 3: 填写内容 -->
      <div v-if="step === 2" class="ti-step-content ti-step-form">
        <el-form :model="form" :rules="rules" ref="talkForm" label-width="90px" size="small">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="谈话时间" prop="talkTime">
                <el-date-picker v-model="form.talkTime" type="date" value-format="yyyy-MM-dd"
                                placeholder="选择日期" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="谈话地点" prop="talkLocation">
                <el-input v-model="form.talkLocation" placeholder="如：学院会议室301" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="谈话人" prop="talkPerson">
                <el-input v-model="form.talkPerson" placeholder="默认当前登录用户" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="谈话内容" prop="talkContent">
            <div class="ti-content-area">
              <div class="ti-content-toolbar">
                <el-button type="primary" size="mini" icon="el-icon-document-copy" @click="openTemplateDialog">
                  从模板库选择
                </el-button>
                <el-button type="warning" size="mini" icon="el-icon-delete" @click="clearContent"
                           :disabled="!form.talkContent">
                  清空内容
                </el-button>
              </div>
              <el-input v-model="form.talkContent" type="textarea" :rows="6"
                        :placeholder="form.talkType === 'group' ? '此内容将共享给所有参与学生。可点击上方「从模板库选择」快速填充内容' : '输入谈话内容，可点击上方「从模板库选择」快速填充'" />
            </div>
          </el-form-item>
          <el-form-item label="内容标签">
            <el-select v-model="form.tags" multiple placeholder="请选择谈话主题标签" style="width:100%">
              <el-option v-for="t in tagOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
        </el-form>

        <el-divider v-if="selectedStudents.length > 0">学生反馈与跟进计划（选填）</el-divider>
        <el-collapse v-if="selectedStudents.length > 0" v-model="activeCollapse">
          <el-collapse-item
            v-for="(stu, idx) in selectedStudents"
            :key="stu.studentId"
            :name="idx"
          >
            <template slot="title">
              <span class="ti-collapse-title">{{ stu.studentName }} ({{ stu.studentCode }})</span>
              <el-tag v-if="stu._feedback || stu._plan" size="mini" type="success">已填写</el-tag>
            </template>
            <el-row :gutter="12">
              <el-col :span="12">
                <div class="ti-field-label">学生反馈</div>
                <el-input v-model="stu._feedback" type="textarea" :rows="2" placeholder="学生反馈" />
              </el-col>
              <el-col :span="12">
                <div class="ti-field-label">跟进计划</div>
                <el-input v-model="stu._plan" type="textarea" :rows="2" placeholder="跟进计划" />
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>

        <div class="ti-step-actions">
          <el-button @click="step = 1">上一步</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存谈话</el-button>
        </div>
      </div>

      <el-dialog title="选择谈话模板" :visible.sync="templateDialogVisible" width="700px" append-to-body>
        <el-tabs v-model="templateTab">
          <el-tab-pane label="系统预置模板" name="system">
            <div class="ti-template-grid">
              <div v-for="tpl in systemTemplates" :key="tpl.templateId" class="ti-template-card"
                   :class="{ selected: selectedTemplateId === tpl.templateId }"
                   @click="selectedTemplateId = tpl.templateId">
                <div class="ti-tpl-name">{{ tpl.templateName }}</div>
                <div class="ti-tpl-preview">{{ tpl.templateContent.substring(0, 80) }}...</div>
                <div class="ti-tpl-tags">
                  <el-tag v-for="tag in (tpl.templateTags||'').split(',').filter(Boolean)" :key="tag" size="mini" type="info">{{ getTagLabel(tag) }}</el-tag>
                </div>
              </div>
              <div v-if="systemTemplates.length === 0" class="ti-empty">暂无系统模板</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="我的模板" name="personal">
            <div class="ti-template-grid">
              <div v-for="tpl in personalTemplates" :key="tpl.templateId" class="ti-template-card"
                   :class="{ selected: selectedTemplateId === tpl.templateId }"
                   @click="selectedTemplateId = tpl.templateId">
                <div class="ti-tpl-name">{{ tpl.templateName }}</div>
                <div class="ti-tpl-preview">{{ tpl.templateContent.substring(0, 80) }}...</div>
              </div>
              <div v-if="personalTemplates.length === 0" class="ti-empty">暂无个人模板，请在「模板库」页面创建</div>
            </div>
          </el-tab-pane>
        </el-tabs>
        <span slot="footer" class="dialog-footer">
          <el-button @click="insertTemplate('replace')" type="primary" :disabled="!selectedTemplateId">替换当前内容</el-button>
          <el-button @click="insertTemplate('append')" type="success" :disabled="!selectedTemplateId">追加到末尾</el-button>
          <el-button @click="templateDialogVisible = false">取消</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { listTalk } from '@/api/talk/talkStudent'
import { createTalk } from '@/api/talk/talkInitiate'
import { listSystemTemplate, listTemplate } from '@/api/talk/talkTemplate'

export default {
  name: 'TalkInitiate',
  data() {
    return {
      step: 0,
      searchKey: '',
      studentLoading: false,
      submitting: false,
      templateDialogVisible: false,
      templateTab: 'system',
      selectedTemplateId: null,
      systemTemplates: [],
      personalTemplates: [],
      activeCollapse: [0],
      allStudents: [],
      selectedStudents: [],
      form: {
        talkType: 'group',
        talkTime: '',
        talkLocation: '',
        talkPerson: '',
        talkContent: '',
        tags: []
      },
      tagOptions: [
        { value: 'thought_education', label: '思想理论教育和价值引领' },
        { value: 'party_class', label: '党团和班级建设' },
        { value: 'study_style', label: '学风建设' },
        { value: 'daily_affairs', label: '日常事务' },
        { value: 'mental_health', label: '心理健康教育与咨询' },
        { value: 'crisis_response', label: '危机事件应对' },
        { value: 'career_guidance', label: '职业规划与就业创业指导' }
      ],
      rules: {
        talkTime: [{ required: true, message: '请选择谈话时间', trigger: 'change' }],
        talkLocation: [{ required: true, message: '请输入谈话地点', trigger: 'blur' }],
        talkPerson: [{ required: true, message: '请输入谈话人', trigger: 'blur' }],
        talkContent: [{ required: true, message: '请输入谈话内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    selectedIds() { return new Set(this.selectedStudents.map(s => s.studentId)) },
    availableStudents() {
      return this.allStudents.filter(s => !this.selectedIds.has(s.studentId))
    },
    filteredAvailable() {
      const k = this.searchKey.toLowerCase()
      if (!k) return this.availableStudents
      return this.availableStudents.filter(s =>
        s.studentCode.includes(k) || s.studentName.toLowerCase().includes(k)
      )
    }
  },
  created() {
    this.loadStudents()
  },
  methods: {
    loadStudents() {
      this.studentLoading = true
      listTalk({ pageNum: 1, pageSize: 9999 }).then(res => {
        this.allStudents = res.rows || []
      }).finally(() => {
        this.studentLoading = false
      })
    },
    selectStudent(stu) {
      if (this.form.talkType === 'individual') {
        this.selectedStudents = [{ ...stu, _feedback: '', _plan: '' }]
      } else {
        if (!this.selectedIds.has(stu.studentId)) {
          this.selectedStudents.push({ ...stu, _feedback: '', _plan: '' })
        }
      }
    },
    removeStudent(stu) {
      this.selectedStudents = this.selectedStudents.filter(s => s.studentId !== stu.studentId)
    },
    selectAllAvailable() {
      this.filteredAvailable.forEach(s => {
        if (!this.selectedIds.has(s.studentId)) {
          this.selectedStudents.push({ ...s, _feedback: '', _plan: '' })
        }
      })
    },
    removeAllSelected() {
      this.selectedStudents = []
    },
    openTemplateDialog() {
      this.selectedTemplateId = null
      this.templateTab = 'system'
      this.templateDialogVisible = true
      this.loadTemplates()
    },
    loadTemplates() {
      listSystemTemplate().then(res => { this.systemTemplates = res.rows || res.data || [] })
      listTemplate({ templateType: 'personal' }).then(res => { this.personalTemplates = res.rows || [] })
    },
    clearContent() {
      this.$confirm('确认清空谈话内容？', '提示', { type: 'warning' }).then(() => {
        this.form.talkContent = ''
        this.$message.success('已清空')
      }).catch(() => {})
    },
    insertTemplate(mode) {
      if (!this.selectedTemplateId) return
      const all = [...this.systemTemplates, ...this.personalTemplates]
      const tpl = all.find(t => t.templateId === this.selectedTemplateId)
      if (!tpl) return
      
      if (mode === 'replace') {
        if (this.form.talkContent) {
          this.$confirm('当前内容将被替换，是否继续？', '提示', { type: 'warning' }).then(() => {
            this.doInsert(tpl)
          }).catch(() => {})
        } else {
          this.doInsert(tpl)
        }
      } else {
        this.doInsert(tpl, true)
      }
    },
    doInsert(tpl, append) {
      if (append) {
        this.form.talkContent = (this.form.talkContent ? this.form.talkContent + '\n\n' : '') + tpl.templateContent
      } else {
        this.form.talkContent = tpl.templateContent
      }
      if (tpl.templateTags) {
        const tags = tpl.templateTags.split(',').map(t => t.trim()).filter(Boolean)
        const existing = this.form.tags || []
        tags.forEach(t => { if (!existing.includes(t)) existing.push(t) })
        this.form.tags = [...existing]
      }
      this.templateDialogVisible = false
      this.$message.success(append ? '模板内容已追加' : '模板内容已替换')
    },
    getTagLabel(value) {
      const map = {
        thought_education: '思想理论教育', party_class: '党团班级建设', study_style: '学风建设',
        daily_affairs: '日常事务', mental_health: '心理健康', crisis_response: '危机应对',
        career_guidance: '职业规划就业'
      }
      return map[value] || value
    },
    submitForm() {
      this.$refs.talkForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        const data = {
          talkType: this.form.talkType,
          talkTime: this.form.talkTime,
          talkLocation: this.form.talkLocation,
          talkPerson: this.form.talkPerson,
          talkContent: this.form.talkContent,
          tags: this.form.tags || [],
          studentIds: this.selectedStudents.map(s => s.studentId),
          studentDataList: this.selectedStudents.map(s => ({
            studentId: s.studentId,
            studentFeedback: s._feedback || '',
            followupPlan: s._plan || '',
            followupStatus: 'pending'
          }))
        }
        createTalk(data).then(res => {
          this.$modal.msgSuccess('谈话记录保存成功')
          this.selectedStudents = []
          this.step = 0
          this.form = { talkType: 'group', talkTime: '', talkLocation: '', talkPerson: '', talkContent: '', tags: [] }
          this.searchKey = ''
          this.activeCollapse = [0]
          this.$refs.talkForm.resetFields()
        }).catch(err => {
          this.$modal.msgError(err + '')
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style scoped>
.ti-header { padding-bottom: 12px; border-bottom: 1px solid #eee; margin-bottom: 20px; }
.ti-header h3 { margin: 0 0 16px; font-size: 16px; }
.ti-body { flex: 1; min-height: 0; }
.ti-step-content { max-width: 900px; margin: 0 auto; }

.ti-type-cards { display: flex; gap: 24px; justify-content: center; margin: 40px 0; }
.ti-type-card {
  width: 220px; padding: 32px 24px; border: 2px solid #e8e8e8; border-radius: 8px;
  text-align: center; cursor: pointer; transition: all .2s;
}
.ti-type-card:hover { border-color: #1890ff; }
.ti-type-card.active { border-color: #1890ff; background: #e6f7ff; }
.ti-type-icon { font-size: 48px; color: #1890ff; margin-bottom: 12px; }
.ti-type-title { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.ti-type-desc { font-size: 13px; color: #999; line-height: 1.6; }

.ti-transfer { display: flex; gap: 12px; align-items: stretch; }
.ti-transfer-panel {
  flex: 1; border: 1px solid #e8e8e8; border-radius: 4px;
  display: flex; flex-direction: column; min-height: 360px;
}
.ti-panel-title {
  padding: 10px 12px; font-weight: 600; font-size: 13px;
  border-bottom: 1px solid #eee; background: #fafafa;
  display: flex; justify-content: space-between; align-items: center;
}
.ti-transfer-search { margin: 8px; width: auto; }
.ti-transfer-list { flex: 1; overflow-y: auto; padding: 4px 8px; }
.ti-transfer-item {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 8px; cursor: pointer; border-radius: 4px;
}
.ti-transfer-item:hover { background: #f0f0f0; }
.ti-transfer-code { color: #999; font-size: 12px; margin-left: auto; }
.ti-transfer-arrows {
  display: flex; flex-direction: column; justify-content: center; gap: 8px;
}
.ti-empty { text-align: center; color: #ccc; padding: 20px; font-size: 13px; }

.ti-step-form { padding-top: 8px; }
.ti-field-label { font-size: 12px; color: #999; margin-bottom: 4px; }
.ti-collapse-title { font-weight: 600; }
.ti-step-actions {
  margin-top: 20px; padding-top: 12px;
  border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 8px;
}
.ti-content-area { width: 100%; }
.ti-content-toolbar {
  display: flex; gap: 6px; margin-bottom: 6px;
  padding: 6px 10px; background: #f5f7fa; border-radius: 6px 6px 0 0;
}
.ti-template-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-height: 380px; overflow-y: auto;
}
.ti-template-card {
  padding: 14px; border: 2px solid #e8e8e8; border-radius: 10px;
  cursor: pointer; transition: all .2s;
}
.ti-template-card:hover { border-color: #409eff; background: #ecf5ff; }
.ti-template-card.selected { border-color: #409eff; background: #e6f7ff; box-shadow: 0 0 0 2px rgba(64,158,255,.2); }
.ti-tpl-name { font-weight: 600; font-size: 14px; margin-bottom: 6px; }
.ti-tpl-preview { font-size: 12px; color: #999; margin-bottom: 8px; line-height: 1.5; }
.ti-tpl-tags { display: flex; gap: 4px; flex-wrap: wrap; }
</style>
