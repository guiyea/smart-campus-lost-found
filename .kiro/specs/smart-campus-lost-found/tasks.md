# 实现计划

## 第一阶段：项目初始化与基础架构

- [ ] 1. Spring Boot项目初始化
  - [x] 1.1 创建Maven项目结构





    - 使用Spring Initializr创建Spring Boot 3.x项目
    - GroupId: `com.campus`, ArtifactId: `lostandfound`
    - 添加依赖: spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-data-redis, mybatis-plus-boot-starter, mysql-connector-j, lombok, jjwt
    - 创建标准目录结构: `src/main/java/com/campus/lostandfound/`
    - 子目录: config/, controller/, service/, service/impl/, repository/, model/entity/, model/dto/, model/vo/, common/, exception/, util/, websocket/
    - 创建资源目录: `src/main/resources/` 包含 application.yml, application-local.yml, mapper/
    - _Requirements: 10.1, 10.5_

  - [x] 1.2 配置application.yml基础配置







    - 配置服务端口: server.port=8080
    - 配置MySQL数据源: spring.datasource.url, username, password, driver-class-name
    - 配置MyBatis-Plus: mybatis-plus.mapper-locations, type-aliases-package, configuration.map-underscore-to-camel-case
    - 配置Redis: spring.redis.host, port, password, database
    - 配置文件上传: spring.servlet.multipart.max-file-size=10MB, max-request-size=20MB
    - 创建application-local.yml用于本地开发敏感配置（不提交到VCS）
    - _Requirements: 10.5_

  - [x] 1.3 创建数据库表结构





    - 创建数据库: `campus_lost_found`
    - 创建user表: id, student_id(UK), name, phone, password, avatar, points, role, status, created_at, updated_at
    - 创建item表: id, user_id(FK), title, description, type, category, longitude, latitude, location_desc, event_time, status, view_count, deleted, created_at, updated_at
    - 创建item_image表: id, item_id(FK), url, sort, created_at
    - 创建item_tag表: id, item_id(FK), tag, confidence
    - 创建match_record表: id, lost_item_id(FK), found_item_id(FK), score, status, created_at, confirmed_at
    - 创建message表: id, user_id(FK), title, content, type, related_id, is_read, created_at
    - 创建point_record表: id, user_id(FK), points, reason, related_id, created_at
    - 为所有外键和常用查询字段创建索引
    - _Requirements: 2.1_

  - [x] 1.4 生成MyBatis-Plus基础代码





    - 配置MyBatis-Plus代码生成器
    - 生成所有表对应的Entity类（使用@TableName, @TableId, @TableField注解）
    - 生成所有表对应的Mapper接口（继承BaseMapper）
    - 在Entity类中添加@Data, @TableLogic(软删除), @TableField(fill=INSERT/UPDATE)注解
    - 创建MyBatisPlusConfig配置类，配置分页插件PaginationInnerInterceptor
    - _Requirements: 2.1_

- [ ] 2. 统一响应与异常处理
  - [x] 2.1 创建统一响应类





    - 创建`common/Result.java`: code(Integer), message(String), data(T), timestamp(Long)
    - 实现静态方法: success(), success(T data), success(String message, T data)
    - 实现静态方法: error(Integer code, String message), error(ResultCode resultCode)
    - 创建`common/PageResult.java`: list(List<T>), total(Long), pageNum(Integer), pageSize(Integer)
    - 创建`common/ResultCode.java`枚举: SUCCESS(200), BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404), VALIDATION_ERROR(422), RATE_LIMIT(429), INTERNAL_ERROR(500)
    - _Requirements: 所有接口_

  - [x] 2.2 创建自定义异常类





    - 创建`exception/BusinessException.java`: code, message, 继承RuntimeException
    - 创建`exception/UnauthorizedException.java`: 401未认证异常
    - 创建`exception/ForbiddenException.java`: 403无权限异常
    - 创建`exception/NotFoundException.java`: 404资源不存在异常
    - 创建`exception/ValidationException.java`: 422参数验证异常
    - 创建`exception/RateLimitException.java`: 429请求频率超限异常
    - _Requirements: 所有接口_

  - [x] 2.3 实现全局异常处理器





    - 创建`exception/GlobalExceptionHandler.java`，使用@RestControllerAdvice
    - 处理BusinessException: 返回对应code和message
    - 处理MethodArgumentNotValidException: 提取字段验证错误信息，返回422
    - 处理ConstraintViolationException: 提取参数验证错误信息，返回422
    - 处理HttpMessageNotReadableException: 返回400请求体格式错误
    - 处理NoHandlerFoundException: 返回404
    - 处理Exception: 记录错误日志（不泄露敏感信息），返回500
    - _Requirements: 所有接口_

- [x] 3. Checkpoint - 基础架构验证





  - 运行`mvn clean compile`确保项目编译通过
  - 启动应用确保能正常连接MySQL和Redis
  - 确保所有测试通过，如有问题请询问用户

## 第二阶段：用户认证模块

- [ ] 4. JWT认证基础设施
  - [x] 4.1 创建JWT工具类





    - 创建`util/JwtUtil.java`
    - 定义常量: SECRET_KEY(从配置读取), ACCESS_TOKEN_EXPIRATION(2小时), REFRESH_TOKEN_EXPIRATION(7天)
    - 实现generateAccessToken(Long userId, String studentId, Integer role): 生成访问令牌
    - 实现generateRefreshToken(Long userId): 生成刷新令牌
    - 实现parseToken(String token): 解析令牌返回Claims
    - 实现getUserIdFromToken(String token): 从令牌提取用户ID
    - 实现getRoleFromToken(String token): 从令牌提取用户角色
    - 实现validateToken(String token): 验证令牌有效性（签名、过期时间）
    - 实现isTokenExpired(String token): 检查令牌是否过期
    - _Requirements: 1.2, 10.1_

  - [x] 4.2 配置Spring Security





    - 创建`config/SecurityConfig.java`
    - 配置SecurityFilterChain: 禁用CSRF, 启用CORS, 设置无状态会话
    - 配置公开路径白名单: /api/v1/auth/register, /api/v1/auth/login, /api/v1/auth/refresh, /api/v1/items(GET), /swagger-ui/**, /v3/api-docs/**
    - 配置其他路径需要认证
    - 配置PasswordEncoder使用BCryptPasswordEncoder
    - _Requirements: 10.1_

  - [x] 4.3 实现JWT认证过滤器




    - 创建`config/JwtAuthenticationFilter.java`，继承OncePerRequestFilter
    - 从请求头Authorization提取Bearer令牌
    - 调用JwtUtil验证令牌有效性
    - 验证通过后从令牌提取用户信息，创建UsernamePasswordAuthenticationToken
    - 将认证信息设置到SecurityContextHolder
    - 令牌无效或过期时不设置认证信息，让后续过滤器处理
    - _Requirements: 10.1_

  - [ ]* 4.4 编写JWT认证属性测试
    - 使用jqwik框架
    - **Property 2: 登录认证往返一致性**
    - 测试: 对于任意有效用户ID和角色，生成的令牌解析后应返回相同的用户ID和角色
    - 测试: 对于任意过期令牌，validateToken应返回false
    - 配置最少100次迭代
    - **Validates: Requirements 1.2, 10.1**

- [ ] 5. 用户注册功能
  - [x] 5.1 创建用户相关DTO和VO





    - 创建`model/dto/RegisterDTO.java`: studentId(学号,@NotBlank,@Size(5,20)), name(@NotBlank,@Size(2,20)), phone(@NotBlank,@Pattern手机号正则), password(@NotBlank,@Size(6,20))
    - 创建`model/dto/LoginDTO.java`: studentId(@NotBlank), password(@NotBlank)
    - 创建`model/vo/UserVO.java`: id, studentId, name, phone(脱敏), avatar, points, role, createdAt
    - 创建`model/vo/TokenVO.java`: accessToken, refreshToken, expiresIn, userInfo(UserVO)
    - _Requirements: 1.1, 1.2_

  - [x] 5.2 实现用户注册Service





    - 创建`service/AuthService.java`接口，定义register, login, refreshToken, logout方法
    - 创建`service/impl/AuthServiceImpl.java`
    - 实现register(RegisterDTO dto)方法:
      - 验证学号是否已存在，存在则抛出BusinessException
      - 验证手机号是否已存在，存在则抛出BusinessException
      - 使用BCryptPasswordEncoder加密密码
      - 创建User实体，设置默认积分为0，角色为0(普通用户)，状态为0(正常)
      - 保存用户到数据库
      - 返回UserVO（不包含密码）
    - _Requirements: 1.1, 10.4_

  - [x] 5.3 实现用户注册Controller





    - 创建`controller/AuthController.java`，使用@RestController, @RequestMapping("/api/v1/auth")
    - 实现POST /register接口
    - 使用@Valid验证RegisterDTO
    - 调用AuthService.register()
    - 返回Result<UserVO>
    - _Requirements: 1.1_

  - [ ]* 5.4 编写用户注册属性测试
    - **Property 1: 用户注册数据完整性**
    - 测试: 对于任意有效注册信息，注册后查询用户应返回相同的studentId, name, phone
    - 测试: 对于任意重复学号，注册应抛出异常
    - 配置最少100次迭代
    - **Validates: Requirements 1.1**

  - [ ]* 5.5 编写密码加密属性测试
    - **Property 20: 密码加密存储**
    - 测试: 对于任意密码，存储后的值应以$2a$或$2b$开头（BCrypt格式）
    - 测试: 对于任意密码，存储值与原始密码不相等
    - 测试: 对于任意密码，使用BCrypt.matches验证应返回true
    - 配置最少100次迭代
    - **Validates: Requirements 10.4**

- [ ] 6. 用户登录功能
  - [x] 6.1 实现Redis工具类





    - 创建`util/RedisUtil.java`
    - 注入StringRedisTemplate
    - 实现set(String key, String value, long timeout, TimeUnit unit)
    - 实现get(String key): String
    - 实现delete(String key)
    - 实现increment(String key): Long
    - 实现expire(String key, long timeout, TimeUnit unit)
    - 实现hasKey(String key): Boolean
    - _Requirements: 1.4, 10.5_

  - [x] 6.2 实现用户登录Service





    - 在AuthServiceImpl中实现login(LoginDTO dto)方法:
      - 根据studentId查询用户，不存在则抛出UnauthorizedException("用户不存在")
      - 检查用户状态，如果status=1(封禁)则抛出ForbiddenException("账户已被封禁")
      - 检查Redis中登录失败次数key: `login:fail:{studentId}`
      - 如果失败次数>=5，检查锁定时间，未过期则抛出BusinessException("账户已锁定，请15分钟后重试")
      - 使用BCrypt验证密码，不匹配则:
        - Redis失败次数+1，设置过期时间15分钟
        - 抛出UnauthorizedException("密码错误")
      - 密码正确则清除失败次数
      - 检查是否为当日首次登录（Redis key: `login:daily:{userId}:{date}`）
      - 如果是首次登录，调用PointService增加2积分
      - 生成accessToken和refreshToken
      - 返回TokenVO
    - _Requirements: 1.2, 1.4, 7.3_

  - [x] 6.3 实现用户登录Controller




    - 在AuthController中实现POST /login接口
    - 使用@Valid验证LoginDTO
    - 调用AuthService.login()
    - 返回Result<TokenVO>
    - _Requirements: 1.2_

  - [x] 6.4 实现令牌刷新功能





    - 在AuthServiceImpl中实现refreshToken(String refreshToken)方法:
      - 验证refreshToken有效性
      - 从refreshToken提取userId
      - 查询用户信息
      - 生成新的accessToken和refreshToken
      - 返回TokenVO
    - 在AuthController中实现POST /refresh接口
    - _Requirements: 1.2_

- [ ] 7. 用户信息管理
  - [x] 7.1 创建用户信息更新DTO





    - 创建`model/dto/UpdateProfileDTO.java`: name(@Size(2,20)), phone(@Pattern手机号正则), avatar(URL), oldPassword, newPassword(@Size(6,20))
    - 所有字段可选，只更新非空字段
    - _Requirements: 1.3_

  - [x] 7.2 实现用户信息Service





    - 创建`service/UserService.java`接口
    - 创建`service/impl/UserServiceImpl.java`
    - 实现getProfile(Long userId): 查询用户信息返回UserVO
    - 实现updateProfile(Long userId, UpdateProfileDTO dto):
      - 如果更新手机号，验证新手机号是否已被其他用户使用
      - 如果更新密码，验证oldPassword是否正确
      - 更新非空字段
      - 返回更新后的UserVO
    - 实现getUserList(String studentId, String name, Integer pageNum, Integer pageSize):
      - 构建查询条件（支持模糊查询）
      - 返回PageResult<UserVO>
    - _Requirements: 1.3, 1.5_

  - [x] 7.3 实现用户信息Controller





    - 创建`controller/UserController.java`，使用@RequestMapping("/api/v1/users")
    - 实现GET /me: 获取当前登录用户信息（从SecurityContext获取userId）
    - 实现PUT /me: 更新当前用户信息
    - 实现GET /（管理员）: 分页查询用户列表，支持studentId和name筛选
    - 使用@PreAuthorize("hasRole('ADMIN')")限制管理员接口
    - _Requirements: 1.3, 1.5_

  - [ ]* 7.4 编写用户信息更新权限属性测试
    - **Property 3: 用户信息更新权限验证**
    - 测试: 对于任意两个不同用户A和B，用户A的令牌尝试更新用户B的信息应被拒绝
    - 测试: 对于任意用户，使用自己的令牌更新自己的信息应成功
    - 配置最少100次迭代
    - **Validates: Requirements 1.3, 2.4**

- [x] 8. Checkpoint - 用户认证模块验证






  - 运行`mvn clean verify`确保所有测试通过
  - 使用Postman/curl测试注册、登录、刷新令牌、获取用户信息接口
  - 验证登录失败5次后账户锁定功能
  - 确保所有测试通过，如有问题请询问用户

## 第三阶段：文件上传模块

- [ ] 9. 阿里云OSS集成
  - [x] 9.1 配置阿里云OSS





    - 添加aliyun-sdk-oss依赖到pom.xml
    - 在application.yml中添加OSS配置（endpoint, accessKeyId, accessKeySecret, bucketName从环境变量读取）
    - 创建`config/OssConfig.java`:
      - 使用@ConfigurationProperties读取配置
      - 创建OSSClient Bean
    - _Requirements: 2.1_

  - [x] 9.2 实现文件服务





    - 创建`service/FileService.java`接口
    - 创建`service/impl/FileServiceImpl.java`
    - 实现uploadImage(MultipartFile file):
      - 验证文件类型（仅允许jpg, jpeg, png, gif, webp）
      - 验证文件大小（不超过10MB）
      - 如果文件大于5MB，调用压缩方法
      - 生成唯一文件名: `images/{yyyy/MM/dd}/{uuid}.{ext}`
      - 上传到OSS
      - 返回文件访问URL
    - 实现compressImage(MultipartFile file): 使用Thumbnailator压缩图片到5MB以下
    - 实现deleteFile(String fileUrl): 从OSS删除文件
    - _Requirements: 2.1, 3.5, 10.2_

  - [ ]* 9.3 编写图片压缩属性测试
    - **Property 7: 图片压缩阈值处理**
    - 测试: 对于任意大于5MB的图片，压缩后大小应<=5MB
    - 测试: 对于任意小于5MB的图片，不应进行压缩
    - 配置最少100次迭代
    - **Validates: Requirements 3.5**

  - [x] 9.4 实现文件上传Controller




    - 创建`controller/FileController.java`，使用@RequestMapping("/api/v1/files")
    - 实现POST /upload: 上传单个文件
      - 使用@RequestParam("file") MultipartFile接收文件
      - 调用FileService.uploadImage()
      - 返回Result包含文件URL
    - 实现POST /upload/batch: 批量上传（最多9张）
      - 返回Result包含文件URL列表
    - _Requirements: 2.1, 10.2_

  - [ ]* 9.5 编写文件上传验证属性测试
    - **Property 21: 文件上传验证**
    - 测试: 对于任意非图片格式文件，上传应被拒绝
    - 测试: 对于任意大于10MB的文件，上传应被拒绝
    - 测试: 对于任意有效图片文件（<=10MB），上传应成功
    - 配置最少100次迭代
    - **Validates: Requirements 10.2**

## 第四阶段：物品信息模块

- [ ] 10. 物品信息发布
  - [x] 10.1 创建物品相关DTO和VO





    - 创建`model/dto/ItemDTO.java`:
      - title(@NotBlank, @Size(2,50)): 标题
      - description(@NotBlank, @Size(10,1000)): 描述
      - type(@NotNull, 0或1): 类型，0失物1招领
      - category: 物品类别（可由AI识别自动填充）
      - images(@NotEmpty, @Size(max=9)): 图片URL列表
      - longitude, latitude: 经纬度坐标
      - locationDesc(@NotBlank): 地点描述
      - eventTime(@NotNull): 丢失/拾获时间
    - 创建`model/dto/ItemSearchDTO.java`:
      - keyword: 搜索关键词
      - type: 类型筛选
      - category: 类别筛选
      - status: 状态筛选
      - startTime, endTime: 时间范围
      - longitude, latitude, radius: 地点范围
      - sortBy: 排序方式（time/distance/match）
      - pageNum, pageSize: 分页参数
    - 创建`model/vo/ItemVO.java`:
      - id, userId, userName, userAvatar
      - title, description, type, category
      - images(List<String>), tags(List<String>)
      - longitude, latitude, locationDesc
      - eventTime, status, viewCount
      - distance(计算字段)
      - createdAt, updatedAt
    - 创建`model/vo/ItemDetailVO.java`: 继承ItemVO，增加matchRecommendations(List<ItemVO>)
    - _Requirements: 2.1, 2.6, 5.3_

  - [x] 10.2 实现物品信息Service - 发布功能





    - 创建`service/ItemService.java`接口
    - 创建`service/impl/ItemServiceImpl.java`
    - 实现publish(ItemDTO dto, Long userId):
      - 创建Item实体，设置userId, 初始status=0(待处理), viewCount=0, deleted=0
      - 保存Item到数据库
      - 批量保存图片到item_image表
      - 如果type=1(招领)，调用PointService为用户增加10积分
      - 异步调用ImageRecognitionService识别图片，保存标签到item_tag表
      - 异步调用MatchService执行匹配计算
      - 返回ItemVO
    - _Requirements: 2.1, 7.2_

  - [x] 10.3 实现物品信息Controller - 发布接口





    - 创建`controller/ItemController.java`，使用@RequestMapping("/api/v1/items")
    - 实现POST /: 发布物品信息
      - 使用@Valid验证ItemDTO
      - 从SecurityContext获取当前用户ID
      - 调用ItemService.publish()
      - 返回Result<ItemVO>
    - _Requirements: 2.1_

  - [ ]* 10.4 编写物品信息发布属性测试
    - **Property 4: 物品信息发布往返一致性**
    - 测试: 对于任意有效物品信息，发布后查询应返回相同的title, description, type, category, locationDesc
    - 测试: 对于任意发布的招领信息，用户积分应增加10
    - 配置最少100次迭代
    - **Validates: Requirements 2.1**

- [ ] 11. 物品信息编辑和删除
  - [x] 11.1 实现物品编辑Service





    - 在ItemServiceImpl中实现update(Long id, ItemDTO dto, Long userId):
      - 查询物品信息，不存在则抛出NotFoundException
      - 验证userId是否为物品发布者，不是则抛出ForbiddenException("无权修改他人发布的信息")
      - 更新物品基本信息
      - 删除旧图片记录，插入新图片记录
      - 如果图片变更，重新调用AI识别更新标签
      - 返回更新后的ItemVO
    - _Requirements: 2.4_

  - [x] 11.2 实现物品删除Service（软删除）





    - 在ItemServiceImpl中实现delete(Long id, Long userId):
      - 查询物品信息，不存在则抛出NotFoundException
      - 验证userId是否为物品发布者，不是则抛出ForbiddenException
      - 设置deleted=1（软删除）
      - 不物理删除数据，保留原始记录
    - _Requirements: 2.5_

  - [x] 11.3 实现物品编辑删除Controller





    - 在ItemController中实现PUT /{id}: 更新物品信息
    - 在ItemController中实现DELETE /{id}: 删除物品信息
    - _Requirements: 2.4, 2.5_

  - [ ]* 11.4 编写软删除属性测试
    - **Property 5: 软删除数据保留**
    - 测试: 对于任意已发布物品，删除后数据库中deleted字段应为1
    - 测试: 对于任意已删除物品，直接查询数据库应能找到原始数据
    - 测试: 对于任意已删除物品，通过API查询列表应不包含该物品
    - 配置最少100次迭代
    - **Validates: Requirements 2.5**

- [ ] 12. 物品详情查看
  - [x] 12.1 实现物品详情Service





    - 在ItemServiceImpl中实现getDetail(Long id):
      - 查询物品信息（包含关联的图片和标签），不存在或已删除则抛出NotFoundException
      - 使用Redis原子操作增加浏览次数: INCR item:view:{id}
      - 定时任务批量同步浏览次数到数据库（或直接更新）
      - 查询发布者信息
      - 调用MatchService获取匹配推荐列表（前10条）
      - 组装ItemDetailVO返回
    - _Requirements: 2.6, 5.3_

  - [x] 12.2 实现物品详情Controller









    - 在ItemController中实现GET /{id}: 获取物品详情
    - 返回Result<ItemDetailVO>
    - _Requirements: 2.6_

  - [ ]* 12.3 编写浏览计数属性测试
    - **Property 6: 浏览计数单调递增**
    - 测试: 对于任意物品，每次调用getDetail后viewCount应比之前增加1
    - 测试: 对于任意物品，连续N次调用getDetail后viewCount应增加N
    - 配置最少100次迭代
    - **Validates: Requirements 2.6**

- [x] 13. Checkpoint - 物品信息模块验证





  - 运行`mvn clean verify`确保所有测试通过
  - 测试物品发布、编辑、删除、详情查看接口
  - 验证软删除功能和浏览计数功能
  - 确保所有测试通过，如有问题请询问用户

## 第五阶段：AI图像识别模块

- [-] 14. 阿里云视觉智能图像识别集成



  - [x] 14.1 配置阿里云视觉智能SDK


    - 添加aliyun-java-sdk-core和aliyun-java-sdk-imagerecog依赖到pom.xml
    - 在application.yml中添加阿里云视觉智能配置（accessKeyId, accessKeySecret, regionId从环境变量读取，复用OSS的accessKey）
    - 创建`config/AliyunVisionConfig.java`:
      - 使用@ConfigurationProperties读取配置
      - 创建IAcsClient Bean（阿里云SDK客户端）
    - _Requirements: 3.1_



  - [x] 14.2 实现图像识别Service



    - 创建`service/ImageRecognitionService.java`接口:
      - RecognitionResult recognize(String imageUrl)
      - List<String> extractTags(String imageUrl)
      - String getCategory(String imageUrl)
    - 创建`model/vo/RecognitionResult.java`: category, confidence, tags(List<TagInfo>), rawResponse
    - 创建`model/vo/TagInfo.java`: tag, confidence
    - 创建`service/impl/ImageRecognitionServiceImpl.java`:
      - 实现recognize(): 调用阿里云视觉智能图像标签API（TaggingImage）
        - 设置超时时间5秒
        - 可直接使用OSS图片URL，无需额外传输
        - 解析返回结果，提取物品类别和置信度
        - 如果调用失败，记录错误日志，返回空结果（降级处理）
      - 实现extractTags(): 从识别结果提取特征标签（颜色、品牌、型号等）
      - 实现getCategory(): 根据识别结果映射到系统预定义类别
        - 预定义类别: 电子设备、证件卡片、钥匙、钱包、书籍文具、衣物配饰、运动器材、其他


    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 14.3 集成图像识别到物品发布流程



    - 修改ItemServiceImpl.publish()方法:
      - 发布成功后，使用@Async异步调用图像识别
      - 对第一张图片调用ImageRecognitionService.recognize()
      - 将识别出的category更新到item表


      - 将识别出的tags批量插入item_tag表
      - 如果识别失败，保留用户手动选择的category
    - _Requirements: 2.2, 3.1_

  - [-] 14.4 实现手动类别选择接口



    - 在ItemController中实现PUT /{id}/category: 手动更新物品类别
    - 用于AI识别失败或识别不准确时的降级方案
    - _Requirements: 3.4_

## 第六阶段：LBS地理服务模块

- [ ] 15. 高德地图API集成
  - [x] 15.1 配置高德地图SDK



    - 在application.yml中添加高德地图配置（key从环境变量读取）
    - 创建`config/AmapConfig.java`:
      - 使用@ConfigurationProperties读取配置
      - 创建RestTemplate Bean用于调用高德API
    - _Requirements: 4.1_

  - [ ] 15.2 实现地理位置Service
    - 创建`service/LocationService.java`接口:
      - GeoPoint geocode(String address): 地址转坐标
      - String reverseGeocode(Double lng, Double lat): 坐标转地址
      - Double calculateDistance(GeoPoint p1, GeoPoint p2): 计算两点距离（米）
      - List<Long> searchInRadius(Double lng, Double lat, Integer radius): 范围查询物品ID
    - 创建`model/vo/GeoPoint.java`: longitude, latitude, address
    - 创建`service/impl/LocationServiceImpl.java`:
      - 实现geocode(): 调用高德地理编码API
        - 设置超时时间3秒，重试2次
        - 解析返回结果提取经纬度
      - 实现reverseGeocode(): 调用高德逆地理编码API
        - 解析返回结果提取格式化地址
      - 实现calculateDistance(): 使用Haversine公式计算球面距离
      - 实现searchInRadius(): 
        - 使用MySQL空间函数ST_Distance_Sphere查询范围内的物品
        - 或使用Redis GEO功能实现（GEORADIUS）
    - _Requirements: 4.1, 4.3_

  - [ ] 15.3 实现附近物品搜索
    - 在ItemServiceImpl中实现getNearby(Double lng, Double lat, Integer radius):
      - 调用LocationService.searchInRadius()获取范围内物品ID
      - 批量查询物品信息
      - 计算每个物品与中心点的距离
      - 按距离升序排序
      - 返回List<ItemVO>（包含distance字段）
    - 在ItemController中实现GET /nearby:
      - 参数: lng, lat, radius(默认1000米)
      - 返回Result<List<ItemVO>>
    - _Requirements: 4.3_

  - [ ]* 15.4 编写地理范围查询属性测试
    - **Property 8: 地理范围查询准确性**
    - 测试: 对于任意中心点和半径，返回的所有物品距离中心点应<=radius
    - 测试: 对于任意中心点和半径，范围外的物品不应出现在结果中
    - 使用随机生成的经纬度坐标进行测试
    - 配置最少100次迭代
    - **Validates: Requirements 4.3**

## 第七阶段：搜索与筛选模块

- [ ] 16. 全文搜索与筛选
  - [ ] 16.1 实现搜索Service
    - 在ItemServiceImpl中实现search(ItemSearchDTO dto):
      - 构建MyBatis-Plus QueryWrapper:
        - deleted = 0（排除已删除）
        - 如果keyword不为空: title LIKE %keyword% OR description LIKE %keyword% OR 存在匹配的tag
        - 如果type不为空: type = dto.type
        - 如果category不为空: category = dto.category
        - 如果status不为空: status = dto.status
        - 如果startTime不为空: event_time >= startTime
        - 如果endTime不为空: event_time <= endTime
        - 如果有地理范围: 使用子查询筛选范围内的物品ID
      - 根据sortBy设置排序:
        - time: ORDER BY created_at DESC
        - distance: 需要计算距离后排序（如果提供了lng/lat）
        - match: 预留，后续实现匹配度排序
      - 使用MyBatis-Plus分页插件进行分页
      - 返回PageResult<ItemVO>
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ] 16.2 实现搜索Controller
    - 在ItemController中实现GET /: 搜索物品列表
      - 使用@ModelAttribute接收ItemSearchDTO
      - 调用ItemService.search()
      - 返回Result<PageResult<ItemVO>>
    - 在ItemController中实现GET /hot: 获取热门物品（按浏览量排序）
      - 返回最近7天浏览量最高的20条记录
    - _Requirements: 8.1, 8.5_

  - [ ]* 16.3 编写搜索结果属性测试
    - **Property 16: 搜索结果相关性**
    - 测试: 对于任意关键词搜索，返回的所有物品的title或description或tags应包含该关键词
    - 配置最少100次迭代
    - **Validates: Requirements 8.1**

  - [ ]* 16.4 编写筛选条件属性测试
    - **Property 17: 筛选条件准确性**
    - 测试: 对于任意type筛选，返回的所有物品type应等于筛选值
    - 测试: 对于任意category筛选，返回的所有物品category应等于筛选值
    - 测试: 对于任意时间范围筛选，返回的所有物品eventTime应在范围内
    - 配置最少100次迭代
    - **Validates: Requirements 8.2**

  - [ ]* 16.5 编写分页属性测试
    - **Property 18: 分页大小限制**
    - 测试: 对于任意分页查询，返回的记录数应<=pageSize
    - 测试: 对于任意pageSize=20的查询，返回的记录数应<=20
    - 配置最少100次迭代
    - **Validates: Requirements 8.4**

- [ ] 17. Checkpoint - 搜索模块验证
  - 运行`mvn clean verify`确保所有测试通过
  - 测试关键词搜索、多条件筛选、分页功能
  - 测试附近物品搜索功能
  - 确保所有测试通过，如有问题请询问用户

## 第八阶段：智能匹配模块

- [ ] 18. 匹配算法实现
  - [ ] 18.1 设计匹配分数计算
    - 创建`service/MatchService.java`接口:
      - List<MatchResult> calculateMatch(Item item): 计算匹配
      - Result<List<MatchVO>> getRecommendations(Long itemId): 获取推荐
      - Result<Void> confirmMatch(Long itemId, Long matchedItemId, Long userId): 确认匹配
      - Result<Void> feedback(MatchFeedbackDTO dto): 反馈匹配结果
    - 创建`model/vo/MatchResult.java`: itemId, matchedItemId, score, categoryScore, tagScore, timeScore, locationScore
    - 创建`model/vo/MatchVO.java`: 继承ItemVO，增加matchScore
    - _Requirements: 5.1_

  - [ ] 18.2 实现匹配分数计算Service
    - 创建`service/impl/MatchServiceImpl.java`
    - 实现calculateMatch(Item item):
      - 查询相反类型的物品列表（失物找招领，招领找失物）
      - 只查询status=0(待处理)且deleted=0的物品
      - 对每个候选物品计算匹配分数:
        - categoryScore(30%): 类别相同得30分，否则0分
        - tagScore(30%): 使用Jaccard相似度计算标签重合度 * 30
        - timeScore(20%): 时间差越小分数越高，超过7天得0分
          - score = max(0, 20 - (daysDiff * 20 / 7))
        - locationScore(20%): 距离越近分数越高，超过5km得0分
          - score = max(0, 20 - (distanceKm * 20 / 5))
      - 总分 = categoryScore + tagScore + timeScore + locationScore
      - 返回所有匹配结果，按分数降序排序
    - _Requirements: 5.1_

  - [ ]* 18.3 编写匹配分数计算属性测试
    - **Property 9: 匹配分数计算一致性**
    - 测试: 对于任意两个物品，多次计算匹配分数应返回相同结果
    - 测试: 对于任意匹配分数，应在0-100范围内
    - 测试: 对于类别相同的物品，categoryScore应为30
    - 配置最少100次迭代
    - **Validates: Requirements 5.1**

  - [ ] 18.4 实现匹配推荐Service
    - 在MatchServiceImpl中实现getRecommendations(Long itemId):
      - 查询物品信息
      - 调用calculateMatch()获取匹配结果
      - 取分数最高的前10条
      - 查询对应的物品详情
      - 组装MatchVO列表返回
    - _Requirements: 5.3_

  - [ ]* 18.5 编写匹配推荐排序属性测试
    - **Property 10: 匹配推荐排序正确性**
    - 测试: 对于任意物品的匹配推荐列表，结果应按matchScore降序排列
    - 测试: 对于任意物品的匹配推荐列表，结果数量应<=10
    - 配置最少100次迭代
    - **Validates: Requirements 5.3**

- [ ] 19. 匹配确认与通知
  - [ ] 19.1 实现匹配确认Service
    - 在MatchServiceImpl中实现confirmMatch(Long itemId, Long matchedItemId, Long userId):
      - 验证当前用户是itemId或matchedItemId的发布者
      - 查询两个物品信息，验证状态为待处理
      - 创建MatchRecord记录: lost_item_id, found_item_id, score, status=1(已确认), confirmed_at
      - 更新两个物品的status为1(已找回)
      - 为招领信息的发布者增加50积分（帮助找回物品奖励）
      - 向双方发送匹配成功的站内消息
    - _Requirements: 5.4, 7.1_

  - [ ]* 19.2 编写匹配确认状态属性测试
    - **Property 11: 匹配确认状态同步**
    - 测试: 对于任意匹配确认操作，两个物品的status应同时变为1(已找回)
    - 测试: 对于任意匹配确认操作，应创建一条MatchRecord记录
    - 测试: 对于任意匹配确认操作，招领方用户积分应增加50
    - 配置最少100次迭代
    - **Validates: Requirements 5.4**

  - [ ] 19.3 实现自动匹配触发
    - 修改ItemServiceImpl.publish()方法:
      - 发布成功后，异步调用MatchService.calculateMatch()
      - 筛选匹配分数>=70的结果
      - 对每个高分匹配，向双方用户发送匹配通知消息
      - 消息内容包含匹配物品的简要信息和链接
    - _Requirements: 5.2_

  - [ ] 19.4 实现匹配反馈Service
    - 在MatchServiceImpl中实现feedback(MatchFeedbackDTO dto):
      - 创建`model/dto/MatchFeedbackDTO.java`: itemId, matchedItemId, isAccurate(boolean), comment
      - 记录反馈数据到数据库（可用于后续优化算法）
      - 如果反馈不准确，可考虑降低该类型匹配的权重
    - _Requirements: 5.5_

  - [ ] 19.5 实现匹配Controller
    - 创建`controller/MatchController.java`，使用@RequestMapping("/api/v1/matches")
    - 实现GET /recommendations/{itemId}: 获取匹配推荐列表
    - 实现POST /confirm: 确认匹配
      - 参数: itemId, matchedItemId
    - 实现POST /feedback: 提交匹配反馈
    - _Requirements: 5.3, 5.4, 5.5_

## 第九阶段：消息通知模块

- [ ] 20. 站内消息功能
  - [ ] 20.1 创建消息相关DTO和VO
    - 创建`model/vo/MessageVO.java`: id, title, content, type, typeName, relatedId, isRead, createdAt
    - 创建`model/dto/SendMessageDTO.java`: userId, title, content, type, relatedId
    - 消息类型枚举: 0-系统通知, 1-匹配通知, 2-留言通知
    - _Requirements: 6.1, 6.2_

  - [ ] 20.2 实现消息Service
    - 创建`service/MessageService.java`接口:
      - void send(SendMessageDTO dto): 发送消息
      - void sendBatch(List<SendMessageDTO> dtos): 批量发送
      - PageResult<MessageVO> getList(Long userId, Integer type, Boolean isRead, Integer pageNum, Integer pageSize): 获取消息列表
      - void markAsRead(Long messageId, Long userId): 标记已读
      - void markAllAsRead(Long userId): 全部标记已读
      - Integer getUnreadCount(Long userId): 获取未读数量
    - 创建`service/impl/MessageServiceImpl.java`:
      - 实现send(): 创建Message记录，isRead=0
      - 实现getList(): 
        - 查询条件: user_id = userId
        - 可选筛选: type, is_read
        - 排序: ORDER BY created_at DESC
        - 分页返回
      - 实现markAsRead(): 更新is_read=1，验证消息属于当前用户
      - 实现getUnreadCount(): COUNT(*) WHERE user_id=? AND is_read=0
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ]* 20.3 编写消息列表排序属性测试
    - **Property 12: 消息列表时间排序**
    - 测试: 对于任意用户的消息列表，结果应按createdAt降序排列
    - 测试: 对于任意两条相邻消息，前一条的createdAt应>=后一条
    - 配置最少100次迭代
    - **Validates: Requirements 6.3**

  - [ ]* 20.4 编写消息已读状态属性测试
    - **Property 13: 消息已读状态更新**
    - 测试: 对于任意未读消息，调用markAsRead后isRead应变为true
    - 测试: 对于任意已读消息，再次调用markAsRead不应报错
    - 配置最少100次迭代
    - **Validates: Requirements 6.4**

  - [ ] 20.5 实现消息Controller
    - 创建`controller/MessageController.java`，使用@RequestMapping("/api/v1/messages")
    - 实现GET /: 获取消息列表
      - 参数: type(可选), isRead(可选), pageNum, pageSize
    - 实现GET /unread-count: 获取未读消息数量
    - 实现PUT /{id}/read: 标记单条消息已读
    - 实现PUT /read-all: 标记全部已读
    - _Requirements: 6.3, 6.4_

- [ ] 21. WebSocket实时推送
  - [ ] 21.1 配置WebSocket
    - 创建`config/WebSocketConfig.java`:
      - 实现WebSocketConfigurer接口
      - 注册WebSocket端点: /ws/messages
      - 配置允许的来源（CORS）
    - _Requirements: 6.5_

  - [ ] 21.2 实现WebSocket处理器
    - 创建`websocket/MessageWebSocket.java`:
      - 使用@ServerEndpoint("/ws/messages/{token}")
      - 维护用户ID到Session的映射: ConcurrentHashMap<Long, Session>
      - 实现@OnOpen: 验证token，提取userId，保存Session映射
      - 实现@OnClose: 移除Session映射
      - 实现@OnError: 记录错误日志
      - 实现sendToUser(Long userId, String message): 向指定用户推送消息
    - _Requirements: 6.5_

  - [ ] 21.3 集成WebSocket到消息发送流程
    - 修改MessageServiceImpl.send()方法:
      - 保存消息到数据库后
      - 检查用户是否有活跃的WebSocket连接
      - 如果有，通过WebSocket实时推送消息内容
    - _Requirements: 6.5_

- [ ] 22. Checkpoint - 消息模块验证
  - 运行`mvn clean verify`确保所有测试通过
  - 测试消息发送、列表查询、标记已读功能
  - 测试WebSocket连接和实时推送功能
  - 确保所有测试通过，如有问题请询问用户

## 第十阶段：积分模块

- [ ] 23. 积分服务实现
  - [ ] 23.1 创建积分相关DTO和VO
    - 创建`model/vo/PointRecordVO.java`: id, points, reason, reasonDesc, relatedId, createdAt
    - 创建`model/vo/PointRankVO.java`: rank, userId, userName, userAvatar, points
    - 积分原因枚举: DAILY_LOGIN(每日登录+2), PUBLISH_FOUND(发布招领+10), HELP_FIND(帮助找回+50)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [ ] 23.2 实现积分Service
    - 创建`service/PointService.java`接口:
      - void addPoints(Long userId, Integer points, String reason, Long relatedId): 增加积分
      - PageResult<PointRecordVO> getRecords(Long userId, Integer pageNum, Integer pageSize): 获取积分明细
      - List<PointRankVO> getRanking(Integer limit): 获取积分排行榜
      - Integer getTotalPoints(Long userId): 获取用户总积分
    - 创建`service/impl/PointServiceImpl.java`:
      - 实现addPoints():
        - 创建PointRecord记录
        - 更新User表的points字段（使用乐观锁或原子操作）
        - 使用@Transactional确保事务一致性
      - 实现getRecords():
        - 查询条件: user_id = userId
        - 排序: ORDER BY created_at DESC
        - 分页返回
      - 实现getRanking():
        - 查询: SELECT id, name, avatar, points FROM user WHERE status=0 ORDER BY points DESC LIMIT ?
        - 添加排名序号
        - 可使用Redis ZSET缓存排行榜数据
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [ ]* 23.3 编写积分奖励属性测试
    - **Property 14: 积分奖励正确性**
    - 测试: 对于任意用户发布招领信息，积分应增加10
    - 测试: 对于任意用户帮助找回物品，积分应增加50
    - 测试: 对于任意用户每日首次登录，积分应增加2
    - 测试: 对于任意积分变动，应创建对应的PointRecord记录
    - 配置最少100次迭代
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4**

  - [ ]* 23.4 编写积分排行榜属性测试
    - **Property 15: 积分排行榜排序正确性**
    - 测试: 对于任意排行榜查询，结果应按points降序排列
    - 测试: 对于任意排行榜查询(limit=100)，结果数量应<=100
    - 测试: 对于任意两条相邻排名，前一名的points应>=后一名
    - 配置最少100次迭代
    - **Validates: Requirements 7.5**

  - [ ] 23.5 实现积分Controller
    - 创建`controller/PointController.java`，使用@RequestMapping("/api/v1/points")
    - 实现GET /: 获取当前用户积分明细
      - 参数: pageNum, pageSize
    - 实现GET /total: 获取当前用户总积分
    - 实现GET /ranking: 获取积分排行榜
      - 参数: limit(默认100，最大100)
    - _Requirements: 7.4, 7.5_

## 第十一阶段：管理后台模块

- [ ] 24. 数据统计功能
  - [ ] 24.1 创建统计相关VO
    - 创建`model/vo/StatisticsVO.java`:
      - totalUsers: 用户总数
      - totalItems: 信息总数
      - totalLostItems: 失物信息数
      - totalFoundItems: 招领信息数
      - totalMatched: 匹配成功数
      - matchRate: 匹配成功率
      - todayNewUsers: 今日新增用户
      - todayNewItems: 今日新增信息
      - weeklyTrend: 近7天数据趋势(List<DailyStatVO>)
    - 创建`model/vo/DailyStatVO.java`: date, newUsers, newItems, matchedCount
    - _Requirements: 9.1_

  - [ ] 24.2 实现管理统计Service
    - 创建`service/AdminService.java`接口
    - 创建`service/impl/AdminServiceImpl.java`
    - 实现getStatistics():
      - 查询用户总数: COUNT(*) FROM user WHERE status=0
      - 查询信息总数: COUNT(*) FROM item WHERE deleted=0
      - 查询失物/招领数: 按type分组统计
      - 查询匹配成功数: COUNT(*) FROM match_record WHERE status=1
      - 计算匹配成功率: matchedCount * 2 / totalItems * 100%
      - 查询今日新增: created_at >= TODAY
      - 查询近7天趋势: GROUP BY DATE(created_at)
    - 可使用Redis缓存统计数据，设置5分钟过期
    - _Requirements: 9.1_

  - [ ] 24.3 实现统计Controller
    - 创建`controller/AdminController.java`，使用@RequestMapping("/api/v1/admin")
    - 使用@PreAuthorize("hasRole('ADMIN')")限制所有接口
    - 实现GET /statistics: 获取统计数据
    - _Requirements: 9.1_

- [ ] 25. 内容管理功能
  - [ ] 25.1 实现管理员物品列表Service
    - 在AdminServiceImpl中实现getItemList(ItemAdminSearchDTO dto):
      - 创建`model/dto/ItemAdminSearchDTO.java`: keyword, type, status, deleted, startTime, endTime, reportCount, pageNum, pageSize
      - 查询所有物品（包括已删除的）
      - 支持按状态、时间、举报次数筛选
      - 返回PageResult<ItemVO>（包含发布者信息）
    - _Requirements: 9.2_

  - [ ] 25.2 实现内容审核Service
    - 在AdminServiceImpl中实现reviewItem(Long itemId, Integer action, String reason):
      - action: 0-通过, 1-删除, 2-警告发布者
      - 通过: 清除举报标记
      - 删除: 设置deleted=1
      - 警告: 向发布者发送警告消息
      - 记录审核日志
    - _Requirements: 9.3_

  - [ ] 25.3 实现内容管理Controller
    - 在AdminController中实现GET /items: 管理员查看物品列表
    - 在AdminController中实现POST /items/{id}/review: 审核物品
      - 参数: action, reason
    - _Requirements: 9.2, 9.3_

- [ ] 26. 用户管理功能
  - [ ] 26.1 实现用户封禁Service
    - 在AdminServiceImpl中实现banUser(Long userId, String reason):
      - 更新用户status=1(封禁)
      - 向用户发送封禁通知消息
      - 记录封禁日志
    - 在AdminServiceImpl中实现unbanUser(Long userId):
      - 更新用户status=0(正常)
      - 向用户发送解封通知消息
    - _Requirements: 9.4_

  - [ ]* 26.2 编写用户封禁属性测试
    - **Property 19: 用户封禁生效性**
    - 测试: 对于任意被封禁用户，使用其凭据登录应返回403错误
    - 测试: 对于任意被封禁用户，其status应为1
    - 测试: 对于任意解封用户，使用其凭据登录应成功
    - 配置最少100次迭代
    - **Validates: Requirements 9.4**

  - [ ] 26.3 实现用户管理Controller
    - 在AdminController中实现POST /users/{id}/ban: 封禁用户
      - 参数: reason
    - 在AdminController中实现POST /users/{id}/unban: 解封用户
    - _Requirements: 9.4_

- [ ] 27. 数据导出功能
  - [ ] 27.1 实现Excel导出Service
    - 添加Apache POI或EasyExcel依赖
    - 在AdminServiceImpl中实现exportReport(LocalDate startDate, LocalDate endDate):
      - 查询指定时间范围内的统计数据
      - 创建Excel工作簿
      - Sheet1: 概览数据（用户数、信息数、匹配率等）
      - Sheet2: 每日明细数据
      - Sheet3: 物品分类统计
      - 返回字节数组或文件路径
    - _Requirements: 9.5_

  - [ ] 27.2 实现导出Controller
    - 在AdminController中实现GET /export: 导出数据报表
      - 参数: startDate, endDate
      - 返回Excel文件下载
    - _Requirements: 9.5_

- [ ] 28. Checkpoint - 管理后台验证
  - 运行`mvn clean verify`确保所有测试通过
  - 测试统计数据、内容管理、用户管理、数据导出功能
  - 验证管理员权限控制
  - 确保所有测试通过，如有问题请询问用户

## 第十二阶段：系统优化

- [ ] 29. Redis缓存优化
  - [ ] 29.1 实现热点数据缓存
    - 缓存物品详情: key=`item:detail:{id}`, 过期时间30分钟
    - 缓存用户信息: key=`user:info:{id}`, 过期时间1小时
    - 缓存搜索结果: key=`item:search:{hash(params)}`, 过期时间5分钟
    - 缓存统计数据: key=`admin:statistics`, 过期时间5分钟
    - 缓存积分排行榜: key=`point:ranking`, 使用ZSET, 过期时间10分钟
    - _Requirements: 10.5_

  - [ ] 29.2 实现缓存更新策略
    - 物品更新/删除时: 删除对应缓存
    - 用户信息更新时: 删除对应缓存
    - 积分变动时: 更新ZSET中的分数
    - 使用@CacheEvict和@Cacheable注解简化缓存操作
    - _Requirements: 10.5_

- [ ] 30. 接口限流
  - [ ] 30.1 实现限流拦截器
    - 创建`config/RateLimitInterceptor.java`:
      - 实现HandlerInterceptor接口
      - 使用Redis滑动窗口算法实现限流
      - key=`rate:limit:{ip}`, 使用ZSET存储请求时间戳
      - 每次请求: ZADD当前时间戳, ZREMRANGEBYSCORE移除1分钟前的记录, ZCARD统计数量
      - 如果数量>100，抛出RateLimitException
    - 在WebMvcConfig中注册拦截器
    - _Requirements: 10.3_

  - [ ] 30.2 配置限流白名单
    - 静态资源路径不限流
    - 健康检查接口不限流
    - 可配置IP白名单
    - _Requirements: 10.3_

- [ ] 31. API文档
  - [ ] 31.1 配置Swagger/OpenAPI
    - 添加springdoc-openapi-starter-webmvc-ui依赖
    - 创建`config/SwaggerConfig.java`:
      - 配置API信息: 标题、描述、版本
      - 配置安全方案: Bearer JWT
      - 配置分组: 用户端API、管理端API
    - _Requirements: 所有接口_

  - [ ] 31.2 添加接口文档注解
    - 为所有Controller添加@Tag注解
    - 为所有接口方法添加@Operation注解: summary, description
    - 为所有参数添加@Parameter注解
    - 为所有DTO/VO字段添加@Schema注解
    - 配置响应示例
    - _Requirements: 所有接口_

## 第十三阶段：前端项目搭建

- [ ] 32. Vue 3项目初始化
  - [ ] 32.1 创建Vue项目
    - 在项目根目录创建web/目录
    - 使用`npm create vite@latest web -- --template vue-ts`创建项目
    - 安装核心依赖:
      - `npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue`
      - `npm install -D sass unplugin-vue-components unplugin-auto-import`
    - 配置vite.config.ts: 代理API请求到后端、自动导入Element Plus组件
    - _Requirements: 所有前端需求_

  - [ ] 32.2 配置项目结构
    - 创建目录结构:
      - src/api/: API请求模块
      - src/components/: 公共组件
      - src/views/: 页面组件
      - src/stores/: Pinia状态管理
      - src/router/: 路由配置
      - src/utils/: 工具函数
      - src/assets/: 静态资源
      - src/types/: TypeScript类型定义
    - 创建src/types/index.ts: 定义所有接口响应类型
    - _Requirements: 所有前端需求_

  - [ ] 32.3 封装Axios请求
    - 创建src/utils/request.ts:
      - 创建axios实例，配置baseURL
      - 请求拦截器: 自动添加Authorization头（从localStorage读取token）
      - 响应拦截器: 
        - 统一处理错误响应
        - 401错误跳转登录页
        - 显示错误提示（Element Plus Message）
      - 封装get, post, put, delete方法
    - 创建src/api/目录下各模块API文件:
      - auth.ts: 登录、注册、刷新令牌
      - user.ts: 用户信息
      - item.ts: 物品信息CRUD
      - match.ts: 匹配相关
      - message.ts: 消息相关
      - point.ts: 积分相关
      - file.ts: 文件上传
    - _Requirements: 10.1_

- [ ] 33. 路由和布局
  - [ ] 33.1 配置Vue Router
    - 创建src/router/index.ts:
      - 配置路由表:
        - /login: 登录页
        - /register: 注册页
        - /: 首页（物品列表）
        - /item/:id: 物品详情
        - /publish: 发布物品
        - /my/items: 我的发布
        - /my/profile: 个人中心
        - /messages: 消息中心
        - /points: 积分中心
        - /admin/*: 管理后台（需要管理员权限）
      - 配置路由守卫:
        - 检查是否需要登录
        - 检查是否需要管理员权限
        - 未登录跳转登录页
    - _Requirements: 1.2_

  - [ ] 33.2 创建布局组件
    - 创建src/components/layout/MainLayout.vue:
      - 顶部导航栏: Logo、搜索框、导航菜单、用户信息/登录按钮
      - 主内容区域: <router-view>
      - 底部版权信息
    - 创建src/components/layout/AdminLayout.vue:
      - 左侧菜单: 数据统计、内容管理、用户管理
      - 顶部面包屑导航
      - 主内容区域
    - _Requirements: 所有前端需求_

  - [ ] 33.3 配置Pinia状态管理
    - 创建src/stores/user.ts:
      - state: token, userInfo, isLoggedIn
      - actions: login, logout, refreshToken, fetchUserInfo
      - 持久化token到localStorage
    - 创建src/stores/app.ts:
      - state: loading, unreadMessageCount
      - actions: setLoading, fetchUnreadCount
    - _Requirements: 1.2, 6.3_

## 第十四阶段：前端用户模块

- [ ] 34. 登录注册页面
  - [ ] 34.1 实现登录页面
    - 创建src/views/auth/LoginView.vue:
      - 表单字段: 学号/工号、密码、记住我
      - 使用Element Plus Form组件和表单验证
      - 提交时调用登录API
      - 登录成功后保存token，跳转首页
      - 显示登录失败错误信息
      - 提供注册页面链接
    - _Requirements: 1.2_

  - [ ] 34.2 实现注册页面
    - 创建src/views/auth/RegisterView.vue:
      - 表单字段: 学号/工号、姓名、手机号、密码、确认密码
      - 表单验证: 学号格式、手机号格式、密码长度、两次密码一致
      - 提交时调用注册API
      - 注册成功后提示并跳转登录页
      - 提供登录页面链接
    - _Requirements: 1.1_

- [ ] 35. 个人中心页面
  - [ ] 35.1 实现个人中心页面
    - 创建src/views/user/ProfileView.vue:
      - 显示用户信息: 头像、学号、姓名、手机号、积分
      - 头像上传组件: 点击上传，预览，调用文件上传API
      - 编辑表单: 姓名、手机号
      - 修改密码表单: 旧密码、新密码、确认新密码
      - 保存时调用更新API
    - _Requirements: 1.3_

  - [ ] 35.2 实现我的发布页面
    - 创建src/views/user/MyItemsView.vue:
      - Tab切换: 我的失物、我的招领
      - 物品列表: 卡片形式展示，显示标题、图片、状态、时间
      - 状态筛选: 全部、待处理、已找回、已关闭
      - 操作按钮: 编辑、删除、查看详情
      - 分页组件
    - _Requirements: 2.4, 2.5_

## 第十五阶段：前端物品信息模块

- [ ] 36. 物品发布页面
  - [ ] 36.1 实现发布页面
    - 创建src/views/item/PublishView.vue:
      - 类型选择: 失物/招领（Radio按钮）
      - 表单字段:
        - 标题: Input
        - 描述: Textarea
        - 物品类别: Select（预定义类别 + AI识别结果）
        - 图片上传: Upload组件，最多9张，支持拖拽
        - 丢失/拾获时间: DateTimePicker
        - 地点选择: 集成高德地图选点组件
      - 图片上传后自动调用AI识别，显示识别结果
      - 表单验证
      - 提交时调用发布API
      - 发布成功后跳转详情页
    - _Requirements: 2.1, 2.2, 2.3, 4.1_

  - [ ] 36.2 实现地图选点组件
    - 创建src/components/map/LocationPicker.vue:
      - 集成高德地图JS API
      - 显示地图，支持点击选点
      - 显示当前选中位置的地址
      - 支持搜索地址定位
      - 支持获取当前位置
      - 双向绑定: v-model返回{lng, lat, address}
    - _Requirements: 4.1_

- [ ] 37. 物品列表页面
  - [ ] 37.1 实现首页物品列表
    - 创建src/views/item/ItemListView.vue:
      - 搜索栏: 关键词输入、搜索按钮
      - 筛选栏: 类型、类别、状态、时间范围
      - 排序选择: 最新发布、距离最近
      - 视图切换: 列表视图、地图视图
      - 列表视图: 卡片网格展示物品
      - 地图视图: 高德地图显示物品位置标记
      - 分页组件
    - _Requirements: 4.2, 8.1, 8.2, 8.3, 8.4_

  - [ ] 37.2 实现物品卡片组件
    - 创建src/components/item/ItemCard.vue:
      - 显示: 首图、标题、类别标签、地点、时间、状态
      - 失物/招领类型标识
      - 点击跳转详情页
    - _Requirements: 2.6_

  - [ ] 37.3 实现地图展示组件
    - 创建src/components/map/ItemMap.vue:
      - 集成高德地图
      - 接收物品列表，在地图上显示标记点
      - 不同类型使用不同颜色标记
      - 点击标记显示物品信息弹窗
      - 弹窗包含: 图片、标题、地点、查看详情按钮
    - _Requirements: 4.2, 4.4_

- [ ] 38. 物品详情页面
  - [ ] 38.1 实现详情页面
    - 创建src/views/item/ItemDetailView.vue:
      - 图片轮播: Element Plus Carousel组件
      - 基本信息: 标题、类型标签、类别、状态
      - 描述内容
      - 时间地点: 丢失/拾获时间、地点（带地图展示）
      - 发布者信息: 头像、姓名
      - AI识别标签展示
      - 浏览次数显示
      - 操作按钮: 联系发布者、确认匹配（如果是匹配推荐跳转来的）
    - _Requirements: 2.6, 5.3_

  - [ ] 38.2 实现匹配推荐列表
    - 在ItemDetailView中添加匹配推荐区域:
      - 标题: "可能匹配的物品"
      - 列表展示匹配推荐（最多10条）
      - 显示匹配度分数
      - 点击跳转对应物品详情
      - 确认匹配按钮
    - _Requirements: 5.3, 5.4_

  - [ ] 38.3 实现匹配确认弹窗
    - 创建src/components/match/ConfirmMatchDialog.vue:
      - 显示两个物品的对比信息
      - 确认按钮: 调用确认匹配API
      - 确认成功后显示成功提示，刷新页面状态
    - _Requirements: 5.4_

- [ ] 39. Checkpoint - 前端物品模块验证
  - 运行`npm run build`确保前端构建成功
  - 测试物品发布、列表、详情、匹配确认功能
  - 测试地图选点和展示功能
  - 确保所有功能正常，如有问题请询问用户

## 第十六阶段：前端消息和积分模块

- [ ] 40. 消息中心页面
  - [ ] 40.1 实现消息列表页面
    - 创建src/views/message/MessageView.vue:
      - Tab切换: 全部、系统通知、匹配通知、留言通知
      - 消息列表: 显示标题、内容摘要、时间、已读状态
      - 未读消息高亮显示
      - 点击消息: 标记已读，跳转相关页面
      - 全部已读按钮
      - 分页组件
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ] 40.2 实现WebSocket实时接收
    - 创建src/utils/websocket.ts:
      - 封装WebSocket连接管理
      - 连接时携带token
      - 自动重连机制
      - 消息接收回调
    - 在App.vue中初始化WebSocket连接
    - 收到新消息时:
      - 更新未读消息数量
      - 显示桌面通知（如果用户授权）
      - 显示Element Plus Notification提示
    - _Requirements: 6.5_

  - [ ] 40.3 实现消息提醒组件
    - 创建src/components/message/MessageBadge.vue:
      - 显示在导航栏的消息图标
      - 显示未读消息数量角标
      - 点击跳转消息中心
    - _Requirements: 6.3_

- [ ] 41. 积分中心页面
  - [ ] 41.1 实现积分明细页面
    - 创建src/views/point/PointView.vue:
      - 顶部显示: 当前总积分、积分等级（可选）
      - 积分规则说明卡片
      - 积分明细列表:
        - 显示: 积分变动(+/-)、原因、时间
        - 不同类型使用不同颜色
      - 分页组件
    - _Requirements: 7.4_

  - [ ] 41.2 实现积分排行榜页面
    - 创建src/views/point/RankingView.vue:
      - 排行榜列表: 排名、头像、姓名、积分
      - 前三名特殊样式（金银铜）
      - 当前用户排名高亮显示
      - 显示前100名
    - _Requirements: 7.5_

## 第十七阶段：前端管理后台

- [ ] 42. 管理后台布局
  - [ ] 42.1 实现管理后台布局
    - 创建src/views/admin/AdminLayout.vue:
      - 左侧菜单:
        - 数据统计
        - 内容管理 > 物品列表、举报处理
        - 用户管理 > 用户列表
        - 数据导出
      - 顶部: 面包屑导航、管理员信息、退出按钮
      - 主内容区域
    - 配置管理后台路由（嵌套路由）
    - _Requirements: 9.1_

- [ ] 43. 数据统计页面
  - [ ] 43.1 实现数据统计仪表盘
    - 创建src/views/admin/DashboardView.vue:
      - 安装ECharts: `npm install echarts vue-echarts`
      - 统计卡片: 用户总数、信息总数、匹配成功数、匹配成功率
      - 今日数据: 新增用户、新增信息
      - 折线图: 近7天数据趋势（用户增长、信息增长、匹配数）
      - 饼图: 物品类别分布
      - 柱状图: 失物/招领比例
    - _Requirements: 9.1_

- [ ] 44. 内容管理页面
  - [ ] 44.1 实现物品管理列表
    - 创建src/views/admin/ItemManageView.vue:
      - 搜索筛选: 关键词、类型、状态、是否删除、时间范围
      - 表格展示: ID、标题、类型、类别、状态、发布者、发布时间、操作
      - 操作按钮: 查看详情、删除、恢复（已删除的）
      - 分页组件
    - _Requirements: 9.2_

  - [ ] 44.2 实现内容审核功能
    - 创建src/views/admin/ReviewView.vue:
      - 显示被举报的物品列表
      - 审核操作: 通过、删除、警告发布者
      - 审核时需要填写原因
      - 审核后刷新列表
    - _Requirements: 9.3_

- [ ] 45. 用户管理页面
  - [ ] 45.1 实现用户管理列表
    - 创建src/views/admin/UserManageView.vue:
      - 搜索筛选: 学号、姓名、状态
      - 表格展示: ID、学号、姓名、手机号、积分、状态、注册时间、操作
      - 操作按钮: 查看详情、封禁/解封
      - 封禁时需要填写原因
      - 分页组件
    - _Requirements: 9.4_

- [ ] 46. 数据导出页面
  - [ ] 46.1 实现数据导出功能
    - 创建src/views/admin/ExportView.vue:
      - 时间范围选择: 开始日期、结束日期
      - 导出按钮: 调用导出API，下载Excel文件
      - 显示导出说明: 包含哪些数据
    - _Requirements: 9.5_

## 第十八阶段：最终集成与测试

- [ ] 47. 前后端集成测试
  - [ ] 47.1 配置生产环境
    - 后端: 创建application-prod.yml，配置生产环境数据库、Redis、OSS
    - 前端: 配置.env.production，设置API地址
    - 配置Nginx: 静态资源服务、API反向代理、HTTPS（可选）
    - _Requirements: 所有需求_

  - [ ] 47.2 端到端功能测试
    - 测试用户注册登录流程
    - 测试物品发布、编辑、删除流程
    - 测试搜索筛选功能
    - 测试智能匹配和确认流程
    - 测试消息通知功能
    - 测试积分获取和排行榜
    - 测试管理后台所有功能
    - _Requirements: 所有需求_

- [ ] 48. Final Checkpoint - 全面验证
  - 运行`mvn clean verify`确保后端所有测试通过
  - 运行`npm run build`确保前端构建成功
  - 进行完整的功能测试
  - 确保所有测试通过，如有问题请询问用户
