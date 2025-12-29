# 测试文档中心

本目录包含项目的所有测试相关文档和指南。

## 📋 测试文档列表

### 功能测试

| 文档 | 说明 | 状态 |
|------|------|------|
| [认证接口测试](../../test-auth-endpoints.md) | 用户认证模块手动测试指南 | ✅ 完成 |
| [文件上传测试](../../test-file-upload.md) | 文件上传功能测试指南 | ✅ 完成 |

### 验证报告

| 文档 | 说明 | 日期 |
|------|------|------|
| [Checkpoint 8 验证报告](../../checkpoint-8-verification-report.md) | 用户认证模块验证报告 | 2025-12-22 |

## 🧪 测试类型

### 单元测试

项目使用 JUnit 5 进行单元测试，测试代码位于 `src/test/java/` 目录。

**运行所有单元测试**:
```bash
.\mvnw.cmd test
```

**运行特定测试类**:
```bash
.\mvnw.cmd test -Dtest=AuthServiceTest
```

**测试覆盖率**:
```bash
.\mvnw.cmd clean verify
```

### 属性测试

项目使用 jqwik 进行属性测试（Property-Based Testing），用于验证系统的正确性属性。

**属性测试示例**:
- `AuthServicePropertyTest` - 认证服务属性测试
- `ItemServicePropertyTest` - 物品服务属性测试
- `UserServicePropertyTest` - 用户服务属性测试

### 集成测试

集成测试验证多个组件协同工作的正确性。

**集成测试示例**:
- `ApiEndpointTest` - API 端点集成测试
- `EndToEndIntegrationTest` - 端到端集成测试

### 手动测试

手动测试指南提供了详细的测试步骤和预期结果。

**手动测试文档**:
- [认证接口测试](../../test-auth-endpoints.md)
- [文件上传测试](../../test-file-upload.md)

## 📊 测试统计

### 当前测试覆盖

| 模块 | 单元测试 | 属性测试 | 集成测试 | 覆盖率 |
|------|---------|---------|---------|--------|
| 用户认证 | ✅ 14 | ✅ 1 | ✅ 1 | 90%+ |
| 文件上传 | ✅ 8 | ✅ 1 | ✅ 1 | 85%+ |
| 物品管理 | ✅ 12 | ✅ 1 | ✅ 1 | 88%+ |
| 智能匹配 | ✅ 10 | ❌ 0 | ✅ 1 | 75%+ |
| 消息通知 | ✅ 6 | ❌ 0 | ✅ 1 | 70%+ |
| 积分系统 | ✅ 8 | ✅ 1 | ✅ 1 | 80%+ |
| 管理后台 | ✅ 10 | ❌ 0 | ✅ 1 | 75%+ |

**总计**: 68 个单元测试，4 个属性测试，7 个集成测试

## 🔧 测试工具

### 后端测试工具

- **JUnit 5**: 单元测试框架
- **jqwik**: 属性测试框架
- **Mockito**: Mock 框架
- **Spring Boot Test**: Spring 测试支持
- **H2 Database**: 内存数据库（测试用）
- **Embedded Redis**: 嵌入式 Redis（测试用）

### 前端测试工具

- **Vitest**: 单元测试框架
- **Vue Test Utils**: Vue 组件测试
- **Playwright**: E2E 测试（计划中）

### API 测试工具

- **curl**: 命令行 HTTP 客户端
- **Postman**: API 测试工具
- **Swagger UI**: API 文档和测试

## 📝 测试规范

### 测试命名规范

**单元测试**:
```java
@Test
void shouldReturnUserWhenLoginWithValidCredentials() {
    // 测试代码
}
```

**属性测试**:
```java
@Property
void anyValidUserShouldBeAbleToLogin(@ForAll("validUsers") User user) {
    // 属性测试代码
}
```

### 测试组织

```
src/test/java/
├── com/campus/lostandfound/
│   ├── config/              # 配置测试
│   ├── service/             # 服务层测试
│   │   ├── AuthServiceTest.java
│   │   └── AuthServicePropertyTest.java
│   ├── controller/          # 控制器测试
│   ├── integration/         # 集成测试
│   └── util/                # 工具类测试
```

### 测试数据

测试数据位于 `src/test/resources/`:
- `application-test.yml` - 测试配置
- `schema.sql` - 测试数据库结构
- `data-test.sql` - 测试数据（可选）

## 🚀 快速开始

### 1. 运行所有测试

```bash
cd lostandfound
.\mvnw.cmd clean verify
```

### 2. 运行特定模块测试

```bash
# 认证模块
.\mvnw.cmd test -Dtest=AuthServiceTest

# 物品模块
.\mvnw.cmd test -Dtest=ItemServiceTest

# 所有属性测试
.\mvnw.cmd test -Dtest=*PropertyTest
```

### 3. 生成测试报告

```bash
.\mvnw.cmd clean verify
# 报告位于 target/site/jacoco/index.html
```

### 4. 手动测试

参考具体的测试文档：
- [认证接口测试](../../test-auth-endpoints.md)
- [文件上传测试](../../test-file-upload.md)

## 📖 测试最佳实践

### 1. 测试独立性

每个测试应该独立运行，不依赖其他测试的执行顺序。

```java
@BeforeEach
void setUp() {
    // 每个测试前重置状态
}

@AfterEach
void tearDown() {
    // 每个测试后清理资源
}
```

### 2. 使用有意义的测试名称

```java
// ❌ 不好
@Test
void test1() { }

// ✅ 好
@Test
void shouldThrowExceptionWhenPasswordIsInvalid() { }
```

### 3. 遵循 AAA 模式

```java
@Test
void shouldReturnUserWhenLoginSuccessful() {
    // Arrange - 准备测试数据
    LoginDTO loginDTO = new LoginDTO("2021001", "password123");
    
    // Act - 执行测试操作
    TokenVO result = authService.login(loginDTO);
    
    // Assert - 验证结果
    assertNotNull(result);
    assertNotNull(result.getAccessToken());
}
```

### 4. 测试边界条件

```java
@Test
void shouldRejectEmptyPassword() { }

@Test
void shouldRejectTooLongPassword() { }

@Test
void shouldAcceptMinimumLengthPassword() { }
```

### 5. 使用属性测试验证通用规则

```java
@Property
void anyValidPasswordShouldBeEncryptedCorrectly(
    @ForAll @StringLength(min = 6, max = 20) String password
) {
    String encrypted = passwordEncoder.encode(password);
    assertTrue(passwordEncoder.matches(password, encrypted));
}
```

## 🐛 常见问题

### Q: 测试失败但本地运行正常？

**A**: 检查以下几点：
1. 测试数据库是否正确初始化
2. Redis 是否正常运行
3. 测试配置文件是否正确
4. 是否有测试间的状态污染

### Q: 如何跳过测试？

```bash
# 跳过所有测试
.\mvnw.cmd clean install -DskipTests

# 跳过特定测试
.\mvnw.cmd test -Dtest=!AuthServiceTest
```

### Q: 如何调试测试？

1. 在 IDE 中右键测试方法，选择 "Debug"
2. 或在测试代码中添加断点
3. 使用 `@Disabled` 临时禁用其他测试

### Q: 属性测试运行太慢？

```java
// 减少测试次数
@Property(tries = 10)  // 默认是 1000
void myPropertyTest() { }
```

## 📚 相关资源

- [JUnit 5 文档](https://junit.org/junit5/docs/current/user-guide/)
- [jqwik 文档](https://jqwik.net/docs/current/user-guide.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

## 🔄 持续集成

项目配置了 CI/CD 流程，每次提交都会自动运行测试：

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: ./mvnw clean verify
```

---

**最后更新**: 2025-12-29  
**维护者**: Smart Campus Team
