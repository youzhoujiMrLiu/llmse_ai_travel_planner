#!/bin/sh
set -e

echo "=========================================="
echo "AI Travel Planner - Starting Services"
echo "=========================================="

# 检查必需的环境变量
check_env() {
    if [ -z "$1" ]; then
        echo "⚠️  WARNING: $2 is not set"
    else
        echo "✓ $2 is configured"
    fi
}

echo ""
echo "Checking environment variables..."
check_env "$SUPABASE_DB_PASSWORD" "SUPABASE_DB_PASSWORD"
check_env "$SUPABASE_JWT_SECRET" "SUPABASE_JWT_SECRET"
check_env "$QWEN_API_KEY" "QWEN_API_KEY"
check_env "$AMAP_API_KEY" "AMAP_API_KEY (Backend)"
check_env "$VITE_SUPABASE_URL" "VITE_SUPABASE_URL (Frontend)"
check_env "$VITE_SUPABASE_ANON_KEY" "VITE_SUPABASE_ANON_KEY (Frontend)"
check_env "$VITE_AMAP_WEB_KEY" "VITE_AMAP_WEB_KEY (Frontend)"

# 如果存在 /app/.env 文件，导出其中的环境变量
if [ -f /app/.env ]; then
    echo ""
    echo "Found /app/.env file, loading environment variables..."
    export $(grep -v '^#' /app/.env | xargs)
    echo "✓ Environment variables loaded from /app/.env"
fi

echo ""
echo "=========================================="
echo "Injecting Frontend Environment Variables"
echo "=========================================="

# 替换前端构建产物中的占位符为实际环境变量
# 这样可以在运行时动态注入前端配置，无需重新构建镜像
if [ -n "$VITE_SUPABASE_URL" ]; then
    find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__SUPABASE_URL__|$VITE_SUPABASE_URL|g" {} +
    echo "✓ Injected VITE_SUPABASE_URL"
fi

if [ -n "$VITE_SUPABASE_ANON_KEY" ]; then
    find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__SUPABASE_ANON_KEY__|$VITE_SUPABASE_ANON_KEY|g" {} +
    echo "✓ Injected VITE_SUPABASE_ANON_KEY"
fi

if [ -n "$VITE_AMAP_WEB_KEY" ]; then
    find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__AMAP_WEB_KEY__|$VITE_AMAP_WEB_KEY|g" {} +
    echo "✓ Injected VITE_AMAP_WEB_KEY"
fi

echo ""
echo "=========================================="
echo "Starting Nginx (Frontend)..."
echo "=========================================="
nginx

echo ""
echo "=========================================="
echo "Starting Spring Boot (Backend)..."
echo "=========================================="
exec java -jar /app/app.jar
