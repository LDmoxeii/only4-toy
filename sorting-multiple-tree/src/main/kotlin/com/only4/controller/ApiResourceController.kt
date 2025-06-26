package com.only4.controller

import com.only4.entity.ApiResource
import com.only4.service.ApiResourceService
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.synchronizer.SortingTreeSynchronizer.SyncResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/resources")
class ApiResourceController(
    private val apiResourceService: ApiResourceService,
    private val apiResourceSynchronizer: SortingTreeSynchronizer<String, ApiResource.ApiResourceInfo>
) {

    /**
     * 获取资源树
     */
    @PostMapping("/tree")
    fun getResourceTree(
        @RequestParam selector: Int
    ): ResponseEntity<List<ApiResource>> {
        val tree = apiResourceService.getTree(selector)
        return ResponseEntity(tree.flattenTree().map { it as ApiResource }, HttpStatus.OK)
    }

    /**
     * 获取资源树的差异
     */
    @PostMapping("/getDifferences")
    fun getDifferences(
        @RequestParam sourceSelector: Int,
        @RequestParam targetSelector: Int,
    ): ResponseEntity<List<SyncResult<String, ApiResource.ApiResourceInfo>>> {
        // 获取源表树
        val sourceTree = apiResourceService.getTree(sourceSelector)

        // 获取目标表树
        val targetTree = apiResourceService.getTree(targetSelector)

        // 计算差异
        val differences = apiResourceSynchronizer.calculateDifferences(sourceTree, targetTree)
        return ResponseEntity(differences, HttpStatus.OK)
    }

    /**
     * 同步资源
     */
    @PostMapping("/sync")
    fun syncResources(
        @RequestParam sourceSelector: Int,
        @RequestParam targetSelector: Int,
        @RequestBody resources: List<String>
    ): ResponseEntity<String> {
        // 获取源表树
        val sourceTree = apiResourceService.getTree(sourceSelector)

        // 获取目标表树
        val targetTree = apiResourceService.getTree(targetSelector)

        // 同步两个树之间的数据
        val syncResults = apiResourceSynchronizer.synchronizeTrees(
            sourceTree = sourceTree,
            targetTree = targetTree,
            resources
        )

        // 将同步结果应用到数据库
        apiResourceService.applySyncResults(syncResults, targetSelector)

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
        @RequestBody resources: List<String>
    ): ResponseEntity<List<SyncResult<String, ApiResource.ApiResourceInfo>>> {
        // 获取源表树
        val sourceTree = apiResourceService.getTree(sourceSelector)

        // 获取目标表树
        val targetTree = apiResourceService.getTree(targetSelector)

        // 计算同步操作但不应用
        val syncResults = apiResourceSynchronizer.synchronizeTrees(
            sourceTree = sourceTree,
            targetTree = targetTree,
            resources,
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
        val updatedResource = apiResourceService.updateResource(resource, selector)
        return ResponseEntity(updatedResource, HttpStatus.OK)
    }

    /**
     * 创建新资源
     */
    @PostMapping
    fun createResource(
        @RequestParam selector: Int,
        @RequestBody resource: ApiResource
    ): ResponseEntity<ApiResource> {
        val createdResource = apiResourceService.createResource(resource, selector)
        return ResponseEntity(createdResource, HttpStatus.CREATED)
    }

    /**
     * 删除资源
     */
    @PostMapping("delete/{id}")
    fun deleteResource(
        @PathVariable id: String,
        @RequestParam selector: Int
    ): ResponseEntity<Void> {
        apiResourceService.deleteResource(id, selector)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * 更新资源状态（启用/停用）
     */
    @PatchMapping("/{id}/status")
    fun updateResourceStatus(
        @PathVariable id: String,
        @RequestParam selector: Int,
        @RequestParam activeStatus: Boolean
    ): ResponseEntity<Int> {
        val updatedResource = apiResourceService.updateResourceStatus(id, activeStatus, selector)
        return ResponseEntity(updatedResource, HttpStatus.OK)
    }

    /**
     * 批量更新资源状态
     */
    @PatchMapping("/batch/status")
    fun batchUpdateStatus(
        @RequestParam selector: Int,
        @RequestParam activeStatus: Boolean,
        @RequestBody ids: List<String>
    ): ResponseEntity<Int> {
        val count = apiResourceService.batchUpdateStatus(ids, activeStatus, selector)
        return ResponseEntity(count, HttpStatus.OK)
    }

    /**
     * 批量删除资源
     */
    @PostMapping("/batch/delete")
    fun batchDelete(
        @RequestParam selector: Int,
        @RequestBody ids: List<String>
    ): ResponseEntity<Boolean> {
        val result = apiResourceService.batchDelete(ids, selector)
        return ResponseEntity(result, HttpStatus.OK)
    }

    /**
     * 搜索资源
     */
    @PostMapping("/search")
    fun searchResources(
        @RequestParam selector: Int,
        @RequestParam query: String
    ): ResponseEntity<List<ApiResource>> {
        val resources = apiResourceService.searchResources(query, selector)
        return ResponseEntity(resources, HttpStatus.OK)
    }

    /**
     * 获取可用的父节点列表（用于下拉框选择）
     */
    @PostMapping("/parents")
    fun getAvailableParents(
        @RequestParam selector: Int,
    ): ResponseEntity<List<Map<String, Any>>> {
        val parents = apiResourceService.getAvailableParents(selector)
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
    }
}
