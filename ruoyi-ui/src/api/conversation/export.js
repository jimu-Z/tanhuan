import request from '@/utils/request'

export function listTask(query) {
    return request({
        url: '/conversation/export/list',
        method: 'get',
        params: query
    })
}

export function getTask(taskId) {
    return request({
        url: '/conversation/export/' + taskId,
        method: 'get'
    })
}

export function submitTask(data) {
    return request({
        url: '/conversation/export',
        method: 'post',
        data: data
    })
}

export function delTask(taskIds) {
    return request({
        url: '/conversation/export/' + taskIds,
        method: 'delete'
    })
}

export function downloadTask(taskId) {
    return request({
        url: '/conversation/export/download/' + taskId,
        method: 'get',
        responseType: 'blob'
    })
}