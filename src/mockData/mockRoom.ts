// src/api/mockRoom.ts
import type { MeetingRoom } from '@/types'

export const mockRoomList: MeetingRoom[] = [
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
    roomStatus: 0, // 0表示不可用
    createUserId: 1,
  },
  // 可以多复制几项，用于测试分页
]
