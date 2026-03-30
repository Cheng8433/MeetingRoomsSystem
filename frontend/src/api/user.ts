// src/api/user.ts
import {request} from '@/utils/requests'
import type {
    ApiResponse,
    LoginData,
    LoginResponse,
    User,
    BackendLoginData,
    RegisterParams,
    RegisterResponse, BackendRegisterData
} from '@/types'

// src/api/user.ts - 确保login函数是这个样子
export const login = (data: LoginData): Promise<LoginResponse> => {
    return request.post<ApiResponse<BackendLoginData>>('/api/users/login', data).then(response => {
        // response.data 现在是 ApiResponse<BackendLoginData>
        const apiResponse = response.data

        // apiResponse.data 是 BackendLoginData
        const backendData = apiResponse.data

        // 提取用户信息和token
        const user = backendData.user
        const token = backendData.token

        return {
            userId: user.id,
            username: user.sysUsername,
            roleType: user.role,
            token: token,
        }
    })
}

// 登出
    export const logout = (): Promise<string> => {
        return request.post('/api/users/logout')
    }

// 注册
// src/api/user.ts - 简化API层
export const register = (data: RegisterParams): Promise<RegisterResponse> => {
    return request.post<ApiResponse<BackendRegisterData>>('/api/users/register', data)
        .then(response => {
            const apiResponse = response.data

            if (!apiResponse.success || !apiResponse.data) {
                // 后端返回了错误，但HTTP状态码可能还是200
                throw new Error(apiResponse.message || '注册失败')
            }

            const backendData = apiResponse.data

            // 确保数据存在
            if (!backendData) {
                throw new Error('注册返回数据不完整')
            }

            return {
                userId: backendData.id,
                username: backendData.sysUsername,
                roleType: backendData.role,
            }
        })
}

// 获取当前用户信息
    export const getUserById = (userId : number): Promise<User> => {
        return request.get(`/api/users/${userId}`).then(res => res.data.data) // 需要后端提供这个接口
    }
