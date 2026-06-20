<template>
  <a-layout class="basic-layout">
    <GlobalHeader/>
    <a-layout-content class="content">
      <div class="content-wrapper">
        <router-view/>
      </div>
    </a-layout-content>
    <GlobalFooter/>
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  padding: 24px;
}

.content-wrapper {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  flex: 1;
}

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
}
</style>
