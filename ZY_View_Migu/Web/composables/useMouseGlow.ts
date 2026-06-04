export function useMouseGlow() {
  const x = ref(0)
  const y = ref(0)
  const isVisible = ref(false)

  function onMouseMove(e: MouseEvent) {
    x.value = e.clientX
    y.value = e.clientY
    if (!isVisible.value) isVisible.value = true
  }

  function onMouseLeave() {
    isVisible.value = false
  }

  let cleanup: (() => void) | null = null

  function start() {
    window.addEventListener('mousemove', onMouseMove, { passive: true })
    window.addEventListener('mouseleave', onMouseLeave, { passive: true })
    cleanup = () => {
      window.removeEventListener('mousemove', onMouseMove)
      window.removeEventListener('mouseleave', onMouseLeave)
    }
  }

  function stop() {
    cleanup?.()
    cleanup = null
  }

  onMounted(() => start())
  onUnmounted(() => stop())

  return { x, y, isVisible }
}
