package com.ruoyi.merchant.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.domain.MerchantMenu;
import com.ruoyi.merchant.mapper.MerchantMenuMapper;
import com.ruoyi.merchant.model.MerchantRouteMeta;
import com.ruoyi.merchant.model.MerchantRouterVO;
import com.ruoyi.merchant.model.MerchantTreeSelect;

/** 商户菜单读取和动态路由装配服务。 */
@Service
public class MerchantMenuService
{
    private static final long ROOT_ID = 0L;

    /** 商户端内嵌外链的本地路由前缀，确保菜单始终在当前应用内容区打开。 */
    private static final String INNER_LINK_PATH_PREFIX = "/merchant/inner-link/";

    @Autowired
    private MerchantMenuMapper menuMapper;

    public List<MerchantMenu> catalog()
    {
        return menuMapper.selectCatalog();
    }

    public MerchantMenu get(Long menuId)
    {
        MerchantMenu menu = menuMapper.selectById(menuId);
        if (menu == null)
        {
            throw new ServiceException("商户菜单不存在");
        }
        return menu;
    }

    public List<Long> selectedMenuIds(Long roleId, boolean strict)
    {
        return menuMapper.selectIdsByRole(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                roleId, strict);
    }

    public List<MerchantTreeSelect> treeSelect(List<MerchantMenu> menus)
    {
        return buildTree(menus).stream().map(this::toTreeSelect).collect(Collectors.toList());
    }

    /**
     * 根据当前商户认证状态装配动态路由。已审核商户读取自身角色和直接授权，未审核商户读取全局页面模板，
     * 从而保留页面预览能力，同时由权限注解阻止新增、修改、删除、导出和授权操作。
     *
     * @return 当前商户可见的动态路由
     */
    public List<MerchantRouterVO> currentRouters()
    {
        List<MerchantMenu> menus = MerchantContextUtils.isMerchantApproved()
                ? menuMapper.selectByUser(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                        SecurityUtils.getUserId())
                : menuMapper.selectDefaultPreviewMenus();
        return buildRouters(buildTree(menus));
    }

    private List<MerchantMenu> buildTree(List<MerchantMenu> menus)
    {
        Set<Long> ids = menus.stream().map(MerchantMenu::getMenuId).collect(Collectors.toSet());
        List<MerchantMenu> roots = new ArrayList<MerchantMenu>();
        for (MerchantMenu menu : menus)
        {
            if (!ids.contains(menu.getParentId()))
            {
                fillChildren(menu, menus, new HashSet<Long>());
                roots.add(menu);
            }
        }
        return roots.isEmpty() ? menus : roots;
    }

    private void fillChildren(MerchantMenu parent, List<MerchantMenu> menus, Set<Long> ancestors)
    {
        if (!ancestors.add(parent.getMenuId()))
        {
            throw new ServiceException("商户菜单存在循环父子关系");
        }
        List<MerchantMenu> children = menus.stream()
                .filter(menu -> parent.getMenuId().equals(menu.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (MerchantMenu child : children)
        {
            fillChildren(child, menus, new HashSet<Long>(ancestors));
        }
    }

    private MerchantTreeSelect toTreeSelect(MerchantMenu menu)
    {
        MerchantTreeSelect node = new MerchantTreeSelect();
        node.setId(menu.getMenuId());
        node.setLabel(menu.getMenuName());
        node.setChildren(menu.getChildren().stream().map(this::toTreeSelect).collect(Collectors.toList()));
        return node;
    }

    private List<MerchantRouterVO> buildRouters(List<MerchantMenu> menus)
    {
        List<MerchantRouterVO> routers = new ArrayList<MerchantRouterVO>();
        for (MerchantMenu menu : menus)
        {
            MerchantRouterVO router = new MerchantRouterVO();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(isRootMenu(menu) ? StringUtils.EMPTY : routeName(menu));
            router.setPath(routerPath(menu));
            router.setComponent(component(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(meta(menu));
            if (!menu.getChildren().isEmpty() && UserConstants.TYPE_DIR.equals(menu.getMenuType()))
            {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildRouters(menu.getChildren()));
            }
            else if (isRootMenu(menu))
            {
                MerchantRouterVO child = new MerchantRouterVO();
                child.setPath(menu.getPath());
                child.setComponent(menu.getComponent());
                child.setName(routeName(menu));
                child.setQuery(menu.getQuery());
                child.setMeta(meta(menu));
                router.setChildren(List.of(child));
                router.setMeta(null);
            }
            routers.add(router);
        }
        return routers;
    }

    private MerchantRouteMeta meta(MerchantMenu menu)
    {
        MerchantRouteMeta meta = new MerchantRouteMeta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setNoCache("1".equals(menu.getIsCache()));
        if (isExternalMenu(menu))
        {
            meta.setLink(menu.getPath());
        }
        return meta;
    }

    /**
     * 判断菜单是否需要以内嵌页面方式打开。
     *
     * <p>商户端不允许菜单把浏览器带离当前后台。对于明确配置为 HTTP/HTTPS 地址的菜单，
     * 服务端保留原始地址到路由元数据中，同时把路由地址改成本地的 InnerLink 路径，
     * 由商户端内容区 iframe 承载。这样侧边栏、顶部菜单和菜单搜索都能复用同一套站内导航规则，
     * 不会因为菜单的 is_frame 配置差异再次退化为新窗口打开。</p>
     *
     * @param menu 待判断的商户菜单
     * @return true 表示该菜单应以内嵌页面方式打开
     */
    private boolean isExternalMenu(MerchantMenu menu)
    {
        return StringUtils.ishttp(menu.getPath());
    }

    /**
     * 获取菜单路由名称。
     *
     * <p>外链菜单使用菜单主键生成稳定且唯一的路由名称，避免直接使用 URL 中的协议、
     * 端口或特殊字符造成 Vue Router 路由名称不合法或重复。</p>
     *
     * @param menu 菜单信息
     * @return 前端路由名称
     */
    private String routeName(MerchantMenu menu)
    {
        if (isExternalMenu(menu))
        {
            return "MerchantInnerLink" + menu.getMenuId();
        }
        String name = StringUtils.isNotEmpty(menu.getRouteName()) ? menu.getRouteName() : menu.getPath();
        return StringUtils.capitalize(name);
    }

    /**
     * 获取菜单实际注册到 Vue Router 的路径。
     *
     * <p>外链菜单不能直接把 HTTP 地址作为 Vue Router 路径，否则前端菜单组件会把它识别成
     * 浏览器外链并调用 window.open。这里使用菜单主键生成站内路径，原始地址由 meta.link 保存。</p>
     *
     * @param menu 菜单信息
     * @return 前端路由路径
     */
    private String routerPath(MerchantMenu menu)
    {
        if (isExternalMenu(menu))
        {
            return INNER_LINK_PATH_PREFIX + menu.getMenuId();
        }
        if (isRootMenu(menu))
        {
            return "/";
        }
        if (ROOT_ID == menu.getParentId() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame()))
        {
            return "/" + menu.getPath();
        }
        return menu.getPath();
    }

    /**
     * 获取菜单对应的前端组件。
     *
     * <p>外链菜单统一使用 InnerLink 组件，由当前后台的右侧内容区加载页面；普通菜单继续
     * 使用菜单配置的业务组件。</p>
     *
     * @param menu 菜单信息
     * @return 前端组件标识
     */
    private String component(MerchantMenu menu)
    {
        if (isExternalMenu(menu))
        {
            return UserConstants.INNER_LINK;
        }
        // 一级商户菜单必须作为独立的后台布局入口注册，保证其下所有业务页面共享左侧菜单、顶部导航和标签页。
        // 只有挂载在一级菜单下面的目录节点才使用 ParentView，否则直接访问订单等深层地址时会脱离 Layout。
        if (isTopLevelMenu(menu))
        {
            return UserConstants.LAYOUT;
        }
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isRootMenu(menu))
        {
            return menu.getComponent();
        }
        return UserConstants.LAYOUT;
    }

    /**
     * 判断菜单是否为商户端一级菜单。
     *
     * <p>一级菜单是动态路由树的挂载入口，必须承载统一后台布局；如果将一级目录直接配置为
     * ParentView，Vue Router 会直接渲染其子页面，导致订单、仓库等页面缺少左侧菜单和顶部导航。</p>
     *
     * @param menu 待判断的菜单
     * @return true 表示菜单位于商户菜单根节点
     */
    private boolean isTopLevelMenu(MerchantMenu menu)
    {
        return menu.getParentId() != null && menu.getParentId() == ROOT_ID;
    }

    private boolean isRootMenu(MerchantMenu menu)
    {
        return menu.getParentId() != null && menu.getParentId() == ROOT_ID
                && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame());
    }
}
