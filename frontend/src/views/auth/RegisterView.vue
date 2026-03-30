<!-- src/views/auth/RegisterView.vue -->
<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>请填写以下信息完成注册</p>
      </div>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="100px"
          class="register-form"
      >
        <el-form-item label="用户名" prop="userName" required>
          <el-input
              v-model="registerForm.userName"
              placeholder="请输入用户名"
              clearable
              @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password" required>
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword" required>
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName" required>
          <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone" required>
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              clearable
              maxlength="11"
          >
            <template #prepend>+86</template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email" required>
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              clearable
          />
        </el-form-item>

        <el-form-item label="部门名称" prop="department_name">
          <el-input
              v-model="registerForm.department_name"
              placeholder="请输入部门名称"
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
          >
            注册
          </el-button>
          <el-button class="back-btn" @click="goToLogin">
            返回登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>已有账号？<el-link type="primary" @click="goToLogin">去登录</el-link></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  userName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  realName: '',
  email: '',
  department_name: ''
})

// 验证规则
const registerRules: FormRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
        } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
          callback(new Error('密码需包含大小写字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  department_name: [
    { max: 50, message: '部门名称不能超过50个字符', trigger: 'blur' }
  ]
}

// RegisterView.vue - handleRegister 方法
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const registerData = {
      userName: registerForm.userName,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      phone: registerForm.phone,
      realName: registerForm.realName,
      email: registerForm.email,
      department_name: registerForm.department_name
    }

    // 调用注册API
    await userStore.register(registerData)

    ElMessage.success('注册成功！')

    setTimeout(() => {
      router.push({
        name: 'login',
        query: {
          username: registerForm.userName,
          autoFocus: 'true'
        }
      })
    }, 3000)

  } catch (error) {
    // 直接使用原始错误，不需要任何包装
    const err = error as any

    // 添加调试信息
    console.log('===== 完整的错误对象 =====')
    console.log('错误:', err)
    console.log('消息:', err.message)
    console.log('response:', err.response)
    console.log('response.data:', err.response?.data)
    console.log('response.data.message:', err.response?.data?.message)
    console.log('=========================')

    // 直接处理后端返回的错误消息
    if (err.response?.data?.message) {
      const errorMsg = err.response.data.message
      if (errorMsg.includes('用户名已存在') || errorMsg.includes('USER_NAME_EXISTS')) {
        ElMessage.warning('用户名已存在，请更换用户名')
      } else if (errorMsg.includes('邮箱') || errorMsg.includes('email')) {
        ElMessage.warning('邮箱已被使用，请更换邮箱')
      } else if (errorMsg.includes('手机号') || errorMsg.includes('phone')) {
        ElMessage.warning('手机号已被使用，请更换手机号')
      } else {
        ElMessage.error(errorMsg)
      }
    }
    // 如果后端没有返回具体的消息，使用通用消息
    else if (err.message) {
      // 如果是 "Request failed with status code 400"，翻译成中文
      if (err.message === 'Request failed with status code 400') {
        ElMessage.error('注册失败，请检查输入信息')
      } else {
        ElMessage.error(err.message)
      }
    }
    // 最后兜底
    else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push({ name: 'login' })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰元素 */
.register-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
  top: -250px;
  right: -250px;
  z-index: 0;
}

.register-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f093fb20 0%, #f5576c20 100%);
  bottom: -200px;
  left: -200px;
  z-index: 0;
}

.register-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow:
      0 15px 35px rgba(50, 50, 93, 0.1),
      0 5px 15px rgba(0, 0, 0, 0.07);
  padding: 40px 30px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow:
      0 20px 40px rgba(50, 50, 93, 0.15),
      0 7px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-logo {
  margin-bottom: 20px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
  font-size: 28px;
  margin-bottom: 15px;
}

.register-header h2 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-header p {
  margin: 0;
  color: #718096;
  font-size: 15px;
  line-height: 1.6;
}

.register-form {
  margin-bottom: 25px;
}

/* 美化表单标签 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #4a5568 !important;
  padding-bottom: 8px;
  display: block;
}

/* 美化输入框 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.3s ease;
  padding: 0 15px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 美化按钮 */
.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  margin-bottom: 15px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.register-btn:active {
  transform: translateY(0);
}

.back-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: transparent;
  color: #4a5568;
  font-weight: 600;
  transition: all 0.3s ease;
}

.back-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.register-footer {
  text-align: center;
  padding-top: 25px;
  border-top: 1px solid #edf2f7;
  color: #718096;
  font-size: 14px;
}

.register-footer .el-link {
  font-weight: 600;
  color: #667eea !important;
  text-decoration: none;
  position: relative;
}

.register-footer .el-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #667eea;
  transition: width 0.3s ease;
}

.register-footer .el-link:hover::after {
  width: 100%;
}

/* 添加一些装饰性分隔线 */
.decorative-line {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 25px 0;
  color: #a0aec0;
}

.decorative-line::before,
.decorative-line::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e2e8f0;
}

.decorative-line span {
  padding: 0 15px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 15px;
  }

  .register-card {
    padding: 30px 25px;
    border-radius: 16px;
  }

  .register-header h2 {
    font-size: 24px;
  }

  :deep(.el-form-item__label) {
    width: auto !important;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 25px 20px;
  }

  .register-btn,
  .back-btn {
    height: 44px;
  }
}
</style>