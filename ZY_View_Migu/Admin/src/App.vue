<template>
  <n-config-provider :theme="naiveTheme" :theme-overrides="themeOverrides" :locale="zhCN">
    <n-loading-bar-provider>
      <n-message-provider>
        <n-dialog-provider>
          <n-notification-provider>
            <router-view />
          </n-notification-provider>
        </n-dialog-provider>
      </n-message-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  darkTheme, zhCN,
  NConfigProvider, NDialogProvider, NLoadingBarProvider,
  NMessageProvider, NNotificationProvider,
  type GlobalThemeOverrides
} from 'naive-ui'
import { useThemeStore } from './stores/theme'

const themeStore = useThemeStore()

const naiveTheme = computed(() => themeStore.dark ? darkTheme : null)

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#6366f1',
    primaryColorHover: '#818cf8',
    primaryColorPressed: '#4f46e5',
    primaryColorSuppl: '#6366f1',
    borderRadius: '12px',
    borderRadiusSmall: '8px',
    fontSize: '14px',
    fontSizeSmall: '12px',
    fontSizeLarge: '16px',
    heightSmall: '30px',
    heightMedium: '36px',
    heightLarge: '42px'
  },
  Card: {
    paddingMedium: '20px 24px',
    borderRadius: '14px',
    borderColor: 'oklch(0 0 0 / 0.06)',
    titleFontSize: '15px',
    titleFontWeight: '700'
  },
  Modal: {
    borderRadius: '16px'
  },
  DataTable: {
    tdPadding: '12px 16px',
    thPadding: '10px 16px',
    borderRadius: '12px'
  },
  Menu: {
    itemHeight: '42px',
    borderRadius: '10px',
    itemColorActive: 'rgba(99, 102, 241, 0.12)',
    itemTextColorActive: '#6366f1',
    itemIconColorActive: '#6366f1',
    itemColorActiveHover: 'rgba(99, 102, 241, 0.18)'
  },
  Button: {
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontSizeTiny: '12px',
    fontSizeSmall: '13px',
    fontSizeMedium: '14px',
    paddingTiny: '0 10px',
    paddingSmall: '0 14px',
    paddingMedium: '0 18px'
  },
  Tag: {
    borderRadius: '6px'
  },
  Input: {
    borderRadius: '8px'
  },
  Select: {
    borderRadius: '8px'
  },
  Dialog: {
    borderRadius: '16px'
  },
  Notification: {
    borderRadius: '12px'
  }
}
</script>
