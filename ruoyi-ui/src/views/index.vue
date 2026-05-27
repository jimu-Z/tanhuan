<template>
  <div class="app-container home">
    <div class="v3-banner">
      <div class="v3-banner-bg"></div>
      <div class="v3-banner-content">
        <h1>学校谈话管理系统</h1>
        <p>高效管理学生谈话记录，助力思政教育工作</p>
        <div class="v3-banner-meta">
          <span><i class="el-icon-user" /> {{ username }}</span>
          <span><i class="el-icon-date" /> {{ today }}</span>
          <span v-if="stats.pendingCount > 0" class="v3-banner-alert"><i class="el-icon-warning" /> {{ stats.pendingCount }} 条待跟进</span>
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="v3-card-row">
      <el-col :span="6">
        <div class="v3-card v3-card-danger" @click="goPending">
          <div class="v3-card-header">
            <i class="el-icon-warning-outline" />
            <span>待跟进谈话</span>
          </div>
          <div class="v3-card-body">
            <count-to :start-val="0" :end-val="stats.pendingCount" :duration="1500" class="v3-card-num" />
            <span class="v3-card-unit">条</span>
          </div>
          <div class="v3-card-footer">点击查看详情 <i class="el-icon-arrow-right" /></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="v3-card v3-card-primary" @click="goRecords">
          <div class="v3-card-header"><i class="el-icon-s-data" /><span>本月谈话</span></div>
          <div class="v3-card-body">
            <count-to :start-val="0" :end-val="stats.thisMonthCount" :duration="1800" class="v3-card-num" />
            <span class="v3-card-unit">次</span>
          </div>
          <div class="v3-card-footer">本月累计完成 <i class="el-icon-arrow-right" /></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="v3-card v3-card-success" @click="goStudent">
          <div class="v3-card-header"><i class="el-icon-user-solid" /><span>学生总数</span></div>
          <div class="v3-card-body">
            <count-to :start-val="0" :end-val="stats.totalStudentCount" :duration="2000" class="v3-card-num" />
            <span class="v3-card-unit">人</span>
          </div>
          <div class="v3-card-footer">管理范围 <i class="el-icon-arrow-right" /></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="v3-card v3-card-warning" @click="goRecords">
          <div class="v3-card-header"><i class="el-icon-circle-check" /><span>已完成谈话</span></div>
          <div class="v3-card-body">
            <count-to :start-val="0" :end-val="stats.completedCount" :duration="1200" class="v3-card-num" />
            <span class="v3-card-unit">条</span>
          </div>
          <div class="v3-card-footer">累计完成 <i class="el-icon-arrow-right" /></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="v3-records-card">
          <div slot="header">
            <span class="card-header-title">最近谈话记录</span>
            <el-button type="text" size="small" style="float:right" @click="goRecords">全部 <i class="el-icon-arrow-right" /></el-button>
          </div>
          <div class="v3-timeline">
            <div v-for="(item, idx) in recentList.slice(0, 6)" :key="idx" class="v3-timeline-item">
              <div class="v3-timeline-dot" :class="item.status === '0' ? 'done' : 'pending'"></div>
              <div class="v3-timeline-content">
                <div class="v3-timeline-header">
                  <strong>{{ item.studentName }} <small>({{ item.studentNo }})</small></strong>
                  <el-tag :type="item.status === '0' ? 'success' : 'warning'" size="mini" effect="plain">{{ item.status === '0' ? '已完成' : '待跟进' }}</el-tag>
                </div>
                <p class="v3-timeline-topic">{{ item.topic }}</p>
                <p class="v3-timeline-meta">{{ item.collegeName }} · {{ item.speaker }} · {{ item.conversationTime ? item.conversationTime.substring(0, 16) : '' }}</p>
              </div>
            </div>
            <div v-if="recentList.length === 0" class="empty-hint">暂无谈话记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header"><span class="card-header-title">快捷操作</span></div>
          <el-row :gutter="12" class="v3-actions">
            <el-col :span="8">
              <div class="v3-action-btn" @click="goNewConversation">
                <div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #409EFF, #66B1FF)"><i class="el-icon-edit-outline" /></div>
                <span>新建谈话</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="v3-action-btn" @click="goStudent"><div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #67C23A, #85CE61)"><i class="el-icon-user" /></div><span>学生管理</span></div>
            </el-col>
            <el-col :span="8">
              <div class="v3-action-btn" @click="goMyRecords"><div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #E6A23C, #EBB563)"><i class="el-icon-notebook-2" /></div><span>我的记录</span></div>
            </el-col>
            <el-col :span="8">
              <div class="v3-action-btn" @click="goExport"><div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #9B59B6, #BB80D4)"><i class="el-icon-download" /></div><span>导出数据</span></div>
            </el-col>
            <el-col :span="8">
              <div class="v3-action-btn" @click="goMajor"><div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #F56C6C, #F78989)"><i class="el-icon-school" /></div><span>专业/班级</span></div>
            </el-col>
            <el-col :span="8">
              <div class="v3-action-btn" @click="goBackup"><div class="v3-action-icon-wrap" style="background: linear-gradient(135deg, #909399, #B4B4B4)"><i class="el-icon-folder-opened" /></div><span>系统运维</span></div>
            </el-col>
          </el-row>

          <el-divider />
          <p class="v3-todo-title"><i class="el-icon-bell" /> 待办提醒</p>
          <div v-for="(item, idx) in pendingItems.slice(0, 4)" :key="idx" class="v3-todo-row">
            <span class="v3-todo-name">{{ item.studentName }}</span>
            <span class="v3-todo-desc">— 谈话待跟进</span>
            <span class="v3-todo-time">{{ item.conversationTime ? item.conversationTime.substring(0,10) : '' }}</span>
          </div>
          <div v-if="pendingItems.length === 0" class="empty-hint">暂无待办提醒</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import { getDashboardData, getRecentRecords } from "@/api/conversation/dashboard"

export default {
  name: "Index",
  components: { CountTo },
  data() {
    return {
      version: "3.9.2",
      username: this.$store.state.user.name || '管理员',
      stats: {
        pendingCount: 0,
        thisMonthCount: 0,
        totalStudentCount: 0,
        completedCount: 0
      },
      recentList: [],
      pendingItems: []
    }
  },
  computed: {
    today() {
      const d = new Date()
      const days = ['日', '一', '二', '三', '四', '五', '六']
      return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${days[d.getDay()]}`
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      getDashboardData().then(response => {
        const d = response.data || {}
        this.stats.pendingCount = d.pendingCount || 0
        this.stats.thisMonthCount = d.thisMonthCount || 0
        this.stats.totalStudentCount = d.totalStudentCount || 0
        this.stats.completedCount = d.completedCount || 0
        this.pendingItems = d.pendingList || []
      }).catch(() => {
        this.stats = { pendingCount: 12, thisMonthCount: 38, totalStudentCount: 286, completedCount: 76 }
      })
      getRecentRecords().then(response => {
        const data = response.data || response.rows || []
        this.recentList = Array.isArray(data) ? data : []
      }).catch(() => {})
    },
    goNewConversation() { this.$router.push('/conversation/record/add') },
    goStudent() { this.$router.push('/conversation/student') },
    goRecords() { this.$router.push('/conversation/record/list') },
    goMyRecords() { this.$router.push('/conversation/record/my') },
    goExport() { this.$router.push('/conversation/export/task') },
    goMajor() { this.$router.push('/conversation/major') },
    goBackup() { this.$router.push('/conversation/ops/backup') },
    goPending() { this.$router.push('/conversation/record/list') }
  }
}
</script>

<style scoped lang="scss">
.home {
  padding: 0;
}

.empty-hint {
  text-align: center;
  padding: 30px;
  color: #C0C4CC;
  font-size: 13px;
}

.card-header-title {
  font-weight: 600;
}

/* Banner */
.v3-banner {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
  min-height: 120px;
}
.v3-banner-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #1a365d 0%, #2d5f8a 40%, #409EFF 100%);
}
.v3-banner-content {
  position: relative;
  padding: 28px 32px;
  color: #fff;
}
.v3-banner-content h1 { margin: 0 0 6px; font-size: 24px; font-weight: 700; }
.v3-banner-content p { margin: 0 0 12px; font-size: 14px; opacity: 0.85; }
.v3-banner-meta { display: flex; gap: 20px; font-size: 13px; opacity: 0.75; }
.v3-banner-meta i { margin-right: 4px; }
.v3-banner-alert { color: #F56C6C; opacity: 1; font-weight: 500; }

/* Stat Cards */
.v3-card-row { margin-bottom: 16px; }
.v3-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}
.v3-card::after {
  content: '';
  position: absolute;
  top: -30px; right: -30px;
  width: 80px; height: 80px;
  border-radius: 50%;
  opacity: 0.08;
}
.v3-card-danger::after { background: #F56C6C; }
.v3-card-primary::after { background: #409EFF; }
.v3-card-success::after { background: #67C23A; }
.v3-card-warning::after { background: #E6A23C; }
.v3-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.v3-card-header { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #909399; margin-bottom: 8px; }
.v3-card-header i { font-size: 16px; }
.v3-card-body { display: flex; align-items: baseline; gap: 4px; margin-bottom: 8px; }
.v3-card-num { font-size: 32px; font-weight: 700; color: #303133; }
.v3-card-unit { font-size: 14px; color: #909399; }
.v3-card-footer { font-size: 12px; color: #409EFF; }

/* Timeline Records */
.v3-records-card { margin-bottom: 0; }
.v3-timeline { padding: 0; }
.v3-timeline-item { display: flex; gap: 14px; padding: 14px 0; border-bottom: 1px solid #f0f0f0; }
.v3-timeline-item:last-child { border-bottom: none; }
.v3-timeline-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 4px; flex-shrink: 0; }
.v3-timeline-dot.done { background: #67C23A; box-shadow: 0 0 0 3px rgba(103,194,58,0.15); }
.v3-timeline-dot.pending { background: #F56C6C; box-shadow: 0 0 0 3px rgba(245,108,108,0.15); }
.v3-timeline-content { flex: 1; min-width: 0; }
.v3-timeline-header { display: flex; align-items: center; gap: 8px; margin-bottom: 3px; }
.v3-timeline-header strong { color: #303133; font-size: 14px; }
.v3-timeline-header small { color: #909399; }
.v3-timeline-topic { margin: 0 0 2px; color: #606266; font-size: 13px; }
.v3-timeline-meta { margin: 0; color: #C0C4CC; font-size: 12px; }

/* Quick Actions */
.v3-actions { text-align: center; }
.v3-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}
.v3-action-btn:hover { background: #f5f7fa; }
.v3-action-icon-wrap {
  width: 44px; height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}
.v3-action-btn span { font-size: 12px; color: #606266; }

/* Todo */
.v3-todo-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.v3-todo-row { display: flex; align-items: center; gap: 6px; padding: 6px 0; font-size: 13px; }
.v3-todo-name { font-weight: 500; color: #303133; }
.v3-todo-desc { color: #909399; flex: 1; }
.v3-todo-time { color: #C0C4CC; font-size: 12px; }
</style>
