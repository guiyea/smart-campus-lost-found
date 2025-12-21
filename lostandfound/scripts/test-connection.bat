@echo off
REM =============================================
REM 测试MySQL连接 (Windows)
REM =============================================

echo ========================================
echo 测试MySQL数据库连接
echo ========================================
echo.

set MYSQL_PATH=mysql
set DB_USER=root
set DB_PASSWORD=40619128

echo 正在连接到MySQL服务器...
echo.

%MYSQL_PATH% -u%DB_USER% -p%DB_PASSWORD% -e "SELECT VERSION() AS MySQL版本, NOW() AS 当前时间;"

if %errorlevel% neq 0 (
    echo.
    echo ❌ 连接失败！
    echo.
    echo 可能的原因:
    echo 1. MySQL服务未启动
    echo    解决: 运行 'net start MySQL80' 或在服务管理器中启动MySQL
    echo.
    echo 2. 用户名或密码错误
    echo    当前配置: 用户=%DB_USER%, 密码=%DB_PASSWORD%
    echo.
    echo 3. MySQL未添加到系统PATH
    echo    解决: 添加MySQL的bin目录到系统PATH环境变量
    echo.
) else (
    echo.
    echo ✓ 连接成功！
    echo.
    echo 检查数据库是否存在...
    %MYSQL_PATH% -u%DB_USER% -p%DB_PASSWORD% -e "SHOW DATABASES LIKE 'campuslostandfound';"
    echo.
)

pause
