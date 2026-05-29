import api from './api'

export default {
  getHistorial()        { return api.get('/historial') },
  getIndicadores()      { return api.get('/historial/indicadores') },
  getAtencionesPorDia() { return api.get('/historial/stats/atenciones-por-dia') },
  getPorTramite()       { return api.get('/historial/stats/por-tramite') },
  getPorCajero()        { return api.get('/historial/stats/por-cajero') }
}
