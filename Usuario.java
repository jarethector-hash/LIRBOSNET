package Proyecto_Final;

import java.util.HashSet;
import java.util.Set;

public class Usuario {
    private static int contadorIds = 1;
    private final int id;
    private String nombre;
    private String bio;
    private Set<Integer> amigos;

    public Usuario(String nombre, String bio) {
        this.id = contadorIds++;
        this.nombre = nombre;
        this.bio = bio;
        this.amigos = new HashSet<>();
    }

    // Constructor to be used when loading data from persistence with a known id
    Usuario(int id, String nombre, String bio) {
        this.id = id;
        this.nombre = nombre;
        this.bio = bio;
        this.amigos = new HashSet<>();
        if (id >= contadorIds) contadorIds = id + 1;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Set<Integer> getAmigos() { return amigos; }

    public void agregarAmigo(int idAmigo) {
        amigos.add(idAmigo);
    }

    public static int getNextId() { return contadorIds; }
    public static void setNextId(int v) { contadorIds = v; }

    // helper for loading
    public static void setIdForLoad(Usuario u, int newId) {
        // no-op; constructor with id should be used instead
    }

    public void quitarAmigo(int idAmigo) {
        amigos.remove(idAmigo);
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nombre='%s', bio='%s', #amigos=%d}", id, nombre, bio, amigos.size());
    }
}

