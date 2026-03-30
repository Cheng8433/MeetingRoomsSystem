<!-- src/views/rooms/RoomListView.vue -->
<template>
  <div class="room-list-container">
    <div style="margin-bottom: 20px">
      <el-button @click="goToLogin" type="primary" plain>
        <el-icon><ArrowLeft /></el-icon>
        返回登录页
      </el-button>
    </div>
    <el-card>
      <!-- 搜索和操作栏 -->
      <div
        style="
          margin-bottom: 20px;
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        "
      >
        <div style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center">
          <!-- 关键字搜索 -->
          <el-input v-model="searchRoomName" placeholder="房号" style="width: 200px" @change="handleKeysearch"/>
          <!-- 状态筛选 -->
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 110px" @change="handleChangeStatus">
            <el-option label="可预约" :value="1" />
            <el-option label="不可预约" :value="0" />
          </el-select>
          <!-- 容量筛选 -->
          <el-select
            v-model="filterMinCapacity"
            placeholder="最小容量"
            clearable
            style="width: 120px"
            @change="handleChangeMinCapacity"
          >
            <el-option label="不限" :value="1" />
            <el-option label="10人以上" :value="10" />
            <el-option label="20人以上" :value="20" />
            <el-option label="30人以上" :value="30" />
            <el-option label="50人以上" :value="50" />
          </el-select>

          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>

          <!-- 关键字搜索 -->
          <el-input v-model="searchKeyWord" placeholder="会议室名称或描述模糊搜索" style="width: 200px" @change="handleKeyWordsearch"/>
          <el-button type="primary" :icon="Search" @click="handleFuzzySearch">搜索</el-button>

        </div>
        <div v-if="isAdmin">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增会议室</el-button>
        </div>
      </div>

      <!-- 会议室表格 -->
      <el-table v-loading="loading" :data="finalRoomList" border style="width: 100%">
        <el-table-column prop="roomId" label="ID" width="80" />
        <el-table-column prop="roomName" label="会议室名称" />
        <el-table-column prop="roomNo" label="房号" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="area" label="面积(㎡)" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />

        <el-table-column label="操作" width="200" v-if="isAdmin">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    <RoomFormDialog v-model="showDialog" :room-data="editingRoom" @success="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus' // 添加这行
import { useRouter } from 'vue-router'
import {ref, onMounted, computed} from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import type { MeetingRoom } from '@/types'
import { useUserStore } from '@/stores/user' // <-- 新增：导入 Pinia Store
import { ArrowLeft } from '@element-plus/icons-vue' // 导入图标
import RoomFormDialog from '@/components/RoomFormDialog.vue' // 假设这是你的表单对话框组件

import {
  getRoomsByPage,          // 获取会议室列表
  getRoomsByFuzzySearch,    //模糊搜索获取会议室信息
  updateMeetingRoom,        // 更新会议室
  deleteMeetingRoom,        // 删除会议室
  type QueryMeetingRoomParams, type CreateMeetingRoomParams  // 查询参数类型
} from '@/api/meetingRoom'

const searchKeyWord = ref('')
const searchRoomName = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const roomList = ref<MeetingRoom[]>([])
const firstRoomList = ref<MeetingRoom[]>([])
const secondRoomList = ref<MeetingRoom[]>([])
const selectRoomList = ref<MeetingRoom[]>([])
const finalRoomList = ref<MeetingRoom[]>([])
// 新增：获取用户状态
const userStore = useUserStore()
// 新增：创建一个计算属性来判断是否是管理员（可选，更规范）
const isAdmin = computed(() => userStore.user?.roleType === 1)
// 获取路由实例和用户状态
const router = useRouter()
const showDialog = ref(false)
const editingRoom = ref<MeetingRoom | null>(null)
const formLoading = ref(false)
const filterStatus = ref<number | string>('')
const filterMinCapacity = ref<number | string>('')
const loading = ref(false)

// 默认的会议室数据（用于新增）
const defaultRoomData = computed<Partial<CreateMeetingRoomParams>>(() => ({
  roomName: '',
  roomNo: '',
  capacity: 10, // 默认容量为10人
  area: 10,
  description: '',
  roomStatus: 1 as 0 | 1, // 默认为可用状态
  photoUrl: null
}))

const goToLogin = () => {
  // 1. 调用登出方法，清除 Pinia 状态和 localStorage
  userStore.logout()
  // 2. 跳转到登录页
  router.push('/login')
}

// 加载数据
// 修改 loadData 函数
const loadData = async () => {
  loading.value = true
  try {
    console.log('--- 开始加载会议室数据 ---')

    // 构建查询参数
    const queryParams: QueryMeetingRoomParams = {
      page: 1,
      size: 100,
    }

    // 添加筛选条件
    if (searchRoomName.value.trim()) {
      const keyword = searchRoomName.value.trim()
      // 根据实际情况，可能后端支持模糊搜索或需要分别设置
      queryParams.roomName = keyword
      // 或者 queryParams.roomNo = keyword
      // 或者使用单独的搜索字段，根据后端API决定
    }

    if (filterStatus.value !== '') {
      queryParams.roomStatus = Number(filterStatus.value) as 0 | 1
    }

    if (filterMinCapacity.value !== '') {
      queryParams.minCapacity = Number(filterMinCapacity.value)
    }

    console.log('查询参数:', queryParams)

    // 调用真实 API
    const resData = await getRoomsByPage(queryParams)

    // 根据后端返回的数据结构调整
    // 假设返回的数据结构是 { list: MeetingRoom[], total: number, ... }
    // 或者直接是 MeetingRoom[] 数组

    if (Array.isArray(resData)) {
      // 如果返回的是数组
      roomList.value = resData
      total.value = resData.length
    } else {
      console.error('未知的响应格式:', resData)
      roomList.value = []
      total.value = 0
    }

    //根据房间号筛选
    nameFilter()
    //执行状态筛选
    statusFilter()

    //执行容量筛选
    capacityFilter()

    // 执行前端分页
    applyFrontendPagination()

    console.log('加载成功，数据量:', roomList.value.length, '总条数:', total.value)

  } catch (error: any) {
    console.error('加载会议室数据失败:', error)
    ElMessage.error(`加载失败: ${error.message || '未知错误'}`)
    roomList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const nameFilter = () =>{
  if (searchRoomName.value !== ''){
    const result = [...roomList.value]
    const name = String(searchRoomName.value)
    firstRoomList.value = result.filter(room => room.roomNo === name)
  }else{
    firstRoomList.value = [...roomList.value]
  }

}
//添加状态筛选
const statusFilter = () => {
  if (filterStatus.value !== ''){
    const result = [...firstRoomList.value]
    const status = Number(filterStatus.value)
    secondRoomList.value = result.filter(room => room.roomStatus === status)
  }else{
    secondRoomList.value = [...firstRoomList.value]
  }
  }
//添加容量筛选
const capacityFilter = () =>{
  if (filterMinCapacity.value !== '') {
    const result = [...secondRoomList.value]
    const minCap = Number(filterMinCapacity.value)
    selectRoomList.value = result.filter(room => room.capacity >= minCap)
  }else{
    selectRoomList.value = [...secondRoomList.value]
  }
}

//  添加前端分页函数
const applyFrontendPagination = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  finalRoomList.value = selectRoomList.value.slice(startIndex, endIndex)
}

// 修改分页事件处理函数
const handleCurrentChange = (val: number) => {
  console.log('分页改变，当前页:', val)
  currentPage.value = val
  applyFrontendPagination()  // 只重新分页，不重新请求数据
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1  // 每页大小改变时，重置到第一页
  applyFrontendPagination()
}


// 修改 handleStatusChange 函数，添加类型检查和调试
// 修改 handleStatusChange 函数
const handleStatusChange = async (room: MeetingRoom, newStatus: number) => {
  if (!room) {
    console.error('handleStatusChange: room is undefined')
    return
  }

  try {
    console.log(`更新会议室 ${room.roomId} (${room.roomName}) 状态为: ${newStatus}`)

    // 调用更新 API，只更新状态字段
    const updatedRoom = await updateMeetingRoom(room.roomId, {
      roomStatus: (newStatus === 0? 0:1) as 0 | 1,
    })

    // 更新本地数据
    const index = roomList.value.findIndex(r => r.roomId === room.roomId)
    if (index !== -1) {
      const roomItem = roomList.value[index]
      if (roomItem) { // 再次检查确保不是 undefined
        roomItem.roomStatus = newStatus as 0 | 1
      }
    }

    ElMessage.success(`会议室 ${room.roomName} 状态已更新`)
  } catch (error: any) {
    console.error('更新状态失败:', error)
    ElMessage.error(`更新失败: ${error.message || '未知错误'}`)

    // 恢复原来的状态
    room.roomStatus = room.roomStatus === 1 ? 0 : 1
  }
}

// 在 script 中添加方法
const handleChangeMinCapacity = (value: number) => {
  console.log('最小容量筛选条件改变:', value)
  filterMinCapacity.value = value
  loadData()
}

const handleChangeStatus = (value: number) => {
  console.log('最小容量筛选条件改变:', value)
  filterStatus.value = value
  loadData()
}

//修改房间号
const handleKeysearch = (value:string) => {
  console.log('房间号筛选条件改变:', value)
  searchRoomName.value = value
}

//修改模糊搜索关键字
const handleKeyWordsearch = (value:string) => {
  console.log('模糊搜索条件改变:', value)
  searchKeyWord.value = value
}

//根据模糊搜索关键字进行搜索
const handleFuzzySearch = async () => {
  console.log("模糊搜索条件：",searchKeyWord.value)
  loading.value = true
  try {
    console.log('--- 开始加载会议室数据111 ---')
    // 1. 正确传递参数对象，并等待异步结果
    const resData2 = await getRoomsByFuzzySearch({ searchKeyWord: searchKeyWord.value });
  //检查返回结果为数组
  if (Array.isArray(resData2)) {
    // 如果返回的是数组
    console.log("返回结果是数组")
    roomList.value = resData2
    total.value = resData2.length
    console.log(roomList.value)

  } else {
    console.error('未知的响应格式:', resData2)
    roomList.value = []
    total.value = 0
  }

  //根据房间号筛选
  nameFilter()
    console.log("firstRoomList.value",firstRoomList.value)
  //执行状态筛选
  statusFilter()
    console.log("secondRoomList.value",secondRoomList.value)

  //执行容量筛选
  capacityFilter()
    console.log("selectRoomList.value",selectRoomList.value)

  // 执行前端分页
  applyFrontendPagination()
    console.log('finalRoomList.value',finalRoomList.value)

    console.log('加载成功，数据量:', roomList.value.length, '总条数:', total.value)

  } catch (error: any) {
    console.error('加载会议室数据失败:', error)
    ElMessage.error(`加载失败: ${error.message || '未知错误'}`)
    roomList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }

}

// 修改 handleSearch 函数，确保调用 API
const handleSearch = () => {
  loadData()
}

// 修改 handleReset 函数
const handleReset = () => {
  searchRoomName.value = ''
  filterStatus.value = ''
  filterMinCapacity.value = ''
  currentPage.value = 1
  loadData()
}

//添加会议室，打开新的表单
const handleAdd = () => {
  // 打开对话框
  showDialog.value = true
}

const handleEdit = (row: MeetingRoom) => {
  // 深拷贝数据，避免直接修改表格数据
  editingRoom.value = JSON.parse(JSON.stringify(row))

  showDialog.value = true
}

const handleDelete = async (row: MeetingRoom) => {
  try {
    // 1. 显示确认对话框
    await ElMessageBox.confirm(
      `确定要删除会议室 "${row.roomName}" 吗？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true,
      },
    )

    // 2. 调用删除API
    await deleteMeetingRoom(row.roomId)

    // 3. 删除成功后重新加载数据
    ElMessage.success('会议室删除成功')
    loadData()
  } catch (error: any) {
    // 4. 处理错误（包括用户取消和API错误）
    if (error === 'cancel' || error === 'close') {
      // 用户点击了取消或关闭了对话框
      console.log('删除操作已取消')
    } else {
      // API调用错误
      handleDeleteError(error, row)
    }
  }
}


// 新增：处理删除错误的函数
const handleDeleteError = (error: any, row: MeetingRoom) => {
  console.error('删除会议室失败:', error)

  // 根据错误信息判断错误类型
  const errorMsg = error.message || '删除失败'

  if (errorMsg.includes('无权限') || errorMsg.includes('权限') || errorMsg.includes('管理员')) {
    // 权限不足的错误
    ElMessage.warning('无权限：仅管理员可删除会议室')
  } else if (
    errorMsg.includes('未来预约') ||
    errorMsg.includes('预约') ||
    errorMsg.includes('使用中')
  ) {
    // 存在未来预约的错误
    ElMessageBox.alert(
      `会议室 "${row.roomName}" 存在未来预约，无法删除。<br/><br/>
      请先取消或完成所有相关预约后再尝试删除。`,
      '无法删除',
      {
        confirmButtonText: '确定',
        type: 'error',
        dangerouslyUseHTMLString: true,
      },
    )
  } else if (errorMsg.includes('网络连接失败') || errorMsg.includes('网络')) {
    // 网络错误
    ElMessage.error('网络连接失败，请检查网络后重试')
  } else {
    // 其他错误
    ElMessage.error(`删除失败: ${errorMsg}`)
  }
}


onMounted(() => {
  loadData()
})
</script>

<style scoped>
.room-list-container {
  padding: 20px;
}

/* 强制显示所有表格列 */
:deep(.el-table th),
:deep(.el-table td) {
  display: table-cell !important;
  visibility: visible !important;
}

/* 给表格添加边框，方便调试 */
:deep(.el-table) {
  border: 1px solid #ebeef5;
}

:deep(.el-table__header-wrapper) {
  background-color: #f5f7fa;
}

:deep(.el-table__body-wrapper) {
  background-color: #fff;
}

/* 确保表格列有最小宽度 */
:deep(.el-table__header th) {
  min-width: 80px;
}
</style>
