import React, { useState, useEffect, useRef } from "react";

// ReaderSocialApp.jsx
// Single-file React component (Tailwind CSS assumed available)
// Simula la red social de lectores: usuarios, publicaciones, reacciones, comentarios,
// barra de búsqueda, notificaciones y botón deshacer (pila de acciones).

export default function ReaderSocialApp() {
  // --- Datos iniciales / estados ---
  const [usuarios, setUsuarios] = useState(() => {
    const saved = localStorage.getItem("rs_usuarios");
    return saved
      ? JSON.parse(saved)
      : [
          { id: 1, nombre: "Ana Lectora", bio: "Amo novelas históricas", amigos: [2, 3] },
          { id: 2, nombre: "Luis Lector", bio: "Fan de la ciencia ficción", friends: [], amigos: [1] },
          { id: 3, nombre: "Marta Bookworm", bio: "Reseñadora amateur", amigos: [1] },
        ];
  });

  const [posts, setPosts] = useState(() => {
    const saved = localStorage.getItem("rs_posts");
    return saved
      ? JSON.parse(saved)
      : [
          { id: 1, autorId: 1, texto: "Releyendo 'Cien años de soledad'. Maravilloso.", img: null, reacciones: {}, comentarios: [] },
          { id: 2, autorId: 2, texto: "Acabo de terminar Dune, recomendable.", img: null, reacciones: {}, comentarios: [] },
        ];
  });

  const [notifs, setNotifs] = useState(() => {
    const saved = localStorage.getItem("rs_notifs");
    return saved ? JSON.parse(saved) : { 1: [], 2: [], 3: [] };
  });

  const [stack, setStack] = useState(() => {
    const saved = localStorage.getItem("rs_stack");
    return saved ? JSON.parse(saved) : [];
  });

  const [currentUserId, setCurrentUserId] = useState(1);
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("all");

  // Form refs
  const nameRef = useRef();
  const bioRef = useRef();
  const postTextRef = useRef();
  const postImgRef = useRef();

  // --- Persistir cambios en localStorage ---
  useEffect(() => localStorage.setItem("rs_usuarios", JSON.stringify(usuarios)), [usuarios]);
  useEffect(() => localStorage.setItem("rs_posts", JSON.stringify(posts)), [posts]);
  useEffect(() => localStorage.setItem("rs_notifs", JSON.stringify(notifs)), [notifs]);
  useEffect(() => localStorage.setItem("rs_stack", JSON.stringify(stack)), [stack]);

  // --- Helpers ---
  const nextId = (arr) => (arr.length ? Math.max(...arr.map((x) => x.id)) + 1 : 1);
  const currentUser = usuarios.find((u) => u.id === currentUserId) || usuarios[0];

  // Añadir usuario
  function registrarUsuario() {
    const nombre = nameRef.current.value.trim();
    const bio = bioRef.current.value.trim();
    if (!nombre) return alert("Escribe un nombre");
    const nuevo = { id: nextId(usuarios), nombre, bio, amigos: [] };
    setUsuarios((s) => [...s, nuevo]);
    setNotifs((n) => ({ ...n, [nuevo.id]: [] }));
    nameRef.current.value = "";
    bioRef.current.value = "";
    // push action
    pushAction({ tipo: "registrar", detalle: nuevo });
  }

  // Crear publicación
  function crearPublicacion() {
    const texto = postTextRef.current.value.trim();
    const img = postImgRef.current.value.trim() || null;
    if (!texto) return alert("Escribe el texto de la publicación");
    const p = { id: nextId(posts), autorId: currentUserId, texto, img, reacciones: {}, comentarios: [] };
    setPosts((s) => [p, ...s]);
    // notificar amigos
    const amigos = currentUser?.amigos || [];
    const newNotifs = { ...notifs };
    amigos.forEach((a) => {
      if (!newNotifs[a]) newNotifs[a] = [];
      newNotifs[a].unshift({ mensaje: `${currentUser.nombre} publicó: ${texto}`, ts: Date.now() });
    });
    setNotifs(newNotifs);
    postTextRef.current.value = "";
    postImgRef.current.value = "";
    pushAction({ tipo: "publicar", detalle: p });
  }

  // Reaccionar
  function reaccionar(postId, tipo) {
    setPosts((ps) =>
      ps.map((p) => {
        if (p.id !== postId) return p;
        const r = { ...p.reacciones };
        r[tipo] = (r[tipo] || 0) + 1;
        // notificar autor
        setNotifs((n) => ({ ...n, [p.autorId]: [{ mensaje: `${currentUser.nombre} reaccionó '${tipo}' a tu publicación`, ts: Date.now() }, ...(n[p.autorId] || [])] }));
        return { ...p, reacciones: r };
      })
    );
    pushAction({ tipo: "reaccion", detalle: { usuarioId: currentUserId, postId, tipo } });
  }

  // Comentar
  function comentar(postId, texto) {
    if (!texto) return;
    setPosts((ps) =>
      ps.map((p) => {
        if (p.id !== postId) return p;
        const c = [...p.comentarios, { autorId: currentUserId, texto, ts: Date.now() }];
        setNotifs((n) => ({ ...n, [p.autorId]: [{ mensaje: `${currentUser.nombre} comentó tu publicación: ${texto}`, ts: Date.now() }, ...(n[p.autorId] || [])] }));
        return { ...p, comentarios: c };
      })
    );
    pushAction({ tipo: "comentar", detalle: { postId, texto } });
  }

  // Agregar amigo (simula aceptar solicitud)
  function agregarAmigo(idAmigo) {
    if (currentUserId === idAmigo) return;
    setUsuarios((us) =>
      us.map((u) => {
        if (u.id === currentUserId) return { ...u, amigos: Array.from(new Set([...(u.amigos || []), idAmigo])) };
        if (u.id === idAmigo) return { ...u, amigos: Array.from(new Set([...(u.amigos || []), currentUserId])) };
        return u;
      })
    );
    setNotifs((n) => ({ ...n, [idAmigo]: [{ mensaje: `${currentUser.nombre} te agregó como amigo`, ts: Date.now() }, ...(n[idAmigo] || [])] }));
    pushAction({ tipo: "agregar_amigo", detalle: { a: currentUserId, b: idAmigo } });
  }

  // Eliminar publicacion
  function eliminarPublicacion(id) {
    const eliminado = posts.find((p) => p.id === id);
    if (!eliminado) return;
    setPosts((ps) => ps.filter((p) => p.id !== id));
    pushAction({ tipo: "eliminar_post", detalle: eliminado });
  }

  // Notifs mostrar y limpiar
  function verNotifs(uid) {
    setNotifs((n) => ({ ...n, [uid]: [] }));
  }

  // Pila de acciones: push y deshacer
  function pushAction(action) {
    setStack((s) => [action, ...s]);
  }

  function deshacer() {
    const [top, ...rest] = stack;
    if (!top) return alert("No hay acciones para deshacer");
    // manejar tipos
    if (top.tipo === "publicar") {
      // eliminar el post que fue creado
      const pid = top.detalle.id;
      setPosts((ps) => ps.filter((p) => p.id !== pid));
      alert("Deshecho: publicación eliminada");
    } else if (top.tipo === "eliminar_post") {
      // restaurar
      setPosts((ps) => [top.detalle, ...ps]);
      alert("Deshecho: publicación restaurada");
    } else if (top.tipo === "reaccion") {
      const { postId, tipo } = top.detalle;
      setPosts((ps) =>
        ps.map((p) => {
          if (p.id !== postId) return p;
          const r = { ...p.reacciones };
          r[tipo] = Math.max(0, (r[tipo] || 0) - 1);
          return { ...p, reacciones: r };
        })
      );
      alert("Deshecho: reacción removida");
    } else if (top.tipo === "comentar") {
      const { postId, texto } = top.detalle;
      setPosts((ps) =>
        ps.map((p) => {
          if (p.id !== postId) return p;
          const c = p.comentarios.filter((cm) => cm.texto !== texto);
          return { ...p, comentarios: c };
        })
      );
      alert("Deshecho: comentario removido");
    } else if (top.tipo === "agregar_amigo") {
      const { a, b } = top.detalle;
      setUsuarios((us) => us.map((u) => (u.id === a ? { ...u, amigos: (u.amigos || []).filter((x) => x !== b) } : u.id === b ? { ...u, amigos: (u.amigos || []).filter((x) => x !== a) } : u)));
      alert("Deshecho: amistad removida");
    } else if (top.tipo === "registrar") {
      // remover usuario
      const uid = top.detalle.id;
      setUsuarios((us) => us.filter((u) => u.id !== uid));
      alert("Deshecho: usuario eliminado");
    }
    setStack(rest);
  }

  // Buscador simple: usuarios y posts
  const usuariosFiltrados = usuarios.filter((u) => u.nombre.toLowerCase().includes(query.toLowerCase()));
  const postsFiltrados = posts.filter((p) => p.texto.toLowerCase().includes(query.toLowerCase()));

  // UI ---
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-sky-50 py-8 px-4">
      <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Perfil y acciones */}
        <aside className="space-y-4">
          <div className="bg-white p-4 rounded-2xl shadow">
            <div className="flex items-center gap-3">
              <div className="h-16 w-16 rounded-full bg-gradient-to-tr from-rose-400 to-indigo-500 flex items-center justify-center text-white font-bold">{currentUser.nombre?.split(" ")[0][0]}</div>
              <div>
                <h3 className="font-semibold">{currentUser.nombre}</h3>
                <p className="text-sm text-slate-500">{currentUser.bio}</p>
              </div>
            </div>
            <div className="mt-4 flex gap-2">
              <select value={currentUserId} onChange={(e) => setCurrentUserId(Number(e.target.value))} className="flex-1 border rounded px-2 py-1">
                {usuarios.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.nombre}
                  </option>
                ))}
              </select>
              <button onClick={deshacer} className="px-3 py-1 bg-slate-800 text-white rounded">Deshacer</button>
            </div>
          </div>

          <div className="bg-white p-4 rounded-2xl shadow">
            <h4 className="font-semibold mb-2">Crear usuario</h4>
            <input ref={nameRef} placeholder="Nombre" className="w-full border rounded px-2 py-1 mb-2" />
            <input ref={bioRef} placeholder="Bio" className="w-full border rounded px-2 py-1 mb-2" />
            <button onClick={registrarUsuario} className="w-full py-2 bg-emerald-500 text-white rounded">Registrar</button>
          </div>

          <div className="bg-white p-4 rounded-2xl shadow">
            <h4 className="font-semibold mb-2">Notificaciones</h4>
            <div className="space-y-2 max-h-48 overflow-auto">
              {(notifs[currentUserId] || []).length === 0 ? (
                <p className="text-sm text-slate-500">No hay notificaciones</p>
              ) : (
                (notifs[currentUserId] || []).map((n, idx) => (
                  <div key={idx} className="text-sm p-2 rounded bg-slate-50 border">
                    <div className="text-xs text-slate-400">{new Date(n.ts).toLocaleString()}</div>
                    <div>{n.mensaje}</div>
                  </div>
                ))
              )}
            </div>
            <div className="mt-3 flex gap-2">
              <button onClick={() => verNotifs(currentUserId)} className="flex-1 py-2 bg-blue-500 text-white rounded">Marcar leídas</button>
              <button
                onClick={() => setNotifs((n) => ({ ...n, [currentUserId]: [] }))}
                className="py-2 px-3 border rounded">
                Limpiar
              </button>
            </div>
          </div>
        </aside>

        {/* Center: Feed */}
        <main className="lg:col-span-2 space-y-6">
          <div className="bg-white p-4 rounded-2xl shadow">
            <div className="flex gap-2 items-center">
              <input placeholder="Buscar usuarios o publicaciones..." value={query} onChange={(e) => setQuery(e.target.value)} className="flex-1 border rounded px-3 py-2" />
              <select value={filter} onChange={(e) => setFilter(e.target.value)} className="border rounded px-2 py-1">
                <option value="all">Todo</option>
                <option value="amigos">Solo amigos</option>
                <option value="mios">Mis publicaciones</option>
              </select>
            </div>

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="col-span-1">
                <h5 className="font-semibold">Crear publicación</h5>
                <textarea ref={postTextRef} placeholder="¿Qué estás leyendo?" className="w-full border rounded p-2 mt-2" />
                <input ref={postImgRef} placeholder="URL imagen (opcional)" className="w-full border rounded px-2 py-1 mt-2" />
                <div className="mt-2 flex gap-2">
                  <button onClick={crearPublicacion} className="py-2 px-4 bg-indigo-600 text-white rounded">Publicar</button>
                  <button
                    onClick={() => {
                      postTextRef.current.value = "";
                      postImgRef.current.value = "";
                    }}
                    className="py-2 px-3 border rounded">
                    Limpiar
                  </button>
                </div>
              </div>

              <div className="col-span-1">
                <h5 className="font-semibold">Usuarios</h5>
                <div className="mt-2 max-h-44 overflow-auto space-y-2">
                  {usuariosFiltrados.map((u) => (
                    <div key={u.id} className="flex items-center justify-between p-2 border rounded">
                      <div>
                        <div className="font-medium">{u.nombre}</div>
                        <div className="text-xs text-slate-500">{u.bio}</div>
                      </div>
                      <div className="flex gap-2">
                        {currentUser?.amigos?.includes(u.id) ? (
                          <span className="text-sm text-emerald-600">Amigo</span>
                        ) : (
                          <button onClick={() => agregarAmigo(u.id)} className="text-sm px-2 py-1 border rounded">Agregar</button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Feed */}
          <div className="space-y-4">
            {(filter === "mios" ? posts.filter((p) => p.autorId === currentUserId) : filter === "amigos" ? posts.filter((p) => currentUser.amigos?.includes(p.autorId)) : posts)
              .filter((p) => p.texto.toLowerCase().includes(query.toLowerCase()))
              .map((p) => (
                <article key={p.id} className="bg-white p-4 rounded-2xl shadow">
                  <header className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-slate-200 flex items-center justify-center font-bold">{(usuarios.find((u) => u.id === p.autorId)?.nombre || "U")[0]}</div>
                      <div>
                        <div className="font-semibold">{usuarios.find((u) => u.id === p.autorId)?.nombre}</div>
                        <div className="text-xs text-slate-400">{new Date().toLocaleDateString()}</div>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {p.autorId === currentUserId && (
                        <button onClick={() => eliminarPublicacion(p.id)} className="text-sm text-rose-500">Eliminar</button>
                      )}
                    </div>
                  </header>

                  <div className="mt-3">
                    <p className="whitespace-pre-line">{p.texto}</p>
                    {p.img && <img src={p.img} alt="adjunta" className="mt-3 rounded max-h-60 object-cover w-full" />}
                  </div>

                  <div className="mt-3 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <button onClick={() => reaccionar(p.id, "like")} className="text-sm px-2 py-1 border rounded">👍 {p.reacciones.like || 0}</button>
                      <button onClick={() => reaccionar(p.id, "love")} className="text-sm px-2 py-1 border rounded">❤️ {p.reacciones.love || 0}</button>
                      <CommentBox onSubmit={(txt) => comentar(p.id, txt)} />
                    </div>
                    <div className="text-sm text-slate-400">{p.comentarios.length} comentarios</div>
                  </div>

                  {p.comentarios.length > 0 && (
                    <div className="mt-3 space-y-2">
                      {p.comentarios.map((c, i) => (
                        <div key={i} className="p-2 bg-slate-50 rounded">
                          <div className="text-xs text-slate-500">{usuarios.find((u) => u.id === c.autorId)?.nombre}</div>
                          <div className="text-sm">{c.texto}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </article>
              ))}
          </div>
        </main>
      </div>
    </div>
  );
}

// Small comment box component
function CommentBox({ onSubmit }) {
  const [open, setOpen] = useState(false);
  const [text, setText] = useState("");
  return (
    <div>
      {!open ? (
        <button onClick={() => setOpen(true)} className="text-sm px-2 py-1 border rounded">Comentar</button>
      ) : (
        <div className="flex gap-2">
          <input value={text} onChange={(e) => setText(e.target.value)} className="border rounded px-2 py-1" placeholder="Escribe..." />
          <button
            onClick={() => {
              onSubmit(text);
              setText("");
              setOpen(false);
            }}
            className="px-2 py-1 bg-indigo-600 text-white rounded">
            Enviar
          </button>
        </div>
      )}
    </div>
  );
}
