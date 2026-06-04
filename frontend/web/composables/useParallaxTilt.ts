import { ref, computed, onMounted, onUnmounted, type Ref } from 'vue'

export function useParallaxTilt(
  elRef: Ref<HTMLElement | null>,
  options?: {
    maxTilt?: number
    perspective?: number
    scale?: number
    glare?: boolean
  }
) {
  const maxTilt = options?.maxTilt ?? 8
  const perspective = options?.perspective ?? 1000
  const scale = options?.scale ?? 1.02
  const glare = options?.glare ?? true

  const xPercent = ref(0.5)
  const yPercent = ref(0.5)
  const isHovering = ref(false)

  function onMouseMove(e: MouseEvent) {
    const el = elRef.value
    if (!el) return
    const rect = el.getBoundingClientRect()
    xPercent.value = (e.clientX - rect.left) / rect.width
    yPercent.value = (e.clientY - rect.top) / rect.height
    isHovering.value = true
  }

  function onMouseLeave() {
    isHovering.value = false
  }

  onMounted(() => {
    const el = elRef.value
    if (!el) return
    el.addEventListener('mousemove', onMouseMove)
    el.addEventListener('mouseleave', onMouseLeave)
  })

  onUnmounted(() => {
    const el = elRef.value
    if (!el) return
    el.removeEventListener('mousemove', onMouseMove)
    el.removeEventListener('mouseleave', onMouseLeave)
  })

  const style = computed(() => {
    if (!isHovering.value) {
      return {
        transform: `perspective(${perspective}px) rotateX(0deg) rotateY(0deg) scale3d(1,1,1)`,
        transition: 'transform 0.3s ease-out'
      }
    }
    const rotateX = (yPercent.value - 0.5) * 2 * maxTilt
    const rotateY = (xPercent.value - 0.5) * 2 * maxTilt
    return {
      transform: `perspective(${perspective}px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(${scale},${scale},${scale})`,
      transition: 'transform 0.1s ease-out'
    }
  })

  const glareStyle = computed(() => {
    if (!isHovering.value || !glare) {
      return { background: 'transparent' }
    }
    const glareX = (1 - xPercent.value) * 100
    const glareY = yPercent.value * 100
    return {
      background: `radial-gradient(circle at ${glareX}% ${glareY}%, rgba(255,255,255,0.2) 0%, transparent 60%)`
    }
  })

  return { style, glareStyle }
}
