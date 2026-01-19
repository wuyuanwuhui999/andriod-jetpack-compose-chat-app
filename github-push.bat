@echo off
chcp 65001 >nul

:retry
cls
echo 正在配置远程仓库并推送代码...
echo.

git remote remove origin
git remote add origin https://github.com/wuyuanwuhui999/andriod-jetpack-compose-chat-app
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo 推送成功！
    timeout /t 5 /nobreak >nul
    exit /b
) else (
    echo.
    echo 推送失败，请检查网络或权限问题。
    echo 按回车键重试...
    pause >nul
    goto retry
)