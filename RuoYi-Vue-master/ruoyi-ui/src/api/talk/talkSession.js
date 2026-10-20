import request from '@/utils/request'
import { TALK_LABELS } from '@/utils/talkLabels'

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

// 下载接口的认证/业务错误也是 Blob，不能作为 Word 或 ZIP 保存。
async function validateExportFile(blob) {
  const type = (blob.type || '').split(';')[0].trim().toLowerCase()
  if (type === 'application/json' || type.endsWith('+json')) {
    let detail
    try {
      detail = JSON.parse(await blob.text())
    } catch (error) {
      throw new Error('导出失败，请重新登录后重试')
    }
    throw new Error(detail.msg || '导出失败，请重新登录后重试')
  }
  return blob
}

// 导出集体谈话汇总表
export function exportGroupSummary(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/exportGroupSummary/' + sessionId,
    method: 'get',
    responseType: 'blob'
  }).then(validateExportFile)
}

// 导出单个会话为.docx
export function exportDocx(sessionId) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/' + sessionId,
    method: 'get',
    responseType: 'blob'
  }).then(validateExportFile)
}

// 导出单个学生的谈话记录为.docx
export function exportDocxForStudent(sessionId, studentId) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/' + sessionId + '/student/' + studentId,
    method: 'get',
    responseType: 'blob'
  }).then(validateExportFile)
}

export function exportDocxForRecord(recordId) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/record/' + recordId,
    method: 'get',
    responseType: 'blob'
  }).then(validateExportFile)
}

// 批量导出会话为.docx（打包为zip）
export function exportDocxBatch(sessionIds) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/batch',
    method: 'post',
    data: { sessionIds: sessionIds },
    responseType: 'blob'
  }).then(validateExportFile)
}

// 按选中的学生谈话记录导出，避免扩大到整场会话。
export function exportDocxRecordsBatch(recordIds) {
  return request({
    url: '/ruoyi-system/talksession/exportDocx/records/batch',
    method: 'post',
    data: { recordIds },
    responseType: 'blob'
  }).then(validateExportFile)
}

// 标签字典映射（作为兜底，优先从数据库读取getLabels()）
export const TAG_LABELS = TALK_LABELS.tagValue
