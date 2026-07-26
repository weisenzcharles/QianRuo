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
