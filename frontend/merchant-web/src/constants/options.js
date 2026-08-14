export const NORMAL_DISABLE_OPTIONS = Object.freeze([
  Object.freeze({ label: '正常', value: '0', elTagType: 'success' }),
  Object.freeze({ label: '停用', value: '1', elTagType: 'danger' })
])

export const OPER_TYPE_OPTIONS = Object.freeze([
  Object.freeze({ label: '其他', value: 0, elTagType: 'info' }),
  Object.freeze({ label: '新增', value: 1, elTagType: 'success' }),
  Object.freeze({ label: '修改', value: 2, elTagType: 'primary' }),
  Object.freeze({ label: '删除', value: 3, elTagType: 'danger' }),
  Object.freeze({ label: '授权', value: 4, elTagType: 'warning' }),
  Object.freeze({ label: '导出', value: 5, elTagType: 'warning' }),
  Object.freeze({ label: '导入', value: 6, elTagType: 'success' }),
  Object.freeze({ label: '强退', value: 7, elTagType: 'danger' }),
  Object.freeze({ label: '生成代码', value: 8, elTagType: 'primary' }),
  Object.freeze({ label: '清空数据', value: 9, elTagType: 'danger' })
])

export const OPER_STATUS_OPTIONS = Object.freeze([
  Object.freeze({ label: '正常', value: 0, elTagType: 'success' }),
  Object.freeze({ label: '异常', value: 1, elTagType: 'danger' })
])

/** 商户业务审计动作字典，值与后端 MerchantBusinessAuditCommand.operationType 保持一致。 */
export const MERCHANT_AUDIT_TYPE_OPTIONS = Object.freeze([
  Object.freeze({ label: '创建', value: 'CREATE', elTagType: 'success' }),
  Object.freeze({ label: '编辑', value: 'UPDATE', elTagType: 'warning' }),
  Object.freeze({ label: '删除', value: 'DELETE', elTagType: 'danger' }),
  Object.freeze({ label: '登录', value: 'LOGIN', elTagType: 'primary' }),
  Object.freeze({ label: '修改密码', value: 'CHANGE_PASSWORD', elTagType: 'warning' }),
  Object.freeze({ label: '重置密码', value: 'RESET_PASSWORD', elTagType: 'warning' }),
  Object.freeze({ label: '授权权限', value: 'GRANT_PERMISSION', elTagType: 'primary' }),
  Object.freeze({ label: '撤销权限', value: 'REVOKE_PERMISSION', elTagType: 'warning' }),
  Object.freeze({ label: '导入', value: 'IMPORT', elTagType: 'success' }),
  Object.freeze({ label: '导出', value: 'EXPORT', elTagType: 'info' })
])
