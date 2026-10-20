<template>
  <div class="app-container inventory-sync-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">EXTERNAL INVENTORY SYNC</p>
        <h2>外部库存同步</h2>
        <p class="page-description">为外部系统授权读取库存，并按签名预占或释放库存，令牌仅显示一次</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadTokens">刷新</el-button>
    </header>

    <el-alert title="读取库存使用 GET /open/inventory/snapshot；预占使用 POST /open/inventory/reservations，释放使用 POST /open/inventory/reservations/{reservationNo}/release。写请求必须携带 X-Inventory-Token、X-Request-Id 和 X-Inventory-Signature；请妥善保管原始令牌。" type="info" :closable="false" show-icon class="sync-alert" />
    <el-card shadow="never">
      <template #header><div class="section-header"><strong>同步令牌</strong><el-button v-hasPermi="['merchant:inventory:sync:edit']" type="primary" :icon="Plus" @click="openCreate">创建令牌</el-button></div></template>
      <el-table v-loading="loading" :data="tokens" border row-key="tokenId">
        <el-table-column label="令牌名称" prop="tokenName" min-width="180" />
        <el-table-column label="令牌前缀" prop="tokenPrefix" width="180" />
        <el-table-column label="授权范围" prop="scopesLabel" min-width="130" />
        <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="Number(row.tokenStatus) === TOKEN_STATUS.ENABLED ? 'success' : 'info'">{{ row.tokenStatusLabel }}</el-tag></template></el-table-column>
        <el-table-column label="过期时间" prop="expireTime" width="170"><template #default="{ row }">{{ row.expireTime || '长期有效' }}</template></el-table-column>
        <el-table-column label="最近使用" prop="lastUsedTime" width="170"><template #default="{ row }">{{ row.lastUsedTime || '尚未使用' }}</template></el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="100" align="center"><template #default="{ row }"><el-button v-if="Number(row.tokenStatus) === TOKEN_STATUS.ENABLED" v-hasPermi="['merchant:inventory:sync:edit']" link type="danger" @click="revoke(row)">吊销</el-button><span v-else class="muted-text">-</span></template></el-table-column>
        <template #empty><el-empty description="暂无同步令牌" /></template>
      </el-table>
    </el-card>

    <el-card shadow="never" class="log-card">
      <template #header>
        <div class="section-header">
          <strong>同步调用记录</strong>
          <div class="log-tools">
            <el-select v-model="selectedTokenId" clearable placeholder="全部令牌" style="width: 180px" @change="loadLogs">
              <el-option v-for="item in tokens" :key="item.tokenId" :label="item.tokenName" :value="item.tokenId" />
            </el-select>
            <el-button :icon="Refresh" :loading="logLoading" @click="loadLogs">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="logLoading" :data="logs" border>
        <el-table-column label="令牌名称" prop="tokenName" min-width="160" show-overflow-tooltip />
        <el-table-column label="请求标识" prop="requestId" min-width="190" show-overflow-tooltip />
        <el-table-column label="请求路径" prop="requestPath" min-width="210" show-overflow-tooltip />
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.resultLabel === '成功' ? 'success' : 'danger'">{{ row.resultLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="返回数量" prop="itemCount" width="100" align="right" />
        <el-table-column label="耗时（毫秒）" prop="latencyMs" width="120" align="right" />
        <el-table-column label="结果说明" prop="resultMessage" min-width="180" show-overflow-tooltip />
        <el-table-column label="调用时间" prop="createTime" width="175" />
        <template #empty><el-empty description="暂无同步调用记录" /></template>
      </el-table>
    </el-card>

    <el-dialog v-model="createVisible" title="创建同步令牌" width="500px" append-to-body destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="90px">
        <el-form-item label="令牌名称" prop="tokenName"><el-input v-model="createForm.tokenName" maxlength="80" show-word-limit placeholder="例如：电商平台库存读取" /></el-form-item>
        <el-form-item label="授权范围"><el-checkbox v-model="inventoryRead" disabled>读取库存</el-checkbox><el-checkbox v-model="inventoryReserve">预占和释放库存</el-checkbox></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="createForm.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" clearable placeholder="不填写表示长期有效" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible = false">取消</el-button><el-button type="primary" :loading="creating" @click="createToken">创建</el-button></template>
    </el-dialog>

    <el-dialog v-model="tokenVisible" title="同步令牌已创建" width="620px" append-to-body>
      <el-alert title="原始令牌只显示这一次，关闭后无法再次找回。" type="warning" :closable="false" show-icon />
      <div class="token-box"><code>{{ createdToken }}</code><el-button text :icon="CopyDocument" @click="copyToken">复制</el-button></div>
      <template #footer><el-button type="primary" @click="tokenVisible = false">我已保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, Plus, Refresh } from '@element-plus/icons-vue'
import { createInventorySyncToken, listInventorySyncLogs, listInventorySyncTokens, revokeInventorySyncToken } from '@/api/merchant/inventoryGovernance'

const TOKEN_STATUS = Object.freeze({ ENABLED: 0, REVOKED: 1 })
const loading = ref(false)
const creating = ref(false)
const tokens = ref([])
const logs = ref([])
const logLoading = ref(false)
const selectedTokenId = ref(undefined)
const createVisible = ref(false)
const tokenVisible = ref(false)
const createdToken = ref('')
const inventoryRead = ref(true)
const inventoryReserve = ref(false)
const createFormRef = ref()
const createForm = reactive({ tokenName: '', expireTime: undefined })
const rules = { tokenName: [{ required: true, message: '请输入令牌名称', trigger: 'blur' }] }

function unwrap(response) { return response?.data ?? response }
function rowsOf(response) { const data = unwrap(response); return Array.isArray(data) ? data : data?.rows || [] }
function errorText(error) { return error?.msg || error?.message || '操作失败，请稍后重试' }
async function loadTokens() { loading.value = true; try { tokens.value = rowsOf(await listInventorySyncTokens()) } catch (error) { ElMessage.error(errorText(error)) } finally { loading.value = false } }
async function loadLogs() { logLoading.value = true; try { logs.value = rowsOf(await listInventorySyncLogs({ tokenId: selectedTokenId.value })) } catch (error) { ElMessage.error(errorText(error)) } finally { logLoading.value = false } }
function openCreate() { Object.assign(createForm, { tokenName: '', expireTime: undefined }); inventoryReserve.value = false; createVisible.value = true }
async function createToken() { const valid = await createFormRef.value?.validate().catch(() => false); if (!valid) return; creating.value = true; try { const scopes = ['inventory:read']; if (inventoryReserve.value) scopes.push('inventory:reserve'); const result = unwrap(await createInventorySyncToken({ tokenName: createForm.tokenName, scopes, expireTime: createForm.expireTime })); createdToken.value = result?.token || ''; createVisible.value = false; tokenVisible.value = true; await loadTokens() } catch (error) { ElMessage.error(errorText(error)) } finally { creating.value = false } }
async function revoke(row) { try { await ElMessageBox.confirm('吊销后外部系统将无法继续读取库存，是否继续？', '吊销确认', { type: 'warning' }); await revokeInventorySyncToken(row.tokenId); ElMessage.success('同步令牌已吊销'); await loadTokens(); await loadLogs() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
async function copyToken() { try { await navigator.clipboard.writeText(createdToken.value); ElMessage.success('令牌已复制') } catch (error) { ElMessage.warning('复制失败，请手动复制令牌') } }
onMounted(() => { loadTokens(); loadLogs() })
</script>

<style scoped>
.inventory-sync-page { min-height: 100%; }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-kicker { margin: 0 0 4px; color: var(--el-color-primary); font-size: 12px; font-weight: 700; letter-spacing: 1px; }
.page-heading h2 { margin: 0; }
.page-description { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.sync-alert { margin-bottom: 16px; }
.log-card { margin-top: 16px; }
.section-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.log-tools { display: flex; align-items: center; gap: 10px; }
.token-box { display: flex; align-items: center; gap: 12px; margin-top: 16px; padding: 14px; border: 1px solid var(--el-border-color); background: var(--el-fill-color-light); }
.token-box code { flex: 1; overflow-wrap: anywhere; color: var(--el-color-primary); }
.muted-text { color: var(--el-text-color-secondary); }
</style>
