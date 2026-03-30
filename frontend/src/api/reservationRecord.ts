//src/api/reservationRecord.ts

import { request } from '@/utils/requests'
import { useUserStore } from '@/stores/user'
import type { Reservation } from '@/types'

// 创建预约的请求参数
export interface CreateReservationParams {
    room_id: number
    purpose: string
    reserveDate: string  // 格式: YYYY-MM-DD
    start_time: string    // 格式: HH:mm
    end_time: string      // 格式: HH:mm
    expectedNum: number
    user_id?: number      // 可选，如果后端从token获取的话
}

// 更新预约的请求参数
export interface UpdateReservationParams {
    purpose?: string
    reserveDate?: string
    start_time?: string
    end_time?: string
    expectedNum?: number
}

// 拒绝预约的参数
export interface RejectReservationParams {
    rejected_reason: string
}

// 取消预约的参数
export interface CancelReservationParams {
    userId: number
    isAdmin?: boolean
}

// 查询预约的参数
export interface QueryReservationParams {
    userId?: number
    roomId?: number
    reserveDate?: string
    approvalStatus?: 0 | 1 | 2
    page?: number
    size?: number
}

/**
 * 创建会议室预约
 * @param params 预约参数
 * @returns 创建的预约记录
 */
export const createReservation = (params: CreateReservationParams): Promise<Reservation> => {
    const userStore = useUserStore()

    // 构建请求数据，包含当前用户ID
    const requestData = {
        ...params,
        user_id: userStore.user?.userId || 0
    }

    return request.post('/api/reservations', requestData).then(response => {
        // 假设返回格式为 { data: { data: Reservation, ... }, ... }
        return response.data.data
    })
}

/**
 * 确认预约（审批通过）
 * @param reservationId 预约ID
 * @returns 操作结果
 */
export const confirmReservation = (reservationId: number): Promise<boolean> => {
    return request.put(`/api/reservations/${reservationId}/confirm`).then(response => {
        return response.data.success || true
    })
}


// 分页获取所有预约记录
export const getReservationsPage = (page: number, size: number) => {
    return request({
        url: '/api/reservations/page',
        method: 'get',
        params: { page, size}
    })
}


/**
 * 拒绝预约
 * @param reservationId 预约ID
 * @param reason 拒绝原因
 * @returns 操作结果
 */
export const rejectReservation = (reservationId: number, reason: string): Promise<boolean> => {
    return request.put(`/api/reservations/${reservationId}/reject`, null, {
        params: { reason }
    }).then(response => {
        return response.data.success || true
    })
}

/**
 * 开始使用预约
 * @param reservationId 预约ID
 * @returns 操作结果
 */
export const startReservation = (reservationId: number): Promise<boolean> => {
    return request.put(`/api/reservations/${reservationId}/start`).then(response => {
        return response.data.success || true
    })
}

/**
 * 完成预约
 * @param reservationId 预约ID
 * @returns 操作结果
 */
export const completeReservation = (reservationId: number): Promise<boolean> => {
    return request.put(`/api/reservations/${reservationId}/complete`).then(response => {
        return response.data.success || true
    })
}

/**
 * 取消预约
 * @param reservationId 预约ID
 * @param isAdmin 是否是管理员取消（可选）
 * @returns 操作结果
 */
export const cancelReservation = (reservationId: number, isAdmin: boolean = false): Promise<boolean> => {
    const userStore = useUserStore()

    return request.put(`/api/reservations/${reservationId}/cancel`, null, {
        params: {
            userId: userStore.user?.userId || 0,
            isAdmin: isAdmin
        }
    }).then(response => {
        return response.data.success || true
    })
}

/**
 * 获取用户的所有预约记录
 * @param userId 用户ID（可选，不传则使用当前用户）
 * @returns 预约记录列表
 */
export const getUserReservations = (userId?: number): Promise<Reservation[]> => {
    const userStore = useUserStore()
    const targetUserId = userId || userStore.user?.userId || 0

    return request.get(`/api/reservations/user/${targetUserId}`).then(response => {
        return response.data.data || []
    })
}

/**
 * 获取待处理的预约列表（管理员用）
 * @returns 待处理预约列表
 */
export const getPendingReservations = (): Promise<Reservation[]> => {
    return request.get('/api/reservations/pending').then(response => {
        return response.data.data || []
    })
}

/**
 * 删除预约
 * @param reservationId 预约ID
 * @returns 删除结果
 */
export const deleteReservation = (reservationId: number): Promise<boolean> => {
    return request.delete(`/api/reservations/${reservationId}`).then(response => {
        return response.data.success || true
    })
}

export const processExpiredReservations = (): Promise<{
    success: boolean
    message: string
    processedCount?: number
}> => {
    return request.post('/api/reservations/process-expired').then(response => {
        // 假设返回格式为 { data: { data: { processedCount: number }, ... }, ... }
        const data = response.data.data

        return {
            success: true,
            message: '过期预约处理完成',
            processedCount: data?.processedCount || 0
        }
    }).catch(error => {
        console.error('处理过期预约失败:', error)
        return {
            success: false,
            message: error.message || '处理过期预约失败',
            processedCount: 0
        }
    })
}




// 预约状态常量
export const RESERVATION_STATUS = {
    PENDING: 0,      // 待审批
    CONFIRMED: 1,    // 通过
    REJECTED: 2,     // 驳回
} as const

// 使用确认状态常量
export const USE_CONFIRM_STATUS = {
    NOT_USED: 0,     // 未使用
    USED: 1,         // 已使用
} as const

/**
 * 获取预约状态文本
 * @param status 状态码
 * @returns 状态文本
 */
export const getReservationStatusText = (status: 0 | 1 | 2): string => {
    switch (status) {
        case RESERVATION_STATUS.PENDING:
            return '待审批'
        case RESERVATION_STATUS.CONFIRMED:
            return '已通过'
        case RESERVATION_STATUS.REJECTED:
            return '已驳回'
        default:
            return '未知'
    }
}

/**
 * 获取使用确认状态文本
 * @param status 状态码
 * @returns 状态文本
 */
export const getUseConfirmStatusText = (status: 0 | 1): string => {
    switch (status) {
        case USE_CONFIRM_STATUS.NOT_USED:
            return '未使用'
        case USE_CONFIRM_STATUS.USED:
            return '已使用'
        default:
            return '未知'
    }
}

/**
 * 获取预约状态对应的颜色
 * @param status 状态码
 * @returns 颜色类型
 */
export const getReservationStatusColor = (status: 0 | 1 | 2): string => {
    switch (status) {
        case RESERVATION_STATUS.PENDING:
            return 'warning'
        case RESERVATION_STATUS.CONFIRMED:
            return 'success'
        case RESERVATION_STATUS.REJECTED:
            return 'danger'
        default:
            return 'info'
    }
}



