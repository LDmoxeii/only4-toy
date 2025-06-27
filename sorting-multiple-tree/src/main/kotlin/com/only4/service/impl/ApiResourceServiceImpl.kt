package com.only4.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.only4.config.ApiResourceTableNameHandler
import com.only4.entity.ApiResource
import com.only4.entity.ApiResourceTree
import com.only4.mapper.ApiResourceMapper
import com.only4.service.ApiResourceService
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.synchronizer.SortingTreeSynchronizer.SyncResult
import com.only4.tree.SortingMultipleTree
import com.only4.tree.SortingTreeNode
import org.springframework.stereotype.Service

@Service
class ApiResourceServiceImpl(
    private val apiResourceSynchronizer: SortingTreeSynchronizer<String, ApiResource.ApiResourceInfo>
) : ServiceImpl<ApiResourceMapper, ApiResource>(), ApiResourceService {

    override fun getTree(rootKey: String): SortingMultipleTree<String, ApiResource.ApiResourceInfo> {
        if (rootKey == "") {
            // 如果没有指定根节点，则获取所有资源
            return ApiResourceTree.buildFromResources(resources = this.list())
        }
        val root = getById(rootKey) ?: throw IllegalArgumentException("Resource with id $rootKey not found")
        val descendants = this.list(
            QueryWrapper<ApiResource>()
                .likeRight("node_path", root.nodePath)
        ).filter { it.nodePath != root.nodePath }

        return ApiResourceTree.buildFromResources(
            root,
            resources = descendants
        )
    }


    override fun applySyncResults(
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetSelector: Int
    ) {
        try {
            // 设置目标表选择器
            ApiResourceTableNameHandler.setTableSelector(targetSelector)
            val (deleted, updated) = targetTree.flattenTree().map { it as ApiResource }.partition { it.deleted }
            removeBatchByIds(deleted)
            saveOrUpdateBatch(updated)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 更新资源
     */
    override fun updateResource(resource: ApiResource): ApiResource {
        // 保存到数据库
        saveOrUpdate(resource)
        return resource
    }

    /**
     * 创建新资源
     */
    override fun createResource(resource: ApiResource): ApiResource {
        // 获取当前树
        val tree = getTree(resource.parentKey)

        // 添加到树中
        val node = tree.addNode(
            key = resource.key,
            parentKey = resource.parentKey,
            data = resource.data,
        ) as ApiResource

        saveOrUpdateBatch(tree.flattenTree().map { it as ApiResource })

        return node
    }

    /**
     * 删除资源
     */
    override fun deleteResource(key: String): Boolean {
        // 获取当前树
        val resource = getById(key) ?: return true
        val tree = getTree(resource.parentKey)

        tree.removeNode(key)

        // 更新顺序
        removeBatchByIds(tree.flattenTree().map { it as ApiResource }.filter { it.deleted })

        return false
    }

    /**
     * 更新资源状态
     */
    override fun updateResourceStatus(key: String, activeStatus: Boolean): Int {
        val resource = getById(key) ?: throw IllegalArgumentException("Resource with id $key not found")
        // 获取当前树
        val tree = getTree(key)

        // 更新状态
        val descendants = tree.flattenTree()
        (descendants + resource)
            .forEach { it.data.activeStatus = activeStatus }

        // 保存到数据库
        saveOrUpdateBatch(descendants.map { it as ApiResource } + resource)

        return (descendants + resource).size
    }

    /**
     * 批量删除
     */
    override fun batchDelete(ids: List<String>): Boolean {
        if (ids.isEmpty()) return true

        ids.forEach { deleteResource(it) }

        return true
    }

    /**
     * 搜索资源
     */
    override fun searchResources(query: String): List<ApiResource> {
        // 构建搜索条件
        val wrapper = QueryWrapper<ApiResource>()
            .like("title", query)
            .or()
            .like("en_title", query)
            .or()
            .like("id", query)

        return list(wrapper)
    }

    /**
     * 获取可用的父节点列表
     */
    override fun getAvailableParents(rootKey: String): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>> {
        // 获取当前树
        val tree = getTree(rootKey)

        // 获取所有节点
        val allNodes = tree.flattenTree()

        return allNodes.map { it as ApiResource }
            .filter { it.activeStatus }
    }

    override fun calculateDifferences(
        sourceTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>
    ): List<SyncResult<String, ApiResource.ApiResourceInfo>> {
        return apiResourceSynchronizer.calculateDifferences(sourceTree, targetTree)
    }

    override fun synchronizeTrees(
        sourceTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        resources: Set<String>
    ): List<SyncResult<String, ApiResource.ApiResourceInfo>> {
        return apiResourceSynchronizer.synchronizeTrees(sourceTree, targetTree, resources)
    }

    override fun moveNode(key: String, target: Map<String, Any>) {
        val resource = getById(key) ?: throw IllegalArgumentException("Resource with id $key not found")

        val tree = getTree()

        tree.moveNode(resource, target["parentKey"] as String, (target["sort"] as Int).toLong())

        saveOrUpdateBatch(tree.flattenTree().map { it as ApiResource })
    }
}
