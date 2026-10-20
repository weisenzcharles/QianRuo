<template>
  <div class="app-container merchant-list-page warehouse-page">
    <header class="page-heading">
      <div>
        <h2>仓库管理</h2>
        <p>维护商户仓库、取件时段和联盟共享设置。</p>
      </div>
    </header>

    <el-form
      v-show="showSearch"
      :inline="true"
      :model="query"
      class="search-form"
      @submit.prevent>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="仓库编码或名称"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部">
          <el-option label="启用" :value="NORMAL_DISABLE_CODE.NORMAL" />
          <el-option label="停用" :value="NORMAL_DISABLE_CODE.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          查询
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openCreate">
          添加仓库
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" row-key="id" border>
      <el-table-column label="仓库编码" prop="code" width="140" />
      <el-table-column label="仓库名称" prop="name" width="120" />

      <el-table-column label="省份" prop="province" min-width="140" />
      <el-table-column label="城市" prop="city" min-width="140" />
      <el-table-column label="负责人" prop="contactName" min-width="80" />
      <el-table-column label="联系电话" prop="contactPhone" min-width="120" />
      <el-table-column
        label="详细地址"
        prop="address"
        min-width="220"
        show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="isNormal(row.status) ? 'success' : 'info'">
            {{ isNormal(row.status) ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortNo" width="90" align="center" />
      <el-table-column label="操作" fixed="right" width="170" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">
            详情
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeWarehouse(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无仓库数据" />
      </template>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="loadList" />

    <el-dialog
      v-model="detailVisible"
      title="仓库详情"
      width="800px"
      append-to-body
      destroy-on-close>
      <el-descriptions
        class="warehouse-descriptions"
        v-loading="detailLoading"
        :column="2"
        border
        label-width="120">
        <el-descriptions-item label="仓库编码">
          {{ warehouseDetail.code || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ warehouseDetail.name || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="负责人">
          {{ warehouseDetail.contactName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ warehouseDetail.contactPhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="所在地区" :span="2">
          {{ regionText(warehouseDetail) }}
        </el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">
          {{ warehouseDetail.address || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="地图坐标">
          {{ coordinateText(warehouseDetail) }}
        </el-descriptions-item>
        <el-descriptions-item label="取件时段">
          {{ pickupTimeText(warehouseDetail.pickupRuleJson) }}
        </el-descriptions-item>
        <el-descriptions-item label="允许归还">
          <el-tag
            :type="
              isEnabled(warehouseDetail.returnEnabled) ? 'success' : 'info'
            ">
            {{ isEnabled(warehouseDetail.returnEnabled) ? "允许" : "不允许" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联盟共享">
          <el-tag
            :type="
              isEnabled(warehouseDetail.allianceShareEnabled)
                ? 'success'
                : 'info'
            ">
            {{
              isEnabled(warehouseDetail.allianceShareEnabled)
                ? "允许"
                : "不允许"
            }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排序">
          {{ warehouseDetail.sortNo ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="isNormal(warehouseDetail.status) ? 'success' : 'info'">
            {{ isNormal(warehouseDetail.status) ? "启用" : "停用" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <span class="detail-remark">{{ warehouseDetail.remark || "-" }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑仓库' : '新增仓库'"
      width="820px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false">
      <el-form
        ref="formRef"
        v-loading="detailLoading"
        :model="form"
        :rules="formRules"
        label-width="120px">
        <el-row :gutter="16">
          <el-col v-if="editing" :span="12">
            <el-form-item label="仓库编码">
              <el-input v-model="form.code" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="editing ? 12 : 24">
            <el-form-item label="仓库名称" prop="name" required>
              <el-input v-model="form.name" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.contactName" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="省份">
              <el-select
                v-model="form.province"
                clearable
                filterable
                placeholder="请选择省份"
                class="field-control"
                @change="changeProvince">
                <el-option
                  v-for="region in provinceOptions"
                  :key="region.code"
                  :label="region.name"
                  :value="region.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-select
                v-model="form.city"
                clearable
                filterable
                placeholder="请选择城市"
                class="field-control"
                @change="changeCity">
                <el-option
                  v-for="region in cityOptions"
                  :key="region.code"
                  :label="region.name"
                  :value="region.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区/县">
              <el-select
                v-model="form.district"
                clearable
                filterable
                placeholder="请选择区/县"
                class="field-control">
                <el-option
                  v-for="region in districtOptions"
                  :key="region.code"
                  :label="region.name"
                  :value="region.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址">
              <el-input
                v-model="form.address"
                clearable
                maxlength="300"
                @input="clearCoordinates">
                <template #append>
                  <el-button @click="addressPickerVisible = true">
                    地图选点
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="取件时段">
              <el-time-picker
                v-model="form.pickupTimeRange"
                is-range
                value-format="HH:mm"
                format="HH:mm"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地图坐标">
              <span v-if="hasCoordinates">
                {{ Number(form.longitude).toFixed(6) }},
                {{ Number(form.latitude).toFixed(6) }}
              </span>
              <span v-else class="muted">选点后自动填入</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许归还">
              <el-switch
                v-model="form.returnEnabled"
                :active-value="YES_NO_CODE.YES"
                :inactive-value="YES_NO_CODE.NO"
                active-text="启用"
                inactive-text="停用"
                inline-prompt />
              <!-- <el-select v-model="form.returnEnabled" class="field-control">
                <el-option label="允许" :value="YES_NO_CODE.YES" />
                <el-option label="不允许" :value="YES_NO_CODE.NO" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许联盟共享">
              <el-switch
                v-model="form.allianceShareEnabled"
                :active-value="YES_NO_CODE.YES"
                :inactive-value="YES_NO_CODE.NO"
                active-text="启用"
                inactive-text="停用"
                inline-prompt />
              <!-- <el-select
                v-model="form.allianceShareEnabled"
                class="field-control">
                <el-option label="允许" :value="YES_NO_CODE.YES" />
                <el-option label="不允许" :value="YES_NO_CODE.NO" />
              </el-select> -->
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number
                v-model="form.sortNo"
                :min="0"
                :max="999999"
                controls-position="right"
                class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="form.status"
                :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                active-text="启用"
                inactive-text="停用"
                inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="saving"
          :disabled="detailLoading"
          @click="submitForm">
          保存
        </el-button>
      </template>
    </el-dialog>

    <AmapAddressPicker
      v-model="addressPickerVisible"
      :address="form.address"
      :longitude="form.longitude"
      :latitude="form.latitude"
      @confirm="applyMapSelection" />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AmapAddressPicker from "@/components/AmapAddressPicker.vue";
import {
  addMerchantWarehouse,
  deleteMerchantWarehouse,
  getMerchantWarehouse,
  listMerchantRegionChildren,
  listMerchantWarehouses,
  updateMerchantWarehouse,
} from "@/api/merchant/rentalBusiness";
import { NORMAL_DISABLE_CODE, YES_NO_CODE } from "@/constants/options";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const saving = ref(false);
const detailLoading = ref(false);
const showSearch = ref(true);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const addressPickerVisible = ref(false);
const editing = ref(false);
const formRef = ref();
const warehouseDetail = ref({});
const provinceOptions = ref([]);
const cityOptions = ref([]);
const districtOptions = ref([]);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined,
});

const createEmptyForm = () => ({
  id: undefined,
  code: "",
  name: "",
  contactName: "",
  contactPhone: "",
  province: undefined,
  city: undefined,
  district: undefined,
  address: "",
  longitude: undefined,
  latitude: undefined,
  pickupTimeRange: [],
  returnEnabled: YES_NO_CODE.YES,
  allianceShareEnabled: YES_NO_CODE.NO,
  remark: "",
  sortNo: 0,
  status: NORMAL_DISABLE_CODE.NORMAL,
});

const form = reactive(createEmptyForm());
const hasCoordinates = computed(
  () => form.longitude != null && form.latitude != null,
);
const formRules = {
  name: [{ required: true, message: "请输入仓库名称", trigger: "blur" }],
};

function unwrapList(response) {
  const result = response?.data || response;
  const data = result?.data || result;
  return {
    rows: data?.rows || [],
    total: Number(data?.total || 0),
  };
}

function loadList() {
  loading.value = true;
  listMerchantWarehouses({ ...query })
    .then((response) => {
      const result = unwrapList(response);
      rows.value = result.rows;
      total.value = result.total;
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery() {
  query.pageNum = 1;
  loadList();
}

function resetQuery() {
  query.pageNum = 1;
  query.pageSize = 10;
  query.keyword = "";
  query.status = undefined;
  loadList();
}

function resetForm() {
  Object.assign(form, createEmptyForm());
  provinceOptions.value = [];
  cityOptions.value = [];
  districtOptions.value = [];
  formRef.value?.clearValidate();
}

function openCreate() {
  editing.value = false;
  resetForm();
  dialogVisible.value = true;
  loadRegions("0", provinceOptions);
}

async function openDetail(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  warehouseDetail.value = {};
  try {
    const response = await getMerchantWarehouse(row.id);
    warehouseDetail.value = response?.data?.data || response?.data || response;
  } finally {
    detailLoading.value = false;
  }
}

async function openEdit(row) {
  editing.value = true;
  detailLoading.value = true;
  resetForm();
  try {
    const response = await getMerchantWarehouse(row.id);
    const detail = response?.data?.data || response?.data || response;
    Object.assign(form, createEmptyForm(), {
      id: detail.id,
      code: detail.code || "",
      name: detail.name || "",
      contactName: detail.contactName || "",
      contactPhone: detail.contactPhone || "",
      province: detail.province || undefined,
      city: detail.city || undefined,
      district: detail.district || undefined,
      address: detail.address || "",
      longitude: detail.longitude,
      latitude: detail.latitude,
      pickupTimeRange: parsePickupRule(detail.pickupRuleJson),
      returnEnabled: Number(detail.returnEnabled ?? YES_NO_CODE.YES),
      allianceShareEnabled: Number(
        detail.allianceShareEnabled ?? YES_NO_CODE.NO,
      ),
      remark: detail.remark || "",
      sortNo: Number(detail.sortNo || 0),
      status: Number(detail.status ?? NORMAL_DISABLE_CODE.NORMAL),
    });
    dialogVisible.value = true;
    await loadWarehouseRegions();
  } finally {
    detailLoading.value = false;
  }
}

function loadRegions(parentCode, target) {
  listMerchantRegionChildren(parentCode).then((response) => {
    target.value = response?.data || response?.rows || [];
  });
}

async function loadWarehouseRegions() {
  const provinces = await listMerchantRegionChildren("0");
  provinceOptions.value = provinces?.data || provinces?.rows || [];
  const province = findRegion(provinceOptions.value, form.province);
  if (!province) return;

  const cities = await listMerchantRegionChildren(province.code);
  cityOptions.value = cities?.data || cities?.rows || [];
  const city = findRegion(cityOptions.value, form.city);
  if (!city) return;

  const districts = await listMerchantRegionChildren(city.code);
  districtOptions.value = districts?.data || districts?.rows || [];
}

function changeProvince(name) {
  const province = provinceOptions.value.find((item) => item.name === name);
  form.city = undefined;
  form.district = undefined;
  cityOptions.value = [];
  districtOptions.value = [];
  if (province) loadRegions(province.code, cityOptions);
}

function changeCity(name) {
  const city = cityOptions.value.find((item) => item.name === name);
  form.district = undefined;
  districtOptions.value = [];
  if (city) loadRegions(city.code, districtOptions);
}

function parsePickupRule(value) {
  if (!value) return [];
  try {
    const rule = typeof value === "string" ? JSON.parse(value) : value;
    const start = String(rule.start || rule.startTime || "").slice(0, 5);
    const end = String(rule.end || rule.endTime || "").slice(0, 5);
    return start && end ? [start, end] : [];
  } catch {
    return [];
  }
}

function clearCoordinates() {
  form.longitude = undefined;
  form.latitude = undefined;
}

async function applyMapSelection(location) {
  form.address = location.address;
  form.longitude = location.longitude;
  form.latitude = location.latitude;
  form.province = undefined;
  form.city = undefined;
  form.district = undefined;
  cityOptions.value = [];
  districtOptions.value = [];

  try {
    const provinces = await listMerchantRegionChildren("0");
    provinceOptions.value = provinces?.data || provinces?.rows || [];
    const province = findRegion(provinceOptions.value, location.province);
    if (!province) {
      ElMessage.warning("地址和坐标已更新，省市区请手动选择");
      return;
    }
    form.province = province.name;

    const cities = await listMerchantRegionChildren(province.code);
    cityOptions.value = cities?.data || cities?.rows || [];
    const city = findRegion(
      cityOptions.value,
      location.city || location.province,
    );
    if (!city) {
      ElMessage.warning("地址和坐标已更新，城市和区县请手动选择");
      return;
    }
    form.city = city.name;

    const districts = await listMerchantRegionChildren(city.code);
    districtOptions.value = districts?.data || districts?.rows || [];
    const district = findRegion(districtOptions.value, location.district);
    if (district) {
      form.district = district.name;
    } else if (location.district) {
      ElMessage.warning("地址和坐标已更新，区县请手动选择");
    }
  } catch {
    ElMessage.warning("地址和坐标已更新，行政区划请手动确认");
  }
}

function findRegion(options, name) {
  const normalize = (value) =>
    String(value || "").replace(/(特别行政区|自治区|省|市)$/, "");
  const expected = normalize(name);
  return options.find((item) => normalize(item.name) === expected);
}

function regionText(detail) {
  return (
    [detail.province, detail.city, detail.district]
      .filter(Boolean)
      .join(" / ") || "-"
  );
}

function coordinateText(detail) {
  const longitude = Number(detail.longitude);
  const latitude = Number(detail.latitude);
  if (
    detail.longitude == null ||
    detail.latitude == null ||
    !Number.isFinite(longitude) ||
    !Number.isFinite(latitude)
  ) {
    return "-";
  }
  return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`;
}

function pickupTimeText(rule) {
  const [start, end] = parsePickupRule(rule);
  return start && end ? `${start} 至 ${end}` : "未设置";
}

function isEnabled(value) {
  return Number(value) === YES_NO_CODE.YES;
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  const [start, end] = form.pickupTimeRange || [];
  const payload = { ...form };
  payload.pickupRuleJson = start && end ? JSON.stringify({ start, end }) : null;
  delete payload.pickupTimeRange;
  if (!editing.value) delete payload.id;

  saving.value = true;
  try {
    if (editing.value) await updateMerchantWarehouse(payload);
    else await addMerchantWarehouse(payload);
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    loadList();
  } finally {
    saving.value = false;
  }
}

function removeWarehouse(row) {
  ElMessageBox.confirm(`确认删除仓库“${row.name}”吗？`, "操作确认", {
    type: "warning",
  })
    .then(() => deleteMerchantWarehouse(row.id))
    .then(() => {
      ElMessage.success("删除成功");
      loadList();
    })
    .catch(() => {});
}

function isNormal(status) {
  return Number(status) === NORMAL_DISABLE_CODE.NORMAL;
}

loadList();
</script>

<style scoped>
.warehouse-page {
  min-height: calc(100vh - 84px);
}

.field-control {
  width: 100%;
}

.warehouse-descriptions :deep(.el-descriptions__table) {
  width: 100%;
  table-layout: fixed;
}

.warehouse-descriptions :deep(.el-descriptions__cell) {
  overflow-wrap: anywhere;
}

.muted {
  color: var(--el-text-color-placeholder);
}

.detail-remark {
  white-space: pre-wrap;
}
</style>
