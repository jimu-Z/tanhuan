<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="16" style="padding-left:20px">
        <h2>学生谈心谈话管理系统</h2>
        <p>
          基于 RuoYi-Vue 框架开发的高校辅导员谈心谈话工作管理平台。支持学生信息管理、个别/集体谈话记录、
          谈话标签分类、跟进状态追踪、docx 文档一键导出等功能，助力辅导员高效完成学生思想教育工作。
        </p>
        <p><b>当前版本:</b> <span>v1.0.0</span></p>
        <div style="margin-top:16px">
          <el-button type="primary" icon="el-icon-edit" size="small" @click="$router.push('/talk/talkInitiate/index')">发起谈话</el-button>
          <el-button type="success" icon="el-icon-user" size="small" @click="$router.push('/talk/talkStudent/index')">学生管理</el-button>
          <el-button type="warning" icon="el-icon-s-data" size="small" @click="$router.push('/talk/my-records')">我的记录</el-button>
          <el-button icon="el-icon-search" size="small" @click="$router.push('/talk/advanced-query')">高级查询</el-button>
        </div>
      </el-col>
      <el-col :sm="24" :lg="8" style="padding-left:20px">
        <h2>核心功能</h2>
        <ul>
          <li>📋 学生台账导入（Excel 自动识别）</li>
          <li>💬 个别 / 集体谈话记录</li>
          <li>🏷️ 7 类谈话内容标签</li>
          <li>📊 跟进状态追踪</li>
          <li>📄 docx 文档一键导出</li>
          <li>🔐 角色权限分级（管理员 / 书记 / 辅导员）</li>
          <li>🔍 多条件高级查询</li>
          <li>💾 自动数据备份</li>
        </ul>
      </el-col>
    </el-row>
    <el-divider />
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>技术栈</span></div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="后端">Spring Boot + MyBatis</el-descriptions-item>
            <el-descriptions-item label="前端">Vue 2 + Element UI</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL + Redis</el-descriptions-item>
            <el-descriptions-item label="权限">Spring Security + RBAC</el-descriptions-item>
            <el-descriptions-item label="文档">Apache POI XWPF</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>快速入门</span></div>
          <ol style="padding-left:20px;line-height:2">
            <li>导入学生台账数据</li>
            <li>在"发起谈话"中创建谈话记录</li>
            <li>在"谈话会话管理"或"我的记录"中查看</li>
            <li>在"谈话记录管理"中填写学生反馈和跟进计划</li>
            <li>导出 docx 文档存档</li>
          </ol>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>关于</span></div>
          <p style="line-height:1.8">
            本系统基于 <el-link type="primary" href="https://gitee.com/y_project/RuoYi-Vue" target="_blank">RuoYi-Vue v3.9.2</el-link> 开发，
            遵循 MIT 开源协议。数据库结构遵循设计文档
            <el-link type="primary" href="https://github.com/jimu-Z/tanhuan" target="_blank">GitHub</el-link>。
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
.home {
  h2 { margin-top: 10px; font-size: 26px; font-weight: 100; }
  h4 { margin-top: 0; }
  p { margin-top: 10px; b { font-weight: 700; } }
  ul { list-style-type: none; padding: 0; li { line-height: 2; } }
  .el-card { margin-bottom: 16px; }
}
</style>
