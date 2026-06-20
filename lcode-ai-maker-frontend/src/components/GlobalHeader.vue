<template>
  <a-layout-header class="header">
    <div class="header-content">
      <div class="logo-section" @click="handleLogoClick">
        <img src="@/assets/code maker.png" alt="logo" class="logo-img"/>
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
        <a-button type="primary">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import type {MenuProps} from 'ant-design-vue'

const router = useRouter()
const selectedKeys = ref<string[]>(['home'])

const menuItems: MenuProps['items'] = [
  {
    key: 'home',
    label: '首页',
  },
]

const handleMenuClick: MenuProps['onClick'] = (e) => {
  router.push({name: e.key as string})
}

const handleLogoClick = () => {
  router.push({name: 'home'})
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
  line-height: 72px;
  margin: 0 32px;
  min-width: 0;
  background: transparent;
}

.menu :deep(.ant-menu-item) {
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s;
}

.menu :deep(.ant-menu-item:hover) {
  background: transparent;
  color: #1890ff;
}

.menu :deep(.ant-menu-item-selected) {
  background: transparent;
  color: #1890ff;
  font-weight: 600;
  position: relative;
}

.menu :deep(.ant-menu-item-selected::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 2px 2px 0 0;
}

.user-section {
  flex-shrink: 0;
}

.user-section :deep(.ant-btn-primary) {
  height: 40px;
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
    margin: 0 16px;
    line-height: 64px;
  }

  .user-section :deep(.ant-btn-primary) {
    height: 36px;
    padding: 0 20px;
    font-size: 14px;
  }
}
</style>
