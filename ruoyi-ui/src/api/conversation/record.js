import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

export function listRecord(query) {
  return request({
    url: '/conversation/record/list',
    method: 'get',
    params: query
  })
}

export function getRecord(recordId) {
  return request({
    url: '/conversation/record/' + parseStrEmpty(recordId),
    method: 'get'
  })
}

export function addRecord(data) {
  return request({
    url: '/conversation/record',
    method: 'post',
    data: data
  })
}

export function updateRecord(data) {
  return request({
    url: '/conversation/record',
    method: 'put',
    data: data
  })
}

export function delRecord(recordIds) {
  return request({
    url: '/conversation/record/' + recordIds,
    method: 'delete'
  })
}

export function exportRecord(query) {
  return request({
    url: '/conversation/record/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

export function listMyRecord(query) {
  return request({
    url: '/conversation/record/my',
    method: 'get',
    params: query
  })
}

export function addFollowUp(data) {
  return request({
    url: '/conversation/record/followup',
    method: 'post',
    data: data
  })
}

export function getFollowUpList(recordId) {
  return request({
    url: '/conversation/record/followup/' + recordId,
    method: 'get'
  })
}