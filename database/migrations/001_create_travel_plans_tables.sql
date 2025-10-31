-- Supabase 数据库迁移脚本
-- 执行步骤：
-- 1. 登录 Supabase Dashboard
-- 2. 进入 SQL Editor
-- 3. 复制并执行此脚本

-- ============================================
-- 1. 创建 travel_plans 表
-- ============================================
CREATE TABLE IF NOT EXISTS travel_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    destination TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    budget NUMERIC(10,2) NOT NULL,
    travelers INTEGER NOT NULL DEFAULT 1,
    preferences TEXT[],
    status TEXT NOT NULL DEFAULT 'planning' CHECK (status IN ('planning', 'ongoing', 'completed')),
    user_input TEXT,
    ai_generated_plan JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_travel_plans_user_id ON travel_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_travel_plans_status ON travel_plans(status);
CREATE INDEX IF NOT EXISTS idx_travel_plans_start_date ON travel_plans(start_date);

-- 创建更新时间触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_travel_plans_updated_at BEFORE UPDATE ON travel_plans
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 2. 创建 plan_itinerary 表（行程详细安排）
-- ============================================
CREATE TABLE IF NOT EXISTS plan_itinerary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL,
    time_slot TEXT,
    activity_type TEXT NOT NULL CHECK (activity_type IN ('transport', 'accommodation', 'attraction', 'restaurant', 'other')),
    title TEXT NOT NULL,
    description TEXT,
    location TEXT,
    latitude NUMERIC(10,8),
    longitude NUMERIC(11,8),
    estimated_cost NUMERIC(10,2),
    actual_cost NUMERIC(10,2),
    notes TEXT,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_plan_itinerary_plan_id ON plan_itinerary(plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_itinerary_day_number ON plan_itinerary(day_number);

-- ============================================
-- 3. 创建 plan_expenses 表（费用记录）
-- ============================================
CREATE TABLE IF NOT EXISTS plan_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    category TEXT NOT NULL CHECK (category IN ('transport', 'food', 'accommodation', 'shopping', 'entertainment', 'other')),
    amount NUMERIC(10,2) NOT NULL,
    currency TEXT DEFAULT 'CNY',
    description TEXT,
    expense_date DATE NOT NULL,
    payment_method TEXT CHECK (payment_method IN ('cash', 'card', 'alipay', 'wechat', 'other')),
    created_by_voice BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_plan_expenses_plan_id ON plan_expenses(plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_expenses_expense_date ON plan_expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_plan_expenses_category ON plan_expenses(category);

-- ============================================
-- 4. 启用 Row Level Security (RLS)
-- ============================================

-- travel_plans 表 RLS
ALTER TABLE travel_plans ENABLE ROW LEVEL SECURITY;

-- 用户只能查看自己的计划
CREATE POLICY "Users can view own plans" ON travel_plans
    FOR SELECT USING (auth.uid() = user_id);

-- 用户只能创建自己的计划
CREATE POLICY "Users can create own plans" ON travel_plans
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- 用户只能更新自己的计划
CREATE POLICY "Users can update own plans" ON travel_plans
    FOR UPDATE USING (auth.uid() = user_id);

-- 用户只能删除自己的计划
CREATE POLICY "Users can delete own plans" ON travel_plans
    FOR DELETE USING (auth.uid() = user_id);

-- plan_itinerary 表 RLS
ALTER TABLE plan_itinerary ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage itinerary of own plans" ON plan_itinerary
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM travel_plans 
            WHERE travel_plans.id = plan_itinerary.plan_id 
            AND travel_plans.user_id = auth.uid()
        )
    );

-- plan_expenses 表 RLS
ALTER TABLE plan_expenses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage expenses of own plans" ON plan_expenses
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM travel_plans 
            WHERE travel_plans.id = plan_expenses.plan_id 
            AND travel_plans.user_id = auth.uid()
        )
    );

-- ============================================
-- 5. 插入测试数据（可选）
-- ============================================
-- 注意：请将 'your-user-id-here' 替换为实际的用户 ID
-- 可以从 Supabase Dashboard 的 Authentication 页面获取

-- INSERT INTO travel_plans (user_id, destination, start_date, end_date, duration, budget, travelers, preferences, status)
-- VALUES 
--     ('your-user-id-here', '📍 日本东京5日游', '2025-11-10', '2025-11-15', 5, 10000.00, 2, ARRAY['美食', '动漫', '购物'], 'planning'),
--     ('your-user-id-here', '📍 成都美食3日游', '2025-10-01', '2025-10-03', 3, 3000.00, 1, ARRAY['美食', '文化'], 'completed');

-- ============================================
-- 完成！
-- ============================================
-- 现在你可以通过后端 API 访问这些表了
