<template>
  <div class="profile-page">
    <div class="container">
      <el-row :gutter="20">
        <!-- 左侧用户信息卡片 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="6">
          <el-card class="user-card">
            <div class="user-avatar-section">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
              >
                <el-avatar :size="100" :src="userInfo?.avatar" class="avatar">
                  <span v-if="!userInfo?.avatar" class="avatar-text">
                    {{ userInfo?.name?.charAt(0) || '?' }}
                  </span>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </el-upload>
            </div>
            
            <div class="user-info-section">
              <h2 class="user-name">{{ userInfo?.name }}</h2>
              <p class="user-student-id">
                <el-icon><User /></el-icon>
                {{ userInfo?.studentId }}
              </p>
              <p class="user-phone">
                <el-icon><Phone /></el-icon>
                {{ userInfo?.phone }}
              </p>
              <div class="user-points">
                <el-icon><Medal /></el-icon>
                <span class="points-value">{{ userInfo?.points || 0 }}</span>
                <span class="points-label">积分</span>
              </div>
            </div>
            
            <div class="user-actions">
              <el-button type="primary" @click="$router.push('/my/items')">
                <el-icon><Document /></el-icon>
                我的发布
              </el-button>
              <el-button @click="$router.push('/points')">
                <el-icon><Trophy /></el-icon>
                积分中心
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧编辑表单 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="18">
          <el-card class="edit-card">
            <template #header>
              <div class="card-header">
                <span>个人信息设置</span>
              </div>
            </template>
            
            <el-tabs v-model="activeTab">
              <!-- 基本信息 -->
              <el-tab-pane label="基本信息" name="basic">
                <el-form
                  ref="basicFormRef"
                  :model="basicForm"
                  :rules="basicRules"
                  label-width="100px"
                  class="edit-form"
                >
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="basicForm.name" placeholder="请输入姓名" />
                  </el-form-item>
                  
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="basicForm.phone" placeholder="请输入手机号" />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" :loading="saving" @click="saveBasicInfo">
                      保存修改
                    </el-button>
                    <el-button @click="resetBasicForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
              
              <!-- 修改密码 -->
              <el-tab-pane label="修改密码" name="password">
                <el-form
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  label-width="100px"
                  class="edit-form"
                >
                  <el-form-item label="旧密码" prop="oldPassword">
                    <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      placeholder="请输入旧密码"
                      show-password
                    />
                  </el-form-item>
                  
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      placeholder="请输入新密码（6-20位）"
                      show-password
                    />
                  </el-form-item>
                  
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" :loading="saving" @click="changePassword">
                      修改密码
                    </el-button>
                    <el-button @click="resetPasswordForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import * as userApi from '@/api/user'
import * as fileApi from '@/api/file'
import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { 
  Camera, 
  User, 
  Phone, 
  Medal, 
  Document, 
  Trophy 
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 当前激活的标签页
const activeTab = ref('basic')

// 保存状态
const saving = ref(false)

// 基本信息表单
const basicFormRef = ref<FormInstance>()
const basicForm = reactive({
  name: '',
  phone: ''
})

// 密码表单
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 基本信息验证规则
const basicRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码验证规则
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 初始化表单数据
const initFormData = () => {
  if (userInfo.value) {
    basicForm.name = userInfo.value.name || ''
    basicForm.phone = userInfo.value.phone || ''
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file: UploadRawFile) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB!')
    return false
  }
  return true
}

// 处理头像上传
const handleAvatarUpload = async (options: { file: File }) => {
  try {
    const response = await fileApi.uploadFile(options.file)
    if (response.code === 200 && response.data) {
      // 更新用户头像
      const updateResponse = await userApi.updateProfile({ avatar: response.data.url })
      if (updateResponse.code === 200 && updateResponse.data) {
        userStore.setUserInfo(updateResponse.data)
        ElMessage.success('头像更新成功')
      }
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '头像上传失败')
  }
}

// 保存基本信息
const saveBasicInfo = async () => {
  if (!basicFormRef.value) return
  
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const response = await userApi.updateProfile({
          name: basicForm.name,
          phone: basicForm.phone
        })
        if (response.code === 200 && response.data) {
          userStore.setUserInfo(response.data)
          ElMessage.success('信息更新成功')
        } else {
          ElMessage.error(response.message || '更新失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        saving.value = false
      }
    }
  })
}

// 重置基本信息表单
const resetBasicForm = () => {
  initFormData()
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const response = await userApi.updateProfile({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          resetPasswordForm()
        } else {
          ElMessage.error(response.message || '密码修改失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        saving.value = false
      }
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 20px;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
}

.user-card {
  text-align: center;
  
  .user-avatar-section {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    
    .avatar-uploader {
      cursor: pointer;
      position: relative;
      
      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        
        .avatar-text {
          font-size: 36px;
          color: #fff;
        }
      }
      
      .avatar-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        color: #fff;
        font-size: 12px;
        
        .el-icon {
          font-size: 20px;
          margin-bottom: 4px;
        }
      }
      
      &:hover .avatar-overlay {
        opacity: 1;
      }
    }
  }
  
  .user-info-section {
    margin-bottom: 20px;
    
    .user-name {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 12px 0;
    }
    
    .user-student-id,
    .user-phone {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #606266;
      font-size: 14px;
      margin: 8px 0;
      
      .el-icon {
        color: #909399;
      }
    }
    
    .user-points {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 16px;
      padding: 12px;
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
      border-radius: 8px;
      
      .el-icon {
        font-size: 20px;
        color: #e6a23c;
      }
      
      .points-value {
        font-size: 24px;
        font-weight: 700;
        color: #e6a23c;
      }
      
      .points-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .user-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .el-button {
      width: 100%;
    }
  }
}

.edit-card {
  .card-header {
    font-size: 16px;
    font-weight: 600;
  }
  
  .edit-form {
    max-width: 500px;
    padding: 20px 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }
  
  .user-card {
    margin-bottom: 20px;
  }
  
  .edit-form {
    padding: 10px 0;
  }
}
</style>
