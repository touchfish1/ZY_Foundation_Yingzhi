<template>
  <div class="waves-container" aria-hidden="true">
    <svg class="waves" viewBox="0 0 1440 180" preserveAspectRatio="none">
      <path
        class="wave wave-1"
        d="M0,90 C360,140 720,20 1080,70 C1260,95 1350,80 1440,85 L1440,180 L0,180 Z"
      />
      <path
        class="wave wave-2"
        d="M0,110 C240,50 600,150 960,80 C1200,40 1320,60 1440,100 L1440,180 L0,180 Z"
      />
      <path
        class="wave wave-3"
        d="M0,130 C180,80 480,160 840,110 C1140,70 1320,120 1440,130 L1440,180 L0,180 Z"
      />
    </svg>
  </div>
</template>

<style scoped>
.waves-container {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 180px;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.waves {
  width: 100%;
  height: 100%;
}

.wave {
  fill: rgba(200, 190, 178, 0.08);
  animation: wave-drift 12s ease-in-out infinite;
}

.wave-1 {
  animation-duration: 14s;
  animation-delay: -2s;
}

.wave-2 {
  fill: rgba(200, 190, 178, 0.05);
  animation-duration: 18s;
  animation-delay: -5s;
}

.wave-3 {
  fill: rgba(200, 190, 178, 0.03);
  animation-duration: 22s;
  animation-delay: -8s;
}

@keyframes wave-drift {
  0%, 100% { d: path("M0,90 C360,140 720,20 1080,70 C1260,95 1350,80 1440,85 L1440,180 L0,180 Z"); }
  33% { d: path("M0,100 C360,50 720,130 1080,60 C1260,40 1350,100 1440,95 L1440,180 L0,180 Z"); }
  66% { d: path("M0,80 C360,110 720,40 1080,100 C1260,130 1350,70 1440,80 L1440,180 L0,180 Z"); }
  100% { d: path("M0,90 C360,140 720,20 1080,70 C1260,95 1350,80 1440,85 L1440,180 L0,180 Z"); }
}
</style>
