package com.ruoyi.manage.domain;

/**
 * 产线班组用户关联对象 production_team_user
 * 
 * @author xgh
 * @date 2026-01-23
 */
public class ProductionTeamUser
{
    /** 班组ID */
    private Long teamId;

    /** 用户ID */
    private Long userId;

    public Long getTeamId()
    {
        return teamId;
    }

    public void setTeamId(Long teamId)
    {
        this.teamId = teamId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
}
