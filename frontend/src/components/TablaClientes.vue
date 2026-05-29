<template>
  <div class="table-responsive">
    <table class="table table-banco mb-0">
      <thead>
        <tr>
          <th>Ticket</th>
          <th>Nombre</th>
          <th>Documento</th>
          <th>Edad</th>
          <th>Tipo</th>
          <th>Estado</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="clientes.length === 0">
          <td colspan="7" class="text-center py-4 text-muted">No hay clientes registrados o en espera.</td>
        </tr>
        <tr v-for="(cliente, idx) in clientes" :key="cliente.id">
          <td><span class="badge bg-dark">{{ cliente.ticket || '-' }}</span></td>
          <td class="fw-medium">{{ cliente.nombre }}</td>
          <td>{{ cliente.documento }}</td>
          <td>{{ cliente.edad }}</td>
          <td>
            <span v-if="cliente.prioridad" class="badge badge-prioritario"><i class="bi bi-star-fill"></i> PRIORIDAD</span>
            <span v-else class="text-muted">Regular</span>
          </td>
          <td>
            <span class="badge" :class="getBadgeClass(cliente.estado)">{{ cliente.estado }}</span>
          </td>
          <td>
            <button 
              v-if="cliente.estado === 'ESPERA'" 
              class="btn btn-sm btn-outline-danger" 
              @click="$emit('cancelar', cliente.id)"
              title="Cancelar Turno">
              <i class="bi bi-x-circle"></i>
            </button>
            <span v-else class="text-muted"><i class="bi bi-dash"></i></span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
defineProps({
  clientes: { type: Array, required: true }
})

defineEmits(['cancelar'])

const getBadgeClass = (estado) => {
  switch (estado) {
    case 'ESPERA': return 'badge-espera'
    case 'EN_ATENCION': return 'badge-atencion'
    case 'ATENDIDO': return 'badge-atendido'
    case 'CANCELADO': return 'badge-cancelado'
    default: return 'bg-secondary'
  }
}
</script>
