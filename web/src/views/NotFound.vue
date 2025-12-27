<template>
  <div class="not-found flex-center">
    <div class="text-center">
      <h1 style="font-size: 72px; margin-bottom: 16px">404</h1>
      <p style="font-size: 20px; margin-bottom: 24px">Page Not Found</p>
      <el-button type="primary" @click="goHome">Go Home</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()

const goHome = () => {
  router.push({ name: 'Home' })
}
</script>

<style scoped lang="scss">
.not-found {
  min-height: 100vh;
}
</style>
