# 数据库设置指南

## 📋 概述

本指南将帮助你在 Supabase 中设置旅行规划应用所需的数据库表。

---

## 🗄️ 数据库表结构

### 1. **travel_plans** (旅行计划主表)
存储用户的旅行计划基本信息

### 2. **plan_itinerary** (行程详细安排)
存储每个旅行计划的详细行程安排

### 3. **plan_expenses** (费用记录)
存储旅行过程中的费用记录

---

## 🚀 设置步骤

### 步骤 1: 登录 Supabase

1. 访问 [Supabase Dashboard](https://app.supabase.com/)
2. 选择你的项目

### 步骤 2: 执行迁移脚本

1. 在左侧菜单中点击 **SQL Editor**
2. 点击 **New Query**
3. 复制 `migrations/001_create_travel_plans_tables.sql` 文件的内容
4. 粘贴到 SQL 编辑器中
5. 点击 **Run** 按钮执行

### 步骤 3: 验证表创建

1. 在左侧菜单中点击 **Table Editor**
2. 你应该能看到以下表：
   - `travel_plans`
   - `plan_itinerary`
   - `plan_expenses`

### 步骤 4: 验证 RLS 策略

1. 点击任意表
2. 点击顶部的 **RLS** 选项卡
3. 确认策略已启用且正确配置

---

## 🔐 Row Level Security (RLS)

所有表都启用了 RLS，确保：
- ✅ 用户只能访问自己的数据
- ✅ 数据通过 `user_id` 关联到 Supabase Auth
- ✅ 子表（itinerary, expenses）通过外键验证权限

---

## 📊 字段说明

### travel_plans 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| user_id | UUID | 用户ID（关联 auth.users） |
| destination | TEXT | 目的地 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| duration | INTEGER | 天数 |
| budget | NUMERIC(10,2) | 预算 |
| travelers | INTEGER | 同行人数 |
| preferences | TEXT[] | 偏好标签数组 |
| status | TEXT | 状态：planning/ongoing/completed |
| user_input | TEXT | 用户原始输入 |
| ai_generated_plan | JSONB | AI 生成的行程（JSON） |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### 状态说明

- **planning (规划中)**: 出发日期未到
- **ongoing (进行中)**: 当前在旅行日期范围内
- **completed (已完成)**: 结束日期已过或手动标记完成

---

## 🧪 测试数据（可选）

如需插入测试数据，请：

1. 从 Supabase Dashboard → Authentication 获取你的 user_id
2. 编辑 SQL 脚本底部的测试数据部分
3. 将 `'your-user-id-here'` 替换为实际的 user_id
4. 取消注释并执行

---

## 🔗 API 端点

后端已创建以下 API 端点：

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/travel-plans` | 获取用户所有计划 |
| GET | `/api/travel-plans/:id` | 获取单个计划详情 |
| POST | `/api/travel-plans` | 创建新计划 |
| PUT | `/api/travel-plans/:id` | 更新计划 |
| DELETE | `/api/travel-plans/:id` | 删除计划 |
| PATCH | `/api/travel-plans/:id/complete` | 标记为已完成 |

---

## ⚠️ 注意事项

1. **确保后端 application.properties 配置正确**
   ```properties
   spring.datasource.url=jdbc:postgresql://your-project.supabase.co:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=your-password
   ```

2. **状态自动更新**
   - 后端会根据当前日期自动更新计划状态
   - planning → ongoing: 当到达开始日期时
   - ongoing → completed: 当超过结束日期时

3. **数据验证**
   - 所有必填字段在后端有验证
   - 日期逻辑验证（开始日期不能早于今天）
   - 预算和人数必须大于 0

---

## 📞 问题排查

### 问题 1: 表创建失败
**解决方案**: 检查是否有语法错误，确保 Supabase 版本支持所有 SQL 功能

### 问题 2: RLS 策略不生效
**解决方案**: 确认已启用 RLS，检查策略中的 `auth.uid()` 是否正确

### 问题 3: 后端无法连接数据库
**解决方案**: 
- 检查 Supabase 连接字符串
- 确认数据库密码正确
- 检查防火墙设置

---

## ✅ 完成检查清单

- [ ] 数据库表已创建
- [ ] RLS 策略已启用
- [ ] 后端依赖已添加
- [ ] application.properties 已配置
- [ ] 前端 API 已更新
- [ ] 测试创建计划功能

---

**祝你使用愉快！** 🎉
