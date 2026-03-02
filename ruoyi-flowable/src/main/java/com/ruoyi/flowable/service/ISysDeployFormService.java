package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.system.domain.SysDeployForm;
import com.ruoyi.system.domain.SysForm;

/**
 * 流程实例关联表单Service接口
 * 
 * @author Tony
 * @date 2021-04-03
 */
public interface ISysDeployFormService 
{
    /**
     * 查询流程实例关联表单
     * 
     * @param id 流程实例关联表单ID
     * @return 流程实例关联表单
     */
    public SysDeployForm selectSysDeployFormById(Long id);

    /**
     * 查询流程实例关联表单列表
     * 
     * @param sysDeployForm 流程实例关联表单
     * @return 流程实例关联表单集合
     */
    public List<SysDeployForm> selectSysDeployFormList(SysDeployForm sysDeployForm);

    /**
     * 新增流程实例关联表单
     * 
     * @param sysDeployForm 流程实例关联表单
     * @return 结果
     */
    public int insertSysDeployForm(SysDeployForm sysDeployForm);

    /**
     * 修改流程实例关联表单
     * 
     * @param sysDeployForm 流程实例关联表单
     * @return 结果
     */
    public int updateSysDeployForm(SysDeployForm sysDeployForm);

    /**
     * 批量删除流程实例关联表单
     * 
     * @param ids 需要删除的流程实例关联表单ID
     * @return 结果
     */
    public int deleteSysDeployFormByIds(Long[] ids);

    /**
     * 删除流程实例关联表单信息
     * 
     * @param id 流程实例关联表单ID
     * @return 结果
     */
    public int deleteSysDeployFormById(Long id);

    /**
     * 查询流程挂着的表单
     * @param deployId
     * @return
     */
    SysForm selectSysDeployFormByDeployId(String deployId);

    /**
     * 保存流程-表单/组件关联（upsert：已存在则更新，不存在则插入）
     * formId 与 formComponent 互斥：有一个非空时，另一个置 null
     *
     * @param sysDeployForm 含 deployId，以及 formId 或 formComponent（二选一）
     * @return 影响行数
     */
    int saveDeployForm(SysDeployForm sysDeployForm);

    /**
     * 按 deployId 查完整关联配置（含 formId + formComponent）
     *
     * @param deployId 流程部署ID
     * @return SysDeployForm，不存在时返回 null
     */
    SysDeployForm selectDeployFormByDeployId(String deployId);
}
