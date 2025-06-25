package com.only4

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
