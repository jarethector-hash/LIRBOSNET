package Proyecto_Final;

import java.util.*;

public class GrafoAmigos {
    private Map<Integer, Set<Integer>> adj;

    public GrafoAmigos() {
        adj = new HashMap<>();
    }

    public void agregarUsuario(int id) {
        adj.putIfAbsent(id, new HashSet<>());
    }

    public void conectar(int id1, int id2) {
        agregarUsuario(id1);
        agregarUsuario(id2);
        adj.get(id1).add(id2);
        adj.get(id2).add(id1);
    }

    public void desconectar(int id1, int id2) {
        if (adj.containsKey(id1)) adj.get(id1).remove(id2);
        if (adj.containsKey(id2)) adj.get(id2).remove(id1);
    }

    public Set<Integer> obtenerAmigos(int id) {
        return adj.getOrDefault(id, Collections.emptySet());
    }

    public boolean sonAmigos(int id1, int id2) {
        return adj.containsKey(id1) && adj.get(id1).contains(id2);
    }


    public Set<Integer> sugerirAmigos(int id) {
        Set<Integer> sugeridos = new HashSet<>();
        if (!adj.containsKey(id)) return sugeridos;

        for (Integer amigo : adj.get(id)) {
            for (Integer amigoDeAmigo : adj.getOrDefault(amigo, Collections.emptySet())) {
                if (amigoDeAmigo != id && !adj.get(id).contains(amigoDeAmigo)) {
                    sugeridos.add(amigoDeAmigo);
                }
            }
        }
        return sugeridos;
    }
}

