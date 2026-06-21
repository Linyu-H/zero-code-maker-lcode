<script setup lang="ts">
import { reactive, ref, watch, computed, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { updateUser } from '@/api/userController.ts'
import { sendMail } from '@/api/mailController.ts'

const props = defineProps<{
  visible: boolean
  user: API.LoginUserVO
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'success', v: API.LoginUserVO): void
}>()

const formRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const form = reactive<API.UserUpdateRequest>({
  userName: '',
  userAvatar: '',
  userProfile: '',
  userEmail: '',
  code: '',
})

// 原始邮箱(小写后),用于判断是否修改了邮箱
const originEmail = computed(() => (props.user.userEmail || '').trim().toLowerCase())

const emailChanged = computed(
  () => (form.userEmail || '').trim().toLowerCase() !== originEmail.value,
)

// 打开时用当前用户信息初始化
watch(
  () => props.visible,
  (v) => {
    if (v) {
      form.userName = props.user.userName || ''
      form.userAvatar = props.user.userAvatar || ''
      form.userProfile = props.user.userProfile || ''
      form.userEmail = props.user.userEmail || ''
      form.code = ''
      countdown.value = 0
      if (timer) {
        clearInterval(timer)
        timer = null
      }
    }
  },
)

const rules = computed<Record<string, Rule[]>>(() => ({
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  code: emailChanged.value
    ? [{ required: true, message: '修改邮箱需要验证码', trigger: 'blur' }]
    : [],
}))

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
  const mail = (form.userEmail || '').trim()
  if (!mail) {
    message.warning('请先填写邮箱')
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(mail)) {
    message.warning('邮箱格式不正确')
    return
  }
  codeLoading.value = true
  try {
    const res = await sendMail({ mail })
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

const handleClose = () => {
  emit('update:visible', false)
}

// 头像上传:转 base64 存入 userAvatar
const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 限制 2MB,避免 base64 过大撑爆数据库
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    form.userAvatar = e.target?.result as string
  }
  reader.readAsDataURL(file)
  // 返回 false 阻止 antd 自动上传
  return false
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const body: API.UserUpdateRequest = {
    id: props.user.id,
    userName: form.userName,
    userAvatar: form.userAvatar,
    userProfile: form.userProfile,
  }

  // 只有改了邮箱才传 userEmail + code,否则后端会要验证码
  if (emailChanged.value) {
    body.userEmail = (form.userEmail || '').trim().toLowerCase()
    body.code = form.code
  }

  loading.value = true
  try {
    const res = await updateUser(body)
    if (res.data.code === 0) {
      message.success('修改成功')
      const updated: API.LoginUserVO = {
        ...props.user,
        userName: body.userName,
        userAvatar: body.userAvatar,
        userProfile: body.userProfile,
        userEmail: body.userEmail ?? props.user.userEmail,
      }
      emit('success', updated)
      handleClose()
    } else {
      message.error(res.data.message || '修改失败')
    }
  } catch (e) {
    message.error('修改失败,请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <a-modal
    :open="visible"
    title="修改个人信息"
    :confirm-loading="loading"
    ok-text="保存"
    cancel-text="取消"
    :width="520"
    @ok="handleSubmit"
    @cancel="handleClose"
  >
    <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
      <a-form-item label="用户名" name="userName">
        <a-input v-model:value="form.userName" placeholder="请输入用户名" allow-clear />
      </a-form-item>

      <a-form-item label="头像" name="userAvatar">
        <div class="avatar-upload">
          <a-upload
            list-type="picture-card"
            class="avatar-uploader"
            :show-upload-list="false"
            :before-upload="handleBeforeUpload"
            accept="image/*"
          >
            <a-image
              v-if="form.userAvatar"
              :src="form.userAvatar"
              :width="104"
              :height="104"
              class="avatar-preview"
            />
            <div v-else class="avatar-placeholder">
              <PlusOutlined />
              <div class="upload-text">上传</div>
            </div>
          </a-upload>
          <a-button
            v-if="form.userAvatar"
            type="link"
            danger
            size="small"
            @click="form.userAvatar = ''"
          >
            移除
          </a-button>
        </div>
        <div class="avatar-tip">支持上传图片,将以 base64 形式保存</div>
      </a-form-item>

      <a-form-item label="个人简介" name="userProfile">
        <a-textarea
          v-model:value="form.userProfile"
          placeholder="介绍一下自己吧"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <a-form-item label="邮箱" name="userEmail">
        <a-input v-model:value="form.userEmail" placeholder="请输入邮箱" allow-clear>
          <template v-if="emailChanged" #addonAfter>
            <a-button
              type="link"
              size="small"
              :disabled="countdown > 0"
              :loading="codeLoading"
              style="padding: 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </a-button>
          </template>
        </a-input>
      </a-form-item>

      <a-form-item v-if="emailChanged" label="验证码" name="code">
        <a-input v-model:value="form.code" placeholder="请输入邮箱收到的验证码" allow-clear />
      </a-form-item>

      <a-alert
        v-if="emailChanged"
        type="info"
        show-icon
        message="修改邮箱需要邮箱验证码,请先点击「获取验证码」"
        style="margin-top: -4px"
      />
    </a-form>
  </a-modal>
</template>

<style scoped>
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-uploader :deep(.ant-upload) {
  width: 104px;
  height: 104px;
  border-radius: 50%;
  overflow: hidden;
  padding: 0;
}

.avatar-preview {
  width: 104px;
  height: 104px;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.avatar-placeholder :deep(.anticon) {
  font-size: 22px;
}

.upload-text {
  margin-top: 4px;
}

.avatar-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
