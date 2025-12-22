# 文件上传功能测试

## 前置条件

1. 应用已启动在 `http://localhost:8080`
2. 已设置环境变量 `ALIYUN_OSS_ACCESS_KEY_ID` 和 `ALIYUN_OSS_ACCESS_KEY_SECRET`
3. 数据库中有测试用户

## 测试步骤

### 1. 登录获取JWT Token

使用已注册的用户登录：

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      ...
    }
  }
}
```

**保存accessToken**，后续请求需要使用。

### 2. 测试上传单张图片

准备一张测试图片（如 `test.jpg`），然后执行：

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/item ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "file=@test.jpg"
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/items/2025-12-22/abc123.jpg"
}
```

### 3. 测试上传头像

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/avatar ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "file=@avatar.jpg"
```

### 4. 测试批量上传（最多9张）

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/items ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "files=@test1.jpg" ^
  -F "files=@test2.jpg" ^
  -F "files=@test3.jpg"
```

### 5. 测试删除文件

```bash
curl -X DELETE "http://localhost:8080/api/v1/files?url=https://oss-pai-99w3ve23kstcxnmhyh-cn-shanghai.oss-cn-shanghai.aliyuncs.com/items/2025-12-22/abc123.jpg" ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 使用Postman测试

### 1. 登录获取Token

- **Method**: POST
- **URL**: `http://localhost:8080/api/v1/auth/login`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
  ```json
  {
    "username": "testuser",
    "password": "password123"
  }
  ```

### 2. 上传图片

- **Method**: POST
- **URL**: `http://localhost:8080/api/v1/files/upload/item`
- **Headers**: 
  - `Authorization: Bearer YOUR_ACCESS_TOKEN`
- **Body** (form-data):
  - Key: `file`
  - Type: File
  - Value: 选择图片文件

## 验证结果

### 1. 检查响应

成功上传后会返回文件URL，复制URL到浏览器访问，应该能看到上传的图片。

### 2. 在阿里云控制台查看

1. 登录阿里云控制台
2. 进入对象存储OSS
3. 选择Bucket: `oss-pai-99w3ve23kstcxnmhyh-cn-shanghai`
4. 进入文件管理
5. 查看 `items/` 或 `avatars/` 目录下的文件

## 错误处理测试

### 1. 测试未认证访问

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/item ^
  -F "file=@test.jpg"
```

**预期**：返回401未授权错误

### 2. 测试上传非图片文件

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/item ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "file=@test.txt"
```

**预期**：返回400错误，提示"只支持上传 JPG、PNG、GIF、WEBP 格式的图片"

### 3. 测试上传超大文件

准备一个超过10MB的图片文件：

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/item ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "file=@large.jpg"
```

**预期**：返回400错误，提示"图片大小不能超过10MB"

### 4. 测试批量上传超过9张

```bash
curl -X POST http://localhost:8080/api/v1/files/upload/items ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -F "files=@1.jpg" ^
  -F "files=@2.jpg" ^
  ... (超过9个)
```

**预期**：返回400错误，提示"最多只能上传9张图片"

## 常见问题

### Q: 返回500错误，提示"Access Denied"
**A**: 检查环境变量是否正确设置：
```cmd
echo %ALIYUN_OSS_ACCESS_KEY_ID%
echo %ALIYUN_OSS_ACCESS_KEY_SECRET%
```

### Q: 上传成功但无法访问图片
**A**: 检查Bucket权限设置，确保是"公共读"或配置了正确的访问策略。

### Q: 返回401错误
**A**: JWT Token可能已过期（2小时有效期），重新登录获取新Token。

## 性能测试

### 测试并发上传

使用Apache Bench或JMeter测试并发上传性能：

```bash
# 需要安装Apache Bench
ab -n 100 -c 10 -H "Authorization: Bearer YOUR_TOKEN" -p test.jpg http://localhost:8080/api/v1/files/upload/item
```

## 下一步

- [ ] 配置Bucket为公共读（或使用签名URL）
- [ ] 添加图片压缩功能
- [ ] 实现前端直传OSS
- [ ] 添加上传进度显示
- [ ] 配置CDN加速
