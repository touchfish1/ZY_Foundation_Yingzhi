import { ref, watch } from 'vue'

const COLOR_SCHEME_KEY = 'vitepress-theme-appearance'

export function useColorMode() {
  const colorMode = ref<'light' | 'dark'>('light')

  function init() {
    const stored = localStorage.getItem(COLOR_SCHEME_KEY)
    if (stored === 'dark' || stored === 'light') {
      colorMode.value = stored
    } else {
      colorMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    apply()
  }

  function toggle() {
    colorMode.value = colorMode.value === 'dark' ? 'light' : 'dark'
  }

  function apply() {
    const root = document.documentElement
    if (colorMode.value === 'dark') {
      root.classList.add('vitepress-theme-appearance', 'dark')
    } else {
      root.classList.remove('vitepress-theme-appearance', 'dark')
    }
    localStorage.setItem(COLOR_SCHEME_KEY, colorMode.value)
  }

  watch(colorMode, apply)

  if (import.meta.client) {
    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    mql.addEventListener('change', (e) => {
      if (!localStorage.getItem(COLOR_SCHEME_KEY)) {
        colorMode.value = e.matches ? 'dark' : 'light'
      }
    })
  }

  return { colorMode, init, toggle }
}
