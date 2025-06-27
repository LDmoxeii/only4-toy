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
      <el-form-item label="节点路径" prop="nodePath" v-if="!isCreate">
        <el-input v-model="form.nodePath" disabled></el-input>
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
        nodePath: '',
        sort: 0, // 保留sort字段，但不再显示和编辑
        showStatus: true,
        activeStatus: true
      },

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
    }
  },
  methods: {
    // 初始化表单
    initForm() {
      if (this.resource) {
        this.form = {...this.resource}
      }
    },

    // 加载可用父节点
    async loadParents() {
      try {
        const response = await getAvailableParents(this.selector)

        // 过滤掉自己，防止循环引用
        if (!this.isCreate && this.form.key) {
          this.parents = response.data.filter(parent => parent.value !== this.form.key);

          // 同时过滤掉自己的所有子节点，避免循环引用
          this.parents = this.parents.filter(parent => {
            // 如果父节点路径包含当前节点的key，说明是当前节点的子节点
            if (parent.path && parent.path.includes('/' + this.form.key + '/')) {
              return false;
            }
            return true;
          });
        } else {
          this.parents = response.data;
        }
      } catch (error) {
        console.error('加载父节点失败:', error)
        this.$message.error('加载父节点失败')
      }
    },

    // 提交表单
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          // 创建提交数据的副本
          const submitData = {...this.form}

          // 创建时不需要手动设置排序，后端会自动处理
          // 编辑时保留原有排序值

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
