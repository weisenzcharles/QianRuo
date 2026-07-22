import request from '@/utils/request'

// 查询商户列表
export function listMember(query) {
  return request({
    url: '/system/rental/member/list',
    method: 'get',
    params: query
  })
}

// 查询商户详细
export function getMember(memberId) {
  return request({
    url: '/system/rental/member/' + memberId,
    method: 'get'
  })
}

// 新增商户
export function addMember(data) {
  return request({
    url: '/system/rental/member',
    method: 'post',
    data: data
  })
}

// 修改商户
export function updateMember(data) {
  return request({
    url: '/system/rental/member',
    method: 'put',
    data: data
  })
}

// 删除商户
export function delMember(memberId) {
  return request({
    url: '/system/rental/member/' + memberId,
    method: 'delete'
  })
}
