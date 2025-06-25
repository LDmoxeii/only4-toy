package com.only4.service

import com.only4.entity.ApiResource
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.tree.SortingMultipleTree

interface ApiResourceService {
    fun getTree(tableSelector: Int): SortingMultipleTree<String, ApiResource.ApiResourceInfo>

    fun applySyncResults(
        result: Collection<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>>,
        targetSelector: Int
    )
}
