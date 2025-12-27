<template>
  <div class="register-page flex-center">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-position="top"
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
        <el-button 
          type="primary" 
          style="width: 100%" 
          :loading="loading"
          :disabled="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
        <div class="register-footer">
          <router-link to="/login">已有账号？立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

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
    // API拦截器已经显示错误消息，这里不需要重复显示
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
  max-width: 90%;

  h2 {
    text-align: center;
    margin-bottom: 24px;
    color: #303133;
  }

  .register-footer {
    text-align: center;
    margin-top: 16px;
  }
}
</style>
