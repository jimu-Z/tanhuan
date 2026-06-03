<template>
  <div class="app-container">
    <!-- 学生基本信息 -->
    <el-card class="detail-card">
      <div slot="header">
        <span>{{ student.studentName }} 的基本信息</span>
      </div>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="学号">{{ student.studentCode }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ student.studentName }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ student.gender === '0' ? '男' : student.gender === '1' ? '女' : student.gender }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ student.nation }}</el-descriptions-item>
        <el-descriptions-item label="政治面貌">{{ student.politicalStatus }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ student.phone }}</el-descriptions-item>
        <el-descriptions-item label="家庭住址" :span="3">{{ student.address }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">{{ student.enrollmentStatus }}</el-descriptions-item>
        <el-descriptions-item label="心理健康">{{ student.mentalHealthStatus }}</el-descriptions-item>
        <el-descriptions-item label="贫困等级">{{ student.povertyLevel }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 历史谈话记录 -->
    <el-card class="detail-card" style="margin-top:16px">
      <div slot="header">
        <span>历史谈话记录 ({{ history.length }}条)</span>
      </div>
      <el-table :data="history" size="small" v-loading="loadingHistory">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="谈话时间" width="120" align="center">
          <template slot-scope="scope">
            {{ scope.row.session.talkTime ? scope.row.session.talkTime.substring(0,10) : '' }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.session.talkType === 'individual'" type="primary" size="small">个别谈话</el-tag>
            <el-tag v-else-if="scope.row.session.talkType === 'group'" type="success" size="small">集体谈话</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="谈话人" prop="session.talkPerson" width="120" align="center" />
        <el-table-column label="谈话内容" prop="session.talkContent" min-width="200" show-overflow-tooltip />
        <el-table-column label="标签" width="120" align="center">
          <template slot-scope="scope">
            <span style="font-size:12px;color:#666">{{ getTagsDisplay(scope.row.session.sessionId) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生反馈" prop="record.studentFeedback" min-width="150" show-overflow-tooltip />
        <el-table-column label="跟进计划" prop="record.followupPlan" min-width="150" show-overflow-tooltip />
        <el-table-column label="跟进状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.record.followupStatus === 'pending'" type="info" size="small">待跟进</el-tag>
            <el-tag v-else-if="scope.row.record.followupStatus === 'in_progress'" type="warning" size="small">跟进中</el-tag>
            <el-tag v-else-if="scope.row.record.followupStatus === 'completed'" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="scope.row.record.followupStatus === 'none'" size="small">无需跟进</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getStudentDetail } from '@/api/talk/talkStudent'
import { getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'

export default {
  name: 'StudentDetail',
  data() {
    return {
      student: {},
      history: [],
      loadingHistory: false,
      tagCache: {}
    }
  },
  created() {
    const studentId = this.$route.params.studentId || this.$route.query.studentId
    if (studentId) {
      this.loadDetail(studentId)
    }
  },
  methods: {
    loadDetail(studentId) {
      this.loadingHistory = true
      getStudentDetail(studentId).then(res => {
        const data = res.data || {}
        this.student = data.student || {}
        this.history = (data.history || []).sort((a, b) => {
          return ((b.session && b.session.talkTime) || '').localeCompare((a.session && a.session.talkTime) || '')
        })
        this.history.forEach(h => {
          const sid = h.session ? h.session.sessionId : null
          if (sid) {
            getSessionTags(sid).then(r => { this.$set(this.tagCache, sid, (r.data || []).map(t => TAG_LABELS[t.tagValue] || t.tagValue).join('、')) }).catch(err => {
              console.warn('加载会话标签失败:', sid, err)
            })
          }
        })
      }).catch(() => { this.$modal.msgError('加载学生详情失败') }).finally(() => {
        this.loadingHistory = false
      })
    },
    getTagsDisplay(sessionId) {
      return this.tagCache[sessionId] || ''
    }
  }
}
</script>

<style scoped>
.detail-card { margin-bottom: 16px; }
</style>
