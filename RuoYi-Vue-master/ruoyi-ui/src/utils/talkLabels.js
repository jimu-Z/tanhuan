// 展示时翻译业务编码，提交和筛选仍使用原始值。
export const TALK_LABELS = {
  gender: { '0': '男', '1': '女', '2': '未知', male: '男', female: '女', unknown: '未知' },
  politicalStatus: { people: '群众', league_member: '共青团员', probationary_member: '中共预备党员', party_member: '中共党员', other: '其他' },
  povertyLevel: { none: '无', general: '家庭经济一般困难', difficult: '家庭经济困难', severe: '家庭经济特别困难' },
  mentalHealthStatus: {
    healthy: '健康', mild_warning: '轻度预警', moderate_warning: '中度预警', severe_warning: '重度预警',
    normal: '正常', weekly_track: '周跟踪', monthly_track: '月跟踪', '0': '正常', '1': '关注', '2': '重点关注'
  },
  enrollmentStatus: { active: '在读', suspended: '休学', withdrawn: '退学', graduated: '毕业', '0': '在读', '1': '休学', '2': '退学' },
  followupStatus: { none: '无需跟进', pending: '待跟进', in_progress: '跟进中', completed: '已完成' },
  talkType: { individual: '个别谈话', group: '集体谈话' },
  alertType: { auto: '自动预警', manual: '手动预警', keyword: '关键词预警' },
  alertLevel: { red: '红色预警', orange: '橙色预警', yellow: '黄色预警' },
  alertStatus: { pending: '待处理', in_progress: '处理中', resolved: '已解除' },
  appointmentStatus: { pending: '待确认', confirmed: '已确认', rejected: '已拒绝', completed: '已完成', cancelled: '已取消' },
  roleKey: { admin: '管理员', school_admin: '校管理员', college_admin: '院管理员', talk_teacher: '老师' },
  templateType: { system: '系统预置', college: '学院模板', personal: '学院模板' },
  scopeType: { system: '系统内置', college: '学院标签' },
  redisMode: { standalone: '单机', cluster: '集群', sentinel: '哨兵' },
  redisSaveStatus: { ok: '成功', err: '失败' },
  tagValue: {
    thought_education: '思想理论教育和价值引领', party_class: '党团和班级建设', study_style: '学风建设',
    daily_affairs: '日常事务', mental_health: '心理健康教育与咨询', crisis_response: '危机事件应对', career_guidance: '职业规划与就业创业指导'
  }
}

export function talkLabel(field, value, options = []) {
  if (value == null || String(value).trim() === '') return '未设置'
  const key = String(value).trim()
  const selected = (options || []).find(item => String(item.dictValue == null ? item.value : item.dictValue) === key)
  const label = selected && (selected.dictLabel || selected.label)
  if (label) return label
  const labels = TALK_LABELS[field] || {}
  if (Object.prototype.hasOwnProperty.call(labels, key)) return labels[key]
  // 历史导入可能直接存储中文，保留其含义，不把未知英文当成翻译结果。
  if (/[\u3400-\u9fff]/.test(key)) return key
  return field === 'tagValue' ? '未知标签' : '未知状态'
}

export function talkTagLabels(value, labels = {}) {
  if (value == null || value === '') return ''
  let parsed = value
  if (typeof value === 'string') {
    try { parsed = JSON.parse(value) } catch (e) { /* 历史普通字符串 */ }
  }
  const values = Array.isArray(parsed) ? parsed : String(parsed).split(',')
  return values.filter(item => item != null && String(item).trim()).map(item => {
    const key = String(item).trim()
    return Object.prototype.hasOwnProperty.call(labels, key) ? labels[key] : talkLabel('tagValue', key)
  }).join('、')
}
