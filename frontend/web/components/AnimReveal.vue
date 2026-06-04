<template>
  <div ref="elRef" :class="['anim-fade-up', animationClass]" :style="{ transitionDelay: `${delay}ms` }">
    <slot />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  animation?: 'fade-up' | 'fade-in' | 'scale-in'
  delay?: number
}>(), {
  animation: 'fade-up',
  delay: 0
})

const elRef = ref<HTMLElement | null>(null)
const { observe } = useScrollReveal()
const animationClass = computed(() => `anim-${props.animation}`)

onMounted(() => {
  if (elRef.value) {
    observe(elRef.value, props.delay)
  }
})
</script>
