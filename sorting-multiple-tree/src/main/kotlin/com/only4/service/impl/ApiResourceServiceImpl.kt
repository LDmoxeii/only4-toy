package com.only4.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.only4.config.ApiResourceTableNameHandler
import com.only4.entity.ApiResource
import com.only4.entity.ApiResourceTree
import com.only4.mapper.ApiResourceMapper
import com.only4.service.ApiResourceService
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.tree.SortingMultipleTree
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
}
