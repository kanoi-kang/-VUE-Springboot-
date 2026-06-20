swagger-API文档地址: http://localhost:8080/swagger-ui.html

微信支付宝接口调用需要成本，所以在开发阶段使用的是模拟支付

# YiJiangNan 项目启动指南

## 项目结构
```
d:\YiJiangNan\
├── YiJiangNan-master\      # 静态网站
├── backend\                # 后端Spring Boot
├── frontend\
│   ├── user-app\          # Vue用户端前端
│   └── admin-app\         # Vue管理端前端
├── nginx.conf             # Nginx配置文件（已创建）
└── start-all.bat          # 一键启动脚本（已创建）
```

## 端口映射
| 服务 | 原地址 | Nginx代理地址 |
|------|--------|---------------|
| 静态网站 | http://127.0.0.1:8081 | http://localhost |
| Vue用户端 | http://localhost:5174 | http://localhost/products |
| Vue管理端 | http://localhost:5173 | http://localhost/admin |
| 后端API | http://localhost:8080 | http://localhost/api/ |

## 快速启动

### 方式一：使用启动脚本（推荐）
1. 双击运行 `start-all.bat`
2. 手动启动Nginx

### 方式二：手动启动
#### 1. 启动静态网站
```bash
cd YiJiangNan-master
http-server -p 8081
```

#### 2. 启动Vue用户端
```bash
cd frontend/user-app
npm run dev
```

#### 3. 启动Vue管理端
```bash
cd frontend/admin-app
npm run dev
```

#### 4. 启动后端
```bash
cd backend
mvnw spring-boot:run
```

#### 5. 启动Nginx
- 将 `nginx.conf` 复制到 nginx 安装目录的 `conf/` 文件夹
- 或者启动时指定配置文件：
  ```bash
  nginx -c d:\YiJiangNan\nginx.conf
  ```

## 访问地址
- **静态网站**: http://localhost
- **Vue用户端商城**: http://localhost/products
- **Vue管理端**: http://localhost/admin
- **后端API文档**: http://localhost/api/swagger-ui.html

## 注意事项
1. 确保已安装 Node.js、Java (JDK 17+)、Nginx
2. 确保 http-server 已全局安装：`npm install -g http-server`
3. 如果端口被占用，请修改相应配置
4. Nginx 配置中已包含 WebSocket 支持，用于 Vite HMR 热更新

## 链接修改说明
已完成的修改：
1. 静态网站导航栏文旅商城链接 → 指向Vue用户端
2. 静态网站首页分类展示链接 → 指向Vue用户端
3. 旧商城页面添加自动重定向 → 跳转到Vue用户端
