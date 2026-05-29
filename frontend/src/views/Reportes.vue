<template>
  <div>
    <h2 class="page-title metro-page-title">Reportes e Indicadores</h2>
    <p class="page-subtitle">Exportacion PDF y Excel del historial de atencion</p>

    <div class="metro-tile-grid mb-4">
      <div class="metro-tile tile-blue">
        <i class="bi bi-check-all"></i>
        <span class="metro-tile-label">Total atendidos</span>
        <span class="metro-tile-value">{{ indicadores.totalAtendidos }}</span>
      </div>
      <div class="metro-tile tile-teal">
        <i class="bi bi-stopwatch"></i>
        <span class="metro-tile-label">Tiempo promedio</span>
        <span class="metro-tile-value">{{ formatEspera(indicadores.tiempoPromedioSegundos) }}</span>
      </div>
      <div class="metro-tile tile-amber">
        <i class="bi bi-star"></i>
        <span class="metro-tile-label">Prioritarios</span>
        <span class="metro-tile-value">{{ indicadores.prioritariosAtendidos }}</span>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-md-6">
        <div class="card-banco metro-panel p-3">
          <h5 class="mb-3"><i class="bi bi-calendar"></i> Atenciones por dia</h5>
          <div v-if="statsPorDia.length > 0" style="min-height:200px"><canvas ref="chartDia"></canvas></div>
          <p v-else class="text-muted text-center my-5">Sin datos</p>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card-banco metro-panel p-3">
          <h5 class="mb-3"><i class="bi bi-pie-chart"></i> Por tipo de tramite</h5>
          <div v-if="statsPorTramite.length > 0" style="min-height:200px"><canvas ref="chartTramite"></canvas></div>
          <p v-else class="text-muted text-center my-5">Sin datos</p>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-12">
        <div class="card-banco metro-panel p-3">
          <h5 class="mb-3"><i class="bi bi-person-badge"></i> Atenciones por cajero</h5>
          <div v-if="statsPorCajero.length > 0" style="min-height:200px"><canvas ref="chartCajero"></canvas></div>
          <p v-else class="text-muted text-center my-5">Sin datos</p>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-md-6">
        <div class="card-banco metro-panel p-0 overflow-hidden">
          <button type="button" class="metro-report-tile pdf" :disabled="descargando" @click="descargarPdf">
            <i class="bi bi-file-pdf display-4 mb-3"></i>
            <h4>Reporte PDF</h4>
            <p class="small opacity-75 mb-3">Historial completo para impresion</p>
            <span v-if="descargando === 'pdf'" class="spinner-border spinner-border-sm"></span>
            <span v-else><i class="bi bi-download me-1"></i> Descargar</span>
          </button>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card-banco metro-panel p-0 overflow-hidden">
          <button type="button" class="metro-report-tile excel" :disabled="descargando" @click="descargarExcel">
            <i class="bi bi-file-excel display-4 mb-3"></i>
            <h4>Reporte Excel</h4>
            <p class="small opacity-75 mb-3">Datos para analisis en hoja de calculo</p>
            <span v-if="descargando === 'excel'" class="spinner-border spinner-border-sm"></span>
            <span v-else><i class="bi bi-download me-1"></i> Descargar</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="errorDescarga" class="alert alert-danger mt-3">{{ errorDescarga }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import turnoService from '@/services/turnoService'
import reporteService from '@/services/reporteService'

Chart.register(...registerables)

const indicadores = ref({
  totalAtendidos: 0,
  tiempoPromedioSegundos: 0,
  prioritariosAtendidos: 0
})

const statsPorDia = ref([])
const statsPorTramite = ref([])
const statsPorCajero = ref([])
const descargando = ref(false)
const errorDescarga = ref(null)

const chartDia = ref(null)
const chartTramite = ref(null)
const chartCajero = ref(null)

let charts = []

const cargarIndicadores = async () => {
  try {
    const { data } = await turnoService.getIndicadores()
    indicadores.value = data
  } catch (err) {
    console.error('Error cargando indicadores', err)
  }
}

const cargarStats = async () => {
  try {
    const [diaRes, tramiteRes, cajeroRes] = await Promise.all([
      turnoService.getAtencionesPorDia(),
      turnoService.getPorTramite(),
      turnoService.getPorCajero()
    ])
    statsPorDia.value = diaRes.data
    statsPorTramite.value = tramiteRes.data
    statsPorCajero.value = cajeroRes.data
    await nextTick()
    renderizarGraficos()
  } catch (err) {
    console.error('Error cargando stats', err)
  }
}

const renderizarGraficos = () => {
  charts.forEach(c => c.destroy())
  charts = []

  if (statsPorDia.value.length > 0) {
    const ctx = chartDia.value?.getContext('2d')
    if (ctx) {
      charts.push(new Chart(ctx, {
        type: 'bar',
        data: {
          labels: statsPorDia.value.map(d => d.dia?.substring(5) || ''),
          datasets: [{
            label: 'Atenciones',
            data: statsPorDia.value.map(d => d.total),
            backgroundColor: 'rgba(13, 110, 253, 0.6)',
            borderColor: 'rgba(13, 110, 253, 1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
        }
      }))
    }
  }

  if (statsPorTramite.value.length > 0) {
    const colores = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1', '#20c997']
    const ctx = chartTramite.value?.getContext('2d')
    if (ctx) {
      charts.push(new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: statsPorTramite.value.map(d => d.tramite),
          datasets: [{
            data: statsPorTramite.value.map(d => d.total),
            backgroundColor: statsPorTramite.value.map((_, i) => colores[i % colores.length])
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: 'bottom' }
          }
        }
      }))
    }
  }

  if (statsPorCajero.value.length > 0) {
    const ctx = chartCajero.value?.getContext('2d')
    if (ctx) {
      charts.push(new Chart(ctx, {
        type: 'bar',
        data: {
          labels: statsPorCajero.value.map(d => d.cajero),
          datasets: [{
            label: 'Atenciones',
            data: statsPorCajero.value.map(d => d.total),
            backgroundColor: 'rgba(25, 135, 84, 0.6)',
            borderColor: 'rgba(25, 135, 84, 1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
        }
      }))
    }
  }
}

const descargarPdf = async () => {
  descargando.value = 'pdf'
  errorDescarga.value = null
  try {
    await reporteService.descargarPdf()
  } catch (err) {
    errorDescarga.value = 'No se pudo descargar el PDF. Verifica que el backend este activo.'
    console.error(err)
  } finally {
    descargando.value = false
  }
}

const descargarExcel = async () => {
  descargando.value = 'excel'
  errorDescarga.value = null
  try {
    await reporteService.descargarExcel()
  } catch (err) {
    errorDescarga.value = 'No se pudo descargar el Excel. Verifica que el backend este activo.'
    console.error(err)
  } finally {
    descargando.value = false
  }
}

const formatEspera = (segs) => {
  if (!segs) return '0s'
  if (segs < 60) return `${segs}s`
  const min = Math.floor(segs / 60)
  return `${min}m`
}

onMounted(() => {
  cargarIndicadores()
  cargarStats()
})
</script>

