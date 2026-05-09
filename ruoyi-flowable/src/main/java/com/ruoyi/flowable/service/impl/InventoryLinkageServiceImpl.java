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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * 库存联动服务实现
 * 在任务节点完成时，根据节点 key 自动触发对应的库存入库/出库操作。
 */
@Slf4j
@Service
public class InventoryLinkageServiceImpl implements IInventoryLinkageService {

    private static final String NODE_STOCK_IN  = "Activity_1uqk506";
    private static final String NODE_STOCK_OUT = "Activity_01xy3yd";
    private static final String NODE_FINAL_IN  = "Activity_1lnd3md";

    private static final String PRODUCT_INBOUND_TYPE = "4";

    @Resource
    private IInventoryService inventoryService;

    @Resource
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
                break;
        }
    }

    private void handleStockIn(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("原料检测入库节点：表单数据为空，无法执行入库操作");
        }
        InventoryDTO dto = new InventoryDTO();
        dto.setMaterialName(toStr(form.get("materialName")));
        dto.setMaterialCategory(toStr(form.get("materialCategory")));
        dto.setMaterialSubcategory(toStr(form.get("materialSubcategory")));
        dto.setWarehouseArea(toStr(form.get("warehouseArea")));
        dto.setInboundType(toStr(form.get("inboundType")));
        dto.setQuantity(toLong(form.get("quantity")));
        dto.setUnitCost(toBigDecimal(form.get("unitCost")));
        log.info("原料检测入库：materialName={}, quantity={}, unitCost={}", dto.getMaterialName(), dto.getQuantity(), dto.getUnitCost());
        int result = inventoryService.insertInventory(dto);
        if (result <= 0) {
            throw new ServiceException("原料检测入库失败，请联系管理员");
        }
    }

    private void handleStockOut(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("出库节点：表单数据为空，无法执行出库操作");
        }
        String materialId = toStr(form.get("materialId"));
        if (materialId.isEmpty()) {
            throw new ServiceException("出库节点：物料ID为空，无法执行出库操作");
        }
        Inventory inventory = inventoryMapper.selectInventoryBymaterialId(materialId);
        if (inventory == null) {
            throw new ServiceException("出库节点：未找到物料 [" + materialId + "] 的库存记录");
        }
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setQuantity(toLong(form.get("outQuantity")));
        dto.setOutboundType(toStr(form.get("outboundType")));
        log.info("出库：materialId={}, quantity={}", materialId, dto.getQuantity());
        inventoryService.processStockOut(dto);
    }

    private void handleFinalStockIn(String nodeKey, Map<String, Object> allVariables) {
        Map<String, Object> form = extractFormData(nodeKey, allVariables);
        if (form.isEmpty()) {
            throw new ServiceException("产品入库节点：表单数据为空，无法执行入库操作");
        }
        InventoryDTO dto = new InventoryDTO();
        dto.setMaterialName(toStr(form.get("productName")));
        dto.setQuantity(toLong(form.get("inQuantity")));
        dto.setWarehouseArea(toStr(form.get("warehouseArea")));
        dto.setMaterialCategory(toStr(form.get("materialCategory")));
        dto.setMaterialSubcategory(toStr(form.get("materialSubcategory")));
        dto.setInboundType(PRODUCT_INBOUND_TYPE);
        dto.setUnitCost(toBigDecimal(form.get("unitCost")));
        log.info("产品入库：productName={}, quantity={}, unitCost={}", dto.getMaterialName(), dto.getQuantity(), dto.getUnitCost());
        int result = inventoryService.insertInventory(dto);
        if (result <= 0) {
            throw new ServiceException("产品入库失败，请联系管理员");
        }
    }

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
                log.error("解析节点 {} 的表单数据失败", taskDefinitionKey, e);
                return Collections.emptyMap();
            }
        }
        log.warn("节点 {} 的表单数据类型不支持：{}", taskDefinitionKey, raw.getClass().getName());
        return Collections.emptyMap();
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return 0L; }
    }

    private String toStr(Object val) {
        return val == null ? "" : val.toString().trim();
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        try { return new BigDecimal(val.toString()); } catch (NumberFormatException e) { return null; }
    }
}
