package com.only4.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.fasterxml.jackson.annotation.JsonIgnore
import com.only4.tree.AbstractSortingMultipleTree
import com.only4.tree.SortingTreeNode

@TableName(value = "api_resource_1", autoResultMap = true)
data class ApiResource(
    @TableId
    override val key: String,

    @TableField("parent_id")
    override var parentKey: String,

    @TableField("node_path")
    override var nodePath: String,

    @TableField
    override var sort: Long,

    @TableField(exist = false)
    override var data: ApiResourceInfo = ApiResourceInfo(),

    @TableField(exist = false)
    @JsonIgnore
    override val children: MutableList<SortingTreeNode<String, ApiResourceInfo>> = mutableListOf(),

    // 表名选择器，默认为1，可以设置为1或2
    @TableField(exist = false)
    @JsonIgnore
    var tableSelector: Int = 1
) : SortingTreeNode<String, ApiResource.ApiResourceInfo> {

    // 无参构造函数，用于MyBatis
    constructor() : this(
        key = "",
        parentKey = "",
        nodePath = "",
        sort = 0,
        data = ApiResourceInfo(),
        children = mutableListOf(),
        tableSelector = 1
    )

    // 添加一个构造函数用于处理MyBatis查询结果映射
    constructor(
        id: String,
        parentId: String,
        nodePath: String,
        sort: Long,
        title: String,
        enTitle: String,
        showStatus: Boolean,
        activeStatus: Boolean
    ) : this(
        key = id,
        parentKey = parentId,
        nodePath = nodePath,
        sort = sort,
        data = ApiResourceInfo(
            title = title,
            enTitle = enTitle,
            showStatus = showStatus,
            activeStatus = activeStatus
        )
    ) {
        // 设置属性
        this.title = title
        this.enTitle = enTitle
        this.showStatus = showStatus
        this.activeStatus = activeStatus
    }

    @TableField("title")
    var title: String = ""
        get() = data.title
        set(value) {
            field = value
            data.title = value
        }

    @TableField("en_title")
    var enTitle: String = ""
        get() = data.enTitle
        set(value) {
            field = value
            data.enTitle = value
        }

    @TableField("show_status")
    var showStatus: Boolean = true
        get() = data.showStatus
        set(value) {
            field = value
            data.showStatus = value
        }

    @TableField("active_status")
    var activeStatus: Boolean = true
        get() = data.activeStatus
        set(value) {
            field = value
            data.activeStatus = value
        }

    data class ApiResourceInfo(
        var title: String = "",
        var enTitle: String = "",
        var showStatus: Boolean = true,
        var activeStatus: Boolean = true,
    )
}

class ApiResourceTree : AbstractSortingMultipleTree<String, ApiResource.ApiResourceInfo>("") {
    override fun calculateNextSort(parentKey: String): Long {
        val children = parentChildMap[parentKey] ?: return 1L

        return if (children.isEmpty()) {
            1L
        } else {
            // 找出最大的排序索引并加1
            children.maxOfOrNull { getSortIndex(it.sort) }?.plus(1) ?: 1L
        }
    }

    /**
     * 生成节点路径
     */
    private fun generateNodePath(key: String, parentKey: String): String {
        if (parentKey == dummyKey) return key

        val parent = findNodeByKey(parentKey)
        requireNotNull(parent) { "Parent node with key $parentKey not found" }

        return "${parent.nodePath}/$key"
    }

    /**
     * 递归更新子节点的排序
     */
    private fun updateChildrenSort(parentKey: String, newParentSort: Long) {
        val children = parentChildMap[parentKey] ?: return

        for (child in children) {
            val childSortIndex = getSortIndex(child.sort)
            val newSort = newParentSort * sortBase + childSortIndex

            child.sort = newSort

            // 递归更新更深层的子节点
            updateChildrenSort(child.key, newSort)
        }
    }

    override fun handleSortConflict(parentKey: String, startSort: Long) {
        val sortIndex = getSortIndex(startSort)
        val children = parentChildMap[parentKey] ?: return

        // 只处理从sortIndex开始的节点
        val conflictNodes = children.filter { getSortIndex(it.sort) >= sortIndex }
        val parentSort = if (children.isNotEmpty()) getParentSort(children[0].sort) else 0L

        if (conflictNodes.isNotEmpty()) {
            // 将冲突的节点排序值递增
            for (node in conflictNodes.sortedBy { it.sort }) {
                val oldSort = node.sort
                val newSortIndex = getSortIndex(oldSort) + 1
                node.sort = parentSort * sortBase + newSortIndex

                // 递归更新子节点排序
                updateChildrenSort(node.key, node.sort)
            }
        }
    }

    override fun moveChildrenForward(parentKey: String, removedSort: Long) {
        val children = parentChildMap[parentKey]?.sortedBy { it.sort } ?: return

        // 只处理需要前移的节点（排序值大于被删除节点的排序值）
        val nodesToMove = children.filter { getSortIndex(it.sort) > removedSort }

        for (node in nodesToMove) {
            val oldSort = node.sort
            val newSortIndex = getSortIndex(oldSort) - 1
            val parentSort = getParentSort(oldSort)
            val newSort = parentSort * sortBase + newSortIndex

            node.sort = newSort

            // 递归更新所有子节点的排序
            updateChildrenSort(node.key, newSort)
        }
    }

    override fun addNode(
        key: String,
        parentKey: String,
        data: ApiResource.ApiResourceInfo,
        sort: Long?
    ): SortingTreeNode<String, ApiResource.ApiResourceInfo> {
        // 检查键是否已存在
        require(!nodeMap.containsKey(key)) { "Node with key $key already exists" }

        // 获取下一个可用的排序索引
        val nextAvailableIndex = calculateNextSort(parentKey)

        // 如果指定的排序号大于下一个可用的排序索引，则使用下一个可用的排序索引
        val actualSortIndex = if (sort == null || sort > nextAvailableIndex) nextAvailableIndex else sort

        // 获取父节点排序值
        val parentSort = if (parentKey != dummyKey) findNodeByKey(parentKey)?.sort ?: 0L else 0L
        // 计算实际排序值: 父节点排序 * 100 + 实际的排序索引
        val actualSort = parentSort * sortBase + actualSortIndex
        val nodePath = generateNodePath(key, parentKey)

        // 创建节点
        val node = ApiResource(key, parentKey, nodePath, actualSort, data)

        // 检查是否需要解决排序冲突
        handleSortConflict(parentKey, actualSort)

        nodeMap[key] = node
        parentChildMap.getOrPut(parentKey) { mutableListOf() }.add(node)

        findNodeByKey(parentKey)?.children?.add(node)

        return node
    }

    override fun removeNode(key: String): Boolean {
        /**
         * 递归删除节点及其子节点
         */
        fun recursiveRemove(node: SortingTreeNode<String, ApiResource.ApiResourceInfo>) {
            // 首先递归删除所有子节点
            val childrenCopy = node.children.toList()
            for (child in childrenCopy) {
                recursiveRemove(child)
            }

            // 删除当前节点
            nodeMap.remove(node.key)

            // 从父子映射中删除
            val parentKey = node.parentKey
            parentChildMap[parentKey]!!.remove(node)
            if (parentChildMap[parentKey]!!.isEmpty()) {
                parentChildMap.remove(parentKey)
            }

            // 从父节点的子列表中删除
            val nodeParentKey = node.parentKey
            if (nodeParentKey != dummyKey) {
                val parentNode = findNodeByKey(nodeParentKey)
                parentNode!!.children.remove(node)
            }
        }

        // 查找节点
        val node = findNodeByKey(key) ?: return true

        // 递归删除节点及其所有子孙节点
        recursiveRemove(node)

        // 重新排序被删除节点的兄弟节点
        val parentKey = node.parentKey
        val lastSortIndex = getSortIndex(node.sort)

        // 将同级节点向前移动
        moveChildrenForward(parentKey, lastSortIndex)

        return true
    }

    override fun moveNode(
        node: SortingTreeNode<String, ApiResource.ApiResourceInfo>,
        newParentKey: String,
        newSort: Long?
    ): SortingTreeNode<String, ApiResource.ApiResourceInfo> {
        /**
         * 将节点从其父节点分离。
         */
        fun detachFromParent(node: SortingTreeNode<String, ApiResource.ApiResourceInfo>) {
            if (node.parentKey != dummyKey) {
                findNodeByKey(node.parentKey)?.children?.remove(node)
            }
            parentChildMap[node.parentKey]?.remove(node)
            if (parentChildMap[node.parentKey]?.isEmpty() == true) {
                parentChildMap.remove(node.parentKey)
            }
        }

        /**
         * 将节点附加到其父节点。
         */
        fun attachToParent(node: SortingTreeNode<String, ApiResource.ApiResourceInfo>) {
            if (node.parentKey != dummyKey) {
                findNodeByKey(node.parentKey)?.children?.add(node)
            }
            parentChildMap.getOrPut(node.parentKey) { mutableListOf() }.add(node)
        }

        /**
         * 递归更新所有子孙节点排序值。
         */
        fun updateDescendantsRecursively(parentNode: SortingTreeNode<String, ApiResource.ApiResourceInfo>) {
            getChildren(parentNode.key).forEach { child ->
                val sortIndex = getSortIndex(child.sort)
                child.sort = parentNode.sort * sortBase + sortIndex
                child.nodePath = generateNodePath(child.key, parentNode.key)

                updateDescendantsRecursively(child)
            }
        }

        // 如果位置和排序值都未改变，则无需移动
        if (node.parentKey == newParentKey && newSort == null) return node

        checkWouldNotCreateCycle(node.key, newParentKey)

        val oldParentKey = node.parentKey
        val oldSort = node.sort

        // 步骤 1: 从旧父节点分离
        detachFromParent(node)

        // 步骤 2: 更新节点自身的核心属性
        // 获取下一个可用的排序索引
        val nextAvailableIndex = calculateNextSort(newParentKey)

        // 如果指定的排序号大于下一个可用的排序索引，则使用下一个可用的排序索引
        val actualSortIndex = if (newSort == null || newSort > nextAvailableIndex) nextAvailableIndex else newSort
        val newParentAbsoluteSort = if (newParentKey == dummyKey) 0L else findNodeByKey(newParentKey)?.sort ?: 0L
        node.sort = newParentAbsoluteSort * sortBase + actualSortIndex
        node.parentKey = newParentKey
        node.nodePath = generateNodePath(node.key, newParentKey)

        // 步骤 3: 递归更新所有子孙节点的路径和排序值
        updateDescendantsRecursively(node)

        // 步骤 4: 将节点附加到新父节点，并处理可能产生的排序冲突
        handleSortConflict(newParentKey, node.sort)
        attachToParent(node)

        // 步骤 5: 重新排序原父节点下的兄弟节点
        moveChildrenForward(oldParentKey, getSortIndex(oldSort))

        return node
    }

    /**
     * 检查移动节点是否会导致循环依赖
     */
    private fun wouldCreateCycle(nodeKey: String, targetParentKey: String): Boolean {
        // 如果目标父节点就是当前节点，肯定会导致循环
        if (nodeKey == targetParentKey) {
            return true
        }

        // 检查目标父节点是否是当前节点的子孙节点
        var currentKey: String? = targetParentKey
        while (currentKey != null && currentKey != dummyKey) {
            if (currentKey == nodeKey) {
                return true
            }
            currentKey = findNodeByKey(currentKey)?.parentKey
        }

        return false
    }

    /**
     * 确保移动操作不会导致循环依赖。
     */
    private fun checkWouldNotCreateCycle(nodeKey: String, targetParentKey: String) {
        require(!wouldCreateCycle(nodeKey, targetParentKey)) {
            "Moving node $nodeKey to parent $targetParentKey would create a cycle in the tree"
        }
    }

    override fun moveNodes(
        keys: Collection<String>,
        newParentKey: String
    ): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>> {
        val result = mutableListOf<SortingTreeNode<String, ApiResource.ApiResourceInfo>>()

        // 先检查所有节点是否存在
        val nodesToMove = keys.mapNotNull { findNodeByKey(it) }
        require(nodesToMove.size == keys.size) { "Some nodes do not exist" }

        // 检查是否会导致循环依赖
        for (node in nodesToMove) {
            require(
                !wouldCreateCycle(
                    node.key,
                    newParentKey
                )
            ) { "Moving node ${node.key} to parent $newParentKey would create a cycle in the tree" }
        }

        // 按排序值排序，确保按顺序移动
        val sortedNodes = nodesToMove.sortedBy { it.sort }

        // 移动每个节点
        for (node in sortedNodes) {
            val movedNode = moveNode(node, newParentKey)
            result.add(movedNode)
        }

        return result
    }

    override fun flattenTree(): List<SortingTreeNode<String, ApiResource.ApiResourceInfo>> {
        return nodeMap.values.sortedBy { it.sort }
    }

    override fun updateNodeData(
        key: String,
        update: (ApiResource.ApiResourceInfo) -> ApiResource.ApiResourceInfo
    ): SortingTreeNode<String, ApiResource.ApiResourceInfo>? {
        // 查找节点
        val node = findNodeByKey(key) ?: return null

        // 更新节点数据
        val newData = update(node.data)
        node.data = newData

        // 返回更新后的节点
        return node
    }

    companion object {
        fun buildFromResources(resources: Collection<ApiResource>): ApiResourceTree {
            val tree = ApiResourceTree()
            resources.forEach { resource ->
                tree.addNode(
                    key = resource.key,
                    parentKey = resource.parentKey,
                    data = resource.data,
                    sort = resource.sort
                )
            }
            return tree
        }
    }
}
