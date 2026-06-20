<script setup lang="ts">
import { ref, onMounted } from 'vue'

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const captchaSrc = ref('')

const refreshCaptcha = () => {
  captchaSrc.value = `/api/admin/auth/captcha?${Date.now()}`
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<template>
  <div class="captcha-wrapper">
    <img
      :src="captchaSrc"
      alt="验证码"
      class="captcha-image"
      @click="refreshCaptcha"
    />
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
}

.captcha-input {
  width: 100px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.captcha-input:focus {
  outline: none;
  border-color: #409eff;
}
</style>
