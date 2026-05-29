<template>
  <div>
    <h2 class="page-title metro-page-title">Gestion de Clientes</h2>
    <p class="page-subtitle">Registro y cola de espera</p>

    <div class="row g-4">
      <div class="col-12">
        <ClienteForm @cliente-registrado="cargarCola" />
      </div>

      <div class="col-12">
        <div class="card-banco metro-panel">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="text-primary mb-0"><i class="bi bi-list-task"></i> Cola Actual de Espera</h5>
            <button class="btn btn-sm btn-outline-primary" @click="cargarCola" title="Actualizar">
              <i class="bi bi-arrow-clockwise"></i>
            </button>
          </div>
          
          <TablaClientes :clientes="cola" @cancelar="cancelarCliente" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import clienteService from '@/services/clienteService'
import ClienteForm from '@/components/ClienteForm.vue'
import TablaClientes from '@/components/TablaClientes.vue'

const cola = ref([])

const cargarCola = async () => {
  try {
    const { data } = await clienteService.getCola()
    cola.value = data
  } catch (err) {
    console.error('Error cargando cola', err)
  }
}

const cancelarCliente = async (id) => {
  if (confirm('Seguro que deseas cancelar este turno?')) {
    try {
      await clienteService.cancelar(id)
      cargarCola()
    } catch (err) {
      alert('Error al cancelar turno')
    }
  }
}

onMounted(cargarCola)
</script>
