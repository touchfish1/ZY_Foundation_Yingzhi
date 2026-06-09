export default defineNuxtRouteMiddleware((to, from) => {
  const token = import.meta.client
    ? localStorage.getItem('saas_token')
    : useCookie('saas_token').value
  if (!token) {
    const redirect = encodeURIComponent(to.fullPath)
    return navigateTo(`/login?redirect=${redirect}`)
  }
})
