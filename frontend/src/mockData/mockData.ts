// src/api/mockData.ts
import type { MeetingRoom, Reservation, User } from '@/types'

// 1. 模拟用户数据
export const mockUsers: User[] = [
  { userId: 1, username: 'admin', realName: '系统管理员', roleType: 1, status: 1 },
  { userId: 2, username: 'zhangsan', realName: '张三', roleType: 0, status: 1 },
  { userId: 3, username: 'lisi', realName: '李四', roleType: 0, status: 1 },
]

// 2. 模拟会议室数据 (与之前mockRoom.ts保持一致，但移到这里集中管理)
export const mockRooms: MeetingRoom[] = [
  {
    roomId: 1,
    roomName: '第一会议室',
    roomNo: '101',
    capacity: 20,
    area: 50.0,
    description: '配备投影仪和视频会议系统',
    roomStatus: 1,
    createUserId: 1,
  },
  {
    roomId: 2,
    roomName: '第二会议室',
    roomNo: '102',
    capacity: 30,
    area: 80.0,
    description: '中型会议室，适合部门会议',
    roomStatus: 1,
    createUserId: 1,
  },
  {
    roomId: 3,
    roomName: 'VIP会议室',
    roomNo: '201',
    capacity: 10,
    area: 30.0,
    description: '高层专用，环境安静',
    roomStatus: 0, // 不可用
    createUserId: 1,
  },
]

// 3. 模拟预约记录数据
export const mockReservations: Reservation[] = [
  {
    recordId: 1001,
    userId: 2, // 张三
    roomId: 1, // 第一会议室
    meetingTopic: '项目启动会',
    reserveDate: '2024-12-01', // 未来的日期，用于演示
    startTime: '09:00:00',
    endTime: '11:00:00',
    expectedNum: 15,
    approvalStatus: 1, // 已通过
    useConfirm: 0,
    createTime: '2024-11-28T09:00:00',
    updateTime: '2024-11-28T09:30:00',
  },
  {
    recordId: 1002,
    userId: 3, // 李四
    roomId: 2, // 第二会议室
    meetingTopic: '周例会',
    reserveDate: '2024-12-01',
    startTime: '14:00:00',
    endTime: '15:30:00',
    expectedNum: 10,
    approvalStatus: 0, // 待审批
    useConfirm: 0,
    createTime: '2024-11-29T10:00:00',
    updateTime: '2024-11-29T10:00:00',
  },
]

// 4. 辅助函数：根据时间、人数等条件筛选可用会议室
export function getAvailableRooms(
  date: string,
  startTime: string,
  endTime: string,
  expectedNum: number,
): MeetingRoom[] {
  // 过滤掉状态不可用的会议室
  return mockRooms.filter((room) => {
    if (room.roomStatus !== 1) return false
    if (room.capacity < expectedNum) return false

    // 检查该会议室在指定时间段是否有冲突的预约
    const hasConflict = mockReservations.some((res) => {
      return (
        res.roomId === room.roomId &&
        res.reserveDate === date &&
        res.approvalStatus !== 2 && // 已驳回的不算冲突
        !(endTime <= res.startTime || startTime >= res.endTime) // 时间有重叠
      )
    })
    return !hasConflict
  })
}

// 5. 辅助函数：新增预约（模拟）
export function addMockReservation(
  newRes: Omit<Reservation, 'recordId' | 'createTime' | 'updateTime'>,
): number {
  const newRecordId = Math.max(...mockReservations.map((r) => r.recordId), 1000) + 1
  const fullRes: Reservation = {
    ...newRes,
    recordId: newRecordId,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString(),
  }
  mockReservations.push(fullRes)
  return newRecordId
}

// --- 在 src/api/mockData.ts 文件末尾添加 ---

// 1. 模拟系统通知数据
export interface MockNotice {
  noticeId: number
  receiveUserId: number
  noticeType: number // 1-预约待审批，2-审批结果通知
  relatedId: number // 关联的预约ID
  noticeContent: string
  readStatus: 0 | 1 // 0-未读，1-已读
  createTime: string
}

export const mockNotices: MockNotice[] = [
  {
    noticeId: 1,
    receiveUserId: 2, // 用户123
    noticeType: 2,
    relatedId: 1001, // 关联预约ID
    noticeContent: '您的预约“项目启动会”已通过审批。',
    readStatus: 0,
    createTime: '2024-11-29T09:30:00',
  },
  {
    noticeId: 2,
    receiveUserId: 3, // 用户李四
    noticeType: 1,
    relatedId: 1002,
    noticeContent: '您有一个新的预约“周例会”待审批。',
    readStatus: 1,
    createTime: '2024-11-29T10:00:00',
  },
]

// 2. 辅助函数：为用户生成通知（模拟审批后调用）
export function createApprovalNotice(
  receiveUserId: number,
  relatedId: number,
  isApproved: boolean,
  rejectReason?: string,
): number {
  const newNoticeId = Math.max(...mockNotices.map((n) => n.noticeId), 0) + 1
  const content = isApproved
    ? `您的预约申请 (#${relatedId}) 已通过审批。`
    : `您的预约申请 (#${relatedId}) 已被驳回。理由：${rejectReason || '无'}`

  const newNotice: MockNotice = {
    noticeId: newNoticeId,
    receiveUserId,
    noticeType: 2,
    relatedId,
    noticeContent: content,
    readStatus: 0, // 默认未读
    createTime: new Date().toISOString(),
  }
  mockNotices.unshift(newNotice) // 添加到开头
  return newNoticeId
}

// 3. 辅助函数：获取用户的通知
export function getUserNotices(userId: number): MockNotice[] {
  return mockNotices
    .filter((notice) => notice.receiveUserId === userId)
    .sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
}

// 4. 辅助函数：标记通知为已读
export function markNoticeAsRead(noticeId: number): void {
  const notice = mockNotices.find((n) => n.noticeId === noticeId)
  if (notice) notice.readStatus = 1
}

// 5. 辅助函数：获取用户未读通知数
export function getUnreadCount(userId: number): number {
  return mockNotices.filter((n) => n.receiveUserId === userId && n.readStatus === 0).length
}
