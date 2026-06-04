import { ref, onMounted, onUnmounted } from 'vue'

export function useMouseTrail(options?: { count?: number; gap?: number }) {
  const { count = 8, gap = 0.04 } = options ?? {}

  type Dot = { x: number; y: number; opacity: number; scale: number }
  const trail = ref<Dot[]>([])

  const history: Array<{ x: number; y: number }> = []
  let currentX = 0
  let currentY = 0
  let lastCapture = 0
  let rafId = 0

  function onMouseMove(e: MouseEvent) {
    currentX = e.clientX
    currentY = e.clientY
  }

  function tick() {
    const now = performance.now()
    if (now - lastCapture >= gap * 1000) {
      lastCapture = now
      history.push({ x: currentX, y: currentY })
      if (history.length > count) history.shift()
      trail.value = history.map((p, i) => ({
        x: p.x,
        y: p.y,
        opacity: 1 - (i / (count - 1)) * 0.9,
        scale: 1 - (i / (count - 1)) * 0.7,
      }))
    }
    rafId = requestAnimationFrame(tick)
  }

  onMounted(() => {
    window.addEventListener('mousemove', onMouseMove, { passive: true })
    rafId = requestAnimationFrame(tick)
  })

  onUnmounted(() => {
    window.removeEventListener('mousemove', onMouseMove)
    cancelAnimationFrame(rafId)
  })

  return { trail }
}
