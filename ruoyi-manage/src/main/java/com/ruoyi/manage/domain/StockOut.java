package com.ruoyi.manage.domain;


import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 出库记录对象 stock_out
 *
 * @author xgh
 * @date 2025-11-04
 */
public class StockOut extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 出库记录ID
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
     * 出库类型（1.生产领料出库、2.销售出库、3.调拨出库、4.报废出库）
     */
    @Excel(name = "出库类型", readConverterExp = "1=.生产领料出库、2.销售出库、3.调拨出库、4.报废出库")
    private String outboundType;

    /**
     * 出库数量
     */
    private Long quantity;

    /**
     * 库区
     */
    @Excel(name = "库区")
    private String warehouseArea;

    /**
     * 出库时间
     */
    private Date outboundTime;

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

    public void setOutboundType(String outboundType) {
        this.outboundType = outboundType;
    }

    public String getOutboundType() {
        return outboundType;
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

    public void setOutboundTime(Date outboundTime) {
        this.outboundTime = outboundTime;
    }

    public Date getOutboundTime() {
        return outboundTime;
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
                .append("outboundType", getOutboundType())
                .append("quantity", getQuantity())
                .append("warehouseArea", getWarehouseArea())
                .append("outboundTime", getOutboundTime())
                .append("operator", getOperator())
                .append("createTime", getCreateTime())
                .toString();
    }
}
