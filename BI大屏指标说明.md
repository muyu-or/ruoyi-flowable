# BI 大屏指标说明

> 大屏每 30 秒自动刷新数据，每 10 秒轮播切换多页面板。所有指标均为全公司范围（admin 视角）。

---

## 一、左上区域

### 1.1 任务完成率

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **已完成数** | 全公司已完成节点任务总数（不含已取消） | `COUNT(status='completed')` |
| **完成率** | 已完成数占全部任务的比例 | `finished / (pending + running + finished + rejected) × 100%` |

数据来源：`task_node_execution` 表（`countAllStats` SQL）。

### 1.2 节点实时进度统计（轮播页 1）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **活跃总数** | 所有节点当前待办 + 进行中的任务合计 | `SUM(activeCount)` across all nodes |
| **完成总数** | 所有节点已完成的任务合计 | `SUM(completedCount)` across all nodes |
| **节点数** | 流程中包含的节点类型数量 | `nodeStatusSummary` 数组长度 |

下方表格按节点列出 **活跃数** 和 **完成数**，活跃 > 3 的节点高亮标记。

数据来源：`selectNodeStatusSummary` SQL（`task_node_execution` 表）。

### 1.3 班组实时进度明细（轮播页 2）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **待办总数** | 所有班组待办任务合计 | `SUM(pending)` across all teams |
| **进行中** | 所有班组已认领（进行中）任务合计 | `SUM(running)` across all teams |
| **完成总数** | 所有班组已完成任务合计 | `SUM(finished)` across all teams |

下方表格按班组列出 **待办 / 进行中 / 完成** 数，待办 > 5 的班组高亮标记。

数据来源：`countTeamStatsByStatus` SQL（`task_node_execution` 表）。

---

## 二、顶部状态栏（卡片）

共 9 张卡片，取前 6 个节点 + 3 张 KPI 汇总卡：

| 卡片 | 含义 | 计算方式 |
|------|------|---------|
| **节点 N/M** | N = 当前活跃（待办+进行中），M = 已完成 | `activeCount` / `completedCount` per node |
| **准时率** | 所有应有结果的任务中，实际完成了多少 | `finished / (finished + 超时未解决数) × 100%` |
| **流转效率** | 各节点已完成占比的平均值，反映流程通畅度 | `AVG(completed / (active + completed))` per node × 100% |
| **产品转化率** | 原料转化为产品的比例 | `产品入库量 / 原料入库量 × 100%` |

数据来源：`nodeStatusSummary` + `warningStats` + `materialConversion`。

---

## 三、中间主区域（生产监控）

### 3.1 水球图

完成率的可视化展示，水面高度 = 完成百分比，与左上角完成率数值一致。

### 3.2 设备监控

| 指标 | 含义 |
|------|------|
| **设备卡片** | 展示前 4 台设备，含设备名、编号、位置、状态（在线/离线/故障） |
| **在线数/总数** | 当前在线设备数 / 接入设备总数 |

数据来源：`listDevice` API。

---

## 四、右上区域

### 4.1 产能概览（轮播页 1）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **准时完成率** | 所有应有结果的任务中，实际完成了多少 | `finished / (finished + ΣunresolvedCount) × 100%` |
| **日均完成数** | 每天平均完成几个任务 | `finished / 统计天数`（天数 = stockTrend 数据量，≥1） |

> 分子 = 全公司已完成数（`companyStats.finished`），分母包含当前超时未解决的任务（`warningStats[].unresolvedCount` 之和），使当前仍在超时中的任务能拉低准时率。当所有预警都未解决时，准时率会显著下降，不再显示 100%。

### 4.2 库存概览（轮播页 2）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **库存总量** | 当前在库物料的总数量 | `SUM(totalQty)` across all categories |
| **库存分布** | 按物料大类展示各类的占比进度条 | `(categoryQty / maxCategoryQty) × 100%`，取 top 5 |

数据来源：`inventoryMapper.sumInboundByCategory`。

### 4.3 经营指标（轮播页 1）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **班组达标率** | 准时率 ≥ 80% 的班组占比 | `达标班组数 / 总班组数 × 100%` |
| **流转效率** | 各节点已完成占比的平均值 | 同顶部状态栏流转效率，`AVG(completed/(active+completed))` × 100% |
| **趋势图** | 选定时段内的入库数量变化曲线 | `stockTrend[].inboundQty` 折线图 |

> 班组达标率中的"班组准时率"来自后端 `buildTeamStability()`：
> `onTimeRate = onTimeCount / (completedCount + unresolvedCount)`
> - `onTimeCount`：该班组已完成且从未触发过超时预警的任务数
> - `completedCount`：该班组已完成任务总数
> - `unresolvedCount`：该班组当前仍有未解决超时预警的任务数

### 4.4 物料转化分析（轮播页 2）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **总入库批次** | 选定时段内的入库数据点数 | `stockTrend.length` |
| **日均入库量** | 平均每天入库数量 | `totalInboundQty / 统计天数` |
| **物料子类数** | 库存中不同 `material_subcategory` 的种类数 | `COUNT(DISTINCT material_subcategory)` from inventory |
| **原料入库** | 选定时段内原料入库总量 | `materialConversion.rawInboundQty` |
| **产品入库** | 选定时段内产品入库总量 | `materialConversion.productInboundQty` |
| **产品转化率** | 原料转化为产品的比例 | `productInboundQty / rawInboundQty × 100%` |

趋势图同时展示原料入库量、产品入库量两条折线。

数据来源：`materialConversion` + `materialConversionTrend`（`stockInMapper.selectMaterialConversionSummary` / `selectMaterialConversionTrend`）。

---

## 五、左下区域

### 5.1 节点瓶颈 Top（轮播页 1）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **P50 耗时** | 该节点 50% 的任务在这个时间内完成（中位数），代表正常速度 | Java 端对已完成任务耗时排序后取 50% 分位，单位转为天 |
| **P90 耗时** | 该节点 90% 的任务在这个时间内完成，代表最慢情况 | Java 端排序后取 90% 分位，单位转为天 |

> P90 / P50 ≥ 3 的节点标红并加 ⚠ 图标，表示该节点偶尔会严重卡顿，需重点关注。列表按 P90 降序排列。

数据来源：`selectCompletedDurationsByNode` SQL → Java 端 `quantile()` 分位数计算。

### 5.2 员工完成排行 Top5（轮播页 2）

| 指标 | 含义 |
|------|------|
| **排名** | 第 1-3 名显示奖牌（🥇🥈🥉），之后为数字 |
| **姓名** | 完成人用户名 |
| **完成数** | 该用户完成的节点任务总数 |

数据来源：`countUserFinishedTop(5)` SQL。

---

## 六、中下区域

### 出入库趋势与相关性

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **入库曲线** | 选定时段内每天/每月的入库数量 | `stockTrend[].inboundQty` |
| **出库曲线** | 选定时段内每天/每月的出库数量 | `stockTrend[].outboundQty` |
| **相关系数 r** | 入库和出库的联动程度（皮尔逊相关系数） | JavaScript 端 `_pearson(inArr, outArr)` |

> r 接近 +1 = 入库多则出库也多（供需匹配）；r 接近 0 = 出入库无关联；r 接近 -1 = 入库多时出库少（可能存在积压）。

---

## 七、右下区域

### 7.1 班组效率 Top（轮播页 1）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **准时率** | 该班组有效任务中按时完成的比例 | `onTimeCount / (completedCount + unresolvedCount) × 100%` |
| **均值耗时** | 该班组完成一个任务的平均用时（天） | `meanSeconds / 86400`，保留一位小数 |

> - `onTimeCount`：已完成且从未触发过超时预警（`task_warning` 中无 overdue 记录）
> - `completedCount`：该班组已完成任务总数
> - `unresolvedCount`：该班组当前仍有未解决超时预警的活跃任务数
> - 按准时率降序排列，取前 5 个班组

数据来源：
- 已完成统计：`selectTeamOnTimeStats` SQL（`task_node_execution` JOIN `task_warning`）
- 超时未解决：`selectTeamUnresolvedOverdue` SQL（从 `task_warning` 表按 `team_name` 聚合）
- 耗时均值：`selectCompletedDurationsByTeam` SQL → Java 端 `meanLong()`

### 7.2 预警响应明细（轮播页 2）

| 指标 | 含义 | 计算方式 |
|------|------|---------|
| **响应(天)** | 该节点预警的平均响应时间 | `AVG(响应分钟数) / 1440`，保留一位小数 |
| **次数** | 该节点触发的预警总次数 | `COUNT(*)` from `task_warning` |

> - 对于**已解决**预警：响应时间 = `create_time` → 任务 `complete_time`（或 `end_date`）
> - 对于**未解决**预警：响应时间 = `create_time` → `NOW()`（当前仍处于待响应状态）
> - 两者均计入平均值，因此即使所有预警都未解决，也能看到当前已等待的时间
> - 无数据时显示 `-`

数据来源：`selectWarningStatsByNode` SQL（`task_warning` 表）。

---

## 关键公式速查

| 简称 | 公式 |
|------|------|
| **准时完成率** | `finished / (finished + ΣunresolvedCount)` |
| **班组准时率** | `onTimeCount / (completedCount + unresolvedCount)` |
| **班组达标率** | `COUNT(onTimeRate ≥ 0.8) / COUNT(全部班组)` |
| **流转效率** | `AVG( completed / (active + completed) )` per node |
| **日均完成** | `finished / MAX(stockTrend.length, 1)` |
| **产品转化率** | `productInboundQty / rawInboundQty` |
| **相关系数 r** | 皮尔逊相关系数（入库量 vs 出库量） |
| **P50/P90 耗时** | 已完成任务耗时的 50% / 90% 分位数 |

---

## 数据流转

```
task_node_execution ──→ 任务完成率、节点进度、班组进度、员工排行、节点瓶颈、班组效率
task_warning        ──→ 准时完成率、班组准时率、预警响应明细
stock_in / stock_out──→ 出入库趋势、库存概览、物料转化
production_team     ──→ 班组名称、成员关联
inventory           ──→ 库存分布、物料子类数、成本汇总
device              ──→ 设备监控
```
