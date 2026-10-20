<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择仓库地址"
    width="900px"
    append-to-body
    :close-on-click-modal="false"
    @opened="initializeMap"
    @closed="destroyMap">
    <el-input
      v-model="keyword"
      clearable
      placeholder="搜索地点或地址"
      @keyup.enter="searchLocation">
      <template #append>
        <el-button :loading="searching" @click="searchLocation">
          搜索
        </el-button>
      </template>
    </el-input>
    <div ref="mapContainer" class="map-container" />
    <div class="location-summary">
      <span>{{ selectedAddress || "点击地图选择位置" }}</span>
      <span>{{ coordinateLabel }}</span>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :disabled="!selectedLocation || !selectedAddress || resolving"
        @click="confirmSelection">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { getMerchantAmapJsConfig } from "@/api/merchant/rentalBusiness";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  address: { type: String, default: "" },
  longitude: { type: [Number, String], default: undefined },
  latitude: { type: [Number, String], default: undefined },
});

const emit = defineEmits(["update:modelValue", "confirm"]);
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const mapContainer = ref();
const keyword = ref("");
const selectedAddress = ref("");
const selectedRegion = ref({ province: "", city: "", district: "" });
const selectedLocation = ref();
const resolving = ref(false);
const searching = ref(false);
const coordinateLabel = computed(() => {
  if (!selectedLocation.value) return "";
  return `${selectedLocation.value.longitude.toFixed(6)}, ${selectedLocation.value.latitude.toFixed(6)}`;
});

let mapApiPromise;
let loaderScriptPromise;
let mapApi;
let mapInstance;
let mapMarker;
let geocoder;
let placeSearch;
let lookupVersion = 0;

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) return;
    keyword.value = "";
    selectedAddress.value = props.address || "";
    selectedRegion.value = { province: "", city: "", district: "" };
    const longitude = Number(props.longitude);
    const latitude = Number(props.latitude);
    selectedLocation.value =
      Number.isFinite(longitude) && Number.isFinite(latitude)
        ? { longitude, latitude }
        : undefined;
  },
);

async function loadAmapApi() {
  if (mapApiPromise) return mapApiPromise;
  mapApiPromise = (async () => {
    const response = await getMerchantAmapJsConfig();
    const config = response.data || response;
    if (!config.jsApiKey) {
      throw new Error("平台尚未配置高德地图 JS API Key");
    }
    if (!config.securityJsCode) {
      throw new Error("平台尚未配置高德地图 JS API 安全密钥");
    }
    window._AMapSecurityConfig = { securityJsCode: config.securityJsCode };
    const loader = await loadAmapLoader();
    return loader.load({
      key: config.jsApiKey,
      version: "2.0",
      plugins: ["AMap.Geocoder", "AMap.PlaceSearch", "AMap.Scale"],
    });
  })().catch((error) => {
    mapApiPromise = undefined;
    throw error;
  });
  return mapApiPromise;
}

function loadAmapLoader() {
  if (window.AMapLoader) return Promise.resolve(window.AMapLoader);
  if (!loaderScriptPromise) {
    loaderScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = "https://webapi.amap.com/loader.js";
      script.async = true;
      script.onload = () => {
        if (window.AMapLoader) resolve(window.AMapLoader);
        else reject(new Error("高德地图加载器不可用"));
      };
      script.onerror = () => reject(new Error("高德地图加载失败"));
      document.head.appendChild(script);
    }).catch((error) => {
      loaderScriptPromise = undefined;
      throw error;
    });
  }
  return loaderScriptPromise;
}

async function initializeMap() {
  if (!mapContainer.value || mapInstance) return;
  try {
    mapApi = await loadAmapApi();
    if (!mapContainer.value || !dialogVisible.value) return;

    const hasLocation = Boolean(selectedLocation.value);
    const center = hasLocation
      ? [selectedLocation.value.longitude, selectedLocation.value.latitude]
      : [116.397428, 39.90923];
    mapInstance = new mapApi.Map(mapContainer.value, {
      center,
      zoom: hasLocation ? 16 : 11,
      resizeEnable: true,
    });
    mapMarker = new mapApi.Marker({
      position: center,
      draggable: true,
      visible: hasLocation,
    });
    mapInstance.add(mapMarker);
    geocoder = new mapApi.Geocoder({ radius: 1000, extensions: "all" });
    placeSearch = new mapApi.PlaceSearch({ city: "全国", pageSize: 10 });
    mapInstance.addControl(new mapApi.Scale());
    mapInstance.on("click", (event) => selectPoint(event.lnglat));
    mapMarker.on("dragend", (event) => selectPoint(event.lnglat));
  } catch (error) {
    ElMessage.error(error.message || "高德地图初始化失败");
  }
}

function searchLocation() {
  if (!keyword.value.trim() || !placeSearch) return;
  searching.value = true;
  placeSearch.search(keyword.value.trim(), (status, result) => {
    searching.value = false;
    const first = result?.poiList?.pois?.[0];
    if (status !== "complete" || !first?.location) {
      ElMessage.warning("没有找到匹配地点");
      return;
    }
    mapInstance.setZoom(16);
    mapInstance.setCenter(first.location);
    selectPoint(first.location);
  });
}

function selectPoint(point) {
  const longitude = Number(point?.lng ?? point?.getLng?.());
  const latitude = Number(point?.lat ?? point?.getLat?.());
  if (!Number.isFinite(longitude) || !Number.isFinite(latitude)) return;

  selectedLocation.value = { longitude, latitude };
  mapMarker?.show();
  mapMarker?.setPosition([longitude, latitude]);
  selectedAddress.value = "";
  selectedRegion.value = { province: "", city: "", district: "" };
  resolving.value = true;
  const currentLookup = ++lookupVersion;
  geocoder.getAddress([longitude, latitude], (status, result) => {
    if (currentLookup !== lookupVersion) return;
    resolving.value = false;
    const regeocode = result?.regeocode;
    if (status !== "complete" || !regeocode) {
      ElMessage.warning("地址解析失败，请在地图上重新选点");
      return;
    }
    selectedAddress.value = regeocode.formattedAddress || "";
    const component = regeocode.addressComponent || {};
    selectedRegion.value = {
      province: normalizeRegion(component.province),
      city: normalizeRegion(component.city) || normalizeRegion(component.province),
      district: normalizeRegion(component.district),
    };
  });
}

function normalizeRegion(value) {
  return Array.isArray(value) ? String(value[0] || "") : String(value || "");
}

function confirmSelection() {
  if (!selectedLocation.value || !selectedAddress.value || resolving.value) return;
  emit("confirm", {
    address: selectedAddress.value,
    longitude: selectedLocation.value.longitude,
    latitude: selectedLocation.value.latitude,
    ...selectedRegion.value,
  });
  dialogVisible.value = false;
}

function destroyMap() {
  lookupVersion += 1;
  resolving.value = false;
  if (mapInstance) mapInstance.destroy();
  mapInstance = undefined;
  mapMarker = undefined;
  geocoder = undefined;
  placeSearch = undefined;
}

onBeforeUnmount(destroyMap);
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 440px;
  margin-top: 12px;
  border: 1px solid var(--el-border-color);
}

.location-summary {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
  color: var(--el-text-color-regular);
}

.location-summary span:first-child {
  min-width: 0;
  overflow-wrap: anywhere;
}
</style>
