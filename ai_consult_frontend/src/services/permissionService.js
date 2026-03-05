import axios from 'axios'

const API_BASE = '/api/v1'

export default {
  /**
   * 获取所有角色
   */
  async getRoles() {
    const response = await axios.get(`${API_BASE}/roles`)
    return response.data
  },

  /**
   * 获取单个角色
   */
  async getRole(id) {
    const response = await axios.get(`${API_BASE}/roles/${id}`)
    return response.data
  },

  /**
   * 创建角色
   */
  async createRole(role) {
    const response = await axios.post(`${API_BASE}/roles`, role)
    return response.data
  },

  /**
   * 更新角色
   */
  async updateRole(id, role) {
    const response = await axios.put(`${API_BASE}/roles/${id}`, role)
    return response.data
  },

  /**
   * 删除角色
   */
  async deleteRole(id) {
    const response = await axios.delete(`${API_BASE}/roles/${id}`)
    return response.data
  },

  /**
   * 获取所有权限
   */
  async getPermissions() {
    const response = await axios.get(`${API_BASE}/permissions`)
    return response.data
  },

  /**
   * 创建权限
   */
  async createPermission(permission) {
    const response = await axios.post(`${API_BASE}/permissions`, permission)
    return response.data
  },

  /**
   * 更新权限
   */
  async updatePermission(id, permission) {
    const response = await axios.put(`${API_BASE}/permissions/${id}`, permission)
    return response.data
  },

  /**
   * 删除权限
   */
  async deletePermission(id) {
    const response = await axios.delete(`${API_BASE}/permissions/${id}`)
    return response.data
  },

  /**
   * 获取所有用户
   */
  async getUsers() {
    const response = await axios.get(`${API_BASE}/users`)
    return response.data
  },

  /**
   * 获取单个用户
   */
  async getUser(id) {
    const response = await axios.get(`${API_BASE}/users/${id}`)
    return response.data
  },

  /**
   * 创建用户
   */
  async createUser(user) {
    const response = await axios.post(`${API_BASE}/users`, user)
    return response.data
  },

  /**
   * 更新用户
   */
  async updateUser(id, user) {
    const response = await axios.put(`${API_BASE}/users/${id}`, user)
    return response.data
  },

  /**
   * 删除用户
   */
  async deleteUser(id) {
    const response = await axios.delete(`${API_BASE}/users/${id}`)
    return response.data
  },

  /**
   * 为用户分配角色
   */
  async assignRoles(userId, roleIds) {
    const response = await axios.post(`${API_BASE}/users/${userId}/roles`, { roleIds })
    return response.data
  },

  /**
   * 更新用户状态
   */
  async updateUserStatus(userId, status) {
    const response = await axios.put(`${API_BASE}/users/${userId}/status`, { status })
    return response.data
  }
}
