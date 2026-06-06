export default defineNuxtRouteMiddleware((to, from) => {
  if (import.meta.client) {
    const token = localStorage.getItem('saas_token')
    if (!token) {
      return navigateTo('/login')
    }
  } else {
    const token = useCookie('saas_token').value
    if (!token) {
      return navigateTo('/login')
    }
  }
})
