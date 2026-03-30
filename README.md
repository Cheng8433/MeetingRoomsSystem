# 会议室预约系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.x-blue.svg)](https://element-plus.org/)

会议室预约系统是一个面向企业内部或机构的 Web 应用，支持用户查看会议室、在线预约、管理员审批等功能。系统采用前后端分离架构，后端基于 Spring Boot，前端使用 Vue 3 + Element Plus，数据库使用 MySQL，并通过 Docker 实现快速部署。

## 功能特性

### 用户模块
- 登录/登出（角色区分：普通用户、管理员）
- 个人信息管理

### 会议室管理（仅管理员）
- 查看会议室列表（支持分页、按状态/容量筛选）
- 新增/修改/删除会议室
- 设置会议室状态（可预约/不可预约）

### 预约管理
**普通用户**
- 条件筛选可用会议室（按时间、容量等）
- 提交预约申请（包含会议主题、时间、人数等）
- 查看个人预约记录（待审批、已通过、已驳回、已取消）
- 取消预约（待审批或已通过状态）
- 使用确认（签到/结束）

**管理员**
- 查看所有待审批预约
- 审批预约（通过/驳回，驳回需填写理由）
- 查看所有预约记录
- 周视图展示各会议室预约状态

### 系统管理
- 系统通知（预约结果通知）
- 操作日志审计
- 数据字典管理（角色类型、审批状态等）

## 技术栈

| 模块 | 技术/框架 |
|------|-----------|
| 后端 | Spring Boot, Spring MVC, MyBatis, MySQL |
| 前端 | Vue 3, Vue Router, Pinia, Element Plus |
| API 交互 | RESTful API (JSON) |
| 部署 | Docker, Nginx |
| 开发工具 | Maven, Vite |

## 系统架构

系统采用经典的三层架构（表现层、业务逻辑层、数据访问层）结合 MVC 模式，前后端完全分离。

- **表现层**：前端 Vue 应用 + 后端 Spring MVC @RestController 提供 API
- **业务逻辑层**：Spring @Service 组件
- **数据访问层**：MyBatis Mapper 接口

详细架构图请参见项目文档 `/docs/architecture.png`。

## 数据库设计

核心表包括：
- `sys_user` – 用户表（用户ID、用户名、密码、角色类型等）
- `meeting_room` – 会议室表（会议室ID、名称、房号、容量、状态等）
- `reservation_record` – 预约记录表（预约ID、用户ID、会议室ID、会议主题、时间、审批状态等）
- `sys_notice` – 系统通知表
- `sys_oper_log` – 操作日志表
- `sys_dict` – 数据字典表

ER 图与详细表结构请参考 `/docs/database.md`。

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis（可选，用于会话管理）

### 后端启动

1. 克隆仓库并进入后端目录：
   ```bash
   git clone https://github.com/Cheng8433/MeetingRoomsSystem.git
   cd MeetingRoomsSystem/backend
   ```

2. 创建数据库（如 `meeting_room_db`），并执行初始化脚本（脚本位于 `/docs/schema.sql`）。

3. 修改配置文件 `src/main/resources/application-dev.yml` 中的数据库连接信息。

4. 使用 Maven 打包并运行：
   ```bash
   mvn clean package
   java -jar target/meeting-room-system-0.0.1-SNAPSHOT.jar
   ```
   或直接使用 Maven 插件：
   ```bash
   mvn spring-boot:run
   ```

后端服务默认运行在 `http://localhost:8080`。

### 前端启动

1. 进入前端目录：
   ```bash
   cd MeetingRoomsSystem/frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 配置 API 地址：修改 `.env.development` 文件中的 `VITE_API_BASE_URL`，默认为 `http://localhost:8080/api`。

4. 启动开发服务器：
   ```bash
   npm run dev
   ```

前端应用默认运行在 `http://localhost:5173`。

## API 接口概览

主要 RESTful API 示例：

| 模块 | 方法 | URL | 描述 |
|------|------|-----|------|
| 用户登录 | POST | `/api/users/login` | 用户登录，返回角色信息 |
| 会议室列表 | GET | `/api/meeting-rooms` | 获取所有会议室（可分页） |
| 新增会议室 | POST | `/api/meeting-rooms` | 管理员添加会议室 |
| 提交预约 | POST | `/api/reservations` | 提交预约申请 |
| 我的预约记录 | GET | `/api/reservations/user/{id}` | 获取当前用户的预约记录 |
| 待审批列表 | GET | `/api/reservations/pending` | 管理员获取待审批记录 |
| 审批通过 | PUT | `/api/reservations/{id}/confirm` | 管理员通过预约 |
| 审批驳回 | PUT | `/api/reservations/{id}/reject` | 管理员驳回预约 |

完整的接口文档请参考 `/docs/api.md`。

## 项目结构

```
MeetingRoomsSystem/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   ├── main/java/com/xxx/
│   │   │   ├── controller/  # 控制器层
│   │   │   ├── service/     # 业务层
│   │   │   ├── mapper/      # 数据访问层
│   │   │   ├── entity/      # 实体类
│   │   │   └── config/      # 配置类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/      # MyBatis XML 映射文件
│   └── pom.xml
├── frontend/                # Vue 前端
│   ├── src/
│   │   ├── api/             # API 请求封装
│   │   ├── components/      # 公共组件
│   │   ├── views/           # 页面视图
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   └── App.vue
│   ├── package.json
│   └── vite.config.js
├── docs/                    # 文档（数据库脚本、接口文档等）
│   ├── schema.sql
│   ├── database.md
│   └── api.md
├── docker-compose.yml       # Docker 一键部署配置
└── README.md
```

## 部署

支持 Docker Compose 一键部署：

```bash
docker-compose up -d
```

Docker Compose 将启动 MySQL、Redis、后端服务及 Nginx 反向代理。

## 许可证

本项目仅供学习交流使用，未经许可不得用于商业用途。

---

**注**：本系统为课程设计项目，如有问题欢迎提交 Issue 或联系作者。
