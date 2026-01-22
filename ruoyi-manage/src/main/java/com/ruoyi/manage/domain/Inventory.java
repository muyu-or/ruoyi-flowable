package com.ruoyi.manage.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 库存信息对象 inventory
 *
 * @author xgh
 * @date 2025-11-04
 */
public class Inventory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 物料ID/唯一标识码
     */
    @Excel(name = "物料ID/唯一标识码")
    private String materialId;

    /**
     * 物料名称
     */
    @Excel(name = "物料名称")
    private String materialName;

    /**
     * 物料大类
     */
    @Excel(name = "物料大类")
    private String materialCategory;

    /**
     * 物料子类
     */
    @Excel(name = "物料子类")
    private String materialSubcategory;

    /**
     * 当前库存数量
     */
    private Long currentQuantity;

    /**
     * 库区
     */
    @Excel(name = "库区")
    private String warehouseArea;

    /**
     * 库存状态
     */
    @Excel(name = "库存状态")
    private String status;

    /**
     * 操作人员
     */
    @Excel(name = "操作人员")
    private String operator;

    /**
     * 首次入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "首次入库时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date firstInboundTime;

    /**
     * 上次入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "上次入库时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastInboundTime;

    /**
     * 上次出库时间
     */
    private Date lastOutboundTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialCategory(String materialCategory) {
        this.materialCategory = materialCategory;
    }

    public String getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialSubcategory(String materialSubcategory) {
        this.materialSubcategory = materialSubcategory;
    }

    public String getMaterialSubcategory() {
        return materialSubcategory;
    }

    public void setCurrentQuantity(Long currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Long getCurrentQuantity() {
        return currentQuantity;
    }

    public void setWarehouseArea(String warehouseArea) {
        this.warehouseArea = warehouseArea;
    }

    public String getWarehouseArea() {
        return warehouseArea;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public void setFirstInboundTime(Date firstInboundTime) {
        this.firstInboundTime = firstInboundTime;
    }

    public Date getFirstInboundTime() {
        return firstInboundTime;
    }

    public void setLastInboundTime(Date lastInboundTime) {
        this.lastInboundTime = lastInboundTime;
    }

    public Date getLastInboundTime() {
        return lastInboundTime;
    }

    public void setLastOutboundTime(Date lastOutboundTime) {
        this.lastOutboundTime = lastOutboundTime;
    }

    public Date getLastOutboundTime() {
        return lastOutboundTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("materialId", getMaterialId())
                .append("materialName", getMaterialName())
                .append("materialCategory", getMaterialCategory())
                .append("materialSubcategory", getMaterialSubcategory())
                .append("currentQuantity", getCurrentQuantity())
                .append("warehouseArea", getWarehouseArea())
                .append("status", getStatus())
                .append("operator", getOperator())
                .append("firstInboundTime", getFirstInboundTime())
                .append("lastInboundTime", getLastInboundTime())
                .append("lastOutboundTime", getLastOutboundTime())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
