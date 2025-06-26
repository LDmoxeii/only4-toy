<template>
  <div class="resource-tree">
    <el-tree
        :data="treeData"
        node-key="key"
        :props="defaultProps"
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
    >
      <div class="custom-tree-node" slot-scope="{ node, data }">
        <div
            class="node-content"
            :class="getNodeStatusClass(data.key)"
        >
          <el-checkbox
              v-model="selectedKeys[data.key]"
              @change="(val) => handleCheckChange(val, data)"
          ></el-checkbox>
          <span class="node-label" @dblclick="handleEditNode(data)">{{ data.title }}</span>
          <div class="node-actions">
            <el-tooltip content="启用/停用" placement="top" v-if="showActionButtons">
              <el-button
                  type="text"
                  size="mini"
                  :icon="data.activeStatus ? 'el-icon-close' : 'el-icon-check'"
                  @click.stop="handleToggleStatus(data)"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" v-if="showActionButtons">
              <el-button
                  type="text"
                  size="mini"
                  icon="el-icon-delete"
                  @click.stop="handleDelete(data)"
              ></el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </el-tree>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'

export default {
  name: 'ResourceTree',
  props: {
    resources: {
      type: Array,
      default: () => []
    },
    isSourceTree: {
      type: Boolean,
      default: false
    },
    showActionButtons: {
      type: Boolean,
      default: true
    },
    selector: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      defaultProps: {
        children: 'children',
        label: 'title'
      },
      selectedKeys: {}
    }
  },
  computed: {
    ...mapGetters(['syncStatus', 'isSelected']),
    treeData() {
      return this.transformToTreeData(this.resources)
    }
  },
  methods: {
    // 将扁平数据转换为树形结构
    transformToTreeData(resources) {
      if (!resources || resources.length === 0) return []

      // 创建映射表
      const map = {}
      resources.forEach(item => {
        map[item.key] = {...item, children: []}
      })

      // 构建树形结构
      const treeData = []
      resources.forEach(item => {
        const node = map[item.key]

        // 根节点直接加入结果
        if (!item.parentKey || item.parentKey === '') {
          treeData.push(node)
        } else if (map[item.parentKey]) {
          // 添加到父节点的children
          map[item.parentKey].children.push(node)
        } else {
          // 找不到父节点，当作根节点
          treeData.push(node)
        }
      })

      return treeData
    },

    // 获取节点状态类名
    getNodeStatusClass(key) {
      const status = this.syncStatus(key)
      if (!status || status === 'SAME') return 'status-same'
      return {
        'ADD': 'status-add',
        'UPDATE': 'status-update',
        'DELETE': 'status-delete'
      }[status] || ''
    },

    // 处理节点点击
    handleNodeClick(data) {
      this.$emit('node-click', data)
    },

    // 处理编辑节点
    handleEditNode(data) {
      this.$emit('edit-node', data)
    },

    // 处理删除
    handleDelete(data) {
      this.$confirm(`确认删除资源 "${data.title}"?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete-node', data)
      }).catch(() => {
      })
    },

    // 处理切换状态
    handleToggleStatus(data) {
      const newStatus = !data.activeStatus
      const actionText = newStatus ? '启用' : '停用'

      this.$confirm(`确认${actionText}资源 "${data.title}"?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('toggle-status', {...data, activeStatus: newStatus})
      }).catch(() => {
      })
    },

    // 处理复选框变化
    handleCheckChange(checked, data) {
      this.$emit('check-change', {data, checked})
    }
  }
}
</script>

<style scoped>
.resource-tree {
  width: 100%;
  height: 100%;
  overflow: auto;
}

/* 增加树节点间距 */
/deep/ .el-tree-node__content {
  height: 36px;
  margin-bottom: 5px;
}

/deep/ .el-tree-node__children .el-tree-node {
  margin-top: 3px;
}

.custom-tree-node {
  width: 100%;
  display: flex;
  align-items: center;
}

.node-content {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 2px 0;
  border: 1px solid transparent;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.node-label {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
  color: #303133;
}

.node-actions {
  display: flex;
  align-items: center;
  visibility: hidden;
  margin-left: 8px;
}

.node-content:hover .node-actions {
  visibility: visible;
}

/* 状态样式 - 使用对比度更高的颜色并加粗边框 */
.status-same {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  border-width: 1px;
}

.status-add {
  background-color: #e8f5e9;
  border-color: #4caf50;
  border-width: 1px;
}

.status-update {
  background-color: #fff3e0;
  border-color: #ff9800;
  border-width: 1px;
}

.status-delete {
  background-color: #ffebee;
  border-color: #f44336;
  border-width: 1px;
}

/* 确保选中状态不会影响背景色 */
/deep/ .is-current > .el-tree-node__content {
  background-color: transparent !important;
}
</style>
