<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="page-title metro-page-title mb-0">Dashboard</h2>
        <p class="page-subtitle">Panel Metro - cola de atencion en tiempo real</p>
      </div>
      <div class="d-flex align-items-center gap-2">
        <select v-model="filtroCola" class="form-select form-select-sm" style="width:auto">
          <option value="general">General</option>
          <option value="prioritarios">Solo Prioritarios</option>
          <option value="regulares">Solo Generales</option>
        </select>
      </div>
    </div>

    <div class="metro-tile-grid mb-4">
      <MetroTile icon="bi-people" label="En espera" :value="estadoCola.totalEnEspera" color="tile-blue" />
      <MetroTile icon="bi-star" label="Prioritarios" :value="estadoCola.prioritariosEnEspera" color="tile-amber" />
      <MetroTile icon="bi-person" label="Regulares" :value="estadoCola.regularesEnEspera" color="tile-teal" />
      <MetroTile icon="bi-check-circle" label="Turno atendido" :value="'#' + estadoCola.turnoActual" color="tile-green" />
    </div>

    <div class="row g-4">
      <div class="col-lg-4">
        <ColaTurnos
          class="metro-turno"
          :turno-actual="estadoCola.turnoActual"
          :proximo-cliente="estadoCola.proximoCliente"
        />
        <div class="d-grid gap-2 mt-3">
          <button
            class="btn btn-accent metro-btn-call"
            :disabled="loadingLlamar || estadoCola.totalEnEspera === 0"
            @click="llamarSiguiente"
          >
            <span v-if="loadingLlamar" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-megaphone-fill me-2"></i> Llamar siguiente
          </button>
        </div>

        <div v-if="estadoCola.enAtencion" class="card-banco metro-panel mt-3 p-3 border-warning">
          <h6 class="fw-bold text-warning"><i class="bi bi-person-workspace"></i> En Atencion</h6>
          <p class="mb-1"><strong>{{ estadoCola.enAtencion.ticket }}</strong> - {{ estadoCola.enAtencion.nombre }}</p>
          <div class="d-flex gap-2 mt-2">
            <button class="btn btn-success btn-sm flex-fill" @click="abrirFinalizar(estadoCola.enAtencion)">
              <i class="bi bi-check-lg"></i> Finalizar
            </button>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card-banco metro-panel h-100">
          <h5 class="mb-3 fw-semibold">
            <i class="bi bi-list-ol"></i>
            {{ filtroCola === 'prioritarios' ? 'Solo Prioritarios' : filtroCola === 'regulares' ? 'Solo Generales' : 'Proximos en cola' }}
          </h5>
          <TablaClientes :clientes="colaFiltrada" @cancelar="cancelarCliente" />
        </div>
      </div>
    </div>

    <!-- Modal Finalizar Atencion -->
    <div class="modal fade" id="modalFinalizar" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-warning text-dark">
            <h5 class="modal-title"><i class="bi bi-check-circle"></i> Finalizar Atencion</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p class="mb-2">
              Cliente: <strong>{{ clienteFinalizar?.ticket }} - {{ clienteFinalizar?.nombre }}</strong>
            </p>
            <div class="mb-3">
              <label class="form-label">Motivo Visita</label>
              <input type="text" v-model="formFinalizar.motivoVisita" class="form-control" placeholder="Ej: Consulta general" />
            </div>
            <div class="mb-3">
              <label class="form-label">Tipo Tramite</label>
              <select v-model="formFinalizar.tipoTramite" class="form-select">
                <option value="">Seleccionar</option>
                <option value="Caja">Caja</option>
                <option value="Asesoria">Asesoria</option>
                <option value="Ventanilla">Ventanilla</option>
                <option value="Credito">Credito</option>
                <option value="Otro">Otro</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">Observaciones</label>
              <textarea v-model="formFinalizar.observaciones" class="form-control" rows="2" placeholder="Notas adicionales..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-success" :disabled="loadingFinalizar" @click="finalizarAtencion">
              <span v-if="loadingFinalizar" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-check-lg"></i> Finalizar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { Modal } from 'bootstrap'
import clienteService from '@/services/clienteService'
import wsService from '@/services/websocket'
import ColaTurnos from '@/components/ColaTurnos.vue'
import TablaClientes from '@/components/TablaClientes.vue'
import MetroTile from '@/components/MetroTile.vue'

const estadoCola = ref({
  totalEnEspera: 0,
  prioritariosEnEspera: 0,
  regularesEnEspera: 0,
  turnoActual: 0,
  proximoCliente: null,
  enAtencion: null
})

const colaCompleta = ref([])
const colaPrioritarios = ref([])
const colaRegulares = ref([])
const loadingLlamar = ref(false)
const loadingFinalizar = ref(false)
const filtroCola = ref('general')
let unsubscribe = null
let modalFinalizar = null

const clienteFinalizar = ref(null)
const formFinalizar = ref({ motivoVisita: '', tipoTramite: '', observaciones: '' })

const colaFiltrada = computed(() => {
  switch (filtroCola.value) {
    case 'prioritarios': return colaPrioritarios.value
    case 'regulares': return colaRegulares.value
    default: return colaCompleta.value
  }
})

const loadDashboard = async () => {
  try {
    const [estadoRes, colaRes, prioRes, regRes] = await Promise.all([
      clienteService.getEstado(),
      clienteService.getCola(),
      clienteService.getPrioritarios(),
      clienteService.getRegulares()
    ])
    estadoCola.value = estadoRes.data
    colaCompleta.value = colaRes.data
    colaPrioritarios.value = prioRes.data
    colaRegulares.value = regRes.data
  } catch (e) {
    console.error('Error cargando dashboard:', e)
  }
}

const handleWsMessage = (msg) => {
  if (msg.tipo === 'QUEUE_UPDATE') {
    const d = msg.data
    estadoCola.value = {
      totalEnEspera: d.totalEnEspera,
      prioritariosEnEspera: d.prioritariosEnEspera,
      regularesEnEspera: d.regularesEnEspera,
      turnoActual: d.turnoActual,
      proximoCliente: d.proximoCliente,
      enAtencion: d.enAtencion
    }
    colaCompleta.value = d.colaCompleta || []
    colaPrioritarios.value = d.colaPrioritarios || []
    colaRegulares.value = d.colaRegulares || []
  }
}

const llamarSiguiente = async () => {
  try {
    loadingLlamar.value = true
    await clienteService.llamarSiguiente()
    await loadDashboard()
  } catch (err) {
    alert(err.response?.data?.error || 'Error al llamar siguiente')
  } finally {
    loadingLlamar.value = false
  }
}

const abrirFinalizar = (cliente) => {
  clienteFinalizar.value = cliente
  formFinalizar.value = { motivoVisita: '', tipoTramite: '', observaciones: '' }
  if (modalFinalizar) modalFinalizar.show()
}

const finalizarAtencion = async () => {
  if (!clienteFinalizar.value) return
  try {
    loadingFinalizar.value = true
    await clienteService.finalizarAtencion(clienteFinalizar.value.id, formFinalizar.value)
    if (modalFinalizar) modalFinalizar.hide()
    await loadDashboard()
  } catch (err) {
    alert(err.response?.data?.error || 'Error al finalizar atencion')
  } finally {
    loadingFinalizar.value = false
  }
}

const cancelarCliente = async (id) => {
  if (confirm('Cancelar este turno?')) {
    try {
      await clienteService.cancelar(id)
      await loadDashboard()
    } catch {
      alert('Error al cancelar')
    }
  }
}

onMounted(() => {
  loadDashboard()
  wsService.connect()
  unsubscribe = wsService.subscribe(handleWsMessage)
  modalFinalizar = new Modal(document.getElementById('modalFinalizar'))
})

onUnmounted(() => {
  unsubscribe?.()
  wsService.disconnect()
})
</script>
