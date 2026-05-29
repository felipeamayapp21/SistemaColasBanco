import api from './api'

export default {
  getCola()              { return api.get('/clientes/cola') },
  getPrioritarios()      { return api.get('/clientes/prioritarios') },
  getRegulares()         { return api.get('/clientes/regulares') },
  registrar(cliente)     { return api.post('/clientes', cliente) },
  getEstado()            { return api.get('/clientes/estado') },
  llamarSiguiente()      { return api.put('/clientes/llamar-siguiente') },
  iniciarAtencion(id)    { return api.put(`/clientes/${id}/iniciar-atencion`) },
  finalizarAtencion(id, data) { return api.put(`/clientes/${id}/finalizar-atencion`, data) },
  cancelar(id)           { return api.delete(`/clientes/${id}`) },
  getCliente(id)         { return api.get(`/clientes/${id}`) },
  getHistorialCliente(id){ return api.get(`/clientes/${id}/historial`) },
  buscar(termino)        { return api.get('/clientes/buscar', { params: { q: termino } }) },
  subirFoto(id, file)    { const fd = new FormData(); fd.append('file', file); return api.post(`/clientes/${id}/foto`, fd, { headers: { 'Content-Type': 'multipart/form-data' } }) },
  getFotoUrl(id)         { return `${api.defaults.baseURL}/clientes/${id}/foto` }
}
