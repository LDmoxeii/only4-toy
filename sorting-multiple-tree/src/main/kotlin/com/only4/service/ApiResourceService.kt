package com.only4.service

import com.only4.entity.ApiResource
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.tree.SortingMultipleTree
import com.only4.tree.SortingTreeNode

interface ApiResourceService {
    fun getTree(tableSelector: Int): SortingMultipleTree<String, ApiResource.ApiResourceInfo>

    fun applySyncResults(
        result: Collection<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>>,
        targetSelector: Int
    )

    /**
     * 更新资源
     */
    fun updateResource(resource: ApiResource, tableSelector: Int): ApiResource

    /**
     * 创建新资源
     */
    fun createResource(resource: ApiResource, tableSelector: Int): ApiResource

    /**
     * 删除资源
     */
    fun deleteResource(id: String, tableSelector: Int): Boolean

    /**
     * 更新资源状态
     */
    fun updateResourceStatus(id: String, activeStatus: Boolean, tableSelector: Int): ApiResource

    /**
     * 批量更新状态
     */
    fun batchUpdateStatus(ids: List<String>, activeStatus: Boolean, tableSelector: Int): Int

    /**
     * 批量删除
     */
    fun batchDelete(ids: List<String>, tableSelector: Int): Int

    /**
     * 搜索资源
     */
    fun searchResources(query: String, tableSelector: Int): List<ApiResource>

    /**
     * 获取可用的父节点列表
     */
    fun getAvailableParents(
        tableSelector: Int,
        excludeId: String? = null
    ): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>>
}
