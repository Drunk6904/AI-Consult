<template>
  <div class="role-management">
    <h2>角色管理</h2>
    
    <div class="toolbar">
      <button @click="openCreateDialog" class="btn btn-primary">
        <i class="icon-plus"></i> 新建角色
      </button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>角色名称</th>
            <th>角色代码</th>
            <th>描述</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.id">
            <td>{{ role.roleName }}</td>
            <td>{{ role.roleCode }}</td>
            <td>{{ role.description || '-' }}</td>
            <td>
              <span :class="role.status === 1 ? 'status-active' : 'status-inactive'">
                {{ role.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="actions">
              <button @click="editRole(role)" class="btn btn-sm">编辑</button>
              <button @click="deleteRole(role)" class="btn btn-sm btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 创建/编辑角色对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay" @click="closeDialog">
      <div class="dialog" @click.stop>
        <h3>{{ showCreateDialog ? '创建角色' : '编辑角色' }}</h3>
        <form @submit.prevent="saveRole">
          <div class="form-group">
            <label>角色名称</label>
            <input v-model="formData.roleName" type="text" required class="form-control" />
          </div>
          <div class="form-group">
            <label>角色代码</label>
            <input v-model="formData.roleCode" type="text" required class="form-control" 
                   :disabled="showEditDialog" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="formData.description" class="form-control" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="formData.status" class="form-control">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" @click="closeDialog" class="btn">取消</button>
            <button type="submit" class="btn btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import permissionService from '../services/permissionService'
import permissionMixin from '../mixins/permissionMixin'

export default {
  name: 'RoleManagement',
  mixins: [permissionMixin],
  data() {
    return {
      roles: [],
      showCreateDialog: false,
      showEditDialog: false,
      formData: {
        roleName: '',
        roleCode: '',
        description: '',
        status: 1
      },
      editingRoleId: null
    }
  },
  mounted() {
    this.loadRoles()
  },
  methods: {
    async loadRoles() {
      try {
        const data = await permissionService.getRoles()
        if (data.success) {
          this.roles = data.data
        }
      } catch (error) {
        console.error('加载角色失败:', error)
        alert('加载角色失败')
      }
    },

    openCreateDialog() {
      this.formData = {
        roleName: '',
        roleCode: '',
        description: '',
        status: 1
      }
      this.showCreateDialog = true
    },

    editRole(role) {
      this.formData = { ...role }
      this.editingRoleId = role.id
      this.showEditDialog = true
    },

    closeDialog() {
      this.showCreateDialog = false
      this.showEditDialog = false
      this.editingRoleId = null
      this.formData = {}
    },

    async saveRole() {
      try {
        if (this.showCreateDialog) {
          const result = await permissionService.createRole(this.formData)
          if (result.success) {
            alert('角色创建成功')
            this.loadRoles()
            this.closeDialog()
          }
        } else {
          const result = await permissionService.updateRole(this.editingRoleId, this.formData)
          if (result.success) {
            alert('角色更新成功')
            this.loadRoles()
            this.closeDialog()
          }
        }
      } catch (error) {
        console.error('保存角色失败:', error)
        alert(error.response?.data?.message || '保存失败')
      }
    },

    async deleteRole(role) {
      if (!confirm(`确定要删除角色 "${role.roleName}" 吗？`)) {
        return
      }

      try {
        const result = await permissionService.deleteRole(role.id)
        if (result.success) {
          alert('角色删除成功')
          this.loadRoles()
        }
      } catch (error) {
        console.error('删除角色失败:', error)
        alert('删除失败')
      }
    }
  }
}
</script>

<style scoped>
.role-management {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.data-table th {
  background-color: #f5f5f5;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 8px;
}

.status-active {
  color: #52c41a;
}

.status-inactive {
  color: #ff4d4f;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  padding: 24px;
  border-radius: 4px;
  width: 500px;
  max-width: 90%;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background: #f0f0f0;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
}

.btn-sm {
  padding: 4px 12px;
  font-size: 12px;
}
</style>
