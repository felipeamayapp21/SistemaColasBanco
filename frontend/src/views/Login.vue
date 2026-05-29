<template>
  <div class="login-page metro-login">
    <div class="metro-login-layout">
      <div class="metro-login-brand d-none d-md-flex">
        <h1><i class="bi bi-bank2"></i> BancoApp</h1>
        <p>Sistema de gestión de clientes y turnos con prioridad para adultos mayores.</p>
        <div class="metro-login-tiles">
          <span style="background:#0078D4">Cola</span>
          <span style="background:#107C10">Turnos</span>
          <span style="background:#5C2D91">Historial</span>
          <span style="background:#D83B01">Reportes</span>
        </div>
      </div>

      <div class="login-card metro-login-card">
        <div class="login-logo text-center mb-4">
          <h2 class="fw-bold mb-1" style="color: var(--metro-blue)">
            <i class="bi bi-shield-lock"></i>
            {{ tituloPantalla }}
          </h2>
          <p class="text-muted small mb-0">{{ subtituloPantalla }}</p>
        </div>

        <!-- Recuperación de contraseña -->
        <form v-if="isRecovering" @submit.prevent="handleRecoverSubmit">
          <div v-if="recoverStep === 'username'" class="mb-3">
            <label class="form-label fw-semibold">Usuario</label>
            <input type="text" v-model="username" class="form-control" required placeholder="admin">
          </div>

          <template v-if="recoverStep === 'verify'">
            <div class="alert alert-info py-2 small mb-3">
              <strong>{{ datosRecuperacion.tipoRecuperacion === 'PALABRA_CLAVE' ? 'Palabra clave:' : 'Pregunta de seguridad:' }}</strong>
              {{ datosRecuperacion.preguntaSeguridad }}
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Su respuesta</label>
              <input type="text" v-model="respuestaSeguridad" class="form-control" required autocomplete="off">
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold">Nueva contraseña</label>
              <input type="password" v-model="nuevaPassword" class="form-control" required minlength="4">
            </div>
          </template>

          <button type="submit" class="btn btn-banco metro-btn w-100 mb-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ recoverStep === 'username' ? 'Continuar' : 'Restablecer contraseña' }}
          </button>

          <div class="text-center">
            <a href="#" class="text-decoration-none small" @click.prevent="volverALogin">Volver al inicio de sesión</a>
          </div>

          <div v-if="error" class="alert alert-danger mt-3 mb-0 py-2 text-center small">{{ error }}</div>
          <div v-if="success" class="alert alert-success mt-3 mb-0 py-2 text-center small">{{ success }}</div>
        </form>

        <!-- Login / Registro -->
        <form v-else @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label class="form-label fw-semibold">Usuario</label>
            <input type="text" v-model="username" class="form-control" required placeholder="admin">
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Contraseña</label>
            <input type="password" v-model="password" class="form-control" required placeholder="admin123">
          </div>

          <template v-if="isRegistering">
            <div class="mb-3">
              <label class="form-label fw-semibold">Método de recuperación</label>
              <select v-model="tipoRecuperacion" class="form-select" required>
                <option value="PREGUNTA">Pregunta de seguridad</option>
                <option value="PALABRA_CLAVE">Palabra clave</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">
                {{ tipoRecuperacion === 'PALABRA_CLAVE' ? 'Indicación de palabra clave' : 'Pregunta de seguridad' }}
              </label>
              <input
                type="text"
                v-model="preguntaSeguridad"
                class="form-control"
                required
                :placeholder="tipoRecuperacion === 'PALABRA_CLAVE' ? 'Ej: Mi palabra clave secreta' : 'Ej: ¿Nombre de su primera mascota?'"
              >
            </div>

            <div class="mb-4">
              <label class="form-label fw-semibold">
                {{ tipoRecuperacion === 'PALABRA_CLAVE' ? 'Palabra clave' : 'Respuesta de seguridad' }}
              </label>
              <input
                type="text"
                v-model="respuestaSeguridad"
                class="form-control"
                required
                autocomplete="off"
                placeholder="Respuesta que usará si olvida su contraseña"
              >
            </div>
          </template>

          <div v-else class="mb-4"></div>

          <button type="submit" class="btn btn-banco metro-btn w-100 mb-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ isRegistering ? 'Crear cuenta' : 'Entrar' }}
          </button>

          <div class="text-center d-flex flex-column gap-2">
            <a href="#" class="text-decoration-none small" @click.prevent="toggleMode">
              {{ isRegistering ? '¿Ya tienes cuenta? Inicia sesión' : '¿No tienes cuenta? Regístrate' }}
            </a>
            <a v-if="!isRegistering" href="#" class="text-decoration-none small" @click.prevent="iniciarRecuperacion">
              ¿Olvidaste tu contraseña?
            </a>
          </div>

          <div v-if="error" class="alert alert-danger mt-3 mb-0 py-2 text-center small">{{ error }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import authService from '@/services/authService'

const router = useRouter()
const auth = useAuthStore()

const isRegistering = ref(false)
const isRecovering = ref(false)
const recoverStep = ref('username')
const username = ref('')
const password = ref('')
const tipoRecuperacion = ref('PREGUNTA')
const preguntaSeguridad = ref('')
const respuestaSeguridad = ref('')
const nuevaPassword = ref('')
const datosRecuperacion = ref({ tipoRecuperacion: '', preguntaSeguridad: '' })
const loading = ref(false)
const error = ref(null)
const success = ref(null)

const tituloPantalla = computed(() => {
  if (isRecovering.value) return 'Recuperar contraseña'
  return isRegistering.value ? 'Registro' : 'Iniciar sesión'
})

const subtituloPantalla = computed(() => {
  if (isRecovering.value) return 'Verifique su identidad con su pregunta o palabra clave'
  return isRegistering.value ? 'Crea tu cuenta de cajero' : 'Accede al panel Metro'
})

const toggleMode = () => {
  isRegistering.value = !isRegistering.value
  error.value = null
  limpiarCamposSeguridad()
}

const limpiarCamposSeguridad = () => {
  preguntaSeguridad.value = ''
  respuestaSeguridad.value = ''
  tipoRecuperacion.value = 'PREGUNTA'
}

const iniciarRecuperacion = () => {
  isRecovering.value = true
  recoverStep.value = 'username'
  error.value = null
  success.value = null
  password.value = ''
}

const volverALogin = () => {
  isRecovering.value = false
  recoverStep.value = 'username'
  error.value = null
  success.value = null
  nuevaPassword.value = ''
  respuestaSeguridad.value = ''
}

const handleRecoverSubmit = async () => {
  loading.value = true
  error.value = null
  success.value = null
  try {
    if (recoverStep.value === 'username') {
      datosRecuperacion.value = await authService.obtenerDatosRecuperacion(username.value)
      recoverStep.value = 'verify'
    } else {
      await authService.recuperarPassword(
        username.value,
        respuestaSeguridad.value,
        nuevaPassword.value
      )
      success.value = 'Contraseña restablecida. Ya puede iniciar sesión.'
      setTimeout(() => volverALogin(), 2000)
    }
  } catch (err) {
    error.value = err.response?.data?.error || 'No se pudo completar la recuperación.'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true
  error.value = null
  try {
    if (isRegistering.value) {
      await auth.register({
        username: username.value,
        password: password.value,
        tipoRecuperacion: tipoRecuperacion.value,
        preguntaSeguridad: preguntaSeguridad.value,
        respuestaSeguridad: respuestaSeguridad.value
      })
      router.push('/dashboard')
    } else {
      await auth.login(username.value, password.value)
      router.push('/dashboard')
    }
  } catch (err) {
    error.value = err.response?.data?.error || (isRegistering.value ? 'Error al registrar' : 'Credenciales incorrectas')
  } finally {
    loading.value = false
  }
}
</script>
