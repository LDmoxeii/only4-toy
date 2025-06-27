<template>
  <div class="resource-sync">
    <div class="page-header">
      <h2>资源管理与同步</h2>
      <div class="header-toolbar">
        <!-- 表选择器和切换按钮 -->
        <el-button type="primary" @click="switchSourceTarget">
          表选择: {{ sourceSelector }} -> {{ targetSelector }} 切换
        </el-button>

        <!-- 同步按钮 -->
        <el-button
            type="success"
            :disabled="selectedResources.length === 0"
            @click="syncResources"
        >
          同步选中项
        </el-button>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
              v-model="searchQuery"
              placeholder="搜索资源..."
              prefix-icon="el-icon-search"
              @keyup.enter.native="searchResources"
          >
            <el-button
                slot="append"
                icon="el-icon-search"
                @click="searchResources"
            ></el-button>
          </el-input>
        </div>
      </div>
    </div>

    <div class="trees-container">
      <!-- 源树面板 -->
      <div class="tree-panel">
        <div class="panel-header">
          <h3>源树 ({{ sourceSelector }})</h3>
          <div class="panel-actions">
            <el-button
                size="small"
                type="primary"
                icon="el-icon-plus"
                @click="showCreateDialog(sourceSelector)"
            >
              新增
            </el-button>
            <el-button
                size="small"
                type="warning"
                icon="el-icon-close"
                :disabled="selectedResources.length === 0"
                @click="handleBatchUpdateStatus(sourceSelector, false)"
            >
              停用
            </el-button>
            <el-button
                size="small"
                type="success"
                icon="el-icon-check"
                :disabled="selectedResources.length === 0"
                @click="handleBatchUpdateStatus(sourceSelector, true)"
            >
              启用
            </el-button>
          </div>
        </div>

        <div class="panel-content">
          <resource-tree
              :resources="sourceResources"
              :is-source-tree="true"
              :show-action-buttons="true"
              :selector="sourceSelector"
              @edit-node="editResource($event, sourceSelector)"
              @delete-node="handleDeleteResource($event, sourceSelector)"
              @toggle-status="handleToggleStatus($event, sourceSelector)"
              @node-move="(node, parentKey, sort) => handleMoveNode(node, parentKey, sort, sourceSelector)"
              @check-change="handleCheckChange"
          />
        </div>
      </div>

      <!-- 目标树面板 -->
      <div class="tree-panel">
        <div class="panel-header">
          <h3>目标树 ({{ targetSelector }})</h3>
          <div class="panel-actions">
            <el-button
                size="small"
                type="primary"
                icon="el-icon-plus"
                @click="showCreateDialog(targetSelector)"
            >
              新增
            </el-button>
            <el-button
                size="small"
                type="warning"
                icon="el-icon-close"
                :disabled="selectedResources.length === 0"
                @click="handleBatchUpdateStatus(targetSelector, false)"
            >
              停用
            </el-button>
            <el-button
                size="small"
                type="success"
                icon="el-icon-check"
                :disabled="selectedResources.length === 0"
                @click="handleBatchUpdateStatus(targetSelector, true)"
            >
              启用
            </el-button>
          </div>
        </div>

        <div class="panel-content">
          <resource-tree
              :resources="targetResources"
              :is-source-tree="false"
              :show-action-buttons="true"
              :selector="targetSelector"
              @edit-node="editResource($event, targetSelector)"
              @delete-node="handleDeleteResource($event, targetSelector)"
              @toggle-status="handleToggleStatus($event, targetSelector)"
              @node-move="(node, parentKey, sort) => handleMoveNode(node, parentKey, sort, targetSelector)"
              @check-change="handleCheckChange"
          />
        </div>
      </div>
    </div>

    <!-- 资源编辑对话框 -->
    <resource-edit-dialog
        :visible.sync="editDialogVisible"
        :resource="currentResource"
        :is-create="isCreateOperation"
        :selector="currentSelector"
        @submit="handleResourceSubmit"
    />

    <!-- 同步预览对话框 -->
    <el-dialog
        title="同步预览"
        :visible.sync="previewDialogVisible"
        width="60%"
    >
      <div v-if="previewResults.length === 0" class="empty-preview">
        <el-empty description="没有需要同步的数据"></el-empty>
      </div>
      <el-table
          v-else
          :data="previewResults"
          style="width: 100%"
      >
        <el-table-column prop="node.key" label="资源ID" width="180"></el-table-column>
        <el-table-column prop="node.title" label="标题"></el-table-column>
        <el-table-column label="操作类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getOperationTagType(scope.row.syncType)">
              {{ getOperationText(scope.row.syncType) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmSync">确认同步</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {mapActions, mapMutations, mapState} from 'vuex'
import ResourceTree from '@/components/ResourceTree'
import ResourceEditDialog from '@/components/ResourceEditDialog'
import {moveNode, previewSync, searchResources, syncResources} from '@/api/resource'

export default {
  name: 'ResourceSync',
  components: {
    ResourceTree,
    ResourceEditDialog
  },
  data() {
    return {
      searchQuery: '',
      editDialogVisible: false,
      isCreateOperation: false,
      currentResource: null,
      currentSelector: 1,
      previewDialogVisible: false,
      previewResults: []
    }
  },
  computed: {
    ...mapState([
      'sourceSelector',
      'targetSelector',
      'sourceResources',
      'targetResources',
      'differences',
      'selectedResources'
    ])
  },
  created() {
    this.loadSourceResources()
    this.loadTargetResources()
  },
  methods: {
    ...mapActions([
      'loadSourceResources',
      'loadTargetResources',
      'loadDifferences',
      'switchSourceTarget',
      'updateResource',
      'createResource',
      'deleteResource',
      'updateResourceStatus',
      'batchUpdateStatus'
    ]),
    ...mapMutations([
      'ADD_SELECTED_RESOURCE',
      'REMOVE_SELECTED_RESOURCE',
      'CLEAR_SELECTED_RESOURCES'
    ]),

    // 搜索资源
    async searchResources() {
      if (!this.searchQuery.trim()) {
        this.loadSourceResources()
        this.loadTargetResources()
        return
      }

      try {
        const sourceResponse = await searchResources(this.searchQuery, this.sourceSelector)
        const targetResponse = await searchResources(this.searchQuery, this.targetSelector)

        // 使用commit直接更新状态
        this.$store.commit('SET_SOURCE_RESOURCES', sourceResponse.data)
        this.$store.commit('SET_TARGET_RESOURCES', targetResponse.data)
      } catch (error) {
        console.error('搜索失败:', error)
        this.$message.error('搜索失败')
      }
    },

    // 显示创建对话框
    showCreateDialog(selector) {
      this.isCreateOperation = true
      this.currentSelector = selector
      this.currentResource = {
        key: '',
        title: '',
        enTitle: '',
        parentKey: '',
        sort: 0,
        showStatus: true,
        activeStatus: true
      }
      this.editDialogVisible = true
    },

    // 编辑资源
    editResource(resource, selector) {
      this.isCreateOperation = false
      this.currentSelector = selector
      this.currentResource = {...resource}
      this.editDialogVisible = true
    },

    // 处理表单提交
    handleResourceSubmit(resource) {
      if (this.isCreateOperation) {
        this.createResource({
          resource,
          selector: this.currentSelector
        })
      } else {
        this.updateResource({
          resource,
          selector: this.currentSelector
        })
      }
    },

    // 处理复选框变化
    handleCheckChange({data, checked}) {
      if (checked) {
        this.ADD_SELECTED_RESOURCE(data.key)
      } else {
        this.REMOVE_SELECTED_RESOURCE(data.key)
      }
    },

    // 同步资源（显示预览）
    async syncResources() {
      if (this.selectedResources.length === 0) {
        this.$message.warning('请选择要同步的资源')
        return
      }

      try {
        // 传递选中资源的key列表给后端
        const response = await previewSync(
            this.selectedResources,
            this.sourceSelector,
            this.targetSelector
        )
        this.previewResults = response.data
        this.previewDialogVisible = true
      } catch (error) {
        console.error('同步预览失败:', error)
        this.$message.error('同步预览失败')
      }
    },

    // 确认同步
    async confirmSync() {
      try {
        // 传递选中资源的key列表给后端进行同步
        await syncResources(
            this.selectedResources,
            this.sourceSelector,
            this.targetSelector
        )
        this.$message.success('同步成功')
        this.previewDialogVisible = false
        this.CLEAR_SELECTED_RESOURCES()
        this.loadTargetResources()
        this.loadDifferences()
      } catch (error) {
        console.error('同步失败:', error)
        this.$message.error('同步失败')
      }
    },

    // 获取操作类型标签样式
    getOperationTagType(syncType) {
      const typeMap = {
        'ADD': 'success',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'SAME': 'info'
      }
      return typeMap[syncType] || 'info'
    },

    // 获取操作类型文本
    getOperationText(syncType) {
      const textMap = {
        'ADD': '新增',
        'UPDATE': '更新',
        'DELETE': '删除',
        'SAME': '相同'
      }
      return textMap[syncType] || syncType
    },

    // 处理状态切换
    handleToggleStatus(resource, selector) {
      if (!resource || !resource.key) {
        console.error('资源ID不存在', resource);
        this.$message.error('无法更新资源状态：资源ID不存在');
        return;
      }

      this.updateResourceStatus({
        id: resource.key,
        activeStatus: resource.activeStatus,
        selector
      });
    },

    // 处理批量更新状态
    handleBatchUpdateStatus(selector, activeStatus) {
      this.batchUpdateStatus({
        activeStatus,
        selector
      });
    },

    // 处理删除资源
    handleDeleteResource(event, selector) {
      if (!event || !event.key) {
        console.error('删除失败：无效的资源ID', event);
        this.$message.error('无法删除：资源ID无效');
        return;
      }

      this.deleteResource({
        id: event.key,
        selector
      });
    },

    // 移动节点
    async handleMoveNode(node, targetParentKey, targetSort, selector) {
      try {
        await moveNode(node.key, {
          parentKey: targetParentKey,
          sort: targetSort
        }, selector)

        // 重新加载数据
        if (selector === this.sourceSelector) {
          this.loadSourceResources()
        } else {
          this.loadTargetResources()
        }

        this.$message.success('节点移动成功')
      } catch (error) {
        console.error('节点移动失败:', error)
        this.$message.error('节点移动失败')
      }
    }
  }
}
</script>

<style scoped>
.resource-sync {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}

.search-box {
  width: 300px;
}

.trees-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.tree-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tree-panel:first-child {
  border-right: 1px solid #dcdfe6;
}

.panel-header {
  padding: 15px;
  border-bottom: 1px solid #dcdfe6;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-actions {
  display: flex;
  gap: 10px;
}

.panel-content {
  flex: 1;
  overflow: auto;
  padding: 10px;
}

.empty-preview {
  padding: 30px 0;
}
</style>
