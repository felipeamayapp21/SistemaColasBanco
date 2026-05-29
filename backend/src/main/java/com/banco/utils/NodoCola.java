package com.banco.utils;

/**
 * NodoCola — Nodo de la lista enlazada para la cola de turnos.
 * Basado en la estructura genérica del proyecto.
 */
public class NodoCola {

    private Object info;
    private NodoCola siguiente;

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public NodoCola getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoCola siguiente) {
        this.siguiente = siguiente;
    }
}
