# 阿里云OSS集成指南

## 配置说明

### 1. 环境变量设置

在Windows系统中设置以下环境变量：

```cmd
setx ALIYUN_OSS_ACCESS_KEY_ID "你的AccessKey ID"
setx ALIYUN_OSS_ACCESS_KEY_SECRET "你的AccessKey Secret"
```

**注意**：设置后需要重启IDE或终端才能生效。

### 2. 配置文件

OSS配置已添加到：
- `application.yml` - 生产环境配置（使用环境变量）
- `application-local.yml` - 本地开发配置

当前Bucket信息：
- **Endpoint**: `oss-cn-shanghai.aliyuncs.com`
- **Bucket名称**: `oss-pai-99w3ve23kstcxnmhyh-cn-shanghai`
- **地域**: 华东2（上海）

## API接口

### 1. 上传物品图片

**接口**: `POST /api/v1/files/upload/item`

**请求头**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 图片文件（必填）

**限制**:
- 文件大小：最大10MB
- 文件格式：JPG、PNG、GIF、WEBP

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/items/2025-12-22/abc123.jpg"
}
```

### 2. 批量上传物品图片

**接口**: `POST /api/v1/files/upload/items`

**请求头**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

**请求参数**:
- `files`: 图片文件数组（必填，最多9张）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    "https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/items/2025-12-22/abc123.jpg",
    "https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/items/2025-12-22/def456.jpg"
  ]
}
```

### 3. 上传用户头像

**接口**: `POST /api/v1/files/upload/avatar`

**请求头**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 头像文件（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/avatars/2025-12-22/xyz789.jpg"
}
```

### 4. 删除文件

**接口**: `DELETE /api/v1/files?url={fileUrl}`

**请求头**:
```
Authorization: Bearer {JWT_TOKEN}
```

**请求参数**:
- `url`: 文件URL（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## 文件存储结构

```
bucket-name/
├── items/              # 物品图片
│   ├── 2025-12-22/
│   │   ├── abc123.jpg
│   │   └── def456.png
│   └── 2025-12-23/
└── avatars/            # 用户头像
    └── 2025-12-22/
        └── xyz789.jpg
```

## 测试步骤

### 1. 启动应用

```cmd
cd lostandfound
mvn spring-boot:run
```

### 2. 获取JWT Token

先登录获取token：

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

### 3. 测试上传图片

使用Postman或curl测试：

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/item \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

## 安全建议

1. ✅ 已使用RAM子账号，权限仅限OSS
2. ✅ AccessKey通过环境变量管理，不提交到Git
3. ✅ 文件类型和大小验证已实现
4. ✅ 需要JWT认证才能上传文件
5. ⚠️ 建议在阿里云控制台配置：
   - Bucket防盗链
   - 跨域CORS规则（如果前端直传）
   - 图片处理样式（缩略图、水印等）

## 常见问题

### Q: 上传失败，提示"Access Denied"
A: 检查RAM用户是否有OSS权限，确认AccessKey是否正确设置。

### Q: 环境变量不生效
A: 设置环境变量后需要重启IDE或终端。

### Q: 如何查看已上传的文件？
A: 登录阿里云控制台 → 对象存储OSS → 选择Bucket → 文件管理。

### Q: 如何配置图片缩略图？
A: 在阿里云OSS控制台配置图片处理样式，然后在URL后添加样式参数。

## 下一步

- [ ] 配置Bucket防盗链和CORS
- [ ] 添加图片压缩和缩略图处理
- [ ] 实现前端直传（减轻服务器压力）
- [ ] 添加文件上传进度显示
- [ ] 配置CDN加速
