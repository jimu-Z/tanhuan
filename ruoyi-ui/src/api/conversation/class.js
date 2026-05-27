import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

export function listClass(query) {
  return request({
    url: '/conversation/class/list',
    method: 'get',
    params: query
  })
}

export function getClass(classId) {
  return request({
    url: '/conversation/class/' + parseStrEmpty(classId),
    method: 'get'
  })
}

export function addClass(data) {
  return request({
    url: '/conversation/class',
    method: 'post',
    data: data
  })
}

export function updateClass(data) {
  return request({
    url: '/conversation/class',
    method: 'put',
    data: data
  })
}

export function delClass(classIds) {
  return request({
    url: '/conversation/class/' + classIds,
    method: 'delete'
  })
}

export function optionSelect(query) {
  return request({
    url: '/conversation/class/optionselect',
    method: 'get',
    params: query
  })
}