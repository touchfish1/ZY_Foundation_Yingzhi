import type { App } from 'vue'
import { usePermissionStore } from '../stores/permission'

export function setupPermissionDirective(app: App) {
  app.directive('permission', {
    mounted(el: HTMLElement, binding) {
      const store = usePermissionStore()
      const value = binding.value

      const hasAccess = Array.isArray(value)
        ? store.hasAnyPermission(value)
        : store.hasPermission(value)

      if (!hasAccess) {
        el.parentNode?.removeChild(el)
      }
    }
  })
}
