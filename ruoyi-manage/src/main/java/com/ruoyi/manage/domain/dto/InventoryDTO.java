package com.ruoyi.manage.domain.dto;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 物料ID/唯一标识码 */
    @Excel(name = "id")
    private Long id;

    @Excel(name = "物料ID/唯一标识码")
    private String materialId;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料大类 */
    @Excel(name = "物料大类")
    private String materialCategory;

    /** 物料子类 */
    @Excel(name = "物料子类")
    private String materialSubcategory;
    /** 入库数量 */
    @Excel(name = "入库数量")
    private Long quantity;


    /** 库区 */
    @Excel(name = "库区")
    private String warehouseArea;
    /** 库存状态 */
    @Excel(name = "库存状态")
    private String status;

    /** 首次入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "首次入库时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date firstInboundTime;

    /** 上次入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "上次入库时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastInboundTime;

    /** 入库类型（1.毛坯入库、2.调拨入库、3.返修入库） */
    @Excel(name = "入库类型", readConverterExp = "1=.毛坯入库、2.调拨入库、3.返修入库")
    private String inboundType;
    /** 出库类型（1.生产领料出库、2.销售出库、3.调拨出库、4.报废出库） */
    @Excel(name = "出库类型", readConverterExp = "1=.生产领料出库、2.销售出库、3.调拨出库、4.报废出库")
    private String outboundType;

    /**
     * 操作人员
     */
    @Excel(name = "操作人员")
    private String operator;

}
