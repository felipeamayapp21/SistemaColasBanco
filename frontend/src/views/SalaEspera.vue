<template>
  <div class="tv-container">
    <div class="tv-header">
      <i class="bi bi-bank2"></i> BancoApp — Sala de Espera
    </div>
    <div class="tv-body">
      <div class="tv-turno-actual">
        <div class="tv-label">Turno Actual</div>
        <div class="tv-numero">#{{ estado.turnoActual || '---' }}</div>
        <div class="tv-cliente" v-if="estado.enAtencion">
          {{ estado.enAtencion.nombre }}
        </div>
        <div class="tv-vacio" v-else>En espera...</div>
      </div>
      <div class="tv-proximos">
        <h3>Proximos turnos</h3>
        <div class="tv-lista" v-if="proximos.length > 0">
          <div class="tv-item" v-for="(c, i) in proximos" :key="c.id">
            <span class="tv-item-ticket">{{ c.ticket }}</span>
            <span class="tv-item-nombre">{{ c.nombre }}</span>
            <span class="tv-item-tipo" :class="c.prioridad ? 'text-warning' : ''">
              {{ c.prioridad ? 'Prioritario' : 'Regular' }}
            </span>
          </div>
        </div>
        <div class="tv-vacio" v-else>No hay clientes en espera</div>
      </div>
    </div>
    <div class="tv-footer">
      <div class="tv-stats">
        <span>Esperando: <strong>{{ estado.totalEnEspera }}</strong></span>
        <span>Prioritarios: <strong>{{ estado.prioritariosEnEspera }}</strong></span>
        <span>Regulares: <strong>{{ estado.regularesEnEspera }}</strong></span>
      </div>
      <div class="tv-hora">{{ horaActual }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const estado = ref({
  totalEnEspera: 0,
  prioritariosEnEspera: 0,
  regularesEnEspera: 0,
  turnoActual: 0,
  proximoCliente: null,
  enAtencion: null
})
const colaCompleta = ref([])
const horaActual = ref('')
let ws = null
let wsTimer = null

const proximos = ref([])

const cargarEstado = async () => {
  try {
    const { data } = await axios.get('/api/clientes/tv')
    estado.value = data
  } catch (e) { /* ignore */ }
}

const cargarCola = async () => {
  try {
    const { data } = await axios.get('/api/clientes/cola')
    colaCompleta.value = data
    proximos.value = data.slice(0, 3)
  } catch (e) { /* ignore */ }
}

const actualizarHora = () => {
  horaActual.value = new Date().toLocaleTimeString('es-ES', {
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

const conectarWS = () => {
  try {
    ws = new WebSocket(`ws://${window.location.hostname}:8080/ws/queue`)
    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        if (msg.tipo === 'QUEUE_UPDATE') {
          estado.value = msg.data
          proximos.value = (msg.data.colaCompleta || []).slice(0, 3)
        }
      } catch (e) { /* ignore */ }
    }
    ws.onclose = () => {
      wsTimer = setTimeout(conectarWS, 3000)
    }
    ws.onerror = () => ws?.close()
  } catch (e) {
    wsTimer = setTimeout(conectarWS, 3000)
  }
}

onMounted(async () => {
  await Promise.all([cargarEstado(), cargarCola()])
  conectarWS()
  actualizarHora()
  setInterval(actualizarHora, 1000)
  setInterval(cargarEstado, 30000)
  setInterval(cargarCola, 30000)
})

onUnmounted(() => {
  ws?.close()
  clearTimeout(wsTimer)
})
</script>

<style scoped>
.tv-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif;
}
.tv-header {
  background: rgba(255,255,255,0.05);
  padding: 20px 40px;
  font-size: 1.5rem;
  font-weight: 600;
  border-bottom: 2px solid rgba(255,255,255,0.1);
}
.tv-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 30px;
  padding: 20px;
}
.tv-turno-actual {
  text-align: center;
}
.tv-label {
  font-size: 1.2rem;
  text-transform: uppercase;
  letter-spacing: 4px;
  opacity: 0.7;
}
.tv-numero {
  font-size: 10rem;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #00d2ff, #3a7bd5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  margin: 10px 0;
}
.tv-cliente {
  font-size: 2rem;
  font-weight: 300;
}
.tv-vacio {
  font-size: 1.5rem;
  opacity: 0.5;
  font-style: italic;
}
.tv-proximos {
  width: 100%;
  max-width: 600px;
  text-align: center;
}
.tv-proximos h3 {
  font-size: 1.1rem;
  text-transform: uppercase;
  letter-spacing: 3px;
  opacity: 0.6;
  margin-bottom: 15px;
}
.tv-lista {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.tv-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255,255,255,0.08);
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 1.2rem;
}
.tv-item-ticket {
  font-weight: 700;
  font-size: 1.3rem;
}
.tv-item-nombre {
  flex: 1;
  margin-left: 20px;
  text-align: left;
}
.tv-item-tipo {
  font-size: 0.9rem;
  opacity: 0.7;
}
.tv-footer {
  background: rgba(0,0,0,0.3);
  padding: 15px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tv-stats {
  display: flex;
  gap: 30px;
  font-size: 1rem;
}
.tv-hora {
  font-size: 1.2rem;
  font-weight: 300;
  font-family: monospace;
}
</style>
