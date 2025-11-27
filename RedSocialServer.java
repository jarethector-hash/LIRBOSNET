package Proyecto_Final;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RedSocialServer {
    private final RedSocial rs;
    private final int port;

    public RedSocialServer(int port) {
        this.port = port;
        this.rs = new RedSocial();
        seedDemoData();
    }

    private void seedDemoData() {
        Usuario a = rs.crearUsuario("mole82", "Fotografo amateur");
        Usuario b = rs.crearUsuario("jaret", "Dev");
        rs.agregarAmistad(a.getId(), b.getId());
        rs.crearPublicacion(a.getId(), "Fotito con muy buena compañía ✨", "https://via.placeholder.com/800x1000");
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/posts", new PostsHandler());
        server.createContext("/api/notifications", new NotifHandler());
        server.createContext("/api/friends", new FriendsHandler());
        server.createContext("/api/search", new SearchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:" + port);
    }

    private String bodyToString(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendText(HttpExchange exchange, String txt, int code) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = txt.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    class StaticHandler implements HttpHandler {
        @Override public void handle(HttpExchange e) throws IOException {
            String path = e.getRequestURI().getPath();
            if (path.equals("/")) path = "/redSocial.html";
            Path fpath = Paths.get("src/Proyecto_Final" + path).normalize();
            if (!Files.exists(fpath)) { sendText(e, "Not Found", 404); return; }
            byte[] bytes = Files.readAllBytes(fpath);
            String contentType = path.endsWith(".css") ? "text/css" : "text/html";
            e.getResponseHeaders().add("Content-Type", contentType + "; charset=utf-8");
            e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            e.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = e.getResponseBody()) { os.write(bytes); }
        }
    }

    class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                List<Usuario> users = rs.getAllUsuarios();
                String json = usersToJson(users);
                sendJson(exchange, json);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String body = bodyToString(exchange.getRequestBody());
                Map<String,String> params = parseForm(body);
                String nombre = params.getOrDefault("nombre", "nuevo");
                String bio = params.getOrDefault("bio", "");
                Usuario u = rs.crearUsuario(nombre, bio);
                sendJson(exchange, usuarioToJson(u));
                return;
            }

            sendText(exchange, "Method not allowed", 405);
        }
    }

    class PostsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String[] parts = path.split("/");

            if ("GET".equals(method) && parts.length == 3) { // /api/posts
                Map<String,String> q = parseQuery(uri.getRawQuery());
                List<Publicacion> posts = rs.getAllPublicaciones();
                String sort = q.getOrDefault("sort", "recent");
                if ("reactions".equals(sort)) posts = MetodoDeOrdenamiento.ordenarPorReacciones(posts);
                else if ("comments".equals(sort)) posts = MetodoDeOrdenamiento.ordenarPorComentarios(posts);
                else posts = MetodoDeOrdenamiento.ordenarPorRecientes(posts);
                sendJson(exchange, postsToJson(posts));
                return;
            }

            if ("POST".equals(method) && parts.length == 3) { // create new post
                String body = bodyToString(exchange.getRequestBody());
                Map<String,String> params = parseForm(body);
                int autorId = Integer.parseInt(params.getOrDefault("autorId","1"));
                String texto = params.getOrDefault("texto","(sin texto)");
                String img = params.getOrDefault("imagenPath", null);
                Publicacion p = rs.crearPublicacion(autorId, texto, img);
                sendJson(exchange, publicacionToJson(p));
                return;
            }

            if (parts.length >= 4) {
                int postId = Integer.parseInt(parts[3]);
                if (parts.length == 5 && "react".equals(parts[4]) && "POST".equals(method)) {
                    String body = bodyToString(exchange.getRequestBody());
                    Map<String,String> params = parseForm(body);
                    String tipo = params.getOrDefault("tipo", "like");
                    rs.reaccionar(postId, tipo);
                    sendText(exchange, "OK", 200);
                    return;
                }
                if (parts.length == 5 && "comment".equals(parts[4]) && "POST".equals(method)) {
                    String body = bodyToString(exchange.getRequestBody());
                    Map<String,String> params = parseForm(body);
                    String texto = params.getOrDefault("texto","");
                    rs.comentar(postId, texto);
                    sendText(exchange, "OK", 200);
                    return;
                }
            }

            sendText(exchange, "Not found", 404);
        }
    }

    class NotifHandler implements HttpHandler {
        @Override public void handle(HttpExchange e) throws IOException {
            if ("OPTIONS".equals(e.getRequestMethod())) {
                e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                e.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                e.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                e.sendResponseHeaders(204, -1);
                return;
            }
            String method = e.getRequestMethod();
            if ("GET".equals(method)) {
                String path = e.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[3]);
                    List<Notificacion> nots = rs.getNotificacionesUsuario(id);
                    sendJson(e, notificacionesToJson(nots));
                    return;
                }
            }
            sendText(e, "Not Found", 404);
        }
    }

    class FriendsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length == 4) { // /api/friends/{id}
                    int id = Integer.parseInt(parts[3]);
                    Usuario u = rs.getUsuarioById(id);
                    if (u != null) {
                        List<Integer> amigos = new ArrayList<>(u.getAmigos());
                        sendJson(exchange, listIntToJson(amigos));
                        return;
                    }
                }
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = bodyToString(exchange.getRequestBody());
                Map<String,String> params = parseForm(body);
                int a = Integer.parseInt(params.getOrDefault("userId","0"));
                int b = Integer.parseInt(params.getOrDefault("friendId","0"));
                rs.agregarAmistad(a, b);
                sendText(exchange, "OK", 200);
                return;
            }
            sendText(exchange, "Not Found", 404);
        }
    }

    class SearchHandler implements HttpHandler {
        @Override public void handle(HttpExchange e) throws IOException {
            if ("OPTIONS".equals(e.getRequestMethod())) {
                e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                e.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                e.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                e.sendResponseHeaders(204, -1);
                return;
            }
            Map<String,String> q = parseQuery(e.getRequestURI().getRawQuery());
            String type = q.getOrDefault("type","users");
            String texto = q.getOrDefault("q","");
            if ("users".equals(type)) {
                List<Usuario> res = MetodoBusqueda.buscarUsuarios(rs.getAllUsuarios(), texto);
                sendJson(e, usersToJson(res));
                return;
            }
            if ("posts".equals(type)) {
                List<Publicacion> res = MetodoBusqueda.buscarPublicaciones(rs.getAllPublicaciones(), rs.getAllUsuarios(), texto);
                sendJson(e, postsToJson(res));
                return;
            }
            sendText(e, "Bad Request", 400);
        }
    }

    // helpers
    private Map<String,String> parseQuery(String q) {
        Map<String,String> map = new HashMap<>();
        if (q == null) return map;
        for (String p : q.split("&")) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = decode(p.substring(0, idx));
                String v = decode(p.substring(idx+1));
                map.put(k, v);
            }
        }
        return map;
    }

    private Map<String,String> parseForm(String body) {
        return parseQuery(body.replace("+", "%20"));
    }

    private String decode(String s) {
        try { return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name()); } catch (java.io.UnsupportedEncodingException ex) { return s; }
    }

    private String usersToJson(List<Usuario> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Usuario u : users) {
            if (!first) sb.append(","); first = false;
            sb.append(usuarioToJson(u));
        }
        sb.append("]");
        return sb.toString();
    }
    private String usuarioToJson(Usuario u) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(u.getId()).append(",");
        sb.append("\"nombre\":\"").append(escape(u.getNombre())).append("\",");
        sb.append("\"bio\":\"").append(escape(u.getBio())).append("\",");
        sb.append("\"amigos\":");
        sb.append(listIntToJson(new ArrayList<>(u.getAmigos())));
        sb.append("}");
        return sb.toString();
    }

    private String postsToJson(List<Publicacion> posts) {
        StringBuilder sb = new StringBuilder(); sb.append("[");
        boolean first = true;
        for (Publicacion p : posts) {
            if (!first) sb.append(","); first = false;
            sb.append(publicacionToJson(p));
        }
        sb.append("]"); return sb.toString();
    }

    private String publicacionToJson(Publicacion p) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(p.getId()).append(",");
        sb.append("\"autorId\":").append(p.getAutorId()).append(",");
        sb.append("\"texto\":\"").append(escape(p.getTexto())).append("\",");
        sb.append("\"imagen\":\"").append(escape(p.getImagenPath())).append("\",");
        sb.append("\"reacciones\":{");
        boolean first = true;
        for (Map.Entry<String,Integer> e : p.getReacciones().entrySet()) {
            if (!first) sb.append(","); first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":").append(e.getValue());
        }
        sb.append("},");
        sb.append("\"comentarios\":");
        sb.append(listStrToJson(p.getComentarios()));
        sb.append("}");
        return sb.toString();
    }

    private String notificacionesToJson(List<Notificacion> nots) {
        StringBuilder sb = new StringBuilder(); sb.append("[");
        boolean first=true;
        for (Notificacion n : nots) {
            if (!first) sb.append(","); first=false;
            sb.append("{");
            sb.append("\"mensaje\":\"").append(escape(n.getMensaje())).append("\",");
            sb.append("\"timestamp\":").append(n.getTimestamp());
            sb.append("}");
        }
        sb.append("]"); return sb.toString();
    }

    private String listIntToJson(List<Integer> list) {
        StringBuilder sb = new StringBuilder(); sb.append("[");
        boolean first = true;
        for (Integer i : list) { if (!first) sb.append(","); first=false; sb.append(i); }
        sb.append("]"); return sb.toString();
    }

    private String listStrToJson(List<String> list) {
        StringBuilder sb = new StringBuilder(); sb.append("[");
        boolean first = true;
        for (String s : list) { if (!first) sb.append(","); first=false; sb.append("\"").append(escape(s)).append("\""); }
        sb.append("]"); return sb.toString();
    }

    private String escape(String s) { if (s == null) return ""; return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }

    public static void main(String[] args) throws Exception {
        RedSocialServer server = new RedSocialServer(8000);
        server.start();
    }
}
