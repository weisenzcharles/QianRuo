<template>
  <div class="app-container merchant-list-page">
    <el-alert title="协议按业务键和版本号管理，确认记录绑定正文哈希；同一商户用户对同一版本重复确认不会生成重复记录。" type="info" :closable="false" />
    <el-card v-for="item in protocols" :key="item.protocolId" shadow="never" class="protocol-card">
      <template #header><div class="protocol-header"><span>{{ item.protocolTitle }}</span><el-tag>v{{ item.versionNo }}</el-tag></div></template>
      <div class="protocol-key">{{ item.protocolKey }} · SHA-256 {{ item.contentHash }}</div>
      <pre class="protocol-content">{{ item.protocolContent }}</pre>
      <el-button v-if="!item.accepted" type="primary" icon="Check" :loading="accepting === item.protocolId" @click="accept(item)">确认协议</el-button>
      <el-tag v-else type="success">已确认</el-tag>
    </el-card>
    <el-empty v-if="!protocols.length" description="暂无待确认协议" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { acceptMerchantProtocol, listMerchantProtocols } from '@/api/merchant/protocol'
import { clearMerchantProtocolCheckCache } from '@/permission'

const protocols = ref([])
const accepting = ref(null)
const route = useRoute()
const router = useRouter()

/** 加载当前商户有效协议版本和当前用户确认状态。 */
function load() {
  listMerchantProtocols({ scopeType: 1 }).then(response => { protocols.value = response.data || [] })
}

/** 二次确认并提交当前商户用户的协议确认记录。 */
function accept(protocol) {
  ElMessageBox.confirm(`确认已阅读并同意《${protocol.protocolTitle}》吗？`, '协议确认', { type: 'warning' }).then(() => {
    accepting.value = protocol.protocolId
    return acceptMerchantProtocol({ protocolId: protocol.protocolId })
  }).then(() => {
    protocol.accepted = true
    ElMessage.success('协议确认成功')
    const pending = protocols.value.some(item => !item.accepted)
    if (!pending) {
      clearMerchantProtocolCheckCache()
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/merchant/home'
      router.replace(redirect)
    }
  }).finally(() => { accepting.value = null })
}

load()
</script>

<style scoped>
.protocol-card { margin-top: 16px; }
.protocol-header { display: flex; align-items: center; justify-content: space-between; }
.protocol-key { color: var(--el-text-color-secondary); font-size: 12px; }
.protocol-content { max-height: 320px; margin: 14px 0; overflow: auto; white-space: pre-wrap; line-height: 1.7; }
</style>
