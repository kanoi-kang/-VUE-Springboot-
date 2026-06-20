<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

defineProps<{
  src: string
  alt?: string
  placeholder?: string
}>()

const isLoaded = ref(false)
const imgRef = ref<HTMLImageElement | null>(null)
const observer = ref<IntersectionObserver | null>(null)

const onLoad = () => {
  isLoaded.value = true
}

onMounted(() => {
  if ('IntersectionObserver' in window && imgRef.value) {
    observer.value = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && imgRef.value) {
            const img = imgRef.value
            if (img.dataset.src) {
              img.src = img.dataset.src
            }
            observer.value?.unobserve(img)
          }
        })
      },
      { rootMargin: '50px' }
    )

    observer.value.observe(imgRef.value)
  }
})

onUnmounted(() => {
  observer.value?.disconnect()
})
</script>

<template>
  <div class="lazy-image">
    <img
      ref="imgRef"
      :data-src="src"
      :alt="alt || ''"
      :class="{ loaded: isLoaded }"
      @load="onLoad"
    />
    <div v-if="!isLoaded" class="placeholder">
      <slot name="placeholder">
        <div class="default-placeholder"></div>
      </slot>
    </div>
  </div>
</template>

<style scoped>
.lazy-image {
  position: relative;
  overflow: hidden;
}

.lazy-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.lazy-image img.loaded {
  opacity: 1;
}

.placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.default-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e8e8e8 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
