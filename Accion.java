package Proyecto_Final;

public class Accion {
    public enum Tipo { PUBLICAR, ELIMINAR_PUBLICACION, REACCIONAR, COMENTAR, AGREGAR_AMIGO }

    private Tipo tipo;
    private Object detalle;

    public Accion(Tipo tipo, Object detalle) {
        this.tipo = tipo;
        this.detalle = detalle;
    }

    public Tipo getTipo() { return tipo; }
    public Object getDetalle() { return detalle; }
}

