<template>
  <div class="user-management">
    <h2>用户管理</h2>
    
    <div class="toolbar">
      <button @click="showCreateDialog = true" class="btn btn-primary" v-permission="'user:create'">
        <i class="icon-plus"></i> 新建用户
      </button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>手机</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span v-for="role in user.roles" :key="role.id" class="role-tag">
                {{ role.roleName }}
              </span>
            </td>
            <td>
              <span :class="user.status === 1 ? 'status-active' : 'status-inactive'">
                {{ user.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="actions">
              <button @click="editUser(user)" class="btn btn-sm" v-permission="'user:update'">编辑</button>
              <button @click="showAssignRoleDialog(user)" class="btn btn-sm" v-permission="'user:role'">分配角色</button>
              <button @click="toggleUserStatus(user)" class="btn btn-sm">
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
              <button @click="deleteUser(user)" class="btn btn-sm btn-danger" v-permission="'user:delete'">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 创建/编辑用户对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay" @click="closeDialog">
      <div class="dialog" @click.stop>
        <h3>{{ showCreateDialog ? '创建用户' : '编辑用户' }}</h3>
        <form @submit.prevent="saveUser">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="formData.username" type="text" required class="form-control" 
                   :disabled="showEditDialog" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="formData.email" type="email" class="form-control" />
          </div>
          <div class="form-group">
            <label>手机</label>
            <input v-model="formData.phone" type="text" class="form-control" />
          </div>
          <div class="form-group" v-if="showCreateDialog">
            <label>密码</label>
            <input v-model="formData.password" type="password" required class="form-control" />
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

    <!-- 分配角色对话框 -->
    <div v-if="showAssignDialog" class="dialog-overlay" @click="closeAssignDialog">
      <div class="dialog" @click.stop>
        <h3>分配角色 - {{ assigningUser?.username }}</h3>
        <form @submit.prevent="saveUserRoles">
          <div class="form-group">
            <label>选择角色</label>
            <div class="role-checkboxes">
              <div v-for="role in allRoles" :key="role.id" class="checkbox-item">
                <input 
                  type="checkbox" 
                  :id="'role-' + role.id"
                  :value="role.id"
                  v-model="selectedRoleIds"
                />
                <label :for="'role-' + role.id">{{ role.roleName }}</label>
              </div>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" @click="closeAssignDialog" class="btn">取消</button>
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
  name: 'UserManagement',
  mixins: [permissionMixin],
  data() {
    return {
      users: [],
      allRoles: [],
      showCreateDialog: false,
      showEditDialog: false,
      showAssignDialog: false,
      formData: {
        username: '',
        email: '',
        phone: '',
        password: '',
        status: 1
      },
      editingUserId: null,
      assigningUser: null,
      selectedRoleIds: []
    }
  },
  mounted() {
    this.loadUsers()
    this.loadRoles()
  },
  methods: {
    async loadUsers() {
      try {
        const data = await permissionService.getUsers()
        if (data.success) {
          this.users = data.data
        }
      } catch (error) {
        console.error('加载用户失败:', error)
        alert('加载用户失败')
      }
    },

    async loadRoles() {
      try {
        const data = await permissionService.getRoles()
        if (data.success) {
          this.allRoles = data.data
        }
      } catch (error) {
        console.error('加载角色失败:', error)
      }
    },

    editUser(user) {
      this.formData = { ...user }
      this.editingUserId = user.id
      this.showEditDialog = true
    },

    showAssignRoleDialog(user) {
      this.assigningUser = user
      this.selectedRoleIds = user.roles.map(r => r.id)
      this.showAssignDialog = true
    },

    closeDialog() {
      this.showCreateDialog = false
      this.showEditDialog = false
      this.editingUserId = null
      this.formData = {}
    },

    closeAssignDialog() {
      this.showAssignDialog = false
      this.assigningUser = null
      this.selectedRoleIds = []
    },

    async saveUser() {
      try {
        if (this.showCreateDialog) {
          const result = await permissionService.createUser(this.formData)
          if (result.success) {
            alert('用户创建成功')
            this.loadUsers()
            this.closeDialog()
          }
        } else {
          const result = await permissionService.updateUser(this.editingUserId, this.formData)
          if (result.success) {
            alert('用户更新成功')
            this.loadUsers()
            this.closeDialog()
          }
        }
      } catch (error) {
        console.error('保存用户失败:', error)
        alert(error.response?.data?.message || '保存失败')
      }
    },

    async saveUserRoles() {
      try {
        const result = await permissionService.assignRoles(this.assigningUser.id, this.selectedRoleIds)
        if (result.success) {
          alert('角色分配成功')
          this.loadUsers()
          this.closeAssignDialog()
        }
      } catch (error) {
        console.error('分配角色失败:', error)
        alert('分配失败')
      }
    },

    async toggleUserStatus(user) {
      if (!confirm(`确定要${user.status === 1 ? '禁用' : '启用'}用户 "${user.username}" 吗？`)) {
        return
      }

      try {
        const newStatus = user.status === 1 ? 0 : 1
        const result = await permissionService.updateUserStatus(user.id, newStatus)
        if (result.success) {
          alert('状态更新成功')
          this.loadUsers()
        }
      } catch (error) {
        console.error('更新状态失败:', error)
        alert('更新失败')
      }
    },

    async deleteUser(user) {
      if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) {
        return
      }

      try {
        const result = await permissionService.deleteUser(user.id)
        if (result.success) {
          alert('用户删除成功')
          this.loadUsers()
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        alert('删除失败')
      }
    }
  }
}
</script>

<style scoped>
.user-management {
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

.role-tag {
  display: inline-block;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 2px 8px;
  margin-right: 4px;
  font-size: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.role-checkboxes {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.checkbox-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.checkbox-item input[type="checkbox"] {
  margin-right: 8px;
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
