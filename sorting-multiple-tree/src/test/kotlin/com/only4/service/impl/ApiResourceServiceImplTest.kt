package com.only4.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.entity.ApiResource
import com.only4.entity.ApiResourceTree
import com.only4.synchronizer.SortingTreeSynchronizer
import com.only4.tree.SortingMultipleTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ApiResourceServiceImplTest {

    private var mapper: ObjectMapper = ObjectMapper()

    init {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    }

    @Mock
    private lateinit var apiResourceSynchronizer: SortingTreeSynchronizer<String, ApiResource.ApiResourceInfo>

    @Spy
    @InjectMocks
    private lateinit var target: ApiResourceServiceImpl

    @Captor
    private lateinit var apiResourceListCaptor: ArgumentCaptor<Collection<ApiResource>>

    @Test
    fun `test getTree with empty rootKey should return tree with all resources`() {
        val resources = mapper.readValue(
            """
            [
                {
                    "key": "resource1",
                    "parentKey": "",
                    "nodePath": "resource1",
                    "sort": 1,
                    "data": {
                        "title": "Resource 1",
                        "enTitle": "Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource2",
                    "parentKey": "",
                    "nodePath": "resource2",
                    "sort": 2,
                    "data": {
                        "title": "Resource 2",
                        "enTitle": "Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 准备
        doReturn(resources).`when`(target).list()

        // 执行
        val tree = target.getTree()

        // 验证
        assertNotNull(tree)
        verify(target).list()

        // 验证树结构
        val flattenNodes = tree.flattenTree()
        assertEquals(resources.size, flattenNodes.size)
    }

    @Test
    fun `test getTree with specific rootKey should return subtree`() {
        val rootKey = "resource1"
        val root = mapper.readValue(
            """
            {
                "key": "resource1",
                "parentKey": "",
                "nodePath": "resource1",
                "sort": 1,
                "data": {
                    "title": "Resource 1",
                    "enTitle": "Resource 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val childResources = mapper.readValue(
            """
            [
                {
                    "key": "child1",
                    "parentKey": "resource1",
                    "nodePath": "resource1/child1",
                    "sort": 101,
                    "data": {
                        "title": "Child 1",
                        "enTitle": "Child 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 准备
        doReturn(root).`when`(target).getById(rootKey)
        doReturn(childResources).`when`(target).list(any<QueryWrapper<ApiResource>>())

        // 执行
        val tree = target.getTree(rootKey)

        // 验证
        assertNotNull(tree)
        verify(target).getById(rootKey)
        verify(target).list(any<QueryWrapper<ApiResource>>())

        // 验证树结构包含所有预期节点
        val flattenNodes = tree.flattenTree()
        assertEquals(childResources.size, flattenNodes.size)
    }

    @Test
    fun `test applySyncResults should handle all sync types`() {
        val addNode = mapper.readValue(
            """
            {
                "key": "new1",
                "parentKey": "root",
                "nodePath": "root/new1",
                "sort": 101,
                "data": {
                    "title": "New 1",
                    "enTitle": "New 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val updateNode = mapper.readValue(
            """
            {
                "key": "update1",
                "parentKey": "root",
                "nodePath": "root/update1",
                "sort": 102,
                "data": {
                    "title": "Update 1",
                    "enTitle": "Update 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val deleteNode = mapper.readValue(
            """
            {
                "key": "delete1",
                "parentKey": "root",
                "nodePath": "root/delete1",
                "sort": 103,
                "data": {
                    "title": "Delete 1",
                    "enTitle": "Delete 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val sameNode = mapper.readValue(
            """
            {
                "key": "same1",
                "parentKey": "root",
                "nodePath": "root/same1",
                "sort": 104,
                "data": {
                    "title": "Same 1",
                    "enTitle": "Same 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val syncResults = listOf(
            SortingTreeSynchronizer.SyncResult(addNode, SortingTreeSynchronizer.SyncType.ADD, true),
            SortingTreeSynchronizer.SyncResult(updateNode, SortingTreeSynchronizer.SyncType.UPDATE, true),
            SortingTreeSynchronizer.SyncResult(deleteNode, SortingTreeSynchronizer.SyncType.DELETE, true),
            SortingTreeSynchronizer.SyncResult(sameNode, SortingTreeSynchronizer.SyncType.SAME, false)
        )

        // 准备
        doReturn(true).`when`(target).saveOrUpdate(any<ApiResource>())
        doReturn(true).`when`(target).removeById(anyString())

        // 执行
        target.applySyncResults(syncResults, 2)

        // 验证
        verify(target, times(2)).saveOrUpdate(any<ApiResource>())  // 两次更新操作(ADD和UPDATE)
        verify(target, times(1)).removeById("delete1")  // 一次删除操作
    }

    @Test
    fun `test updateResource should save and return resource`() {
        val resource = mapper.readValue(
            """
            {
                "key": "resource1",
                "parentKey": "",
                "nodePath": "resource1",
                "sort": 1,
                "data": {
                    "title": "Resource 1",
                    "enTitle": "Resource 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        // 准备
        doReturn(true).`when`(target).saveOrUpdate(any<ApiResource>())

        // 执行
        val result = target.updateResource(resource)

        // 验证
        verify(target).saveOrUpdate(resource)
        assertEquals(resource, result)
    }

    @Test
    fun `test createResource should add node to tree and save to database`() {
        val newResource = mapper.readValue(
            """
            {
                "key": "new1",
                "parentKey": "resource1",
                "nodePath": "",
                "sort": 0,
                "data": {
                    "title": "New 1",
                    "enTitle": "New 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val existingResources = mapper.readValue(
            """
            [
                {
                    "key": "resource1",
                    "parentKey": "",
                    "nodePath": "resource1",
                    "sort": 1,
                    "data": {
                        "title": "Resource 1",
                        "enTitle": "Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建一个模拟树
        val mockTree = ApiResourceTree.buildFromResources(resources = existingResources)

        // 准备
        val spyTarget = spy(target)
        doReturn(mockTree).`when`(spyTarget).getTree(newResource.parentKey)
        doReturn(true).`when`(spyTarget).saveBatch(anyList<ApiResource>())

        // 执行
        val result = spyTarget.createResource(newResource)

        // 验证
        assertNotNull(result)
        assertEquals(newResource.key, result.key)
        assertEquals(newResource.parentKey, result.parentKey)
        verify(spyTarget).saveBatch(anyList<ApiResource>())
    }

    @Test
    fun `test deleteResource should remove node from tree and database`() {
        val keyToDelete = "resource2"

        val resourceToDelete = mapper.readValue(
            """
            {
                "key": "resource2",
                "parentKey": "",
                "nodePath": "resource2",
                "sort": 2,
                "data": {
                    "title": "Resource 2",
                    "enTitle": "Resource 2 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val treeResources = mapper.readValue(
            """
            [
                {
                    "key": "resource2",
                    "parentKey": "",
                    "nodePath": "resource2",
                    "sort": 2,
                    "data": {
                        "title": "Resource 2",
                        "enTitle": "Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "child3",
                    "parentKey": "resource2",
                    "nodePath": "resource2/child3",
                    "sort": 201,
                    "data": {
                        "title": "Child 3",
                        "enTitle": "Child 3 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建一个模拟树
        val originalTree = ApiResourceTree.buildFromResources(resources = treeResources)
        val modifiedTree = ApiResourceTree.buildFromResources(resources = listOf(treeResources[0]))

        // 准备
        val spyTarget = spy(target)
        doReturn(resourceToDelete).`when`(spyTarget).getById(keyToDelete)
        doReturn(originalTree).`when`(spyTarget).getTree(resourceToDelete.parentKey)
        doReturn(true).`when`(spyTarget).removeByIds(anyList<String>())
        doReturn(true).`when`(spyTarget).saveOrUpdateBatch(anyList<ApiResource>())

        // 执行
        val result = spyTarget.deleteResource(keyToDelete)

        // 验证
        verify(spyTarget).getById(keyToDelete)
        verify(spyTarget).removeByIds(anyList<String>())
        verify(spyTarget).saveOrUpdateBatch(anyList<ApiResource>())
    }

    @Test
    fun `test updateResourceStatus should update status for node and all descendants`() {
        val key = "resource1"

        val resource = mapper.readValue(
            """
            {
                "key": "resource1",
                "parentKey": "",
                "nodePath": "resource1",
                "sort": 1,
                "data": {
                    "title": "Resource 1",
                    "enTitle": "Resource 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        val descendants = mapper.readValue(
            """
            [
                {
                    "key": "child1",
                    "parentKey": "resource1",
                    "nodePath": "resource1/child1",
                    "sort": 101,
                    "data": {
                        "title": "Child 1",
                        "enTitle": "Child 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "child2",
                    "parentKey": "resource1",
                    "nodePath": "resource1/child2",
                    "sort": 102,
                    "data": {
                        "title": "Child 2",
                        "enTitle": "Child 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建一个模拟树
        val tree = ApiResourceTree.buildFromResources("resource1", descendants)

        // 准备
        doReturn(resource).`when`(target).getById(key)
        doReturn(tree).`when`(target).getTree(key)
        doReturn(true).`when`(target).saveOrUpdateBatch(anyList<ApiResource>())

        // 执行
        val count = target.updateResourceStatus(key, false)

        // 验证
        verify(target).getById(key)
        verify(target).getTree(key)
        verify(target).saveOrUpdateBatch(apiResourceListCaptor.capture())

        // 验证所有节点状态都被更新
        val updatedResources = apiResourceListCaptor.value
        assertEquals(descendants.size + 1, count)
        updatedResources.forEach {
            assertFalse(it.data.activeStatus)
        }
    }

    @Test
    fun `test batchDelete should delete multiple resources`() {
        val keysToDelete = listOf("resource1", "resource2")

        // 准备
        val spyTarget = spy(target)
        doReturn(true).`when`(spyTarget).deleteResource(anyString())

        // 执行
        val result = spyTarget.batchDelete(keysToDelete)

        // 验证
        assertTrue(result)
        keysToDelete.forEach {
            verify(spyTarget).deleteResource(it)
        }
    }

    @Test
    fun `test searchResources should return matching resources`() {
        val query = "test"

        val matchingResources = mapper.readValue(
            """
            [
                {
                    "key": "test1",
                    "parentKey": "",
                    "nodePath": "test1",
                    "sort": 1,
                    "data": {
                        "title": "Test Resource 1",
                        "enTitle": "Test Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "test2",
                    "parentKey": "",
                    "nodePath": "test2",
                    "sort": 2,
                    "data": {
                        "title": "Test Resource 2",
                        "enTitle": "Test Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 准备
        doReturn(matchingResources).`when`(target).list(any<QueryWrapper<ApiResource>>())

        // 执行
        val result = target.searchResources(query)

        // 验证
        verify(target).list(any<QueryWrapper<ApiResource>>())
        assertEquals(matchingResources, result)
    }

    @Test
    fun `test getAvailableParents should return active nodes`() {
        val rootKey = ""

        val resources = mapper.readValue(
            """
            [
                {
                    "key": "resource1",
                    "parentKey": "",
                    "nodePath": "resource1",
                    "sort": 1,
                    "data": {
                        "title": "Resource 1",
                        "enTitle": "Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource2",
                    "parentKey": "",
                    "nodePath": "resource2",
                    "sort": 2,
                    "data": {
                        "title": "Resource 2",
                        "enTitle": "Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": false
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建一个模拟树
        val tree = ApiResourceTree.buildFromResources(resources = resources)

        // 准备
        val spyTarget = spy(target)
        doReturn(tree).`when`(spyTarget).getTree(rootKey)

        // 执行
        val result = spyTarget.getAvailableParents(rootKey)

        // 验证
        verify(spyTarget).getTree(rootKey)
        assertEquals(1, result.size)
        assertTrue((result[0] as ApiResource).activeStatus)
    }

    @Test
    fun `test calculateDifferences should delegate to synchronizer`() {
        val sourceTree =
            mock(SortingMultipleTree::class.java) as SortingMultipleTree<String, ApiResource.ApiResourceInfo>
        val targetTree =
            mock(SortingMultipleTree::class.java) as SortingMultipleTree<String, ApiResource.ApiResourceInfo>

        val expectedResults = listOf<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>>()

        // 准备
        doReturn(expectedResults).`when`(apiResourceSynchronizer).calculateDifferences(sourceTree, targetTree)

        // 执行
        val result = target.calculateDifferences(sourceTree, targetTree)

        // 验证
        verify(apiResourceSynchronizer).calculateDifferences(sourceTree, targetTree)
        assertEquals(expectedResults, result)
    }

    @Test
    fun `test synchronizeTrees should delegate to synchronizer`() {
        val sourceTree =
            mock(SortingMultipleTree::class.java) as SortingMultipleTree<String, ApiResource.ApiResourceInfo>
        val targetTree =
            mock(SortingMultipleTree::class.java) as SortingMultipleTree<String, ApiResource.ApiResourceInfo>
        val resources = listOf("resource1", "resource2")

        val expectedResults = listOf<SortingTreeSynchronizer.SyncResult<String, ApiResource.ApiResourceInfo>>()

        // 准备
        doReturn(expectedResults).`when`(apiResourceSynchronizer).synchronizeTrees(sourceTree, targetTree)

        // 执行
        val result = target.synchronizeTrees(sourceTree, targetTree, resources)

        // 验证
        verify(apiResourceSynchronizer).synchronizeTrees(sourceTree, targetTree)
        assertEquals(expectedResults, result)
    }

    @Test
    fun `test moveNode should move resource from source tree to target tree`() {
        val keyToMove = "resource1"

        // 待移动的资源
        val resource = mapper.readValue(
            """
            {
                "key": "resource1",
                "parentKey": "sourceParent",
                "nodePath": "sourceParent/resource1",
                "sort": 101,
                "data": {
                    "title": "Resource 1",
                    "enTitle": "Resource 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        // 源树资源
        val sourceTreeResources = mapper.readValue(
            """
            [
                {
                    "key": "sourceParent",
                    "parentKey": "",
                    "nodePath": "sourceParent",
                    "sort": 1,
                    "data": {
                        "title": "Source Parent",
                        "enTitle": "Source Parent EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource1",
                    "parentKey": "sourceParent",
                    "nodePath": "sourceParent/resource1",
                    "sort": 101,
                    "data": {
                        "title": "Resource 1",
                        "enTitle": "Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource2",
                    "parentKey": "sourceParent",
                    "nodePath": "sourceParent/resource2",
                    "sort": 102,
                    "data": {
                        "title": "Resource 2",
                        "enTitle": "Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 目标树资源
        val targetTreeResources = mapper.readValue(
            """
            [
                {
                    "key": "targetParent",
                    "parentKey": "",
                    "nodePath": "targetParent",
                    "sort": 2,
                    "data": {
                        "title": "Target Parent",
                        "enTitle": "Target Parent EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource3",
                    "parentKey": "targetParent",
                    "nodePath": "targetParent/resource3",
                    "sort": 201,
                    "data": {
                        "title": "Resource 3",
                        "enTitle": "Resource 3 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建源树和目标树
        val sourceTree = ApiResourceTree.buildFromResources(resources = sourceTreeResources)
        val targetTree = ApiResourceTree.buildFromResources(resources = targetTreeResources)

        // 移动目标
        val targetMap = mapOf(
            "parentKey" to "targetParent",
            "sort" to 202L
        )

        // 准备
        doReturn(resource).`when`(target).getById(keyToMove)
        doReturn(sourceTree).`when`(target).getTree("sourceParent")
        doReturn(targetTree).`when`(target).getTree("targetParent")
        doReturn(true).`when`(target).removeById(anyString())
        doReturn(true).`when`(target).saveOrUpdateBatch(anyList<ApiResource>())

        // 执行
        target.moveNode(keyToMove, targetMap)

        // 验证
        verify(target).getById(keyToMove)
        verify(target).getTree("sourceParent")
        verify(target).getTree("targetParent")
        verify(target).removeById(keyToMove)

        // 验证两次批量保存操作
        verify(target, times(2)).saveOrUpdateBatch(apiResourceListCaptor.capture())

        // 获取捕获的参数
        val capturedLists = apiResourceListCaptor.allValues
        assertEquals(2, capturedLists.size) // 应该有两次批量保存

        // 验证第一次保存是源树的所有节点（不包含被移动的节点）
        val firstSavedList = capturedLists[0]
        assertEquals(2, firstSavedList.size) // sourceParent 和 resource2
        assertFalse(firstSavedList.any { it.key == keyToMove })

        // 验证第二次保存是目标树的所有节点（包含被移动的节点）
        val secondSavedList = capturedLists[1]
        assertTrue(secondSavedList.any { it.key == keyToMove })
        assertEquals(3, secondSavedList.size) // targetParent, resource3 和移动后的 resource1
    }

    @Test
    fun `test moveNode with same parent but different sort should update order`() {
        val keyToMove = "resource1"

        // 待移动的资源
        val resource = mapper.readValue(
            """
            {
                "key": "resource1",
                "parentKey": "parent",
                "nodePath": "parent/resource1",
                "sort": 101,
                "data": {
                    "title": "Resource 1",
                    "enTitle": "Resource 1 EN",
                    "showStatus": true,
                    "activeStatus": true
                }
            }
            """.trimIndent(), ApiResource::class.java
        )

        // 树资源
        val treeResources = mapper.readValue(
            """
            [
                {
                    "key": "parent",
                    "parentKey": "",
                    "nodePath": "parent",
                    "sort": 1,
                    "data": {
                        "title": "Parent",
                        "enTitle": "Parent EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource1",
                    "parentKey": "parent",
                    "nodePath": "parent/resource1",
                    "sort": 101,
                    "data": {
                        "title": "Resource 1",
                        "enTitle": "Resource 1 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource2",
                    "parentKey": "parent",
                    "nodePath": "parent/resource2",
                    "sort": 102,
                    "data": {
                        "title": "Resource 2",
                        "enTitle": "Resource 2 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                },
                {
                    "key": "resource3",
                    "parentKey": "parent",
                    "nodePath": "parent/resource3",
                    "sort": 103,
                    "data": {
                        "title": "Resource 3",
                        "enTitle": "Resource 3 EN",
                        "showStatus": true,
                        "activeStatus": true
                    }
                }
            ]
            """.trimIndent(), object : TypeReference<List<ApiResource>>() {}
        )

        // 创建树
        val tree = ApiResourceTree.buildFromResources(resources = treeResources)

        // 移动目标 - 相同父节点但不同排序
        val targetMap = mapOf(
            "parentKey" to "parent",
            "sort" to 4L // 移到最后
        )

        // 准备
        doReturn(resource).`when`(target).getById(keyToMove)
        doReturn(tree).`when`(target).getTree("parent") // 源树和目标树相同
        doReturn(true).`when`(target).saveOrUpdateBatch(anyList<ApiResource>())

        // 执行
        target.moveNode(keyToMove, targetMap)

        // 验证
        verify(target).getById(keyToMove)
        verify(target).getTree("parent")

        verify(target).saveOrUpdateBatch(apiResourceListCaptor.capture())
    }

    @Test
    fun `test moveNode should throw exception when resource not found`() {
        val keyToMove = "nonexistent"

        // 移动目标
        val targetMap = mapOf(
            "parentKey" to "targetParent",
            "sort" to 1L
        )

        // 准备
        doReturn(null).`when`(target).getById(keyToMove)

        // 执行 & 验证
        val exception = assertThrows(IllegalArgumentException::class.java) {
            target.moveNode(keyToMove, targetMap)
        }

        assertEquals("Resource with id $keyToMove not found", exception.message)
        verify(target).getById(keyToMove)
        verify(target, never()).getTree(anyString())
        verify(target, never()).removeById(anyString())
        verify(target, never()).saveOrUpdateBatch(anyList<ApiResource>())
    }
}
