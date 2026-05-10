/**
 * v-table-col-width 表格列宽全局配置指令
 *
 * admin 拖拽后保存到后端，所有角色同步使用。
 *
 * 核心原则：
 * - 只保存 col.width（admin 拖拽后的明确意图值），绝不保存 col.realWidth
 *   （因为 realWidth 在弹性布局后可能远大于拖拽值，保存它会导致列宽锁死）
 * - 恢复时，短字段和操作列用固定 width，长文本列用 min-width 允许弹性增长
 * - 确保至少保留一个主业务列完全弹性（无 width/min-width），用于吸收容器剩余空间
 */
import { getColumnsWidth, saveColumnsWidth, isAdmin } from '@/utils/tableColWidth'
import { register, unregister } from '@/utils/tableLayout'

const DEBUG = false
function log(...args) {
  if (DEBUG) console.debug('[colWidth]', ...args)
}

// 始终固定宽度的列标签（这些列即使用户拖拽也保持固定 width，不转为弹性）
const FIXED_LABELS = new Set(['操作'])
// 不参与「至少保留一个弹性列」检查的列类型
const UTILITY_COLUMN_TYPES = new Set(['selection', 'index', 'expand'])

export default {
  name: 'table-col-width',

  inserted(el, binding, vnode) {
    const context = vnode.context
    const tableId = binding.value || 'main'

    const tableComponent = findTableComponent(vnode)
    if (!tableComponent) {
      console.warn('v-table-col-width: 未找到 el-table 组件实例')
      return
    }

    el._tableComponent = tableComponent
    el._tableId = tableId

    register(tableComponent)
    log('表格已注册:', tableId, 'path:', context.$route?.path)

    if (isAdmin()) {
      tableComponent.$on('header-dragend', () => {
        handleHeaderDragend(el, context, tableId, tableComponent)
      })
    }

    loadAndApplyWidth(el, context, tableId, tableComponent)

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
    if (el._unwatchData) { el._unwatchData(); el._unwatchData = null }
    if (el._saveWidthTimer) { clearTimeout(el._saveWidthTimer) }
    if (el._tableComponent) { unregister(el._tableComponent); el._tableComponent = null }
  }
}

async function loadAndApplyWidth(el, context, tableId, tableComponent) {
  const storedWidths = await getColumnsWidth(context, tableId)
  if (!storedWidths) {
    log('无后端配置，使用默认列宽:', tableId)
    return
  }
  log('加载后端列宽配置:', tableId, Object.keys(storedWidths).length, '列')
  context.$nextTick(() => {
    setTimeout(() => {
      applyWidthToColumns(tableComponent, storedWidths)
    }, 100)
  })
}

function handleHeaderDragend(el, context, tableId, tableComponent) {
  if (!isAdmin()) return
  if (el._saveWidthTimer) clearTimeout(el._saveWidthTimer)

  el._saveWidthTimer = setTimeout(async() => {
    const columnsWidth = collectColumnsWidth(tableComponent)
    if (Object.keys(columnsWidth).length > 0) {
      log('保存列宽:', tableId, columnsWidth)
      await saveColumnsWidth(context, tableId, columnsWidth)
    }
  }, 300)
}

function findTableComponent(vnode) {
  if (vnode.componentInstance && vnode.componentInstance.$options.name === 'ElTable') {
    return vnode.componentInstance
  }
  if (vnode.componentInstance) {
    const children = vnode.componentInstance.$children
    for (const child of children) {
      if (child.$options.name === 'ElTable') return child
    }
  }
  return null
}

/**
 * 收集列宽 —— 只保存 col.width（admin 拖拽后的意图值）。
 *
 * 关键：绝不使用 col.realWidth！
 * realWidth 是弹性布局后当前屏幕上的实际渲染宽度，保存它会导致：
 * 1. 大屏上弹性增长的宽度被误存为"用户意图"
 * 2. 下次加载时变成巨大的 min-width，小屏体验极差
 * 3. 每次拖拽 → 保存 → 加载的循环中宽度不断膨胀
 */
function collectColumnsWidth(tableComponent) {
  const columns = {}
  const tableColumns = tableComponent.columns || []

  tableColumns.forEach(col => {
    const label = col.label || col.property
    if (!label) return

    // 只收集有明确 width 的列（admin 拖拽过，或模板声明了 width）
    // min-width 列是弹性列，由 applyWidthToColumns 管理，不需要重复保存
    if (col.width !== undefined && col.width > 0) {
      columns[label] = Math.round(col.width)
    }
  })

  return columns
}

/**
 * 将后端存储的宽度应用到列。
 *
 * 策略：
 * - 操作列 → 固定 width（按钮不能被压缩）
 * - 窄字段（≤130px，如状态、版本、编号）→ 固定 width
 * - 其他列 → min-width（弹性增长，admin 值作为最小宽度底线）
 * - 保证至少一个主业务列完全弹性（无 width/min-width），用于吸收容器剩余空间
 */
function applyWidthToColumns(tableComponent, storedWidths) {
  const tableColumns = tableComponent.columns || []
  let appliedFixed = 0
  let appliedElastic = 0

  // 第一遍：应用保存的列宽
  tableColumns.forEach(col => {
    const label = col.label || col.property
    if (!label) return

    const storedWidth = storedWidths[label]
    if (!storedWidth) return

    // 跳过工具列（selection / index / expand）
    if (UTILITY_COLUMN_TYPES.has(col.type)) return
    // 跳过原本就没有 width 也没有 minWidth 的完全弹性列 —— 让它们保持弹性
    if (col.width === undefined && col.minWidth === undefined) return

    try {
      const isFixed = FIXED_LABELS.has(label) || storedWidth <= 130

      if (isFixed) {
        col.width = storedWidth
        appliedFixed++
      } else {
        col.minWidth = storedWidth
        col.width = undefined
        appliedElastic++
      }
    } catch (e) {
      console.warn('应用列宽失败:', label, e)
    }
  })

  // 第二遍：确保至少有一个主业务列是完全弹性的（吸收剩余空间）
  ensureAtLeastOneElasticColumn(tableColumns, storedWidths)

  if ((appliedFixed + appliedElastic) > 0 && tableComponent.doLayout) {
    tableComponent.$nextTick(() => {
      tableComponent.doLayout()
    })
  }
}

/**
 * 确保至少有一个主业务列完全弹性（无 width 也无 min-width）。
 *
 * 如果所有列都被设置了 width 或 min-width，表格在大屏上没有列能够吸收额外空间，
 * 可能出现右侧留白。此函数找到最宽的 min-width 列（通常是名称/描述等主业务列），
 * 清除其 min-width，使其成为完全弹性列。
 */
function ensureAtLeastOneElasticColumn(tableColumns, storedWidths) {
  // 检查是否已有完全弹性的主业务列
  const hasElastic = tableColumns.some(col => {
    if (UTILITY_COLUMN_TYPES.has(col.type)) return false
    return col.width === undefined && col.minWidth === undefined
  })

  if (hasElastic) return

  // 找到最宽的 min-width 列（最可能是主业务文本列），将其改为完全弹性
  let bestCandidate = null
  let bestMinWidth = 0

  tableColumns.forEach(col => {
    if (UTILITY_COLUMN_TYPES.has(col.type)) return
    if (FIXED_LABELS.has(col.label || col.property)) return
    if (col.minWidth !== undefined && col.minWidth > bestMinWidth) {
      bestMinWidth = col.minWidth
      bestCandidate = col
    }
  })

  if (bestCandidate) {
    log('释放弹性列:', bestCandidate.label || bestCandidate.property, '(min-width:', bestMinWidth + 'px)')
    bestCandidate.minWidth = undefined
  }
}
