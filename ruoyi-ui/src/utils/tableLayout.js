/**
 * 全局表格自适应布局工具
 *
 * 单例模式：维护全局 resize/zoom/侧边栏/容器尺寸 监听器，
 * 所有注册的 el-table 实例在触发时自动 doLayout()。
 *
 * 核心改进：
 * - ResizeObserver 监听每个表格容器真实尺寸变化（浏览器缩放、父容器变化、标签页切换）
 * - transitionend 精确捕获侧边栏动画结束
 * - 防抖 + rAF 合并快速连续触发
 */
import store from '@/store'

const DEBUG = false

function log(...args) {
  if (DEBUG) console.debug('[tableLayout]', ...args)
}

const tables = new Set()
const observers = new Map()
let resizeTimer = null
let sidebarTransitionAttached = false
let initialized = false

function init() {
  if (initialized) return
  initialized = true

  window.addEventListener('resize', () => scheduleDoLayout(120), { passive: true })
  document.addEventListener('visibilitychange', onVisibilityChange)
  attachSidebarTransition()

  if (store && store.watch) {
    store.watch(
      state => state.app && state.app.sidebar && state.app.sidebar.opened,
      () => scheduleDoLayout(120)
    )
  }
}

function attachSidebarTransition() {
  if (sidebarTransitionAttached) return
  const tryAttach = () => {
    const sidebar = document.querySelector('.sidebar-container')
    if (sidebar) {
      sidebar.addEventListener('transitionend', (e) => {
        if (e.propertyName === 'width' || e.propertyName === 'margin-left') {
          log('侧边栏 transitionend:', e.propertyName)
          scheduleDoLayout(50)
        }
      })
      // 也监听 main-container 的 margin-left transition
      const mainContainer = document.querySelector('.main-container')
      if (mainContainer) {
        mainContainer.addEventListener('transitionend', (e) => {
          if (e.propertyName === 'margin-left') {
            log('主容器 transitionend:', e.propertyName)
            scheduleDoLayout(50)
          }
        })
      }
      sidebarTransitionAttached = true
      log('侧边栏 transition 监听已绑定')
    } else {
      setTimeout(tryAttach, 300)
    }
  }
  setTimeout(tryAttach, 500)
}

function onVisibilityChange() {
  if (!document.hidden) {
    scheduleDoLayout(60)
  }
}

function scheduleDoLayout(delay = 100) {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    requestAnimationFrame(() => {
      doLayoutAll()
    })
  }, delay)
}

function doLayoutAll() {
  const dead = []
  tables.forEach(table => {
    if (table && table.$el && table.doLayout) {
      try {
        table.doLayout()
      } catch (e) { /* 静默 */ }
    } else {
      dead.push(table)
    }
  })
  dead.forEach(t => {
    tables.delete(t)
    if (observers.has(t)) {
      observers.get(t).disconnect()
      observers.delete(t)
    }
  })
}

function setupTableObserver(table) {
  if (!table.$el) return
  if (observers.has(table)) {
    observers.get(table).disconnect()
  }
  try {
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const w = entry.contentRect.width
        if (w > 0) {
          log('ResizeObserver 触发, 容器宽度:', Math.round(w) + 'px')
          scheduleDoLayout(100)
          break
        }
      }
    })
    observer.observe(table.$el)
    observers.set(table, observer)
    log('ResizeObserver 已绑定:', table.$el.className || table.$el.tagName)
  } catch (e) {
    // ResizeObserver 不可用，回退到 window.resize 方案
  }
}

export function register(table) {
  if (!table) return
  init()
  tables.add(table)
  table.$nextTick(() => setupTableObserver(table))
}

export function unregister(table) {
  tables.delete(table)
  if (observers.has(table)) {
    observers.get(table).disconnect()
    observers.delete(table)
  }
}

export function refreshAll() {
  doLayoutAll()
  scheduleDoLayout()
}
