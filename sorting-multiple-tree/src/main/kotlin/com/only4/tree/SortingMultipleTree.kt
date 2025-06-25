package com.only4.tree

/**
 * 有序多叉树节点接口
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
interface SortingTreeNode<K, V> {
    /** 节点唯一键 */
    val key: K

    /** 父节点唯一键 */
    var parentKey: K

    /** 节点排序值 */
    var sort: Long

    /** 节点路径，格式为 "parent.key/this.key" */
    var nodePath: String

    /** 节点数据 */
    var data: V

    /** 子节点列表 */
    val children: MutableList<SortingTreeNode<K, V>>
        get() = mutableListOf()
}

/**
 * 有序多叉树节点实现
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
data class DefaultSortingTreeNode<K, V>(
    override val key: K,
    override var parentKey: K,
    override var sort: Long,
    override var nodePath: String,
    override var data: V,
    override val children: MutableList<SortingTreeNode<K, V>> = mutableListOf()
) : SortingTreeNode<K, V>

/**
 * 有序多叉树实现接口
 * 后续将实现此接口
 */
interface SortingMultipleTree<K, V> {
    // 根节点的虚拟父键标识
    val dummyKey: K

    // 路径分隔符
    val pathSeparator: String

    // 节点排序基数
    val sortBase: Long

    /**
     * 添加根节点（自动分配排序值）
     *
     * @param key 节点唯一键
     * @param data 节点数据
     * @return 添加的根节点
     */
    fun addRootNode(key: K, data: V): SortingTreeNode<K, V> = this.addNode(key, dummyKey, data)

    /**
     * 添加根节点（指定排序值）
     *
     * @param key 节点唯一键
     * @param data 节点数据
     * @param sort 指定的排序值
     * @return 添加的根节点
     */
    fun addRootNode(key: K, data: V, sort: Long): SortingTreeNode<K, V> = this.addNode(key, dummyKey, data, sort)

    /**
     * 判断一个节点是否为根节点
     *
     * @param node 要检查的节点
     * @return 如果是根节点返回true，否则返回false
     */
    fun isRoot(node: SortingTreeNode<K, V>): Boolean {
        return node.parentKey == dummyKey
    }

    /**
     * 添加节点（自动分配排序值）
     *
     * @param key 节点唯一键
     * @param parentKey 父节点键
     * @param data 节点数据
     * @return 添加的节点
     */
    fun addNode(key: K, parentKey: K, data: V): SortingTreeNode<K, V>

    /**
     * 添加节点（指定排序值）
     *
     * @param key 节点唯一键
     * @param parentKey 父节点键
     * @param data 节点数据
     * @param sort 指定的排序值
     * @return 添加的节点
     */
    fun addNode(key: K, parentKey: K, data: V, sort: Long): SortingTreeNode<K, V>

    /**
     * 删除节点
     *
     * @param key 要删除的节点键
     * @return 如果删除成功返回true，否则返回false
     */
    fun removeNode(key: K): Boolean

    /**
     * 移动节点到新的父节点下
     *
     * @param key 要移动的节点键
     * @param newParentKey 新的父节点键
     * @param newSort 可选的新排序值，如果为null则自动计算
     * @return 移动后的节点，如果移动失败则返回null
     */
    fun moveNode(key: K, newParentKey: K, newSort: Long? = null): SortingTreeNode<K, V>?

    /**
     * 移动节点为根节点
     *
     * @param key 要移动的节点键
     * @param newSort 可选的新排序值，如果为null则自动计算
     * @return 移动后的节点，如果移动失败则返回null
     */
    fun moveNodeToRoot(key: K, newSort: Long? = null): SortingTreeNode<K, V>? {
        return moveNode(key, dummyKey, newSort)
    }

    /**
     * 移动节点到新的父节点下
     *
     * @param node 要移动的节点对象
     * @param newParentKey 新的父节点键
     * @param newSort 可选的新排序值，如果为null则自动计算
     * @return 移动后的节点，如果移动失败则返回null
     */
    fun moveNode(node: SortingTreeNode<K, V>, newParentKey: K, newSort: Long? = null): SortingTreeNode<K, V>

    /**
     * 移动节点为根节点
     */
    fun moveNodeToRoot(node: SortingTreeNode<K, V>, newSort: Long? = null): SortingTreeNode<K, V> {
        return moveNode(node, dummyKey, newSort)
    }

    /**
     * 批量移动节点到新的父节点下
     *
     * @param keys 要移动的节点键集合
     * @param newParentKey 新的父节点键
     * @return 移动后的节点列表
     */
    fun moveNodes(keys: Collection<K>, newParentKey: K): List<SortingTreeNode<K, V>>

    /**
     * 通过节点路径移动节点
     *
     * @param nodePath 要移动的节点路径
     * @param newParentPath 新的父节点路径
     * @return 移动后的节点，如果移动失败则返回null
     */
    fun moveNodeByPath(nodePath: String, newParentPath: String): SortingTreeNode<K, V>

    /**
     * 根据键查找节点
     *
     * @param key 节点键
     * @return 找到的节点，如果不存在则返回null
     */
    fun findNodeByKey(key: K): SortingTreeNode<K, V>?

    /**
     * 将树转换为扁平列表（按排序值排序）
     *
     * @return 扁平化的节点列表
     */
    fun flattenTree(): List<SortingTreeNode<K, V>>

    /**
     * 获取树的根节点列表
     *
     * @return 根节点列表
     */
    fun getRootNodes(): List<SortingTreeNode<K, V>>

    /**
     * 根据父节点键查找子节点列表
     *
     * @param parentKey 父节点键
     * @return 子节点列表
     */
    fun getChildren(parentKey: K): List<SortingTreeNode<K, V>>

    /**
     * 根据节点路径查找所有子孙节点
     *
     * @param nodePath 节点路径
     * @return 子孙节点列表
     */
    fun getDescendants(nodePath: String): List<SortingTreeNode<K, V>>

    companion object {
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
            nodeList: Collection<SortingTreeNode<K, V>>,
            isRoot: (SortingTreeNode<K, V>) -> Boolean
        ): List<SortingTreeNode<K, V>> {
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
            nodeList: Collection<SortingTreeNode<K, V>>,
            rootKey: K?
        ): List<SortingTreeNode<K, V>> {
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
            parentNode: SortingTreeNode<K, V>,
            parentChildMap: Map<K, List<SortingTreeNode<K, V>>>
        ) {
            val childNodes = parentChildMap[parentNode.key] ?: return

            // 清除现有的子节点
            if (parentNode is DefaultSortingTreeNode) {
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
        ): List<SortingTreeNode<K, V>> {
            // 将数据转换为树节点
            val rootSort = 1L
            var sortCounter = rootSort

            // 首先处理根节点
            val rootNodes = mutableListOf<SortingTreeNode<K, V>>()
            val childDataItems = mutableListOf<T>()

            // 分离根节点和子节点
            for (item in dataList) {
                if (isRoot(item)) {
                    val key = keyExtractor(item)
                    val parentKey = parentKeyExtractor(item)
                    val data = dataExtractor(item)
                    val nodePath = "$key"
                    val node = DefaultSortingTreeNode(key, parentKey, sortCounter++, nodePath, data)
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

                    val node = DefaultSortingTreeNode(key, parentKey, sort, nodePath, data)
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
        ): List<SortingTreeNode<K, V>> {
            return buildTreeFromList(
                dataList,
                keyExtractor,
                parentKeyExtractor,
                dataExtractor
            ) { parentKeyExtractor(it) == rootParentKey }
        }
    }
}

/**
 * 有序多叉树抽象类实现
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
abstract class AbstractSortingMultipleTree<K, V>(
    // 根节点的虚拟父键标识
    override val dummyKey: K,
    // 路径分隔符
    override val pathSeparator: String = "/",
    // 节点排序基数
    override val sortBase: Long = 100L,
) : SortingMultipleTree<K, V> {

    // 存储所有节点的映射，便于快速查找
    protected val nodeMap: MutableMap<K, SortingTreeNode<K, V>> = mutableMapOf()

    // 存储路径到节点的映射
    protected val pathNodeMap: MutableMap<String, SortingTreeNode<K, V>> = mutableMapOf()

    // 存储父节点到子节点的映射
    protected val parentChildMap: MutableMap<K, MutableList<SortingTreeNode<K, V>>> = mutableMapOf()

    /**
     * 生成节点路径
     *
     * @param key 节点键
     * @param parentKey 父节点键
     * @return 节点路径
     */
    protected abstract fun generateNodePath(key: K, parentKey: K): String

    /**
     * 计算新节点的排序值
     *
     * @param parentKey 父节点键
     * @return 新的排序值
     */
    protected abstract fun calculateNextSort(parentKey: K): Long

    /**
     * 处理排序冲突
     *
     *
     * @param parentKey 父节点键
     * @param startSort 开始排序值
     */
    protected abstract fun handleSortConflict(parentKey: K, startSort: Long)

    /**
     * 子节点向前移动排序
     *
     * @param parentKey 父节点键
     * @param removedSort 被移除节点的排序值
     */
    protected abstract fun moveChildrenForward(parentKey: K, removedSort: Long)

    /**
     * 递归删除节点及其所有子孙节点
     *
     * @param node 要删除的节点
     */
    protected abstract fun recursiveRemove(node: SortingTreeNode<K, V>)

    override fun findNodeByKey(key: K): SortingTreeNode<K, V>? {
        return nodeMap[key]
    }

    override fun getChildren(parentKey: K): List<SortingTreeNode<K, V>> {
        return parentChildMap[parentKey]?.sortedBy { it.sort } ?: emptyList()
    }

    override fun getDescendants(nodePath: String): List<SortingTreeNode<K, V>> {
        return pathNodeMap
            .filter { (key, _) -> key.startsWith(nodePath) }
            .map { (_, value) -> value }
            .sortedBy { it.sort }
    }
}
