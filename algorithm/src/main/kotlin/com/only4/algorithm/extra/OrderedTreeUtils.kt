package com.only4.algorithm.extra

/**
 * 有序多叉树工具类
 * 提供构建和操作树形结构的工具方法
 */
object OrderedTreeUtils {

    /**
     * 根据节点列表构建树形结构
     *
     * @param nodeList 节点列表
     * @param isRoot 根节点判断器，用于确定哪些节点是根节点
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 根节点列表
     */
    fun <K, V> buildTree(
        nodeList: Collection<OrderedTreeNode<K, V>>,
        isRoot: (OrderedTreeNode<K, V>) -> Boolean
    ): List<OrderedTreeNode<K, V>> {
        if (nodeList.isEmpty()) {
            return emptyList()
        }

        // 将节点分为根节点和子节点
        val rootNodes = nodeList.filter(isRoot).toMutableList()
        val childNodes = nodeList.filterNot(isRoot)

        if (rootNodes.isEmpty()) {
            return emptyList()
        }

        // 按父节点ID分组子节点
        val parentChildMap = childNodes.groupBy { it.parentKey }

        // 递归构建树
        for (rootNode in rootNodes) {
            buildChildrenTree(rootNode, parentChildMap)
        }

        // 按排序值排序根节点
        return rootNodes.sortedBy { it.sort }
    }

    /**
     * 根据节点列表构建树形结构，使用指定键作为根节点
     *
     * @param nodeList 节点列表
     * @param rootKey 根节点键
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 根节点列表
     */
    fun <K, V> buildTreeByRootKey(
        nodeList: Collection<OrderedTreeNode<K, V>>,
        rootKey: K?
    ): List<OrderedTreeNode<K, V>> {
        return buildTree(nodeList) { node -> node.parentKey == rootKey }
    }

    /**
     * 构建节点的子树
     *
     * @param parentNode 父节点
     * @param parentChildMap 父节点ID到子节点列表的映射
     * @param <K> 键类型
     * @param <V> 数据类型
     */
    private fun <K, V> buildChildrenTree(
        parentNode: OrderedTreeNode<K, V>,
        parentChildMap: Map<K, List<OrderedTreeNode<K, V>>>
    ) {
        val childNodes = parentChildMap[parentNode.key] ?: return

        // 清除现有的子节点
        if (parentNode is DefaultOrderedTreeNode) {
            parentNode.children.clear()
        }

        // 添加排序后的子节点
        val sortedChildren = childNodes.sortedBy { it.sort }
        parentNode.children.addAll(sortedChildren)

        // 递归处理每个子节点
        for (child in parentNode.children) {
            buildChildrenTree(child, parentChildMap)
        }
    }

    /**
     * 根据ID从树形结构中查找节点
     *
     * @param key 要查找的节点ID
     * @param treeList 要搜索的树节点集合
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 找到的节点，如果未找到则返回null
     */
    fun <K, V> findNodeByKey(key: K, treeList: Collection<OrderedTreeNode<K, V>>?): OrderedTreeNode<K, V>? {
        if (treeList.isNullOrEmpty()) {
            return null
        }

        for (node in treeList) {
            if (node.key == key) {
                return node
            }

            val childResult = findNodeByKey(key, node.children)
            if (childResult != null) {
                return childResult
            }
        }

        return null
    }

    /**
     * 将树形结构转换为扁平列表
     *
     * @param treeList 树节点列表
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 扁平化的节点列表（按排序值排序）
     */
    fun <K, V> flattenTree(treeList: Collection<OrderedTreeNode<K, V>>): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()
        flattenTreeInternal(treeList, result)
        return result.sortedBy { it.sort }
    }

    /**
     * 将树形结构转换为扁平列表，支持深度优先和广度优先遍历
     *
     * @param treeList 树节点列表
     * @param traversalType 遍历类型，深度优先或广度优先
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 扁平化的节点列表
     */
    fun <K, V> flattenTree(
        treeList: Collection<OrderedTreeNode<K, V>>,
        traversalType: TraversalType
    ): List<OrderedTreeNode<K, V>> {
        return when (traversalType) {
            TraversalType.DEPTH_FIRST -> flattenTreeDepthFirst(treeList)
            TraversalType.BREADTH_FIRST -> flattenTreeBreadthFirst(treeList)
        }
    }

    /**
     * 使用深度优先遍历将树形结构转换为扁平列表
     */
    private fun <K, V> flattenTreeDepthFirst(
        treeList: Collection<OrderedTreeNode<K, V>>
    ): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()
        flattenTreeInternal(treeList, result)
        return result
    }

    /**
     * 使用广度优先遍历将树形结构转换为扁平列表
     */
    private fun <K, V> flattenTreeBreadthFirst(
        treeList: Collection<OrderedTreeNode<K, V>>
    ): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()
        val queue = ArrayDeque<OrderedTreeNode<K, V>>()

        // 添加所有根节点到队列
        queue.addAll(treeList)

        // 广度优先遍历
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            result.add(node)
            queue.addAll(node.children)
        }

        return result
    }

    /**
     * 遍历类型枚举
     */
    enum class TraversalType {
        /**
         * 深度优先遍历
         */
        DEPTH_FIRST,

        /**
         * 广度优先遍历
         */
        BREADTH_FIRST
    }

    /**
     * 递归地将树节点添加到扁平列表中
     */
    private fun <K, V> flattenTreeInternal(
        treeList: Collection<OrderedTreeNode<K, V>>,
        result: MutableList<OrderedTreeNode<K, V>>
    ) {
        for (node in treeList) {
            result.add(node)
            flattenTreeInternal(node.children, result)
        }
    }

    /**
     * 根据指定路径查找节点
     *
     * @param nodePath 节点路径，如 "parent/child"
     * @param rootNodes 根节点列表
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 找到的节点，如果未找到则返回null
     */
    fun <K, V> findNodeByPath(nodePath: String, rootNodes: Collection<OrderedTreeNode<K, V>>): OrderedTreeNode<K, V>? {
        if (rootNodes.isEmpty()) {
            return null
        }

        val pathParts = nodePath.split("/")
        if (pathParts.isEmpty()) {
            return null
        }

        // 查找起始节点
        var currentLevel = rootNodes
        var currentNode: OrderedTreeNode<K, V>? = null

        for (element in pathParts) {
            currentNode = currentLevel.find { it.nodePath.endsWith(element) }
            if (currentNode == null) {
                return null
            }
            currentLevel = currentNode.children
        }

        return currentNode
    }

    /**
     * 获取节点及其所有子孙节点
     *
     * @param node 起始节点
     * @param <K> 键类型
     * @param <V> 数据类型
     * @return 节点及其所有子孙节点的列表
     */
    fun <K, V> getNodeAndDescendants(node: OrderedTreeNode<K, V>): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()
        result.add(node)
        collectDescendants(node.children, result)
        return result
    }

    /**
     * 递归收集子孙节点
     */
    private fun <K, V> collectDescendants(
        children: Collection<OrderedTreeNode<K, V>>,
        result: MutableList<OrderedTreeNode<K, V>>
    ) {
        for (child in children) {
            result.add(child)
            collectDescendants(child.children, result)
        }
    }

    /**
     * 从扁平列表构建树结构
     *
     * @param dataList 数据列表
     * @param keyExtractor 键提取函数
     * @param parentKeyExtractor 父键提取函数
     * @param dataExtractor 数据提取函数
     * @param isRoot 根节点判断器
     * @param <K> 键类型
     * @param <V> 数据类型
     * @param <T> 源数据类型
     * @return 构建的树结构根节点列表
     */
    fun <K, V, T> buildTreeFromList(
        dataList: Collection<T>,
        keyExtractor: (T) -> K,
        parentKeyExtractor: (T) -> K,
        dataExtractor: (T) -> V,
        isRoot: (T) -> Boolean
    ): List<OrderedTreeNode<K, V>> {
        // 将数据转换为树节点
        val rootSort = 1L
        var sortCounter = rootSort

        // 首先处理根节点
        val rootNodes = mutableListOf<OrderedTreeNode<K, V>>()
        val childDataItems = mutableListOf<T>()

        // 分离根节点和子节点
        for (item in dataList) {
            if (isRoot(item)) {
                val key = keyExtractor(item)
                val parentKey = parentKeyExtractor(item)
                val data = dataExtractor(item)
                val nodePath = "$key"
                val node = DefaultOrderedTreeNode(key, parentKey, sortCounter++, nodePath, data)
                rootNodes.add(node)
            } else {
                childDataItems.add(item)
            }
        }

        // 构建节点ID到节点的映射
        val nodeMap = rootNodes.associateBy { it.key }.toMutableMap()

        // 处理子节点，使用广度优先遍历确保父节点先被处理
        val queue = ArrayDeque(childDataItems)
        val processed = mutableSetOf<T>()
        val unprocessed = mutableListOf<T>()

        while (queue.isNotEmpty()) {
            val item = queue.removeFirst()
            val parentKey = parentKeyExtractor(item)

            // 如果找到父节点，则添加子节点
            if (parentKey != null && nodeMap.containsKey(parentKey)) {
                val key = keyExtractor(item)
                val data = dataExtractor(item)
                val parentNode = nodeMap[parentKey]!!
                val nodePath = "${parentNode.nodePath}/$key"

                // 计算子节点排序值（父节点排序 * 100 + 子节点索引）
                val childIndex = (parentNode.children.size + 1).toLong()
                val sort = parentNode.sort * 100 + childIndex

                val node = DefaultOrderedTreeNode(key, parentKey, sort, nodePath, data)
                parentNode.children.add(node)
                nodeMap[key] = node
                processed.add(item)
            } else {
                // 如果还没找到父节点，则暂时加入未处理列表，稍后再处理
                unprocessed.add(item)
            }

            // 如果当前队列为空，但还有未处理的项，则将它们加回队列
            if (queue.isEmpty() && unprocessed.isNotEmpty()) {
                queue.addAll(unprocessed)
                // 如果处理过一轮后还是没有变化，说明有循环依赖或孤立节点，此时需要跳出
                if (processed.isEmpty()) {
                    break
                }
                processed.clear()
                unprocessed.clear()
            }
        }

        return rootNodes.sortedBy { it.sort }
    }

    /**
     * 从扁平列表构建树结构，指定根节点键
     *
     * @param dataList 数据列表
     * @param keyExtractor 键提取函数
     * @param parentKeyExtractor 父键提取函数
     * @param dataExtractor 数据提取函数
     * @param rootParentKey 根节点的父键
     * @param <K> 键类型
     * @param <V> 数据类型
     * @param <T> 源数据类型
     * @return 构建的树结构根节点列表
     */
    fun <K, V, T> buildTreeFromListByRootKey(
        dataList: Collection<T>,
        keyExtractor: (T) -> K,
        parentKeyExtractor: (T) -> K,
        dataExtractor: (T) -> V,
        rootParentKey: K?
    ): List<OrderedTreeNode<K, V>> {
        return buildTreeFromList(
            dataList,
            keyExtractor,
            parentKeyExtractor,
            dataExtractor
        ) { parentKeyExtractor(it) == rootParentKey }
    }
}
