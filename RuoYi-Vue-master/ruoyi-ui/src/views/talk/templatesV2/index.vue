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
              <el-select
                v-model="selectedTags"
                multiple
                filterable
                placeholder="请选择标签"
                style="width:100%"
              >
                <el-option
                  v-for="opt in tagOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
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
import { getLabels } from '@/api/talk/talkTag'

export default {
  name: 'TemplatesV2',
  data() {
    // 构建标签选项列表
    const tagOptions = Object.keys(TAG_LABELS).map(k => ({ value: k, label: TAG_LABELS[k] }))
    return {
      loading: false,
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
      tagOptions: tagOptions,
      selectedTags: [],
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
    this.loadTagOptions()
  },
  methods: {
    loadTagOptions() {
      getLabels().then(res => {
        if (res.data && Object.keys(res.data).length > 0) {
          this.tagOptions = Object.keys(res.data).map(k => ({ value: k, label: res.data[k] }))
        }
      }).catch(() => {}) // 失败则用兜底 TAG_LABELS
    },
    getList() {
      this.loading = true
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.$modal.msgError('加载模板列表失败')
      })
    },
    parseTags(tags) {
      if (!tags) return []
      function toLabel(t) { return TAG_LABELS[t] || t }
      try {
        var parsed = JSON.parse(tags)
        if (Array.isArray(parsed)) return parsed.map(toLabel)
      } catch (e) {
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
      this.selectedTags = []
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.templateId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
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
        // 解析标签到多选数组
        this.selectedTags = this.parseTagValues(this.form.templateTags)
        this.open = true
        this.title = '修改谈话模板'
      }).catch(() => {
        this.$modal.msgError('获取模板信息失败')
      })
    },
    parseTagValues(tags) {
      if (!tags) return []
      try {
        const parsed = JSON.parse(tags)
        if (Array.isArray(parsed)) return parsed
      } catch (e) {}
      return tags.split(',').map(t => t.trim()).filter(Boolean)
    },
    submitForm() {
      var self = this
      // 多选标签拼接为逗号分隔字符串
      this.form.templateTags = this.selectedTags.join(',')
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

  ::v-deep .el-table__body tr.current-row > td {
    background: #e8f4f8 !important;
  }
}

.small-padding {
  ::v-deep .el-button--text {
    padding: 4px 8px;
    font-size: 13px;

    &:first-child {
      color: #1a5276;
      &:hover { color: #2a6fa8; text-decoration: underline; }
    }

    &:nth-child(2) {
      color: #2a6fa8;
      &:hover { color: #3a85c0; text-decoration: underline; }
    }

    &:nth-child(3) {
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