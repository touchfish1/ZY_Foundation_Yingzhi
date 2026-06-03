<template>
  <section class="image-banner">
    <div class="banner-inner" :style="bannerStyle">
      <div class="overlay" v-if="props.title || props.subtitle">
        <h2 v-if="props.title">{{ props.title }}</h2>
        <p v-if="props.subtitle">{{ props.subtitle }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

// 图片横幅展示区块组件
const props = defineProps<{ props: Record<string, unknown> }>()

// 计算横幅背景样式（图片 URL、尺寸、位置）
const bannerStyle = computed(() => {
  const imageUrl = props.props.imageUrl || props.props.image || ''
  return {
    backgroundImage: `url(${imageUrl})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    minHeight: props.props.height || '400px'
  }
})

onMounted(() => {
  console.log('[Block] ImageBannerBlock mounted')
})
</script>

<style scoped>
.image-banner {
  width: 100%;
  margin: 0 auto 96px;
  border-radius: 0.75rem;
  overflow: hidden;
}

.banner-inner {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay {
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  padding: 48px 40px;
  text-align: center;
  width: 100%;
}

.overlay h2 {
  font-size: 32px;
  margin: 0 0 12px;
}

.overlay p {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
}
</style>
