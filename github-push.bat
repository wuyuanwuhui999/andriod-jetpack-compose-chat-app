@echo off
chcp 65001 >nul
title GitHub推送脚本
setlocal enabledelayedexpansion

:main
cls
echo ========================================
echo       GitHub代码推送脚本
echo ========================================
echo.

:: 检查git是否可用
where git >nul 2>nul
if errorlevel 1 (
    echo ✗ 错误：未找到Git命令！
    echo 请确保Git已正确安装并添加到系统PATH环境变量中
    echo 或者从 https://git-scm.com/ 下载安装Git
    echo.
    pause
    exit /b 1
)

:: 检查当前目录是否是git仓库
if not exist ".git" (
    echo ✗ 错误：当前目录不是Git仓库！
    echo 请确保在正确的Git仓库目录中运行此脚本
    echo.
    pause
    exit /b 1
)

echo 正在检查git仓库状态...
git status
echo.

set /p confirm=是否继续推送代码到GitHub? (y/n): 
if /i "%confirm%" neq "y" (
    echo 操作已取消
    timeout /t 2 /nobreak >nul
    exit /b 0
)

:push_attempt
cls
echo 正在推送代码到GitHub仓库...
echo.

:: 先添加所有更改
echo 1. 添加更改到暂存区...
git add .
if errorlevel 1 goto git_error

:: 提交更改
echo 2. 提交更改...
set commit_msg=Auto commit %date% %time%
git commit -m "!commit_msg!"
if errorlevel 1 (
    echo 注意：没有新的更改需要提交
    echo 将尝试直接推送...
)

:: 推送代码
echo 3. 推送到远程仓库...
echo 仓库地址: https://github.com/wuyuanwuhui999/andriod-jetpack-compose-chat-app
echo 目标分支: main
echo.

:: 添加远程仓库
git remote remove origin 2>nul
git remote add origin https://github.com/wuyuanwuhui999/andriod-jetpack-compose-chat-app
if errorlevel 1 goto git_error

:: 推送代码
git push -u origin main
if errorlevel 1 goto push_error

:: 成功
echo.
echo ========================================
echo ✓ 代码推送成功！
echo ========================================
echo.
echo 窗口将在5秒后自动关闭...
timeout /t 5 /nobreak >nul
exit /b 0

:git_error
echo.
echo ✗ Git命令执行出错！
echo 错误代码: %errorlevel%
echo.
pause
exit /b %errorlevel%

:push_error
echo.
echo ✗ 推送失败！
echo 可能的原因:
echo 1. GitHub账户认证失败
echo 2. 没有访问该仓库的权限
echo 3. 网络连接问题
echo 4. 分支名称不一致
echo.
echo 错误代码: %errorlevel%
echo.
set /p retry=是否重试? (y/n): 
if /i "%retry%" equ "y" goto push_attempt

echo 操作已取消
pause
exit /b %errorlevel%