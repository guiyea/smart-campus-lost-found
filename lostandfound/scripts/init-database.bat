@echo off
REM =============================================
REM 数据库初始化脚本 (Windows)
REM =============================================

echo ========================================
echo 智能校园失物招领平台 - 数据库初始化
echo ========================================
echo.

REM 设置MySQL路径（如果MySQL不在PATH中，请修改此路径）
set MYSQL_PATH=mysql

REM 设置数据库配置
set DB_USER=root
set DB_PASSWORD=40619128
set DB_NAME=campuslostandfound

echo [1/3] 正在创建数据库和表结构...
%MYSQL_PATH% -u%DB_USER% -p%DB_PASSWORD% < ..\src\main\resources\schema.sql
if %errorlevel% neq 0 (
    echo 错误: 数据库初始化失败！
    echo 请检查:
    echo 1. MySQL服务是否正在运行
    echo 2. 用户名和密码是否正确
    echo 3. MySQL是否在系统PATH中
    pause
    exit /b 1
)
echo ✓ 数据库结构创建成功！
echo.

echo [2/3] 是否导入测试数据？(Y/N)
set /p IMPORT_TEST_DATA=
if /i "%IMPORT_TEST_DATA%"=="Y" (
    echo 正在导入测试数据...
    %MYSQL_PATH% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < ..\src\main\resources\data-test.sql
    if %errorlevel% neq 0 (
        echo 警告: 测试数据导入失败！
    ) else (
        echo ✓ 测试数据导入成功！
    )
    echo.
)

echo [3/3] 正在验证数据库安装...
%MYSQL_PATH% -u%DB_USER% -p%DB_PASSWORD% < verify-database.sql
if %errorlevel% neq 0 (
    echo 警告: 数据库验证失败！
) else (
    echo ✓ 数据库验证通过！
)
echo.

echo ========================================
echo 数据库初始化完成！
echo ========================================
echo.
echo 数据库名称: %DB_NAME%
echo 用户名: %DB_USER%
echo.
echo 测试账号（密码均为: password123）:
echo - 学号: 2021001 (普通用户)
echo - 学号: 2021002 (普通用户)
echo - 学号: 2021003 (普通用户)
echo - 学号: ADMIN001 (管理员)
echo.
echo 下一步: 运行 'mvnw spring-boot:run' 启动应用
echo ========================================
pause
