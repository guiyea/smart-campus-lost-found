# 智能校园失物招领平台 - 部署指南

## 目录

1. [环境要求](#环境要求)
2. [快速部署（Docker Compose）](#快速部署docker-compose)
3. [手动部署](#手动部署)
4. [SSL证书配置](#ssl证书配置)
5. [环境变量说明](#环境变量说明)
6. [常见问题](#常见问题)

## 环境要求

### 服务器要求
- CPU: 2核以上
- 内存: 4GB以上
- 磁盘: 50GB以上
- 操作系统: Ubuntu 20.04+ / CentOS 7+

### 软件要求
- Docker 20.10+
- Docker Compose 2.0+
- 或者手动安装:
  - JDK 17+
  - Node.js 18+
  - MySQL 8.0+
  - Redis 7.0+
  - Nginx 1.20+

## 快速部署（Docker Compose）

### 1. 克隆项目
```bash
git clone https://github.com/your-repo/campus-lostandfound.git
cd campus-lostandfound/lostandfound/deploy
```

### 2. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，填写实际配置值
vim .env
```

### 3. 准备SSL证书（可选）
```bash
mkdir -p ssl
# 将SSL证书文件放入ssl目录
# - fullchain.pem (证书链)
# - privkey.pem (私钥)
```

### 4. 启动服务
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 5. 初始化数据库
```bash
# 数据库会自动创建，如需导入初始数据：
docker exec -i lostandfound-mysql mysql -u root -p${DB_ROOT_PASSWORD} campuslostandfound < ../src/main/resources/schema.sql
```

### 6. 验证部署
```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端
curl http://localhost/
```

## 手动部署

### 后端部署

#### 1. 构建JAR包
```bash
cd lostandfound
mvn clean package -DskipTests -Pprod
```

#### 2. 配置环境变量
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=campuslostandfound
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_jwt_secret
# ... 其他环境变量
```

#### 3. 启动应用
```bash
java -jar target/lostandfound-0.0.1-SNAPSHOT.jar
```

#### 4. 使用systemd管理（推荐）
```bash
# 创建服务文件
sudo vim /etc/systemd/system/lostandfound.service
```

```ini
[Unit]
Description=Campus Lost and Found Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=spring
WorkingDirectory=/opt/lostandfound
ExecStart=/usr/bin/java -jar /opt/lostandfound/lostandfound.jar
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
EnvironmentFile=/opt/lostandfound/.env

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable lostandfound
sudo systemctl start lostandfound
```

### 前端部署

#### 1. 构建前端
```bash
cd web
npm install
npm run build
```

#### 2. 部署到Nginx
```bash
# 复制构建产物
sudo cp -r dist/* /var/www/lostandfound/

# 复制Nginx配置
sudo cp ../lostandfound/deploy/nginx.conf /etc/nginx/conf.d/lostandfound.conf

# 测试配置
sudo nginx -t

# 重载Nginx
sudo systemctl reload nginx
```

## SSL证书配置

### 使用Let's Encrypt（推荐）

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d campus-lostandfound.com -d www.campus-lostandfound.com -d api.campus-lostandfound.com

# 自动续期（已自动配置）
sudo certbot renew --dry-run
```

### 使用自签名证书（测试用）

```bash
cd lostandfound/deploy/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout privkey.pem \
  -out fullchain.pem \
  -subj "/CN=campus-lostandfound.com"
```

## 环境变量说明

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| DB_HOST | 数据库主机 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | campuslostandfound |
| DB_USERNAME | 数据库用户名 | lostandfound_user |
| DB_PASSWORD | 数据库密码 | your_password |
| REDIS_HOST | Redis主机 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| REDIS_PASSWORD | Redis密码 | your_redis_password |
| JWT_SECRET | JWT密钥（至少256位） | your_secret_key |
| ALIYUN_ACCESS_KEY_ID | 阿里云AccessKey ID | LTAI... |
| ALIYUN_ACCESS_KEY_SECRET | 阿里云AccessKey Secret | ... |
| OSS_ENDPOINT | OSS端点 | oss-cn-shanghai.aliyuncs.com |
| OSS_BUCKET_NAME | OSS存储桶名称 | your-bucket |
| OSS_BASE_URL | OSS访问URL | https://your-bucket.oss-cn-shanghai.aliyuncs.com |
| AMAP_KEY | 高德地图API Key | your_amap_key |

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 检查数据库用户权限
- 检查防火墙设置

### 2. Redis连接失败
- 检查Redis服务是否启动
- 检查Redis密码配置
- 检查Redis绑定地址

### 3. 文件上传失败
- 检查OSS配置是否正确
- 检查AccessKey权限
- 检查存储桶CORS配置

### 4. WebSocket连接失败
- 检查Nginx WebSocket代理配置
- 检查防火墙是否允许WebSocket连接
- 检查SSL证书配置

### 5. 前端页面404
- 检查Nginx配置中的try_files
- 确保Vue Router使用history模式
- 检查静态资源路径

## 监控与维护

### 日志查看
```bash
# Docker方式
docker-compose logs -f backend

# 手动部署
tail -f /var/log/lostandfound/application.log
```

### 数据库备份
```bash
# 备份
docker exec lostandfound-mysql mysqldump -u root -p${DB_ROOT_PASSWORD} campuslostandfound > backup_$(date +%Y%m%d).sql

# 恢复
docker exec -i lostandfound-mysql mysql -u root -p${DB_ROOT_PASSWORD} campuslostandfound < backup_20240101.sql
```

### 服务重启
```bash
# Docker方式
docker-compose restart backend

# 手动部署
sudo systemctl restart lostandfound
```
