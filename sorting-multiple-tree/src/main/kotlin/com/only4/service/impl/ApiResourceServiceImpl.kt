package com.only4.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.only4.config.ApiResourceTableNameHandler
import com.only4.entity.ApiResource
import com.only4.entity.ApiResourceTree
import com.only4.mapper.ApiResourceMapper
import com.only4.service.ApiResourceService
import com.only4.synchronizer.SortingTreeSynchronizer
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
        val root = getById(rootKey)
        val descendants = this.list(
            QueryWrapper<ApiResource>()
                .likeLeft("nodePath", root.nodePath)
        ).filter { it.nodePath != root.nodePath }

        return ApiResourceTree.buildFromResources(
            rootKey = root.key,
            resources = descendants
        )
    }


    override fun applySyncResults(
        result: Collection<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>>,
        targetSelector: Int
    ) {
        try {
            // 设置目标表选择器
            ApiResourceTableNameHandler.setTableSelector(targetSelector)


            SortingTreeSynchronizer.SyncType.ADD
            // 遍历所有同步结果
            for (syncResult in result) {
                when (syncResult.syncType) {
                    SortingTreeSynchronizer.SyncType.ADD -> {
                        // 处理添加操作
                        val node = syncResult.node
                        if (node is ApiResource) {
                            saveOrUpdate(node)
                        }
                    }

                    SortingTreeSynchronizer.SyncType.UPDATE -> {
                        // 处理更新操作
                        val node = syncResult.node
                        if (node is ApiResource) {
                            saveOrUpdate(node)
                        }
                    }

                    SortingTreeSynchronizer.SyncType.DELETE -> {
                        // 处理删除操作
                        val nodeKey = syncResult.node.key
                        // 从数据库中删除

                        removeById(nodeKey)
                    }

                    SortingTreeSynchronizer.SyncType.SAME -> continue
                }
            }
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
            sort = resource.sort
        ) as ApiResource

        saveBatch(tree.flattenTree().map { it as ApiResource })

        return node
    }

    /**
     * 删除资源
     */
    override fun deleteResource(key: String): Boolean {
        // 获取当前树
        val resource = getById(key)
        val originalTree = getTree(resource.parentKey)
        val tree = originalTree

        tree.removeNode(key)
        val deletedKey = originalTree.flattenTree()
            .filter { tree.findNodeByKey(it.key) == null }
            .map { it.key }
        // 删除数据库中的资源
        removeByIds(deletedKey)
        // 更新顺序
        saveOrUpdateBatch(tree.flattenTree().map { it as ApiResource })

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
    ): List<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>> {
        return apiResourceSynchronizer.calculateDifferences(sourceTree, targetTree)
    }

    override fun synchronizeTrees(
        sourceTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        targetTree: SortingMultipleTree<String, ApiResource.ApiResourceInfo>,
        resources: List<String>
    ): List<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>> {
        return apiResourceSynchronizer.synchronizeTrees(sourceTree, targetTree)
    }

    override fun moveNode(key: String, target: Map<String, Any>) {
        val resource = getById(key) ?: throw IllegalArgumentException("Resource with id $key not found")
        // 获取当前树
        if (resource.parentKey == target["parentKey"]) {
            val tree = getTree(resource.parentKey)
            tree.moveNode(resource, target["parentKey"] as String, target["sort"] as Long)
            saveOrUpdateBatch(tree.flattenTree().map { it as ApiResource })
            return
        }
        val sourceTree = getTree(resource.parentKey)
        val targetTree = getTree(target["parentKey"] as String)

        // 移动节点
        sourceTree.removeNode(key)
        targetTree.addNode(key, target["parentKey"] as String, resource.data, target["sort"] as Long)

        // 更新顺序
        removeById(key)
        saveOrUpdateBatch(sourceTree.flattenTree().map { it as ApiResource })
        saveOrUpdateBatch(targetTree.flattenTree().map { it as ApiResource })
    }
}
