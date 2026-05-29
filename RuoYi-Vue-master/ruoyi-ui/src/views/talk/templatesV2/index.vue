<!--
  ================================================================================
  THROWAWAY PROTOTYPE — templatesV2/index.vue
  学生谈心谈话管理系统 · 模板库 V2 — 模板↔谈话内容联动
  目的：展示模板库与发起谈话流程的联动交互
  所有数据均为模拟数据，无API调用
  ================================================================================
-->
<template>
  <div class="tplv2-root">
    <div class="tplv2-header">
      <div class="tplv2-header-left">
        <div class="tplv2-logo-icon">📋</div>
        <div>
          <div class="tplv2-title">模板库 V2</div>
          <div class="tplv2-subtitle">模板 ↔ 谈话内容联动演示</div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="tplv2-tabs">
      <el-tab-pane label="系统预置模板" name="system">
        <div class="tplv2-template-grid">
          <div class="tplv2-card" v-for="(tmpl, idx) in systemTemplates" :key="'st'+idx">
            <div class="tplv2-card-body">
              <div class="tplv2-card-icon" :style="{ background: iconGradients[idx] }">
                {{ tmpl.emoji || '📋' }}
              </div>
              <div class="tplv2-card-title">{{ tmpl.title || tmpl.templateName }}</div>
              <div class="tplv2-card-desc">{{ tmpl.description || (tmpl.templateContent || '').substring(0, 60) }}</div>
              <div class="tplv2-card-tags">
                <el-tag
                  v-for="tag in (tmpl.tags || [])"
                  :key="tag"
                  size="mini"
                  :type="tagTypes[tag] || ''"
                  effect="plain"
                >{{ tag }}</el-tag>
              </div>
            </div>
            <div class="tplv2-card-footer">
              <el-button type="primary" size="small" @click="useTemplate(tmpl)">
                <i class="el-icon-document-copy"></i> 使用此模板
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的模板" name="my">
        <div class="tplv2-my-layout">
          <div class="tplv2-my-create">
            <div class="tplv2-section-title">
              <i class="el-icon-plus"></i> 创建个人模板
            </div>
            <div class="tplv2-create-form">
              <el-input
                v-model="newTemplateName"
                placeholder="模板名称"
                size="small"
                class="tplv2-create-input"
              />
              <el-input
                v-model="newTemplateContent"
                type="textarea"
                :rows="3"
                placeholder="模板内容..."
                size="small"
                class="tplv2-create-textarea"
              />
              <el-button
                type="primary"
                size="small"
                @click="saveMyTemplate"
                :disabled="!newTemplateName || !newTemplateContent"
              >保存模板</el-button>
            </div>
          </div>

          <div class="tplv2-my-list">
            <div class="tplv2-section-title">
              <i class="el-icon-collection-tag"></i> 已保存 ({{ myTemplates.length }})
            </div>
            <div class="tplv2-card" v-for="(tmpl, idx) in myTemplates" :key="'mt'+idx">
              <div class="tplv2-card-body">
                <div class="tplv2-card-title">{{ tmpl.title || tmpl.templateName }}</div>
                <div class="tplv2-card-desc">{{ tmpl.description || (tmpl.templateContent || '').substring(0, 60) }}</div>
                <div class="tplv2-card-tags">
                  <el-tag size="mini" type="success" effect="plain">个人模板</el-tag>
                </div>
              </div>
              <div class="tplv2-card-footer">
                <el-button type="primary" size="small" @click="useTemplate(tmpl)">
                  <i class="el-icon-document-copy"></i> 使用此模板
                </el-button>
                <el-button type="danger" size="small" icon="el-icon-delete" @click="deleteMyTemplate(idx)"></el-button>
              </div>
            </div>

            <div v-if="myTemplates.length === 0" class="tplv2-empty">
              <i class="el-icon-document"></i>
              <span>暂无个人模板，请在左侧创建</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Slide-in Panel: Template ↔ Talk Content Linkage -->
    <transition name="panel-slide">
      <div v-if="linkedTemplate" class="tplv2-linked-panel">
        <div class="tplv2-linked-header">
          <div class="tplv2-linked-header-left">
            <span class="tplv2-linked-dot"></span>
            <span class="tplv2-linked-title">{{ linkedTemplate.title }}</span>
            <el-tag size="mini" type="info" effect="plain" v-if="linkedTemplate.source">
              {{ linkedTemplate.source }}
            </el-tag>
          </div>
          <el-button type="text" size="mini" icon="el-icon-close" @click="closeLinkedPanel" class="tplv2-linked-close"></el-button>
        </div>

        <div class="tplv2-linked-body">
          <div class="tplv2-linked-content-area">
            <div class="tplv2-linked-label">
              <i class="el-icon-edit"></i> 编辑谈话内容
            </div>
            <el-input
              v-model="linkedTemplate.editableContent"
              type="textarea"
              :rows="5"
              placeholder="在此编辑谈话内容..."
              class="tplv2-linked-textarea"
            />
          </div>

          <div class="tplv2-linked-actions">
            <el-button type="info" size="small" icon="el-icon-document-copy" @click="copyContent">
              复制内容
            </el-button>
            <el-button type="primary" size="small" icon="el-icon-s-promotion" @click="openTalkForm">
              发起谈话
            </el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Dialog: 发起谈话 -->
    <el-dialog
      title="发起谈话"
      :visible.sync="talkDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      class="tplv2-dialog"
    >
      <el-form :model="talkForm" :rules="talkRules" ref="talkFormRef" label-width="90px" size="small">
        <el-form-item label="选择学生" prop="studentId">
          <el-select v-model="talkForm.studentId" placeholder="请选择学生" style="width:100%" filterable>
            <el-option
              v-for="stu in mockStudents"
              :key="stu.value"
              :label="stu.label"
              :value="stu.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="谈话时间" prop="talkTime">
          <el-date-picker
            v-model="talkForm.talkTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择谈话时间"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="谈话地点" prop="talkLocation">
          <el-input v-model="talkForm.talkLocation" placeholder="如：学院会议室301" />
        </el-form-item>
        <el-form-item label="谈话人">
          <el-input v-model="talkForm.talkPerson" placeholder="默认当前用户" />
        </el-form-item>
      </el-form>

      <div class="tplv2-dialog-preview" v-if="linkedTemplate">
        <div class="tplv2-dialog-preview-title">
          <i class="el-icon-info"></i> 谈话内容预览
        </div>
        <div class="tplv2-dialog-preview-content">{{ linkedTemplate.editableContent }}</div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="talkDialogVisible = false" size="small">取 消</el-button>
        <el-button type="primary" @click="submitTalk" :loading="talkSubmitting" size="small">
          确认发起
        </el-button>
      </div>
    </el-dialog>

    <!-- Success Toast Simulation Area -->
    <transition name="toast-fade">
      <div v-if="toastMessage" class="tplv2-toast">
        <i class="el-icon-success"></i> {{ toastMessage }}
      </div>
    </transition>
  </div>
</template>

<script>
import { listSystemTemplate, listTemplate, addTemplate, updateTemplate, delTemplate } from '@/api/talk/talkTemplate'

export default {
  name: 'TemplatesV2',
  data() {
    return {
      activeTab: 'system',
      linkedTemplate: null,
      talkDialogVisible: false,
      talkSubmitting: false,
      toastMessage: '',
      toastTimer: null,
      newTemplateName: '',
      newTemplateContent: '',
      talkForm: {
        studentId: '',
        talkTime: '',
        talkLocation: '',
        talkPerson: '张老师'
      },
      talkRules: {
        studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
        talkTime: [{ required: true, message: '请选择谈话时间', trigger: 'change' }],
        talkLocation: [{ required: true, message: '请输入谈话地点', trigger: 'blur' }]
      },
      tagTypes: {
        '学风建设': 'primary',
        '学业困难': 'warning',
        '心理健康': 'danger',
        '情绪疏导': 'warning',
        '违纪处理': 'danger',
        '警示教育': 'info',
        '职业规划': 'success',
        '就业帮扶': '',
        '新生适应': 'success',
        '校园生活': 'info'
      },
      iconGradients: [
        'linear-gradient(135deg, #667eea, #764ba2)',
        'linear-gradient(135deg, #f56c6c, #e6a23c)',
        'linear-gradient(135deg, #e6a23c, #f56c6c)',
        'linear-gradient(135deg, #67c23a, #409eff)',
        'linear-gradient(135deg, #409eff, #67c23a)'
      ],
      systemTemplates: [],
      myTemplates: [],
      mockStudents: [
        { value: '2024001001', label: '张晓明 - 计算机科学24-1班' },
        { value: '2024002001', label: '李芳华 - 软件工程24-2班' },
        { value: '2024003001', label: '王建国 - 数据科学24-1班' },
        { value: '2023001005', label: '陈小丽 - 计算机科学23-1班' },
        { value: '2024004002', label: '林美琪 - 人工智能24-1班' },
        { value: '2023002003', label: '赵文博 - 软件工程23-2班' },
        { value: '2024005001', label: '黄建华 - 电子工程24-1班' },
        { value: '2023003002', label: '孙雨晴 - 数据科学23-1班' }
      ]
    }
  },
  mounted: function() {
    this.loadTemplates()
  },
  methods: {
    loadTemplates: function() {
      var self = this
      listSystemTemplate().then(function(res) {
        console.log('[DEBUG-tpl] system templates res:', res)
        self.systemTemplates = (res.data || res.rows || (Array.isArray(res) ? res : [])).filter(Boolean)
      }).catch(function(e) {
        console.error('[DEBUG-tpl] system templates error:', e)
      })
      listTemplate({ templateType: 'personal', pageSize: 999 }).then(function(res) {
        console.log('[DEBUG-tpl] personal templates res:', res)
        self.myTemplates = res.rows || (Array.isArray(res) ? res : [])
      }).catch(function(e) {
        console.error('[DEBUG-tpl] personal templates error:', e)
      })
    },

    useTemplate(tmpl) {
      var source = this.systemTemplates.indexOf(tmpl) >= 0 ? '系统预置' : '个人模板'
      this.linkedTemplate = {
        title: tmpl.title || tmpl.templateName,
        content: tmpl.content || tmpl.templateContent,
        editableContent: tmpl.content || tmpl.templateContent,
        source: source
      }
      this.$nextTick(function() {
        var el = document.querySelector('.tplv2-linked-panel')
        if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'center' }) }
      })
    },

    closeLinkedPanel() {
      this.linkedTemplate = null
    },

    copyContent() {
      var self = this
      var content = this.linkedTemplate.editableContent
      if (!content) return

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(content).then(function() {
          self.showToast('内容已复制到剪贴板')
        }).catch(function() {
          self.fallbackCopy(content)
        })
      } else {
        this.fallbackCopy(content)
      }
    },

    fallbackCopy(text) {
      var self = this
      var textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        self.showToast('内容已复制到剪贴板')
      } catch (err) {
        self.showToast('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    },

    openTalkForm() {
      this.talkForm = {
        studentId: '',
        talkTime: '',
        talkLocation: '',
        talkPerson: '张老师'
      }
      this.talkDialogVisible = true
    },

    submitTalk() {
      var self = this
      this.$refs.talkFormRef.validate(function(valid) {
        if (!valid) return
        self.talkSubmitting = true

        setTimeout(function() {
          self.talkSubmitting = false
          self.talkDialogVisible = false

          var student = self.mockStudents.find(function(s) { return s.value === self.talkForm.studentId })
          var studentName = student ? student.label.split(' - ')[0] : self.talkForm.studentId

          self.showToast('谈话已发起！学生：' + studentName + '，时间：' + self.talkForm.talkTime)

          self.$nextTick(function() {
            self.talkForm = {
              studentId: '',
              talkTime: '',
              talkLocation: '',
              talkPerson: '张老师'
            }
          })
        }, 800)
      })
    },

    saveMyTemplate() {
      var self = this
      if (!this.newTemplateName || !this.newTemplateContent) {
        this.$message.warning('请填写模板名称和内容')
        return
      }
      addTemplate({ templateName: this.newTemplateName, templateContent: this.newTemplateContent, templateType: 'personal' }).then(function() {
        self.$message.success('保存成功')
        self.newTemplateName = ''
        self.newTemplateContent = ''
        self.loadTemplates()
      }).catch(function() {})
    },

    deleteMyTemplate(idx) {
      var self = this
      var tpl = this.myTemplates[idx]
      if (!tpl) return
      this.$confirm('确定删除该模板吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        delTemplate(tpl.templateId || tpl.id).then(function() {
          self.$message.success('删除成功')
          self.loadTemplates()
        }).catch(function() {})
      }).catch(function() {})
    },

    showToast(msg) {
      var self = this
      this.toastMessage = msg
      if (this.toastTimer) {
        clearTimeout(this.toastTimer)
      }
      this.toastTimer = setTimeout(function() {
        self.toastMessage = ''
        self.toastTimer = null
      }, 2500)
    }
  },
  beforeDestroy() {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer)
    }
  }
}
</script>

<style scoped>
.tplv2-root {
  --tpl-primary: #667eea;
  --tpl-primary-dark: #5a6fd6;
  --tpl-success: #67c23a;
  --tpl-warning: #e6a23c;
  --tpl-danger: #f56c6c;
  --tpl-info: #409eff;
  --tpl-bg: #f0f4fc;
  --tpl-card-bg: #ffffff;
  --tpl-card-shadow: 0 2px 12px rgba(0,0,0,0.06);
  --tpl-card-hover-shadow: 0 8px 30px rgba(0,0,0,0.12);
  --tpl-text-primary: #1a1a2e;
  --tpl-text-secondary: #909399;
  --tpl-border-color: #ebeef5;
  --tpl-radius: 16px;
  --tpl-radius-sm: 10px;

  min-height: 100vh;
  padding: 24px 28px 40px;
  background: var(--tpl-bg);
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  color: var(--tpl-text-primary);
  box-sizing: border-box;
}

/* ==================== HEADER ==================== */
.tplv2-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.tplv2-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.tplv2-logo-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(102,126,234,0.15), rgba(118,75,162,0.15));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.tplv2-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--tpl-text-primary);
  letter-spacing: 0.5px;
}

.tplv2-subtitle {
  font-size: 13px;
  color: var(--tpl-text-secondary);
  margin-top: 2px;
}

/* ==================== TABS ==================== */
.tplv2-tabs {
  border-radius: var(--tpl-radius) !important;
  overflow: hidden;
  box-shadow: var(--tpl-card-shadow);
  border: 1px solid var(--tpl-border-color) !important;
}

.tplv2-tabs >>> .el-tabs__content {
  padding: 20px;
  min-height: 300px;
}

.tplv2-tabs >>> .el-tabs__header {
  background: #fafbfc;
  border-bottom-color: var(--tpl-border-color);
  margin-bottom: 0;
}

.tplv2-tabs >>> .el-tabs__nav-wrap::after {
  display: none;
}

.tplv2-tabs >>> .el-tabs__item {
  font-weight: 600;
  font-size: 14px;
  padding: 0 24px;
  height: 44px;
  line-height: 44px;
}

.tplv2-tabs >>> .el-tabs__item.is-active {
  color: var(--tpl-primary);
}

/* ==================== TEMPLATE CARD GRID ==================== */
.tplv2-template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  padding: 8px 0;
}

.tplv2-card {
  background: var(--tpl-card-bg);
  border-radius: var(--tpl-radius-sm);
  border: 1px solid var(--tpl-border-color);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
}

.tplv2-card:hover {
  box-shadow: var(--tpl-card-hover-shadow);
  transform: translateY(-3px);
  border-color: var(--tpl-primary);
}

.tplv2-card-body {
  padding: 20px 20px 12px;
  flex: 1;
}

.tplv2-card-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 14px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.tplv2-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--tpl-text-primary);
  margin-bottom: 8px;
}

.tplv2-card-desc {
  font-size: 12px;
  color: var(--tpl-text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.tplv2-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tplv2-card-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--tpl-border-color);
  background: #fafbfc;
  display: flex;
  gap: 8px;
}

/* ==================== MY TEMPLATES ==================== */
.tplv2-my-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
}

.tplv2-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--tpl-text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tplv2-my-create {
  background: #fafbfc;
  border-radius: var(--tpl-radius-sm);
  padding: 20px;
  border: 1px dashed #d9d9d9;
  height: fit-content;
}

.tplv2-create-input {
  margin-bottom: 12px;
}

.tplv2-create-textarea {
  margin-bottom: 12px;
}

.tplv2-my-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tplv2-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #c0c4cc;
  font-size: 14px;
  gap: 8px;
}

.tplv2-empty i {
  font-size: 36px;
}

/* ==================== LINKED PANEL (Template ↔ Talk Content) ==================== */
.tplv2-linked-panel {
  margin-top: 20px;
  background: #fff;
  border: 2px solid var(--tpl-primary);
  border-radius: var(--tpl-radius);
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(102,126,234,0.2);
}

.tplv2-linked-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, var(--tpl-primary), var(--tpl-primary-dark));
  color: #fff;
}

.tplv2-linked-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tplv2-linked-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 8px rgba(255,255,255,0.6);
  animation: dotPulse 1.5s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50%      { opacity: 1;   transform: scale(1.3); }
}

.tplv2-linked-title {
  font-size: 15px;
  font-weight: 600;
}

.tplv2-linked-close {
  color: rgba(255,255,255,0.8) !important;
  font-size: 16px !important;
}

.tplv2-linked-close:hover {
  color: #fff !important;
}

.tplv2-linked-body {
  padding: 20px;
  display: flex;
  gap: 20px;
}

.tplv2-linked-content-area {
  flex: 1;
  min-width: 0;
}

.tplv2-linked-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--tpl-text-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tplv2-linked-textarea >>> textarea {
  font-size: 14px;
  line-height: 1.8;
  color: var(--tpl-text-primary);
  border-radius: 8px;
  resize: vertical;
}

.tplv2-linked-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: flex-end;
  flex-shrink: 0;
}

.tplv2-linked-actions .el-button {
  min-width: 120px;
}

/* Panel Slide Transition */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1.2);
}

.panel-slide-enter,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

/* ==================== DIALOG ==================== */
.tplv2-dialog >>> .el-dialog {
  border-radius: var(--tpl-radius);
  overflow: hidden;
}

.tplv2-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, var(--tpl-primary), var(--tpl-primary-dark));
  padding: 16px 24px;
}

.tplv2-dialog >>> .el-dialog__title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.tplv2-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: rgba(255,255,255,0.8);
}

.tplv2-dialog >>> .el-dialog__body {
  padding: 24px;
}

.tplv2-dialog-preview {
  margin-top: 16px;
  background: #fafbfc;
  border: 1px solid var(--tpl-border-color);
  border-radius: 8px;
  padding: 14px 16px;
}

.tplv2-dialog-preview-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--tpl-text-secondary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tplv2-dialog-preview-content {
  font-size: 13px;
  color: var(--tpl-text-primary);
  line-height: 1.7;
  white-space: pre-line;
}

/* ==================== TOAST ==================== */
.tplv2-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5000;
  background: linear-gradient(135deg, var(--tpl-success), #409eff);
  color: #fff;
  padding: 12px 28px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 8px 24px rgba(103,194,58,0.4);
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
}

.toast-fade-enter,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 1200px) {
  .tplv2-template-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
  .tplv2-my-layout {
    grid-template-columns: 1fr;
  }
  .tplv2-linked-body {
    flex-direction: column;
  }
  .tplv2-linked-actions {
    flex-direction: row;
  }
}

@media (max-width: 768px) {
  .tplv2-root {
    padding: 16px 12px 40px;
  }
  .tplv2-template-grid {
    grid-template-columns: 1fr;
  }
  .tplv2-my-layout {
    grid-template-columns: 1fr;
  }
  .tplv2-linked-body {
    flex-direction: column;
  }
  .tplv2-linked-actions {
    flex-direction: row;
  }
  .tplv2-title {
    font-size: 18px;
  }
}
</style>