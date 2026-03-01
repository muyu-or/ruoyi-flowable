// src/components/taskForms/index.js
// 节点 key → 对应 Vue 组件 的映射表
// 在 todo/detail/index.vue 中通过 taskDefinitionKey 查找并动态渲染

import StockInForm from './StockInForm'
import StockOutForm from './StockOutForm'
import PreprocessForm from './PreprocessForm'
import VacuumForm from './VacuumForm'
import BakingForm from './BakingForm'
import TestForm from './TestForm'
import FinalStockInForm from './FinalStockInForm'

export const TASK_FORM_MAP = {
  'Activity_1uqk506': StockInForm,      // 原料检测入库
  'Activity_01xy3yd': StockOutForm,     // 出库
  'Activity_0kzrvj3': PreprocessForm,   // 预处理
  'Activity_17q9igw': VacuumForm,       // 开机真空处理
  'Activity_1qot9f7': BakingForm,       // 烘烤镀膜
  'Activity_0tn05o0': TestForm,         // 测试
  'Activity_1lnd3md': FinalStockInForm  // 产品入库
}
