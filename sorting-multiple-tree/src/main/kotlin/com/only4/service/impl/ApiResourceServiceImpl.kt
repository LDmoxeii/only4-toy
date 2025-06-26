package com.only4.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
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

                        val wrapper = lambdaQuery().eq(ApiResource::key, nodeKey)
                        remove(wrapper)
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

            // 如果父节点或排序发生变化，需要移动节点
            if (node.parentKey != resource.parentKey || node.sort != resource.sort) {
                tree.moveNode(node, resource.parentKey, resource.sort)
            }

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

            // 保存到数据库
            saveOrUpdate(node)

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
            val tree = getTree(tableSelector)

            // 从树中删除节点
            if (tree.removeNode(id)) {
                // 从数据库中删除节点及其子节点
                val wrapper = LambdaQueryWrapper<ApiResource>()
                    .likeRight(ApiResource::nodePath, "$id/")
                    .or()
                    .eq(ApiResource::key, id)

                return remove(wrapper)
            }

            return false
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 更新资源状态
     */
    override fun updateResourceStatus(id: String, activeStatus: Boolean, tableSelector: Int): ApiResource {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 查找节点
            val node = tree.findNodeByKey(id) as? ApiResource
                ?: throw IllegalArgumentException("Resource with id $id not found")

            // 更新状态
            node.activeStatus = activeStatus

            // 保存到数据库
            saveOrUpdate(node)

            return node
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
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 构建条件
            val wrapper = LambdaQueryWrapper<ApiResource>()
                .`in`(ApiResource::key, ids)

            // 更新状态
            val entity = ApiResource()
            entity.activeStatus = activeStatus

            return baseMapper.update(entity, wrapper)
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 批量删除
     */
    override fun batchDelete(ids: List<String>, tableSelector: Int): Int {
        if (ids.isEmpty()) return 0

        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 删除的计数
            var count = 0

            // 从树中删除节点
            for (id in ids) {
                if (tree.removeNode(id)) {
                    count++
                }
            }

            // 从数据库中删除节点及其子节点
            if (count > 0) {
                // 构建多个条件，删除节点及其子节点
                val wrapper = LambdaQueryWrapper<ApiResource>()

                // 添加节点自身条件
                wrapper.`in`(ApiResource::key, ids)

                // 添加子节点条件
                for (id in ids) {
                    wrapper.or().likeRight(ApiResource::nodePath, "$id/")
                }

                remove(wrapper)
            }

            return count
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
            val wrapper = LambdaQueryWrapper<ApiResource>()
                .like(ApiResource::title, query)
                .or()
                .like(ApiResource::enTitle, query)
                .or()
                .like(ApiResource::key, query)

            return list(wrapper)
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 获取可用的父节点列表
     */
    override fun getAvailableParents(
        tableSelector: Int,
        excludeId: String?
    ): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>> {
        try {
            ApiResourceTableNameHandler.setTableSelector(tableSelector)

            // 获取当前树
            val tree = getTree(tableSelector)

            // 获取所有节点
            val allNodes = tree.flattenTree()

            // 如果需要排除特定节点及其子节点
            return if (excludeId != null) {
                // 查找要排除的节点
                val excludeNode = tree.findNodeByKey(excludeId)

                if (excludeNode != null) {
                    // 获取要排除的节点路径
                    val excludePath = excludeNode.nodePath

                    // 过滤掉要排除的节点及其所有子节点
                    allNodes.filter { node ->
                        !node.key.equals(excludeId) && !node.nodePath.startsWith("$excludePath/")
                    }
                } else {
                    allNodes
                }
            } else {
                allNodes
            }
        } finally {
            ApiResourceTableNameHandler.clear()
        }
    }
}
