@echo off
chcp 65001 >nul
title GitHub代码推送脚本

:push_attempt
cls
echo ========================================
echo            GitHub代码推送
echo ========================================
echo.
echo 正在设置远程仓库并推送代码...
echo 仓库地址: https://github.com/wuyuanwuhui999/andriod-jetpack-compose-chat-app
echo 目标分支: main
echo.

:: 设置远程仓库并推送
git remote remove origin
git remote add origin https://github.com/wuyuanwuhui999/andriod-jetpack-compose-chat-app
git push -u origin main

:: 检查推送结果
if %errorlevel% equ 0 (
    echo.
    echo ✓ 代码推送成功！
    echo 窗口将在5秒后自动关闭...
    timeout /t 5 /nobreak >nul
    exit
) else (
    echo.
    echo ✗ 推送失败，错误代码: %errorlevel%
    echo.
    echo 请检查:
    echo 1. 网络连接是否正常
    echo 2. GitHub账户权限是否正确
    echo 3. 本地是否有新的提交需要推送
    echo.
    echo 按回车键重试推送，或按 Ctrl+C 退出...
    pause >nul