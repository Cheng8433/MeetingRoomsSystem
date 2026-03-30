// src/api/meetingRoom.ts
import {request} from '@/utils/requests'
import type { MeetingRoom } from '@/types'
import { http } from '@/utils/requests'
import type { ApiResponse } from '@/types'
import {useUserStore} from "@/stores/user.ts";

// 获取单个会议室信息（需要 roomId）
export const getRoomInfo = (roomId: number): Promise<MeetingRoom> => {
    return request.get<ApiResponse<MeetingRoom>>(`/api/meeting-rooms/${roomId}`).then(response => {
        const apiResponse = response.data
        return apiResponse.data
    })
}

// 创建会议室请求参数
export interface CreateMeetingRoomParams {
    roomName: string
    roomNo: string
    capacity: number
    area?: number | null
    description?: string
    roomStatus: 0 | 1
    photoUrl?: string | null
    createUserId:number
}

// 更新会议室请求参数
export interface UpdateMeetingRoomParams extends Partial<CreateMeetingRoomParams> {
    roomId: number
}

// 查询会议室参数
export interface QueryMeetingRoomParams {
    page?: number
    size?: number
    roomName?: string
    roomNo?: string
    capacityMin?: number
    capacityMax?: number
    roomStatus?: number
    createUserId?: number
    minCapacity : number
}

// 会议室列表响应
export interface MeetingRoomListResponse {
    list: MeetingRoom[]
    total: number
    page: number
    size: number
}

// 创建会议室
export const createMeetingRoom = (params: CreateMeetingRoomParams): Promise<MeetingRoom> => {
    const userStore = useUserStore()

    const requestData = {
        ...params,
        createUserId: userStore.user?.userId || 0,
    }

    return request.post('/api/meeting-rooms', requestData).then(response => {
        // 假设 response 是 ApiResponse<MeetingRoom>
        return response.data.data
    })
}

// 更新会议室
export const updateMeetingRoom = (roomId: number, data: Partial<UpdateMeetingRoomParams>): Promise<MeetingRoom> => {
    return request.put<ApiResponse<MeetingRoom>>(`/api/meeting-rooms/${roomId}`, data)
        .then(res => res.data.data)
}

// 获取会议室列表
export const getAllRooms = () : Promise<MeetingRoom[]>  => {
    return request.get<ApiResponse<MeetingRoom[]>>('/api/meeting-rooms/available')
        .then(res => res.data.data)
}

// 条件筛选会议室
export const getAvailableRooms = () : Promise<MeetingRoom[]>  => {
  return request.get<ApiResponse<MeetingRoom[]>>('/api/meeting-rooms/available')
            .then(res => res.data.data)
}

export const getRoomsByCapacity = (minCapacity: number): Promise<MeetingRoom[]> => {
  return request.get<ApiResponse<MeetingRoom[]>>('/api/meeting-rooms/capacity', {
    params: { minCapacity}}).then(res => res.data.data)
}

// 设置会议室状态
export const updateRoomStatus = (roomId: number, roomStatus: 0 | 1): Promise<MeetingRoom> => {
  return request.patch<ApiResponse<MeetingRoom>>(`/api/meeting-rooms/${roomId}/status`, null, {
    params: { roomStatus },
  }).then(res => res.data.data)
}

export async function deleteMeetingRoom(roomId: number): Promise<void> {
  try {
    // 这里直接调用http.delete，它会返回data字段的内容
    // 由于后端返回的data为null，所以这里返回void
    await http.delete(`/api/meeting-rooms/${roomId}`)
  } catch (error) {
    // 将错误重新抛出，让调用方处理
    throw error
  }
}

// 分页响应类型
export interface PaginationResponse<T> {
    list: T[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 分页查询会议室
export const getRoomsByPage = (params: {
    page?: number
    size?: number
}): Promise<PaginationResponse<MeetingRoom[]>> => {
    return request.get<ApiResponse<PaginationResponse<MeetingRoom[]>>>('/api/meeting-rooms/page', { params })
        .then(res => res.data.data)
}

// 模糊搜索查询会议室
export const getRoomsByFuzzySearch = (params: {
    searchKeyWord: string
}): Promise<PaginationResponse<MeetingRoom[]>> => {
    return request.get<ApiResponse<PaginationResponse<MeetingRoom[]>>>('/api/search/rooms', { params })
        .then(res => res.data.data)
}




