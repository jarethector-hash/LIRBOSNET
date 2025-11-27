package Proyecto_Final;

public class Notificacion {
    private final String mensaje;
    private final long timestamp;

    public Notificacion(String mensaje) {
        this.mensaje = mensaje;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor para cargar notificaciones con timestamp preexistente
    public Notificacion(String mensaje, long timestamp) {
        this.mensaje = mensaje;
        this.timestamp = timestamp;
    }

    public String getMensaje() { return mensaje; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s", new java.util.Date(timestamp).toString(), mensaje);
    }
}

