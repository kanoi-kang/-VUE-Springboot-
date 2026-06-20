<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCaptcha } from '@/api/auth'

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const captchaUrl = ref('')

const refreshCaptcha = async () => {
  try {
    const blob = await getCaptcha()
    const url = URL.createObjectURL(blob)
    if (captchaUrl.value) {
      URL.revokeObjectURL(captchaUrl.value)
    }
    captchaUrl.value = url
  } catch (error) {
    console.error('Failed to load captcha:', error)
  }
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

onMounted(() => {
  refreshCaptcha()
})

defineExpose({
  refreshCaptcha
})
</script>

<template>
  <div class="captcha-wrapper">
    <img
      v-if="captchaUrl"
      :src="captchaUrl"
      alt="验证码"
      class="captcha-image"
      @click="refreshCaptcha"
    />
    <div v-else class="captcha-placeholder" @click="refreshCaptcha">加载中...</div>
    <input
      :value="modelValue"
      type="text"
      class="captcha-input"
      placeholder="请输入验证码"
      maxlength="4"
      @input="handleInput"
    />
  </div>
</template>

<style scoped>
.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-image {
  width: 100px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  object-fit: fill;
}

.captcha-placeholder {
  width: 100px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
}

.captcha-input {
  flex: 1;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.captcha-input:focus {
  outline: none;
  border-color: #E67E22;
}
</style>
