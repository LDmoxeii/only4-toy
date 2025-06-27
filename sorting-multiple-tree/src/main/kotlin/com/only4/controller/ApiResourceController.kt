package com.only4.controller

import com.only4.config.ApiResourceTableNameHandler
import com.only4.entity.ApiResource
import com.only4.service.ApiResourceService
import com.only4.synchronizer.SortingTreeSynchronizer.SyncResult
import com.only4.tree.SortingMultipleTree
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/resources")
class ApiResourceController(
    private val apiResourceService: ApiResourceService,
) {

    /**
     * 获取资源树
     */
    @PostMapping("/tree")
    fun getResourceTree(
        @RequestParam selector: Int,
        @RequestParam(required = false) rootKey: String = ""
    ): ResponseEntity<List<ApiResource>> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val tree = apiResourceService.getTree(rootKey)
            return ResponseEntity(tree.flattenTree().map { it as ApiResource }, HttpStatus.OK)

        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }

    }

    private fun getSourceAndTargetTrees(
        sourceSelector: Int,
        targetSelector: Int
    ): Pair<SortingMultipleTree<String, ApiResource.ApiResourceInfo>, SortingMultipleTree<String, ApiResource.ApiResourceInfo>> {
        // 获取源表树
        val sourceTask = CompletableFuture.supplyAsync {
            try {
                // 设置表选择器
                ApiResourceTableNameHandler.setTableSelector(sourceSelector)
                apiResourceService.getTree()
            } finally {
                // 清理线程变量
                ApiResourceTableNameHandler.clear()
            }
        }

        // 获取目标表树
        val targetTask = CompletableFuture.supplyAsync {
            try {
                // 设置表选择器
                ApiResourceTableNameHandler.setTableSelector(targetSelector)
                apiResourceService.getTree()
            } finally {
                // 清理线程变量
                ApiResourceTableNameHandler.clear()
            }
        }

        // 等待两个任务完成
        CompletableFuture.allOf(sourceTask, targetTask).join()
        val sourceTree = sourceTask.get()
        val targetTree = targetTask.get()
        return Pair(sourceTree, targetTree)
    }

    /**
     * 获取资源树的差异
     */
    @PostMapping("/getDifferences")
    fun getDifferences(
        @RequestParam sourceSelector: Int,
        @RequestParam targetSelector: Int,
    ): ResponseEntity<List<SyncResult<String, ApiResource.ApiResourceInfo>>> {
        // 获取源和目标表树
        val (sourceTree, targetTree) = getSourceAndTargetTrees(sourceSelector, targetSelector)

        // 计算差异
        val differences = apiResourceService.calculateDifferences(sourceTree, targetTree)
        return ResponseEntity(differences, HttpStatus.OK)
    }


    /**
     * 同步资源
     */
    @PostMapping("/sync")
    fun syncResources(
        @RequestParam sourceSelector: Int,
        @RequestParam targetSelector: Int,
        @RequestBody resources: Set<String>
    ): ResponseEntity<String> {
        // 获取源和目标表树
        val (sourceTree, targetTree) = getSourceAndTargetTrees(sourceSelector, targetSelector)

        // 同步两个树之间的数据
        val syncResults = apiResourceService.synchronizeTrees(
            sourceTree = sourceTree,
            targetTree = targetTree,
            resources
        )

        // 将同步结果应用到数据库
        apiResourceService.applySyncResults(targetTree, targetSelector)

        return ResponseEntity(
            "Resources synchronized successfully. ${syncResults.size} changes applied.",
            HttpStatus.OK
        )
    }

    /**
     * 同步预览 - 预览将要进行的同步操作但不实际执行
     */
    @PostMapping("/preview-sync")
    fun previewSync(
        @RequestParam sourceSelector: Int,
        @RequestParam targetSelector: Int,
        @RequestBody resources: Set<String>
    ): ResponseEntity<List<SyncResult<String, ApiResource.ApiResourceInfo>>> {
        // 获取源和目标表树
        val (sourceTree, targetTree) = getSourceAndTargetTrees(sourceSelector, targetSelector)

        // 同步两个树之间的数据
        val syncResults = apiResourceService.synchronizeTrees(
            sourceTree = sourceTree,
            targetTree = targetTree,
            resources
        )

        return ResponseEntity(syncResults, HttpStatus.OK)
    }

    /**
     * 更新单个资源
     */
    @PostMapping("add/{id}")
    fun updateResource(
        @PathVariable id: String,
        @RequestParam selector: Int,
        @RequestBody resource: ApiResource
    ): ResponseEntity<ApiResource> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val updatedResource = apiResourceService.updateResource(resource)
            return ResponseEntity(updatedResource, HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 创建新资源
     */
    @PostMapping
    fun createResource(
        @RequestParam selector: Int,
        @RequestBody resource: ApiResource
    ): ResponseEntity<ApiResource> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val createdResource = apiResourceService.createResource(resource)
            return ResponseEntity(createdResource, HttpStatus.CREATED)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }

    }

    /**
     * 删除资源
     */
    @PostMapping("delete/{key}")
    fun deleteResource(
        @PathVariable key: String,
        @RequestParam selector: Int
    ): ResponseEntity<Unit> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            apiResourceService.deleteResource(key)
            return ResponseEntity(HttpStatus.NO_CONTENT)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }

    }

    /**
     * 更新资源状态（启用/停用）
     */
    @PostMapping("/{key}/status")
    fun updateResourceStatus(
        @PathVariable key: String,
        @RequestParam selector: Int,
        @RequestParam activeStatus: Boolean
    ): ResponseEntity<Int> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val updatedResource = apiResourceService.updateResourceStatus(key, activeStatus)
            return ResponseEntity(updatedResource, HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 批量删除资源
     */
    @PostMapping("/batch/delete")
    fun batchDelete(
        @RequestParam selector: Int,
        @RequestBody keys: List<String>
    ): ResponseEntity<Boolean> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val result = apiResourceService.batchDelete(keys)
            return ResponseEntity(result, HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 搜索资源
     */
    @PostMapping("/search")
    fun searchResources(
        @RequestParam selector: Int,
        @RequestParam query: String
    ): ResponseEntity<List<ApiResource>> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val resources = apiResourceService.searchResources(query)
            return ResponseEntity(resources, HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 获取可用的父节点列表（用于下拉框选择）
     */
    @PostMapping("/parents")
    fun getAvailableParents(
        @RequestParam selector: Int,
    ): ResponseEntity<List<Map<String, Any>>> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            val parents = apiResourceService.getAvailableParents()
            // 转换为前端下拉框友好的格式
            val result = parents.map {
                mapOf(
                    "value" to it.key,
                    "label" to it.data.title,
                    "path" to it.nodePath,
                    "sort" to it.sort,
                )
            }
            return ResponseEntity(result, HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }

    /**
     * 节点移动保存
     *
     * @param key 节点的唯一标识符
     * @param selector 表选择器，用于区分不同的资源表
     * @param target 目标节点信息，包含父节点和排序等信息
     */
    @PostMapping("/move/{key}")
    fun moveNode(
        @PathVariable key: String,
        @RequestParam selector: Int,
        @RequestBody target: Map<String, Any>
    ): ResponseEntity<Unit> {
        try {
            // 设置表选择器
            ApiResourceTableNameHandler.setTableSelector(selector)
            apiResourceService.moveNode(key, target)
            return ResponseEntity(HttpStatus.OK)
        } finally {
            // 清理线程变量
            ApiResourceTableNameHandler.clear()
        }
    }
}
