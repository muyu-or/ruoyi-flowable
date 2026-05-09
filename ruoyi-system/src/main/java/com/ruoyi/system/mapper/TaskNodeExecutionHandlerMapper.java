package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.TaskNodeExecutionHandler;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点执行处理人员关联Mapper接口
 *
 * @author claude
 * @date 2026-04-09
 */
@Mapper
public interface TaskNodeExecutionHandlerMapper
{
    /**
     * 批量插入处理人员
     *
     * @param list 处理人员列表
     * @return 插入条数
     */
    int batchInsert(@Param("list") List<TaskNodeExecutionHandler> list);

    /**
     * 根据节点执行记录ID查询处理人员
     *
     * @param nodeExecutionId 节点执行记录ID
     * @return 处理人员列表
     */
    List<TaskNodeExecutionHandler> selectByNodeExecutionId(@Param("nodeExecutionId") Long nodeExecutionId);
}
