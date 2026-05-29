<template>
  <div class="card-banco mb-4">
    <h5 class="mb-3 text-primary"><i class="bi bi-person-plus"></i> Registrar Cliente</h5>
    
    <form @submit.prevent="handleSubmit">
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label">Nombre</label>
          <input type="text" v-model="form.nombre" class="form-control" required placeholder="Ej: Juan Perez" />
        </div>
        
        <div class="col-md-3">
          <label class="form-label">Documento</label>
          <input
            type="text"
            inputmode="numeric"
            pattern="[0-9]*"
            maxlength="10"
            v-model="form.documento"
            class="form-control"
            :class="{ 'is-invalid': documentoError }"
            required
            placeholder="Solo numeros, max. 10"
            @input="onDocumentoInput"
          />
          <div v-if="documentoError" class="invalid-feedback d-block">{{ documentoError }}</div>
        </div>

        <div class="col-md-2">
          <label class="form-label">Edad</label>
          <input type="number" v-model="form.edad" class="form-control" required min="1" max="120" />
        </div>

        <div class="col-md-3">
          <label class="form-label">Genero</label>
          <select v-model="form.genero" class="form-select">
            <option value="">Seleccionar</option>
            <option value="Masculino">Masculino</option>
            <option value="Femenino">Femenino</option>
            <option value="Otro">Otro</option>
          </select>
        </div>

        <div class="col-md-4">
          <label class="form-label">Direccion</label>
          <input type="text" v-model="form.direccion" class="form-control" placeholder="Calle, carrera, etc." />
        </div>

        <div class="col-md-2">
          <label class="form-label">Telefono</label>
          <input type="text" v-model="form.telefono" class="form-control" placeholder="3001234567" />
        </div>

        <div class="col-md-3">
          <label class="form-label">Correo</label>
          <input type="email" v-model="form.correo" class="form-control" placeholder="correo@mail.com" />
        </div>

        <div class="col-md-3">
          <label class="form-label">Foto</label>
          <input type="file" accept="image/*" class="form-control" @change="onFotoChange" />
        </div>

        <div class="col-md-12 d-flex align-items-end justify-content-end">
          <button type="submit" class="btn btn-banco" :disabled="loading || !!documentoError">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-ticket"></i> Ingresar a Cola
          </button>
        </div>
      </div>
      
      <div v-if="error" class="alert alert-danger mt-3 mb-0 py-2">{{ error }}</div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import clienteService from '@/services/clienteService'
import { validarDocumento, filtrarEntradaDocumento, normalizarDocumento } from '@/utils/documentoValidator'

const emit = defineEmits(['cliente-registrado'])
const loading = ref(false)
const error = ref(null)
const documentoError = ref(null)
const fotoFile = ref(null)

const form = ref({
  nombre: '',
  documento: '',
  edad: null,
  genero: '',
  direccion: '',
  telefono: '',
  correo: ''
})

const onDocumentoInput = (event) => {
  const filtrado = filtrarEntradaDocumento(event.target.value)
  form.value.documento = filtrado
  event.target.value = filtrado
  documentoError.value = validarDocumento(filtrado)
}

const onFotoChange = (event) => {
  fotoFile.value = event.target.files[0] || null
}

const handleSubmit = async () => {
  documentoError.value = validarDocumento(form.value.documento)
  if (documentoError.value) return

  try {
    loading.value = true
    error.value = null
    const payload = {
      nombre: form.value.nombre,
      documento: normalizarDocumento(form.value.documento),
      edad: form.value.edad,
      genero: form.value.genero || undefined,
      direccion: form.value.direccion || undefined,
      telefono: form.value.telefono || undefined,
      correo: form.value.correo || undefined
    }
    const { data } = await clienteService.registrar(payload)

    if (fotoFile.value && data.id) {
      await clienteService.subirFoto(data.id, fotoFile.value)
    }
    
    form.value = { nombre: '', documento: '', edad: null, genero: '', direccion: '', telefono: '', correo: '' }
    fotoFile.value = null
    documentoError.value = null
    emit('cliente-registrado')
    
  } catch (err) {
    error.value = err.response?.data?.error || 'Error al registrar cliente.'
  } finally {
    loading.value = false
  }
}
</script>
