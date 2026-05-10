/**
 * v-table-col-width 表格列宽全局配置指令
 *
 * admin 拖拽后保存到后端，所有角色同步使用
 * 普通角色只读使用 admin 配置，不保存
 */

import { getColumnsWidth, saveColumnsWidth, isAdmin } from '@/utils/tableColWidth'
import { register, unregister } from '@/utils/tableLayout'

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

    // 保存引用用于 unbind 清理
    el._tableComponent = tableComponent

    // 注册到全局自适应布局（resize/zoom/侧边栏变化时自动 doLayout）
    register(tableComponent)

    // admin 监听拖拽并保存到后端
    if (isAdmin()) {
      tableComponent.$on('header-dragend', (newWidth, oldWidth, column, event) => {
        handleHeaderDragend(el, context, tableId, tableComponent)
      })
    }

    // 所有用户加载后端配置
    loadAndApplyWidth(el, context, tableId, tableComponent)

    // 监听表格数据变化，数据加载完成后重新触发布局
    const unwatchData = context.$watch(
      function() { return tableComponent.data },
      function(newVal) {
        if (newVal && newVal.length > 0) {
          tableComponent.$nextTick(() => {
            if (tableComponent.doLayout) tableComponent.doLayout()
          })
        }
      }
    )
    el._unwatchData = unwatchData
  },

  unbind(el) {
    // 清理数据监听
    if (el._unwatchData) {
      el._unwatchData()
      el._unwatchData = null
    }
    // 清理定时器
    if (el._saveWidthTimer) {
      clearTimeout(el._saveWidthTimer)
    }
    // 从全局自适应布局中注销
    if (el._tableComponent) {
      unregister(el._tableComponent)
      el._tableComponent = null
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
    const label = col.label || col.property
    if (!label) return

    // 只收集原本就设了固定 width 的列，min-width/无width 的列保持弹性不保存
    if (col.width !== undefined) {
      const width = col.realWidth || col.width
      if (width && width > 0) {
        columns[label] = Math.round(width)
      }
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

    try {
      // 只对原本就有 width 的列应用缓存宽度（min-width 列保持弹性）
      if (col.width !== undefined) {
        col.width = storedWidth
        applied = true
      }
      // 不再直接改写 col.realWidth，让 Element-UI 的布局引擎自行计算
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
