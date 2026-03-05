/**
 * 权限检查混入
 */
export default {
  computed: {
    /**
     * 获取当前用户的权限列表
     */
    userPermissions() {
      const permissions = localStorage.getItem('user_permissions')
      return permissions ? JSON.parse(permissions) : []
    },

    /**
     * 获取当前用户的角色列表
     */
    userRoles() {
      const roles = localStorage.getItem('user_roles')
      return roles ? JSON.parse(roles) : []
    }
  },

  methods: {
    /**
     * 检查是否拥有指定权限
     * @param {string} permission - 权限代码
     * @returns {boolean}
     */
    hasPermission(permission) {
      return this.userPermissions.includes(permission)
    },

    /**
     * 检查是否拥有指定角色
     * @param {string} role - 角色代码
     * @returns {boolean}
     */
    hasRole(role) {
      return this.userRoles.includes(role)
    },

    /**
     * 检查是否拥有任一权限
     * @param {string[]} permissions - 权限代码列表
     * @returns {boolean}
     */
    hasAnyPermission(permissions) {
      return permissions.some(permission => this.userPermissions.includes(permission))
    },

    /**
     * 检查是否拥有任一角色
     * @param {string[]} roles - 角色代码列表
     * @returns {boolean}
     */
    hasAnyRole(roles) {
      return roles.some(role => this.userRoles.includes(role))
    }
  }
}
