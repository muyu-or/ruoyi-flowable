// src/components/taskForms/index.js
// 导出两个映射表：
//   TASK_FORM_MAP           — 节点 key → Vue 组件，供 todo/detail 按 taskDefinitionKey 动态渲染（旧机制，向后兼容）
//   TASK_FORM_COMPONENT_MAP — 组件名字符串 → Vue 组件，供 extensionElements 绑定机制按组件名查找（新机制，跨流程复用）

import MainForm from './MainForm'
import StockInForm from './StockInForm'
import StockOutForm from './StockOutForm'
import PreprocessForm from './PreprocessForm'
import VacuumForm from './VacuumForm'
import BakingForm from './BakingForm'
import TestForm from './TestForm'
import FinalStockInForm from './FinalStockInForm'
import DrawingForm from './DrawingForm'
import CoatingFixtureForm from './CoatingFixtureForm'
import FilmDesignForm from './FilmDesignForm'
import VacuumBakingForm from './VacuumBakingForm'

export const TASK_FORM_MAP = {
  'Activity_1uqk506': StockInForm,      // 原料检测入库
  'Activity_01xy3yd': StockOutForm,     // 出库
  'Activity_0kzrvj3': PreprocessForm,   // 预处理
  'Activity_17q9igw': VacuumForm,       // 开机真空处理（旧）
  'Activity_1qot9f7': BakingForm,       // 烘烤镀膜（旧）
  'Activity_0tn05o0': TestForm,         // 测试
  'Activity_1lnd3md': FinalStockInForm, // 产品入库
  'Activity_0rtejx0': DrawingForm,      // 图纸下发
  'Activity_02bpvdf': CoatingFixtureForm, // 镀膜工装设计
  'Activity_0oicg28': FilmDesignForm,   // 膜系设计
  'Activity_1vdsjgn': VacuumBakingForm  // 开机真空烘烤（合并）
}

// 按组件名字符串查组件对象（供新的 extensionElements 绑定机制使用）
export const TASK_FORM_COMPONENT_MAP = {
  'MainForm':         MainForm,         // 主表单（发起）
  'StockInForm':      StockInForm,      // 原料检测入库
  'StockOutForm':     StockOutForm,     // 出库
  'PreprocessForm':   PreprocessForm,   // 预处理
  'VacuumForm':       VacuumForm,       // 开机真空处理（旧）
  'BakingForm':       BakingForm,       // 烘烤镀膜（旧）
  'TestForm':         TestForm,         // 测试
  'FinalStockInForm': FinalStockInForm, // 产品入库
  'DrawingForm':      DrawingForm,      // 图纸下发
  'CoatingFixtureForm': CoatingFixtureForm, // 镀膜工装设计
  'FilmDesignForm':   FilmDesignForm,   // 膜系设计
  'VacuumBakingForm': VacuumBakingForm  // 开机真空烘烤（合并）
}
