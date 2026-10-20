<template>
  <component :is="type" v-bind="linkProps()">
    <slot />
  </component>
</template>

<script setup>
import { resolveMerchantMenuTarget } from '@/utils/merchantMenu'

const props = defineProps({
  to: {
    type: [String, Object],
    required: true
  }
})

const type = 'router-link'

/**
 * 生成菜单组件的路由参数。
 *
 * <p>所有菜单都渲染为 Vue Router 链接；HTTP/HTTPS 地址会在公共导航工具中转换为
 * 商户端内嵌路由，因此这里不会生成 href、target 或新窗口参数。</p>
 *
 * @returns {{to: string|Object}} 当前菜单的站内路由参数
 */
function linkProps() {
  return {
    to: resolveMerchantMenuTarget(props.to)
  }
}
</script>
