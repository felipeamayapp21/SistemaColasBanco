import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService'

function guardarSesion(data) {
  localStorage.setItem('banco_user', JSON.stringify(data.user))
  localStorage.setItem('banco_token', data.token)
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('banco_user')) || null)
  const token = ref(localStorage.getItem('banco_token') || null)

  const isLoggedIn = computed(() => !!token.value)

  function aplicarSesion(data) {
    user.value = data.user
    token.value = data.token
    guardarSesion(data)
  }

  async function login(username, password) {
    aplicarSesion(await authService.login(username, password))
  }

  async function register(payload) {
    const data = await authService.register(payload)
    console.log('📝 Register response:', data)
    aplicarSesion(data)
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('banco_user')
    localStorage.removeItem('banco_token')
  }

  const isAdmin = computed(() => user.value?.rol === 'ADMIN')
  const rol = computed(() => user.value?.rol || '')

  return { user, token, isLoggedIn, isAdmin, rol, login, register, logout }
})
