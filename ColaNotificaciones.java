package Proyecto_Final;

import java.util.LinkedList;
import java.util.Queue;

public class ColaNotificaciones {
    private Queue<Notificacion> cola;

    public ColaNotificaciones() {
        cola = new LinkedList<>();
    }

    public void push(Notificacion n) {
        cola.offer(n);
    }

    public Notificacion pop() {
        return cola.poll();
    }

    public boolean estaVacia() {
        return cola.isEmpty();
    }

    public void mostrarTodas() {
        if (cola.isEmpty()) {
            System.out.println("No hay notificaciones.");
            return;
        }
        for (Notificacion n : cola) {
            System.out.println(n);
        }
    }

    public java.util.List<Notificacion> toList() {
        return new java.util.ArrayList<>(cola);
    }
}

