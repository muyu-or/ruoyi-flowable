# Inventory Linkage on Task Complete — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 当生产流程的三个库存节点（原料检测入库、出库、产品入库）审批通过时，`complete()` 方法自动联动调用库存服务，完成入库/出库操作。

**Architecture:** 新建 `IInventoryLinkageService` 接口及其实现，封装"节点 key → 库存操作"的映射逻辑。`FlowTaskServiceImpl.complete()` 在 `taskService.complete(...)` 之后调用它；库存操作失败会触发整体事务回滚，工作流不推进。

**Tech Stack:** Spring Boot 2.5.15, Java 8, Flowable 6.8.0, fastjson2, MyBatis (ruoyi-manage 模块的 IInventoryService 已被 ruoyi-flowable 依赖)

---

## 节点 Key 对照表（实现时必须使用）

| 节点名称       | taskDefinitionKey    |
|--------------|----------------------|
| 原料检测入库   | `Activity_1uqk506`   |
| 出库           | `Activity_01xy3yd`   |
| 产品入库       | `Activity_1lnd3md`   |

---

## 表单数据在流程变量中的存储方式

前端提交时，节点表单数据以 `{taskDefinitionKey}__formData` 为 key 存入 `variables`。
值可能是 `Map<String, Object>`（fastjson2 反序列化后），也可能是 JSON 字符串。

`complete()` 方法在调用 `taskService.complete(...)` 之前，已将前端传来的 `variables` 合并到 `allVariables`：

```java
if (taskVo.getVariables() != null && !taskVo.getVariables().isEmpty()) {
    allVariables.putAll(taskVo.getVariables());
}
```

因此 `allVariables` 里含有 `Activity_1uqk506__formData` 等 key。

---

## 字段映射说明

### 原料检测入库（Activity_1uqk506）→ `insertInventory(InventoryDTO)`

前端表单字段（来自 StockInForm.vue）：

| 表单字段              | InventoryDTO 字段      | 备注                          |
|---------------------|----------------------|-------------------------------|
| `materialName`      | `materialName`       |                               |
| `materialCategory`  | `materialCategory`   | 字典值（如 "MTL"）              |
| `materialSubcategory` | `materialSubcategory` | 字典值（如 "PT"）             |
| `warehouseArea`     | `warehouseArea`      | 字典值                        |
| `inboundType`       | `inboundType`        | 字典值                        |
| `quantity`          | `quantity`           | Long（需要转换）               |
| `materialId`        | 留空                 | 后端自动生成，不从前端读取      |

### 出库（Activity_01xy3yd）→ `processStockOut(InventoryDTO)`

前端表单字段（来自 StockOutForm.vue）：

| 表单字段        | 处理方式                                                   |
|--------------|-------------------------------------------------------------|
| `materialId` | 字符串编码（如 MTL-PT20230815-0001A），用于查库存主键 id      |
| `outQuantity` | 出库数量，映射到 `inventoryDTO.quantity`（Long）            |
| `outboundType` | 出库类型，映射到 `inventoryDTO.outboundType`               |

后端流程：
1. 用 `materialId` 调 `inventoryMapper.selectInventoryBymaterialId(materialId)` 查出 Inventory
2. 将 Inventory 的 `id` 设置到 `inventoryDTO.setId(inventory.getId())`
3. 调 `inventoryService.processStockOut(inventoryDTO)`

### 产品入库（Activity_1lnd3md）→ `insertInventory(InventoryDTO)`

前端表单字段（来自 FinalStockInForm.vue）：

| 表单字段           | InventoryDTO 字段      | 备注                                  |
|------------------|----------------------|---------------------------------------|
| `productName`    | `materialName`       | 产品名称映射为物料名称                  |
| `inQuantity`     | `quantity`           | Long（需要转换）                       |
| `storageLocation`| `warehouseArea`      | 存放位置映射为库区                     |
| —                | `materialCategory`   | 固定值 `"product"`（成品大类）          |
| —                | `materialSubcategory`| 固定值 `"finished"`（成品子类）         |
| —                | `inboundType`        | 固定值 `"1"`（生产入库，对应字典值）    |
| `materialId`     | 留空                 | 后端自动生成                           |

---

## 辅助方法：从 allVariables 提取节点表单数据

```java
/**
 * 从流程变量中提取指定节点的表单数据
 * 键格式：{taskDefinitionKey}__formData
 * 值可能是 Map 或 JSON 字符串
 */
private Map<String, Object> extractFormData(String taskDefinitionKey, Map<String, Object> allVariables) {
    String key = taskDefinitionKey + "__formData";
    Object raw = allVariables.get(key);
    if (raw == null) return Collections.emptyMap();
    if (raw instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) raw;
        return m;
    }
    if (raw instanceof String) {
        return JSONObject.parseObject((String) raw, new TypeReference<Map<String, Object>>() {});
    }
    return Collections.emptyMap();
}
```

---

## 辅助方法：安全转换 quantity（Number 或 String → Long）

```java
private Long toLong(Object val) {
    if (val == null) return 0L;
    if (val instanceof Number) return ((Number) val).longValue();
    try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return 0L; }
}
```

---

## Task 1：新建 IInventoryLinkageService 接口

**Files:**
- Create: `ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/IInventoryLinkageService.java`

**Step 1：创建接口文件**

```java
package com.ruoyi.flowable.service;

import java.util.Map;

/**
 * 库存联动服务接口
 * 在流程节点完成（审批通过）时，根据节点 key 自动触发对应的库存操作。
 *
 * 涉及节点：
 *   Activity_1uqk506 — 原料检测入库 → insertInventory
 *   Activity_01xy3yd — 出库         → processStockOut
 *   Activity_1lnd3md — 产品入库     → insertInventory
 */
public interface IInventoryLinkageService {

    /**
     * 根据任务节点 key 与流程变量，执行对应的库存操作。
     * 非库存节点调用时直接返回，不抛异常。
     * 库存操作失败时抛出异常，由调用方（complete 方法的 @Transactional）回滚。
     *
     * @param taskDefinitionKey 当前完成的任务节点 key
     * @param allVariables      合并后的全量流程变量（含前端提交的 formData）
     */
    void handleNodeCompletion(String taskDefinitionKey, Map<String, Object> allVariables);
}
```

**Step 2：编译验证**

```bash
mvn compile -pl ruoyi-flowable -am -q
```

期望：编译成功，无错误。

**Step 3：Commit**

```bash
git add ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/IInventoryLinkageService.java
git commit -m "feat: add IInventoryLinkageService interface for inventory linkage on task complete"
```

---

## Task 2：实现 InventoryLinkageServiceImpl

**Files:**
- Create: `ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/impl/InventoryLinkageServiceImpl.java`

**依赖说明：**
- `IInventoryService`：在 `ruoyi-manage` 模块，ruoyi-flowable 的 pom.xml 已依赖该模块
- `InventoryMapper`：同样在 ruoyi-manage 模块，通过 Spring 容器注入
- `InventoryDTO`：`com.ruoyi.manage.domain.dto.InventoryDTO`
- fastjson2：`com.alibaba.fastjson2.JSONObject` 和 `TypeReference`（已在 FlowTaskServiceImpl 使用）

**Step 1：创建实现文件**

```java
package com.ruoyi.flowable.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.flowable.service.IInventoryLinkageService;
import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.mapper.InventoryMapper;
import com.ruoyi.manage.service.IInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * 库存联动服务实现
 * 在任务节点完成时，根据节点 key 自动触发对应的库存入库/出库操作。
 */
@Slf4j
@Service
public class InventoryLinkageServiceImpl implements IInventoryLinkageService {

    /** 原料检测入库节点 key */
    private static final String NODE_STOCK_IN     = "Activity_1uqk506";
    /** 出库节点 key */
    private static final String NODE_STOCK_OUT    = "Activity_01xy3yd";
    /** 产品入库节点 key */
    private static final String NODE_FINAL_IN     = "Activity_1lnd3md";

    /** 产品入库使用的固定分类值（字典 material_category / material_subcategory） */
    private static final String PRODUCT_CATEGORY    = "product";
    private static final String PRODUCT_SUBCATEGORY = "finished";
    /** 产品入库类型（字典 inbound_type，"1" = 生产入库） */
    private static final String PRODUCT_INBOUND_TYPE = "1";

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public void handleNodeCompletion(String taskDefinitionKey, Map<String, Object> allVariables) {
        switch (taskDefinitionKey) {
            case NODE_STOCK_IN:
                handleStockIn(taskDefinitionKey, allVariables);
                break;
            case NODE_STOCK_OUT:
                handleStockOut(taskDefinitionKey, allVariables);
                break;
            case NODE_FINAL_IN:
                handleFinalStockIn(taskDefinitionKey, allVariables);
                break;
            default:
                // 非库存节点，不做任何操作
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 原料检测入库
    // ─────────────────────────────────────────────────────────────

    private void handleStockIn(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("原料检测入库节点：表单数据为空，无法执行入库操作");
        }

        InventoryDTO dto = new InventoryDTO();
        dto.setMaterialName(toString(form.get("materialName")));
        dto.setMaterialCategory(toString(form.get("materialCategory")));
        dto.setMaterialSubcategory(toString(form.get("materialSubcategory")));
        dto.setWarehouseArea(toString(form.get("warehouseArea")));
        dto.setInboundType(toString(form.get("inboundType")));
        dto.setQuantity(toLong(form.get("quantity")));
        // materialId 留空：insertInventory 内部调 generateMaterialCode() 自动生成

        log.info("原料检测入库节点完成，执行入库操作：materialName={}, category={}, quantity={}",
                dto.getMaterialName(), dto.getMaterialCategory(), dto.getQuantity());

        int result = inventoryService.insertInventory(dto);
        if (result <= 0) {
            throw new ServiceException("原料检测入库失败，请联系管理员");
        }
        log.info("原料检测入库成功：{}", dto.getMaterialName());
    }

    // ─────────────────────────────────────────────────────────────
    // 出库
    // ─────────────────────────────────────────────────────────────

    private void handleStockOut(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("出库节点：表单数据为空，无法执行出库操作");
        }

        String materialId = toString(form.get("materialId"));
        if (materialId == null || materialId.isEmpty()) {
            throw new ServiceException("出库节点：物料ID为空，无法执行出库操作");
        }

        // 通过 materialId 字符串编码查找库存记录，获取数字主键 id
        Inventory inventory = inventoryMapper.selectInventoryBymaterialId(materialId);
        if (inventory == null) {
            throw new ServiceException("出库节点：未找到物料 [" + materialId + "] 的库存记录");
        }

        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());           // processStockOut 需要数字主键
        dto.setQuantity(toLong(form.get("outQuantity")));
        dto.setOutboundType(toString(form.get("outboundType")));

        log.info("出库节点完成，执行出库操作：materialId={}, quantity={}, outboundType={}",
                materialId, dto.getQuantity(), dto.getOutboundType());

        inventoryService.processStockOut(dto);
        log.info("出库成功：materialId={}", materialId);
    }

    // ─────────────────────────────────────────────────────────────
    // 产品入库
    // ─────────────────────────────────────────────────────────────

    private void handleFinalStockIn(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("产品入库节点：表单数据为空，无法执行入库操作");
        }

        InventoryDTO dto = new InventoryDTO();
        dto.setMaterialName(toString(form.get("productName")));      // productName → materialName
        dto.setQuantity(toLong(form.get("inQuantity")));             // inQuantity → quantity
        dto.setWarehouseArea(toString(form.get("storageLocation"))); // storageLocation → warehouseArea
        // 固定分类值（产品）
        dto.setMaterialCategory(PRODUCT_CATEGORY);
        dto.setMaterialSubcategory(PRODUCT_SUBCATEGORY);
        dto.setInboundType(PRODUCT_INBOUND_TYPE);
        // materialId 留空：insertInventory 内部自动生成

        log.info("产品入库节点完成，执行入库操作：productName={}, quantity={}",
                dto.getMaterialName(), dto.getQuantity());

        int result = inventoryService.insertInventory(dto);
        if (result <= 0) {
            throw new ServiceException("产品入库失败，请联系管理员");
        }
        log.info("产品入库成功：{}", dto.getMaterialName());
    }

    // ─────────────────────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────────────────────

    /**
     * 从 allVariables 中提取节点表单数据
     * key 格式：{taskDefinitionKey}__formData
     * 值可能是 Map<String,Object> 或 JSON 字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractFormData(String taskDefinitionKey, Map<String, Object> allVariables) {
        String key = taskDefinitionKey + "__formData";
        Object raw = allVariables.get(key);
        if (raw == null) {
            log.warn("流程变量中未找到节点 {} 的表单数据（key={}）", taskDefinitionKey, key);
            return Collections.emptyMap();
        }
        if (raw instanceof Map) {
            return (Map<String, Object>) raw;
        }
        if (raw instanceof String) {
            try {
                return JSONObject.parseObject((String) raw, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.error("解析节点 {} 的表单数据失败：{}", taskDefinitionKey, raw, e);
                return Collections.emptyMap();
            }
        }
        log.warn("节点 {} 的表单数据类型不支持：{}", taskDefinitionKey, raw.getClass().getName());
        return Collections.emptyMap();
    }

    /** 安全转 Long（支持 Number、String） */
    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return 0L; }
    }

    /** 安全转 String */
    private String toString(Object val) {
        return val == null ? "" : val.toString().trim();
    }
}
```

**Step 2：编译验证**

```bash
mvn compile -pl ruoyi-flowable -am -q
```

期望：编译成功，无错误。

**Step 3：Commit**

```bash
git add ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/impl/InventoryLinkageServiceImpl.java
git commit -m "feat: implement InventoryLinkageServiceImpl - stock in/out linkage on task complete"
```

---

## Task 3：在 FlowTaskServiceImpl.complete() 中调用联动服务

**Files:**
- Modify: `ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/impl/FlowTaskServiceImpl.java`

**改动说明：**

在 `FlowTaskServiceImpl` 中注入 `IInventoryLinkageService`，并在非委派任务完成（`taskService.complete(...)` 调用）之后、处理候选人注入之前，调用 `inventoryLinkageService.handleNodeCompletion(...)`。

**Step 1：在字段注入区添加依赖（约第 76-82 行之后）**

在现有的 `@Autowired` 字段下方添加：

```java
@Autowired
private com.ruoyi.flowable.service.IInventoryLinkageService inventoryLinkageService;
```

**Step 2：在 `complete()` 方法中添加调用（约第 126-130 行之间）**

找到：
```java
// 完成任务，传递合并后的所有变量到下一节点
taskService.complete(taskVo.getTaskId(), allVariables);

// 任务完成后，为后续新创建的任务注入班组候选人
```

在 `taskService.complete(...)` 调用后、候选人注入的 `try` 块之前，插入：

```java
// 完成任务，传递合并后的所有变量到下一节点
taskService.complete(taskVo.getTaskId(), allVariables);

// 库存联动：根据节点 key 自动执行入库/出库操作（失败时事务回滚）
inventoryLinkageService.handleNodeCompletion(task.getTaskDefinitionKey(), allVariables);

// 任务完成后，为后续新创建的任务注入班组候选人
```

**Step 3：编译验证**

```bash
mvn compile -pl ruoyi-flowable -am -q
```

期望：编译成功，无错误。

**Step 4：Commit**

```bash
git add ruoyi-flowable/src/main/java/com/ruoyi/flowable/service/impl/FlowTaskServiceImpl.java
git commit -m "feat: invoke inventoryLinkageService in complete() for automatic inventory sync"
```

---

## 验收标准

1. **原料检测入库节点**：审批通过后，`inventory` 表新增一条记录，`materialId` 自动生成（格式如 `MTL-PT20230815-0001A`），`stock_in` 表也新增一条记录
2. **出库节点**：审批通过后，`inventory` 表对应物料的 `current_quantity` 减少，`stock_out` 表新增一条记录；若库存不足，`complete()` 返回错误，工作流不推进
3. **产品入库节点**：审批通过后，`inventory` 表新增产品记录，`materialCategory='product'`，`materialSubcategory='finished'`
4. **其他节点**（预处理/真空/烘烤/测试）：完成后无任何库存变动，行为与原来一致
5. **两次编译均通过**：`mvn compile -pl ruoyi-flowable -am -q`

---

## 注意事项

- `processStockOut()` 内部校验 `inventory == null`，但顺序有问题（先减再判 null，见 InventoryServiceImpl 第 391 行），不在本计划修复范围内，联动时已通过 materialId 预先查询，若为 null 会在联动层提前抛出异常
- `InventoryMapper.selectInventoryBymaterialId` 的方法名拼写不规范（小写 m），实现时需用 grep 确认实际方法名后再调用
- 产品入库的 `materialCategory`/`materialSubcategory` 固定值（`"product"` / `"finished"`）需要与实际字典数据库中的值匹配；若字典值不同需调整常量
