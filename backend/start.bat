@echo off
title 会议室预约系统一键启动

echo 正在启动前端...
start "前端" cmd /k "cd /d C:\MyMeetingSystemFronted\meeting-system-frontend\meeting-system-frontend && npm run dev"

echo 正在启动后端...
start "后端" cmd /k "cd /d C:\Users\程培昊\IdeaProjects\Group_work && mvn spring-boot:run"

echo 正在启动 Redis...
start "Redis" cmd /k "cd /d E:\Redis && redis-server.exe"

echo 正在启动 MySQL Workbench...
start "" "C:\Program Files\MySQL\MySQL Workbench 8.0\MySQLWorkbench.exe"

echo 所有服务启动命令已发出，请查看对应窗口。
pause