import { ref, computed } from 'vue'

export interface SaasUser {
  id: number
  email: string
  nickname: string
  apiKey: string
  quotaUsed: number
  quotaLimit: number
  status: string
}

export const useSaasAuth = () => {
  const user = ref<SaasUser | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)

  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  // Initialize from localStorage
  if (import.meta.client) {
    const savedToken = localStorage.getItem('saas_token')
    const savedUser = localStorage.getItem('saas_user')
    if (savedToken) token.value = savedToken
    if (savedUser) {
      try { user.value = JSON.parse(savedUser) } catch { /* ignore corrupt data */ }
    }
  }

  async function register(email: string, password: string, nickname?: string) {
    loading.value = true
    try {
      const res = await $fetch(`${apiBase}/api/auth/register`, {
        method: 'POST',
        body: { email, password, nickname }
      }) as any
      token.value = res.token
      user.value = res.user
      if (import.meta.client) {
        localStorage.setItem('saas_token', res.token)
        localStorage.setItem('saas_user', JSON.stringify(res.user))
      }
      return res
    } finally {
      loading.value = false
    }
  }

  async function login(email: string, password: string) {
    loading.value = true
    try {
      const res = await $fetch(`${apiBase}/api/auth/login`, {
        method: 'POST',
        body: { email, password }
      }) as any
      token.value = res.token
      user.value = res.user
      if (import.meta.client) {
        localStorage.setItem('saas_token', res.token)
        localStorage.setItem('saas_user', JSON.stringify(res.user))
      }
      return res
    } finally {
      loading.value = false
    }
  }

  async function fetchProfile() {
    if (!token.value) return
    try {
      const res = await $fetch(`${apiBase}/api/auth/profile`, {
        headers: { Authorization: `Bearer ${token.value}` }
      }) as any
      user.value = res
      if (import.meta.client) {
        localStorage.setItem('saas_user', JSON.stringify(res))
      }
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    if (import.meta.client) {
      localStorage.removeItem('saas_token')
      localStorage.removeItem('saas_user')
    }
    navigateTo('/login')
  }

  const isLoggedIn = computed(() => !!token.value)

  return { user, token, loading, isLoggedIn, register, login, logout, fetchProfile }
}
