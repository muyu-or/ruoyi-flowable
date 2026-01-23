package com.ruoyi.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysUser;
import java.util.List;

/**
 * 产线班组对象 production_team
 * 
 * @author xgh
 * @date 2026-01-23
 */
public class ProductionTeam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 班组名称 */
    @Excel(name = "班组名称")
    private String teamName;

    /** 班组长姓名 */
    @Excel(name = "班组长姓名")
    private String leaderName;

    /** 班组长ID  */
    private Long leaderId;

    /** 状态  */
    @Excel(name = "状态 ")
    private String teamStatus;

    /** 班组成员ID列表 */
    private Long[] userIds;

    /** 班组成员对象列表 */
    private List<SysUser> userList;

    public void setUserIds(Long[] userIds)
    {
        this.userIds = userIds;
    }

    public Long[] getUserIds()
    {
        return userIds;
    }

    public void setUserList(List<SysUser> userList)
    {
        this.userList = userList;
    }

    public List<SysUser> getUserList()
    {
        return userList;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTeamName(String teamName) 
    {
        this.teamName = teamName;
    }

    public String getTeamName() 
    {
        return teamName;
    }

    public void setLeaderName(String leaderName) 
    {
        this.leaderName = leaderName;
    }

    public String getLeaderName() 
    {
        return leaderName;
    }

    public void setLeaderId(Long leaderId) 
    {
        this.leaderId = leaderId;
    }

    public Long getLeaderId() 
    {
        return leaderId;
    }

    public void setTeamStatus(String teamStatus) 
    {
        this.teamStatus = teamStatus;
    }

    public String getTeamStatus() 
    {
        return teamStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("teamName", getTeamName())
            .append("leaderName", getLeaderName())
            .append("leaderId", getLeaderId())
            .append("teamStatus", getTeamStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
