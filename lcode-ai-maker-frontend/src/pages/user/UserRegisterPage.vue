<script setup lang="ts">
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { userRegister } from '@/api/userController.ts'
import { sendMail } from '@/api/mailController.ts'

const router = useRouter()

const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const form = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userName: '',
  userEmail: '',
  code: '',
  userPassword: '',
  checkPassword: '',
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value !== form.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 16, message: '账号长度 4-16 位', trigger: 'blur' },
  ],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度 6-16 位', trigger: 'blur' },
  ],
  checkPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer!)
      timer = null
    }
  }, 1000)
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

const handleSendCode = async () => {
  if (!form.userEmail) {
    message.warning('请先填写邮箱')
    return
  }
  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.userEmail)
  if (!emailOk) {
    message.warning('邮箱格式不正确')
    return
  }
  codeLoading.value = true
  try {
    const res = await sendMail({ mail: form.userEmail })
    if (res.data.code === 0) {
      message.success('验证码已发送,请查收邮件')
      startCountdown()
    } else {
      message.error(res.data.message || '验证码发送失败')
    }
  } catch (e) {
    message.error('验证码发送失败,请稍后重试')
  } finally {
    codeLoading.value = false
  }
}

const handleSubmit = async (values: API.UserRegisterRequest) => {
  loading.value = true
  try {
    const res = await userRegister(values)
    if (res.data.code === 0) {
      message.success('注册成功,请登录')
      router.replace('/user/login')
    } else {
      message.error(res.data.message || '注册失败')
    }
  } catch (e) {
    message.error('注册失败,请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-blob blob-1"></div>
      <div class="brand-blob blob-2"></div>
      <div class="brand-content">
        <div class="brand-logo" @click="router.push('/')">
          <img src="@/assets/code maker.png" alt="logo" class="logo-img" />
          <span class="brand-name">Lcode Maker</span>
        </div>
        <h1 class="brand-slogan">加入我们,从零开始构建</h1>
        <p class="brand-desc">
          几分钟即可完成注册,开启你的零代码创造之旅。
        </p>
        <div class="brand-stats">
          <div class="stat-item">
            <div class="stat-num">1000+</div>
            <div class="stat-label">活跃创作者</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">10w+</div>
            <div class="stat-label">已生成应用</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">99.9%</div>
            <div class="stat-label">服务可用性</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">创建账号 ✨</h2>
          <p class="form-subtitle">填写信息,快速完成注册</p>
        </div>

        <a-form
          layout="vertical"
          :model="form"
          :rules="rules"
          autocomplete="off"
          @finish="handleSubmit"
        >
          <div class="form-row">
            <a-form-item label="账号" name="userAccount">
              <a-input
                v-model:value="form.userAccount"
                size="large"
                placeholder="4-16 位"
                allow-clear
              />
            </a-form-item>

            <a-form-item label="用户名" name="userName">
              <a-input
                v-model:value="form.userName"
                size="large"
                placeholder="请输入用户名"
                allow-clear
              />
            </a-form-item>
          </div>

          <a-form-item label="邮箱" name="userEmail">
            <a-input
              v-model:value="form.userEmail"
              size="large"
              placeholder="请输入邮箱"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="验证码" name="code">
            <div class="code-row">
              <a-input
                v-model:value="form.code"
                size="large"
                placeholder="请输入验证码"
                allow-clear
              />
              <a-button
                size="large"
                class="code-btn"
                :disabled="countdown > 0"
                :loading="codeLoading"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </a-button>
            </div>
          </a-form-item>

          <div class="form-row">
            <a-form-item label="密码" name="userPassword">
              <a-input-password
                v-model:value="form.userPassword"
                size="large"
                placeholder="6-16 位"
              />
            </a-form-item>

            <a-form-item label="确认密码" name="checkPassword">
              <a-input-password
                v-model:value="form.checkPassword"
                size="large"
                placeholder="再次输入"
              />
            </a-form-item>
          </div>

          <a-form-item>
            <a-button
              type="primary"
              size="large"
              block
              html-type="submit"
              :loading="loading"
              class="submit-btn"
            >
              注 册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="form-footer">
          已有账号?
          <router-link to="/user/login" class="auth-link">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  background: #f7f9fc;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #2563eb 100%);
  padding: 64px 56px;
}

.brand-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.55;
}

.blob-1 {
  width: 360px;
  height: 360px;
  background: #a78bfa;
  top: -80px;
  left: -80px;
}

.blob-2 {
  width: 320px;
  height: 320px;
  background: #38bdf8;
  bottom: -60px;
  right: -60px;
}

.brand-content {
  position: relative;
  z-index: 1;
  color: #fff;
  max-width: 440px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  margin-bottom: 56px;
}

.logo-img {
  height: 44px;
  width: auto;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.25));
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.brand-slogan {
  font-size: 38px;
  font-weight: 800;
  line-height: 1.25;
  margin: 0 0 20px;
}

.brand-desc {
  font-size: 16px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 48px;
}

.brand-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-num {
  font-size: 26px;
  font-weight: 800;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.25);
}

/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  overflow-y: auto;
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 28px;
}

.form-title {
  font-size: 30px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 10px;
}

.form-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row :deep(.ant-form-item) {
  flex: 1;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-row :deep(.ant-input-affix-wrapper),
.code-row :deep(.ant-input) {
  flex: 1;
}

.code-btn {
  flex-shrink: 0;
  min-width: 120px;
  border-radius: 12px;
  border-color: #c7d2fe;
  color: #4f46e5;
}

.code-btn:not(:disabled):hover {
  border-color: #4f46e5 !important;
  color: #4f46e5 !important;
  background: #eef2ff;
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(79, 70, 229, 0.4) !important;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
  margin-top: 16px;
}

.auth-link {
  color: #4f46e5;
  font-weight: 600;
  margin-left: 4px;
}

/* ===== 输入框美化 ===== */
:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #374151;
}

:deep(.ant-input-affix-wrapper-lg),
:deep(.ant-input-lg) {
  border-radius: 12px;
  padding: 10px 14px;
}

:deep(.ant-input-affix-wrapper-lg:hover),
:deep(.ant-input-lg:hover) {
  border-color: #a5b4fc;
}

:deep(.ant-input-affix-wrapper-focused),
:deep(.ant-input-affix-wrapper-lg-focused) {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    background: linear-gradient(135deg, #eef2ff 0%, #f5f0ff 50%, #ecfeff 100%);
  }
}

@media (max-width: 560px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
