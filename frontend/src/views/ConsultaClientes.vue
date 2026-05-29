<template>
  <div>
    <h2 class="page-title metro-page-title">Consulta de Clientes</h2>
    <p class="page-subtitle">Buscar clientes por cedula o nombre</p>

    <div class="card-banco mb-4">
      <div class="row g-2 align-items-center">
        <div class="col-md-8">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input
              type="text"
              v-model="termino"
              class="form-control"
              placeholder="Buscar por cedula o nombre..."
              @keyup.enter="buscar"
            />
            <button class="btn btn-banco" @click="buscar" :disabled="!termino.trim()">
              <i class="bi bi-search"></i> Buscar
            </button>
          </div>
          <small class="text-muted">Ingrese al menos 3 caracteres para buscar.</small>
        </div>
        <div class="col-md-4 text-end">
          <span v-if="resultados.length" class="text-muted">{{ resultados.length }} resultado(s)</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4">
      <span class="spinner-border text-primary"></span>
      <p class="mt-2 text-muted">Buscando...</p>
    </div>

    <div v-if="errorBusqueda" class="alert alert-danger">{{ errorBusqueda }}</div>

    <!-- Resultados -->
    <div v-if="!loading && resultados.length > 0" class="row g-4">
      <div v-for="cliente in resultados" :key="cliente.id" class="col-md-6 col-lg-4">
        <div class="card-banco metro-panel h-100" :class="{ 'border-warning': cliente.estado === 'EN_ATENCION', 'border-primary': cliente.estado === 'ESPERA' }">
          <div class="d-flex align-items-start gap-3">
            <div class="foto-cliente" @click="verDetalle(cliente)" style="cursor:pointer">
              <img
                v-if="cliente.foto"
                :src="`/api/clientes/${cliente.id}/foto`"
                class="rounded-circle"
                width="64" height="64"
                style="object-fit:cover"
                alt="foto"
              />
              <div v-else class="foto-placeholder rounded-circle d-flex align-items-center justify-content-center" style="width:64px;height:64px">
                <i class="bi bi-person fs-2 text-muted"></i>
              </div>
            </div>
            <div class="flex-grow-1">
              <div class="d-flex justify-content-between">
                <h6 class="mb-0 fw-bold">{{ cliente.nombre }}</h6>
                <span class="badge bg-dark">{{ cliente.ticket || '-' }}</span>
              </div>
              <small class="text-muted d-block">CC: {{ cliente.documento }}</small>
              <small class="d-block">
                <span v-if="cliente.prioridad" class="badge badge-prioritario">Prioritario</span>
                <span v-else class="badge bg-secondary">Regular</span>
                <span class="badge ms-1" :class="badgeEstado(cliente.estado)">{{ cliente.estado }}</span>
              </small>
              <p class="mt-2 mb-0 small">
                <i class="bi bi-telephone"></i> {{ cliente.telefono || '-' }} |
                <i class="bi bi-envelope"></i> {{ cliente.correo || '-' }}
              </p>
            </div>
          </div>
          <hr class="my-2" />
          <div class="d-flex gap-2">
            <button class="btn btn-sm btn-outline-primary flex-fill" @click="verDetalle(cliente)">
              <i class="bi bi-eye"></i> Ver detalle
            </button>
            <button class="btn btn-sm btn-outline-info flex-fill" @click="verHistorial(cliente)">
              <i class="bi bi-clock-history"></i> Historial
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && buscado && resultados.length === 0" class="text-center py-5 text-muted">
      <i class="bi bi-search fs-1"></i>
      <p class="mt-2">No se encontraron clientes con el termino "{{ termino }}"</p>
    </div>

    <!-- Modal Detalle Cliente -->
    <div class="modal fade" id="modalDetalle" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title"><i class="bi bi-person-badge"></i> Detalle del Cliente</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="detalle">
            <div class="row">
              <div class="col-md-4 text-center mb-3">
                <img
                  v-if="detalle.foto"
                  :src="`/api/clientes/${detalle.id}/foto`"
                  class="rounded"
                  width="150" height="150"
                  style="object-fit:cover"
                />
                <div v-else class="foto-placeholder rounded d-inline-flex align-items-center justify-content-center" style="width:150px;height:150px">
                  <i class="bi bi-person fs-1 text-muted"></i>
                </div>
              </div>
              <div class="col-md-8">
                <table class="table table-sm table-borderless">
                  <tr><th>Ticket:</th><td><span class="badge bg-dark fs-6">{{ detalle.ticket }}</span></td></tr>
                  <tr><th>Nombre:</th><td>{{ detalle.nombre }}</td></tr>
                  <tr><th>Documento:</th><td>{{ detalle.documento }}</td></tr>
                  <tr><th>Edad:</th><td>{{ detalle.edad }} años</td></tr>
                  <tr><th>Genero:</th><td>{{ detalle.genero || '-' }}</td></tr>
                  <tr><th>Direccion:</th><td>{{ detalle.direccion || '-' }}</td></tr>
                  <tr><th>Telefono:</th><td>{{ detalle.telefono || '-' }}</td></tr>
                  <tr><th>Correo:</th><td>{{ detalle.correo || '-' }}</td></tr>
                  <tr><th>Tipo:</th><td><span v-if="detalle.prioridad" class="badge badge-prioritario">Prioritario</span><span v-else class="badge bg-secondary">Regular</span></td></tr>
                  <tr><th>Estado:</th><td><span class="badge" :class="badgeEstado(detalle.estado)">{{ detalle.estado }}</span></td></tr>
                  <tr><th>Hora Ingreso:</th><td>{{ formatearFecha(detalle.horaIngreso) }}</td></tr>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Historial Cliente -->
    <div class="modal fade" id="modalHistorial" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title"><i class="bi bi-clock-history"></i> Historial del Cliente</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="historialCliente.length > 0">
            <div class="table-responsive">
              <table class="table table-sm table-banco">
                <thead>
                  <tr>
                    <th>Ticket</th>
                    <th>Fecha</th>
                    <th>Espera</th>
                    <th>Duracion</th>
                    <th>Motivo</th>
                    <th>Tramite</th>
                    <th>Cajero</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="h in historialCliente" :key="h.id">
                    <td>{{ h.ticket || '-' }}</td>
                    <td>{{ formatearFecha(h.horaAtencion) }}</td>
                    <td>{{ formatSegundos(h.tiempoEspera) }}</td>
                    <td>{{ formatSegundos(h.duracion) }}</td>
                    <td><small>{{ h.motivoVisita || '-' }}</small></td>
                    <td>{{ h.tipoTramite || '-' }}</td>
                    <td>{{ h.cajeroUsuario || '-' }}</td>
                    <td><span class="badge bg-success">{{ h.estadoAtencion }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-body text-center text-muted" v-else>
            <p>No hay historial de atencion para este cliente.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Modal } from 'bootstrap'
import clienteService from '@/services/clienteService'

const termino = ref('')
const resultados = ref([])
const loading = ref(false)
const errorBusqueda = ref(null)
const buscado = ref(false)

const detalle = ref(null)
const historialCliente = ref([])
let modalDetalle = null
let modalHistorial = null

const buscar = async () => {
  const q = termino.value.trim()
  if (q.length < 3) return
  try {
    loading.value = true
    errorBusqueda.value = null
    buscado.value = true
    const { data } = await clienteService.buscar(q)
    resultados.value = data
  } catch (err) {
    errorBusqueda.value = 'Error al buscar clientes.'
    resultados.value = []
  } finally {
    loading.value = false
  }
}

const verDetalle = async (cliente) => {
  try {
    const { data } = await clienteService.getCliente(cliente.id)
    detalle.value = data
  } catch {
    detalle.value = cliente
  }
  if (!modalDetalle) modalDetalle = new Modal(document.getElementById('modalDetalle'))
  modalDetalle.show()
}

const verHistorial = async (cliente) => {
  try {
    const { data } = await clienteService.getHistorialCliente(cliente.id)
    historialCliente.value = data
  } catch {
    historialCliente.value = []
  }
  if (!modalHistorial) modalHistorial = new Modal(document.getElementById('modalHistorial'))
  modalHistorial.show()
}

const formatearFecha = (fechaStr) => {
  if (!fechaStr) return ''
  const d = new Date(fechaStr)
  return d.toLocaleString('es-ES', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

const formatSegundos = (segs) => {
  if (!segs && segs !== 0) return '-'
  if (segs < 60) return `${segs}s`
  const min = Math.floor(segs / 60)
  return `${min}m ${segs % 60}s`
}

const badgeEstado = (estado) => {
  switch (estado) {
    case 'ESPERA': return 'badge-espera'
    case 'EN_ATENCION': return 'badge-atencion'
    case 'ATENDIDO': return 'badge-atendido'
    case 'CANCELADO': return 'badge-cancelado'
    default: return 'bg-secondary'
  }
}
</script>

<style scoped>
.foto-cliente:hover {
  opacity: 0.8;
}
</style>
