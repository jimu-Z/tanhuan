<template>
  <div class="app-container home">
    <el-row :gutter="16">
      <el-col :span="16">
        <h2 style="font-weight:300;font-size:28px;margin-bottom:20px">学生谈心谈话管理系统</h2>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="8" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" :body-style="{padding:'24px'}" style="margin-bottom:16px;min-height:200px">
          <div style="text-align:center">
            <i :class="card.icon" :style="{fontSize:'48px',color:card.color}"></i>
            <h3 style="margin:12px 0;font-weight:500">{{ card.title }}</h3>
            <p style="font-size:13px;color:#909399;line-height:1.8">{{ card.desc }}</p>
            <el-button :type="card.type" size="small" style="margin-top:12px" @click="$router.push(card.path)">进入</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      cards: [
        { icon:'el-icon-user', color:'#409EFF', title:'学生管理', desc:'导入台账、查看学生信息、管理学生数据', type:'primary', path:'/talk/talkStudent/index' },
        { icon:'el-icon-edit', color:'#67C23A', title:'发起谈话', desc:'创建个别/集体谈话记录，选择标签和内容', type:'success', path:'/talk/talkInitiate/index' },
        { icon:'el-icon-s-order', color:'#E6A23C', title:'会话管理', desc:'查看、编辑、导出历史谈话会话', type:'warning', path:'/talk/talkSession/index' },
        { icon:'el-icon-document', color:'#F56C6C', title:'记录管理', desc:'管理学生反馈、跟进计划和状态', type:'danger', path:'/talk/talkStudentRecord/index' },
        { icon:'el-icon-search', color:'#909399', title:'高级查询', desc:'多条件组合查询，批量导出文档', type:'info', path:'/talk/advanced-query' },
        { icon:'el-icon-s-data', color:'#409EFF', title:'我的记录', desc:'快速查看个人创建的谈话记录', type:'', path:'/talk/my-records' }
      ]
    }
  }
}
</script>

<style scoped>
.home { padding-bottom: 20px; }
h2 { margin-top: 10px; }
</style>
