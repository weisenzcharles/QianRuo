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
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.model.rental.RentalMemberAuditCommand;
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
    private static final Long DEFAULT_TENANT_ID = 1L;

    @Autowired
    private IRentalMemberService memberService;

    /**
     * 校验当前请求来自平台终端，防止商户令牌越权调用平台商户管理接口。
     *
     * @throws ServiceException 当前终端不是平台端时抛出
     */
    private void assertAdminClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商家端不允许访问平台商户管理接口");
        }
    }

    /**
     * 查询租赁商户列表
     */
    @RequiresPermissions("platform:member:list")
    @GetMapping("/list")
    public TableDataInfo list(RentalMember member)
    {
        assertAdminClient();
        startPage();
        List<RentalMember> list = memberService.selectRentalMemberList(member);
        return getDataTable(list);
    }

    /**
     * 导出租赁商户列表
     */
    @Log(title = "租赁商户", businessType = BusinessType.EXPORT)
    @RequiresPermissions("platform:member:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, RentalMember member)
    {
        assertAdminClient();
        List<RentalMember> list = memberService.selectRentalMemberList(member);
        ExcelUtil<RentalMember> util = new ExcelUtil<RentalMember>(RentalMember.class);
        util.exportExcel(response, list, "租赁商户数据");
    }

    /**
     * 获取租赁商户详细信息
     */
    @RequiresPermissions("platform:member:query")
    @GetMapping(value = "/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId)
    {
        assertAdminClient();
        return success(memberService.selectRentalMemberById(memberId));
    }

    /**
     * 新增租赁商户
     */
    @RequiresPermissions("platform:member:add")
    @Log(title = "租赁商户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody RentalMember member)
    {
        assertAdminClient();
        setDefaultTenantId(member);
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
    @RequiresPermissions("platform:member:edit")
    @Log(title = "租赁商户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody RentalMember member)
    {
        assertAdminClient();
        setDefaultTenantId(member);
        if (!memberService.checkMemberCodeUnique(member))
        {
            return error("修改商户'" + member.getMemberCode() + "'失败，商户编码已存在");
        }
        member.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(memberService.updateRentalMember(member));
    }

    /**
     * 审核商户入驻申请。审核通过后由服务层同步开通商户管理员账号，
     * 审核驳回时必须给出可供商户修改资料的具体原因。
     *
     * @param memberId 服务端商户主键
     * @param command 审核结果和审核意见
     * @return 审核操作结果
     */
    @RequiresPermissions("platform:member:audit")
    @Log(title = "商户入驻审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{memberId}/audit")
    public AjaxResult audit(@PathVariable Long memberId,
            @Validated @RequestBody RentalMemberAuditCommand command)
    {
        assertAdminClient();
        return toAjax(memberService.auditRentalMember(memberId, command, SecurityUtils.getUsername()));
    }

    /**
     * 删除租赁商户
     */
    @RequiresPermissions("platform:member:remove")
    @Log(title = "租赁商户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberIds}")
    public AjaxResult remove(@PathVariable Long[] memberIds)
    {
        assertAdminClient();
        return toAjax(memberService.deleteRentalMemberByIds(memberIds));
    }

    /**
     * 为平台手工创建的商户补充系统默认租户，客户端不能决定商户归属租户。
     *
     * @param member 待保存的商户资料
     */
    private void setDefaultTenantId(RentalMember member)
    {
        if (member.getTenantId() == null)
        {
            member.setTenantId(DEFAULT_TENANT_ID);
        }
    }
}
