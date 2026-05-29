<template>
  <aside class="sidebar metro-sidebar" :class="{ open: isOpen }">
    <nav class="flex-column">
      <div class="metro-nav-label">Principal</div>
      <router-link to="/dashboard" class="nav-link" active-class="active">
        <i class="bi bi-grid"></i> Dashboard
      </router-link>

      <div class="metro-nav-label">Operaciones</div>
      <router-link to="/clientes" class="nav-link" active-class="active">
        <i class="bi bi-people"></i> Clientes en cola
      </router-link>
      <router-link to="/consulta" class="nav-link" active-class="active">
        <i class="bi bi-search"></i> Consultar Clientes
      </router-link>

      <div class="metro-nav-label">Analisis</div>
      <router-link to="/historial" class="nav-link" active-class="active">
        <i class="bi bi-clock-history"></i> Historial
      </router-link>
      <router-link to="/reportes" class="nav-link" active-class="active">
        <i class="bi bi-file-earmark-bar-graph"></i> Reportes
      </router-link>

      <div v-if="auth.isAdmin" class="metro-nav-label">Admin</div>
      <router-link v-if="auth.isAdmin" to="/usuarios" class="nav-link" active-class="active">
        <i class="bi bi-shield-lock"></i> Usuarios
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

defineProps({
  isOpen: { type: Boolean, default: false }
})

const auth = useAuthStore()
</script>
