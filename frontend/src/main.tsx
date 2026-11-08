import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./style.css";
type Habit = {
  id: number;
  name: string;
  description: string | null;
  archived: boolean;
};
type History = { dates: string[]; total: number };
const today = () =>
  new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Recife",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date());
async function api<T>(
  path: string,
  token: string,
  method = "GET",
  body?: object,
): Promise<T> {
  const r = await fetch("/api" + path, {
    method,
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!r.ok) {
    let msg = `Erro ${r.status}. Tente novamente.`;
    try {
      const x = await r.json();
      if (x.message && x.message !== "Unauthorized") msg = x.message;
    } catch {
      // Use the generic error message when the response is not JSON.
    }
    if (r.status === 401) msg = "Sessão expirada ou credenciais inválidas.";
    throw new Error(msg);
  }
  const text = await r.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
function App({ onLogout }: { onLogout: () => void }) {
  const [token, setToken] = useState(""),
    [person, setPerson] = useState(""),
    [register, setRegister] = useState(false),
    [name, setName] = useState(""),
    [email, setEmail] = useState(""),
    [password, setPassword] = useState("");
  const [showArchived, setShowArchived] = useState(false);
  const [habits, setHabits] = useState<Habit[]>([]),
    [title, setTitle] = useState(""),
    [description, setDescription] = useState(""),
    [editing, setEditing] = useState<number | null>(null),
    [selected, setSelected] = useState<Habit | null>(null),
    [date, setDate] = useState(today()),
    [from, setFrom] = useState(today().slice(0, 8) + "01"),
    [to, setTo] = useState(today()),
    [history, setHistory] = useState<History | null>(null),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false);
  async function run(fn: () => Promise<void>) {
    setMessage("");
    setBusy(true);
    try {
      await fn();
    } catch (e) {
      setMessage(e instanceof Error ? e.message : "Ocorreu um erro.");
    } finally {
      setBusy(false);
    }
  }
  async function loadHabits(t = token) {
    setHabits(await api<Habit[]>(`/habits?archived=${showArchived}`, t));
  }
  async function loadHistory(h: Habit) {
    setHistory(
      await api<History>(
        `/habits/${h.id}/completions?from=${from}&to=${to}`,
        token,
      ),
    );
  }
  useEffect(() => {
    if (token) void run(() => loadHabits(token));
  }, [token, showArchived]);
  function authenticate(e: React.FormEvent) {
    e.preventDefault();
    void run(async () => {
      if (register) {
        await api("/auth/register", "", "POST", { name, email, password });
        setRegister(false);
        setMessage("Conta criada! Entre com seu e-mail e senha.");
        setPassword("");
      } else {
        const x = await api<{ token: string; name: string }>(
          "/auth/login",
          "",
          "POST",
          { email, password },
        );
        setToken(x.token);
        setPerson(x.name);
        setPassword("");
      }
    });
  }
  function save(e: React.FormEvent) {
    e.preventDefault();
    void run(async () => {
      await api(
        editing ? `/habits/${editing}` : "/habits",
        token,
        editing ? "PUT" : "POST",
        { name: title, description: description || null },
      );
      setTitle("");
      setDescription("");
      setEditing(null);
      await loadHabits();
    });
  }
  function select(h: Habit) {
    setSelected(h);
    setHistory(null);
    void run(() => loadHistory(h));
  }
  function mark(on: boolean) {
    if (!selected) return;
    void run(async () => {
      await api(
        `/habits/${selected.id}/completions/${date}`,
        token,
        on ? "PUT" : "DELETE",
      );
      await loadHistory(selected);
      setMessage(on ? "Realização registrada." : "Realização removida.");
    });
  }
  return (
    <>
      <header>
        <div className="top">
          <a className="brand" href="/">
            ◒ <span>habit tracker</span>
          </a>
          {token && (
            <div className="account">
              <span>Olá, {person}</span>
              <button className="ghost" onClick={onLogout}>
                Sair
              </button>
            </div>
          )}
        </div>
      </header>
      <main>
        {!token ? (
          <div className="auth-layout">
            <section className="intro">
              <p className="eyebrow">UM DIA DE CADA VEZ</p>
              <h1>
                Pequenos passos,
                <br />
                <em>grandes mudanças.</em>
              </h1>
              <p>
                Crie hábitos que importam para você e acompanhe seu progresso,
                no seu ritmo.
              </p>
              <div className="quote">
                ✦ &nbsp; Consistência é melhor que perfeição.
              </div>
            </section>
            <section className="card auth">
              <p className="eyebrow">BEM-VINDO(A)</p>
              <h2>{register ? "Crie sua conta" : "Acesse sua conta"}</h2>
              <p className="muted">
                {register
                  ? "Comece sua jornada hoje."
                  : "Continue construindo seus hábitos."}
              </p>
              <form onSubmit={authenticate}>
                {register && (
                  <label>
                    Nome
                    <input
                      required
                      maxLength={100}
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      autoComplete="name"
                    />
                  </label>
                )}
                <label>
                  E-mail
                  <input
                    required
                    type="email"
                    maxLength={254}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                  />
                </label>
                <label>
                  Senha
                  <input
                    required
                    type="password"
                    minLength={register ? 8 : 1}
                    maxLength={72}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete={
                      register ? "new-password" : "current-password"
                    }
                  />
                  {register && (
                    <small>Pelo menos 8 caracteres, máximo 72 bytes.</small>
                  )}
                </label>
                <button className="primary wide" disabled={busy}>
                  {register ? "Criar conta" : "Entrar"} <span>→</span>
                </button>
              </form>
              <p className="switch">
                {register ? "Já tem uma conta?" : "Ainda não tem conta?"}{" "}
                <button
                  className="link"
                  onClick={() => {
                    setRegister(!register);
                    setMessage("");
                  }}
                >
                  {register ? "Entrar" : "Cadastre-se"}
                </button>
              </p>
            </section>
          </div>
        ) : (
          <>
            <div className="heading">
              <div>
                <p className="eyebrow">SEU ESPAÇO DE CRESCIMENTO</p>
                <h1>
                  Seus hábitos<span className="dot">.</span>
                </h1>
                <p className="muted">Um pequeno compromisso de cada vez.</p>
              </div>
              <div className="date-pill">
                ◷ &nbsp;{" "}
                {new Intl.DateTimeFormat("pt-BR", {
                  dateStyle: "full",
                  timeZone: "America/Recife",
                }).format(new Date())}
              </div>
            </div>
            <div className="dashboard">
              <div>
                <section className="card">
                  <div className="section-head">
                    <div>
                      <p className="eyebrow">COMECE AQUI</p>
                      <h2>{editing ? "Editar hábito" : "Novo hábito"}</h2>
                    </div>
                    <span className="icon">✳</span>
                  </div>
                  <form onSubmit={save}>
                    <label>
                      Nome do hábito
                      <input
                        required
                        maxLength={100}
                        placeholder="Ex.: Ler por 15 minutos"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                      />
                    </label>
                    <label>
                      Descrição <span className="optional">opcional</span>
                      <textarea
                        maxLength={500}
                        placeholder="Por que esse hábito importa para você?"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                      />
                    </label>
                    <div className="row">
                      <button className="primary" disabled={busy}>
                        {editing ? "Salvar alterações" : "Adicionar hábito"}{" "}
                        &nbsp; +
                      </button>
                      {editing && (
                        <button
                          type="button"
                          className="secondary"
                          onClick={() => {
                            setEditing(null);
                            setTitle("");
                            setDescription("");
                          }}
                        >
                          Cancelar
                        </button>
                      )}
                    </div>
                  </form>
                </section>
                <section className="habits-section">
                  <div className="section-head">
                    <div>
                      <p className="eyebrow">SUA ROTINA</p>
                      <h2>
                        {showArchived ? "Hábitos arquivados" : "Hábitos ativos"}
                      </h2>
                      <button
                        className="link"
                        onClick={() => {
                          setShowArchived(!showArchived);
                          setSelected(null);
                          setHistory(null);
                        }}
                      >
                        {showArchived ? "Ver ativos" : "Ver arquivados"}
                      </button>
                    </div>
                    <span className="count">{habits.length}</span>
                  </div>
                  {habits.length === 0 ? (
                    <div className="empty">
                      ✧<p>Nenhum hábito ainda.</p>
                      <small>Adicione seu primeiro hábito acima.</small>
                    </div>
                  ) : (
                    <div className="habit-list">
                      {habits.map((h) => (
                        <article
                          className={
                            "habit " + (selected?.id === h.id ? "selected" : "")
                          }
                          key={h.id}
                        >
                          <button
                            className="habit-select"
                            onClick={() => select(h)}
                          >
                            <span className="habit-symbol" aria-hidden="true" />
                            <span>
                              <span className="sr-only">Consultar </span>
                              <strong>{h.name}</strong>
                              <small>{h.description || "Hábito diário"}</small>
                            </span>
                          </button>
                          {!showArchived && (
                            <div className="habit-actions">
                              <button
                                className="link"
                                onClick={() => {
                                  setEditing(h.id);
                                  setTitle(h.name);
                                  setDescription(h.description || "");
                                  window.scrollTo({
                                    top: 0,
                                    behavior: "smooth",
                                  });
                                }}
                              >
                                Editar
                              </button>
                              <button
                                className="link danger"
                                onClick={() => {
                                  if (
                                    window.confirm(
                                      `Arquivar “${h.name}”? O histórico será preservado.`,
                                    )
                                  )
                                    void run(async () => {
                                      await api(
                                        `/habits/${h.id}`,
                                        token,
                                        "DELETE",
                                      );
                                      if (selected?.id === h.id) {
                                        setSelected(null);
                                        setHistory(null);
                                      }
                                      await loadHabits();
                                    });
                                }}
                              >
                                Arquivar
                              </button>
                            </div>
                          )}
                        </article>
                      ))}
                    </div>
                  )}
                </section>
              </div>
              <aside className="card progress">
                <p className="eyebrow">ACOMPANHAMENTO</p>
                <h2>{selected ? selected.name : "Seu progresso"}</h2>
                {selected ? (
                  <>
                    <p className="muted">
                      {selected.archived
                        ? "Hábito arquivado: histórico disponível para consulta."
                        : "Registre uma realização por dia."}
                    </p>
                    {!selected.archived && (
                      <>
                        <label>
                          Data da realização
                          <input
                            type="date"
                            value={date}
                            max={today()}
                            onChange={(e) => setDate(e.target.value)}
                          />
                        </label>
                        <div className="row">
                          <button
                            disabled={busy || !date}
                            className="primary"
                            onClick={() => mark(true)}
                          >
                            Marcar ✓
                          </button>
                          <button
                            disabled={busy || !date}
                            className="secondary"
                            onClick={() => mark(false)}
                          >
                            Desmarcar
                          </button>
                        </div>
                      </>
                    )}
                    <hr />
                    <h3>Histórico</h3>
                    <div className="dates">
                      <label>
                        De
                        <input
                          type="date"
                          value={from}
                          onChange={(e) => setFrom(e.target.value)}
                        />
                      </label>
                      <label>
                        Até
                        <input
                          type="date"
                          value={to}
                          onChange={(e) => setTo(e.target.value)}
                        />
                      </label>
                    </div>
                    <button
                      className="secondary wide"
                      disabled={busy || !from || !to}
                      onClick={() => void run(() => loadHistory(selected))}
                    >
                      Consultar período
                    </button>
                    {history && (
                      <div className="history">
                        <div className="total">
                          <strong>{history.total}</strong>{" "}
                          {history.total === 1
                            ? "dia concluído"
                            : "dias concluídos"}{" "}
                          no período
                        </div>
                        {history.dates.length > 0 && (
                          <ul>
                            {history.dates.map((d) => (
                              <li key={d}>
                                ✓ &nbsp; {d.split("-").reverse().join("/")}
                              </li>
                            ))}
                          </ul>
                        )}
                      </div>
                    )}
                  </>
                ) : (
                  <div className="progress-empty">
                    <span>◉</span>
                    <p>
                      Selecione um hábito para registrar realizações e
                      acompanhar seu progresso.
                    </p>
                  </div>
                )}
              </aside>
            </div>
          </>
        )}
        {message && (
          <div className="notice" role="status" aria-live="polite">
            {message}
          </div>
        )}
      </main>
      <footer>
        HABIT TRACKER <span>✦</span> UM DIA DE CADA VEZ
      </footer>
    </>
  );
}
function Root() {
  const [session, setSession] = useState(0);
  return (
    <App
      key={session}
      onLogout={() => setSession((previous) => previous + 1)}
    />
  );
}

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
);
