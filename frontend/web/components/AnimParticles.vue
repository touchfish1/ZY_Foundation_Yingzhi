<template>
  <canvas ref="canvasRef" class="particle-canvas" aria-hidden="true" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(
  defineProps<{
    particleCount?: number
    connectDistance?: number
    lineOpacity?: number
    particleColor?: string
    lineColor?: string
    speed?: number
    mouseRadius?: number
  }>(),
  {
    particleCount: 60,
    connectDistance: 130,
    lineOpacity: 0.15,
    particleColor: '59, 130, 246',
    lineColor: '59, 130, 246',
    speed: 0.3,
    mouseRadius: 150,
  },
)

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationId = 0
let ctx: CanvasRenderingContext2D | null = null
let particles: Particle[] = []
let mouseX = -9999
let mouseY = -9999
let mouseActive = false
let w = 0
let h = 0

class Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number

  constructor() {
    this.x = Math.random() * w
    this.y = Math.random() * h
    this.vx = (Math.random() - 0.5) * props.speed
    this.vy = (Math.random() - 0.5) * props.speed
    this.size = 1 + Math.random() * 1.5
  }

  update() {
    if (mouseActive) {
      const dx = this.x - mouseX
      const dy = this.y - mouseY
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < props.mouseRadius) {
        const force = (props.mouseRadius - dist) / props.mouseRadius
        this.vx += (dx / dist) * force * 0.5
        this.vy += (dy / dist) * force * 0.5
      }
    }

    this.vx += (Math.random() - 0.5) * 0.05
    this.vy += (Math.random() - 0.5) * 0.05

    const maxSpeed = props.speed * 1.5
    const speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy)
    if (speed > maxSpeed) {
      this.vx = (this.vx / speed) * maxSpeed
      this.vy = (this.vy / speed) * maxSpeed
    }

    this.x += this.vx
    this.y += this.vy

    if (this.x < -20) this.x = w + 20
    if (this.x > w + 20) this.x = -20
    if (this.y < -20) this.y = h + 20
    if (this.y > h + 20) this.y = -20
  }

  draw() {
    if (!ctx) return
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${props.particleColor}, 0.35)`
    ctx.fill()
  }
}

function initParticles() {
  particles = []
  const count = w < 768 ? Math.floor(props.particleCount * 0.3) : props.particleCount
  for (let i = 0; i < count; i++) {
    particles.push(new Particle())
  }
}

function drawLines() {
  if (!ctx) return
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < props.connectDistance) {
        const alpha = (1 - dist / props.connectDistance) * props.lineOpacity
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(${props.lineColor}, ${alpha})`
        ctx.lineWidth = 0.6
        ctx.stroke()
      }
    }
  }
}

function animate() {
  if (!ctx) return
  ctx.clearRect(0, 0, w, h)
  for (const p of particles) {
    p.update()
    p.draw()
  }
  drawLines()
  animationId = requestAnimationFrame(animate)
}

function resize() {
  w = window.innerWidth
  h = window.innerHeight
  if (canvasRef.value) {
    canvasRef.value.width = w
    canvasRef.value.height = h
  }
  initParticles()
}

function onMouseMove(e: MouseEvent) {
  mouseX = e.clientX
  mouseY = e.clientY
  mouseActive = true
}

function onMouseLeave() {
  mouseActive = false
  mouseX = -9999
  mouseY = -9999
}

function onTouchMove(e: TouchEvent) {
  const t = e.touches[0]
  if (t) {
    mouseX = t.clientX
    mouseY = t.clientY
    mouseActive = true
  }
}

function onTouchEnd() {
  mouseActive = false
  mouseX = -9999
  mouseY = -9999
}

let reducedMotion = false

onMounted(() => {
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reducedMotion) return

  ctx = canvasRef.value?.getContext('2d') || null
  if (!ctx) return

  resize()
  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseleave', onMouseLeave)
  window.addEventListener('touchmove', onTouchMove, { passive: true })
  window.addEventListener('touchend', onTouchEnd)
  animate()
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseleave', onMouseLeave)
  window.removeEventListener('touchmove', onTouchMove)
  window.removeEventListener('touchend', onTouchEnd)
})
</script>

<style scoped>
.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}
</style>
