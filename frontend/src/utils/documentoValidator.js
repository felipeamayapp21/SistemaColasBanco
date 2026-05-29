const MAX_DIGITOS = 10
const SOLO_NUMEROS = /^\d+$/

export function normalizarDocumento(documento) {
  return (documento ?? '').trim()
}

/**
 * Valida el documento. Retorna mensaje de error o null si es válido.
 */
export function validarDocumento(documento) {
  const doc = normalizarDocumento(documento)

  if (!doc) {
    return 'El documento es obligatorio.'
  }
  if (!SOLO_NUMEROS.test(doc)) {
    return 'El documento solo puede contener números. No se permiten letras ni caracteres especiales.'
  }
  if (doc.length > MAX_DIGITOS) {
    return `El documento no puede superar los ${MAX_DIGITOS} dígitos.`
  }
  return null
}

/**
 * Filtra la entrada para permitir solo dígitos y limitar longitud.
 */
export function filtrarEntradaDocumento(valor) {
  return valor.replace(/\D/g, '').slice(0, MAX_DIGITOS)
}
