<template>
  <div class="all-list">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 1.2em; font-weight: bold">所有预约记录</span>
          <el-tag type="warning">管理员操作</el-tag>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="reservationRecord_id" label="ID" width="80" />
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">
            {{ getUserName(row.user_id) }}
          </template>
        </el-table-column>
        <el-table-column label="会议室" width="120">
          <template #default="{ row }">
            {{ getRoomName(row.room_id) }}
          </template>
        </el-table-column>
        <el-table-column label="会议主题" prop="purpose" show-overflow-tooltip />
        <el-table-column label="预约日期" width="100">
          <template #default="{ row }">
            {{ row.reserveDate }}
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="140">
          <template #default="{ row }">
            {{ formatTimeOnly(row.startTime) }} - {{ formatTimeOnly(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="canForceReject(row)"
                type="danger"
                size="small"
                @click="showForceRejectDialog(row)"
                :loading="row.forceRejecting"
            >
              强制驳回
            </el-button>
            <span v-else>——</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>

      <div v-if="tableData.length === 0 && !loading" class="empty-state">
        <el-icon :size="50"><ChatLineRound /></el-icon>
        <p>暂无预约记录</p>
      </div>
    </el-card>

    <!-- 强制驳回对话框 -->
    <el-dialog v-model="forceRejectDialog.visible" title="强制驳回预约" width="500px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        强制驳回将直接取消该预约，请谨慎操作！
      </el-alert>
      <el-form :model="forceRejectDialog.form">
        <el-form-item label="驳回理由">
          <el-input
              v-model="forceRejectDialog.form.rejectReason"
              type="textarea"
              :rows="4"
              placeholder="请输入驳回理由（2-200字符）"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forceRejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="handleForceReject" :loading="forceSubmitting">确认强制驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import type { MeetingRoom, Reservation, User } from '@/types'

import { getReservationsPage, rejectReservation } from '@/api/reservationRecord'
import { getUserById } from '@/api/user'
import { getRoomInfo } from '@/api/meetingRoom'

const loading = ref(false)
const forceSubmitting = ref(false)

const tableData = ref<Reservation[]>([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const forceRejectDialog = reactive({
  visible: false,
  currentRecord: null as Reservation | null,
  form: { rejectReason: '' }
})

const userCache = ref<Map<number, User>>(new Map())
const roomCache = ref<Map<number, MeetingRoom>>(new Map())

const loadData = async () => {
  try {
    loading.value = true
    const res = await getReservationsPage(pagination.currentPage - 1, pagination.pageSize)
    const { content, totalElements } = res.data.data
    tableData.value = content.map(item => ({
      reservationRecord_id: item.id || item.reservationRecord_id,
      user_id: item.user_id || item.userId,
      room_id: item.roomId || item.room_id,
      purpose: item.purpose,
      reserveDate: item.reserveDate,
      startTime: item.start_time,
      endTime: item.end_time,
      status: item.reservationRecord_status,
      createTime: item.createTime,
      forceRejecting: false
    }))
    console.log('映射后 tableData:', tableData.value)
    pagination.total = totalElements
  } catch (error) {
    ElMessage.error('获取预约记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadData()
}
const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
  loadData()
}

const formatTime = (timeStr: string) => dayjs(timeStr).format('MM-DD HH:mm')
const formatTimeOnly = (timeStr: string) => dayjs(`2000-01-01 ${timeStr}`).format('HH:mm')

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'danger',
    'COMPLETED': 'success',
    'IN_USE'  : 'success'
  }
  return map[status] || 'info'
}
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待审批',
    'CONFIRMED': '已通过',
    'REJECTED': '已驳回',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成',
    'IN_USE'  : '在使用'
  }
  return map[status] || status
}

const canForceReject = (row: Reservation) => {
  if (!row.reserveDate || !row.endTime) return false   // 注意这里是 endTime 不是 end_time
  const now = dayjs()
  const endDateTime = dayjs(`${row.reserveDate} ${row.endTime}`)
  // 只允许对已通过的预约进行强制驳回
  return endDateTime.isAfter(now) && (row.status === 'CONFIRMED')  // 注意这里是 status 不是 reservationRecord_status
}

const showForceRejectDialog = (row: Reservation) => {
  forceRejectDialog.currentRecord = row
  forceRejectDialog.form.rejectReason = ''
  forceRejectDialog.visible = true
}

const handleForceReject = async () => {
  const reason = forceRejectDialog.form.rejectReason.trim()
  if (reason.length < 2) {
    ElMessage.warning('请输入驳回理由（至少2个字符）')
    return
  }
  if (!forceRejectDialog.currentRecord) return
  const row = forceRejectDialog.currentRecord
  try {
    forceSubmitting.value = true
    // 直接使用普通驳回接口
    const success = await rejectReservation(row.reservationRecord_id, reason)
    console.log(success)
    if (success) {
      ElMessage.warning('已强制驳回')
      forceRejectDialog.visible = false
      await loadData()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  } finally {
    forceSubmitting.value = false
  }
}

const getUserName = (userId: number) => {
  if (!userId) return '未知用户'  // 关键：避免传入 undefined
  if (userCache.value.has(userId)) {
    return userCache.value.get(userId)!.sysUsername || `用户${userId}`
  }
  fetchUserName(userId)
  return `用户${userId}`
}

const fetchUserName = async (userId: number) => {
  if (!userId) return  // 再次检查
  try {
    const end_user = await getUserById(userId)
    console.log(end_user)
    userCache.value.set(userId, end_user)
  } catch (error) {
    console.error(`获取用户${userId}信息失败:`, error)
    // 可存入一个默认对象，避免反复请求
    userCache.value.set(userId, { sysUsername: `用户${userId}` } as end_user)
  }
}

const getRoomName = (roomId: number) => {
  if(!roomId) return
  if (roomCache.value.has(roomId)) {
    return roomCache.value.get(roomId)!.roomName || `会议室${roomId}`
  }
  fetchRoomName(roomId)
  return `会议室${roomId}`
}
const fetchRoomName = async (roomId: number) => {
  if (!roomId) return
  try {
    const room = await getRoomInfo(roomId)
    roomCache.value.set(roomId, room)
  } catch (error) {
    console.error(`获取会议室${roomId}信息失败:`, error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.allReservationRecord-list {
  padding: 20px;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>