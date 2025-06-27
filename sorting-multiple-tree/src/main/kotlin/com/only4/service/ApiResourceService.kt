package com.only4.service

import com.only4.entity.ApiResource
import com.only4.synchronizer.SortingTreeSynchronizer.SyncResult
import com.only4.tree.SortingMultipleTree
import com.only4.tree.SortingTreeNode

interface ApiResourceService {
    fun getTree(rootKey: String = ""): SortingMultipleTree<String, ApiResource.ApiResourceInfo>

    fun applySyncResults(
        result: Collection<SyncResult<String, ApiResource.ApiResourceInfo>>, targetSelector: Int
    )

    /**
     * 更新资源
     */
    fun updateResource(resource: ApiResource): ApiResource

    /**
     * 创建新资源
     */
    fun createResource(resource: ApiResource): ApiResource

    /**
     * 删除资源
     */
    fun deleteResource(key: String): Boolean

    /**
     * 更新资源状态
     */
    fun updateResourceStatus(key: String, activeStatus: Boolean): Int

    /**
     * 批量删除
     */
    fun batchDelete(keys: List<String>): Boolean

    /**
     * 搜索资源
     */
    fun searchResources(query: String): List<ApiResource>

    /**
     * 获取可用的父节点列表
     */
    fun getAvailableParents(rootKey: String = ""): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>>

    fun calculateDifferences(
        sourceTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>
    ): List<SyncResult<String, ApiResource.ApiResourceInfo>>

    fun synchronizeTrees(
        sourceTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        resources: List<String>
    ): List<SyncResult<String, ApiResource.ApiResourceInfo>>

    fun moveNode(key: String, target: Map<String, Any>)
}
