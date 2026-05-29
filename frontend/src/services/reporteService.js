import api from './api'

async function descargar(url, nombre) {
  const { data } = await api.get(url, { responseType: 'blob' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(data)
  link.download = nombre
  link.click()
  URL.revokeObjectURL(link.href)
}

export default {
  descargarPdf()   { return descargar('/reportes/pdf', 'historial_atencion.pdf') },
  descargarExcel() { return descargar('/reportes/excel', 'historial_atencion.xlsx') }
}
