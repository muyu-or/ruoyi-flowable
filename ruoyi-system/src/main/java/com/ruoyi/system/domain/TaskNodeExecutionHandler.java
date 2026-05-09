package com.ruoyi.system.domain;

/**
 * 节点执行处理人员关联对象 task_node_execution_handler
 *
 * @author claude
 * @date 2026-04-09
 */
public class TaskNodeExecutionHandler
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 关联的节点执行记录ID */
    private Long nodeExecutionId;

    /** 处理人员用户ID */
    private Long userId;

    /** 处理人员姓名 */
    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNodeExecutionId() {
        return nodeExecutionId;
    }

    public void setNodeExecutionId(Long nodeExecutionId) {
        this.nodeExecutionId = nodeExecutionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "TaskNodeExecutionHandler{" +
                "id=" + id +
                ", nodeExecutionId=" + nodeExecutionId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
