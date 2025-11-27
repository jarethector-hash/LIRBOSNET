package Proyecto_Final;

public class ListaDoblePublicaciones {
    NodoPublicacion head;
    private NodoPublicacion tail;
    private int size;

    public ListaDoblePublicaciones() {
        head = tail = null;
        size = 0;
    }


    public void insertarFrente(Publicacion p) {
        NodoPublicacion nodo = new NodoPublicacion(p);
        if (head == null) {
            head = tail = nodo;
        } else {
            nodo.next = head;
            head.prev = nodo;
            head = nodo;
        }
        size++;
    }


    public Publicacion eliminarPorId(int id) {
        NodoPublicacion actual = head;
        while (actual != null) {
            if (actual.data.getId() == id) {
                if (actual.prev != null) actual.prev.next = actual.next;
                else head = actual.next;

                if (actual.next != null) actual.next.prev = actual.prev;
                else tail = actual.prev;

                size--;
                return actual.data;
            }
            actual = actual.next;
        }
        return null;
    }


    public Publicacion buscarPorId(int id) {
        NodoPublicacion actual = head;
        while (actual != null) {
            if (actual.data.getId() == id) return actual.data;
            actual = actual.next;
        }
        return null;
    }


    public void mostrarFeed() {
        NodoPublicacion actual = head;
        while (actual != null) {
            System.out.println(actual.data);
            actual = actual.next;
        }
    }

    public int size() { return size; }
}

