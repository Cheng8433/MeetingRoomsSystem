// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, RegisterParams } from '@/types'
import * as userApi from '@/api/user'
import { wrapApiError } from '@/utils/errors'

export const useUserStore = defineStore('user', () => {
    // 状态
    const user = ref<User | null>(null)
    const token = ref<string>('')
    const loading = ref(false)
    const error = ref<string | null>(null)

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.roleType === 1)

    // 清除错误信息
    const clearError = () => {
        error.value = null
    }

    // 注册
    // src/stores/user.ts - register 方法
    const register = async (params: RegisterParams) => {
        loading.value = true
        error.value = null

        try {
            const response = await userApi.register(params)

            return {
                success: true,
                message: '注册成功',
                userId: response.userId,
                username: response.username
            }
        } catch (err: any) {
            // 直接使用原始错误，不要包装
            console.error('Store捕获的原始错误:', err)

            // 提取错误消息，但不要改变错误对象
            error.value = err.response?.data?.message || err.message || '注册失败'

            // 直接抛出原始错误
            throw err
        } finally {
            loading.value = false
        }
    }

    // 登录（修改后，接收验证码）
    const login = async (username: string, password: string, captcha: string) => {
        loading.value = true
        error.value = null

        try {
            const response = await userApi.login({ username, password, captcha })
            token.value = response.token

            // 转换 roleType
            const roleTypeMap = {
                'user': 0 as const,
                'admin': 1 as const,
            } as const

            user.value = {
                userId: response.userId,
                username: response.username,
                real_name: response.username,
                roleType: roleTypeMap[response.roleType as keyof typeof roleTypeMap] || 0,
                status: 1,
            }

            // 保存到localStorage
            localStorage.setItem('token', response.token)
            localStorage.setItem('user', JSON.stringify(user.value))

            return response
        } catch (err: any) {
            error.value = err.message || '登录失败'
            throw err
        } finally {
            loading.value = false
        }
    }

    // 登出
    const logout = async () => {
        loading.value = true
        try {
            await userApi.logout()
        } finally {
            token.value = ''
            user.value = null
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            loading.value = false
        }
    }

    // 初始化时从localStorage恢复
    const initFromStorage = () => {
        const storedToken = localStorage.getItem('token')
        const storedUser = localStorage.getItem('user')
        if (storedToken && storedUser) {
            token.value = storedToken
            user.value = JSON.parse(storedUser)
        }
    }

    return {
        // 状态
        user,
        token,
        loading,
        error,

        // 计算属性
        isLoggedIn,
        isAdmin,

        // 方法
        register,
        login,
        logout,
        initFromStorage,
        clearError,
    }
})