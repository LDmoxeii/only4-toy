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

    @PostMapping("/sync")
    fun syncResources(
        @RequestParam(required = false) sourceSelector: Int = 1,
        @RequestParam(required = false) targetSelector: Int = 2,
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

    @GetMapping("/getDifferences")
    fun getDifferences(
        @RequestParam(required = false) sourceSelector: Int = 1,
        @RequestParam(required = false) targetSelector: Int = 2
    ): ResponseEntity<List<SyncResult<String, ApiResource.ApiResourceInfo>>> {
        // 获取源表树
        val sourceTree = apiResourceService.getTree(sourceSelector)

        // 获取目标表树
        val targetTree = apiResourceService.getTree(targetSelector)

        // 计算差异
        val differences = apiResourceSynchronizer.calculateDifferences(sourceTree, targetTree)
        return ResponseEntity(differences, HttpStatus.OK)
    }
}
