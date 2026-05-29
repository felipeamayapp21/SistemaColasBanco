<template>
  <div class="app-wrapper">
    <!-- Navbar (solo si el usuario está logueado) -->
    <Navbar v-if="auth.isLoggedIn" @toggle-sidebar="sidebarOpen = !sidebarOpen" />
    
    <!-- Sidebar (solo si el usuario está logueado) -->
    <Sidebar v-if="auth.isLoggedIn" :is-open="sidebarOpen" />
    
    <!-- Contenido Principal -->
    <main :class="{ 'main-content metro-main': auth.isLoggedIn }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'
import Sidebar from '@/components/Sidebar.vue'

const auth = useAuthStore()
const sidebarOpen = ref(false)
</script>
