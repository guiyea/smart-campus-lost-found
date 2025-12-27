<template>
  <div class="item-publish">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>{{ isEdit ? '编辑信息' : '发布信息' }}</h1>
        <p class="subtitle">{{ isEdit ? '修改您发布的失物/招领信息' : '发布失物或招领信息，帮助找回物品' }}</p>
      </div>

      <!-- 发布表单 -->
      <el-card class="publish-card">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          size="large"
        >
          <!-- 信息类型选择 -->
          <el-form-item label="信息类型" prop="type">
            <el-radio-group v-model="formData.type" class="type-radio-group">
              <el-radio-button :value="0">
                <el-icon><Search /></el-icon>
                失物寻找
              </el-radio-button>
              <el-radio-button :value="1">
                <el-icon><Present /></el-icon>
                招领启事
              </el-radio-button>
            </el-radio-group>
            <div class="type-hint">
              {{ formData.type === 0 ? '我丢失了物品，希望找回' : '我捡到了物品，寻找失主' }}
            </div>
          </el-form-item>

          <!-- 标题 -->
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="formData.title"
              placeholder="请输入标题，如：丢失黑色钱包一个"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <!-- 描述 -->
          <el-form-item label="详细描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="5"
              placeholder="请详细描述物品特征，如颜色、品牌、型号、内含物品等，描述越详细越容易找回"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <!-- 物品类别 -->
          <el-form-item label="物品类别" prop="category">
            <el-select
              v-model="formData.category"
              placeholder="请选择物品类别"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="cat in categories"
                :key="cat.value"
                :label="cat.label"
                :value="cat.value"
              >
                <span>{{ cat.label }}</span>
                <span v-if="cat.value === aiCategory" class="ai-tag">AI推荐</span>
              </el-option>
            </el-select>
            <div v-if="aiCategory && aiCategory !== formData.category" class="ai-hint">
              <el-icon><MagicStick /></el-icon>
              AI识别建议：{{ getCategoryLabel(aiCategory) }}
              <el-button type="primary" link size="small" @click="formData.category = aiCategory">
                使用此类别
              </el-button>
            </div>
          </el-form-item>

          <!-- 图片上传 -->
          <el-form-item label="物品图片" prop="images">
            <el-upload
              v-model:file-list="fileList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="9"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :on-exceed="handleExceed"
              :before-upload="beforeUpload"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="upload-tip">
                  支持 jpg、png、gif 格式，单张不超过 10MB，最多上传 9 张
                </div>
              </template>
            </el-upload>
            <div v-if="recognizing" class="recognizing-hint">
              <el-icon class="is-loading"><Loading /></el-icon>
              正在识别图片内容...
            </div>
          </el-form-item>

          <!-- 时间选择 -->
          <el-form-item :label="formData.type === 0 ? '丢失时间' : '拾获时间'" prop="eventTime">
            <el-date-picker
              v-model="formData.eventTime"
              type="datetime"
              :placeholder="formData.type === 0 ? '请选择丢失时间' : '请选择拾获时间'"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 地点选择 -->
          <el-form-item :label="formData.type === 0 ? '丢失地点' : '拾获地点'" prop="locationDesc">
            <el-input
              v-model="formData.locationDesc"
              placeholder="请输入地点描述，如：图书馆三楼阅览室"
              maxlength="100"
            >
              <template #append>
                <el-button @click="showMapPicker = true">
                  <el-icon><Location /></el-icon>
                  地图选点
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- 地图选点对话框 -->
          <el-dialog
            v-model="showMapPicker"
            title="选择位置"
            width="700px"
            :close-on-click-modal="false"
          >
            <LocationPicker
              v-model="locationData"
              :default-center="{ lng: 116.397428, lat: 39.90923 }"
            />
            <template #footer>
              <el-button @click="showMapPicker = false">取消</el-button>
              <el-button type="primary" @click="confirmLocation">确认位置</el-button>
            </template>
          </el-dialog>

          <!-- 已选位置显示 -->
          <div v-if="formData.longitude && formData.latitude" class="selected-location">
            <el-tag type="success" closable @close="clearLocationData">
              <el-icon><Location /></el-icon>
              已选择位置：{{ formData.locationDesc }}
            </el-tag>
          </div>

          <!-- 提交按钮 -->
          <el-form-item class="submit-buttons">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ isEdit ? '保存修改' : '立即发布' }}
            </el-button>
            <el-button size="large" @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import { Search, Present, Plus, Location, MagicStick, Loading } from '@element-plus/icons-vue'
import LocationPicker from '@/components/map/LocationPicker.vue'
import { publishItem, updateItem, getItemDetail } from '@/api/item'
import { uploadFile, recognizeImage } from '@/api/file'
import type { ItemDTO } from '@/types'

const router = useRouter()
const route = useRoute()

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const itemId = computed(() => route.params.id ? Number(route.params.id) : null)

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<ItemDTO>({
  title: '',
  description: '',
  type: 0,
  category: '',
  images: [],
  longitude: undefined,
  latitude: undefined,
  locationDesc: '',
  eventTime: '',
})

// 文件列表
const fileList = ref<UploadFile[]>([])

// 地图选点相关
const showMapPicker = ref(false)
const locationData = ref<{ longitude: number; latitude: number; address: string } | null>(null)

// AI识别相关
const aiCategory = ref('')
const recognizing = ref(false)

// 提交状态
const submitting = ref(false)

// 物品类别选项
const categories = [
  { value: '电子设备', label: '电子设备' },
  { value: '证件卡片', label: '证件卡片' },
  { value: '钥匙', label: '钥匙' },
  { value: '钱包', label: '钱包' },
  { value: '书籍文具', label: '书籍文具' },
  { value: '衣物配饰', label: '衣物配饰' },
  { value: '运动器材', label: '运动器材' },
  { value: '其他', label: '其他' },
]

// 表单验证规则
const formRules: FormRules = {
  type: [
    { required: true, message: '请选择信息类型', trigger: 'change' },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在 2-50 个字符之间', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度应在 10-1000 个字符之间', trigger: 'blur' },
  ],
  eventTime: [
    { required: true, message: '请选择时间', trigger: 'change' },
  ],
  locationDesc: [
    { required: true, message: '请输入地点描述', trigger: 'blur' },
  ],
}

// 获取类别标签
const getCategoryLabel = (value: string) => {
  const cat = categories.find(c => c.value === value)
  return cat ? cat.label : value
}

// 禁用未来日期
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 文件变化处理
const handleFileChange = async (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  // 如果是第一张图片，触发AI识别
  if (uploadFiles.length === 1 && uploadFile.raw) {
    await recognizeImageFromFile(uploadFile.raw)
  }
}

// 文件移除处理
const handleFileRemove = (_uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  // 如果移除了所有图片，清除AI识别结果
  if (uploadFiles.length === 0) {
    aiCategory.value = ''
  }
}

// 超出限制处理
const handleExceed = () => {
  ElMessage.warning('最多只能上传 9 张图片')
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

// AI图像识别
const recognizeImageFromFile = async (file: File) => {
  recognizing.value = true
  try {
    // 先上传图片获取URL
    const uploadRes = await uploadFile(file)
    if (uploadRes.code === 200 && uploadRes.data) {
      const imageUrl = uploadRes.data.url
      
      // 调用AI识别接口
      try {
        const recognizeRes = await recognizeImage(imageUrl)
        if (recognizeRes.code === 200 && recognizeRes.data) {
          aiCategory.value = recognizeRes.data.category
          recognizing.value = false
          return
        }
      } catch (aiError) {
        console.warn('AI识别服务暂不可用，使用本地模拟:', aiError)
      }
      
      // AI服务不可用时，使用本地模拟识别
      simulateRecognition(file.name)
    }
  } catch (error) {
    console.error('图片识别失败:', error)
    recognizing.value = false
  }
}

// 模拟AI识别（当后端服务不可用时的降级方案）
const simulateRecognition = (fileName: string) => {
  setTimeout(() => {
    const lowerName = fileName.toLowerCase()
    if (lowerName.includes('phone') || lowerName.includes('手机') || lowerName.includes('电脑') || lowerName.includes('laptop')) {
      aiCategory.value = '电子设备'
    } else if (lowerName.includes('card') || lowerName.includes('证') || lowerName.includes('卡') || lowerName.includes('id')) {
      aiCategory.value = '证件卡片'
    } else if (lowerName.includes('key') || lowerName.includes('钥匙')) {
      aiCategory.value = '钥匙'
    } else if (lowerName.includes('wallet') || lowerName.includes('钱包')) {
      aiCategory.value = '钱包'
    } else if (lowerName.includes('book') || lowerName.includes('书') || lowerName.includes('pen') || lowerName.includes('笔')) {
      aiCategory.value = '书籍文具'
    } else if (lowerName.includes('cloth') || lowerName.includes('衣') || lowerName.includes('帽') || lowerName.includes('鞋')) {
      aiCategory.value = '衣物配饰'
    } else if (lowerName.includes('ball') || lowerName.includes('球') || lowerName.includes('sport')) {
      aiCategory.value = '运动器材'
    } else {
      aiCategory.value = '其他'
    }
    recognizing.value = false
  }, 1000)
}

// 确认地图选点
const confirmLocation = () => {
  if (locationData.value) {
    formData.longitude = locationData.value.longitude
    formData.latitude = locationData.value.latitude
    formData.locationDesc = locationData.value.address
  }
  showMapPicker.value = false
}

// 清除位置数据
const clearLocationData = () => {
  formData.longitude = undefined
  formData.latitude = undefined
  locationData.value = null
}

// 上传所有图片并获取URL列表
const uploadAllImages = async (): Promise<string[]> => {
  const urls: string[] = []
  
  for (const file of fileList.value) {
    if (file.raw) {
      // 新上传的文件
      try {
        const res = await uploadFile(file.raw)
        if (res.code === 200 && res.data) {
          urls.push(res.data.url)
        }
      } catch (error) {
        console.error('文件上传失败:', error)
        throw new Error('图片上传失败，请重试')
      }
    } else if (file.url) {
      // 已有的图片URL（编辑模式）
      urls.push(file.url)
    }
  }
  
  return urls
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    // 验证图片
    if (fileList.value.length === 0) {
      ElMessage.warning('请至少上传一张物品图片')
      return
    }

    submitting.value = true

    // 上传图片
    const imageUrls = await uploadAllImages()
    formData.images = imageUrls

    // 提交数据
    let res
    if (isEdit.value && itemId.value) {
      res = await updateItem(itemId.value, formData)
    } else {
      res = await publishItem(formData)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      // 跳转到详情页
      if (res.data?.id) {
        router.push({ name: 'ItemDetail', params: { id: res.data.id } })
      } else {
        router.push({ name: 'Home' })
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== false) {
      // 非表单验证错误
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 加载编辑数据
const loadItemData = async () => {
  if (!isEdit.value || !itemId.value) return

  try {
    const res = await getItemDetail(itemId.value)
    if (res.code === 200 && res.data) {
      const item = res.data
      formData.title = item.title
      formData.description = item.description
      formData.type = item.type
      formData.category = item.category
      formData.longitude = item.longitude
      formData.latitude = item.latitude
      formData.locationDesc = item.locationDesc
      formData.eventTime = item.eventTime

      // 设置图片列表
      if (item.images && item.images.length > 0) {
        fileList.value = item.images.map((url, index) => ({
          name: `image-${index}`,
          url: url,
          uid: index,
        })) as UploadFile[]
      }

      // 设置位置数据
      if (item.longitude && item.latitude) {
        locationData.value = {
          longitude: item.longitude,
          latitude: item.latitude,
          address: item.locationDesc,
        }
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 监听位置数据变化
watch(locationData, (newVal) => {
  if (newVal) {
    formData.longitude = newVal.longitude
    formData.latitude = newVal.latitude
    if (newVal.address && !formData.locationDesc) {
      formData.locationDesc = newVal.address
    }
  }
})

// 初始化
onMounted(() => {
  loadItemData()
})
</script>

<style scoped lang="scss">
.item-publish {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px 0;

  .container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .page-header {
    margin-bottom: 24px;
    text-align: center;

    h1 {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .publish-card {
    :deep(.el-card__body) {
      padding: 32px;
    }
  }

  .type-radio-group {
    width: 100%;
    display: flex;

    :deep(.el-radio-button) {
      flex: 1;

      .el-radio-button__inner {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 16px 20px;
        font-size: 16px;
      }
    }
  }

  .type-hint {
    margin-top: 8px;
    font-size: 13px;
    color: #909399;
  }

  .ai-tag {
    margin-left: 8px;
    padding: 2px 6px;
    background: #e6f7ff;
    color: #1890ff;
    border-radius: 4px;
    font-size: 12px;
  }

  .ai-hint {
    margin-top: 8px;
    padding: 8px 12px;
    background: #f0f9eb;
    border-radius: 4px;
    font-size: 13px;
    color: #67c23a;
    display: flex;
    align-items: center;
    gap: 6px;

    .el-button {
      margin-left: auto;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }

  .recognizing-hint {
    margin-top: 8px;
    font-size: 13px;
    color: #409eff;
    display: flex;
    align-items: center;
    gap: 6px;

    .is-loading {
      animation: rotating 2s linear infinite;
    }
  }

  @keyframes rotating {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .selected-location {
    margin-top: -12px;
    margin-bottom: 18px;

    .el-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  .submit-buttons {
    margin-top: 32px;
    margin-bottom: 0;

    :deep(.el-form-item__content) {
      justify-content: center;
      gap: 16px;
    }

    .el-button {
      min-width: 120px;
    }
  }
}
</style>
