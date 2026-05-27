import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

// 查询学生列表
export function listStudent(query) {
  return request({
    url: '/conversation/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生详细
export function getStudent(studentId) {
  return request({
    url: '/conversation/student/' + parseStrEmpty(studentId),
    method: 'get'
  })
}

// 新增学生
export function addStudent(data) {
  return request({
    url: '/conversation/student',
    method: 'post',
    data: data
  })
}

// 修改学生
export function updateStudent(data) {
  return request({
    url: '/conversation/student',
    method: 'put',
    data: data
  })
}

// 删除学生
export function delStudent(studentIds) {
  return request({
    url: '/conversation/student/' + studentIds,
    method: 'delete'
  })
}

// 导入学生
export function importStudent(data) {
  return request({
    url: '/conversation/student/import',
    method: 'post',
    data: data
  })
}

// 导出学生
export function exportStudent(query) {
  return request({
    url: '/conversation/student/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 下载学生导入模板
export function getStudentTemplate() {
  return request({
    url: '/conversation/student/template',
    method: 'get',
    responseType: 'blob'
  })
}

// 查询学生下拉选项
export function optionSelect(query) {
  return request({
    url: '/conversation/student/optionselect',
    method: 'get',
    params: query
  })
}