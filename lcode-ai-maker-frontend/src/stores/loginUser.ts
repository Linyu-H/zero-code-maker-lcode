import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUser } from '@/api/userController.ts'

/**
 * 登陆用户信息
 */
export const useLoginUserStore = defineStore('loginUser', () => {

  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录'
  })

  // 获取登陆用户信息
  const fetchLoginUser = async () => {
    const res = await getLoginUser();
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }


  // 更新登陆用户信息
  const setLoginUser = (newLoginUser: API.LoginUserVO) => {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
