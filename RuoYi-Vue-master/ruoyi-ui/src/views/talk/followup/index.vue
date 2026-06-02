<template>
  <div class="app-container followup-page">
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.status">
        <div class="stat-card" :style="{ borderLeftColor: card.color }" @click="filterByStatus(card.status)">
          <div class="stat-num" :style="{ color: card.color }">{{ card.count }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" inline size="small">
      <el-form-item label="跟进状态">
        <el-select v-model="queryParams.followupStatus" placeholder="全部" clearable style="width:130px" @change="handleSearch">
          <el-option label="待跟进" value="pending" />
          <el-option label="跟进中" value="in_progress" />
          <el-option label="已完成" value="completed" />
          <el-option label="无需跟进" value="none" />
        </el-select>
      </el-form-item>
      <el-form-item label="学生姓名">
        <el-input v-model="queryParams.studentName" placeholder="搜索学生" clearable style="width:150px" @keyup.enter.native="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert v-if="overdueCount > 0" :title="'有 ' + overdueCount + ' 条记录超过7天未跟进，请及时处理'" type="warning" show-icon :closable="false" style="margin-bottom:15px" />

    <div v-loading="loading" class="timeline-view">
      <div v-if="records.length === 0" class="timeline-empty">
        <i class="el-icon-s-data" style="font-size:48px;color:#c0c4cc" />
        <p>暂无跟进记录</p>
      </div>
      <div v-for="rec in records" :key="rec.recordId" class="timeline-item">
        <div class="timeline-dot" :class="'dot-' + (rec.followupStatus || 'pending')" />
        <div class="timeline-content">
          <div class="timeline-header">
            <strong>{{ rec.studentName || '未知' }}</strong>
            <span class="timeline-code">（{{ rec.studentCode || '-' }}）</span>
            <el-tag :type="tagType(rec.followupStatus)" size="small">{{ statusLabel(rec.followupStatus) }}</el-tag>
            <span v-if="rec.updateTime" class="timeline-date">{{ rec.updateTime }}</span>
            <span v-if="isOverdue(rec)" class="overdue-badge">超期</span>
          </div>
          <div class="timeline-body">
            <div v-if="rec.studentFeedback" class="timeline-feedback">
              <span class="tl-label">反馈:</span> {{ rec.studentFeedback }}
            </div>
            <div v-if="rec.followupPlan" class="timeline-plan">
              <span class="tl-label">计划:</span> {{ rec.followupPlan }}
            </div>
          </div>
          <div class="timeline-actions">
            <el-button size="mini" @click="openEditDialog(rec)">编辑</el-button>
            <el-button size="mini" type="warning" v-if="rec.followupStatus === 'pending'" @click="quickUpdateStatus(rec, 'in_progress')">开始跟进</el-button>
            <el-button size="mini" type="success" v-if="rec.followupStatus === 'in_progress'" @click="quickUpdateStatus(rec, 'completed')">完成</el-button>
          </div>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="fetchRecords" />

    <el-dialog :title="editTitle" :visible.sync="dialogVisible" width="520px" append-to-body>
      <el-form ref="editForm" :model="editForm" label-width="90px">
        <el-form-item label="学生">
          <span>{{ editForm.studentName || '-' }}（{{ editForm.studentCode || '-' }}）</span>
        </el-form-item>
        <el-form-item label="学生反馈" prop="studentFeedback">
          <el-input v-model="editForm.studentFeedback" type="textarea" :rows="3" placeholder="学生在谈话中的反馈" />
        </el-form-item>
        <el-form-item label="跟进状态" prop="followupStatus">
          <el-radio-group v-model="editForm.followupStatus">
            <el-radio-button label="pending">待跟进</el-radio-button>
            <el-radio-button label="in_progress">跟进中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="none">无需跟进</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="跟进计划" prop="followupPlan">
          <el-input v-model="editForm.followupPlan" type="textarea" :rows="3" placeholder="后续跟进计划及措施" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTalkrecord, updateTalkrecord } from "@/api/talk/talkStudentRecord"
import { getAlerts } from "@/api/talk/talkStatistics"

const FOLLOWUP_OVERDUE_MS = 7 * 24 * 60 * 60 * 1000

export default {
  name: "FollowupIndex",
  data() {
    return {
      loading: false,
      total: 0,
      records: [],
      queryParams: { pageNum: 1, pageSize: 10, followupStatus: null, studentName: null },
      dialogVisible: false,
      editTitle: "编辑跟进记录",
      editForm: {},
      statCounts: { pending: 0, in_progress: 0, completed: 0, none: 0 },
    }
  },
  computed: {
    statCards() {
      return [
        { status: 'pending',     label: '待跟进',   count: this.statCounts.pending,     color: '#909399' },
        { status: 'in_progress', label: '跟进中',   count: this.statCounts.in_progress, color: '#E6A23C' },
        { status: 'completed',   label: '已完成',   count: this.statCounts.completed,   color: '#67C23A' },
        { status: 'none',        label: '无需跟进', count: this.statCounts.none,        color: '#409EFF' },
      ]
    },
    overdueCount() {
      const now = Date.now()
      return this.records.filter(r => {
        if (r.followupStatus === 'completed' || r.followupStatus === 'none') return false
        const t = r.updateTime ? new Date(r.updateTime).getTime() : 0
        return t > 0 && (now - t) > FOLLOWUP_OVERDUE_MS
      }).length
    },
  },
  created() { this.fetchRecords(); this.fetchStats(); },
  methods: {
    fetchStats() {
      getAlerts().then(res => {
        const data = res.data || {}
        this.statCounts = {
          pending: data.pendingFollowups || 0,
          in_progress: data.inProgressFollowups || 0,
          completed: data.completedFollowups || 0,
          none: data.noneFollowups || 0,
        }
      }).catch(() => { this.$modal.msgError('操作失败') })
    },
    fetchRecords() {
      this.loading = true
      listTalkrecord(this.queryParams).then(res => {
        this.records = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleSearch() {
      this.queryParams.pageNum = 1
      this.fetchRecords()
    },
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10, followupStatus: null, studentName: null }
      this.fetchRecords()
    },
    filterByStatus(status) {
      this.queryParams.followupStatus = status === this.queryParams.followupStatus ? null : status
      this.queryParams.pageNum = 1
      this.fetchRecords()
    },
    quickUpdateStatus(rec, status) {
      updateTalkrecord({ ...rec, followupStatus: status }).then(() => {
        this.$modal.msgSuccess("状态更新成功")
        this.fetchRecords()
        this.fetchStats()
      }).catch(() => { this.$modal.msgError('状态更新失败') })
    },
    openEditDialog(row) {
      this.editForm = { ...row }
      this.editTitle = '编辑跟进记录 — ' + (row.studentName || row.studentCode || '')
      this.dialogVisible = true
    },
    submitEdit() {
      updateTalkrecord(this.editForm).then(() => {
        this.$modal.msgSuccess("保存成功")
        this.dialogVisible = false
        this.fetchRecords()
        this.fetchStats()
      }).catch(() => { this.$modal.msgError('保存失败') })
    },
    tagType(v) {
      return { none:'info', pending:'info', in_progress:'warning', completed:'success' }[v] || ''
    },
    statusLabel(v) {
      return { none:'无需跟进', pending:'待跟进', in_progress:'跟进中', completed:'已完成' }[v] || v
    },
    isOverdue(rec) {
      if (rec.followupStatus === 'completed' || rec.followupStatus === 'none') return false
      const t = rec.updateTime ? new Date(rec.updateTime).getTime() : 0
      return t > 0 && (Date.now() - t) > FOLLOWUP_OVERDUE_MS
    },
  },
}
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-left: 4px solid;
  border-radius: 6px;
  padding: 16px 18px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  transition: all 0.2s;
  margin-bottom: 12px;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-num { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }

.timeline-view { padding: 10px 0; }
.timeline-empty { text-align: center; color: #c0c4cc; padding: 60px 0; font-size: 14px; }
.timeline-item { display: flex; gap: 14px; padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.timeline-dot { width: 12px; height: 12px; border-radius: 50%; margin-top: 4px; flex-shrink: 0; }
.dot-pending { background: #909399; }
.dot-in_progress { background: #E6A23C; }
.dot-completed { background: #67C23A; }
.dot-none { background: #409EFF; }
.timeline-content { flex: 1; }
.timeline-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.timeline-code { font-size: 12px; color: #999; }
.timeline-date { font-size: 12px; color: #999; margin-left: auto; }
.overdue-badge { background: #F56C6C; color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 3px; }
.timeline-body { margin-top: 8px; }
.timeline-feedback, .timeline-plan { font-size: 13px; color: #606266; margin-bottom: 4px; }
.tl-label { color: #409EFF; font-weight: 500; }
.timeline-actions { margin-top: 8px; display: flex; gap: 8px; }
</style>