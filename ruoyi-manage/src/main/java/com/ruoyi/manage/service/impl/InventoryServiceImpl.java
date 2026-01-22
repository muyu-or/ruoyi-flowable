package com.ruoyi.manage.service.impl;


import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.StockIn;
import com.ruoyi.manage.domain.StockOut;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.domain.vo.InventoryVO;
import com.ruoyi.manage.mapper.InventoryMapper;
import com.ruoyi.manage.mapper.StockInMapper;
import com.ruoyi.manage.mapper.StockOutMapper;
import com.ruoyi.manage.service.IInventoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 库存信息Service业务层处理
 *
 * @author xgh
 * @date 2025-11-04
 */
@Service
public class InventoryServiceImpl implements IInventoryService
{
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private StockInMapper stockInMapper;
    @Autowired
    private StockOutMapper stockOutMapper;

    /**
     * 查询库存信息
     *
     * @param id 库存信息主键
     * @return 库存信息
     */
    @Override
    public Inventory selectInventoryById(Long id)
    {
        return inventoryMapper.selectInventoryById(id);
    }

    /**
     * 查询库存信息列表
     *
     * @param inventory 库存信息
     * @return 库存信息
     */
    @Override
    public List<Inventory> selectInventoryList(Inventory inventory)
    {
        return inventoryMapper.selectInventoryList(inventory);
    }

    /**
     * 新增库存信息
     *
     * @param inventoryDTO 库存信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertInventory(InventoryDTO inventoryDTO)
    {
        String materialId = inventoryDTO.getMaterialId();
        // 查询库存（无需提前new Inventory()）
        Inventory existingInventory = inventoryMapper.selectInventoryBymaterialId(materialId);

        if (existingInventory != null) {
            // 库存已存在，更新数量和时间
            Long newQuantity = existingInventory.getCurrentQuantity() + inventoryDTO.getQuantity();
            existingInventory.setCurrentQuantity(newQuantity);
            existingInventory.setUpdateTime(DateUtils.getNowDate()); // 直接用框架工具类
            return inventoryMapper.updateInventory(existingInventory);
        } else {
            // 库存不存在，新增库存
            Inventory newInventory = new Inventory();
            BeanUtils.copyProperties(inventoryDTO, newInventory);
            // 1. 生成物料编码
            String materialCode = generateMaterialCode(inventoryDTO.getMaterialCategory(), inventoryDTO.getMaterialSubcategory());
            newInventory.setMaterialId(materialCode);
            newInventory.setCurrentQuantity(inventoryDTO.getQuantity());
            newInventory.setStatus("1"); // 可用状态
            Date now = DateUtils.getNowDate(); // 统一用框架的当前时间
            newInventory.setOperator(SecurityUtils.getUsername());
            newInventory.setFirstInboundTime(now);
            newInventory.setLastInboundTime(now);
            newInventory.setCreateTime(now);
            newInventory.setUpdateTime(now);
            int inventoryResult = inventoryMapper.insertInventory(newInventory);

            // 新增入库记录
            StockIn stockIn = new StockIn();
            BeanUtils.copyProperties(inventoryDTO, stockIn);
            //将新生成的 material_id 赋给入库记录
            stockIn.setMaterialId(materialCode);
            stockIn.setInboundTime(now);
            stockIn.setOperator(SecurityUtils.getUsername()); // SecurityUtils.getUsername()
            stockIn.setCreateTime(now);
            int stockInResult = stockInMapper.insertStockIn(stockIn);

            return inventoryResult + stockInResult;
        }
    }

    /**
     * 生成物料唯一编码
     * 规则: 大类-子类-日期-流水号-校验位
     * 示例: MTL-PT20230815-0001A
     */
    private String generateMaterialCode(String majorCategory, String subCategory) {
        // 1. 获取日期码 (YYYYMMDD)
        String dateCode = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 2. 获取当日流水号
        // 构造查询前缀，例如 "MTL-PT20230815-"
        String codePrefix = majorCategory + "-" + subCategory + dateCode + "-";
        String lastCode = inventoryMapper.findMaxCodeByPrefix(codePrefix);

        int nextSerial = 1;
        if (lastCode != null && !lastCode.isEmpty()) {
            // 从 "MTL-PT20230815-0001A" 中提取 "0001"
            String lastSerialStr = lastCode.substring(codePrefix.length(), codePrefix.length() + 4);
            nextSerial = Integer.parseInt(lastSerialStr) + 1;
        }
        String serialNumber = String.format("%04d", nextSerial); // 格式化为4位

        // 3. 计算校验位 (这是一个简单的示例算法，你可以替换为更复杂的)
        String dataToHash = majorCategory + subCategory + dateCode + serialNumber;
        int sum = 0;
        for (char c : dataToHash.toCharArray()) {
            sum += c;
        }
        char checksum = (char) ('A' + (sum % 26));

        // 4. 拼接完整编码
        return codePrefix + serialNumber + checksum;
    }

    /**
     * 修改库存信息
     *
     * @param inventory 库存信息
     * @return 结果
     */
    @Override
    public int updateInventory(Inventory inventory)
    {


        return inventoryMapper.updateInventory(inventory);
    }

  /*  @Override
    public int outInventory(InventoryDTO inventoryDTO)
    {
        Inventory inventory = new Inventory();
        BeanUtils.copyProperties(inventoryDTO, inventory);
        inventory.setUpdateTime(DateUtils.getNowDate());

        int inventoryResult = inventoryMapper.updateInventory(inventory);
        StockOut stockOut = new StockOut();
        BeanUtils.copyProperties(inventoryDTO, stockOut);
        stockOut.setOutboundTime(DateUtils.getNowDate());
        stockOut.setOperator(SecurityUtils.getUsername());
        stockOut.setCreateTime(DateUtils.getNowDate());
        stockOutMapper.insertStockOut(stockOut);
        int outResult = stockOutMapper.insertStockOut(stockOut);
        return inventoryResult+outResult;
    }*/

    /**
     * 批量删除库存信息
     *
     * @param ids 需要删除的库存信息主键
     * @return 结果
     */
    @Override
    public int deleteInventoryByIds(Long[] ids)
    {
        return inventoryMapper.deleteInventoryByIds(ids);
    }

    /**
     * 删除库存信息信息
     *
     * @param id 库存信息主键
     * @return 结果
     */
    @Override
    public int deleteInventoryById(Long id)
    {
        return inventoryMapper.deleteInventoryById(id);
    }


    @Override
    public List<InventoryVO> selectInventoryVOList(InventoryDTO inventoryDTO) {
        return inventoryMapper.selectInventoryVOList(inventoryDTO);
    }

    /*@Override
    public int processStockOut(InventoryDTO inventoryDTO) {
        // 1. 参数校验


        // 2. 查询并锁定库存记录
        Inventory inventory = inventoryMapper.selectInventoryBymaterialId(inventoryDTO.getMaterialId());

        // 3. 业务校验：检查库存是否存在且状态是否为“在库”
        if (inventory == null) {
            throw new ServiceException("库存记录不存在");
        }
        // 假设 "1" 是 "在库" 状态的字典值
        if (!"1".equals(inventory.getStatus())) {
            throw new ServiceException("只有'在库'状态的物料才能出库");
        }

        // 4. 更新库存状态和时间
        inventory.setStatus("2"); // "2" 代表 "已出库"
        inventory.setUpdateTime(DateUtils.getNowDate());
        // 如果有“最近出库时间”字段，也在这里更新
        inventory.setLastOutboundTime(DateUtils.getNowDate());
        int inventoryResult = inventoryMapper.updateInventory(inventory);

        // 5. 插入出库记录（只执行一次）
        StockOut stockOut = new StockOut();
        BeanUtils.copyProperties(inventoryDTO, stockOut); // 从前端传来的DTO复制信息
        stockOut.setOutboundTime(DateUtils.getNowDate());
        stockOut.setOperator(SecurityUtils.getUsername());
        stockOut.setCreateTime(DateUtils.getNowDate());
        int outResult = stockOutMapper.insertStockOut(stockOut);

        // 6. 检查两个操作是否都成功
        if (inventoryResult == 0 || outResult == 0) {
            // 如果任一操作失败，手动抛出异常以触发事务回滚
            throw new ServiceException("出库失败，数据已回滚");
        }

        return inventoryResult; // 或者直接返回
    }*/

    /*
    出库记录处理方法
     */
    @Override
    @Transactional // 1
    public AjaxResult processStockOut(InventoryDTO inventoryDTO) {
        // 1. 参数校验 (仅校验必填的出库类型)
        if (inventoryDTO.getOutboundType() == null) {
            throw new ServiceException("出库失败，必须选择出库类型");
        }

        // 2. 查询并锁定库存记录
        Inventory inventory = inventoryMapper.selectInventoryById(inventoryDTO.getId());

        Long stockOutQuantity = inventoryDTO.getQuantity();
        Long currentQuantity = inventory.getCurrentQuantity()-stockOutQuantity;
        // 3. 业务校验
        if (inventory == null) {
            throw new ServiceException("库存记录不存在");
        }
        if (!"1".equals(inventory.getStatus())) { // "1" 代表 "在库"
            throw new ServiceException("只有'在库'状态的物料才能出库");
        }
        if(currentQuantity<0){
            throw new ServiceException("出库失败，库存数量不足");
        }
        String message;
        if (currentQuantity == 0) {
            // 库存为0，更新状态为"已出库"
            inventory.setStatus("2"); // '2' 表示"已出库"
            message = "出库成功，状态已变更为已出库";
        } else {
            // 库存仍大于0，保持"在库"状态
            inventory.setStatus("1"); // '1' 表示"在库"
            message = "出库成功";
        }

        //设置当前库存数量
        inventory.setCurrentQuantity(currentQuantity);

        if (currentQuantity == 0) {
            inventory.setStatus("2"); // "2" 代表 "已出库"
        }
        //  更新相关时间
        inventory.setLastOutboundTime(DateUtils.getNowDate());
        inventory.setUpdateTime(DateUtils.getNowDate());
        inventory.setOperator(SecurityUtils.getUsername());
        inventoryMapper.updateInventory(inventory);

        // 5. 创建并填充出库记录
        StockOut stockOut = new StockOut();

        // 从查询出的 inventory 对象获取基础信息，这比完全信任前端数据更安全
        stockOut.setMaterialId(inventory.getMaterialId());
        stockOut.setMaterialName(inventory.getMaterialName());
        stockOut.setQuantity(stockOutQuantity);
        stockOut.setWarehouseArea(inventory.getWarehouseArea());

        // 关键步骤：设置出库类型；生产计划已不再强制
        stockOut.setOutboundType(inventoryDTO.getOutboundType());

        // 设置操作员和时间
        stockOut.setOperator(SecurityUtils.getUsername());
        stockOut.setOutboundTime(DateUtils.getNowDate());
        stockOut.setCreateTime(DateUtils.getNowDate());

        // 6. 插入出库记录
        stockOutMapper.insertStockOut(stockOut);

        // 7. 返回成功消息
        return AjaxResult.success(message);
    }
}
