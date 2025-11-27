package Proyecto_Final;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class MetodoDeOrdenamiento {
    public static List<Publicacion> ordenarPorRecientes(List<Publicacion> posts) {
        List<Publicacion> res = new ArrayList<>(posts);
       
        res.sort(Comparator.comparingInt(Publicacion::getId).reversed());
        return res;
    }

    public static List<Publicacion> ordenarPorReacciones(List<Publicacion> posts) {
        List<Publicacion> res = new ArrayList<>(posts);
        res.sort((a, b) -> Integer.compare(totalReacciones(b), totalReacciones(a)));
        return res;
    }

    public static List<Publicacion> ordenarPorComentarios(List<Publicacion> posts) {
        List<Publicacion> res = new ArrayList<>(posts);
        res.sort((a, b) -> Integer.compare(b.getComentarios().size(), a.getComentarios().size()));
        return res;
    }

    public static List<Usuario> ordenarUsuariosPorAmigos(List<Usuario> users) {
        List<Usuario> res = new ArrayList<>(users);
        res.sort((a, b) -> Integer.compare(b.getAmigos().size(), a.getAmigos().size()));
        return res;
    }

    private static int totalReacciones(Publicacion p) {
        return p.getReacciones().values().stream().mapToInt(Integer::intValue).sum();
    }
}
