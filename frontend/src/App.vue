<!-- src/App.vue -->
<template>
  <div id="app">
    <!-- 非登录页显示导航栏 -->
    <AppHeader v-if="$route.path !== '/login'" />
    <!-- 路由内容 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/Layout/AppHeader.vue'
</script>

<style>
/* 这里可以放置一些全局样式 */
html,
body {
  margin: 0;
  padding: 0;
}
#app {
  font-family: Arial, Helvetica, sans-serif;
}
</style>
