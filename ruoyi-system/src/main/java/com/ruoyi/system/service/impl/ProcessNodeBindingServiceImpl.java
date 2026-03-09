package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.ProcessNodeBindingMapper;
import com.ruoyi.system.domain.ProcessNodeBinding;
import com.ruoyi.system.service.IProcessNodeBindingService;
import java.util.List;

/**
 * 流程节点与班组绑定配置服务实现
 *
 * @author xgh
 * @date 2026-02-28
 */
@Service
public class ProcessNodeBindingServiceImpl implements IProcessNodeBindingService
{
    @Autowired
    private ProcessNodeBindingMapper processNodeBindingMapper;

    /**
     * 查询流程节点绑定配置
     *
     * @param id 主键
     * @return 流程节点绑定配置
     */
    @Override
    public ProcessNodeBinding selectProcessNodeBindingById(Long id)
    {
        return processNodeBindingMapper.selectProcessNodeBindingById(id);
    }

    /**
     * 查询流程节点绑定配置列表
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 流程节点绑定配置集合
     */
    @Override
    public List<ProcessNodeBinding> selectProcessNodeBindingList(ProcessNodeBinding processNodeBinding)
    {
        return processNodeBindingMapper.selectProcessNodeBindingList(processNodeBinding);
    }

    /**
     * 新增流程节点绑定配置
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 结果
     */
    @Override
    public int insertProcessNodeBinding(ProcessNodeBinding processNodeBinding)
    {
        processNodeBinding.setCreateTime(DateUtils.getNowDate());
        return processNodeBindingMapper.insertProcessNodeBinding(processNodeBinding);
    }

    /**
     * 修改流程节点绑定配置
     *
     * @param processNodeBinding 流程节点绑定配置
     * @return 结果
     */
    @Override
    public int updateProcessNodeBinding(ProcessNodeBinding processNodeBinding)
    {
        processNodeBinding.setUpdateTime(DateUtils.getNowDate());
        return processNodeBindingMapper.updateProcessNodeBinding(processNodeBinding);
    }

    /**
     * 删除流程节点绑定配置
     *
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteProcessNodeBindingById(Long id)
    {
        return processNodeBindingMapper.deleteProcessNodeBindingById(id);
    }

    /**
     * 批量删除流程节点绑定配置
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    @Override
    public int deleteProcessNodeBindingByIds(Long[] ids)
    {
        return processNodeBindingMapper.deleteProcessNodeBindingByIds(ids);
    }

    /**
     * 根据流程定义和节点key查询绑定配置
     *
     * @param procDefKey 流程定义key
     * @param procDefVersion 流程版本
     * @param nodeKey 节点key
     * @return 流程节点绑定配置
     */
    @Override
    public ProcessNodeBinding selectByNodeKey(String procDefKey, Integer procDefVersion, String nodeKey)
    {
        return processNodeBindingMapper.selectByNodeKey(procDefKey, procDefVersion, nodeKey);
    }
}
