<template>
  <div class="auth-page">
    <div class="hero-panel">
      <p class="eyebrow">校园失物招领 · 新用户注册</p>
      <h1>加入我们，一起让物品回家</h1>
      <p class="subtitle">
        注册后即可发布失物/招领、查看匹配消息、管理个人资料。还没准备好？可以先体验游客模式。
      </p>
      <div class="hero-highlights">
        <div class="highlight">
          <el-icon><Compass /></el-icon>
          精准匹配推荐
        </div>
        <div class="highlight">
          <el-icon><Location /></el-icon>
          地图定位发布
        </div>
        <div class="highlight">
          <el-icon><Tickets /></el-icon>
          积分激励与排行
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-header">
        <h2>创建账户</h2>
        <p>填写基本信息即可完成注册</p>
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
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入姓名"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <div class="form-inline">
          <router-link class="link" to="/login">已有账号？去登录</router-link>
          <el-button type="primary" text @click="handleGuestLogin">
            先体验游客模式
          </el-button>
        </div>

        <el-button
          type="primary"
          size="large"
          class="full-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Compass, Location, Tickets } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  studentId: '',
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

// 自定义验证：确认密码
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 5, max: 20, message: '学号/工号长度为5-20位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch {
    return
  }
  
  loading.value = true
  try {
    await userStore.register({
      studentId: form.studentId,
      name: form.name,
      phone: form.phone,
      password: form.password
    })
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    const message = error?.response?.data?.message || error?.message || '注册失败，请稍后重试'
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

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 20px;
  }
}
</style>
