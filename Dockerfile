# ============================================
# 多阶段构建 - AI 旅行规划助手
# 前后端一体化镜像
# ============================================

# ============================================
# 阶段 1: 构建前端
# ============================================
# 使用标准版本的 Node 镜像以减少安全漏洞
FROM node:20 AS frontend-builder

WORKDIR /app/frontend

# 复制前端依赖文件
COPY travel-planner-frontend/package*.json ./

# 安装依赖
RUN npm install

# 复制前端源代码
COPY travel-planner-frontend/ ./

# 使用占位符构建前端 (运行时会被替换)
ENV VITE_SUPABASE_URL=__SUPABASE_URL__
ENV VITE_SUPABASE_ANON_KEY=__SUPABASE_ANON_KEY__
ENV VITE_AMAP_WEB_KEY=__AMAP_WEB_KEY__

# 构建前端 (生成 dist 目录)
RUN npm run build


# ============================================
# 阶段 2: 构建后端
# ============================================
# 使用最新版本的 Maven + JDK 17 镜像以减少安全漏洞
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app/backend

# 复制 Maven 配置文件
COPY travel-planner-backend/pom.xml ./

# 下载依赖 (利用 Docker 缓存层)
RUN mvn dependency:go-offline -B

# 复制后端源代码
COPY travel-planner-backend/src ./src

# 打包后端 (跳过测试以加快构建)
RUN mvn clean package -DskipTests


# ============================================
# 阶段 3: 运行时镜像
# ============================================
FROM eclipse-temurin:17-jre-alpine

# 安装 nginx 用于服务前端静态文件
RUN apk add --no-cache nginx

WORKDIR /app

# 从构建阶段复制后端 JAR 包
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# 从构建阶段复制前端构建产物
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# 复制 nginx 配置文件
COPY nginx.conf /etc/nginx/nginx.conf

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 暴露端口
# 80: Nginx (前端)
# 8080: Spring Boot (后端 API)
EXPOSE 80 8080

# 环境变量说明 (运行时通过 docker run -e 或 .env 文件提供)
# 后端环境变量
ENV SUPABASE_DB_PASSWORD="" \
    SUPABASE_JWT_SECRET="" \
    QWEN_API_KEY="" \
    AMAP_API_KEY="" \
    XFYUN_APP_ID="" \
    XFYUN_API_KEY="" \
    XFYUN_API_SECRET=""

# 前端环境变量
ENV VITE_SUPABASE_URL="" \
    VITE_SUPABASE_ANON_KEY="" \
    VITE_AMAP_WEB_KEY=""

# 启动脚本
ENTRYPOINT ["/app/docker-entrypoint.sh"]
