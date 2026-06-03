import request from '@/utils/request'

// 查询谈话会话管理列表
export function listTalksession(query) {
  return request({
    url: '/ruoyi-system/talksession/list',
    method: 'get',
    params: query
  })
}

// 查询谈话会话管理详细
export function getTalksession(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/detail/' + sessionId,
    method: 'get'
  })
}

// 新增谈话会话管理
export function addTalksession(data) {
  return request({
    url: '/ruoyi-system/talksession',
    method: 'post',
    data: data
  })
}

// 修改谈话会话管理
export function updateTalksession(data) {
  return request({
    url: '/ruoyi-system/talksession',
    method: 'put',
    data: data
  })
}

// 删除谈话会话管理
export function delTalksession(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/' + sessionId,
    method: 'delete'
  })
}

// 获取会话标签
export function getSessionTags(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/tags/' + sessionId,
    method: 'get'
  })
}

// 获取集体谈话参与人
export function getParticipants(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/participants/' + sessionId,
    method: 'get'
  })
}

// 批量获取会话标签
export function getBatchTags(sessionIds) {
  return request({
    url: '/ruoyi-system/talksession/session-tags/batch',
    method: 'get',
    params: { sessionIds: sessionIds.join(',') }
  })
}

// 导出集体谈话汇总表
export function exportGroupSummary(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/exportGroupSummary/' + sessionId,
    method: 'get',
    responseType: 'blob'
  })
}

// 导出单个会话为.docx
export function exportDocx(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/' + sessionId,
    method: 'get',
    responseType: 'blob'
  })
}

// 导出单个学生的谈话记录为.docx
export function exportDocxForStudent(sessionId, studentId) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/' + sessionId + '/student/' + studentId,
    method: 'get',
    responseType: 'blob'
  })
}

// 批量导出会话为.docx（打包为zip）
export function exportDocxBatch(sessionIds) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/batch',
    method: 'post',
    data: { sessionIds: sessionIds },
    responseType: 'blob'
  })
}

// 标签字典映射（作为兜底，优先从数据库读取getLabels()）
export const TAG_LABELS = {
  thought_education: '思想理论教育和价值引领',
  party_class: '党团和班级建设',
  study_style: '学风建设',
  daily_affairs: '日常事务',
  mental_health: '心理健康教育与咨询',
  crisis_response: '危机事件应对',
  career_guidance: '职业规划与就业创业指导'
}
