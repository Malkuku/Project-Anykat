<template>
  <div class="error-message">
    <p>{{ displayMessage }}</p>
    <p>将在 {{ countdown }} 秒后返回...</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  redirectTo: {
    type: String,
    default: '/'
  },
  delay: {
    type: Number,
    default: 5
  },
  errorMessage: {
    type: String,
    default: '抱歉，发生了一些错误。'
  }
})

const route = useRoute()
const router = useRouter()
const countdown = ref(props.delay)
let timer = null

// 优先使用路由参数中的错误信息
const displayMessage = computed(() => {
  return route.query.errorMessage || props.errorMessage
})

const startCountdown = () => {
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      redirect()
    }
  }, 1000)
}

const redirect = () => {
  router.push(route.query.redirectTo || props.redirectTo)
}

onMounted(() => {
  startCountdown()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.error-message {
  padding: 20px;
  background-color: #fff8f8;
  border: 1px solid #ffcccc;
  border-radius: 4px;
  color: #ff4444;
  text-align: center;
}
</style>