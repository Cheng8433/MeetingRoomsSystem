<template>
  <el-header class="app-header">
    <div class="header-left">
      <span class="logo" @click="$router.push('/rooms')" style="cursor: pointer"
        >会议室预约系统</span
      >
      <el-menu mode="horizontal" :default-active="$route.path" router>
        <!-- 所有登录用户都能看到 -->
        <el-menu-item index="/rooms">会议室管理</el-menu-item>
        <el-menu-item index="/my-reservations">我的预约</el-menu-item>

        <!-- 仅管理员可见 -->
        <el-menu-item v-if="userStore.isAdmin" index="/admin/approvals">审批管理</el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/rooms/week-view">周视图</el-menu-item>
        <!-- 未来可以在此添加：数据统计等 -->
      </el-menu>
    </div>
    <div class="header-right">
      <el-avatar :size="32" style="margin-right: 10px; background-color: #409eff">
        {{ userStore.user?.realName?.charAt(0) || userStore.user?.username?.charAt(0) }}
      </el-avatar>
      <!-- 新增：通知图标（带未读小红点） -->
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
        <el-button link :icon="Bell" @click="$router.push('/notices')" style="margin-right: 15px" />
      </el-badge>

      <span>欢迎，{{ userStore.user?.realName || userStore.user?.username }}</span>
      <el-divider direction="vertical" />
      <el-button type="text" @click="handleLogout">退出登录</el-button>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
import { getUnreadCount } from '@/mockData/mockData.ts'
import { computed } from 'vue'
const unreadCount = computed(() => {
  if (!userStore.user) return 0
  return getUnreadCount(userStore.user.userId)
})
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.header-left {
  display: flex;
  align-items: center;
}
.logo {
  font-size: 1.2em;
  font-weight: bold;
  margin-right: 30px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>
