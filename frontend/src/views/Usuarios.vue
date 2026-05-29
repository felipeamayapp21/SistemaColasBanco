<template>
  <div>
    <h2 class="page-title metro-page-title">Gestion de Usuarios</h2>
    <p class="page-subtitle">Administracion de cuentas del sistema</p>

    <div class="card-banco metro-panel mb-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0"><i class="bi bi-person-plus"></i> Usuarios registrados</h5>
        <button class="btn btn-primary btn-sm" @click="abrirCrear">
          <i class="bi bi-plus-lg"></i> Nuevo usuario
        </button>
      </div>
      <div class="table-responsive">
        <table class="table table-banco mb-0">
          <thead>
            <tr>
              <th>ID</th>
              <th>Usuario</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center py-4 text-muted">
                <span class="spinner-border spinner-border-sm me-2"></span> Cargando...
              </td>
            </tr>
            <tr v-else-if="usuarios.length === 0">
              <td colspan="5" class="text-center py-4 text-muted">No hay usuarios</td>
            </tr>
            <tr v-for="u in usuarios" :key="u.id">
              <td>{{ u.id }}</td>
              <td><strong>{{ u.username }}</strong></td>
              <td>
                <select class="form-select form-select-sm" style="width:auto"
                  :value="u.rol" @change="cambiarRol(u, $event.target.value)">
                  <option value="CAJERO">CAJERO</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td>
                <span v-if="u.activo" class="badge bg-success">Activo</span>
                <span v-else class="badge bg-danger">Inactivo</span>
              </td>
              <td>
                <div class="d-flex gap-1">
                  <button class="btn btn-sm" :class="u.activo ? 'btn-warning' : 'btn-success'"
                    @click="toggleEstado(u)">
                    <i :class="u.activo ? 'bi bi-pause-circle' : 'bi bi-play-circle'"></i>
                    {{ u.activo ? 'Desactivar' : 'Activar' }}
                  </button>
                  <button class="btn btn-sm btn-danger" @click="eliminarUsuario(u)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <div class="modal fade" id="modalCrearUsuario" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title"><i class="bi bi-person-plus"></i> Nuevo Usuario</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">Usuario</label>
              <input v-model="form.username" class="form-control" placeholder="Nombre de usuario" />
            </div>
            <div class="mb-3">
              <label class="form-label">Contraseña</label>
              <input v-model="form.password" type="password" class="form-control" placeholder="Minimo 4 caracteres" />
            </div>
            <div class="mb-3">
              <label class="form-label">Rol</label>
              <select v-model="form.rol" class="form-select">
                <option value="CAJERO">CAJERO</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-primary" :disabled="creando" @click="crearUsuario">
              <span v-if="creando" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-check-lg"></i> Crear
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Modal } from 'bootstrap'
import adminService from '@/services/adminService'

const usuarios = ref([])
const loading = ref(false)
const creando = ref(false)
const error = ref(null)
const success = ref(null)
const form = ref({ username: '', password: '', rol: 'CAJERO' })
let modalCrear = null

const cargarUsuarios = async () => {
  try {
    loading.value = true
    error.value = null
    const { data } = await adminService.listarUsuarios()
    usuarios.value = data
  } catch (e) {
    error.value = 'Error al cargar usuarios'
  } finally {
    loading.value = false
  }
}

const abrirCrear = () => {
  form.value = { username: '', password: '', rol: 'CAJERO' }
  modalCrear?.show()
}

const crearUsuario = async () => {
  if (!form.value.username || !form.value.password) {
    error.value = 'Completa todos los campos'
    return
  }
  try {
    creando.value = true
    error.value = null
    await adminService.crearUsuario(form.value)
    modalCrear?.hide()
    success.value = 'Usuario creado correctamente'
    await cargarUsuarios()
    setTimeout(() => success.value = null, 3000)
  } catch (e) {
    error.value = e.response?.data?.error || 'Error al crear usuario'
  } finally {
    creando.value = false
  }
}

const toggleEstado = async (u) => {
  try {
    error.value = null
    await adminService.toggleEstado(u.id, !u.activo)
    u.activo = !u.activo
  } catch (e) {
    error.value = 'Error al cambiar estado'
  }
}

const cambiarRol = async (u, nuevoRol) => {
  try {
    error.value = null
    await adminService.cambiarRol(u.id, nuevoRol)
    u.rol = nuevoRol
    success.value = `Rol de ${u.username} actualizado a ${nuevoRol}`
    setTimeout(() => success.value = null, 3000)
  } catch (e) {
    error.value = 'Error al cambiar rol'
  }
}

const eliminarUsuario = async (u) => {
  if (!confirm(`Eliminar usuario "${u.username}"?`)) return
  try {
    error.value = null
    await adminService.eliminarUsuario(u.id)
    success.value = 'Usuario eliminado'
    await cargarUsuarios()
    setTimeout(() => success.value = null, 3000)
  } catch (e) {
    error.value = e.response?.data?.error || 'Error al eliminar'
  }
}

onMounted(() => {
  cargarUsuarios()
  modalCrear = new Modal(document.getElementById('modalCrearUsuario'))
})
</script>
