<!-- src/views/auth/LoginView.vue -->
<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>会议室预约系统</h2>
        <p>请登录您的账户</p>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <!-- 新增验证码表单项 -->
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input
                v-model="loginForm.captcha"
                placeholder="验证码"
                size="large"
                class="captcha-input"
            />
            <img
                :src="captchaUrl"
                @click="refreshCaptcha"
                class="captcha-img"
                alt="验证码"
                title="点击刷新"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              @click="goToRegister"
              style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>测试账号(用户)：admin / 123456</p>
        <p>测试账号(管理员)：zhangsan / 654321</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

// 验证码图片地址（添加时间戳防止缓存）
const captchaUrl = ref('/captcha?' + Date.now())

// 刷新验证码
const refreshCaptcha = () => {
  console.log('刷新验证码')
  captchaUrl.value = '/captcha?' + Date.now()
  console.log('新URL:', captchaUrl.value);
}

// 登录表单数据（新增 captcha 字段）
const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

// 表单验证规则（新增验证码规则）
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用登录API，传入用户名、密码和验证码
    await userStore.login(loginForm.username, loginForm.password, loginForm.captcha)

    ElMessage.success('登录成功')
    // 临时：登录后直接跳转到会议室列表页，方便测试
    router.push('/rooms')
    // 根据角色跳转不同页面（如需启用可取消注释）
    /* if (userStore.isAdmin) {
      router.push('/admin/dashboard')
    } else {
      router.push('/dashboard')
    } */
  } catch (error: unknown) {
    // 如果验证码错误，刷新验证码
    refreshCaptcha()
    // 显示错误信息
    if (error.message !== '校验失败') {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

const goToRegister = async () => {
  router.push('/Register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

/* 验证码容器样式 */
.captcha-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.captcha-input {
  flex: 1;
  margin-right: 10px;
}

.captcha-img {
  width: 100px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #999;
}
</style>