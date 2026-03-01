package com.ruoyi.flowable.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.common.enums.FlowComment;
import com.ruoyi.flowable.domain.dto.FlowStartDto;
import com.ruoyi.system.domain.FlowProcDefDto;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.service.IFlowDefinitionService;
import com.ruoyi.flowable.service.ISysDeployFormService;
import com.ruoyi.system.domain.SysForm;
import com.ruoyi.system.domain.TaskExecutionRecord;
import com.ruoyi.system.mapper.FlowDeployMapper;
import com.ruoyi.system.service.ITaskExecutionRecordService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 流程定义
 *
 * @author Tony
 * @date 2021-04-03
 */
@Service
@Slf4j
public class FlowDefinitionServiceImpl extends FlowServiceFactory implements IFlowDefinitionService {

    @Resource
    private ISysDeployFormService sysDeployFormService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysDeptService sysDeptService;

    @Resource
    private FlowDeployMapper flowDeployMapper;

    @Resource
    private ITaskExecutionRecordService taskExecutionRecordService;

    private static final String BPMN_FILE_SUFFIX = ".bpmn";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean exist(String processDefinitionKey) {
        ProcessDefinitionQuery processDefinitionQuery
                = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        long count = processDefinitionQuery.count();
        return count > 0 ? true : false;
    }


    /**
     * 流程定义列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 流程定义分页列表数据
     */
    @Override
    public Page<FlowProcDefDto> list(String name, Integer pageNum, Integer pageSize) {
        Page<FlowProcDefDto> page = new Page<>();
//        // 流程定义列表数据查询
//        final ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
//        if (StringUtils.isNotEmpty(name)) {
//            processDefinitionQuery.processDefinitionNameLike(name);
//        }
////        processDefinitionQuery.orderByProcessDefinitionKey().asc();
//        page.setTotal(processDefinitionQuery.count());
//        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(pageSize * (pageNum - 1), pageSize);
//
//        List<FlowProcDefDto> dataList = new ArrayList<>();
//        for (ProcessDefinition processDefinition : processDefinitionList) {
//            String deploymentId = processDefinition.getDeploymentId();
//            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
//            FlowProcDefDto reProcDef = new FlowProcDefDto();
//            BeanUtils.copyProperties(processDefinition, reProcDef);
//            SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(deploymentId);
//            if (Objects.nonNull(sysForm)) {
//                reProcDef.setFormName(sysForm.getFormName());
//                reProcDef.setFormId(sysForm.getFormId());
//            }
//            // 流程定义时间
//            reProcDef.setDeploymentTime(deployment.getDeploymentTime());
//            dataList.add(reProcDef);
//        }
        PageHelper.startPage(pageNum, pageSize);
        final List<FlowProcDefDto> dataList = flowDeployMapper.selectDeployList(name);
        // 加载挂表单
        for (FlowProcDefDto procDef : dataList) {
            SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(procDef.getDeploymentId());
            if (Objects.nonNull(sysForm)) {
                procDef.setFormName(sysForm.getFormName());
                procDef.setFormId(sysForm.getFormId());
            }
        }
        page.setTotal(new PageInfo(dataList).getTotal());
        page.setRecords(dataList);
        return page;
    }


    /**
     * 导入流程文件
     *
     * 当每个key的流程第一次部署时，指定版本为1。对其后所有使用相同key的流程定义，
     * 部署时版本会在该key当前已部署的最高版本号基础上加1。key参数用于区分流程定义
     * @param name
     * @param category
     * @param in
     */
    @Override
    public void importFile(String name, String category, InputStream in) {
        Deployment deploy = repositoryService.createDeployment().addInputStream(name + BPMN_FILE_SUFFIX, in).name(name).category(category).deploy();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        repositoryService.setProcessDefinitionCategory(definition.getId(), category);

        // ✅ 新增: 流程定义保存后，自动更新所有运行中的该流程实例的任务候选人
        try {
            log.info("========== 开始更新流程定义后的所有任务候选人 ==========");
            log.info("流程定义: {}, 部署ID: {}", definition.getKey(), deploy.getId());

            // 获取该流程定义的所有运行中的流程实例
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(definition.getKey())
                    .list();

            log.info("找到{}个运行中的流程实例", processInstances.size());

            if (processInstances.isEmpty()) {
                log.info("无运行中的流程实例，无需更新任务候选人");
                log.info("========== 流程定义保存完成 ==========");
                return;
            }

            // ✅ 为每个运行中的流程实例更新任务候选人
            int successCount = 0;
            int failCount = 0;

            for (ProcessInstance processInstance : processInstances) {
                try {
                    log.info("正在更新流程实例: {}", processInstance.getId());
                    // 调用 updateFlowInstanceTasksCandidates 方法更新所有任务候选人
                    com.ruoyi.common.utils.spring.SpringUtils.getBean(
                            com.ruoyi.flowable.service.IFlowTaskService.class)
                            .updateFlowInstanceTasksCandidates(processInstance.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("更新流程实例{}的任务候选人失败", processInstance.getId(), e);
                    failCount++;
                }
            }

            log.info("✅ 流程定义保存完成，已更新{}个流程实例的任务候选人，成功{}个，失败{}个",
                    processInstances.size(), successCount, failCount);
            log.info("========== 流程定义保存完成 ==========");

        } catch (Exception e) {
            log.error("❌ 更新流程定义后的任务候选人时出错", e);
            // 不抛出异常，避免影响流程定义保存
        }
    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public AjaxResult readXml(String deployId) throws IOException {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName());
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        return AjaxResult.success("", result);
    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public InputStream readImage(String deployId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        //获得图片流
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        //输出为图片
        return diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                Collections.emptyList(),
                Collections.emptyList(),
                "宋体",
                "宋体",
                "宋体",
                null,
                1.0,
                false);

    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程模板ID
     * @param variables 流程变量
     * @return
     */
    @Override
    public AjaxResult startProcessInstanceById(String procDefId, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId)
                    .latestVersion().singleResult();
            if (Objects.nonNull(processDefinition) && processDefinition.isSuspended()) {
                return AjaxResult.error("流程已被挂起,请先激活流程");
            }
            // 设置流程发起人Id到流程中
            SysUser sysUser = SecurityUtils.getLoginUser().getUser();
            identityService.setAuthenticatedUserId(sysUser.getUserId().toString());

            // 如果variables为null，初始化为HashMap
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put(ProcessConstants.PROCESS_INITIATOR, sysUser.getUserId());

            // 流程发起时 跳过发起人节点
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDefId, variables);

            String procInstId = processInstance.getProcessInstanceId();

            // 为流程中的所有任务节点注入班组候选人
            // 这确保即使任务监听器没有运行，候选人也能被正确设置
            try {
                List<Task> allTasks = taskService.createTaskQuery()
                        .processInstanceId(procInstId)
                        .list();

                log.info("流程启动后，共有 {} 个待办任务", allTasks.size());

                for (Task task : allTasks) {
                    String nodeKey = task.getTaskDefinitionKey();
                    String nodeName = task.getName();

                    log.info("为节点 {} ({}) 注入班组候选人", nodeKey, nodeName);

                    // 直接调用服务方法注入班组
                    try {
                        com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                com.ruoyi.flowable.service.IFlowTeamService.class)
                                .injectTeamCandidates(task.getId(), procInstId, nodeKey, nodeName);
                    } catch (Exception e) {
                        log.warn("为节点 {} 注入班组时出错", nodeKey, e);
                    }
                }
            } catch (Exception e) {
                log.warn("注入班组候选人时出错", e);
            }

            return AjaxResult.success("流程启动成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("流程启动错误");
        }
    }

    /**
     * 根据FlowStartDto启动流程实例（支持班组任务分配）
     *
     * @param flowStartDto 流程启动参数，包含nodeTeamMap节点班组映射
     * @return AjaxResult
     */
    @Override
    public AjaxResult startProcessInstanceByDto(FlowStartDto flowStartDto) {
        try {
            String procDefId = flowStartDto.getProcDefId();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(procDefId).latestVersion().singleResult();
            if (Objects.nonNull(processDefinition) && processDefinition.isSuspended()) {
                return AjaxResult.error("流程已被挂起,请先激活流程");
            }

            // 设置流程发起人Id到流程中
            SysUser sysUser = SecurityUtils.getLoginUser().getUser();
            identityService.setAuthenticatedUserId(sysUser.getUserId().toString());

            // 初始化流程变量
            Map<String, Object> variables = new HashMap<>();
            if (flowStartDto.getVariables() != null) {
                variables.putAll(flowStartDto.getVariables());
            }
            variables.put(ProcessConstants.PROCESS_INITIATOR, sysUser.getUserId());

            // 将nodeTeamMap和mainTeamId序列化为JSON存入流程变量
            if (flowStartDto.getNodeTeamMap() != null && !flowStartDto.getNodeTeamMap().isEmpty()) {
                String nodeTeamMapJson = JSON.toJSONString(flowStartDto.getNodeTeamMap());
                variables.put(ProcessConstants.NODE_TEAM_MAP_KEY, nodeTeamMapJson);
                log.info("已设置NODE_TEAM_MAP: {}", nodeTeamMapJson);
            }
            if (flowStartDto.getMainTeamId() != null && flowStartDto.getMainTeamId() > 0) {
                variables.put(ProcessConstants.MAIN_TEAM_ID_KEY, flowStartDto.getMainTeamId());
                log.info("已设置MAIN_TEAM_ID: {}", flowStartDto.getMainTeamId());
            }

            // 启动流程实例
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                    procDefId,
                    flowStartDto.getBusinessKey(),
                    variables);

            String procInstId = processInstance.getProcessInstanceId();

            // 写入task_execution_record主记录
            TaskExecutionRecord record = new TaskExecutionRecord();
            record.setProcInstId(procInstId);
            record.setProcDefKey(processDefinition.getKey());
            record.setProcDefVersion(processDefinition.getVersion());
            record.setInitiatorId(sysUser.getUserId());
            record.setMainTeamId(flowStartDto.getMainTeamId());
            record.setStatus("running");
            taskExecutionRecordService.insertTaskExecutionRecord(record);
            log.info("已创建流程执行记录，procInstId={}", procInstId);

            // ⚠️ 注意：不要自动完成任务节点，让 Listener 能正常触发
            // 原代码会自动完成第一个 UserTask，导致 Listener 无法正常工作
            // 任务应该由用户在待办列表中手动认领和完成

            // 由于 Listener 可能无法正常触发，这里直接调用 IFlowTeamService
            // 为流程中的所有 UserTask 节点注入班组候选人
            try {
                List<Task> allTasks = taskService.createTaskQuery()
                        .processInstanceId(procInstId)
                        .list();

                log.info("流程启动后，共有 {} 个待办任务", allTasks.size());

                for (Task task : allTasks) {
                    String nodeKey = task.getTaskDefinitionKey();
                    String nodeName = task.getName();

                    log.info("直接为节点 {} ({}) 注入班组候选人", nodeKey, nodeName);

                    // 直接调用服务方法注入班组
                    try {
                        com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                com.ruoyi.flowable.service.IFlowTeamService.class)
                                .injectTeamCandidates(task.getId(), procInstId, nodeKey, nodeName);
                    } catch (Exception e) {
                        log.warn("为节点 {} 注入班组时出错", nodeKey, e);
                    }
                }
            } catch (Exception e) {
                log.warn("注入班组候选人时出错", e);
            }

            return AjaxResult.success("流程启动成功");
        } catch (Exception e) {
            log.error("根据DTO启动流程实例失败", e);
            e.printStackTrace();
            return AjaxResult.error("流程启动错误");
        }
    }


    /**
     * 激活或挂起流程定义
     *
     * @param state    状态
     * @param deployId 流程部署ID
     */
    @Override
    public void updateState(Integer state, String deployId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        // 激活
        if (state == 1) {
            repositoryService.activateProcessDefinitionById(procDef.getId(), true, null);
        }
        // 挂起
        if (state == 2) {
            repositoryService.suspendProcessDefinitionById(procDef.getId(), true, null);
        }
    }


    /**
     * 删除流程定义
     *
     * @param deployId 流程部署ID act_ge_bytearray 表中 deployment_id值
     */
    @Override
    public void delete(String deployId) {
        // true 允许级联删除 ,不设置会导致数据库外键关联异常
        repositoryService.deleteDeployment(deployId, true);
    }


}
