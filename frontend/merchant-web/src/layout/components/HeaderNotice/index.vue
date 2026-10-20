<template>
  <div>
    <el-popover
      ref="noticePopover"
      placement="bottom-end"
      :width="320"
      trigger="manual"
      v-model:visible="noticeVisible"
      popper-class="notice-popover"
    >
      <div class="notice-header">
        <span class="notice-title">消息通知</span>
        <span class="notice-mark-all" @click="markAllRead">全部已读</span>
      </div>
      <div v-if="noticeLoading" class="notice-loading">
        <el-icon class="is-loading"><Loading /></el-icon> 加载中...
      </div>
      <div v-else-if="noticeList.length === 0" class="notice-empty">
        <el-icon class="notice-empty-icon"><Postcard /></el-icon>
        暂无公告
      </div>
      <div v-else>
        <div
          v-for="item in noticeList"
          :key="item.noticeId"
          class="notice-item"
          :class="{ 'is-read': item.isRead }"
          @click="previewNotice(item)"
        >
          <el-tag
            size="small"
            :type="noticeTagType(item)"
            class="notice-tag"
          >
            {{ noticeTagLabel(item) }}
          </el-tag>
          <span class="notice-item-title">{{ item.noticeTitle }}</span>
          <span class="notice-item-date">{{ item.createTime }}</span>
        </div>
      </div>

      <template #reference>
        <div class="right-menu-item hover-effect notice-trigger" @mouseenter="onNoticeEnter" @mouseleave="onNoticeLeave">
          <el-icon><Bell /></el-icon>
          <span v-if="unreadCount > 0" class="notice-badge">{{ unreadCount }}</span>
        </div>
      </template>
    </el-popover>

    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="680px"
      append-to-body
      class="notice-preview-dialog"
    >
      <div class="notice-preview-meta">
        <el-tag size="small" :type="previewSourceType === NOTICE_SOURCE.INTERNAL ? 'primary' : (previewNoticeType === NOTICE_TYPE_CODE.NOTIFICATION ? 'warning' : 'success')">
          {{ previewSourceType === NOTICE_SOURCE.INTERNAL ? '内部通知' : (previewNoticeType === NOTICE_TYPE_CODE.NOTIFICATION ? '通知' : '公告') }}
        </el-tag>
        <span class="notice-preview-info">
          <el-icon><User /></el-icon> {{ previewPublisher }}
        </span>
        <span class="notice-preview-info">
          <el-icon><Timer /></el-icon> {{ previewCreateTime }}
        </span>
      </div>
      <div class="notice-preview-divider"></div>
      <div class="notice-preview-content" v-html="previewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import {
  getMerchantNotice,
  listMerchantNoticeTop,
  markMerchantNoticeRead,
  markMerchantNoticeReadAll
} from '@/api/merchant/notice'
import {
  getMerchantInternalNotice,
  listMerchantInternalNoticeTop,
  markMerchantInternalNoticeRead,
  markMerchantInternalNoticeReadAll
} from '@/api/merchant/internalNotice'
import { NOTICE_TYPE_CODE } from '@/constants/options'

const NOTICE_SOURCE = Object.freeze({ PLATFORM: 'platform', INTERNAL: 'internal' })
const noticePopover = ref(null)
const noticeList = ref([])
const unreadCount = ref(0)
const noticeLoading = ref(false)
const noticeVisible = ref(false)
const noticeLeaveTimer = ref(null)
const previewVisible = ref(false)
const previewTitle = ref('')
const previewContent = ref('')
const previewNoticeType = ref(null)
const previewSourceType = ref(NOTICE_SOURCE.PLATFORM)
const previewPublisher = ref('')
const previewCreateTime = ref('')

async function loadNoticeTop() {
  noticeLoading.value = true
  try {
    const [platformResult, internalResult] = await Promise.allSettled([
      listMerchantNoticeTop(),
      listMerchantInternalNoticeTop()
    ])
    const platformData = platformResult.status === 'fulfilled' ? platformResult.value?.data || {} : {}
    const internalData = internalResult.status === 'fulfilled' ? internalResult.value?.data || {} : {}
    const platformNotices = Array.isArray(platformData.notices)
      ? platformData.notices.map(item => ({ ...item, sourceType: NOTICE_SOURCE.PLATFORM }))
      : []
    const internalNotices = Array.isArray(internalData.notices)
      ? internalData.notices.map(item => ({
        ...item,
        noticeId: item.internalNoticeId,
        sourceType: NOTICE_SOURCE.INTERNAL,
        publisherName: '商户内部'
      }))
      : []
    noticeList.value = [...platformNotices, ...internalNotices]
      .sort((left, right) => String(right.createTime || '').localeCompare(String(left.createTime || '')))
      .slice(0, 20)
    unreadCount.value = Number(platformData.unreadCount || 0) + Number(internalData.unreadCount || 0)
  } finally {
    noticeLoading.value = false
  }
}

onMounted(loadNoticeTop)

function onNoticeEnter() {
  clearTimeout(noticeLeaveTimer.value)
  noticeVisible.value = true
  nextTick(() => {
    const popper = noticePopover.value?.popperRef?.contentRef
    if (popper && !popper._merchantNoticeBound) {
      popper._merchantNoticeBound = true
      popper.addEventListener('mouseenter', () => clearTimeout(noticeLeaveTimer.value))
      popper.addEventListener('mouseleave', () => {
        noticeLeaveTimer.value = setTimeout(() => {
          noticeVisible.value = false
        }, 100)
      })
    }
  })
}

function onNoticeLeave() {
  noticeLeaveTimer.value = setTimeout(() => {
    noticeVisible.value = false
  }, 150)
}

function previewNotice(item) {
  if (!item.isRead) {
    const markPromise = item.sourceType === NOTICE_SOURCE.INTERNAL
      ? markMerchantInternalNoticeRead(item.noticeId)
      : markMerchantNoticeRead(item.noticeId)
    markPromise.catch(() => {})
    const index = noticeList.value.indexOf(item)
    if (index !== -1) {
      noticeList.value[index] = { ...item, isRead: true }
    }
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  const detailPromise = item.sourceType === NOTICE_SOURCE.INTERNAL
    ? getMerchantInternalNotice(item.noticeId)
    : getMerchantNotice(item.noticeId)
  detailPromise
    .then(response => {
      const notice = response.data || {}
      previewTitle.value = notice.noticeTitle || ''
      previewContent.value = notice.noticeContent || ''
      previewNoticeType.value = notice.noticeType
      previewSourceType.value = item.sourceType
      previewPublisher.value = notice.publisherName || '平台'
      previewCreateTime.value = notice.createTime || ''
      previewVisible.value = true
    })
    .catch(error => {
      ElMessage.error(error?.message || '公告详情加载失败')
    })
}

function markAllRead() {
  const platformIds = noticeList.value.filter(item => item.sourceType !== NOTICE_SOURCE.INTERNAL).map(item => item.noticeId)
  const internalIds = noticeList.value.filter(item => item.sourceType === NOTICE_SOURCE.INTERNAL).map(item => item.noticeId)
  if (platformIds.length) markMerchantNoticeReadAll(platformIds.join(',')).catch(() => {})
  if (internalIds.length) markMerchantInternalNoticeReadAll(internalIds.join(',')).catch(() => {})
  noticeList.value = noticeList.value.map(item => ({ ...item, isRead: true }))
  unreadCount.value = 0
}

function noticeTagLabel(item) {
  return item.sourceType === NOTICE_SOURCE.INTERNAL
    ? '内部'
    : item.noticeType === NOTICE_TYPE_CODE.NOTIFICATION ? '通知' : '公告'
}

function noticeTagType(item) {
  return item.sourceType === NOTICE_SOURCE.INTERNAL
    ? 'primary'
    : item.noticeType === NOTICE_TYPE_CODE.NOTIFICATION ? 'warning' : 'success'
}
</script>

<style lang="scss" scoped>
.notice-trigger {
  position: relative;

  .el-icon {
    font-size: 18px;
    vertical-align: -2px;
  }

  .notice-badge {
    position: absolute;
    top: 7px;
    right: -3px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 10px;
    background: #f56c6c;
    color: #fff;
    font-size: 10px;
    line-height: 16px;
    text-align: center;
    white-space: nowrap;
    pointer-events: none;
  }
}

.notice-popover {
  padding: 0 !important;

  .notice-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    border-bottom: 1px solid #eee;
    background: #f7f9fb;
    color: #333;
    font-size: 13px;
    font-weight: 600;
  }

  .notice-mark-all {
    color: var(--el-color-primary);
    font-size: 12px;
    font-weight: normal;
    cursor: pointer;
  }

  .notice-loading,
  .notice-empty {
    padding: 24px;
    color: #bbb;
    font-size: 12px;
    line-height: 1.8;
    text-align: center;
  }

  .notice-empty-icon {
    display: block;
    margin-bottom: 6px;
    font-size: 24px;
  }

  .notice-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background 0.15s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f7f9fb;
    }

    &.is-read {
      .notice-tag,
      .notice-item-title,
      .notice-item-date {
        color: #999;
        opacity: 0.45;
        filter: grayscale(1);
      }
    }
  }

  .notice-tag,
  .notice-item-date {
    flex-shrink: 0;
  }

  .notice-item-title {
    flex: 1;
    overflow: hidden;
    color: #333;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .notice-item-date {
    color: #bbb;
    font-size: 11px;
  }
}
</style>

<style>
.notice-preview-dialog .el-dialog__body {
  padding: 0 10px 10px;
}

.notice-preview-dialog .notice-preview-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  color: #888;
  font-size: 12px;
}

.notice-preview-dialog .notice-preview-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.notice-preview-dialog .notice-preview-divider {
  height: 1px;
  margin-bottom: 16px;
  background: linear-gradient(to right, transparent, #e2e8f0, transparent);
}

.notice-preview-dialog .notice-preview-content {
  color: #2d3748;
  font-size: 14px;
  line-height: 1.85;
  word-break: break-word;
}

.notice-preview-dialog .notice-preview-content img {
  max-width: 100%;
  border-radius: 4px;
}

.notice-preview-dialog .notice-preview-content p {
  margin: 0 0 1em;
}

.notice-preview-dialog .notice-preview-content a {
  color: var(--el-color-primary);
  text-decoration: underline;
}
</style>
