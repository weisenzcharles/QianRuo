import request from '@/utils/request'

// 查询菜单列表
export function listMenu(query) {
  return request({
    url: '/merchant/menu/list',
    method: 'get',
    params: query
  })
}

// 查询菜单详细
export function getMenu(menuId) {
  return request({
    url: '/merchant/menu/' + menuId,
    method: 'get'
  })
}

// 查询菜单下拉树结构
export function treeselect() {
  return request({
    url: '/merchant/menu/treeselect',
    method: 'get'
  })
}

// 根据角色编号查询菜单下拉树结构
export function roleMenuTreeselect(roleId) {
  return request({
    url: '/merchant/menu/roleMenuTreeselect/' + roleId,
    method: 'get'
  })
}
