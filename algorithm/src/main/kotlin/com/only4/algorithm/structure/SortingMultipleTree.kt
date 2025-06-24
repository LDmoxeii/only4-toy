package com.only4.algorithm.structure

/**
 * 有序多叉树实现接口
 * 后续将实现此接口
 */
interface SortingMultipleTree<K, V> {
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
     * @param sort 指定的排序值, 同级节点排序号, 需要配合父节点  规则为：父节点排序 * 100 + 同级节点排序号递增
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
     * 移动节点到新的父节点下
     *
     * @param node 要移动的节点对象
     * @param newParentKey 新的父节点键
     * @param newSort 可选的新排序值，如果为null则自动计算
     * @return 移动后的节点，如果移动失败则返回null
     */
    fun moveNode(node: SortingTreeNode<K, V>, newParentKey: K, newSort: Long? = null): SortingTreeNode<K, V>

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
}

/**
 * 有序多叉树抽象类实现
 *
 * @param K 键类型
 * @param V 节点数据类型
 */
abstract class AbstractSortingMultipleTree<K, V> : SortingMultipleTree<K, V> {

    // 存储所有节点的映射，便于快速查找
    protected val nodeMap: MutableMap<K, SortingTreeNode<K, V>> = mutableMapOf()

    // 存储路径到节点的映射
    protected val pathNodeMap: MutableMap<String, SortingTreeNode<K, V>> = mutableMapOf()

    // 存储父节点到子节点的映射
    protected val parentChildMap: MutableMap<K?, MutableList<SortingTreeNode<K, V>>> = mutableMapOf()

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
