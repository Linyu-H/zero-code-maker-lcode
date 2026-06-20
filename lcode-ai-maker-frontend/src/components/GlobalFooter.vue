<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      © 2026 resume-lcode ·
      <a href="https://mine-resume.lcode.space" target="_blank" rel="noopener noreferrer">
        Lcode
      </a>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts"></script>

<style scoped>
.footer {
  background: #f0f2f5;
  text-align: center;
  padding: 24px 0;
  position: sticky;
  bottom: 0;
  width: 100%;
  margin-top: auto;
}

.footer-content {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .footer {
    padding: 16px 0;
  }

  .footer-content {
    font-size: 12px;
  }
}
</style>
