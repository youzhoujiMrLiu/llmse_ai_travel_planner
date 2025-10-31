# 从 IDEA 运行配置迁移到 .env 文件

## 为什么要迁移？

| IDEA 运行配置 | .env 文件 |
|--------------|----------|
| ❌ 仅在 IDEA 中有效 | ✅ 在任何 IDE 和终端中都有效 |
| ❌ 每个团队成员需要手动配置 | ✅ 复制 .env 文件即可 |
| ❌ 不支持命令行启动 | ✅ 支持 `mvnw`、Docker、云部署 |
| ❌ 配置在项目文件中，可能被误提交 | ✅ 已在 .gitignore 中排除 |

## 迁移步骤 (3 分钟)

### 步骤 1: 导出现有配置

1. 在 IDEA 中打开项目
2. 点击右上角的运行配置下拉菜单 → **Edit Configurations...**
3. 选择 `TravelPlannerBackendApplication`
4. 找到 **Environment variables** 字段
5. 复制所有变量 (格式: `KEY1=value1;KEY2=value2`)

**示例**:
```
SUPABASE_DB_PASSWORD=mypassword123;SUPABASE_JWT_SECRET=jwt-secret-key;QWEN_API_KEY=sk-abc123;AMAP_API_KEY=amap-key-456
```

### 步骤 2: 创建 .env 文件

在 `travel-planner-backend` 目录下:

```bash
# 复制模板文件
cp .env.example .env
```

或者手动创建 `.env` 文件 (注意文件名没有扩展名)

### 步骤 3: 填入真实值

编辑 `.env` 文件，将 IDEA 中的值按行分隔：

```env
# 从 IDEA 复制的值
SUPABASE_DB_PASSWORD=mypassword123
SUPABASE_JWT_SECRET=jwt-secret-key
QWEN_API_KEY=sk-abc123
AMAP_API_KEY=amap-key-456

# 可选的科大讯飞配置
XFYUN_APP_ID=
XFYUN_API_KEY=
XFYUN_API_SECRET=
```

### 步骤 4: 测试 .env 加载

**方法 A: 保留 IDEA 配置测试**

1. 启动应用 (不修改 IDEA 配置)
2. 查看控制台输出:
   ```
   [DotEnvLoader] Successfully loaded 7 environment variables from .env file: /path/to/.env
   ```
3. 应用正常启动 → 迁移成功 ✅

**方法 B: 移除 IDEA 配置测试**

1. 在 **Edit Configurations** 中清空 **Environment variables** 字段
2. 点击 **Apply** → **OK**
3. 重新启动应用
4. 应用正常启动 → 迁移成功 ✅

### 步骤 5: 验证功能

```bash
# 健康检查
curl http://localhost:8080/api/health

# 预期输出
{"status":"OK","timestamp":"2024-01-01T00:00:00Z"}
```

## 常见问题

### Q1: .env 文件放在哪里？

```
travel-planner-backend/
├── src/
├── pom.xml
├── .env          ← 就在这里 (与 pom.xml 同级)
└── .env.example
```

### Q2: 为什么我的 .env 文件没有被加载？

检查以下几点：
1. 文件名是 `.env` 而不是 `.env.txt` 或其他
2. 文件在项目根目录 (`travel-planner-backend` 文件夹内)
3. 文件编码是 UTF-8
4. 没有使用示例值 (包含 `your_` 或 `_here`)

查看详细日志:
```bash
# 启动时会打印 .env 文件路径
./mvnw spring-boot:run

# 查找日志
[DotEnvLoader] Successfully loaded X environment variables from .env file: /absolute/path/to/.env
```

### Q3: IDEA 配置和 .env 文件哪个优先？

优先级 (从高到低):
1. **系统环境变量** (Windows 系统属性 / Linux `export`)
2. **IDEA 运行配置**
3. **.env 文件**

**建议**: 迁移后完全移除 IDEA 配置，只使用 .env 文件。

### Q4: 团队其他成员怎么配置？

1. 确保 `.env.example` 已提交到 Git
2. 新成员克隆项目后:
   ```bash
   cd travel-planner-backend
   cp .env.example .env
   # 编辑 .env 填入真实值
   ```
3. 通过安全渠道 (如 1Password) 分享真实的 API Keys

### Q5: 可以在 .env 中添加注释吗？

可以！使用 `#` 开头的行会被忽略：

```env
# 这是注释
SUPABASE_DB_PASSWORD=mypassword

# 多行注释也可以
# 第二行注释
QWEN_API_KEY=sk-abc123
```

### Q6: Windows 和 Linux 的 .env 文件通用吗？

完全通用！✅

- 自动处理不同的行结束符 (CRLF / LF)
- 使用 UTF-8 编码
- 路径自动适配

在 Windows 创建的 .env 可以直接在 Linux 服务器上使用。

## 回滚方案

如果迁移后遇到问题，可以临时回滚到 IDEA 配置：

1. 在 **Edit Configurations** → **Environment variables** 中重新填入变量
2. 删除或重命名 `.env` 文件: `mv .env .env.backup`
3. 重启应用

## 下一步

迁移完成后，推荐：

1. ✅ 将 `.env.example` 提交到 Git (供团队参考)
2. ✅ 在团队文档中说明 .env 配置方式
3. ✅ 定期轮换 API Keys (建议每 90 天)
4. ✅ 阅读完整文档: [ENVIRONMENT_SETUP.md](../ENVIRONMENT_SETUP.md)

## 需要帮助？

- 📖 完整文档: [ENVIRONMENT_SETUP.md](../ENVIRONMENT_SETUP.md)
- 🐛 Issue 反馈: [GitHub Issues](https://github.com/your-repo/issues)
- 💬 常见问题: [FAQ](../ENVIRONMENT_SETUP.md#常见问题排查)
