<template>
  <nav class="navbar-banco metro-app-bar">
    <div class="brand">
      <i class="bi bi-bank2"></i> BancoApp
    </div>
    <div class="nav-actions">
      <button class="btn btn-sm btn-outline-light d-md-none" @click="$emit('toggle-sidebar')">
        <i class="bi bi-list"></i>
      </button>
      <div class="user-menu" ref="menuRef">
        <span class="metro-user-badge d-none d-sm-inline-flex" @click="abrir = !abrir">
          <i class="bi bi-person-badge"></i> {{ auth.user?.username }} · {{ auth.user?.rol }}
          <i class="bi bi-chevron-down ms-2" style="font-size:.65rem;opacity:.7"></i>
        </span>
        <div v-if="abrir" class="user-dropdown">
          <div class="user-dropdown-header">
            <i class="bi bi-person-circle me-1"></i> {{ auth.user?.username }}
          </div>
          <div class="user-dropdown-divider"></div>
          <a class="user-dropdown-item" href="#" @click.prevent="handleLogout">
            <i class="bi bi-box-arrow-right me-2"></i> Cerrar sesión
          </a>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

defineEmits(['toggle-sidebar'])

const auth = useAuthStore()
const router = useRouter()
const abrir = ref(false)
const menuRef = ref(null)

const handleLogout = () => {
  abrir.value = false
  auth.logout()
  router.push('/login')
}

const cerrarFuera = (e) => {
  if (menuRef.value && !menuRef.value.contains(e.target)) {
    abrir.value = false
  }
}

onMounted(() => document.addEventListener('click', cerrarFuera))
onUnmounted(() => document.removeEventListener('click', cerrarFuera))
</script>

<style scoped>
.user-menu {
  position: relative;
}
.metro-user-badge {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  background: rgba(255,255,255,.12);
  border: 1px solid rgba(255,255,255,.18);
  padding: .25rem .75rem .25rem .4rem;
  font-size: .78rem;
  font-weight: 600;
  color: #fff;
  transition: background var(--t-fast);
  white-space: nowrap;
}
.metro-user-badge:hover {
  background: rgba(255,255,255,.2);
}
.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 6px;
  min-width: 200px;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  box-shadow: 0 4px 16px rgba(0,0,0,.12);
  z-index: 1000;
}
.user-dropdown-header {
  padding: .6rem .9rem;
  font-size: .82rem;
  font-weight: 600;
  color: #333;
}
.user-dropdown-divider {
  height: 1px;
  background: #e9ecef;
  margin: 0;
}
.user-dropdown-item {
  display: flex;
  align-items: center;
  padding: .55rem .9rem;
  font-size: .82rem;
  color: #444;
  text-decoration: none;
  cursor: pointer;
  transition: background .12s;
}
.user-dropdown-item:hover {
  background: #f0f0f0;
  color: #000;
}
</style>
