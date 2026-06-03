<template>
  <n-layout has-sider class="shell">
    <n-layout-sider bordered collapse-mode="width" :width="236">
      <div class="brand">ZHANGYUAN CMS</div>
      <n-menu :options="menuOptions" :value="activeKey" @update:value="go" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">
        <div>内容管理后台</div>
        <n-button text @click="logout">退出</n-button>
      </n-layout-header>
      <n-layout-content class="content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NLayout, NLayoutContent, NLayoutHeader, NLayoutSider, NMenu } from 'naive-ui'
import { clearToken } from '../api/http'

const route = useRoute()
const router = useRouter()

const menuOptions = [
  { label: '仪表盘', key: '/' },
  { label: '页面管理', key: '/cms/pages' }
]

const activeKey = computed(() => route.path.startsWith('/cms') ? '/cms/pages' : '/')

function go(path: string) {
  router.push(path)
}

function logout() {
  clearToken()
  router.push('/login')
}
</script>

<style scoped>
.shell {
  height: 100vh;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: white;
}

.content {
  padding: 24px;
}
</style>
