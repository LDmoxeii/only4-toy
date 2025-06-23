//package com.only4.utils.tree;
//
//import lombok.Data;
//
//import java.util.List;
//
/// **
// * 树结构节点类，用于构建和表示通用的树形数据结构。
// * <p>
// * 该类是树形结构的基本构建单元，支持泛型以适应不同类型的节点数据。
// * 每个树节点包含当前节点数据、父节点引用和子节点集合，并通过{@link YmTreePointer}
// * 维护节点间的父子关系标识（ID映射）。
// * <p>
// * 主要特性：
// * <ul>
// *   <li>支持任意类型的节点数据（通过泛型T）</li>
// *   <li>保持对父节点的引用，便于向上遍历</li>
// *   <li>维护子节点列表，支持向下遍历</li>
// *   <li>通过ID和父ID标识树的层级关系</li>
// * </ul>
// * <p>
// * 通常与{@link YmTreeUtils}配合使用，用于构建和操作树形结构。
// *
// * @param <T> 节点数据的类型
// * @author liuxiaohua
// * @see YmTreePointer 节点指针，维护ID和父ID关系
// * @see YmTreeUtils 树形结构构建和操作工具类
// * @since 2025-03-11
// */
//@Data
//public class YmTree<T> {
//
//    /**
//     * 父子节点关系指针，维护节点的ID和父ID信息。
//     * <p>
//     * 该字段标记为transient，表示在序列化时不包含此字段，
//     * 主要用于在构建树结构时引用节点间的关系，而不需要在最终树结构中保留。
//     */
//    private transient YmTreePointer treePointer;
//
//    /**
//     * 父节点引用，指向当前节点的父节点数据对象。
//     * <p>
//     * 该字段标记为transient，表示在序列化时不包含此字段，
//     * 避免在JSON序列化等场景中产生循环引用。
//     */
//    private transient T parent;
//
//    /**
//     * 当前节点的数据对象，包含节点的实际业务数据。
//     */
//    private T current;
//
//    /**
//     * 子节点列表，包含当前节点的所有直接子节点。
//     * <p>
//     * 如果节点是叶子节点，此列表可能为null或空列表。
//     */
//    private List<YmTree<T>> children;
//
//    /**
//     * 构造一个树节点。
//     *
//     * @param current     当前节点的数据对象
//     * @param treePointer 节点的ID和父ID关系指针
//     */
//    public YmTree(T current, YmTreePointer treePointer) {
//        this.current = current;
//        this.treePointer = treePointer;
//    }
//
//    /**
//     * 获取当前节点的ID。
//     * <p>
//     * 该方法是对{@link YmTreePointer#getId()}的便捷调用，
//     * 用于获取节点在树结构中的唯一标识。
//     *
//     * @return 当前节点的ID
//     */
//    public Object getId() {
//        return treePointer.getId();
//    }
//
//    /**
//     * 获取当前节点的父节点ID。
//     * <p>
//     * 该方法是对{@link YmTreePointer#getParentId()}的便捷调用，
//     * 用于获取节点父节点的唯一标识，便于构建和遍历树结构。
//     *
//     * @return 当前节点的父节点ID
//     */
//    public Object getParentId() {
//        return treePointer.getParentId();
//    }
//
//}
