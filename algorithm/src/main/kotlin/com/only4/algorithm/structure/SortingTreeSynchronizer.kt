package com.only4.algorithm.structure

/**
 * 有序多叉树同步器接口
 * 用于比较和同步两个相同泛型的有序多叉树
 *
 * @param K 节点键类型
 * @param V 节点数据类型
 */
interface SortingTreeSynchronizer<K, V> {

    /**
     * 同步类型枚举
     */
    enum class SyncType {
        SAME,    // 相同节点
        ADD,     // 需要新增的节点
        DELETE,  // 需要删除的节点
        UPDATE   // 需要更新的节点
    }

    /**
     * 同步结果数据类
     */
    data class SyncResult<K, V>(
        val node: SortingTreeNode<K, V>,
        val syncType: SyncType,
        val isSyncNeeded: Boolean = false
    )

    data class SyncMataData<K, V>(
        val source: SortingMultipleTree<K, V>,
        val dummyKey: K,
        val pathSeparator: String,
        val sortBase: Long,
    )

    val syncContext: Pair<SyncMataData<K, V>, SyncMataData<K, V>>


    /**
     * 统计两棵树之间的差异
     *
     * @param sourceTree 源树
     * @param targetTree 目标树
     * @return 包含所有差异节点及其同步类型的结果列表
     */
    fun calculateDifferences(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>
    ): List<SyncResult<K, V>>

    /**
     * 同步两棵树
     *
     * @param sourceTree 源树（作为同步的基准）
     * @param targetTree 目标树（将被修改以匹配源树）
     * @param keys 需要同步的节点键列表，如果为空则同步所有节点
     * @return 同步后的差异结果列表
     */
    fun synchronizeTrees(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        keys: Collection<K> = emptyList()
    ): List<SyncResult<K, V>>

    /**
     * 同步两棵树
     *
     * @param sourceTree 源树（作为同步的基准）
     * @param targetTree 目标树（将被修改以匹配源树）
     * @param syncResults 需要同步的节点列表，如果为空则同步所有节点
     * @return 同步后的差异结果列表
     */
    fun synchronizeTreesByResults(
        sourceTree: SortingMultipleTree<K, V>,
        targetTree: SortingMultipleTree<K, V>,
        syncResults: Collection<SyncResult<K, V>> = emptyList()
    ): List<SyncResult<K, V>>
}
