package com.only4.algorithm.extra

/**
 * 有序多叉树节点接口
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
interface OrderedTreeNode<K, V> {
    /** 节点唯一键 */
    val key: K

    /** 父节点唯一键 */
    val parentKey: K?

    /** 节点排序值，规则为：父节点排序 * 100 + 同级节点排序号递增 */
    val sort: Long

    /** 节点路径，格式为 "parent.key/this.key" */
    val nodePath: String

    /** 节点数据 */
    val data: V

    /** 子节点列表 */
    val children: MutableList<OrderedTreeNode<K, V>>
}

/**
 * 有序多叉树接口
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
interface OrderedMultipleTree<K, V> {
    /**
     * 添加节点（自动分配排序值）
     *
     * @param key 节点唯一键
     * @param parentKey 父节点键
     * @param data 节点数据
     * @return 添加的节点
     */
    fun addNode(key: K, parentKey: K?, data: V): OrderedTreeNode<K, V>

    /**
     * 添加节点（指定排序值）
     *
     * @param key 节点唯一键
     * @param parentKey 父节点键
     * @param data 节点数据
     * @param sort 指定的排序值, 同级节点排序号, 需要配合父节点  规则为：父节点排序 * 100 + 同级节点排序号递增
     * @return 添加的节点
     */
    fun addNode(key: K, parentKey: K?, data: V, sort: Long): OrderedTreeNode<K, V>

    /**
     * 删除节点
     *
     * @param key 要删除的节点键
     * @return 如果删除成功返回true，否则返回false
     */
    fun removeNode(key: K): Boolean

    /**
     * 根据键查找节点
     *
     * @param key 节点键
     * @return 找到的节点，如果不存在则返回null
     */
    fun findNodeByKey(key: K): OrderedTreeNode<K, V>?

    /**
     * 根据父节点键查找子节点列表
     *
     * @param parentKey 父节点键
     * @return 子节点列表
     */
    fun findNodesByParentKey(parentKey: K?): List<OrderedTreeNode<K, V>>

    /**
     * 根据节点路径查找所有子孙节点
     *
     * @param nodePath 节点路径
     * @return 子孙节点列表
     */
    fun findNodesByPath(nodePath: String): List<OrderedTreeNode<K, V>>
}

/**
 * 有序多叉树节点实现
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
data class DefaultOrderedTreeNode<K, V>(
    override val key: K,
    override val parentKey: K?,
    override var sort: Long,
    override var nodePath: String,
    override val data: V,
    override val children: MutableList<OrderedTreeNode<K, V>> = mutableListOf()
) : OrderedTreeNode<K, V>

/**
 * 有序多叉树抽象类实现
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
abstract class AbstractOrderedMultipleTree<K, V> : OrderedMultipleTree<K, V> {

    // 存储所有节点的映射，便于快速查找
    protected val nodeMap: MutableMap<K, OrderedTreeNode<K, V>> = mutableMapOf()

    // 存储父节点到子节点的映射
    protected val parentChildMap: MutableMap<K?, MutableList<OrderedTreeNode<K, V>>> = mutableMapOf()

    // 存储路径到节点的映射
    protected val pathNodeMap: MutableMap<String, MutableList<OrderedTreeNode<K, V>>> = mutableMapOf()

    /**
     * 生成节点路径
     *
     * @param key 节点键
     * @param parentKey 父节点键
     * @return 节点路径
     */
    protected abstract fun generateNodePath(key: K, parentKey: K?): String

    /**
     * 计算新节点的排序值
     *
     * @param parentKey 父节点键
     * @return 新的排序值
     */
    protected abstract fun calculateNextSort(parentKey: K?): Int

    /**
     * 重新排序子节点，解决排序冲突
     *
     * @param parentKey 父节点键
     * @param startSort 开始排序值
     */
    protected abstract fun reorderChildren(parentKey: K?, startSort: Int)

    /**
     * 子节点向前移动排序
     *
     * @param parentKey 父节点键
     * @param removedSort 被移除节点的排序值
     */
    protected abstract fun moveChildrenForward(parentKey: K?, removedSort: Int)

    /**
     * 递归删除节点及其所有子孙节点
     *
     * @param node 要删除的节点
     */
    protected abstract fun recursiveRemove(node: OrderedTreeNode<K, V>)

    override fun findNodeByKey(key: K): OrderedTreeNode<K, V>? {
        return nodeMap[key]
    }

    override fun findNodesByParentKey(parentKey: K?): List<OrderedTreeNode<K, V>> {
        return parentChildMap[parentKey]?.sortedBy { it.sort } ?: emptyList()
    }

    override fun findNodesByPath(nodePath: String): List<OrderedTreeNode<K, V>> {
        val result = mutableListOf<OrderedTreeNode<K, V>>()

        // 找到以指定路径开头的所有节点
        pathNodeMap.forEach { (path, nodes) ->
            if (path.startsWith(nodePath)) {
                result.addAll(nodes)
            }
        }

        return result.sortedBy { it.sort }
    }
}

/**
 * 有序多叉树实现接口
 * 后续将实现此接口
 */
interface SortingMultipleTree<K, V> : OrderedMultipleTree<K, V> {
    /**
     * 获取树的所有节点列表（按排序值排序）
     *
     * @return 所有节点列表
     */
    fun getAllNodes(): List<OrderedTreeNode<K, V>>

    /**
     * 获取树的根节点列表
     *
     * @return 根节点列表
     */
    fun getRootNodes(): List<OrderedTreeNode<K, V>>

    /**
     * 将树转换为扁平列表（按排序值排序）
     *
     * @return 扁平化的节点列表
     */
    fun flattenTree(): List<OrderedTreeNode<K, V>>
}
