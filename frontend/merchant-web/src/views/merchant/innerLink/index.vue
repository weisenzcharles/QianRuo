<template>
  <div class="merchant-inner-link">
    <iframe
      v-if="targetUrl"
      :src="targetUrl"
      title="商户内嵌页面"
      frameborder="0"
      class="merchant-inner-link__frame"
    />
    <el-empty v-else description="页面地址不存在" />
  </div>
</template>

<script setup>
/** 当前路由用于读取菜单传入的外部页面地址。 */
const route = useRoute()

/**
 * 获取当前菜单需要在内容区加载的页面地址。
 *
 * @returns {string} iframe 页面地址；地址缺失时返回空字符串
 */
const targetUrl = computed(() => {
  const url = route.query.url
  return typeof url === 'string' ? url : ''
})
</script>

<style scoped>
.merchant-inner-link {
  width: 100%;
  height: calc(100vh - 84px);
  min-height: 560px;
  overflow: hidden;
  background: var(--el-bg-color);
}

.merchant-inner-link__frame {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
}
</style>
