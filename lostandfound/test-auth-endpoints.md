# 用户认证模块手动测试指南

## 前置条件
1. 确保数据库已启动并初始化
2. 启动Spring Boot应用: `./mvnw.cmd spring-boot:run`
3. 应用运行在 http://localhost:8080

## 测试场景

### 1. 用户注册测试

```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"studentId\":\"2021001\",\"name\":\"张三\",\"phone\":\"13800138000\",\"password\":\"123456\"}"
```

**预期结果**: 返回200状态码，包含用户信息（不含密码）

### 2. 用户登录测试

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"studentId\":\"2021001\",\"password\":\"123456\"}"
```

**预期结果**: 返回200状态码，包含accessToken和refreshToken

**保存返回的token用于后续测试**

### 3. 获取当前用户信息测试

```bash
curl -X GET http://localhost:8080/api/v1/users/me ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**预期结果**: 返回200状态码，包含当前用户详细信息

### 4. 刷新令牌测试

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh ^
  -H "Content-Type: application/json" ^
  -d "{\"refreshToken\":\"YOUR_REFRESH_TOKEN_HERE\"}"
```

**预期结果**: 返回200状态码，包含新的accessToken和refreshToken

### 5. 登录失败5次锁定测试

连续5次使用错误密码登录：

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"studentId\":\"2021001\",\"password\":\"wrongpassword\"}"
```

重复执行5次后，第6次尝试应该返回账户锁定错误。

**预期结果**: 
- 前5次返回401 "密码错误"
- 第6次返回400 "账户已锁定，请15分钟后重试"

### 6. 更新用户信息测试

```bash
curl -X PUT http://localhost:8080/api/v1/users/me ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"张三三\",\"phone\":\"13900139000\"}"
```

**预期结果**: 返回200状态码，包含更新后的用户信息

### 7. 未授权访问测试

不带token访问受保护接口：

```bash
curl -X GET http://localhost:8080/api/v1/users/me
```

**预期结果**: 返回401状态码

### 8. 无效token测试

使用无效token访问：

```bash
curl -X GET http://localhost:8080/api/v1/users/me ^
  -H "Authorization: Bearer invalid_token_here"
```

**预期结果**: 返回401状态码

## 测试检查清单

- [x] 所有单元测试通过 (42个测试全部通过)
- [ ] 用户注册功能正常
- [ ] 用户登录功能正常
- [ ] JWT令牌生成和验证正常
- [ ] 刷新令牌功能正常
- [ ] 获取用户信息功能正常
- [ ] 更新用户信息功能正常
- [ ] 登录失败5次后账户锁定功能正常
- [ ] 未授权访问被正确拦截
- [ ] 无效token被正确拒绝

## 注意事项

1. 测试前确保Redis服务正常运行（用于登录失败计数和锁定）
2. 测试账户锁定功能时，需要等待15分钟或手动清除Redis中的锁定记录
3. 所有密码在数据库中应为BCrypt加密格式
4. 手机号格式需符合中国大陆手机号规则
