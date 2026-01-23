package com.ruoyi.manage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.service.IProductionTeamService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 产线班组Controller
 * 
 * @author xgh
 * @date 2026-01-23
 */
@RestController
@RequestMapping("/manage/team")
public class ProductionTeamController extends BaseController
{
    @Autowired
    private IProductionTeamService productionTeamService;

    /**
     * 查询用户列表（含角色名称）
     */
    @PreAuthorize("@ss.hasPermi('manage:team:list')")
    @GetMapping("/userList")
    public TableDataInfo userList(com.ruoyi.common.core.domain.entity.SysUser user)
    {
        startPage();
        List<com.ruoyi.common.core.domain.entity.SysUser> list = productionTeamService.selectUserListForTeam(user);
        return getDataTable(list);
    }

    /**
     * 查询产线班组列表
     */
    @PreAuthorize("@ss.hasPermi('manage:team:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProductionTeam productionTeam)
    {
        startPage();
        List<ProductionTeam> list = productionTeamService.selectProductionTeamList(productionTeam);
        return getDataTable(list);
    }

    /**
     * 导出产线班组列表
     */
    @PreAuthorize("@ss.hasPermi('manage:team:export')")
    @Log(title = "产线班组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductionTeam productionTeam)
    {
        List<ProductionTeam> list = productionTeamService.selectProductionTeamList(productionTeam);
        ExcelUtil<ProductionTeam> util = new ExcelUtil<ProductionTeam>(ProductionTeam.class);
        util.exportExcel(response, list, "产线班组数据");
    }

    /**
     * 获取产线班组详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:team:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(productionTeamService.selectProductionTeamById(id));
    }

    /**
     * 新增产线班组
     */
    @PreAuthorize("@ss.hasPermi('manage:team:add')")
    @Log(title = "产线班组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductionTeam productionTeam)
    {
        productionTeam.setCreateBy(getUsername());
        return toAjax(productionTeamService.insertProductionTeam(productionTeam));
    }

    /**
     * 修改产线班组
     */
    @PreAuthorize("@ss.hasPermi('manage:team:edit')")
    @Log(title = "产线班组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductionTeam productionTeam)
    {
        productionTeam.setUpdateBy(getUsername());
        return toAjax(productionTeamService.updateProductionTeam(productionTeam));
    }

    /**
     * 删除产线班组
     */
    @PreAuthorize("@ss.hasPermi('manage:team:remove')")
    @Log(title = "产线班组", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(productionTeamService.deleteProductionTeamByIds(ids));
    }
}
