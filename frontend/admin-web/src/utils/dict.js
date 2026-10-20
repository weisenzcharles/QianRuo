import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

const NUMERIC_DICT_TYPES = new Set([
  'sys_normal_disable',
  'sys_show_hide',
  'sys_yes_no',
  'sys_job_status',
  'sys_notice_status',
  'sys_notice_type'
])

function normalizeDictValue(dictType, value) {
  if (!NUMERIC_DICT_TYPES.has(dictType)) {
    return value
  }
  const numericValue = Number(value)
  return Number.isInteger(numericValue) ? numericValue : value
}

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({})
  return (() => {
    args.forEach((dictType, index) => {
      res.value[dictType] = []
      const dicts = useDictStore().getDict(dictType)
      if (dicts) {
        res.value[dictType] = dicts.map(item => ({
          ...item,
          value: normalizeDictValue(dictType, item.value)
        }))
      } else {
        getDicts(dictType).then(resp => {
          res.value[dictType] = resp.data.map(p => ({
            label: p.dictLabel,
            value: normalizeDictValue(dictType, p.dictValue),
            elTagType: p.listClass,
            elTagClass: p.cssClass
          }))
          useDictStore().setDict(dictType, res.value[dictType])
        })
      }
    })
    return toRefs(res.value)
  })()
}
