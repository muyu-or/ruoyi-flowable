package com.ruoyi.manage.domain.vo;


import com.ruoyi.common.annotation.Excel;
import com.ruoyi.manage.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryVO extends Inventory {
    /** 入库类型（1.毛坯入库、2.调拨入库、3.返修入库） */
    @Excel(name = "入库类型", readConverterExp = "1=.毛坯入库、2.调拨入库、3.返修入库")
    private String inboundType;
}
