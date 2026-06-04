export default defineNuxtRouteMiddleware((to, from) => {
  // Only run on client side
  if (import.meta.client) {
    const token = localStorage.getItem('saas_token')
    if (!token) {
      return navigateTo('/login')
    }
  }
})
