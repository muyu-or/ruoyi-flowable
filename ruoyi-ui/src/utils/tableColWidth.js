/**
 * 表格列宽全局配置工具函数
 *
 * admin 配置保存到后端数据库，所有角色同步使用
 * 普通角色只读，不允许保存自己的配置
 */

import store from '@/store'
import { getTableColWidth, saveTableColWidth, resetTableColWidth } from '@/api/system/tableColWidth'

const VERSION = '2.0'

/**
 * 判断当前用户是否为 admin
 */
export function isAdmin() {
  const roles = store.getters && store.getters.roles
  return roles && roles.includes('admin')
}

/**
 * 获取列宽配置（从后端读取 admin 全局配置）
 * @param context Vue 组件上下文
 * @param tableId 表格标识
 * @returns Promise<Object|null> 列宽配置
 */
export async function getColumnsWidth(context, tableId = 'main') {
  const routePath = context.$route?.path || ''

  if (!routePath) {
    return null
  }

  try {
    const response = await getTableColWidth(routePath, tableId)
    if (response.data && response.data.columnsConfig) {
      const config = JSON.parse(response.data.columnsConfig)
      // 版本检查
      if (config.version === VERSION) {
        return config.columns
      }
    }
  } catch (e) {
    // API 失败时静默处理，使用默认宽度
    console.warn('表格列宽配置读取失败，使用默认宽度:', e.message)
  }

  return null
}

/**
 * 保存列宽配置（仅 admin 可保存到后端）
 * @param context Vue 组件上下文
 * @param tableId 表格标识
 * @param columnsWidth 列宽配置 { label: width }
 * @returns Promise<boolean> 是否成功
 */
export async function saveColumnsWidth(context, tableId, columnsWidth) {
  if (!isAdmin()) {
    // 普通角色不允许保存
    return false
  }

  const routePath = context.$route?.path || ''
  if (!routePath) {
    return false
  }

  const data = {
    routePath,
    tableId,
    columnsConfig: JSON.stringify({
      version: VERSION,
      columns: columnsWidth,
      updatedAt: new Date().toISOString()
    })
  }

  try {
    await saveTableColWidth(data)
    return true
  } catch (e) {
    console.error('表格列宽配置保存失败:', e.message)
    return false
  }
}

/**
 * 重置列宽配置（仅 admin）
 * @param context Vue 组件上下文
 * @param tableId 表格标识
 * @returns Promise<boolean> 是否成功
 */
export async function clearColumnsWidth(context, tableId = 'main') {
  if (!isAdmin()) {
    return false
  }

  const routePath = context.$route?.path || ''
  if (!routePath) {
    return false
  }

  try {
    await resetTableColWidth(routePath, tableId)
    return true
  } catch (e) {
    console.error('表格列宽配置重置失败:', e.message)
    return false
  }
}
