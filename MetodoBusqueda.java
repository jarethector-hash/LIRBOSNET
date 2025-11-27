package Proyecto_Final;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

/**
 * Helper estático para búsquedas de usuarios y publicaciones.
 *
 * NOTE: To reduce coupling, this helper now works with Lists of users
 * and posts rather than using a RedSocial object directly. This avoids
 * needing the RedSocial class to be present when compiling this helper.
 */
public class MetodoBusqueda {
    public static List<Usuario> buscarUsuarios(List<Usuario> usuarios, String texto) {
        if (usuarios == null) return new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) return new ArrayList<>(usuarios);
        String lower = texto.toLowerCase(Locale.ROOT);
        List<Usuario> encontrados = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u.getNombre() != null && u.getNombre().toLowerCase(Locale.ROOT).contains(lower)) {
                encontrados.add(u);
            }
        }
        return encontrados;
    }

    public static List<Publicacion> buscarPublicaciones(List<Publicacion> publicaciones,
                                                        List<Usuario> usuarios, String texto) {
        List<Publicacion> encontrados = new ArrayList<>();
        if (publicaciones == null) return encontrados;
        if (texto == null || texto.trim().isEmpty()) return new ArrayList<>(publicaciones);

        String lower = texto.toLowerCase(Locale.ROOT);
        Set<Integer> autoresCoincidentes = new HashSet<>();
        if (usuarios != null) {
            for (Usuario u : usuarios) {
                if (u.getNombre() != null && u.getNombre().toLowerCase(Locale.ROOT).contains(lower)) {
                    autoresCoincidentes.add(u.getId());
                }
            }
        }

        for (Publicacion p : publicaciones) {
            if (p.getTexto() != null && p.getTexto().toLowerCase(Locale.ROOT).contains(lower)) {
                encontrados.add(p);
                continue;
            }
            if (autoresCoincidentes.contains(p.getAutorId()) && !encontrados.contains(p)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }
}
