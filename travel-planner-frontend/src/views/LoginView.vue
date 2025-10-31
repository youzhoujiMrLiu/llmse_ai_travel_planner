<!-- src/views/LoginView.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>AI 旅行规划师</h2>
          <p>{{ isLoginMode ? '登录您的账号' : '创建新账号' }}</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            type="text"
            placeholder="请输入邮箱作为用户名"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            size="large"
            style="width: 100%"
          >
            {{ isLoginMode ? '登录' : '注册' }}
          </el-button>
        </el-form-item>

        <div class="switch-mode">
          <el-link type="primary" @click="toggleMode">
            {{ isLoginMode ? '没有账号？立即注册' : '已有账号？返回登录' }}
          </el-link>
        </div>
      </el-form>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        closable
        @close="errorMessage = ''"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

import { signIn, signUp } from '@/services/authService'
import apiClient from '@/api/apiClient'

const router = useRouter()
const formRef = ref<FormInstance>()
const isLoginMode = ref(true)
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: '',
})

const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
})

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  errorMessage.value = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    errorMessage.value = ''

    try {
      let result

      if (isLoginMode.value) {
        result = await signIn(form.email, form.password)
      } else {
        result = await signUp(form.email, form.password)
      }

      if (result.error) {
        // Supabase 错误（如邮箱已存在、密码太弱等）
        errorMessage.value = result.error.message || '未知错误'
        ElMessage.error(errorMessage.value)
        loading.value = false
        return
      }

      // 确保在调用后端接口前 session 已经完全建立
      if (isLoginMode.value) {
        // 等待一小段时间确保 session 建立完成
        await new Promise(resolve => setTimeout(resolve, 100))

        // 调用后端接口验证 JWT
        await apiClient.get('/user/profile')
      }

      ElMessage.success(isLoginMode.value ? '登录成功！' : '注册成功！')
      // 登录成功才跳转到 dashboard
      if (isLoginMode.value) {
        await router.push('/dashboard')
      } else {
        // 注册成功后切换到登录模式
        isLoginMode.value = true
        form.password = ''
      }
    } catch (err: any) {
      console.error('登录/注册错误:', err)
      errorMessage.value = err.response?.data?.error || err.message || '操作失败，请重试'
      ElMessage.error(errorMessage.value)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f9fafb;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
  text-align: center;
}

.card-header p {
  margin: 8px 0 0;
  color: #6b7280;
  text-align: center;
}

.switch-mode {
  text-align: center;
  margin-top: 12px;
}

:deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
}
</style>
