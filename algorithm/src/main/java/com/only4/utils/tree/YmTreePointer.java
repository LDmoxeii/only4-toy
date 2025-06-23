//package com.only4.utils.tree;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
/// **
// * 树结构节点关联关系指针类，用于维护树节点之间的父子关系标识。
// * <p>
// * 该类为树形结构提供了基础的ID映射机制，通过存储节点ID和父节点ID，
// * 使得可以在扁平数据集合中建立起层级关系，便于构建和遍历树形结构。
// * <p>
// * 该类通常与{@link YmTree}和{@link YmTreeUtils}配合使用：
// * <ul>
// *   <li>被{@link YmTree}引用，提供节点间关系的元数据</li>
// *   <li>被{@link YmTreeUtils}用于构建树形结构时识别节点间的父子关系</li>
// * </ul>
// * <p>
// * 设计为使用Object类型的ID和父ID，可以适应不同类型的标识符（如Integer、Long、String等）。
// *
// * @author liuxiaohua
// * @see YmTree 树结构节点类
// * @see YmTreeUtils 树形结构构建和操作工具类
// * @since 2025-03-11
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class YmTreePointer {
//
//    /**
//     * 节点唯一标识。
//     * <p>
//     * 作为树节点在树结构中的唯一标识符，用于在构建树时
//     * 识别节点并与其子节点建立关联关系。
//     * <p>
//     * 类型为Object，可以适配各种类型的ID（如Integer、Long、String等）。
//     */
//    private Object id;
//
//    /**
//     * 父节点唯一标识。
//     * <p>
//     * 指向当前节点的父节点ID，用于在构建树时
//     * 将当前节点挂载到正确的父节点下。
//     * <p>
//     * 对于根节点，此值通常为null或特定值（如0）。
//     * 类型为Object，可以适配各种类型的ID（如Integer、Long、String等）。
//     */
//    private Object parentId;
//
//}
