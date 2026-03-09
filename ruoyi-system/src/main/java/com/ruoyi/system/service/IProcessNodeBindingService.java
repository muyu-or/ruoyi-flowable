package com.ruoyi.system.service;

import com.ruoyi.system.domain.ProcessNodeBinding;
import java.util.List;

/**
 * 流程节点与班组绑定配置服务接口
 *
 * @author xgh
 * @date 2026-02-28
 */
public interface IProcessNodeBindingService
{
    /**
     * 查询流程节点绑定配置
     *
     * @param id 主键
     * @return 流程节点绑定配置
     */
    public ProcessNodeBinding selectProcessNodeBindingById(Long id);

    /**
     * 查询流程节点绑定配置列表
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 流程节点绑定配置集合
     */
    public List<ProcessNodeBinding> selectProcessNodeBindingList(ProcessNodeBinding processNodeBinding);

    /**
     * 新增流程节点绑定配置
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 结果
     */
    public int insertProcessNodeBinding(ProcessNodeBinding processNodeBinding);

    /**
     * 修改流程节点绑定配置
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 结果
     */
    public int updateProcessNodeBinding(ProcessNodeBinding processNodeBinding);

    /**
     * 删除流程节点绑定配置
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteProcessNodeBindingById(Long id);

    /**
     * 批量删除流程节点绑定配置
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    public int deleteProcessNodeBindingByIds(Long[] ids);

    /**
     * 根据流程定义和节点key查询绑定配置
     *
     * @param procDefKey 流程定义key
     * @param procDefVersion 流程版本
     * @param nodeKey 节点key
     * @return 流程节点绑定配置
     */
    public ProcessNodeBinding selectByNodeKey(String procDefKey, Integer procDefVersion, String nodeKey);
}
