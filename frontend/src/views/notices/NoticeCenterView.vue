<template>
  <div class="notice-center">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 1.2em; font-weight: bold">消息中心</span>
          <div>
            <el-button :icon="Refresh" @click="loadNotices" :loading="loading">刷新</el-button>
            <el-button type="primary" :icon="Check" @click="markAllAsRead" v-if="unreadCount > 0">
              全部标记为已读
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="noticeList.length === 0" class="empty-notice">
        <el-empty description="暂无系统消息" />
      </div>

      <el-timeline v-else style="padding-left: 10px">
        <el-timeline-item
          v-for="notice in noticeList"
          :key="notice.noticeId"
          :timestamp="formatTime(notice.createTime)"
          :type="notice.readStatus === 0 ? 'primary' : 'info'"
          :hollow="notice.readStatus === 1"
        >
          <el-card
            :class="['notice-card', { unread: notice.readStatus === 0 }]"
            @click="viewNoticeDetail(notice)"
          >
            <div style="display: flex; justify-content: space-between; align-items: flex-start">
              <div style="flex: 1">
                <!-- 根据类型显示不同图标 -->
                <el-icon v-if="notice.noticeType === 1" style="color: #e6a23c; margin-right: 8px">
                  <Bell />
                </el-icon>
                <el-icon
                  v-else-if="notice.noticeType === 2"
                  style="color: #67c23a; margin-right: 8px"
                >
                  <MessageBox />
                </el-icon>
                <span style="font-weight: 500">{{ notice.noticeContent }}</span>
              </div>
              <div style="margin-left: 20px; display: flex; align-items: center">
                <!-- 未读小红点 -->
                <el-badge is-dot v-if="notice.readStatus === 0" />
                <el-tag size="small" :type="notice.readStatus === 0 ? 'warning' : 'info'">
                  {{ notice.readStatus === 0 ? '未读' : '已读' }}
                </el-tag>
              </div>
            </div>
            <div style="margin-top: 8px; font-size: 0.9em; color: #888">
              关联预约ID: {{ notice.relatedId }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Bell, MessageBox } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserNotices, markNoticeAsRead, getUnreadCount, type MockNotice } from '@/mockData/mockData.ts'
import dayjs from 'dayjs'

const userStore = useUserStore()
const loading = ref(false)
const noticeList = ref<MockNotice[]>([])

// 计算未读数量
const unreadCount = computed(() => {
  if (!userStore.user) return 0
  return getUnreadCount(userStore.user.userId)
})

const loadNotices = () => {
  if (!userStore.user) return
  loading.value = true
  // 模拟网络请求
  setTimeout(() => {
    noticeList.value = getUserNotices(userStore.user!.userId)
    loading.value = false
  }, 300)
}

const formatTime = (timeStr: string) => {
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm')
}

// 查看通知详情（这里可以跳转到对应预约详情页，目前先简单标记已读）
const viewNoticeDetail = (notice: MockNotice) => {
  if (notice.readStatus === 0) {
    markNoticeAsRead(notice.noticeId)
    notice.readStatus = 1 // 立即更新本地状态
    ElMessage.info('已标记为已读')
  }
  // 实际项目中，这里可以跳转到预约详情页：
  // router.push(`/reservations/detail/${notice.relatedId}`)
}

// 全部标记为已读
const markAllAsRead = () => {
  noticeList.value.forEach((notice) => {
    if (notice.readStatus === 0) {
      markNoticeAsRead(notice.noticeId)
      notice.readStatus = 1
    }
  })
  ElMessage.success('全部标记为已读')
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.notice-center {
  padding: 20px;
}

.notice-card {
  cursor: pointer;
  transition: all 0.2s;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notice-card.unread {
  border-left: 4px solid #409eff;
}

.empty-notice {
  padding: 40px 0;
}
</style>
