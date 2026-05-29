package com.banco.utils;

import com.banco.models.Cliente;

/**
 * ColaPrioritaria — Gestiona dos ColaBancaria para implementar
 * la lógica de prioridad del sistema bancario.
 *
 * Regla de negocio:
 *   - Clientes con edad > 60 → ingresan a colaPrioritaria
 *   - Clientes regulares     → ingresan a colaRegular
 *   - Al atender: SIEMPRE se atiende primero colaPrioritaria.
 *     Si está vacía, se atiende colaRegular.
 *
 * Ambas colas son ColaBancaria (lista enlazada manual).
 */
public class ColaPrioritaria {

    private final ColaBancaria colaPrioritaria; // Adultos mayores (>60 años)
    private final ColaBancaria colaRegular;     // Clientes regulares
    private int turnoActual;

    public ColaPrioritaria() {
        colaPrioritaria = new ColaBancaria();
        colaRegular     = new ColaBancaria();
        turnoActual     = 0;
    }

    /**
     * Ingresa un cliente a la cola correspondiente según su edad.
     * Si edad > 60 → cola prioritaria; de lo contrario → cola regular.
     */
    public void ingresarCliente(Cliente cliente) {
        if (cliente.isPrioridad()) {
            colaPrioritaria.insertar(cliente);
        } else {
            colaRegular.insertar(cliente);
        }
    }

    /**
     * Atiende al siguiente cliente.
     * Primero vacía colaPrioritaria; si está vacía, atiende colaRegular.
     * Retorna null si ambas colas están vacías.
     */
    public Cliente atenderSiguiente() {
        if (!colaPrioritaria.estaVacia()) {
            turnoActual++;
            return (Cliente) colaPrioritaria.extraer();
        }
        if (!colaRegular.estaVacia()) {
            turnoActual++;
            return (Cliente) colaRegular.extraer();
        }
        return null; // No hay clientes en espera
    }

    /** Consulta el próximo cliente a atender sin sacarlo de la cola. */
    public Cliente verProximo() {
        if (!colaPrioritaria.estaVacia()) {
            return (Cliente) colaPrioritaria.verFrente();
        }
        if (!colaRegular.estaVacia()) {
            return (Cliente) colaRegular.verFrente();
        }
        return null;
    }

    /** Total de clientes en espera (ambas colas). */
    public int totalEnEspera() {
        return colaPrioritaria.getTamaño() + colaRegular.getTamaño();
    }

    /** Clientes prioritarios en espera. */
    public int clientesPrioritariosEnEspera() {
        return colaPrioritaria.getTamaño();
    }

    /** Clientes regulares en espera. */
    public int clientesRegularesEnEspera() {
        return colaRegular.getTamaño();
    }

    /** Verifica si no hay clientes en ninguna cola. */
    public boolean estaVacia() {
        return colaPrioritaria.estaVacia() && colaRegular.estaVacia();
    }

    /** Turno actual (incrementa con cada atención). */
    public int getTurnoActual() {
        return turnoActual;
    }

    /** Remueve un cliente por ID de cualquiera de las dos colas. */
    public boolean removerPorId(Long id) {
        boolean removido = colaPrioritaria.removerPorId(id);
        if (!removido) {
            removido = colaRegular.removerPorId(id);
        }
        return removido;
    }

    /** Imprime ambas colas (para depuración). */
    public void imprimir() {
        System.out.println("\n>>> COLA PRIORITARIA (Adultos mayores):");
        colaPrioritaria.imprimir();
        System.out.println("\n>>> COLA REGULAR:");
        colaRegular.imprimir();
    }
}
