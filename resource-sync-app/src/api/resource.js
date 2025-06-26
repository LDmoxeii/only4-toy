import axios from 'axios'

// 获取资源树
export function getResourceTree(selector) {
    return axios.post('/resources/tree', null, {params: {selector}})
}

// 获取差异数据
export function getDifferences(sourceSelector, targetSelector) {
    return axios.post('/resources/getDifferences', null, {
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
    return axios.post(`/resources/add/${id}`, resource, {
        params: {selector}
    })
}

// 创建资源
export function createResource(resource, selector) {
    // 创建资源对象的副本，避免修改原对象
    const resourceToSend = {...resource};

    // 移除relativeSortIndex字段，后端API可能不需要这个字段
    if ('relativeSortIndex' in resourceToSend) {
        // 如果sort值为0且存在relativeSortIndex，使用relativeSortIndex作为sort值
        if (resourceToSend.sort === 0 && resourceToSend.relativeSortIndex) {
            resourceToSend.sort = resourceToSend.relativeSortIndex;
        }
        delete resourceToSend.relativeSortIndex;
    }

    // 确保sort字段有值
    if (!resourceToSend.sort && resourceToSend.sort !== 0) {
        resourceToSend.sort = 1; // 默认值
    }

    return axios.post('/resources', resourceToSend, {
        params: {selector}
    })
}

// 删除资源
export function deleteResource(id, selector) {
    return axios.post(`/resources/delete/${id}`, null, {
        params: {selector}
    })
}

// 更新资源状态
export function updateResourceStatus(id, activeStatus, selector) {
    return axios.post(`/resources/${id}/status`, {}, {
        params: {selector, activeStatus}
    })
}

// 批量更新状态
export function batchUpdateStatus(ids, activeStatus, selector) {
    return axios.post('/resources/batch/status', ids, {
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
    return axios.post('/resources/search', null, {
        params: {selector, query}
    })
}

// 获取可用父节点
export function getAvailableParents(selector) {
    return axios.post('/resources/parents', null, {
        params: {selector}
    })
}
