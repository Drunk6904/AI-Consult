/**
 * 权限指令
 * 使用方式：
 * <button v-permission="'knowledge:upload'">上传</button>
 * <button v-permission="['knowledge:upload', 'knowledge:manage']">上传</button>
 */
export default {
  inserted(el, binding, vnode) {
    const { value } = binding
    
    // 获取用户权限
    const permissions = JSON.parse(localStorage.getItem('user_permissions') || '[]')
    const roles = JSON.parse(localStorage.getItem('user_roles') || '[]')
    
    if (value && value instanceof Array) {
      // 如果值是数组，检查是否拥有任一权限
      const hasPermission = value.some(code => 
        permissions.includes(code) || 
        roles.includes('SUPER_ADMIN') ||
        roles.includes('ADMIN')
      )
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else if (value) {
      // 如果值是字符串，检查是否拥有该权限
      const hasPermission = permissions.includes(value) || 
                           roles.includes('SUPER_ADMIN') ||
                           roles.includes('ADMIN')
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}
