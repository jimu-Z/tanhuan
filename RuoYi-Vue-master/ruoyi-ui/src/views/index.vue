<template>
  <div class="home-page">
    <section class="portal-hero">
      <div class="hero-copy">
        <div class="brand-mark"><i class="el-icon-chat-dot-round"></i><span>德州学院</span></div>
        <p class="hero-kicker">STUDENT CONVERSATION PLATFORM</p>
        <h1>学生谈心谈话管理平台</h1>
        <p class="hero-intro">以真诚沟通连接师生，用规范记录沉淀关怀，让每一次谈话都有温度、有依据。</p>
        <div class="hero-welcome"><strong>{{ greeting }}，{{ userName }}</strong><span>{{ roleLabel }} · {{ dateStr }}</span></div>
        <div class="hero-buttons">
          <el-button type="primary" icon="el-icon-chat-line-round" @click="go('/talk/initiate')">发起谈话</el-button>
          <el-button icon="el-icon-search" @click="go('/talk/query')">统一查询</el-button>
        </div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="orbit orbit-one"></div><div class="orbit orbit-two"></div>
        <div class="conversation-card card-back"><span></span><span></span><span></span></div>
        <div class="conversation-card card-front">
          <div class="visual-head"><i class="el-icon-chat-dot-round"></i><b>用心倾听</b></div>
          <div class="visual-line wide"></div><div class="visual-line"></div>
          <div class="visual-footer"><i class="el-icon-circle-check"></i> 记录每一次成长</div>
        </div>
      </div>
    </section>

    <section class="overview-strip" v-loading="loading">
      <div v-for="item in overview" :key="item.label" class="overview-item">
        <span class="overview-icon" :style="{ color: item.color, background: item.tint }"><i :class="item.icon"></i></span>
        <div><small>{{ item.label }}</small><strong>{{ item.value }}<em>{{ item.unit }}</em></strong></div>
      </div>
      <button class="overview-link" @click="go('/talk/dashboard')"><span>查看完整统计</span><i class="el-icon-right"></i></button>
    </section>

    <section class="workspace-section">
      <div class="section-heading"><div><span>WORKBENCH</span><h2>工作台</h2></div><p>从这里开始今天的谈心谈话工作</p></div>
      <div class="workspace-grid">
        <article v-for="(card, index) in cards" :key="card.title" class="workspace-card" :class="'tone-' + card.tone" @click="go(card.path)">
          <div class="card-number">0{{ index + 1 }}</div>
          <div class="workspace-icon"><i :class="card.icon"></i></div>
          <h3>{{ card.title }}</h3>
          <p>{{ card.desc }}</p>
          <span class="card-entry">进入功能 <i class="el-icon-right"></i></span>
        </article>
      </div>
    </section>

    <section class="home-bottom">
      <article class="recent-panel">
        <div class="panel-heading"><div><span>LATEST</span><h2>最近谈话动态</h2></div><el-button type="text" @click="go('/talk/query')">查看全部</el-button></div>
        <div v-if="recentActivities.length" class="recent-list">
          <div v-for="(item, index) in recentActivities.slice(0, 5)" :key="index" class="recent-row">
            <span class="recent-avatar">{{ (item.studentName || '?').slice(0, 1) }}</span>
            <div class="recent-copy"><strong>{{ item.studentName || '学生' }}</strong><p>{{ item.talkPerson || '未填写谈话人' }} · {{ $talkLabel('talkType', item.talkType) }}</p></div>
            <time>{{ formatDate(item.talkTime) }}</time>
          </div>
        </div>
        <div v-else class="empty-copy"><i class="el-icon-chat-line-round"></i><span>暂无谈话动态</span></div>
      </article>

      <article class="value-panel">
        <span class="value-kicker">OUR APPROACH</span>
        <h2>教育始于看见，成长源于对话</h2>
        <p>围绕学生、会话、记录、附件、标签与模板，形成清晰、可靠、可追溯的工作闭环。</p>
        <div class="value-points"><span><i class="el-icon-check"></i> 学院数据边界</span><span><i class="el-icon-check"></i> 谈话过程留痕</span><span><i class="el-icon-check"></i> 统计辅助决策</span></div>
      </article>
    </section>
  </div>
</template>

<script>
import auth from '@/plugins/auth'
import { getDashboard } from '@/api/talk/talkStatistics'

export default {
  name: 'Index',
  data() {
    const isGlobalAdmin = auth.hasRole('admin') || auth.hasRole('school_admin')
    const isCollegeAdmin = !isGlobalAdmin && auth.hasRole('college_admin')
    const commonCards = [
      { title: '学生信息', desc: `查看、筛选并维护${isGlobalAdmin ? '全校' : '本学院'}学生资料`, path: '/talk/student', icon: 'el-icon-user', tone: 'teal' },
      { title: '谈话管理', desc: '管理个别与集体谈话及相关记录', path: '/talk/talksession', icon: 'el-icon-chat-line-square', tone: 'blue' },
      { title: '统一查询', desc: '组合查询谈话记录与未谈学生', path: '/talk/query', icon: 'el-icon-search', tone: 'gold' },
      { title: '统计分析', desc: '查看谈话趋势、覆盖率与主题分布', path: '/talk/dashboard', icon: 'el-icon-data-analysis', tone: 'purple' },
      { title: '谈话标签', desc: '使用系统标签并维护学院标签', path: '/talk/talktag', icon: 'el-icon-collection-tag', tone: 'green' },
      { title: '谈话模板', desc: '沉淀并复用规范的谈话内容模板', path: '/talk/template', icon: 'el-icon-document', tone: 'rose' }
    ]
    const adminCards = [
      { title: '教师管理', desc: isGlobalAdmin ? '维护全校教师账号与教师资料' : '维护本学院教师账号与教师资料', path: '/talk/teacher', icon: 'el-icon-s-custom', tone: 'navy' },
      { title: '班级设置', desc: isGlobalAdmin ? '维护学院、年级与班级组织结构' : '维护本学院年级与班级组织结构', path: '/system/dept', icon: 'el-icon-office-building', tone: 'orange' }
    ]
    return {
      loading: false,
      dashboardData: {},
      recentActivities: [],
      roleLabel: isGlobalAdmin ? '校级管理工作台' : (isCollegeAdmin ? '院级管理工作台' : '教师工作台'),
      cards: [...commonCards, ...(isGlobalAdmin || isCollegeAdmin ? adminCards : [])]
    }
  },
  computed: {
    userName() { return (this.$store.state.user && this.$store.state.user.name) || '老师' },
    dateStr() {
      const date = new Date()
      return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    overview() {
      const data = this.dashboardData
      return [
        { label: '学生总数', value: data.totalStudents || 0, unit: '人', icon: 'el-icon-user', color: '#147c88', tint: '#e4f3f4' },
        { label: '谈话场次', value: data.totalSessions || 0, unit: '场', icon: 'el-icon-chat-dot-round', color: '#5470a7', tint: '#edf1f9' },
        { label: '已谈学生', value: data.coveredStudents || 0, unit: '人', icon: 'el-icon-circle-check', color: '#378864', tint: '#eaf5ef' },
        { label: '谈话覆盖率', value: data.coverageRate || 0, unit: '%', icon: 'el-icon-pie-chart', color: '#b57a2e', tint: '#fff4e5' }
      ]
    }
  },
  created() { this.loadOverview() },
  methods: {
    loadOverview() {
      this.loading = true
      getDashboard().then(res => {
        this.dashboardData = res.data || {}
        this.recentActivities = this.dashboardData.recentActivities || []
      }).catch(() => {
        this.dashboardData = {}
        this.recentActivities = []
      }).finally(() => { this.loading = false })
    },
    go(path) { this.$router.push(path) },
    formatDate(value) { return value ? String(value).replace('T', ' ').substring(5, 16) : '-' }
  }
}
</script>

<style lang="scss" scoped>
.home-page { min-height: calc(100vh - 84px); padding: 24px; color: #17324d; background: #f2f5f6; }
.portal-hero { position: relative; overflow: hidden; display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(330px, .75fr); min-height: 360px; border-radius: 22px; color: #fff; background: linear-gradient(118deg, #0e3d51 0%, #0f6675 57%, #3a917e 100%); box-shadow: 0 20px 46px rgba(17, 76, 87, .22); }
.portal-hero::after { content: ''; position: absolute; top: -170px; right: -80px; width: 440px; height: 440px; border: 76px solid rgba(255,255,255,.055); border-radius: 50%; }
.hero-copy { position: relative; z-index: 2; padding: 42px 48px; }
.brand-mark { display: inline-flex; align-items: center; gap: 9px; padding: 7px 12px; border: 1px solid rgba(255,255,255,.18); border-radius: 999px; background: rgba(255,255,255,.08); font-size: 13px; }
.brand-mark i { color: #dcefae; font-size: 17px; }
.hero-kicker { margin: 28px 0 7px; font-size: 10px; letter-spacing: 3px; opacity: .58; }
.hero-copy h1 { margin: 0; font-family: "Microsoft YaHei", sans-serif; font-size: 35px; font-weight: 600; letter-spacing: 3px; }
.hero-intro { max-width: 610px; margin: 18px 0 22px; color: rgba(255,255,255,.76); font-size: 14px; line-height: 1.9; }
.hero-welcome { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.hero-welcome strong { font-size: 16px; }
.hero-welcome span { font-size: 12px; opacity: .63; }
.hero-buttons { display: flex; gap: 10px; }
.hero-buttons .el-button { min-width: 122px; border-color: rgba(255,255,255,.22); color: #fff; background: rgba(255,255,255,.08); }
.hero-buttons .el-button--primary { border-color: #d8eaa5; color: #234e4d; background: #d8eaa5; }
.hero-visual { position: relative; z-index: 1; min-height: 300px; }
.orbit { position: absolute; border: 1px solid rgba(255,255,255,.11); border-radius: 50%; }
.orbit-one { top: 44px; right: 70px; width: 250px; height: 250px; }
.orbit-two { top: 93px; right: 120px; width: 150px; height: 150px; }
.conversation-card { position: absolute; width: 235px; height: 155px; border: 1px solid rgba(255,255,255,.22); border-radius: 18px; backdrop-filter: blur(13px); }
.card-back { top: 82px; right: 112px; padding: 35px; transform: rotate(-9deg); background: rgba(255,255,255,.08); }
.card-back span { display: block; width: 70%; height: 7px; margin: 10px 0; border-radius: 6px; background: rgba(255,255,255,.17); }
.card-front { top: 128px; right: 54px; padding: 23px; transform: rotate(4deg); background: rgba(245,255,250,.17); box-shadow: 0 20px 40px rgba(4,47,55,.24); }
.visual-head { display: flex; align-items: center; gap: 8px; }
.visual-head i { color: #e3efb6; font-size: 22px; }
.visual-head b { font-size: 14px; }
.visual-line { width: 67%; height: 7px; margin-top: 16px; border-radius: 8px; background: rgba(255,255,255,.28); }
.visual-line.wide { width: 92%; }
.visual-footer { position: absolute; right: 21px; bottom: 18px; color: rgba(255,255,255,.75); font-size: 10px; }
.visual-footer i { margin-right: 4px; color: #ddecaa; }
.overview-strip { display: grid; grid-template-columns: repeat(4, 1fr) 180px; align-items: stretch; margin: 18px 0 30px; border: 1px solid #e3eaed; border-radius: 16px; background: #fff; box-shadow: 0 8px 22px rgba(30,62,74,.05); }
.overview-item { display: flex; align-items: center; gap: 12px; min-height: 94px; padding: 18px 22px; border-right: 1px solid #edf1f3; }
.overview-icon { display: flex; align-items: center; justify-content: center; flex: 0 0 39px; height: 39px; border-radius: 11px; }
.overview-item small { display: block; margin-bottom: 6px; color: #8496a2; font-size: 11px; }
.overview-item strong { color: #244359; font-size: 22px; }
.overview-item em { margin-left: 3px; color: #93a2ac; font-size: 10px; font-style: normal; font-weight: normal; }
.overview-link { border: 0; color: #fff; background: #173f53; cursor: pointer; font-size: 12px; }
.overview-link i { display: block; margin: 8px auto 0; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; margin: 0 2px 15px; }
.section-heading span, .panel-heading span { color: #9aa9b2; font-size: 9px; letter-spacing: 2px; }
.section-heading h2, .panel-heading h2 { margin: 4px 0 0; color: #26455a; font-size: 19px; }
.section-heading p { margin: 0; color: #8b9aa4; font-size: 12px; }
.workspace-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.workspace-card { position: relative; overflow: hidden; min-height: 190px; padding: 24px; border: 1px solid #e4ebee; border-radius: 16px; background: #fff; box-shadow: 0 7px 20px rgba(29,61,75,.045); cursor: pointer; transition: .25s ease; }
.workspace-card:hover { border-color: var(--tone); box-shadow: 0 14px 28px rgba(29,61,75,.11); transform: translateY(-4px); }
.card-number { position: absolute; right: 18px; top: 15px; color: #edf1f3; font: 700 34px Georgia, serif; }
.workspace-icon { display: flex; align-items: center; justify-content: center; width: 43px; height: 43px; margin-bottom: 18px; border-radius: 12px; color: var(--tone); background: var(--tint); font-size: 19px; }
.workspace-card h3 { margin: 0 0 8px; color: #29485d; font-size: 15px; }
.workspace-card p { min-height: 36px; margin: 0 0 16px; color: #91a0a9; font-size: 11px; line-height: 1.65; }
.card-entry { color: var(--tone); font-size: 11px; }
.card-entry i { transition: .2s; }
.workspace-card:hover .card-entry i { transform: translateX(4px); }
.tone-teal { --tone: #147c88; --tint: #e4f3f4; }.tone-blue { --tone: #5470a7; --tint: #edf1f9; }.tone-gold { --tone: #bd7c25; --tint: #fff3e2; }.tone-purple { --tone: #82629a; --tint: #f2edf5; }.tone-green { --tone: #3c8969; --tint: #eaf5ef; }.tone-rose { --tone: #b46163; --tint: #f9eded; }.tone-navy { --tone: #365d76; --tint: #eaf0f4; }.tone-orange { --tone: #bd6e3a; --tint: #faeee6; }
.home-bottom { display: grid; grid-template-columns: minmax(0, 1.15fr) minmax(340px, .85fr); gap: 18px; margin-top: 28px; }
.recent-panel, .value-panel { overflow: hidden; min-height: 280px; border-radius: 17px; }
.recent-panel { padding: 21px 24px; border: 1px solid #e3eaed; background: #fff; box-shadow: 0 8px 22px rgba(30,62,74,.05); }
.panel-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.recent-row { display: grid; grid-template-columns: 36px minmax(0,1fr) auto; align-items: center; gap: 11px; padding: 11px 0; border-bottom: 1px solid #f0f3f4; }
.recent-row:last-child { border-bottom: 0; }
.recent-avatar { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 10px; color: #147c88; background: #e6f3f4; font-size: 12px; font-weight: 700; }
.recent-copy strong { color: #405d70; font-size: 12px; }.recent-copy p { margin: 3px 0 0; color: #9aa8b1; font-size: 10px; }.recent-row time { color: #91a2ad; font-size: 10px; }
.empty-copy { display: flex; align-items: center; justify-content: center; flex-direction: column; height: 190px; color: #a2b0b9; font-size: 12px; }.empty-copy i { margin-bottom: 8px; color: #cbd6dc; font-size: 28px; }
.value-panel { position: relative; padding: 37px; color: #fff; background: linear-gradient(135deg, #183f52, #2d6c68); }
.value-panel::after { content: '“'; position: absolute; right: 30px; top: -24px; color: rgba(255,255,255,.07); font: 180px Georgia, serif; }
.value-kicker { color: #d7e9a6; font-size: 9px; letter-spacing: 3px; }
.value-panel h2 { max-width: 360px; margin: 22px 0 14px; font: 500 23px/1.5 "Microsoft YaHei", sans-serif; letter-spacing: 1px; }
.value-panel p { max-width: 500px; color: rgba(255,255,255,.66); font-size: 12px; line-height: 1.9; }
.value-points { display: flex; flex-wrap: wrap; gap: 9px; margin-top: 27px; }.value-points span { padding: 7px 10px; border: 1px solid rgba(255,255,255,.13); border-radius: 999px; color: rgba(255,255,255,.75); font-size: 10px; }.value-points i { margin-right: 4px; color: #d8e9a7; }
@media (max-width: 1380px) { .workspace-grid { grid-template-columns: repeat(3, 1fr); }.overview-strip { grid-template-columns: repeat(4, 1fr); }.overview-link { grid-column: 1 / -1; min-height: 52px; }.overview-link i { display: inline-block; margin: 0 0 0 7px; } }
@media (max-width: 900px) { .portal-hero { grid-template-columns: 1fr; }.hero-visual { display: none; }.workspace-grid { grid-template-columns: repeat(2, 1fr); }.home-bottom { grid-template-columns: 1fr; }.overview-strip { grid-template-columns: repeat(2, 1fr); }.overview-item:nth-child(2) { border-right: 0; } }
@media (max-width: 600px) { .home-page { padding: 14px; }.hero-copy { padding: 28px 22px; }.hero-copy h1 { font-size: 27px; }.hero-welcome { align-items: flex-start; flex-direction: column; gap: 4px; }.overview-strip, .workspace-grid { grid-template-columns: 1fr; }.overview-item { border-right: 0; border-bottom: 1px solid #edf1f3; }.section-heading { align-items: flex-start; flex-direction: column; gap: 5px; }.home-bottom { margin-top: 20px; } }
</style>
