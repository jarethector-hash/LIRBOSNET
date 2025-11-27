package Proyecto_Final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Publicacion {
    private static int contador = 1;
    private final int id;
    private final int autorId;
    private String texto;
    private String imagenPath;
    private Map<String, Integer> reacciones;
    private List<String> comentarios;

    public Publicacion(int autorId, String texto, String imagenPath) {
        this.id = contador++;
        this.autorId = autorId;
        this.texto = texto;
        this.imagenPath = imagenPath;
        this.reacciones = new HashMap<>();
        this.comentarios = new ArrayList<>();
    }

    // Constructor to use when loading from persistence
    Publicacion(int id, int autorId, String texto, String imagenPath) {
        this.id = id;
        this.autorId = autorId;
        this.texto = texto;
        this.imagenPath = imagenPath;
        this.reacciones = new HashMap<>();
        this.comentarios = new ArrayList<>();
        if (id >= contador) contador = id + 1;
    }

    public int getId() { return id; }
    public int getAutorId() { return autorId; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getImagenPath() { return imagenPath; }
    public void setImagenPath(String imagenPath) { this.imagenPath = imagenPath; }

    public void reaccionar(String tipo) {
        reacciones.put(tipo, reacciones.getOrDefault(tipo, 0) + 1);
    }

    public void comentar(String comentario) {
        comentarios.add(comentario);
    }

    public Map<String, Integer> getReacciones() { return reacciones; }
    public List<String> getComentarios() { return comentarios; }

    public static int getNextId() { return contador; }
    public static void setNextId(int v) { contador = v; }

    public static void setIdForLoad(Publicacion p, int id) {
        // no-op; use constructor with id
    }

    @Override
    public String toString() {
        return String.format("Post{id=%d, autor=%d, texto='%s', img=%s, reacciones=%s, comentarios=%d}",
                id, autorId, texto, (imagenPath==null?"-":imagenPath), reacciones.toString(), comentarios.size());
    }
}

