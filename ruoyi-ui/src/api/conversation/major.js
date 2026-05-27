import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

// 查询专业列表
export function listMajor(query) {
  return request({
    url: '/conversation/major/list',
    method: 'get',
    params: query
  })
}

// 查询专业详细
export function getMajor(majorId) {
  return request({
    url: '/conversation/major/' + parseStrEmpty(majorId),
    method: 'get'
  })
}

// 新增专业
export function addMajor(data) {
  return request({
    url: '/conversation/major',
    method: 'post',
    data: data
  })
}

// 修改专业
export function updateMajor(data) {
  return request({
    url: '/conversation/major',
    method: 'put',
    data: data
  })
}

// 删除专业
export function delMajor(majorIds) {
  return request({
    url: '/conversation/major/' + majorIds,
    method: 'delete'
  })
}

// 查询专业部门树
export function deptTree() {
  return request({
    url: '/conversation/major/deptTree',
    method: 'get'
  })
}

// 查询专业下拉选项
export function optionSelect(query) {
  return request({
    url: '/conversation/major/optionselect',
    method: 'get',
    params: query
  })
}