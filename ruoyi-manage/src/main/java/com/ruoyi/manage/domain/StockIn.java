package com.ruoyi.manage.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 入库记录对象 stock_in
 *
 * @author xgh
 * @date 2025-11-04
 */
public class StockIn extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 入库记录ID
     */
    private Long id;

    /**
     * 物料ID
     */
    @Excel(name = "物料ID")
    private String materialId;


    /**
     * 物料名称
     */
    @Excel(name = "物料名称")
    private String materialName;

    /**
     * 入库类型（1.毛坯入库、2.调拨入库、3.返修入库）
     */
    @Excel(name = "入库类型", readConverterExp = "1=.毛坯入库、2.调拨入库、3.返修入库")
    private String inboundType;

    /**
     * 入库数量
     */
    private Long quantity;

    /**
     * 库区
     */
    @Excel(name = "库区")
    private String warehouseArea;

    /**
     * 入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入库时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date inboundTime;

    /**
     * 操作人员
     */
    private String operator;

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

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

    public void setInboundType(String inboundType) {
        this.inboundType = inboundType;
    }

    public String getInboundType() {
        return inboundType;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setWarehouseArea(String warehouseArea) {
        this.warehouseArea = warehouseArea;
    }

    public String getWarehouseArea() {
        return warehouseArea;
    }

    public void setInboundTime(Date inboundTime) {
        this.inboundTime = inboundTime;
    }

    public Date getInboundTime() {
        return inboundTime;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("materialId", getMaterialId())
                .append("materialName", getMaterialName())
                .append("inboundType", getInboundType())
                .append("quantity", getQuantity())
                .append("warehouseArea", getWarehouseArea())
                .append("inboundTime", getInboundTime())
                .append("operator", getOperator())
                .append("createTime", getCreateTime())
                .toString();
    }
}
