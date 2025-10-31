// src/services/authService.ts
import { supabase } from '@/lib/supabase'

export const signIn = (email: string, password: string) =>
  supabase.auth.signInWithPassword({ email, password })

export const signUp = (email: string, password: string) =>
  supabase.auth.signUp({
    email,
    password,
    options: {
      // 显式禁用邮箱重定向（即使开启邮箱确认，也不跳转）
      emailRedirectTo: undefined,
    },
  })
