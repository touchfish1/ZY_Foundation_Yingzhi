import { ref, computed, onMounted, onUnmounted, type Ref } from 'vue'

export function useMagneticHover(
  elRef: Ref<HTMLElement | null>,
  options?: { strength?: number; radius?: number }
) {
  const { strength = 0.3, radius = 200 } = options ?? {}
  const x = ref(0)
  const y = ref(0)

  function onMouseMove(e: MouseEvent) {
    const el = elRef.value
    if (!el) return
    const rect = el.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const dx = e.clientX - cx
    const dy = e.clientY - cy
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist > radius) {
      x.value = 0
      y.value = 0
      return
    }
    x.value = dx * strength
    y.value = dy * strength
  }

  function onMouseLeave() {
    x.value = 0
    y.value = 0
  }

  onMounted(() => {
    const el = elRef.value
    if (!el) return
    el.addEventListener('mousemove', onMouseMove, { passive: true })
    el.addEventListener('mouseleave', onMouseLeave, { passive: true })
  })

  onUnmounted(() => {
    const el = elRef.value
    if (!el) return
    el.removeEventListener('mousemove', onMouseMove)
    el.removeEventListener('mouseleave', onMouseLeave)
  })

  const style = computed(() => ({
    transform: `translate(${x.value}px, ${y.value}px)`,
    transition: 'transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
  }))

  return { style }
}
