<template>
  <div class="product-page">
    <aside class="class-panel">
      <h3>经营类目</h3>
      <el-skeleton :loading="loading" animated :rows="6">
        <template #default>
          <el-empty v-if="classTree.length === 0" description="暂无经营类目" :image-size="72" />
          <el-tree
            v-else
            :data="classTree"
            node-key="classId"
            default-expand-all
            :expand-on-click-node="false"
            :props="{ label: 'className', children: 'children' }"
          />
        </template>
      </el-skeleton>
    </aside>
    <main class="product-content">
      <stage-one-panel kicker="Product" title="商品资料" :items="items" />
    </main>
  </div>
</template>

<script setup>
import { listBusinessProductClasses } from '@/api/merchant/productClass'
import StageOnePanel from '../components/StageOnePanel.vue'

const classTree = ref([])
const loading = ref(false)
const items = [
  { title: '品牌型号', desc: '维护平台标准型号和商家常用型号。' },
  { title: '商品/SN', desc: '维护商品、SN、配件、二维码和条码。' },
  { title: '套餐', desc: '维护录单快捷套餐和适用渠道配置。' }
]

/**
 * 加载当前登录商户已选择的经营类目，商品业务不读取平台全量类目。
 */
function loadBusinessClasses() {
  loading.value = true
  listBusinessProductClasses().then(response => {
    classTree.value = response.data || []
  }).finally(() => {
    loading.value = false
  })
}

loadBusinessClasses()
</script>

<style scoped>
.product-page {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(0, 1fr);
  min-height: calc(100vh - 84px);
}

.class-panel {
  padding: 20px 16px;
  border-right: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}

.class-panel h3 {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
}

.product-content {
  min-width: 0;
}

@media (max-width: 768px) {
  .product-page {
    grid-template-columns: 1fr;
  }

  .class-panel {
    border-right: 0;
    border-bottom: 1px solid var(--el-border-color-light);
  }
}
</style>
