/**
 * v-table-col-width 表格列宽全局配置指令
 *
 * admin 拖拽后保存到后端，所有角色同步使用
 * 普通角色只读使用 admin 配置，不保存
 */

import { getColumnsWidth, saveColumnsWidth, isAdmin } from '@/utils/tableColWidth'

export default {
  name: 'table-col-width',

  inserted(el, binding, vnode) {
    const context = vnode.context
    const tableId = binding.value || 'main'

    // 查找 el-table 组件实例
    const tableComponent = findTableComponent(vnode)
    if (!tableComponent) {
      console.warn('v-table-col-width: 未找到 el-table 组件实例')
      return
    }

    // admin 监听拖拽并保存到后端
    if (isAdmin()) {
      tableComponent.$on('header-dragend', (newWidth, oldWidth, column, event) => {
        handleHeaderDragend(el, context, tableId, tableComponent)
      })
    }

    // 所有用户加载后端配置
    loadAndApplyWidth(el, context, tableId, tableComponent)
  },

  unbind(el) {
    // 清理定时器
    if (el._saveWidthTimer) {
      clearTimeout(el._saveWidthTimer)
    }
  }
}

/**
 * 从后端加载配置并应用到表格
 */
async function loadAndApplyWidth(el, context, tableId, tableComponent) {
  const storedWidths = await getColumnsWidth(context, tableId)
  if (!storedWidths) {
    return // 无配置，使用 Vue 默认宽度
  }

  // 延迟执行，确保表格已完全渲染
  context.$nextTick(() => {
    setTimeout(() => {
      applyWidthToColumns(tableComponent, storedWidths)
    }, 100)
  })
}

/**
 * 处理列宽拖拽结束（仅 admin 触发保存）
 */
function handleHeaderDragend(el, context, tableId, tableComponent) {
  if (!isAdmin()) {
    return
  }

  // 防抖保存
  if (el._saveWidthTimer) {
    clearTimeout(el._saveWidthTimer)
  }

  el._saveWidthTimer = setTimeout(async() => {
    const columnsWidth = collectColumnsWidth(tableComponent)
    await saveColumnsWidth(context, tableId, columnsWidth)
  }, 300)
}

/**
 * 查找 el-table 组件实例
 */
function findTableComponent(vnode) {
  // 直接是 el-table
  if (vnode.componentInstance && vnode.componentInstance.$options.name === 'ElTable') {
    return vnode.componentInstance
  }

  // 递归查找子组件
  if (vnode.componentInstance) {
    const children = vnode.componentInstance.$children
    for (const child of children) {
      if (child.$options.name === 'ElTable') {
        return child
      }
    }
  }

  return null
}

/**
 * 收集当前所有列宽
 */
function collectColumnsWidth(tableComponent) {
  const columns = {}
  const tableColumns = tableComponent.columns || []

  tableColumns.forEach(col => {
    // 获取列的 label（跳过无 label 的特殊列）
    const label = col.label || col.property
    if (!label) return

    // 获取当前宽度
    const width = col.realWidth || col.width
    if (width && width > 0) {
      columns[label] = Math.round(width)
    }
  })

  return columns
}

/**
 * 将存储的宽度应用到列
 */
function applyWidthToColumns(tableComponent, storedWidths) {
  const tableColumns = tableComponent.columns || []
  let applied = false

  tableColumns.forEach(col => {
    const label = col.label || col.property
    if (!label) return

    const storedWidth = storedWidths[label]
    if (!storedWidth) return

    // 尝试修改列宽
    try {
      // 如果列原本有 width 属性
      if (col.width !== undefined) {
        col.width = storedWidth
        applied = true
      }

      // 如果列只有 min-width，转换为 width
      if (col.minWidth !== undefined && col.width === undefined) {
        col.width = storedWidth
        applied = true
      }

      // 设置 realWidth（Element-UI 内部使用）
      col.realWidth = storedWidth
    } catch (e) {
      console.warn('应用列宽失败:', label, e)
    }
  })

  // 触发表格重新布局
  if (applied && tableComponent.doLayout) {
    tableComponent.$nextTick(() => {
      tableComponent.doLayout()
    })
  }
}
