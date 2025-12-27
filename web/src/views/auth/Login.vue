<template>
  <div class="login-page flex-center">
    <el-card class="login-card">
      <h2>登录</h2>
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
        <el-form-item>
          <el-checkbox v-model="form.rememberMe">记住学号</el-checkbox>
        </el-form-item>
        <el-button 
          type="primary" 
          style="width: 100%" 
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
        <div class="login-footer">
          <router-link to="/register">没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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

// 加载保存的学号
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
    
    // 处理记住学号
    if (form.rememberMe) {
      localStorage.setItem('saved_student_id', form.studentId)
    } else {
      localStorage.removeItem('saved_student_id')
    }
    
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    // API拦截器已经显示错误消息，这里不需要重复显示
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  max-width: 90%;

  h2 {
    text-align: center;
    margin-bottom: 24px;
    color: #303133;
  }

  .login-footer {
    text-align: center;
    margin-top: 16px;
  }
}
</style>
