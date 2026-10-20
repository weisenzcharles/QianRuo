package com.ruoyi.system.controller.rental;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.mapper.rental.RentalMerchantMenuMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantRoleTemplateMapper;
import com.ruoyi.system.model.rental.MerchantRoleTemplateCommand;
import com.ruoyi.system.model.rental.MerchantRoleTemplateQuery;
import com.ruoyi.system.model.rental.MerchantRoleTemplateVO;
import com.ruoyi.system.service.ISysMenuService;

/**
 * 平台全局商户角色模板管理。
 *
 * <p>本控制器只维护所有商户可使用的角色定义和菜单权限模板，不接收也不解析
 * tenantId、memberId 或所属商户参数。商户端实际用户和角色授权仍由 merchant-service
 * 按当前登录商户上下文独立处理。</p>
 */
@RestController
@RequestMapping("/rental/merchantRole")
public class RentalMerchantRoleController extends BaseController
{
    @Autowired
    private RentalMerchantRoleTemplateMapper roleTemplateMapper;

    @Autowired
    private RentalMerchantMenuMapper merchantMenuMapper;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 分页查询平台维护的全局商户角色模板。
     *
     * @param query 角色名称、权限字符和状态筛选条件
     * @return 全局角色模板分页数据
     */
    @RequiresPermissions("admin:merchantRole:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantRoleTemplateQuery query)
    {
        assertAdminClient();
        startPage();
        return getDataTable(roleTemplateMapper.selectList(query));
    }

    /**
     * 查询全局商户角色模板详情。
     *
     * @param roleTemplateId 角色模板唯一编号
     * @return 角色模板详情
     */
    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/{roleTemplateId}")
    public AjaxResult getInfo(@PathVariable Long roleTemplateId)
    {
        assertAdminClient();
        MerchantRoleTemplateVO role = roleTemplateMapper.selectById(roleTemplateId);
        if (role == null)
        {
            return error("商户角色不存在");
        }
        role.setMenuIds(roleTemplateMapper.selectMenuIds(roleTemplateId).toArray(new Long[0]));
        return success(role);
    }

    /**
     * 新增平台全局商户角色模板。
     *
     * @param command 角色基础信息和菜单授权
     * @return 操作结果
     */
    @RequiresPermissions("admin:merchantRole:add")
    @Log(title = "商户角色", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@Validated @RequestBody MerchantRoleTemplateCommand command)
    {
        assertAdminClient();
        applyDefaults(command);
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        roleTemplateMapper.insert(command, SecurityUtils.getUserId());
        insertMenus(command.getRoleTemplateId(), menuIds);
        return success();
    }

    /**
     * 修改平台全局商户角色模板及其菜单授权。
     *
     * @param command 角色基础信息和菜单授权
     * @return 操作结果
     */
    @RequiresPermissions("admin:merchantRole:edit")
    @Log(title = "商户角色", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@Validated @RequestBody MerchantRoleTemplateCommand command)
    {
        assertAdminClient();
        if (command.getRoleTemplateId() == null || roleTemplateMapper.selectById(command.getRoleTemplateId()) == null)
        {
            return error("商户角色不存在");
        }
        applyDefaults(command);
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        roleTemplateMapper.update(command, SecurityUtils.getUserId());
        roleTemplateMapper.deleteMenus(command.getRoleTemplateId());
        insertMenus(command.getRoleTemplateId(), menuIds);
        return success();
    }

    /**
     * 软删除平台全局商户角色模板。
     *
     * <p>角色模板不直接绑定商户用户，因此删除时不再查询或校验所属商户的用户关系；
     * 已经存在的商户运行角色不会被级联删除。</p>
     *
     * @param roleTemplateIds 待删除角色模板唯一编号集合
     * @return 操作结果
     */
    @RequiresPermissions("admin:merchantRole:remove")
    @Log(title = "商户角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleTemplateIds}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult remove(@PathVariable Long[] roleTemplateIds)
    {
        assertAdminClient();
        Long[] ids = unique(roleTemplateIds);
        if (ids.length == 0)
        {
            return error("请选择要删除的商户角色");
        }
        for (Long id : ids)
        {
            if (roleTemplateMapper.selectById(id) == null)
            {
                return error("商户角色不存在");
            }
            roleTemplateMapper.deleteMenus(id);
        }
        return toAjax(roleTemplateMapper.softDelete(ids, SecurityUtils.getUserId()));
    }

    /**
     * 查询全局商户菜单树，供角色模板配置权限使用。
     *
     * @return 商户端菜单树
     */
    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/menuTreeselect")
    public AjaxResult menuTreeselect()
    {
        assertAdminClient();
        List<SysMenu> menus = merchantMenuMapper.selectMenuList(new SysMenu());
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 查询角色模板菜单树和已经选中的菜单。
     *
     * @param roleTemplateId 角色模板唯一编号
     * @return 菜单树及已选菜单唯一编号
     */
    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/roleMenuTreeselect/{roleTemplateId}")
    public AjaxResult roleMenuTreeselect(@PathVariable Long roleTemplateId)
    {
        assertAdminClient();
        if (roleTemplateMapper.selectById(roleTemplateId) == null)
        {
            return error("商户角色不存在");
        }
        List<SysMenu> menus = merchantMenuMapper.selectMenuList(new SysMenu());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", roleTemplateMapper.selectMenuIds(roleTemplateId));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    /**
     * 设置新增角色的默认配置，确保数据库字段不会收到空值。
     *
     * @param command 角色模板写入命令
     */
    private void applyDefaults(MerchantRoleTemplateCommand command)
    {
        if (command.getMenuCheckStrictly() == null)
        {
            command.setMenuCheckStrictly(RentalCode.YesNo.YES.getCode());
        }
        if (command.getStatus() == null)
        {
            command.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        }
    }

    /**
     * 校验角色名称和权限字符在全局命名空间内唯一。
     *
     * @param command 角色模板写入命令
     */
    private void validateUnique(MerchantRoleTemplateCommand command)
    {
        MerchantRoleTemplateVO sameName = roleTemplateMapper.selectByName(command.getRoleName());
        if (sameName != null && !sameName.getRoleTemplateId().equals(command.getRoleTemplateId()))
        {
            throw new ServiceException("角色名称已存在");
        }
        MerchantRoleTemplateVO sameKey = roleTemplateMapper.selectByKey(command.getRoleKey());
        if (sameKey != null && !sameKey.getRoleTemplateId().equals(command.getRoleTemplateId()))
        {
            throw new ServiceException("角色权限字符已存在");
        }
    }

    /**
     * 校验前端提交的菜单唯一编号确实属于当前启用的商户菜单模板。
     *
     * @param menuIds 前端提交的菜单唯一编号
     * @return 去重后的菜单唯一编号
     */
    private Long[] validateMenus(Long[] menuIds)
    {
        Long[] ids = unique(menuIds);
        for (Long menuId : ids)
        {
            if (merchantMenuMapper.selectMenuById(0L, 0L, menuId) == null)
            {
                throw new ServiceException("菜单权限包含不存在的商户菜单");
            }
        }
        return ids;
    }

    /**
     * 写入角色模板菜单授权；没有选中菜单时保持空授权集合。
     *
     * @param roleTemplateId 角色模板唯一编号
     * @param menuIds 菜单唯一编号集合
     */
    private void insertMenus(Long roleTemplateId, Long[] menuIds)
    {
        if (menuIds.length > 0)
        {
            roleTemplateMapper.insertMenus(roleTemplateId, menuIds);
        }
    }

    /**
     * 对唯一编号数组去重并移除空值，保证批量软删除和授权写入稳定。
     *
     * @param ids 原始唯一编号数组
     * @return 去重后的唯一编号数组
     */
    private Long[] unique(Long[] ids)
    {
        if (ids == null)
        {
            return new Long[0];
        }
        Set<Long> values = new LinkedHashSet<Long>(Arrays.asList(ids));
        values.remove(null);
        return values.toArray(new Long[0]);
    }

    /**
     * 平台角色模板接口只能由 platform 客户端访问。
     */
    private void assertAdminClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台商户角色管理接口");
        }
    }
}
