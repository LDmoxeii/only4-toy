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
class ApiResourceServiceImpl : ServiceImpl<ApiResourceMapper, ApiResource>(), ApiResourceService {

    override fun getTree(tableSelector: Int): SortingMultipleTree<String, ApiResource.ApiResourceInfo> {

        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取所有根节点
            val all = this.findAllFromTable(tableSelector)

            return ApiResourceTree.buildFromResources(all)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
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
     * 获取指定表中的所有资源
     */
    fun findAllFromTable(tableSelector: Int): List<ApiResource> {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)
            return baseMapper.findAllFromTable(tableSelector)
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 更新资源
     */
    override fun updateResource(resource: ApiResource, tableSelector: Int): ApiResource {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 查找节点
            val node = tree.findNodeByKey(resource.key) as? ApiResource
                ?: throw IllegalArgumentException("Resource with id ${resource.key} not found")

            // 更新数据
            node.title = resource.title
            node.enTitle = resource.enTitle
            node.showStatus = resource.showStatus

            // 保存到数据库
            saveOrUpdate(node)

            return node
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 创建新资源
     */
    override fun createResource(resource: ApiResource, tableSelector: Int): ApiResource {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 设置表选择器
            resource.tableSelector = tableSelector

            // 添加到树中
            val node = tree.addNode(
                key = resource.key,
                parentKey = resource.parentKey,
                data = resource.data,
                sort = resource.sort
            ) as ApiResource

            saveBatch(tree.flattenTree().map { it as ApiResource })

            return node
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 删除资源
     */
    override fun deleteResource(id: String, tableSelector: Int): Boolean {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val originalTree = getTree(tableSelector)
            val tree = getTree(tableSelector)

            tree.removeNode(id)
            val deletedKey = originalTree.flattenTree()
                .filter { tree.findNodeByKey(it.key) == null }
                .map { it.key }
            batchDelete(deletedKey, tableSelector)
            saveOrUpdateBatch(tree.flattenTree().map { it as ApiResource })

            return false
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 更新资源状态
     */
    override fun updateResourceStatus(id: String, activeStatus: Boolean, tableSelector: Int): Int {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 查找节点
            val node = tree.findNodeByKey(id) as? ApiResource
                ?: throw IllegalArgumentException("Resource with id $id not found")

            val descendants = tree.getDescendants(node.key)

            // 更新状态
            (descendants + node)
                .forEach { it.data.activeStatus = activeStatus }

            // 保存到数据库
            saveOrUpdateBatch(descendants.map { it as ApiResource } + node)

            return (descendants + node).size
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 批量更新状态
     */
    override fun batchUpdateStatus(ids: List<String>, activeStatus: Boolean, tableSelector: Int): Int {
        if (ids.isEmpty()) return 0
        try {
            var total = 0
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            ids.forEach { total += updateResourceStatus(it, activeStatus, tableSelector) }
            return total
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 批量删除
     */
    override fun batchDelete(ids: List<String>, tableSelector: Int): Boolean {
        if (ids.isEmpty()) return true

        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            ids.forEach { deleteResource(it, tableSelector) }

            return true
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 搜索资源
     */
    override fun searchResources(query: String, tableSelector: Int): List<ApiResource> {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 构建搜索条件
            val wrapper = QueryWrapper<ApiResource>()
                .like("title", query)
                .or()
                .like("en_title", query)
                .or()
                .like("id", query)

            return list(wrapper)
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 获取可用的父节点列表
     */
    override fun getAvailableParents(tableSelector: Int): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>> {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 获取所有节点
            val allNodes = tree.flattenTree()

            return allNodes.map { it as ApiResource }
                .filter { it.activeStatus }

        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }
}
