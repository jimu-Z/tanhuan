<template>
  <div class="app-container">
    <!-- 学生基本信息 -->
    <el-card class="detail-card">
      <div slot="header">
        <span>{{ student.studentName }} 的基本信息</span>
        <el-button size="mini" class="return-button" @click="$router.push('/talk/student')">返回列表</el-button>
      </div>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="学号">{{ student.studentCode }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ student.studentName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ student.deptName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="管理教师" :span="3">{{ managers.map(t => t.teacherName + '（' + t.teacherCode + '）').join('、') || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ $talkLabel('gender', student.gender) }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ student.nation }}</el-descriptions-item>
        <el-descriptions-item label="政治面貌">{{ $talkLabel('politicalStatus', student.politicalStatus, studentDicts.politicalStatus) }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ student.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ student.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭住址" :span="3">{{ student.address }}</el-descriptions-item>
        <el-descriptions-item label="父亲姓名">{{ student.fatherName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="父亲电话">{{ student.fatherPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="母亲姓名">{{ student.motherName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="母亲电话">{{ student.motherPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班长">{{ student.classMonitor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="舍长">{{ student.dormLeader || '-' }}</el-descriptions-item>
        <el-descriptions-item label="宿舍楼">{{ student.dormBuilding || '-' }}</el-descriptions-item>
        <el-descriptions-item label="宿舍号">{{ student.dormRoom || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">{{ $talkLabel('enrollmentStatus', student.enrollmentStatus, studentDicts.enrollmentStatus) }}</el-descriptions-item>
        <el-descriptions-item label="心理健康">{{ $talkLabel('mentalHealthStatus', student.mentalHealthStatus, studentDicts.mentalHealthStatus) }}</el-descriptions-item>
        <el-descriptions-item label="贫困等级">{{ $talkLabel('povertyLevel', student.povertyLevel, studentDicts.povertyLevel) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ student.remark || '-' }}</el-descriptions-item>
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
            <span v-else>{{ $talkLabel('talkType', scope.row.session.talkType) }}</span>
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
            <span v-else>{{ $talkLabel('followupStatus', scope.row.record.followupStatus) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getDicts } from "@/api/system/dict/data"
import { getLabels } from "@/api/talk/talkTag"
import { getStudentDetail } from '@/api/talk/talkStudent'
import { getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'

export default {
  name: 'StudentDetail',
  data() {
    return {
      student: {},
      studentDicts: {},
      history: [],
      managers: [],
      loadingHistory: false,
      tagCache: {},
      tagLabels: { ...TAG_LABELS }
    }
  },
  created() {
    for (const [field, type] of Object.entries({ politicalStatus: 'political_status', enrollmentStatus: 'enrollment_status', mentalHealthStatus: 'mental_health_status', povertyLevel: 'poverty_level' })) {
      getDicts(type).then(res => { this.$set(this.studentDicts, field, res.data || []) }).catch(() => {})
    }
    getLabels().then(res => { this.tagLabels = { ...TAG_LABELS, ...(res.data || {}) } }).catch(() => {})
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
        this.managers = data.managers || []
        this.history = (data.history || []).sort((a, b) => {
          return ((b.session && b.session.talkTime) || '').localeCompare((a.session && a.session.talkTime) || '')
        })
        this.history.forEach(h => {
          const sid = h.session ? h.session.sessionId : null
          if (sid) {
            getSessionTags(sid).then(r => { this.$set(this.tagCache, sid, r.data || []) }).catch(err => {
              console.warn('加载会话标签失败:', sid, err)
            })
          }
        })
      }).catch(() => { this.$modal.msgError('加载学生详情失败') }).finally(() => {
        this.loadingHistory = false
      })
    },
    getTagsDisplay(sessionId) {
      return (this.tagCache[sessionId] || []).map(t => this.$talkTags(t.tagValue, this.tagLabels)).join('、')
    }
  }
}
</script>

<style scoped>
.detail-card { margin-bottom: 16px; }
.return-button { float: right; }
</style>
