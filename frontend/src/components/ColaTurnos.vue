<template>
  <div class="turno-badge metro-turno">
    <div class="turno-label">Turno Actual</div>
    <div class="turno-num">#{{ turnoActual }}</div>
    <div v-if="proximoCliente && proximoCliente !== 'Ninguno'" class="mt-3 fs-5">
      Siguiente: <strong class="text-warning">{{ proximoCliente }}</strong>
    </div>
    <div v-else class="mt-3 fs-6 text-white-50">
      Cola Vacia
    </div>
  </div>
</template>

<script setup>
defineProps({
  turnoActual: { type: Number, default: 0 },
  proximoCliente: { type: String, default: null }
})
</script>
