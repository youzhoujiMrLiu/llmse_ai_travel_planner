#!/bin/bash

# .env 文件验证脚本
# 用于检查 .env 文件配置是否正确

echo "=========================================="
echo "  Travel Planner - .env 配置验证工具"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ 错误: 未找到 .env 文件${NC}"
    echo ""
    echo "请执行以下命令创建 .env 文件:"
    echo "  cp .env.example .env"
    echo ""
    exit 1
fi

echo -e "${GREEN}✅ .env 文件存在${NC}"
echo ""

# 定义必需的环境变量
REQUIRED_VARS=("SUPABASE_DB_PASSWORD" "SUPABASE_JWT_SECRET" "QWEN_API_KEY" "AMAP_API_KEY")
OPTIONAL_VARS=("XFYUN_APP_ID" "XFYUN_API_KEY" "XFYUN_API_SECRET")

# 检查必需的环境变量
echo "检查必需的环境变量..."
echo "----------------------------"

MISSING_COUNT=0
EXAMPLE_VALUE_COUNT=0

for var in "${REQUIRED_VARS[@]}"; do
    # 从 .env 文件读取变量值
    value=$(grep "^${var}=" .env | cut -d '=' -f2- | tr -d '"' | tr -d "'" | xargs)
    
    if [ -z "$value" ]; then
        echo -e "${RED}❌ ${var}: 未设置${NC}"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    elif [[ "$value" == *"your_"* ]] || [[ "$value" == *"_here"* ]]; then
        echo -e "${YELLOW}⚠️  ${var}: 使用了示例值${NC}"
        EXAMPLE_VALUE_COUNT=$((EXAMPLE_VALUE_COUNT + 1))
    else
        # 隐藏敏感信息,只显示前 8 个字符
        masked_value="${value:0:8}..."
        echo -e "${GREEN}✅ ${var}: ${masked_value}${NC}"
    fi
done

echo ""

# 检查可选的环境变量
echo "检查可选的环境变量..."
echo "----------------------------"

for var in "${OPTIONAL_VARS[@]}"; do
    value=$(grep "^${var}=" .env | cut -d '=' -f2- | tr -d '"' | tr -d "'" | xargs)
    
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⭕ ${var}: 未设置 (可选)${NC}"
    elif [[ "$value" == *"your_"* ]] || [[ "$value" == *"_here"* ]]; then
        echo -e "${YELLOW}⭕ ${var}: 使用了示例值 (可选)${NC}"
    else
        masked_value="${value:0:8}..."
        echo -e "${GREEN}✅ ${var}: ${masked_value}${NC}"
    fi
done

echo ""
echo "=========================================="

# 总结
if [ $MISSING_COUNT -gt 0 ] || [ $EXAMPLE_VALUE_COUNT -gt 0 ]; then
    echo -e "${RED}❌ 配置不完整!${NC}"
    echo ""
    if [ $MISSING_COUNT -gt 0 ]; then
        echo "有 $MISSING_COUNT 个必需的环境变量未设置。"
    fi
    if [ $EXAMPLE_VALUE_COUNT -gt 0 ]; then
        echo "有 $EXAMPLE_VALUE_COUNT 个必需的环境变量使用了示例值。"
    fi
    echo ""
    echo "请编辑 .env 文件,填入真实的 API Keys:"
    echo "  nano .env    (Linux/Mac)"
    echo "  notepad .env (Windows Git Bash)"
    echo ""
    echo "如何获取 API Keys:"
    echo "  参考: ../ENVIRONMENT_SETUP.md"
    echo ""
    exit 1
else
    echo -e "${GREEN}✅ 所有必需的环境变量都已正确配置!${NC}"
    echo ""
    echo "可以启动应用了:"
    echo "  ./mvnw spring-boot:run    (Linux/Mac)"
    echo "  .\\mvnw.cmd spring-boot:run (Windows)"
    echo ""
fi

# 检查文件编码 (Linux/Mac)
if command -v file &> /dev/null; then
    encoding=$(file -b --mime-encoding .env)
    if [ "$encoding" != "utf-8" ] && [ "$encoding" != "us-ascii" ]; then
        echo -e "${YELLOW}⚠️  警告: .env 文件编码不是 UTF-8 ($encoding)${NC}"
        echo "   建议转换为 UTF-8 编码"
        echo ""
    fi
fi

# 检查文件权限 (Linux/Mac)
if [ "$(uname)" != "MINGW64_NT"* ] && [ "$(uname)" != "MSYS_NT"* ]; then
    permissions=$(stat -c %a .env 2>/dev/null || stat -f %A .env 2>/dev/null)
    if [ "$permissions" != "644" ] && [ "$permissions" != "600" ]; then
        echo -e "${YELLOW}⚠️  警告: .env 文件权限不安全 ($permissions)${NC}"
        echo "   建议执行: chmod 600 .env"
        echo ""
    fi
fi

echo "=========================================="
