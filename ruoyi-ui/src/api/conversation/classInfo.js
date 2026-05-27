import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

// 查询班级列表
export function listClass(query) {
  return request({
    url: '/conversation/class/list',
    method: 'get',
    params: query
  })
}

// 查询班级详细
export function getClass(classId) {
  return request({
    url: '/conversation/class/' + parseStrEmpty(classId),
    method: 'get'
  })
}

// 新增班级
export function addClass(data) {
  return request({
    url: '/conversation/class',
    method: 'post',
    data: data
  })
}

// 修改班级
export function updateClass(data) {
  return request({
    url: '/conversation/class',
    method: 'put',
    data: data
  })
}

// 删除班级
export function delClass(classIds) {
  return request({
    url: '/conversation/class/' + classIds,
    method: 'delete'
  })
}

// 查询专业树（用于班级关联专业）
export function majorTree() {
  return request({
    url: '/conversation/class/majorTree',
    method: 'get'
  })
}

// 查询班级下拉选项
export function optionSelect(query) {
  return request({
    url: '/conversation/class/optionselect',
    method: 'get',
    params: query
  })
}