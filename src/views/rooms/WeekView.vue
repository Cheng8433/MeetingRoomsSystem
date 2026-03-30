<template>
  <div class="week-view">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 1.2em; font-weight: bold">会议室周视图</span>
          <div>
            <el-button-group>
              <el-button :icon="ArrowLeft" @click="changeWeek(-1)">上一周</el-button>
              <el-button @click="goToToday">今天</el-button>
              <el-button @click="changeWeek(1)" :icon="ArrowRight">下一周</el-button>
            </el-button-group>
            <el-select
              v-model="selectedRoomId"
              placeholder="筛选会议室"
              style="margin-left: 20px; width: 200px"
            >
              <el-option label="全部会议室" :value="null" />
              <el-option
                v-for="room in availableRooms"
                :key="room.roomId"
                :label="`${room.roomName} (${room.roomNo})`"
                :value="room.roomId"
              />
            </el-select>
          </div>
        </div>
        <div style="text-align: center; margin-top: 10px; font-size: 1.1em; color: #333">
          {{ weekTitle }}
        </div>
      </template>

      <!-- 周视图表格 -->
      <div class="week-table-container">
        <table class="week-table">
          <thead>
            <tr>
              <th class="time-header">时间/会议室</th>
              <th v-for="day in weekDays" :key="day.date" :class="{ today: day.isToday }">
                <div>{{ day.weekDay }}</div>
                <div :class="{ 'today-date': day.isToday }">{{ day.monthDay }}</div>
              </th>
            </tr>
          </thead>
          <tbody>
            <!-- 每个时间段一行 -->
            <tr v-for="timeSlot in timeSlots" :key="timeSlot">
              <td class="time-slot">{{ timeSlot }}</td>
              <!-- 每个会议室在每一天的单元格 -->
              <td v-for="day in weekDays" :key="`${day.date}-${timeSlot}`">
                <!-- 筛选并显示该会议室在该日期该时间段的预约 -->
                <div
                  v-for="res in getReservationsForCell(day.date, timeSlot)"
                  :key="res.recordId"
                  class="reservation-block"
                  :class="getStatusClass(res)"
                  :title="`${res.meetingTopic} (${getUserName(res.userId)})`"
                >
                  <div class="reservation-topic">{{ res.meetingTopic }}</div>
                  <div class="reservation-time">
                    {{ res.startTime.slice(0, 5) }}-{{ res.endTime.slice(0, 5) }}
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 图例说明 -->
      <div style="margin-top: 30px; padding: 15px; background-color: #f8f9fa; border-radius: 4px">
        <h4 style="margin-top: 0">图例说明：</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 20px">
          <div style="display: flex; align-items: center">
            <span
              class="status-demo"
              style="background-color: #f0f9ff; border-color: #409eff"
            ></span>
            <span>待审批</span>
          </div>
          <div style="display: flex; align-items: center">
            <span
              class="status-demo"
              style="background-color: #f0f9ff; border-left: 4px solid #67c23a"
            ></span>
            <span>已通过</span>
          </div>
          <div style="display: flex; align-items: center">
            <span
              class="status-demo"
              style="background-color: #fef0f0; border-color: #f56c6c"
            ></span>
            <span>已驳回</span>
          </div>
          <div style="display: flex; align-items: center">
            <span
              class="status-demo"
              style="background-color: #f4f4f5; border-color: #909399"
            ></span>
            <span>会议室不可用</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { mockRooms, mockReservations, mockUsers } from '@/mockData/mockData.ts'
import type { Reservation, MeetingRoom } from '@/types'

// 当前周的基准日期（默认本周一）
const baseDate = ref(dayjs().startOf('week').add(1, 'day')) // 以周一为起点

// 筛选的会议室ID
const selectedRoomId = ref<number | null>(null)

// 生成一周的日期数组
const weekDays = computed(() => {
  const days = []
  for (let i = 0; i < 7; i++) {
    const date = baseDate.value.add(i, 'day')
    const today = dayjs().isSame(date, 'day')
    days.push({
      date: date.format('YYYY-MM-DD'),
      weekDay: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
      monthDay: date.format('MM/DD'),
      isToday: today,
    })
  }
  return days
})

// 周标题
const weekTitle = computed(() => {
  const start = baseDate.value.format('YYYY年MM月DD日')
  const end = baseDate.value.add(6, 'day').format('YYYY年MM月DD日')
  return `${start} - ${end}`
})

// 时间段 (8:00 - 20:00，每小时一格)
const timeSlots = computed(() => {
  const slots = []
  for (let hour = 8; hour <= 20; hour++) {
    slots.push(`${hour.toString().padStart(2, '0')}:00`)
  }
  return slots
})

// 可用的会议室列表
const availableRooms = computed(() => {
  return mockRooms.filter((room) => room.roomStatus === 1)
})

// 获取用于显示的会议室列表（根据筛选）
const displayRooms = computed(() => {
  if (selectedRoomId.value === null) {
    return availableRooms.value
  }
  return availableRooms.value.filter((room) => room.roomId === selectedRoomId.value)
})

// 根据状态获取CSS类
const getStatusClass = (res: Reservation) => {
  switch (res.approvalStatus) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-approved'
    case 2:
      return 'status-rejected'
    default:
      return ''
  }
}

// 获取用户名
const getUserName = (userId: number) => {
  const user = mockUsers.find((u) => u.userId === userId)
  return user ? user.realName : `用户${userId}`
}

// 获取某个单元格（特定日期、特定时间段开始）的预约
const getReservationsForCell = (date: string, timeSlot: string) => {
  const [hour] = timeSlot.split(':').map(Number)
  const slotStart = `${hour.toString().padStart(2, '0')}:00:00`

  return mockReservations.filter((res) => {
    // 日期匹配
    if (res.reserveDate !== date) return false
    // 时间匹配：预约的开始时间在这个时间段内
    if (res.startTime.slice(0, 5) !== timeSlot) return false
    // 会议室匹配
    if (selectedRoomId.value !== null && res.roomId !== selectedRoomId.value) return false
    // 只显示已通过或待审批的
    return res.approvalStatus !== 2
  })
}

// 切换周
const changeWeek = (weeks: number) => {
  baseDate.value = baseDate.value.add(weeks, 'week')
}

// 回到本周
const goToToday = () => {
  baseDate.value = dayjs().startOf('week').add(1, 'day')
}

onMounted(() => {
  // 初始化逻辑
})
</script>

<style scoped>
.week-view {
  padding: 20px;
}

.week-table-container {
  overflow-x: auto;
  margin-top: 20px;
}

.week-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

.week-table th,
.week-table td {
  border: 1px solid #ebeef5;
  padding: 8px;
  text-align: center;
  vertical-align: top;
  min-width: 140px;
  height: 80px;
}

.week-table th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.time-header {
  width: 100px;
  background-color: #fafafa;
}

.time-slot {
  font-size: 0.9em;
  color: #666;
  background-color: #fafafa;
}

.today {
  background-color: #f0f9ff !important;
  color: #409eff;
}

.today-date {
  font-weight: bold;
}

.reservation-block {
  margin: 2px;
  padding: 6px;
  border-radius: 4px;
  border-left: 4px solid #409eff;
  font-size: 0.85em;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
}

.reservation-block:hover {
  opacity: 0.9;
}

.status-pending {
  background-color: #f0f9ff;
  border-color: #409eff;
}

.status-approved {
  background-color: #f0f9ff;
  border-left-color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.reservation-topic {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reservation-time {
  font-size: 0.8em;
  color: #666;
  margin-top: 2px;
}

.status-demo {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 1px solid;
  border-radius: 4px;
  margin-right: 8px;
}
</style>
