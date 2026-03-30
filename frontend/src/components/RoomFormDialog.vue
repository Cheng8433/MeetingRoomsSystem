<!-- src/components/RoomFormDialog.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="formData.roomId ? '编辑会议室' : '新增会议室'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="会议室名称" prop="roomName">
        <el-input
          v-model="formData.roomName"
          placeholder="请输入会议室名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="房号" prop="roomNo">
        <el-input
          v-model="formData.roomNo"
          placeholder="请输入唯一房号，如101、A201"
          maxlength="20"
        />
      </el-form-item>

      <el-form-item label="容纳人数" prop="capacity">
        <el-input-number
          v-model="formData.capacity"
          :min="1"
          :max="500"
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="面积(㎡)" prop="area">
        <el-input-number
          v-model="formData.area"
          :min="0"
          :precision="2"
          :step="0.5"
          controls-position="right"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="用途描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入会议室配置、适用场景等描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="状态" prop="roomStatus">
        <el-switch
          v-model="formData.roomStatus"
          :active-value="1"
          :inactive-value="0"
          active-text="可预约"
          inactive-text="不可预约"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting"> 确定 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { MeetingRoom } from '@/types'
import {useUserStore} from "@/stores/user.ts";
import {createMeetingRoom, type CreateMeetingRoomParams, updateMeetingRoom} from "@/api/meetingRoom.ts";

const props = defineProps<{
  modelValue: boolean
  roomData?: MeetingRoom | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const visible = ref(props.modelValue)

const formData = reactive({
  roomId: 0,
  roomName: '',
  roomNo: '',
  capacity: 20,
  area: undefined as number | undefined,
  description: '',
  roomStatus: 1 as 0 | 1,
  photoUrl : undefined as string | undefined,
})

const formRules: FormRules = {
  roomName: [
    { required: true, message: '请输入会议室名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' },
           { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }],

  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 500, message: '人数在 1 到 500 之间', trigger: 'blur' }],
  area: [
    { type: 'number', min: 0, max: 1000, message: '面积在 0 到 100 平方米之间', trigger: 'blur' }
  ]

}

// 监听props变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.roomData) {
      // 编辑模式：填充数据
      Object.assign(formData, props.roomData)
    } else {
      // 新增模式：重置数据
      resetForm()
    }
  },
)

// 监听visible变化同步给父组件
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 填充表单数据
const fillFormData = (roomData: MeetingRoom) => {
  Object.assign(formData, {
    roomId: roomData.roomId || 0,
    roomName: roomData.roomName || '',
    roomNo: roomData.roomNo || '',
    capacity: roomData.capacity || 20,
    area: roomData.area,
    description: roomData.description || '',
    roomStatus: roomData.roomStatus || 1,
    photoUrl: roomData.photoUrl
  })
}

//重置表单数据
const resetForm = () => {
  Object.assign(formData, {
    roomId: 0,
    roomName: '',
    roomNo: '',
    capacity: 20,
    area: undefined,
    description: '',
    roomStatus: 1,
    photoUrl: undefined,
  })
  formRef.value?.clearValidate()
}

// 准备创建会议室的参数
const prepareCreateParams = (): CreateMeetingRoomParams => {
  return {
    roomName: formData.roomName.trim(),
    roomNo: formData.roomNo.trim(),
    capacity: formData.capacity,
    area: formData.area || null,
    description: formData.description.trim() || undefined,
    roomStatus: formData.roomStatus,
    photoUrl: formData.photoUrl?.trim() || null,
    createUserId: userStore.user?.userId || 0 // createMeetingRoom会处理这个，但这里也提供
  }
}

// 准备更新会议室的参数
const prepareUpdateParams = () => {
  const params: any = {}

  // 只包含有变化的字段或必需的字段
  if (formData.roomName) params.roomName = formData.roomName.trim()
  if (formData.roomNo) params.roomNo = formData.roomNo.trim()
  if (formData.capacity) params.capacity = formData.capacity
  if (formData.area !== undefined) params.area = formData.area || null
  if (formData.description !== undefined) params.description = formData.description.trim() || ''
  if (formData.roomStatus !== undefined) params.roomStatus = formData.roomStatus
  if (formData.photoUrl !== undefined) params.photoUrl = formData.photoUrl?.trim() || null

  return params
}



// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    if (formData.roomId) {
      // 编辑会议室
      await handleEdit()
    } else {
      // 新增会议室
      await handleCreate()
    }

  } catch (error: any) {
    console.error('表单提交失败:', error)
    handleSubmitError(error)
  } finally {
    submitting.value = false
  }
}

// 创建会议室
const handleCreate = async () => {
  try {
    const params = prepareCreateParams()
    console.log('创建会议室参数:', params)

    // 调用真实API
    const result = await createMeetingRoom(params)
    console.log('创建会议室成功:', result)

    ElMessage.success({
      message: `会议室 "${result.roomName}" 创建成功`,
      duration: 3000
    })

    handleSuccess()

  } catch (error: any) {
    // 重新抛出错误，让外层处理
    throw error
  }
}

// 编辑会议室
const handleEdit = async () => {
  try {
    const params = prepareUpdateParams()
    console.log('更新会议室参数:', params, '会议室ID:', formData.roomId)

    if (Object.keys(params).length === 0) {
      ElMessage.warning('没有需要更新的字段')
      return
    }

    // 调用真实API
    const result = await updateMeetingRoom(formData.roomId, params)
    console.log('更新会议室成功:', result)

    ElMessage.success({
      message: `会议室 "${result.roomName}" 更新成功`,
      duration: 3000
    })

    handleSuccess()

  } catch (error: any) {
    // 重新抛出错误，让外层处理
    throw error
  }
}

// 处理提交错误
const handleSubmitError = (error: any) => {
  console.error('提交失败:', error)

  let errorMessage = '操作失败，请稍后重试'

  if (error.message) {
    // 根据错误信息判断错误类型
    const errorMsg = error.message.toLowerCase()

    if (errorMsg.includes('已存在') || errorMsg.includes('重复') || errorMsg.includes('exist')) {
      errorMessage = '会议室名称或房号已存在，请修改后重试'
    } else if (errorMsg.includes('权限') || errorMsg.includes('管理员') || errorMsg.includes('permission')) {
      errorMessage = '无权限：仅管理员可操作会议室'
    } else if (errorMsg.includes('网络') || errorMsg.includes('network') || errorMsg.includes('timeout')) {
      errorMessage = '网络连接失败，请检查网络后重试'
    } else if (errorMsg.includes('required') || errorMsg.includes('必填')) {
      errorMessage = '请填写所有必填字段'
    } else if (errorMsg.includes('invalid') || errorMsg.includes('无效') || errorMsg.includes('格式')) {
      errorMessage = '数据格式不正确，请检查输入'
    } else {
      // 显示原始错误信息
      errorMessage = `操作失败: ${error.message}`
    }
  }

  ElMessage.error(errorMessage)
}

// 处理成功后的操作
const handleSuccess = () => {
  // 关闭对话框
  handleClose()

  // 通知父组件刷新列表
  emit('success')

  // 可选：重置表单状态
  setTimeout(() => {
    resetForm()
  }, 100)
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
