# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Verification

每次修改 Java / 后端代码后，**必须**运行以下命令验证编译，完成前不得进行下一步：

```bash
# 验证单个受影响模块（推荐）
mvn compile -pl <受影响模块> -am -q

# 例：修改了 ruoyi-flowable 模块
mvn compile -pl ruoyi-flowable -am -q

# 例：修改了 ruoyi-system 或 ruoyi-manage
mvn compile -pl ruoyi-system -am -q
mvn compile -pl ruoyi-manage -am -q
```

若 Maven 无法执行（环境问题），必须明确标注 **"⚠️ 未验证编译"** 并列出所有修改的文件路径，不得假设代码正确。

## File Generation Rules

- **禁止**生成任何未被明确要求的文档文件、README、设计文档、GUIDE、SUMMARY 等 `.md` 文件。
- 只创建直接实现所需的源代码文件（`.java`、`.vue`、`.xml`、`.sql`）。
- 如需新建文件，必须先说明原因并获得确认（见 Scope Control）。
- 不得对需求范围外的文件做重构或"顺手改进"。

## Flowable & RuoYi Conventions

### Java 导入规范
- fastjson 使用 `com.alibaba.fastjson2`（**不是** fastjson 1.x）
- IdentityLink 从 `org.flowable.identitylink.api.IdentityLink` 导入
- 不得假设 Flowable API 方法名——使用前**必须** grep 现有代码确认签名

### 使用 API 前必做的确认步骤
在调用任何 Flowable 服务方法或常量之前，先执行：
```bash
# 将下方 <方法名>/<常量名> 替换为实际要查找的标识符后执行
grep -r "<方法名>" ruoyi-flowable/src --include="*.java" | head -10
grep -r "<常量名>" ruoyi-flowable/src --include="*.java" | head -10
```
禁止假设以下内容存在（历史上多次导致编译失败）：
- `getProcessDefinitionKey()` — 需确认实际方法名
- `getExecution()` — 需确认实际方法名
- `CANDIDATE_TEAMS` 常量 — 查看 `ProcessConstants.java` 确认实际常量名

### Vue 前端规范
- 响应式新增属性使用 `this.$set(obj, 'key', value)`，不得直接赋值
- 数组更新使用 Vue 数组变异方法（push/splice 等），不得直接替换索引
- Element-UI 版本为 2.15.14（Vue 2 版本）

## Scope Control

### 分阶段实现原则
大型功能**必须**按层分阶段，每阶段独立可编译：

| 阶段 | 内容 | 验证方式 |
|------|------|---------|
| Phase 1 | 实体类 + Mapper 接口 + XML | `mvn compile` 通过 |
| Phase 2 | Service 接口 + ServiceImpl | `mvn compile` 通过 |
| Phase 3 | Controller | `mvn compile` 通过 |
| Phase 4 | 前端 Vue 组件 | `npm run lint` 通过 |

- 每阶段完成并**编译通过**后，停下来汇报结果，等待确认再进入下一阶段。
- **不做一次性端到端实现**（从 mapper 到 Vue 全部写完再编译）。
- 涉及超过 **5 个文件**的改动，主动建议拆分为多个会话，防止 context 溢出。

### 开始编码前的确认步骤
实现任何功能前，先用一句话说明（示例）：
> "我将修改 X、Y、Z 文件，实现 A 功能。"
获得确认后再动手，不得在确认范围外修改其他文件。

## Project Overview

RuoYi-Flowable v3.8.9 — a management system based on the RuoYi framework with Flowable workflow engine integration. The business domain covers inventory management (库存管理), stock in/out tracking, production team management (班组管理), and test report templates.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 2.5.15, Java 8 (ruoyi-manage uses Java 11) |
| Workflow | Flowable 6.8.0 (excludes flowable-spring-security) |
| ORM | MyBatis (primary) + MyBatis-Plus 3.4.0 (flowable module) |
| Database | MySQL (schema: `flowable`), Druid connection pool |
| Cache | Redis (localhost:6379, Lettuce client) |
| Security | Spring Security 5.7.12 + JWT 0.9.1 |
| Frontend | Vue 2.6.12, Element-UI 2.15.14, Vue CLI 4.x |
| BPMN | bpmn-js 11.1.0 (frontend process designer) |

## Module Dependency Graph

```
ruoyi-admin (entry point, port 8080)
├── ruoyi-framework → ruoyi-system → ruoyi-common
├── ruoyi-quartz → ruoyi-common
├── ruoyi-generator → ruoyi-common
├── ruoyi-flowable → ruoyi-framework, ruoyi-system, ruoyi-common, ruoyi-manage
└── ruoyi-manage → ruoyi-framework
```

**Module purposes:**
- **ruoyi-admin** — Web entry point. All REST controllers for system management (user, role, dept, menu, dict, config, monitor). Contains `application.yml` and `application-druid.yml`.
- **ruoyi-common** — Shared utilities: BaseController, AjaxResult, RedisCache, custom annotations, exception types, security utils.
- **ruoyi-framework** — Infrastructure: Spring Security config, web filters, AOP aspects, datasource config, interceptors.
- **ruoyi-system** — System domain entities and mappers: SysUser, SysRole, SysDept, SysMenu, plus Flowable-related system entities (SysForm, SysListener, SysExpression, SysDeployForm).
- **ruoyi-flowable** — Flowable workflow integration: process definition/instance/task controllers, custom BPMN diagram rendering, EL expression evaluation (Aviator 5.3.3), execution/task listeners, WebSocket support.
- **ruoyi-manage** — Business domain: Inventory, StockIn, StockOut, ProductionTeam, ProductionTeamUser, ReportTemplate. Uses Lombok.
- **ruoyi-quartz** — Quartz scheduled task management.
- **ruoyi-generator** — Code generation from database tables using Velocity templates.

## Build & Run Commands

### Backend
```bash
# Build all modules
mvn clean package

# Run the application (entry module)
mvn spring-boot:run -pl ruoyi-admin

# Run tests
mvn test

# Build a single module
mvn clean package -pl ruoyi-manage -am
```

### Frontend (from ruoyi-ui/)
```bash
npm install
npm run dev          # Dev server on port 80, proxies /dev-api → localhost:8080
npm run build:prod   # Production build → dist/
npm run build:stage  # Staging build
npm run lint         # ESLint check
```

## Key Configuration Files

- `ruoyi-admin/src/main/resources/application.yml` — Server port (8080), Redis, token config (header: Authorization, 30min expiry), MyBatis mapper locations, Flowable settings, XSS filter, Swagger (/dev-api)
- `ruoyi-admin/src/main/resources/application-druid.yml` — MySQL datasource (localhost:3306/flowable, root/123456), Druid pool (5-20 connections), monitoring at /druid/*
- `ruoyi-ui/vue.config.js` — Dev proxy config, Gzip compression, SVG sprite loader for `src/assets/icons/`, code splitting (element-ui, libs, commons chunks)
- `ruoyi-ui/.env.development` — VUE_APP_BASE_API = '/dev-api'

## Backend Architecture Patterns

**Request flow:** Controller → Service (interface + impl) → Mapper (interface + XML) → Database

**Naming conventions:**
- Controllers: `{Entity}Controller` with `@PreAuthorize("@ss.hasPermi('module:entity:action')")` annotations
- Services: `I{Entity}Service` interface + `{Entity}ServiceImpl`
- Mappers: `{Entity}Mapper` interface + `{Entity}Mapper.xml` in `src/main/resources/mapper/{module}/`
- Permission strings: `"manage:inventory:list"`, `"manage:inventory:add"`, `"manage:inventory:edit"`, `"manage:inventory:remove"`, `"manage:inventory:export"`

**Response wrappers:** `AjaxResult` for single objects, `TableDataInfo` for paginated lists (via `BaseController.getDataTable()`).

**Pagination:** PageHelper with `startPage()` called in controller before service query.

**MyBatis XML locations:** `classpath*:mapper/**/*Mapper.xml` — each module places XML files under its own `src/main/resources/mapper/{module}/` directory.

## Frontend Architecture Patterns

**State management:** Vuex with modules in `src/store/modules/` — user.js (auth/token), permission.js (RBAC menu generation), dict.js (dictionary cache), app.js (sidebar/theme), tagsView.js (open tabs).

**Permission system:** Backend returns permission array via API → stored in Vuex → `v-hasPermi` directive checks in templates, router guards in `src/permission.js`.

**API layer:** `src/api/{module}/{entity}.js` files wrapping Axios calls. Axios instance in `src/utils/request.js` handles JWT injection, error interception, and response unwrapping.

**Path alias:** `@` resolves to `src/` (configured in vue.config.js).

**Key component directories:**
- `src/components/customBpmn/` — BPMN process designer
- `src/components/vform/` — Visual form builder
- `src/components/flow/` — Workflow UI components
- `src/components/parser/`, `src/components/render/` — Dynamic form rendering

## SQL Schema Files

Located in `sql/` directory:
- `ry_20240629.sql` — Base RuoYi system tables
- `tony-flowable.sql` — Flowable workflow extension tables
- `quartz.sql` — Quartz scheduler tables
- `库存管理.sql`, `班组管理.sql`, `测试报告.sql` — Business domain tables

## Flowable-Specific Notes

- Flowable auto-creates/updates its own tables (`database-schema-update: true` in application.yml)
- Async executor is disabled (`async-executor-activate: false`)
- Custom process diagram rendering via `CustomProcessDiagramCanvas` and `CustomProcessDiagramGenerator`
- EL expressions evaluated with Aviator library (`com.ruoyi.flowable.common.expand.el`)
- Process constants defined in `ProcessConstants.java` — includes task candidate variables, process initiator keys
- Flowable's built-in Spring Security is excluded; the project uses RuoYi's own Spring Security config instead

---

## 流程管理功能总结

> 本章记录已实现的流程管理完整机制，供后续开发参考。

### 一、整体流程架构

```
发起流程（myProcess/send）
    ↓ 填写发起表单 → 存入流程变量
待办任务（task/todo）
    ↓ 审批（通过 / 返回上一节点 / 不通过）
  ┌─ 通过 → 下一节点（或流程结束）
  ├─ 返回上一节点 → 重新激活上一节点（在待办列表重现）
  └─ 不通过 → 终止整个流程实例（状态=失败）
已发流程（myProcess）— 发起人查看自己发起的所有流程
已办任务（task/finished）— 办理人查看自己处理过的任务
```

### 二、流程状态（procStatus）

| 值 | 含义 | 触发条件 |
|----|------|---------|
| `running` | 进行中 | `HistoricProcessInstance.endTime == null` |
| `finished` | 已完成 | 流程走到结束节点，`deleteReason` 为空 |
| `rejected` | 失败（不通过） | `deleteReason` 以 `REJECTED:` 开头 |
| `stopped` | 已取消 | `deleteReason` 以 `STOPPED:` 开头 |

状态由 `FlowTaskServiceImpl.myProcess()` 填入 `FlowTaskDto.procStatus`，前端 `myProcess/index.vue` 用 `<el-tag>` 色块展示。

### 三、节点表单机制

#### 3.1 表单绑定
- 每个流程节点（UserTask）在 BPMN 设计器中可绑定一个 `SysForm`，以表单 ID 作为 `formKey` 存储在 BPMN 定义里。
- `HistoricTaskInstance.getFormKey()` **始终为 null**，必须通过 `repositoryService.getBpmnModel(procDefId)` 读取 UserTask 节点定义来获取 `formKey`。

#### 3.2 表单数据命名空间隔离
同一张表单可被多个节点复用。为防止字段 ID 相同导致数据覆盖，每个节点提交时以 `{taskDefinitionKey}__formData` 为 key 存入流程变量：

```js
// 前端提交（todo/detail/index.vue → submitForm）
this.$set(this.taskForm.variables, this.taskDefinitionKey + '__formData', formData);
```

后端读取时（`flowTaskForm` 方法）将命名空间数据平铺回 `parameters`：
```java
String nsKey = ht.getTaskDefinitionKey() + "__formData";
Object nsData = parameters.get(nsKey);
if (nsData != null) {
    JSONObject.parseObject(...nsData...).forEach(parameters::put);
}
```

#### 3.3 `flowTaskForm` 接口行为（`GET /flowable/task/flowTaskForm?taskId=`）

| 场景 | 行为 |
|------|------|
| 运行时任务（待办） | 只返回当前节点绑定的表单 + 从命名空间取已保存数据（可编辑） |
| 已完成任务（已办/已发） | 从 BPMN 遍历所有历史节点，**合并所有节点的 widgetList**，从历史变量取各节点命名空间数据回填（全只读） |
| task 已不在运行时 | 降级查 `HistoricTaskInstance`；若流程实例 `endTime != null` 则进入聚合分支 |

**关键实现细节：**
- 流程变量使用 `historyService.createHistoricVariableInstanceQuery().processInstanceId(procInsId)` 重新加载，保证拿到所有节点完成后的最终变量快照（不依赖单个 historicTaskInstance 的变量快照）。
- 历史任务查询**不加 `.finished()` 过滤**，因为 `deleteProcessInstance` 强制终止的流程中，最后一个活跃任务的 `endTime` 可能为 null，但其表单数据仍需纳入聚合。

### 四、审批操作

#### 4.1 通过（approved）
调用 `complete(taskForm)` → `POST /flowable/task/complete`，Flowable 正常推进流程到下一节点。

#### 4.2 返回上一节点（returned）
**后端实现（`taskReturn` + `findReturnTaskList`）：**

- `findReturnTaskList`：查 `historyService.historicTaskInstanceQuery().finished().orderByEndTime().desc()`，去重后按倒序返回已经执行过的节点列表（排除当前节点自身）。**不使用** `FlowableUtils.findRoad`（该方法存在共享可变列表 bug，且 BPMN 图结构遍历在网关场景下不可靠）。
- `taskReturn`：
  1. 若前端传了 `targetKey` 则直接使用；否则自动取历史中最近完成的、不同于当前节点的节点 key。
  2. 取当前所有活跃任务 key 列表（`taskService.createTaskQuery().processInstanceId()`）。
  3. 调用 `runtimeService.createChangeActivityStateBuilder().moveActivityIdsToSingleActivityId(currentKeys, targetKey)`。
  4. 退回后注入班组候选人（`IFlowTeamService.injectTeamCandidates`），确保退回节点的审批人能在待办列表看到任务。

**前端行为（`todo/detail/index.vue`）：**
- 只有 1 个可退回节点 → 自动选中，显示节点名 tag。
- 多个可退回节点 → 显示下拉框供选择。
- 无可退回节点 → 提示"当前已是第一个节点"，清除审批结果。

#### 4.3 不通过（rejected）
调用 `rejectTask(taskForm)` → `POST /flowable/task/reject` → 后端直接调用：
```java
runtimeService.deleteProcessInstance(procInsId, "REJECTED:" + comment);
```
整个流程实例立即终止，`procStatus` 变为 `rejected`（失败）。不论当前在哪个节点，效果相同。

#### 4.4 取消申请（stopped）
由发起人在"已发流程"列表点击"取消申请"触发：
```java
runtimeService.deleteProcessInstance(instanceId, "STOPPED:" + comment);
```
`procStatus` 变为 `stopped`（已取消）。

### 五、核心文件索引

| 文件 | 职责 |
|------|------|
| `ruoyi-flowable/.../FlowTaskServiceImpl.java` | 全部流程任务逻辑：complete、return、reject、stop、flowTaskForm、myProcess、findReturnTaskList |
| `ruoyi-flowable/.../FlowTaskDto.java` | 任务 DTO，含 `procStatus` 字段 |
| `ruoyi-flowable/.../FlowableUtils.java` | BPMN 图遍历工具（`findRoad`、`iteratorFindParentUserTasks` 等）—— **退回逻辑已不再使用，改用历史记录方式** |
| `ruoyi-ui/src/views/flowable/task/todo/detail/index.vue` | 待办任务详情、审批弹框（通过/返回/不通过）、表单命名空间提交 |
| `ruoyi-ui/src/views/flowable/task/finished/detail/index.vue` | 已办任务详情、聚合所有节点表单只读展示 |
| `ruoyi-ui/src/views/flowable/task/myProcess/index.vue` | 已发流程列表、流程状态色块展示 |
| `ruoyi-ui/src/views/flowable/task/myProcess/detail/index.vue` | 已发流程详情（与已办详情逻辑相同，调用 `flowTaskForm` 聚合展示） |
| `ruoyi-ui/src/views/flowable/task/myProcess/send/index.vue` | 发起新流程、填写发起表单 |
| `ruoyi-ui/src/api/flowable/todo.js` | 前端 API：`flowTaskForm`、`complete`、`returnTask`、`returnList`、`rejectTask` |

### 六、已知约束与注意事项

1. **`HistoricTaskInstance.getFormKey()` 永远为 null** — 只能从 `repositoryService.getBpmnModel()` 的 UserTask 定义读取。
2. **`FlowableUtils.findRoad` 不可用于退回** — 共享可变列表导致多路径场景结果错误，已废弃用于退回逻辑。
3. **`deleteProcessInstance` 终止的流程** — 最后一个活跃任务没有 `endTime`，`flowTaskForm` 的聚合分支判断必须检查**流程实例**的 `endTime`，而非任务本身。
4. **流程变量快照不完整** — `HistoricTaskInstance.getProcessVariables()` 只含该任务完成时的快照，后续节点设置的变量不在其中。聚合展示时必须改用 `historyService.createHistoricVariableInstanceQuery().processInstanceId()`。
5. **退回后候选人丢失** — `changeActivityState` 后重新激活的节点是全新任务实例，原有 candidate/assignee 不会自动继承，必须在退回后重新调用 `injectTeamCandidates`。
