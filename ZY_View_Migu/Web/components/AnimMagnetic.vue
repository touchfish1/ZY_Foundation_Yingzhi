<template>
  <component :is="as" ref="elRef" :style="[magneticStyle, { pointerEvents: 'auto' }]">
    <slot />
  </component>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  strength?: number
  radius?: number
  as?: string
}>(), {
  strength: 0.3,
  radius: 200,
  as: 'div'
})

const elRef = ref<HTMLElement | null>(null)
const { style: magneticStyle } = useMagneticHover(elRef, {
  strength: props.strength,
  radius: props.radius
})
</script>
