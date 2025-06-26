<template>
  <el-dialog
      :title="isCreate ? '新增资源' : '编辑资源'"
      :visible.sync="dialogVisible"
      width="500px"
      @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="form" label-width="80px">
      <el-form-item label="ID" prop="key" v-if="!isCreate">
        <el-input v-model="form.key" disabled></el-input>
      </el-form-item>
      <el-form-item label="ID" prop="key" v-else>
        <el-input v-model="form.key" placeholder="请输入资源ID"></el-input>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题"></el-input>
      </el-form-item>
      <el-form-item label="英文标题" prop="enTitle">
        <el-input v-model="form.enTitle" placeholder="请输入英文标题"></el-input>
      </el-form-item>
      <el-form-item label="父节点" prop="parentKey">
        <el-select v-model="form.parentKey" placeholder="请选择父节点" style="width: 100%">
          <el-option label="无 (根节点)" value=""></el-option>
          <el-option
              v-for="parent in parents"
              :key="parent.value"
              :label="parent.label"
              :value="parent.value"
          >
            <span>{{ parent.label }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ parent.path }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.relativeSortIndex" :min="0" style="width: 100%"></el-input-number>
        <div class="sort-hint">
          <i class="el-icon-info"></i>
          <span v-if="!form.parentKey">排序值: {{ form.relativeSortIndex }}</span>
          <span v-else>
            <template v-if="getParentSort() > 0">
              排序值: {{ getParentSort() }}{{ form.relativeSortIndex.toString().padStart(2, '0') }}
              (父节点: {{ getParentSort() }})
            </template>
            <template v-else>
              排序值: {{ form.sort }}
            </template>
          </span>
        </div>
      </el-form-item>
      <el-form-item label="是否显示" prop="showStatus">
        <el-switch v-model="form.showStatus"></el-switch>
      </el-form-item>
      <el-form-item label="状态" prop="activeStatus">
        <el-tag :type="form.activeStatus ? 'success' : 'danger'">
          {{ form.activeStatus ? '启用' : '停用' }}
        </el-tag>
        <span style="margin-left: 10px; color: #909399; font-size: 12px">
          (状态需通过启用/停用按钮修改)
        </span>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {getAvailableParents} from '@/api/resource'

export default {
  name: 'ResourceEditDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    resource: {
      type: Object,
      default: () => ({
        key: '',
        title: '',
        enTitle: '',
        parentKey: '',
        sort: 0,
        showStatus: true,
        activeStatus: true
      })
    },
    isCreate: {
      type: Boolean,
      default: false
    },
    selector: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      dialogVisible: false,
      form: {
        key: '',
        title: '',
        enTitle: '',
        parentKey: '',
        sort: 0,
        relativeSortIndex: 0,
        showStatus: true,
        activeStatus: true
      },
      sortBase: 100,
      parents: [],
      rules: {
        key: [
          {required: true, message: '请输入资源ID', trigger: 'blur'},
          {min: 1, max: 50, message: 'ID长度在1到50个字符', trigger: 'blur'}
        ],
        title: [
          {required: true, message: '请输入标题', trigger: 'blur'},
          {min: 1, max: 100, message: '标题长度在1到100个字符', trigger: 'blur'}
        ],
        enTitle: [
          {required: false, message: '请输入英文标题', trigger: 'blur'},
          {min: 0, max: 100, message: '英文标题长度在0到100个字符', trigger: 'blur'}
        ]
      }
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.initForm()
        this.loadParents()
      }
    },
    resource: {
      handler(val) {
        if (val && this.dialogVisible) {
          this.initForm()
        }
      },
      deep: true
    },
    'form.parentKey': {
      handler() {
        this.calculateRelativeSortIndex()
      }
    }
  },
  methods: {
    // 初始化表单
    initForm() {
      if (this.resource) {
        this.form = {...this.resource}
        this.calculateRelativeSortIndex()
      }
    },

    // 计算相对排序索引
    calculateRelativeSortIndex() {
      const parentSort = this.getParentSort()
      if (parentSort === 0) {
        // 如果是顶级节点，直接返回
        this.form.relativeSortIndex = this.form.sort
      } else {
        // 如果是子节点，计算与父节点的相对索引
        this.form.relativeSortIndex = this.form.sort % this.sortBase
      }
    },

    // 获取父节点排序值
    getParentSort() {
      if (!this.form.parentKey) return 0

      // 从父节点列表中找到对应的父节点
      const parent = this.parents.find(p => p.value === this.form.parentKey)

      // 检查父节点是否存在且有排序值
      if (parent && parent.sort !== undefined) {
        // 获取父节点的基本排序值
        const baseSort = Math.floor(parent.sort / this.sortBase) * this.sortBase
        return baseSort / this.sortBase
      }

      // 如果无法获取父节点排序值，则从当前节点排序推算
      if (this.form.sort > this.sortBase) {
        // 如果当前节点排序值大于基数，推算父节点排序值
        return Math.floor(this.form.sort / this.sortBase)
      }

      return 0
    },

    // 加载可用父节点
    async loadParents() {
      try {
        const excludeId = this.isCreate ? null : this.resource.key
        const response = await getAvailableParents(this.selector, excludeId)
        this.parents = response.data

        // 加载完父节点后重新计算相对排序索引
        this.calculateRelativeSortIndex()
      } catch (error) {
        console.error('加载父节点失败:', error)
        this.$message.error('加载父节点失败')
      }
    },

    // 提交表单
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          // 计算实际的sort值
          const submitData = {...this.form}

          // 根据相对索引和父节点计算实际的sort值
          const parentSort = this.getParentSort()
          if (!this.form.parentKey || parentSort === 0) {
            // 顶级节点 - 直接使用相对索引作为排序值
            submitData.sort = this.form.relativeSortIndex
          } else {
            // 子节点 - 父节点值*100 + 相对索引
            submitData.sort = parentSort * this.sortBase + this.form.relativeSortIndex
          }

          // 确保排序值在合理范围内
          if (submitData.sort < 0) {
            submitData.sort = 0
          }

          this.$emit('submit', submitData)
          this.dialogVisible = false
        }
      })
    },

    // 关闭对话框
    handleClose() {
      this.$emit('update:visible', false)
      this.$refs.form.resetFields()
    }
  }
}
</script>

<style scoped>
.sort-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
  display: flex;
  align-items: center;
}

.sort-hint i {
  margin-right: 4px;
  color: #E6A23C;
}
</style>
