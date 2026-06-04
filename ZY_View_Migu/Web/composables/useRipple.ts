import { ref, onMounted, onUnmounted, type Ref } from 'vue'

export function useRipple(el: Ref<HTMLElement | null>, options?: { color?: string; duration?: number }) {
  const { duration = 600 } = options || {}
  const ripples = ref<Array<{ id: number; x: number; y: number; size: number }>>([])

  let nextId = 0
  let cleanup: (() => void) | null = null

  function onClick(e: MouseEvent) {
    const target = el.value
    if (!target) return
    const rect = target.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    const size = 20 + Math.random() * 30
    const id = nextId++
    ripples.value.push({ id, x, y, size })
    setTimeout(() => {
      ripples.value = ripples.value.filter(r => r.id !== id)
    }, duration)
  }

  function start() {
    el.value?.addEventListener('click', onClick, { passive: true })
    cleanup = () => {
      el.value?.removeEventListener('click', onClick)
    }
  }

  function stop() {
    cleanup?.()
    cleanup = null
  }

  onMounted(() => start())
  onUnmounted(() => stop())

  return { ripples }
}
