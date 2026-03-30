// src/types/index.ts
export interface User {

  userId: number
  username: string
  real_name: string
  roleType: number // 0-用户, 1-管理员
  phone?: string
  email?: string
  status: 0 | 1 // 0-禁用, 1-正常
}

export interface MeetingRoom {
  roomId: number
  roomName: string
  roomNo: string
  capacity: number
  area?: number
  description?: string
  roomStatus: 0 | 1 // 0-不可用, 1-可用
  photoUrl?: string
  createUserId: number
  createTime?: string
  updateTime?: string
}

export enum ReservationStatus {
    PENDING = 'PENDING',      // 待审核
    CONFIRMED = 'CONFIRMED',  // 已确认
    IN_USE = 'IN_USE',        // 使用中
    COMPLETED = 'COMPLETED',  // 已完成
    CANCELLED = 'CANCELLED',  // 已取消
    REJECTED = 'REJECTED'     // 已拒绝
}

export interface RegisterParams{
    userName: string ;
    password: string;
    confirmPassword: string ;
    phone: string;
    realName: string;
    email: string;
    department_name: string;

}

export interface Reservation {
  reservationRecord_id: number
  user_id: number
  room_id: number
  purpose: string
  reserveDate: string
  start_time: string
  end_time: string
  expectedNum: number
    reservationRecord_status: ReservationStatus // 使用枚举类型
  rejected_reason?: string
  approverId?: number
  approvalTime?: string
  useConfirm?: 0 | 1 // 0-未使用, 1-已使用
    created_time: string
    updated_time: string
}

export interface LoginData {
    username: string
    password: string
    captcha:  string
}

// 后端返回的数据结构
export interface BackendLoginData {
    user: {
        id: number
        password: string
        realName: string
        email: string
        status: number
        role: string
        phone?: string
        createTime: string
        updateTime: string
        department_name: string | null
        sysUsername: string
    }
    token: string
}

// 后端返回的数据结构
export interface BackendRegisterData {

        id: number
        password: string
        realName: string
        email: string
        status: number
        role: string
        phone?: string
        createTime: string
        updateTime: string
        department_name: string | null
        sysUsername: string

}


export interface LoginResponse {
    userId: number
    username: string
    roleType: string
    token: string
}

export interface RegisterResponse {
    userId: number
    username: string
    roleType: string
}

export interface ApiResponse<T = unknown> {
    success:boolean
    message: string
    data: T
    code: number
}
// 新增：通用API响应类型
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

// 新增：删除会议室响应类型
export interface DeleteRoomResponse {
  code: number
  msg: string
  data: null
}
