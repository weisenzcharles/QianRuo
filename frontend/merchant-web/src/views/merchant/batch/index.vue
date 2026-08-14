<template>
  <div class="app-container merchant-list-page">
    <el-alert title="导入按行执行现有领域校验并返回失败行；导入文件占用商户文件配额，导出操作只记录审计不重复占用永久配额。" type="info" :closable="false" />
    <el-card shadow="never" class="batch-card">
      <el-form :inline="true" :model="form">
        <el-form-item label="数据资源"><el-select v-model="form.resource" @change="clearFile"><el-option v-for="item in resources" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item><el-upload :auto-upload="false" :limit="1" :show-file-list="true" :on-change="handleFileChange" :on-remove="clearFile"><el-button icon="Upload">选择 Excel 文件</el-button></el-upload></el-form-item>
        <el-form-item><el-button type="primary" :loading="loading" :disabled="!file" icon="UploadFilled" @click="handleImport">开始导入</el-button></el-form-item>
        <el-form-item><el-button type="success" plain :loading="loading" icon="Download" @click="handleExport">导出当前资源</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="result" shadow="never" class="batch-card">
      <template #header>导入结果</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="总行数">{{ result.total }}</el-descriptions-item>
        <el-descriptions-item label="成功">{{ result.success }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ result.failed }}</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="result.errors?.length" class="error-list" title="失败行明细" type="error" :closable="false"><template #default><div v-for="item in result.errors" :key="item">{{ item }}</div></template></el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { exportMerchantResource, importMerchantResource } from '@/api/merchant/batch'

const resources = [
  { label: '仓库', value: 'warehouse' },
  { label: '资源池', value: 'pool' },
  { label: '商品', value: 'product' },
  { label: 'SN', value: 'sn' },
  { label: '配件', value: 'accessory' },
  { label: '套餐', value: 'package' }
]
const form = reactive({ resource: 'product' })
const file = ref(null)
const loading = ref(false)
const result = ref(null)

/** 接收 Element Plus 选择的原始文件，并保存为后端上传对象。 */
function handleFileChange(uploadFile) {
  file.value = uploadFile.raw
}

/** 清除当前资源或文件选择。 */
function clearFile() {
  file.value = null
  result.value = null
}

/** 上传当前资源 Excel，并展示逐行处理结果。 */
function handleImport() {
  if (!file.value) return ElMessage.warning('请先选择 Excel 文件')
  loading.value = true
  importMerchantResource(form.resource, file.value).then(response => { result.value = response.data; ElMessage.success('导入处理完成') }).finally(() => { loading.value = false })
}

/** 下载当前商户资源的 Excel 导出文件。 */
function handleExport() {
  loading.value = true
  exportMerchantResource(form.resource).then(data => { saveAs(new Blob([data]), `${form.resource}-export.xlsx`) }).finally(() => { loading.value = false })
}
</script>

<style scoped>
.batch-card { margin-top: 16px; }
.error-list { margin-top: 16px; }
</style>
