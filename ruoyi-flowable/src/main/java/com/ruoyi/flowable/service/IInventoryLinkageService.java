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
