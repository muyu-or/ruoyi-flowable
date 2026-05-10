/**
 * 全局表格自适应布局工具
 *
 * 单例模式：维护一个全局 resize/zoom/侧边栏 监听器，
 * 所有注册的 el-table 实例在触发时自动 doLayout()。
 *
 * 解决浏览器缩放、窗口大小变化、侧边栏展开收起时
 * 表格列宽异常（右侧留白、操作列溢出、列断层）的问题。
 */

import store from '@/store'

// 已注册的表格实例集合
const tables = new Set()
let resizeTimer = null
let sidebarUnwatch = null
let initialized = false

function init() {
  if (initialized) return
  initialized = true

  // 窗口大小变化（含浏览器缩放触发）
  window.addEventListener('resize', onResize, { passive: true })

  // 标签页切换回来时（缩放后切标签页再切回也会触发）
  document.addEventListener('visibilitychange', onVisibilityChange)

  // 侧边栏展开/收起（只监听 opened 原始值，避免 deep 监听不必要的内部属性变化）
  if (store && store.watch) {
    sidebarUnwatch = store.watch(
      state => state.app && state.app.sidebar && state.app.sidebar.opened,
      () => scheduleDoLayout()
    )
  }
}

function onResize() {
  scheduleDoLayout()
}

function onVisibilityChange() {
  if (!document.hidden) {
    scheduleDoLayout()
  }
}

/**
 * 防抖调度：200ms 内多次触发只执行一次
 */
function scheduleDoLayout() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    // 等待浏览器完成当前绘制周期后再触发布局重算
    requestAnimationFrame(() => {
      doLayoutAll()
    })
  }, 100)
}

/**
 * 对所有已注册表格执行 doLayout()
 * 自动跳过已销毁的实例
 */
function doLayoutAll() {
  const dead = []
  tables.forEach(table => {
    if (table && table.$el && table.doLayout) {
      try {
        table.doLayout()
      } catch (e) {
        // 静默
      }
    } else {
      dead.push(table)
    }
  })
  // 清理已销毁的实例
  dead.forEach(t => tables.delete(t))
}

/**
 * 注册表格实例
 * @param {Object} table el-table 组件实例
 */
export function register(table) {
  if (!table) return
  init()
  tables.add(table)
}

/**
 * 注销表格实例
 * @param {Object} table el-table 组件实例
 */
export function unregister(table) {
  tables.delete(table)
}

/**
 * 立即触发一次全局 doLayout（用于数据加载完成后）
 */
export function refreshAll() {
  // 当前帧立即执行一次
  doLayoutAll()
  // 延迟再执行一次，处理可能级联渲染的列（固定列、tooltip 等）
  scheduleDoLayout()
}
