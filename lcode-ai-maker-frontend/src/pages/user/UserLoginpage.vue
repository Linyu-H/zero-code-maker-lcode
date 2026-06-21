<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const loading = ref(false)

const form = reactive<API.UserLoginRequest>({
  accountOrMail: '',
  userPassword: '',
})

const rules: Record<string, Rule[]> = {
  accountOrMail: [{ required: true, message: '请输入账号或邮箱', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleSubmit = async (values: API.UserLoginRequest) => {
  loading.value = true
  try {
    const res = await userLogin(values)
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success('登录成功')
      const redirect = (route.query.redirect as string) || '/'
      router.replace(redirect)
    } else {
      message.error(res.data.message || '登录失败')
    }
  } catch (e) {
    message.error('登录失败,请稍后重试')
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
        <h1 class="brand-slogan">零代码,创造无限可能</h1>
        <p class="brand-desc">
          可视化搭建、一键生成,让每一个想法都能快速落地。
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>可视化编辑,所见即所得</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>多端适配,一键发布</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>云端协作,团队共建</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">欢迎回来 👋</h2>
          <p class="form-subtitle">登录以继续使用 Lcode Maker</p>
        </div>

        <a-form
          layout="vertical"
          :model="form"
          :rules="rules"
          autocomplete="off"
          @finish="handleSubmit"
        >
          <a-form-item label="账号 / 邮箱" name="accountOrMail">
            <a-input
              v-model:value="form.accountOrMail"
              size="large"
              placeholder="请输入账号或邮箱"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="密码" name="userPassword">
            <a-input-password
              v-model:value="form.userPassword"
              size="large"
              placeholder="请输入密码"
            />
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              size="large"
              block
              html-type="submit"
              :loading="loading"
              class="submit-btn"
            >
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="form-footer">
          还没有账号?
          <router-link to="/user/register" class="auth-link">立即注册</router-link>
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
  margin: 0 0 44px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.92);
}

.feature-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 0 5px rgba(255, 255, 255, 0.18);
  flex-shrink: 0;
}

/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 36px;
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
:deep(.ant-input-affix-wrapper-lg:focus),
:deep(.ant-input-affix-wrapper-lg-focused) {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
}

/* ===== 响应式:窄屏隐藏左侧品牌区 ===== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    background: linear-gradient(135deg, #eef2ff 0%, #f5f0ff 50%, #ecfeff 100%);
  }
}
</style>
