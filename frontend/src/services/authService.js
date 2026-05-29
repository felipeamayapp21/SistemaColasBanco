import api from './api'

export default {
  login(username, password) {
    return api.post('/auth/login', { username, password }).then(r => r.data)
  },
  register(payload) {
    return api.post('/auth/register', payload).then(r => r.data)
  },
  obtenerDatosRecuperacion(username) {
    return api.post('/auth/recuperar/datos', { username }).then(r => r.data)
  },
  recuperarPassword(username, respuesta, nuevaPassword) {
    return api.post('/auth/recuperar/restablecer', { username, respuesta, nuevaPassword }).then(r => r.data)
  }
}
