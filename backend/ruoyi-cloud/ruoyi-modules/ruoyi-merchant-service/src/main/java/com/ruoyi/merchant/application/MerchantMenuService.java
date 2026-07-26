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

    public List<MerchantRouterVO> currentRouters()
    {
        List<MerchantMenu> menus = menuMapper.selectByUser(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId());
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
        if (StringUtils.ishttp(menu.getPath()))
        {
            meta.setLink(menu.getPath());
        }
        return meta;
    }

    private String routeName(MerchantMenu menu)
    {
        String name = StringUtils.isNotEmpty(menu.getRouteName()) ? menu.getRouteName() : menu.getPath();
        return StringUtils.capitalize(name);
    }

    private String routerPath(MerchantMenu menu)
    {
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

    private String component(MerchantMenu menu)
    {
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isRootMenu(menu))
        {
            return menu.getComponent();
        }
        return UserConstants.LAYOUT;
    }

    private boolean isRootMenu(MerchantMenu menu)
    {
        return menu.getParentId() != null && menu.getParentId() == ROOT_ID
                && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame());
    }
}
