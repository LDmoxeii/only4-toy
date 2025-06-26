import axios from 'axios'

// 获取资源树
export function getResourceTree(selector) {
    return axios.get('/resources/tree', {params: {selector}})
}

// 获取差异数据
export function getDifferences(sourceSelector, targetSelector) {
    return axios.get('/resources/getDifferences', {
        params: {sourceSelector, targetSelector}
    })
}

// 同步资源
export function syncResources(resources, sourceSelector, targetSelector) {
    return axios.post('/resources/sync', resources, {
        params: {sourceSelector, targetSelector}
    })
}

// 同步预览
export function previewSync(resources, sourceSelector, targetSelector) {
    return axios.post('/resources/preview-sync', resources, {
        params: {sourceSelector, targetSelector}
    })
}

// 更新资源
export function updateResource(id, resource, selector) {
    return axios.post(`/resources/${id}`, resource, {
        params: {selector}
    })
}

// 创建资源
export function createResource(resource, selector) {
    return axios.post('/resources', resource, {
        params: {selector}
    })
}

// 删除资源
export function deleteResource(id, selector) {
    return axios.delete(`/resources/${id}`, {
        params: {selector}
    })
}

// 更新资源状态
export function updateResourceStatus(id, activeStatus, selector) {
    return axios.patch(`/resources/${id}/status`, {}, {
        params: {selector, activeStatus}
    })
}

// 批量更新状态
export function batchUpdateStatus(ids, activeStatus, selector) {
    return axios.patch('/resources/batch/status', ids, {
        params: {selector, activeStatus}
    })
}

// 批量删除
export function batchDelete(ids, selector) {
    return axios.post('/resources/batch/delete', ids, {
        params: {selector}
    })
}

// 搜索资源
export function searchResources(query, selector) {
    return axios.get('/resources/search', {
        params: {selector, query}
    })
}

// 获取可用父节点
export function getAvailableParents(selector, excludeId) {
    return axios.get('/resources/parents', {
        params: {selector, excludeId}
    })
}
