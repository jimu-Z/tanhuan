<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="queryParams.templateName"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模板类型" prop="templateType">
        <el-select v-model="queryParams.templateType" placeholder="请选择模板类型" clearable>
          <el-option label="系统预置" value="system" />
          <el-option label="个人模板" value="personal" />
        </el-select>
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
          v-hasPermi="['talk:template:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column label="模板ID" align="center" prop="templateId" width="80" />
      <el-table-column label="模板名称" align="center" prop="templateName" show-overflow-tooltip />
      <el-table-column label="模板类型" align="center" prop="templateType" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.templateType === 'system' ? 'primary' : 'success'" size="small">
            {{ scope.row.templateType === 'system' ? '系统预置' : '个人模板' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" align="center" prop="templateTags" width="200">
        <template slot-scope="scope">
          <el-tag
            v-for="(tag, idx) in parseTags(scope.row.templateTags)"
            :key="idx"
            size="mini"
            style="margin: 2px;"
          >{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document-copy"
            @click="handleUseTemplate(scope.row)"
          >使用</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
          >预览</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['talk:template:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['talk:template:remove']"
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

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="form.templateName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="模板类型" prop="templateType">
              <el-select v-model="form.templateType" placeholder="请选择模板类型" style="width:100%">
                <el-option label="个人模板" value="personal" />
                <el-option label="系统预置" value="system" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签" prop="templateTags">
              <el-input v-model="form.templateTags" placeholder="请输入标签，多个用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="模板内容" prop="templateContent">
              <el-input
                v-model="form.templateContent"
                type="textarea"
                :rows="8"
                placeholder="请输入模板内容"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="模板预览" :visible.sync="previewOpen" width="600px" append-to-body>
      <div v-if="previewTemplate" style="padding: 10px;">
        <div style="margin-bottom: 10px;">
          <span style="font-weight: bold;">模板名称：</span>{{ previewTemplate.templateName }}
        </div>
        <div style="margin-bottom: 10px;">
          <span style="font-weight: bold;">模板类型：</span>
          <el-tag :type="previewTemplate.templateType === 'system' ? 'primary' : 'success'" size="small">
            {{ previewTemplate.templateType === 'system' ? '系统预置' : '个人模板' }}
          </el-tag>
        </div>
        <div style="margin-bottom: 10px;">
          <span style="font-weight: bold;">标签：</span>
          <el-tag
            v-for="(tag, idx) in parseTags(previewTemplate.templateTags)"
            :key="idx"
            size="mini"
            style="margin: 2px;"
          >{{ tag }}</el-tag>
        </div>
        <div>
          <span style="font-weight: bold;">模板内容：</span>
          <div style="margin-top: 8px; padding: 12px; background: #f5f7fa; border-radius: 4px; white-space: pre-wrap; line-height: 1.8;">
            {{ previewTemplate.templateContent }}
          </div>
        </div>
        <div style="margin-top: 16px; text-align: right;">
          <el-button type="primary" size="small" icon="el-icon-document-copy" @click="handleUseFromPreview">
            使用此模板
          </el-button>
          <el-button size="small" @click="previewOpen = false">关 闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, getTemplate, addTemplate, updateTemplate, delTemplate } from '@/api/talk/talkTemplate'
import { TAG_LABELS } from '@/api/talk/talkSession'

export default {
  name: 'TemplatesV2',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      templateList: [],
      title: '',
      open: false,
      previewOpen: false,
      previewTemplate: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateName: null,
        templateType: null
      },
      form: {},
      rules: {
        templateName: [
          { required: true, message: '模板名称不能为空', trigger: 'blur' }
        ],
        templateContent: [
          { required: true, message: '模板内容不能为空', trigger: 'blur' }
        ],
        templateType: [
          { required: true, message: '模板类型不能为空', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    parseTags(tags) {
      if (!tags) return []
      function toLabel(t) { return TAG_LABELS[t] || t }
      try {
        var parsed = JSON.parse(tags)
        if (Array.isArray(parsed)) return parsed.map(toLabel)
      } catch (e) {
        return tags.split(',').map(function(t) { return toLabel(t.trim()) }).filter(Boolean)
      }
      return tags.split(',').map(function(t) { return toLabel(t.trim()) }).filter(Boolean)
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        templateId: null,
        templateName: null,
        templateContent: null,
        templateType: 'personal',
        templateTags: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange() {},
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增谈话模板'
    },
    handleUpdate(row) {
      this.reset()
      var templateId = row.templateId
      getTemplate(templateId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改谈话模板'
      }).catch(() => {
        this.$modal.msgError('获取模板信息失败')
      })
    },
    submitForm() {
      var self = this
      this.$refs['form'].validate(function(valid) {
        if (valid) {
          if (self.form.templateId != null) {
            updateTemplate(self.form).then(function() {
              self.$modal.msgSuccess('修改成功')
              self.open = false
              self.getList()
            }).catch(function() {
              self.$modal.msgError('修改失败')
            })
          } else {
            addTemplate(self.form).then(function() {
              self.$modal.msgSuccess('新增成功')
              self.open = false
              self.getList()
            }).catch(function() {
              self.$modal.msgError('新增失败')
            })
          }
        }
      })
    },
    handleDelete(row) {
      var self = this
      this.$modal.confirm('是否确认删除模板"' + row.templateName + '"？').then(function() {
        return delTemplate(row.templateId)
      }).then(function() {
        self.getList()
        self.$modal.msgSuccess('删除成功')
      }).catch(function() {})
    },
    handlePreview(row) {
      this.previewTemplate = row
      this.previewOpen = true
    },
    handleUseTemplate(row) {
      var content = row.templateContent
      if (!content) {
        this.$modal.msgWarning('模板内容为空')
        return
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        var self = this
        navigator.clipboard.writeText(content).then(function() {
          self.$modal.msgSuccess('模板内容已复制到剪贴板，可直接粘贴到编辑器中')
        }).catch(function() {
          self.fallbackCopy(content)
        })
      } else {
        this.fallbackCopy(content)
      }
    },
    handleUseFromPreview() {
      if (this.previewTemplate) {
        this.handleUseTemplate(this.previewTemplate)
        this.previewOpen = false
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
        self.$modal.msgSuccess('模板内容已复制到剪贴板，可直接粘贴到编辑器中')
      } catch (err) {
        self.$modal.msgError('复制失败，请手动复制')
      } finally {
        try {
          document.body.removeChild(textarea)
        } catch (e) {}
      }
    }
  }
}
</script>