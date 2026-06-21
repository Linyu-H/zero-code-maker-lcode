<template>
  <a-layout-header class="header">
    <div class="header-content">
      <div class="logo-section" @click="handleLogoClick">
        <img src="@/assets/code maker.png" alt="logo" class="logo-img" />
        <span class="site-title">Lcode Maker</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        class="menu"
        @click="handleMenuClick"
      />
      <div class="user-section">
        <a-dropdown v-if="loginUserStore.loginUser.id" :trigger="['click']">
          <div class="user-trigger" @click.prevent>
            <a-avatar :size="32" :src="loginUserStore.loginUser.userAvatar" />
            <span class="user-name">{{ loginUserStore.loginUser.userName }}</span>
          </div>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="profile">
                <EditOutlined /> 修改个人信息
              </a-menu-item>
              <a-menu-item key="logout">
                <span class="logout-item"><LogoutOutlined /> 退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <div v-else>
          <a-button type="primary" @click="router.push('/user/login')">登录</a-button>
        </div>
      </div>
    </div>
    <EditProfileModal
      v-model:visible="editVisible"
      :user="loginUserStore.loginUser"
      @success="handleEditSuccess"
    />
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { EditOutlined, HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import EditProfileModal from '@/components/EditProfileModal.vue'

// 获取登陆用户信息
const loginUserStore = useLoginUserStore()
const router = useRouter()
const selectedKeys = ref<string[]>(['/'])
const editVisible = ref(false)
// 菜单配置项
// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const handleMenuClick: MenuProps['onClick'] = (e) => {
  router.push(e.key as string)
}

const handleLogoClick = () => {
  router.push('/')
}

const handleLogout = async () => {
  try {
    await userLogout()
  } catch (e) {
    // 即使接口失败也继续清理本地状态
  }
  loginUserStore.setLoginUser({ userName: '未登录' })
  message.success('已退出登录')
  router.replace('/user/login')
}

const handleUserMenuClick: MenuProps['onClick'] = (e) => {
  if (e.key === 'logout') {
    Modal.confirm({
      title: '确认退出登录?',
      okText: '退出',
      cancelText: '取消',
      onOk: handleLogout,
    })
  } else if (e.key === 'profile') {
    editVisible.value = true
  }
}

const handleEditSuccess = (updated: API.LoginUserVO) => {
  loginUserStore.setLoginUser(updated)
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 0 32px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
  height: 72px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px 12px;
  border-radius: 12px;
  margin-left: -12px;
}

.logo-section:hover {
  background: transparent;
  transform: translateY(-1px);
}

.logo-section:active {
  transform: translateY(0);
}

.logo-img {
  height: 42px;
  width: auto;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.08));
  animation: logo-float 2.8s ease-in-out infinite;
}

@keyframes logo-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-6px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .logo-img {
    animation: none;
  }
}

.site-title {
  font-family: 'Poppins', 'HarmonyOS Sans SC', 'PingFang SC', sans-serif;
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #1677ff 0%, #7c3aed 48%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  letter-spacing: 0.03em;
  text-shadow: 0 8px 24px rgba(22, 119, 255, 0.18);
}

.menu {
  flex: 1;
  border: none;
  line-height: 1;
  margin: 0 32px;
  min-width: 0;
  background: transparent;
}

.menu :deep(.ant-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 0 4px !important;
  padding: 0 18px !important;
  font-size: 15px;
  font-weight: 500;
  color: #4b5563;
  border-radius: 20px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
  margin-right: 6px;
}

.menu :deep(.ant-menu-item:hover) {
  color: #4f46e5;
  background: rgba(79, 70, 229, 0.08) !important;
}

.menu :deep(.ant-menu-item-selected) {
  color: #fff !important;
  font-weight: 600;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.menu :deep(.ant-menu-item-selected:hover) {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
}

.menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.user-section {
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 22px;
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-trigger:hover {
  transform: translateY(-2px);
}

.user-trigger:active {
  transform: translateY(0);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
}

.logout-item {
  color: #ef4444;
}

.user-section :deep(.ant-btn-primary) {
  height: 40px;
  line-height: 1;
  padding: 0 24px;
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-section :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.user-section :deep(.ant-btn-primary:active) {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .header-content {
    height: 64px;
  }

  .logo-section {
    gap: 10px;
    padding: 6px 10px;
    margin-left: -10px;
  }

  .logo-img {
    height: 36px;
  }

  .site-title {
    font-size: 18px;
  }

  .menu {
    margin: 0 12px;
  }

  .menu :deep(.ant-menu-item) {
    padding: 0 14px !important;
    font-size: 14px;
  }

  .user-section :deep(.ant-btn-primary) {
    height: 36px;
    padding: 0 20px;
    font-size: 14px;
  }
}
</style>
