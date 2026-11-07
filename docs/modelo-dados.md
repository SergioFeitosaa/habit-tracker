# Modelo de dados

```text
app_users 1 ── N habits 1 ── N completions
```

| Tabela | Campos | Restrições |
|---|---|---|
| app_users | id, name, email, password_hash | PK; e-mail único; campos obrigatórios; hash BCrypt |
| habits | id, user_id, name, description, archived | PK; FK para usuário; nome obrigatório; índice por usuário |
| completions | id, habit_id, completion_date | PK; FK para hábito; UNIQUE(habit_id, completion_date) |

A exclusão lógica é `habits.archived`; não remove realizações. A propriedade é verificada por consulta do hábito vinculada ao usuário autenticado antes de acessar o histórico. Esquema real é definido em `backend/src/main/resources/db/migration/V1__initial.sql`.
