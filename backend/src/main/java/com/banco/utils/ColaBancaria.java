package com.banco.utils;

import com.banco.models.Cliente;

/**
 * ColaBancaria — Cola FIFO implementada con lista enlazada.
 *
 * Implementación manual basada en NodoCola (sin usar java.util.Queue
 * ni ninguna colección de Java), siguiendo la estructura del proyecto.
 *
 * Operaciones:
 *   insertar(x)  → agrega al fondo  — O(1)
 *   extraer()    → retira del frente — O(1)
 *   estaVacia()  → verificación      — O(1)
 *   getTamaño()  → tamaño actual     — O(1)
 */
public class ColaBancaria {

    private NodoCola cabeza;
    private NodoCola fondo;
    private int tamaño;

    public ColaBancaria() {
        cabeza = null;
        fondo  = null;
        tamaño = 0;
    }

    /** Verifica si la cola está vacía. */
    public boolean estaVacia() {
        return cabeza == null;
    }

    /** Agrega un elemento al fondo de la cola. */
    public void insertar(Object x) {
        NodoCola nuevo = new NodoCola();
        nuevo.setInfo(x);
        nuevo.setSiguiente(null);

        if (estaVacia()) {
            cabeza = nuevo;
            fondo  = nuevo;
        } else {
            fondo.setSiguiente(nuevo);
            fondo = nuevo;
        }
        tamaño++;
    }

    /**
     * Extrae y retorna el elemento del frente.
     * Retorna null si la cola está vacía.
     */
    public Object extraer() {
        if (estaVacia()) {
            return null;
        }

        Object informacion = cabeza.getInfo();

        if (cabeza == fondo) {
            // Era el único elemento
            cabeza = null;
            fondo  = null;
        } else {
            cabeza = cabeza.getSiguiente();
        }
        tamaño--;
        return informacion;
    }

    /** Consulta el frente sin extraerlo. Retorna null si vacía. */
    public Object verFrente() {
        return estaVacia() ? null : cabeza.getInfo();
    }

    /** Consulta el fondo sin extraerlo. Retorna null si vacía. */
    public Object verFondo() {
        return estaVacia() ? null : fondo.getInfo();
    }

    /** Retorna la cantidad de elementos en la cola. */
    public int getTamaño() {
        return tamaño;
    }

    /** Remueve un cliente específico de la cola por su ID. */
    public boolean removerPorId(Long id) {
        if (estaVacia()) {
            return false;
        }

        // Si es la cabeza
        Cliente cCabeza = (Cliente) cabeza.getInfo();
        if (cCabeza.getId().equals(id)) {
            extraer();
            return true;
        }

        // Buscar en la lista enlazada
        NodoCola anterior = cabeza;
        NodoCola actual = cabeza.getSiguiente();
        while (actual != null) {
            Cliente c = (Cliente) actual.getInfo();
            if (c.getId().equals(id)) {
                anterior.setSiguiente(actual.getSiguiente());
                if (actual == fondo) {
                    fondo = anterior;
                }
                tamaño--;
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    /** Imprime todos los elementos (para depuración). */
    public void imprimir() {
        NodoCola reco = cabeza;
        System.out.println("=== Cola Bancaria [" + tamaño + " elementos] ===");
        int pos = 1;
        while (reco != null) {
            System.out.println("  " + pos + ". " + reco.getInfo());
            reco = reco.getSiguiente();
            pos++;
        }
        System.out.println("=== Fin de cola ===");
    }
}
