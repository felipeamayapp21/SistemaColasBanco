import api from './api'

export default {
  listarUsuarios()    { return api.get('/admin/usuarios') },
  crearUsuario(data)  { return api.post('/admin/usuarios', data) },
  toggleEstado(id, activo) { return api.put(`/admin/usuarios/${id}/estado`, { activo }) },
  cambiarRol(id, rol) { return api.put(`/admin/usuarios/${id}/rol`, { rol }) },
  eliminarUsuario(id) { return api.delete(`/admin/usuarios/${id}`) }
}
