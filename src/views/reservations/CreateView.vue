<template>
  <div class="create-reservation">
    <el-card>
      <template #header>
        <span style="font-size: 1.2em; font-weight: bold">预约会议室</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <!-- 步骤1: 选择时间和人数 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预约日期" prop="reserveDate">
              <el-date-picker
                  v-model="form.reserveDate"
                  type="date"
                  placeholder="选择日期"
                  :disabled-date="disabledDate"
                  @change="handleDateOrTimeChange"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始时间" prop="start_time">
              <el-time-select
                  v-model="form.start_time"
                  :max-time="form.end_time"
                  placeholder="开始时间"
                  start="08:00"
                  step="01:00"
                  end="22:00"
                  @change="handleDateOrTimeChange"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间" prop="end_time">
              <el-time-select
                  v-model="form.end_time"
                  :min-time="form.start_time"
                  placeholder="结束时间"
                  start="09:00"
                  step="01:00"
                  end="23:00"
                  @change="handleDateOrTimeChange"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预计人数" prop="expectedNum">
              <el-input-number
                  v-model="form.expectedNum"
                  :min="1"
                  :max="200"
                  controls-position="right"
                  style="width: 100%"
                  @change="handleExpectedNumChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会议主题" prop="purpose">
              <el-input
                  v-model="form.purpose"
                  placeholder="请输入会议主题"
                  maxlength="100"
                  show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 步骤2: 选择会议室 (根据条件动态筛选) -->
        <el-form-item label="选择会议室">
          <div v-if="loadingRooms" class="loading-rooms">
            <el-skeleton :rows="3" animated />
          </div>

          <div v-else-if="availableRooms.length === 0" class="no-rooms-hint">
            <el-alert
                title="暂无可用会议室"
                type="info"
                :closable="false"
                show-icon
                v-if="hasSearchParams"
            >
              <p>请检查：</p>
              <ul>
                <li>是否选择了未来的日期和时间</li>
                <li>预计人数是否超过会议室容量</li>
                <li>该时间段会议室是否已被预约</li>
                <li>会议室状态是否为"可预约"</li>
              </ul>
            </el-alert>

            <el-alert
                title="请先选择预约时间和人数"
                type="warning"
                :closable="false"
                show-icon
                v-else
            />
          </div>

          <!-- 使用网格布局排列会议室 -->
          <div v-else class="rooms-grid">
            <el-row :gutter="16">
              <el-col
                  v-for="room in availableRooms"
                  :key="room.roomId"
                  :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
                  class="room-col"
              >
                <div
                    :class="['room-card', { 'room-card-selected': selectedRoomId === room.roomId }]"
                    @click="selectedRoomId = room.roomId"
                >
                  <!-- 会议室卡片内容 -->
                  <div class="room-card-header">
                    <h3 class="room-name">{{ room.roomName }}</h3>
                    <el-radio :label="room.roomId" v-model="selectedRoomId" class="room-radio" />
                  </div>

                  <div class="room-card-content">
                    <div class="room-info">
                      <p class="room-no">
                        <el-icon><Location /></el-icon>
                        {{ room.roomNo }}
                      </p>
                      <p class="room-capacity">
                        <el-icon><User /></el-icon>
                        {{ room.capacity }}人
                      </p>
                      <p v-if="room.area" class="room-area">
                        <el-icon><House /></el-icon>
                        {{ room.area }}㎡
                      </p>
                    </div>

                    <div v-if="room.description" class="room-description">
                      <p>{{ room.description }}</p>
                    </div>

                    <div class="room-status">
                      <el-tag
                          size="small"
                          :type="room.roomStatus === 1 ? 'success' : 'danger'"
                      >
                        {{ room.roomStatus === 1 ? '可预约' : '不可预约' }}
                      </el-tag>

                      <!-- 显示是否满足容量要求 -->
                      <el-tag
                          v-if="room.capacity < form.expectedNum"
                          size="small"
                          type="warning"
                      >
                        容量不足
                      </el-tag>
                      <el-tag
                          v-else
                          size="small"
                          type="success"
                      >
                        容量充足
                      </el-tag>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitting"
              :disabled="!selectedRoomId"
          >
            提交预约申请
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import type { MeetingRoom } from '@/types'

// 导入真实API
import { createReservation } from '@/api/reservationRecord'
import { getAvailableRooms } from '@/api/meetingRoom'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loadingRooms = ref(false)

// 表单数据 - 匹配Reservation接口字段名
const form = reactive({
  reserveDate: '', // YYYY-MM-DD 格式
  start_time: '',
  end_time: '',
  expectedNum: 5,
  purpose: '', // 修改为purpose，匹配接口
})

// 表单验证规则
const rules: FormRules = {
  reserveDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  start_time: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  end_time: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  expectedNum: [
    { required: true, message: '请输入预计人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '人数在1-200之间', trigger: 'blur' }
  ],
  purpose: [
    { required: true, message: '请输入会议主题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' },
  ],
}

// 选择的会议室ID
const selectedRoomId = ref<number | null>(null)

// 可用会议室列表
const availableRooms = ref<MeetingRoom[]>([])

// 检查是否有搜索参数
const hasSearchParams = computed(() => {
  return !!(form.reserveDate && form.start_time && form.end_time && form.expectedNum)
})

// 监听表单参数变化，加载可用会议室
watch(
    () => [form.reserveDate, form.start_time, form.end_time, form.expectedNum],
    async () => {
      if (!hasSearchParams.value) {
        availableRooms.value = []
        selectedRoomId.value = null
        return
      }

      await loadAvailableRooms()
    },
    { deep: true }
)

// 加载可用会议室 - 使用 getAvailableRooms API
const loadAvailableRooms = async () => {
  if (!hasSearchParams.value) return

  loadingRooms.value = true
  availableRooms.value = []
  selectedRoomId.value = null

  try {
    // 1. 直接调用 getAvailableRooms API 获取所有可用会议室
    const allAvailableRooms = await getAvailableRooms()

    console.log('API返回的可用会议室:', allAvailableRooms)

    // 2. 处理返回的数据
    let allRooms: MeetingRoom[] = []

    if (Array.isArray(allAvailableRooms)) {
      allRooms = allAvailableRooms
    }

    console.log('处理后会议室列表:', allRooms)

    // 3. 过滤容量符合要求的会议室
    const capacityFilteredRooms = allRooms.filter(
        room => room.capacity >= form.expectedNum
    )

    console.log('容量过滤后会议室:', capacityFilteredRooms)

    if (capacityFilteredRooms.length === 0) {
      availableRooms.value = []
      ElMessage.warning('没有找到容量足够的会议室，请调整预计人数')
      return
    }


    const availabilityResults = await Promise.all(capacityFilteredRooms)
    const available = availabilityResults.filter(room => room !== null) as MeetingRoom[]

    // 5. 按容量排序，容量小的排在前面
    available.sort((a, b) => a.capacity - b.capacity)

    // 6. 更新可用会议室列表
    availableRooms.value = available

    if (available.length === 0) {
      ElMessage.warning('在您选择的时间段内没有可用的会议室，请调整时间')
    }

  } catch (error: any) {
    console.error('加载可用会议室失败:', error)

    // 根据错误类型显示不同的提示
    let errorMessage = '加载会议室失败，请稍后重试'

    if (error.message) {
      const errorMsg = error.message.toLowerCase()

      if (errorMsg.includes('网络') || errorMsg.includes('连接')) {
        errorMessage = '网络连接失败，请检查网络连接'
      } else if (errorMsg.includes('认证') || errorMsg.includes('登录')) {
        errorMessage = '请先登录后再操作'
      } else if (errorMsg.includes('权限') || errorMsg.includes('无权')) {
        errorMessage = '您没有权限查看会议室列表'
      } else {
        errorMessage = `加载失败: ${error.message}`
      }
    }

    ElMessage.error(errorMessage)
  } finally {
    loadingRooms.value = false
  }
}


// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000 // 只能预约从明天开始的日期
}

// 当时间或人数变化时，清空已选择的会议室
const handleDateOrTimeChange = () => {
  selectedRoomId.value = null
}

const handleExpectedNumChange = () => {
  selectedRoomId.value = null
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    // 1. 表单验证
    await formRef.value.validate()

    // 2. 检查是否选择了会议室
    if (!selectedRoomId.value) {
      ElMessage.warning('请选择一个会议室')
      return
    }

    // 3. 获取选择的会议室
    const selectedRoom = availableRooms.value.find(r => r.roomId === selectedRoomId.value)
    if (!selectedRoom) {
      ElMessage.error('选择的会议室不存在或已不可用')
      return
    }

    // 4. 再次确认会议室容量
    if (selectedRoom.capacity < form.expectedNum) {
      ElMessage.error(`该会议室容量不足（最大${selectedRoom.capacity}人）`)
      return
    }

    // 5. 再次确认会议室可用性(会议室在任何时间段可用，所以永远为1)
    const isAvailable = 1

    if (!isAvailable) {
      ElMessage.error('该时间段会议室已被预约，请重新选择')
      return
    }

    submitting.value = true

    // 6. 调用真实API创建预约
    const reservationData = {
      room_id: selectedRoomId.value,
      purpose: form.purpose,
      reserveDate: form.reserveDate,
      start_time: form.start_time,
      end_time: form.end_time,
      expectedNum: form.expectedNum,
      // user_id 会在createReservation函数中自动添加
    }

    console.log('提交预约数据:', reservationData)

    const createdReservation = await createReservation(reservationData)

    console.log('预约创建成功:', createdReservation)

    // 7. 提交成功
    ElMessage.success({
      message: '预约申请已提交，等待管理员审批',
      duration: 3000
    })

    // 8. 跳转到"我的预约"页面
    setTimeout(() => {
      router.push('/my-reservations')
    }, 1000)

  } catch (error: any) {
    console.error('提交预约失败:', error)

    // 根据错误类型显示不同的提示
    let errorMessage = '提交失败，请稍后重试'

    if (error.message) {
      const errorMsg = error.message.toLowerCase()

      if (errorMsg.includes('已存在') || errorMsg.includes('冲突') || errorMsg.includes('已预约')) {
        errorMessage = '该时间段会议室已被预约，请重新选择时间或会议室'
      } else if (errorMsg.includes('容量') || errorMsg.includes('人数')) {
        errorMessage = '会议室容量不足，请减少人数或选择其他会议室'
      } else if (errorMsg.includes('时间') || errorMsg.includes('无效')) {
        errorMessage = '预约时间无效，请检查开始和结束时间'
      } else if (errorMsg.includes('权限') || errorMsg.includes('未登录')) {
        errorMessage = '请先登录再提交预约'
      } else if (errorMsg.includes('会议室') || errorMsg.includes('不可用')) {
        errorMessage = '选择的会议室当前不可用'
      } else {
        errorMessage = `提交失败: ${error.message}`
      }
    }

    ElMessage.error(errorMessage)

  } finally {
    submitting.value = false
  }
}

// 页面加载时，设置默认时间为明天
onMounted(() => {
  const tomorrow = dayjs().add(1, 'day').format('YYYY-MM-DD')
  form.reserveDate = tomorrow
  form.start_time = '09:00'
  form.end_time = '10:00'
})
</script>

<style scoped>
.create-reservation {
  padding: 20px;
}

.room-card {
  width: 100%;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.room-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.room-card-selected {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.no-rooms-hint {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.no-rooms-hint ul {
  margin: 8px 0 0 20px;
  color: #606266;
}

.loading-rooms {
  padding: 20px;
}
</style>