<template>
  <div class="auth-page">
    <div class="hero-panel">
      <p class="eyebrow">校园失物招领 · 智能匹配</p>
      <h1>欢迎回来，继续帮物品找到主人</h1>
      <p class="subtitle">
        登录后可发布信息、接收匹配通知、查看我的发布和消息。还没有账号？可以先注册或使用游客模式体验。
      </p>
      <div class="hero-highlights">
        <div class="highlight">
          <el-icon><Bell /></el-icon>
          实时消息提醒
        </div>
        <div class="highlight">
          <el-icon><Location /></el-icon>
          地图定位发布
        </div>
        <div class="highlight">
          <el-icon><Compass /></el-icon>
          智能匹配推荐
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-header">
        <h2>登录账号</h2>
        <p>使用学号/工号即可登录；也可先体验游客模式</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form"
      >
        <el-form-item label="学号/工号" prop="studentId">
          <el-input
            v-model="form.studentId"
            placeholder="请输入学号/工号"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-inline">
          <el-checkbox v-model="form.rememberMe">记住学号</el-checkbox>
          <router-link class="link" to="/register">没有账号？去注册</router-link>
        </div>

        <el-button
          type="primary"
          size="large"
          class="full-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          登录
        </el-button>

        <el-button
          class="full-btn ghost"
          size="large"
          @click="handleGuestLogin"
        >
          体验游客模式
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Bell, Location, Compass } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  studentId: '',
  password: '',
  rememberMe: false,
})

const rules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const redirectPath = computed(() => {
  return (route.query.redirect as string) || '/'
})

onMounted(() => {
  const savedStudentId = localStorage.getItem('saved_student_id')
  if (savedStudentId) {
    form.studentId = savedStudentId
    form.rememberMe = true
  }
})

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch {
    return
  }
  
  loading.value = true
  try {
    await userStore.login({
      studentId: form.studentId,
      password: form.password
    })
    
    if (form.rememberMe) {
      localStorage.setItem('saved_student_id', form.studentId)
    } else {
      localStorage.removeItem('saved_student_id')
    }
    
    ElMessage.success('登录成功')
    router.push(redirectPath.value)
  } catch (error: any) {
    const message = error?.response?.data?.message || error?.message || '登录失败，请检查账号或密码'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = () => {
  userStore.loginAsGuest()
  ElMessage.success('已进入游客模式，部分功能需正式登录')
  router.push('/')
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  padding: 40px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f9fbff 100%);
}

.hero-panel {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eyebrow {
  color: #409eff;
  font-weight: 600;
  margin: 0;
}

.subtitle {
  margin: 0;
  color: #606266;
}

.hero-highlights {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.highlight {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 10px 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.form-panel {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-header h2 {
  margin: 0 0 4px;
}

.form-header p {
  margin: 0;
  color: #909399;
}

.auth-form {
  margin-top: 4px;
}

.form-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.link {
  color: #409eff;
}

.full-btn {
  width: 100%;
  margin-top: 8px;
}

.ghost {
  background: #f5f7fa;
  border-color: #dcdfe6;
  color: #303133;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 20px;
  }
}
</style>
