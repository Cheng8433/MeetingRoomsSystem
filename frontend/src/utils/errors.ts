// src/utils/errors.ts
export function createApiError(
    message: string,
    originalError?: any
): any {
    const error = new Error(message) as any
    error.name = 'ApiError'
    error.response = originalError?.response
    error.originalError = originalError
    error.isCustomError = true
    return error
}

// 或者更完整的方法
export function wrapApiError(error: any): any {
    const message = error.response?.data?.message || error.message || '请求失败'
    const wrappedError = new Error(message) as any
    wrappedError.name = 'ApiError'
    wrappedError.response = error.response
    wrappedError.originalError = error
    wrappedError.isCustomError = true
    return wrappedError
}