package Proyecto_Final;

public class NodoPublicacion {
    public Publicacion data;
    public NodoPublicacion prev;
    public NodoPublicacion next;

    public NodoPublicacion(Publicacion p) {
        this.data = p;
        this.prev = null;
        this.next = null;
    }
}

