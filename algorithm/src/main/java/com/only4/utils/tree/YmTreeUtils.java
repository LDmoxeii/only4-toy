//package com.only4.utils.tree;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//import java.util.*;
//import java.util.function.BiConsumer;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
/// **
// * 树结构构建和操作工具类，提供了一系列用于构建、查询和操作树形结构的静态方法。
// * <p>
// * 该工具类支持两种主要的树形结构构建方式：
// * <ul>
// *   <li>基于{@link YmTree}的树形结构构建，保留完整的树结构信息</li>
// *   <li>直接在原始数据对象上构建树形结构，通过设置子节点集合实现</li>
// * </ul>
// * <p>
// * 主要功能：
// * <ul>
// *   <li>从扁平集合构建树形结构</li>
// *   <li>根据ID查找树节点</li>
// *   <li>获取树的所有节点数据</li>
// *   <li>处理循环依赖和特殊情况</li>
// * </ul>
// *
// * @author liuxiaohua
// * @see YmTree 树结构节点类
// * @see YmTreePointer 节点关联关系指针类
// * @since 2025-03-11
// */
//public class YmTreeUtils {
//
//    /**
//     * 构建基于{@link YmTree}的树形结构。
//     * <p>
//     * 此方法将扁平的数据集合转换为层级结构的树，使用提供的根节点判断器和节点指针生成器
//     * 来确定节点间的层级关系。构建过程如下：
//     * <ol>
//     *   <li>遍历数据集合，为每个数据项创建对应的树节点</li>
//     *   <li>根据根节点判断器区分根节点和普通节点</li>
//     *   <li>根据节点的父ID关系，递归构建树形结构</li>
//     * </ol>
//     *
//     * @param dataList    源数据集合，包含所有需要构建为树的节点数据
//     * @param isRoot      根节点判断器，用于确定哪些节点是根节点，接收节点数据返回布尔值
//     * @param nodePointer 节点指针生成器，用于从节点数据生成包含ID和父ID的关系指针
//     * @param <T>         节点数据类型
//     * @return 树形结构的根节点列表，如果没有根节点则返回空列表
//     */
//    public static <T> List<YmTree<T>> buildTree(Collection<T> dataList,
//                                                Function<T, Boolean> isRoot,
//                                                Function<T, YmTreePointer> nodePointer) {
//        List<YmTree<T>> nodeList = new ArrayList<>();
//        List<YmTree<T>> rootNode = new ArrayList<>();
//        for (T t : dataList) {
//            YmTree<T> tree = new YmTree<>(t, nodePointer.apply(t));
//            if (isRoot.apply(t)) {
//                rootNode.add(tree);
//            } else {
//                nodeList.add(tree);
//            }
//        }
//        if (rootNode.isEmpty()) {
//            return new ArrayList<>();
//        }
//        if (nodeList.isEmpty()) {
//            return rootNode;
//        }
//        Map<Object, List<YmTree<T>>> objectListMap = nodeList.stream()
//                .collect(Collectors.groupingBy(YmTree::getParentId));
//        for (YmTree<T> tTree : rootNode) {
//            buildTree(objectListMap, tTree);
//        }
//        return rootNode;
//
//    }
//
//    /**
//     * 根据ID从树形结构中查找指定节点。
//     * <p>
//     * 此方法使用深度优先搜索算法，递归遍历树形结构，查找与指定ID匹配的节点。
//     * 如果找到匹配节点，立即返回；如果遍历完整个树仍未找到，则返回null。
//     *
//     * @param id       要查找的节点ID
//     * @param treeList 要搜索的树节点集合
//     * @param <T>      节点数据类型
//     * @return 找到的节点，如果未找到则返回null
//     */
//    public static <T> YmTree<T> getTreeNode(Object id, Collection<YmTree<T>> treeList) {
//        if (treeList == null || treeList.isEmpty()) {
//            return null;
//        }
//        for (YmTree<T> tTree : treeList) {
//            if (tTree.getId().equals(id)) {
//                return tTree;
//            }
//            YmTree<T> treeNode = getTreeNode(id, tTree.getChildren());
//            if (treeNode != null) {
//                return treeNode;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 根据ID集合从树形结构中查找指定节点，并将结果添加到结果集合中。
//     * <p>
//     * 此方法使用深度优先搜索算法，递归遍历树形结构，查找ID在指定集合中的节点。
//     * 注意：对于同一棵子树，只会返回找到的最顶层节点（同树只会返回根节点）。
//     *
//     * @param ids      要查找的节点ID集合
//     * @param treeList 要搜索的树节点集合
//     * @param result   找到的节点将被添加到此集合中
//     * @param <T>      节点数据类型
//     */
//    public static <T> void getTreeNode(Collection<?> ids, Collection<YmTree<T>> treeList, Collection<YmTree<T>> result) {
//        if (treeList == null || treeList.isEmpty()) {
//            return;
//        }
//        for (YmTree<T> tTree : treeList) {
//            if (ids.contains(tTree.getId())) {
//                result.add(tTree);
//                continue;
//            }
//            getTreeNode(ids, tTree.getChildren(), result);
//        }
//    }
//
//    /**
//     * 获取树形结构中所有节点的数据对象，并将结果添加到结果集合中。
//     * <p>
//     * 此方法使用深度优先遍历算法，递归访问树中的每个节点，
//     * 提取节点的数据对象（通过{@link YmTree#getCurrent()}获取），
//     * 并将其添加到指定的结果集合中。
//     *
//     * @param treeList 要遍历的树节点集合
//     * @param result   节点数据对象将被添加到此集合中
//     * @param <T>      节点数据类型
//     */
//    public static <T> void getNodeList(Collection<YmTree<T>> treeList, Collection<T> result) {
//        if (treeList == null || treeList.isEmpty()) {
//            return;
//        }
//        for (YmTree<T> tTree : treeList) {
//            result.add(tTree.getCurrent());
//            getNodeList(tTree.getChildren(), result);
//        }
//    }
//
//    /**
//     * 直接在原始数据对象上构建树形结构，使用默认的节点筛选器。
//     * <p>
//     * 此方法是{@link #buildTree(Collection, Function, Function, BiConsumer, Function)}的简化版本，
//     * 使用默认的节点筛选器（接受所有节点作为有效父节点）。
//     *
//     * @param dataList        源数据集合，包含所有需要构建为树的节点数据
//     * @param keyMapper       ID提取函数，用于从节点数据获取唯一标识
//     * @param parentKeyMapper 父ID提取函数，用于从节点数据获取父节点标识
//     * @param childrenSetter  子节点设置函数，用于将子节点集合设置到父节点
//     * @param <T>             节点数据类型
//     * @return 树形结构的根节点列表
//     */
//    public static <T> List<T> buildTree(Collection<T> dataList,
//                                        Function<T, ?> keyMapper,
//                                        Function<T, ?> parentKeyMapper,
//                                        BiConsumer<T, List<T>> childrenSetter) {
//        return buildTree(dataList, keyMapper, parentKeyMapper, childrenSetter, t -> true);
//    }
//
//    /**
//     * 直接在原始数据对象上构建树形结构，支持自定义父节点筛选器。
//     * <p>
//     * 此方法不创建额外的树节点类，而是直接通过修改原始数据对象的引用关系来构建树结构。
//     * 构建过程如下：
//     * <ol>
//     *   <li>收集所有节点的ID，并按父ID分组</li>
//     *   <li>通过比较所有节点ID和所有父ID，找出根节点（没有对应父节点的节点）</li>
//     *   <li>递归设置每个节点的子节点集合</li>
//     * </ol>
//     * <p>
//     * 该方法会自动处理循环依赖问题（直接剔除循环依赖的节点）。
//     *
//     * @param dataList        源数据集合，包含所有需要构建为树的节点数据
//     * @param keyMapper       ID提取函数，用于从节点数据获取唯一标识
//     * @param parentKeyMapper 父ID提取函数，用于从节点数据获取父节点标识
//     * @param childrenSetter  子节点设置函数，用于将子节点集合设置到父节点
//     * @param isRealParent    父节点筛选器，用于确定哪些节点是有效的父节点
//     * @param <T>             节点数据类型
//     * @return 树形结构的根节点列表
//     */
//    public static <T> List<T> buildTree(Collection<T> dataList,
//                                        Function<T, ?> keyMapper,
//                                        Function<T, ?> parentKeyMapper,
//                                        BiConsumer<T, List<T>> childrenSetter,
//                                        Function<T, Boolean> isRealParent) {
//        Set<Object> allIds = new HashSet<>();
//        Map<Object, List<T>> parentIdMap = new HashMap<>();
//
//        for (T data : dataList) {
//            if (data == null) continue;
//            allIds.add(keyMapper.apply(data));
//            parentIdMap.computeIfAbsent(parentKeyMapper.apply(data), k -> new ArrayList<>()).add(data);
//        }
//        // 处理并发，包裹一层
//        Set<Object> parentIds = new HashSet<>(parentIdMap.keySet());
//        // 获取根节点 id。循环依赖会直接剔除
//        parentIds.removeAll(allIds);
//
//        List<T> parents = parentIds.stream()
//                .map(parentIdMap::remove)
//                .filter(Objects::nonNull)
//                .flatMap(Collection::stream)
//                .filter(isRealParent::apply)
//                .collect(Collectors.toList());
//
//        for (T parent : parents) {
//            buildTree(parentIdMap, parent, keyMapper, childrenSetter);
//        }
//
//        return parents;
//    }
//
//    /**
//     * 递归构建树形结构的辅助方法（用于直接在原始数据对象上构建树）。
//     * <p>
//     * 此方法通过递归调用，逐层构建树形结构：
//     * <ol>
//     *   <li>根据父节点的ID，从父ID映射中获取其子节点列表</li>
//     *   <li>将子节点列表设置到父节点</li>
//     *   <li>递归处理每个子节点</li>
//     * </ol>
//     * <p>
//     * 如果父节点没有子节点，则设置一个空列表。
//     *
//     * @param parentIdMap    父ID到子节点列表的映射
//     * @param parent         当前处理的父节点
//     * @param keyMapper      ID提取函数
//     * @param childrenSetter 子节点设置函数
//     * @param <T>            节点数据类型
//     */
//    private static <T> void buildTree(Map<?, List<T>> parentIdMap,
//                                      T parent,
//                                      Function<T, ?> keyMapper,
//                                      BiConsumer<T, List<T>> childrenSetter) {
//        List<T> remove = parentIdMap.remove(keyMapper.apply(parent));
//        if (remove != null && !remove.isEmpty()) {
//            childrenSetter.accept(parent, remove);
//            for (T tTree : remove) {
//                buildTree(parentIdMap, tTree, keyMapper, childrenSetter);
//            }
//        } else {
//            childrenSetter.accept(parent, new ArrayList<>());
//        }
//    }
//
//    /**
//     * 递归构建树形结构的辅助方法（用于构建{@link YmTree}类型的树）。
//     * <p>
//     * 此方法通过递归调用，逐层构建树形结构：
//     * <ol>
//     *   <li>根据父节点的ID，从父ID映射中获取其子节点列表</li>
//     *   <li>将子节点列表设置到父节点</li>
//     *   <li>设置每个子节点的父节点引用</li>
//     *   <li>递归处理每个子节点</li>
//     * </ol>
//     *
//     * @param objectListMap 父ID到子节点列表的映射
//     * @param parentNode    当前处理的父节点
//     * @param <T>           节点数据类型
//     */
//    private static <T> void buildTree(Map<Object, List<YmTree<T>>> objectListMap, YmTree<T> parentNode) {
//        List<YmTree<T>> remove = objectListMap.remove(parentNode.getId());
//        if (remove != null && !remove.isEmpty()) {
//            parentNode.setChildren(remove);
//            for (YmTree<T> tTree : remove) {
//                tTree.setParent(parentNode.getCurrent());
//                buildTree(objectListMap, tTree);
//            }
//        }
//    }
//
//    /**
//     * 示例代码和测试方法，演示工具类的使用。
//     * <p>
//     * 该方法创建测试数据，并使用不同的方法构建树形结构，
//     * 展示了查找节点、获取节点列表等功能的使用方式。
//     * <p>
//     * 注意：此方法仅用于演示和测试目的。
//     *
//     * @param args 命令行参数（未使用）
//     */
//    public static void main(String[] args) {
//        List<Test> dataList = new ArrayList<>();
//        dataList.add(new Test(1, null));
//        dataList.add(new Test(2, 0));
//        dataList.add(new Test(3, 1));
//        dataList.add(new Test(4, 3));
//        dataList.add(new Test(null, 2));
//        dataList.add(new Test(6, 2));
//        dataList.add(new Test(7, 2));
//        dataList.add(new Test(8, 9));
//        dataList.add(new Test(9, 8));
//
//        List<YmTree<Test>> trees = buildTree(dataList,
//                t -> t.getParentId() == null || t.getParentId() == 0 || t.getParentId() == 9,
//                t -> new YmTreePointer(t.getId(), t.getParentId()));
//        // System.out.println(JSONObject.toJSONString(trees));
//
//        YmTree<Test> treeNode = getTreeNode(3, trees);
//        // System.out.println(JSONObject.toJSONString(treeNode));
//
//        List<Test> tests = new ArrayList<>();
//        getNodeList(Collections.singletonList(treeNode), tests);
//        // System.out.println(JSONObject.toJSONString(tests));
//
//        List<YmTree<Test>> result = new ArrayList<>();
//        getTreeNode(Arrays.asList(3, 2), trees, result);
//        // System.out.println(JSONObject.toJSONString(result));
//
//        tests = new ArrayList<>();
//        getNodeList(new ArrayList<>(result), tests);
//        // System.out.println(JSONObject.toJSONString(tests));
//
//
//        List<Test> tests1 = buildTree(dataList, Test::getId, Test::getParentId, Test::setChildren);
//        // System.out.println(JSONObject.toJSONString(tests1));
//    }
//
//    /**
//     * 测试用的数据类，用于演示树形结构构建。
//     * <p>
//     * 该类包含ID、父ID和子节点列表，可用于构建树形结构。
//     */
//    @Data
//    @AllArgsConstructor
//    public static final class Test {
//        /**
//         * 节点ID
//         */
//        private Integer id;
//
//        /**
//         * 父节点ID
//         */
//        private Integer parentId;
//
//        /**
//         * 子节点列表
//         */
//        private List<Test> children;
//
//        /**
//         * 构造函数，创建一个只有ID和父ID的测试节点。
//         *
//         * @param id       节点ID
//         * @param parentId 父节点ID
//         */
//        public Test(Integer id, Integer parentId) {
//            this.id = id;
//            this.parentId = parentId;
//        }
//    }
//
//}
