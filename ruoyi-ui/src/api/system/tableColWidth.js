import request from '@/utils/request'

/**
 * 获取指定表格的列宽配置（所有用户可读）
 * @param routePath 路由路径
 * @param tableId 表格标识
 */
export function getTableColWidth(routePath, tableId = 'main') {
  return request({
    url: '/system/tableColWidth/get',
    method: 'get',
    params: { routePath, tableId }
  })
}

/**
 * 获取指定路由下所有表格的列宽配置
 * @param routePath 路由路径
 */
export function getTableColWidthByRoute(routePath) {
  return request({
    url: '/system/tableColWidth/getByRoute',
    method: 'get',
    params: { routePath }
  })
}

/**
 * 保存列宽配置（仅 admin）
 * @param data { routePath, tableId, columnsConfig }
 */
export function saveTableColWidth(data) {
  return request({
    url: '/system/tableColWidth',
    method: 'post',
    data
  })
}

/**
 * 重置指定表格的列宽配置（仅 admin）
 * @param routePath 路由路径
 * @param tableId 表格标识
 */
export function resetTableColWidth(routePath, tableId = 'main') {
  return request({
    url: '/system/tableColWidth/reset',
    method: 'delete',
    params: { routePath, tableId }
  })
}

/**
 * 重置指定路由下所有表格配置（仅 admin）
 * @param routePath 路由路径
 */
export function resetTableColWidthByRoute(routePath) {
  return request({
    url: '/system/tableColWidth/resetByRoute',
    method: 'delete',
    params: { routePath }
  })
}

/**
 * 重置所有表格列宽配置（仅 admin）
 */
export function resetAllTableColWidth() {
  return request({
    url: '/system/tableColWidth/all',
    method: 'delete'
  })
}
