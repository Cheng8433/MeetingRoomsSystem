<template>
  <div class="my-reservations">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 1.2em; font-weight: bold">我的预约记录</span>
          <el-button type="primary" @click="$router.push('/reservations/create')">
            <el-icon><Plus /></el-icon>
            新建预约
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div style="margin-bottom: 20px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center">
        <!-- 状态筛选 -->
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="待审批" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="使用中" value="IN_USE" />
          <el-option label="已取消" value="CANCELLED" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>

        <!-- 日期筛选 -->
        <el-date-picker
            v-model="filterDate"
            type="date"
            placeholder="预约日期"
            style="width: 150px"
            clearable
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
        />

        <!-- 会议室搜索 -->
        <el-select
            v-model="filterRoomId"
            placeholder="会议室筛选"
            type = "int"
            clearable
            filterable
            style="width: 180px"
            @focus="loadAllRooms"
        >
          <!-- 添加"所有会议室"选项 -->
          <el-option
              label="所有会议室"
              :value="null"
          />

          <el-option
              v-for="room in availableRooms"
              :key="room.roomId"
              :label="`${room.roomName} (${room.roomNo})`"
              :value="room.roomId"
          />
        </el-select>

        <!-- 搜索按钮 -->
        <el-button type="primary" @click="handleSearch" :loading="loading">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <!-- 重置按钮 -->
        <el-button @click="handleReset" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <el-table :data="pagedReservations" style="width: 100%" v-loading="loading">
        <el-table-column prop="reservationRecord_id" label="预约号" width="100" />
        <el-table-column label="会议室" width="140">
          <template #default="{ row }">
            {{ getRoomName(row.room_id) }}
          </template>
        </el-table-column>
        <el-table-column label="会议主题" prop="purpose" show-overflow-tooltip />

        <!-- 预约日期 -->
        <el-table-column label="预约日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.reserveDate) }}
          </template>
        </el-table-column>

        <!-- 开始时间 -->
        <el-table-column label="开始时间" width="100">
          <template #default="{ row }">
            <div style="font-size: 0.9em">
              {{ formatTime(row.start_time) }}
            </div>
          </template>
        </el-table-column>

        <!-- 结束时间 -->
        <el-table-column label="结束时间" width="100">
          <template #default="{ row }">
            <div style="font-size: 0.9em">
              {{ formatTime(row.end_time) }}
            </div>
          </template>
        </el-table-column>

        <!-- 预约时间段状态 -->
        <el-table-column label="时间段" width="100">
          <template #default="{ row }">
            <div v-if="isInProgress(row)" style="color: #67c23a; font-size: 0.9em">
              <el-icon><Clock /></el-icon>
              进行中
            </div>
            <div v-else-if="isToday(row.reserveDate)" style="color: #e6a23c; font-size: 0.9em">
              今日
            </div>
            <div v-else-if="isPast(row.reserveDate)" style="color: #909399; font-size: 0.9em">
              已过期
            </div>
            <div v-else style="color: #409eff; font-size: 0.9em">
              未开始
            </div>
          </template>
        </el-table-column>

        <el-table-column label="人数" width="80">
          <template #default="{ row }">
            <span style="font-weight: 500">{{ row.expectedNum }}人</span>
          </template>
        </el-table-column>

        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 2px">
              <el-tag
                  :type="getStatusType(row.reservationRecord_status)"
                  size="small"
                  style="width: fit-content"
              >
                {{ getStatusText(row.reservationRecord_status) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="使用状态" width="120">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 2px">
              <el-tag
                  v-if="row.useConfirm !== undefined"
                  :type="row.useConfirm === 1 ? 'success' : 'info'"
                  size="small"
                  style="width: fit-content"
              >
                {{ getUseConfirmText(row.useConfirm) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap">
              <!-- 取消按钮 - 仅限待审批状态 -->
              <el-button
                  v-if="canCancel(row)"
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                  plain
              >
                取消
              </el-button>

              <!-- 开始按钮 - 仅限已通过且未开始 -->
              <el-button
                  v-if="canStart(row)"
                  type="success"
                  size="small"
                  @click="handleStart(row)"
                  :loading="startingId === row.reservationRecord_id"
              >
                开始
              </el-button>

              <!-- 完成按钮 - 仅限已开始 -->
              <el-button
                  v-if="canComplete(row)"
                  type="primary"
                  size="small"
                  @click="handleComplete(row)"
                  :loading="completingId === row.reservationRecord_id"
              >
                完成
              </el-button>

              <!-- 详情按钮 -->
              <el-button
                  type="info"
                  size="small"
                  @click="handleViewDetail(row)"
                  plain
              >
                详情
              </el-button>

              <span v-if="!hasActions(row)" style="color: #999; font-size: 12px">
                无操作
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="filteredReservations.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        :title="`预约详情 - ${selectedReservation?.purpose || ''}`"
        width="600px"
    >
      <div v-if="selectedReservation" style="line-height: 1.8">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约号">
            {{ selectedReservation.reservationRecord_id }}
          </el-descriptions-item>
          <el-descriptions-item label="会议室">
            {{ getRoomName(selectedReservation.room_id) }}
          </el-descriptions-item>
          <el-descriptions-item label="会议主题">
            {{ selectedReservation.purpose }}
          </el-descriptions-item>
          <el-descriptions-item label="预约日期">
            {{ formatDate(selectedReservation.reserveDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatTime(selectedReservation.start_time) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatTime(selectedReservation.end_time) }}
          </el-descriptions-item>
          <el-descriptions-item label="预计人数">
            {{ selectedReservation.expectedNum }}人
          </el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(selectedReservation.reservationRecord_status)">
              {{ getStatusText(selectedReservation.reservationRecord_status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="使用状态">
            <el-tag :type="selectedReservation.useConfirm === 1 ? 'success' : 'info'">
              {{ getUseConfirmText(selectedReservation.useConfirm) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedReservation.rejected_reason" label="驳回原因" :span="2">
            {{ selectedReservation.rejected_reason }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedReservation.created_time) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedReservation.approvalTime" label="审批时间" :span="2">
            {{ formatDateTime(selectedReservation.approvalTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
            v-if="selectedReservation && canCancel(selectedReservation)"
            type="danger"
            @click="handleCancel(selectedReservation)"
        >
          取消预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, Search, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import {type Reservation, type MeetingRoom, ReservationStatus} from '@/types'

// 导入API函数
import {
  getUserReservations,
  cancelReservation,
  startReservation,
  completeReservation
} from '@/api/reservationRecord'
import { getRoomInfo, getRoomsByPage } from '@/api/meetingRoom.ts'

const userStore = useUserStore()
const loading = ref(false)
const startingId = ref<number | null>(null)
const completingId = ref<number | null>(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选相关
const filterStatus = ref('all')
const filterDate = ref('')
const filterRoomId = ref<number | null>(null)

// 对话框相关
const detailDialogVisible = ref(false)
const selectedReservation = ref<Reservation | null>(null)

// 预约数据
const allReservations = ref<Reservation[]>([])

// 会议室数据
const availableRooms = ref<MeetingRoom[]>([])
const loadingRooms = ref(false)

// 加载所有会议室（用于筛选）
const loadAllRooms = async () => {
  if (availableRooms.value.length > 0 || loadingRooms.value) return

  loadingRooms.value = true
  try {
    const rooms = await getRoomsByPage({ page: 1, size: 1000 })
    availableRooms.value = Array.isArray(rooms) ? rooms : []
  } catch (error) {
    console.error('加载会议室列表失败:', error)
  } finally {
    loadingRooms.value = false
  }
}

// 加载预约数据
const loadReservations = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    // 调用真实API获取用户预约记录
    const data = await getUserReservations(userId)
    allReservations.value = data || []

    // 按创建时间倒序排列
    allReservations.value.sort((a, b) =>
        new Date(b.created_time).getTime() - new Date(a.created_time).getTime()
    )

  } catch (error: any) {
    console.error('加载预约记录失败:', error)
    ElMessage.error(`加载失败: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

// 搜索按钮处理
const handleSearch = () => {
  currentPage.value = 1 // 搜索后回到第一页
  // 这里可以添加后端搜索逻辑，目前只是前端筛选
  // 如果有后端搜索API，可以在这里调用
  // 目前我们使用前端筛选，所以只是触发重新筛选
}

// 重置按钮处理
const handleReset = () => {
  filterStatus.value = 'all'
  filterDate.value = ''
  filterRoomId.value = null
  currentPage.value = 1
}

// 筛选后的预约记录
const filteredReservations = computed(() => {
  let result = allReservations.value

  // 状态筛选
  if (filterStatus.value !== 'all') {
    result = result.filter(res => res.reservationRecord_status === filterStatus.value)
  }

  // 日期筛选
  if (filterDate.value) {
    const selectedDate = dayjs(filterDate.value).format('YYYY-MM-DD')
    result = result.filter(res => res.reserveDate === selectedDate)
  }

  // 会议室筛选
  if (filterRoomId.value) {
    result = result.filter(res => res.room_id === filterRoomId.value)
  }

  return result
})

// 分页后的预约记录
const pagedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReservations.value.slice(start, end)
})

// 会议室缓存
interface RoomCache {
  [key: number]: {
    name: string
    loading: boolean
    error?: boolean
  }
}

const roomCache = ref<RoomCache>({})

// 获取会议室名称的函数
const getRoomName = (room_id: number): string => {
  // 先从availableRooms中查找
  const roomFromList = availableRooms.value.find(r => r.roomId === room_id)
  if (roomFromList) {
    return `${roomFromList.roomName} (${roomFromList.roomNo})`
  }

  // 如果缓存中有数据，直接返回
  if (roomCache.value[room_id]) {
    const cache = roomCache.value[room_id]
    if (cache.error) {
      return `会议室 #${room_id}`
    }
    return cache.name
  }

  // 初始化缓存并异步加载
  roomCache.value[room_id] = {
    name: `会议室 #${room_id}`,
    loading: true
  }

  // 异步获取会议室信息
  getRoomInfo(room_id)
      .then(roomInfo => {
        if (roomInfo && roomInfo.roomName && roomInfo.roomNo) {
          const roomName = `${roomInfo.roomName} (${roomInfo.roomNo})`
          roomCache.value[room_id] = {
            name: roomName,
            loading: false
          }
        } else {
          roomCache.value[room_id] = {
            name: `会议室 #${room_id}`,
            loading: false,
            error: true
          }
        }
      })
      .catch(error => {
        console.error(`获取会议室 ${room_id} 信息失败:`, error)
        roomCache.value[room_id] = {
          name: `会议室 #${room_id}`,
          loading: false,
          error: true
        }
      })

  return `会议室 #${room_id}`
}

// 格式化函数
const formatDate = (dateStr: string | undefined | null): string => {
  if (!dateStr) return '--'
  try {
    return dayjs(dateStr).format('YYYY-MM-DD')
  } catch (error) {
    console.error('日期格式化错误:', dateStr, error)
    return '--'
  }
}

const formatTime = (timeStr: string | undefined | null): string => {
  if (!timeStr) return '--'
  try {
    if (timeStr.length >= 5) {
      return timeStr.slice(0, 5)
    }
    const paddedTime = timeStr.padEnd(5, '0')
    return paddedTime.slice(0, 5)
  } catch (error) {
    console.error('时间格式化错误:', timeStr, error)
    return '--'
  }
}

const formatDateTime = (timeStr: string | undefined | null): string => {
  if (!timeStr) return '--'
  try {
    return dayjs(timeStr).format('YYYY-MM-DD HH:mm')
  } catch (error) {
    console.error('日期时间格式化错误:', timeStr, error)
    return '--'
  }
}

// 状态标签类型和文本
const getStatusType = (status: ReservationStatus) => {
  const map: Record<ReservationStatus, '' | 'success' | 'warning' | 'danger' | 'info' | 'primary'> = {
    [ReservationStatus.PENDING]: 'warning',    // 待审核 - 黄色
    [ReservationStatus.CONFIRMED]: 'success',  // 已确认 - 绿色
    [ReservationStatus.IN_USE]: 'primary',     // 使用中 - 蓝色
    [ReservationStatus.COMPLETED]: 'info',     // 已完成 - 灰色
    [ReservationStatus.CANCELLED]: 'danger',   // 已取消 - 红色
    [ReservationStatus.REJECTED]: 'danger'     // 已拒绝 - 红色
  }
  return map[status] || ''
}

//
const getStatusText = (status: ReservationStatus) => {
  const map: Record<ReservationStatus, string> = {
    [ReservationStatus.PENDING]: '待审核',
    [ReservationStatus.CONFIRMED]: '已确认',
    [ReservationStatus.IN_USE]: '使用中',
    [ReservationStatus.COMPLETED]: '已完成',
    [ReservationStatus.CANCELLED]: '已取消',
    [ReservationStatus.REJECTED]: '已拒绝'
  }
  return map[status] || '未知'
}

// 使用确认状态文本
const getUseConfirmText = (status: number = 0) => {
  return status === 1 ? '已使用' : '未使用'
}

// 检查是否今天
const isToday = (dateStr: string | undefined): boolean => {
  if (!dateStr) return false
  const today = dayjs().format('YYYY-MM-DD')
  return dateStr === today
}

// 检查是否已过期
const isPast = (dateStr: string | undefined): boolean => {
  if (!dateStr) return false
  const today = dayjs().format('YYYY-MM-DD')
  return dateStr < today
}

// 检查预约是否正在进行中
const isInProgress = (row: Reservation): boolean => {
  if (row.reservationRecord_status !== 'IN_USE') return false

  const now = dayjs()
  const today = now.format('YYYY-MM-DD')

  if (!row.reserveDate || row.reserveDate !== today) return false

  const currentTime = now.format('HH:mm')
  return currentTime >= (row.start_time || '') && currentTime <= (row.end_time || '')
}

// 操作权限检查
const canCancel = (row: Reservation): boolean => {
  // 待审批状态可以取消
  return row.reservationRecord_status === 'PENDING'
}

const canStart = (row: Reservation): boolean => {
  // 已通过、未使用、且当前时间在预约开始时间前10分钟到结束时间之间
  if (row.reservationRecord_status !== 'CONFIRMED' || row.useConfirm === 1) return false

  const now = dayjs()
  const today = now.format('YYYY-MM-DD')

  if (!row.reserveDate || row.reserveDate !== today) return false

  if (!row.start_time || !row.end_time) return false

  try {
    const start_time = dayjs(`${row.reserveDate} ${row.start_time}`)
    const end_time = dayjs(`${row.reserveDate} ${row.end_time}`)

    // 允许提前10分钟开始
    const canStartTime = start_time.subtract(10, 'minute')

    return now.isAfter(canStartTime) && now.isBefore(end_time)
  } catch (error) {
    console.error('时间解析错误:', row.start_time, row.end_time, error)
    return false
  }
}

const canComplete = (row: Reservation): boolean => {
  // 已通过、已开始（useConfirm=1）、且预约已经开始
  if (row.reservationRecord_status !== 'CONFIRMED' || row.useConfirm !== 1) return false

  if (!row.reserveDate || !row.start_time) return false

  try {
    const now = dayjs()
    const start_time = dayjs(`${row.reserveDate} ${row.start_time}`)
    return now.isAfter(start_time)
  } catch (error) {
    console.error('时间解析错误:', row.start_time, error)
    return false
  }
}

const hasActions = (row: Reservation): boolean => {
  return canCancel(row) || canStart(row) || canComplete(row)
}

// 取消预约
const handleCancel = async (row: Reservation) => {
  try {
    await ElMessageBox.confirm(
        `确定要取消预约 "${row.purpose}" 吗？`,
        '取消预约',
        {
          confirmButtonText: '确定取消',
          cancelButtonText: '再想想',
          type: 'warning',
        }
    )

    const result = await cancelReservation(row.reservationRecord_id,userStore.isAdmin)

    if (result) {
      ElMessage.success('预约已取消')
      loadReservations()
    } else {
      ElMessage.error('取消失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(`取消失败: ${error.message || '未知错误'}`)
    }
  }
}

// 开始预约
const handleStart = async (row: Reservation) => {
  try {
    await ElMessageBox.confirm(
        `确定要开始使用会议室吗？\n会议主题: ${row.purpose}`,
        '开始会议',
        {
          confirmButtonText: '开始',
          cancelButtonText: '取消',
          type: 'info',
        }
    )

    startingId.value = row.reservationRecord_id

    const result = await startReservation(row.reservationRecord_id)

    if (result) {
      ElMessage.success('会议已开始')
      loadReservations()
    } else {
      ElMessage.error('开始失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(`开始失败: ${error.message || '未知错误'}`)
    }
  } finally {
    startingId.value = null
  }
}

// 完成预约
const handleComplete = async (row: Reservation) => {
  try {
    await ElMessageBox.confirm(
        `确定要结束会议吗？\n会议主题: ${row.purpose}`,
        '结束会议',
        {
          confirmButtonText: '完成',
          cancelButtonText: '取消',
          type: 'info',
        }
    )

    completingId.value = row.reservationRecord_id

    const result = await completeReservation(row.reservationRecord_id)

    if (result) {
      ElMessage.success('会议已完成')
      loadReservations()
    } else {
      ElMessage.error('完成失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(`完成失败: ${error.message || '未知错误'}`)
    }
  } finally {
    completingId.value = null
  }
}

// 查看详情
const handleViewDetail = (row: Reservation) => {
  selectedReservation.value = row
  detailDialogVisible.value = true
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 页面加载时获取数据
onMounted(() => {
  loadReservations()
})

// 监听用户变化重新加载数据
watch(() => userStore.user?.userId, () => {
  loadReservations()
})
</script>

<style scoped>
.my-reservations {
  padding: 20px;
}

/* 状态标签样式 */
.el-tag {
  margin-right: 4px;
}

/* 分页样式 */
:deep(.el-pagination) {
  margin-top: 20px;
}

/* 表格行高亮 */
:deep(.el-table__row.in-progress) {
  background-color: #f0f9eb !important;
}

/* 详情对话框样式 */
:deep(.el-descriptions) {
  margin-top: 10px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
}

/* 搜索区域样式 */
.search-area {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>