<template>
  <div>
    <h2 class="page-title metro-page-title">Historial de Atencion</h2>
    <p class="page-subtitle">Clientes ya atendidos por el sistema</p>

    <div class="card-banco">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <p class="mb-0 text-muted">Todos los clientes que han sido atendidos por el sistema.</p>
        <button class="btn btn-sm btn-outline-primary" @click="cargarHistorial">
          <i class="bi bi-arrow-clockwise"></i> Refrescar
        </button>
      </div>

      <div class="table-responsive">
        <table class="table table-banco mb-0">
          <thead>
            <tr>
              <th>Ticket</th>
              <th>Nombre</th>
              <th>Documento</th>
              <th>Tipo</th>
              <th>Motivo</th>
              <th>Tramite</th>
              <th>Hora Atencion</th>
              <th>Espera</th>
              <th>Duracion</th>
              <th>Cajero</th>
              <th>Obs.</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="11" class="text-center py-4 text-muted">
                <span class="spinner-border spinner-border-sm me-2"></span> Cargando...
              </td>
            </tr>
            <tr v-else-if="historial.length === 0">
              <td colspan="11" class="text-center py-4 text-muted">No hay registros de atencion.</td>
            </tr>
            <tr v-for="h in historial" :key="h.id">
              <td><span class="badge bg-dark">{{ h.ticket || '-' }}</span></td>
              <td class="fw-medium">{{ h.cliente?.nombre }}</td>
              <td>{{ h.cliente?.documento }}</td>
              <td>
                <span v-if="h.cliente?.prioridad" class="badge badge-prioritario">Prioridad</span>
                <span v-else class="text-muted">Regular</span>
              </td>
              <td><small>{{ h.motivoVisita || '-' }}</small></td>
              <td><span class="badge bg-info bg-opacity-25 text-dark">{{ h.tipoTramite || '-' }}</span></td>
              <td>{{ formatearFecha(h.horaAtencion) }}</td>
              <td>{{ formatSegundos(h.tiempoEspera) }}</td>
              <td>{{ formatSegundos(h.duracion) }}</td>
              <td><span class="badge bg-light text-dark border"><i class="bi bi-person"></i> {{ h.cajeroUsuario }}</span></td>
              <td>
                <span v-if="h.observaciones" :title="h.observaciones">
                  <i class="bi bi-chat-dots text-muted"></i>
                </span>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import turnoService from '@/services/turnoService'

const historial = ref([])
const loading = ref(false)

const cargarHistorial = async () => {
  try {
    loading.value = true
    const { data } = await turnoService.getHistorial()
    historial.value = data
  } catch (err) {
    console.error('Error cargando historial', err)
  } finally {
    loading.value = false
  }
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
  const s = segs % 60
  return `${min}m ${s}s`
}

onMounted(cargarHistorial)
</script>
