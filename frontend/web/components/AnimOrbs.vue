<template>
  <div class="orb-container" aria-hidden="true">
    <div
      v-for="(orb, i) in orbs"
      :key="i"
      class="orb"
      :class="'orb-' + (i + 1)"
      :style="orb.style"
    />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    count?: number
    colors?: string[]
  }>(),
  {
    count: 4,
    colors: () => ['#6366f1', '#8b5cf6', '#a78bfa', '#c4b5fd'],
  },
)

const orbs = Array.from({ length: props.count }, (_, i) => {
  const size = 200 + Math.floor(Math.random() * 300)
  const color = props.colors[i % props.colors.length]
  const top = Math.floor(Math.random() * 80)
  const left = Math.floor(Math.random() * 80)
  const delay = -(Math.floor(Math.random() * 20))

  return {
    style: {
      width: `${size}px`,
      height: `${size}px`,
      top: `${top}%`,
      left: `${left}%`,
      backgroundColor: color,
      animationDelay: `${delay}s`,
    },
  }
})
</script>

<style scoped>
.orb-container {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  will-change: transform;
  opacity: 0.2;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}

.orb-1 {
  animation-name: float-1, pulse-1;
  animation-duration: 18s, 6s;
}

.orb-2 {
  filter: blur(120px);
  opacity: 0.18;
  animation-name: float-2, pulse-2;
  animation-duration: 22s, 8s;
}

.orb-3 {
  filter: blur(80px);
  opacity: 0.22;
  animation-name: float-3, pulse-3;
  animation-duration: 16s, 7s;
}

.orb-4 {
  filter: blur(110px);
  opacity: 0.16;
  animation-name: float-4, pulse-1;
  animation-duration: 20s, 9s;
}

.orb-5 {
  filter: blur(90px);
  opacity: 0.19;
  animation-name: float-5, pulse-2;
  animation-duration: 24s, 7s;
}

/* Float animations — different paths for each orb */

@keyframes float-1 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(8%, -6%) scale(1.05);
  }
  50% {
    transform: translate(-4%, -12%) scale(0.98);
  }
  75% {
    transform: translate(10%, 4%) scale(1.02);
  }
}

@keyframes float-2 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-10%, 8%) scale(0.95);
  }
  50% {
    transform: translate(6%, -10%) scale(1.04);
  }
  75% {
    transform: translate(-8%, -6%) scale(0.97);
  }
}

@keyframes float-3 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(6%, 10%) scale(1.03);
  }
  50% {
    transform: translate(-8%, -4%) scale(0.96);
  }
  75% {
    transform: translate(-6%, 8%) scale(1.01);
  }
}

@keyframes float-4 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-6%, -8%) scale(1.06);
  }
  50% {
    transform: translate(10%, 6%) scale(0.94);
  }
  75% {
    transform: translate(4%, -10%) scale(1.03);
  }
}

@keyframes float-5 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(10%, -4%) scale(0.97);
  }
  50% {
    transform: translate(-6%, 10%) scale(1.05);
  }
  75% {
    transform: translate(-10%, -8%) scale(0.99);
  }
}

/* Opacity pulsing */

@keyframes pulse-1 {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.28; }
}

@keyframes pulse-2 {
  0%, 100% { opacity: 0.16; }
  50% { opacity: 0.25; }
}

@keyframes pulse-3 {
  0%, 100% { opacity: 0.22; }
  50% { opacity: 0.3; }
}

@media (prefers-reduced-motion: reduce) {
  .orb {
    animation: none;
  }
}
</style>
