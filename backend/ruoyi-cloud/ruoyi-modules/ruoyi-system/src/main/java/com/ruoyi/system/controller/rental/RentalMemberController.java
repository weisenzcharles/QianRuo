package com.ruoyi.system.controller.rental;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.service.rental.IRentalMemberService;

/**
 * 租赁商户管理
 *
 * @author qianruo
 */
@RestController
@RequestMapping("/rental/member")
public class RentalMemberController extends BaseController
{
    @Autowired
    private IRentalMemberService memberService;

    /**
     * 查询租赁商户列表
     */
    @RequiresPermissions("rental:member:list")
    @GetMapping("/list")
    public TableDataInfo list(RentalMember member)
    {
        startPage();
        List<RentalMember> list = memberService.selectRentalMemberList(member);
        return getDataTable(list);
    }

    /**
     * 导出租赁商户列表
     */
    @Log(title = "租赁商户", businessType = BusinessType.EXPORT)
    @RequiresPermissions("rental:member:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, RentalMember member)
    {
        List<RentalMember> list = memberService.selectRentalMemberList(member);
        ExcelUtil<RentalMember> util = new ExcelUtil<RentalMember>(RentalMember.class);
        util.exportExcel(response, list, "租赁商户数据");
    }

    /**
     * 获取租赁商户详细信息
     */
    @RequiresPermissions("rental:member:query")
    @GetMapping(value = "/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId)
    {
        return success(memberService.selectRentalMemberById(memberId));
    }

    /**
     * 新增租赁商户
     */
    @RequiresPermissions("rental:member:add")
    @Log(title = "租赁商户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody RentalMember member)
    {
        if (!memberService.checkMemberCodeUnique(member))
        {
            return error("新增商户'" + member.getMemberCode() + "'失败，商户编码已存在");
        }
        member.setCreateBy(SecurityUtils.getUsername());
        return toAjax(memberService.insertRentalMember(member));
    }

    /**
     * 修改租赁商户
     */
    @RequiresPermissions("rental:member:edit")
    @Log(title = "租赁商户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody RentalMember member)
    {
        if (!memberService.checkMemberCodeUnique(member))
        {
            return error("修改商户'" + member.getMemberCode() + "'失败，商户编码已存在");
        }
        member.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(memberService.updateRentalMember(member));
    }

    /**
     * 删除租赁商户
     */
    @RequiresPermissions("rental:member:remove")
    @Log(title = "租赁商户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberIds}")
    public AjaxResult remove(@PathVariable Long[] memberIds)
    {
        return toAjax(memberService.deleteRentalMemberByIds(memberIds));
    }
}
