import request from '@/utils/request'

export function listBackup(query) {
    return request({
        url: '/conversation/backup/list',
        method: 'get',
        params: query
    })
}

export function manualBackup() {
    return request({
        url: '/conversation/backup/manual',
        method: 'post'
    })
}

export function delBackup(backupIds) {
    return request({
        url: '/conversation/backup/' + backupIds,
        method: 'delete'
    })
}

export function downloadBackup(backupId) {
    return request({
        url: '/conversation/backup/download/' + backupId,
        method: 'get',
        responseType: 'blob'
    })
}