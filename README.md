# Habit Tracker

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-green) ![React](https://img.shields.io/badge/React-18-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

Aplicação web para criar hábitos diários e acompanhar realizações. A API usa autenticação JWT e separa dados por usuário. O MVP foi desenvolvido para execução local e não está pronto para produção.

## Funcionalidades
Implementadas no código: cadastro, login, criação/listagem/detalhes/edição/arquivamento de hábitos, registro e remoção de realizações, histórico por período e total de dias. Hábitos arquivados permanecem consultáveis com seu histórico. A sessão fica apenas na memória do navegador e termina ao sair ou recarregar a página. Ao sair, rascunhos e filtros são descartados.

Fora do escopo inicial ou possíveis evoluções: recuperação de senha, restauração de hábitos, documentação OpenAPI, indicadores avançados e endurecimento de segurança. O CI valida backend, frontend e fluxo no navegador; não há implantação automática.

## Tecnologias e pré-requisitos
- Windows 10/11, PowerShell, Git, Java **21** (JDK), Maven **3.9+**, Node.js **22.13 ou superior na linha 22**, ou **24 ou superior** (recomendado: versão LTS) e npm.
- PostgreSQL 16 recomendado (17 foi usado no teste e gera aviso de compatibilidade do Flyway). Serviço em execução na porta 5432.
- Spring Boot 3, Spring Security, JPA, Hibernate, Flyway, PostgreSQL, JWT; React, TypeScript, Vite.
- Acesso à internet para instalar dependências. As instruções abaixo usam Maven instalado no sistema; não há Maven Wrapper.

Verifique em um novo PowerShell: `java -version`, `mvn -v`, `node -v`, `npm -v`, `git --version`. Se `mvn` ou `psql` não for encontrado, acrescente sua pasta `bin` ao PATH e abra um novo terminal.

## 1. PostgreSQL local (caminho principal)

Abra **SQL Shell (psql)** do PostgreSQL, conecte-se como `postgres` e execute, substituindo a senha por uma senha local própria (não compartilhe nem publique):

```sql
CREATE USER habit_user WITH PASSWORD 'COLOQUE_UMA_SENHA_LOCAL_FORTE';
CREATE DATABASE habit_tracker OWNER habit_user;
```

Se usuário ou banco já existirem, não os recrie; ajuste as variáveis abaixo conforme seu ambiente. Em PowerShell, teste a conexão substituindo o valor solicitado pela senha criada:

```powershell
psql -h localhost -U habit_user -d habit_tracker -c 'SELECT 1;'
```

A migration `backend/src/main/resources/db/migration/V1__initial.sql` roda automaticamente **na primeira inicialização do backend** via Flyway. O usuário do banco precisa poder criar tabelas no schema `public`. Não rode o SQL de migration manualmente em paralelo.

### Alternativa: somente PostgreSQL em Docker

Com Docker Desktop em execução, copie `backend/.env.example` para um arquivo local `backend/.env` e substitua `DB_PASSWORD` e `JWT_SECRET`. Depois, na raiz do projeto: `docker compose --env-file backend/.env up -d`. A porta é 5432 e os dados ficam no volume `postgres_data`; não execute esse contêiner na mesma porta de um PostgreSQL local ativo. Os serviços Java e Vite continuam sendo iniciados localmente conforme abaixo. `docker compose down` para o contêiner sem apagar o volume; `docker compose down -v` apaga os dados.

## 2. Configurar e iniciar o backend

Abra o PowerShell **na raiz do projeto**. Variáveis definidas com `$env:` valem apenas para a janela atual. Substitua a senha e gere um segredo próprio antes de executar; não use valores reais no Git.

```powershell
cd backend
$env:DB_URL = 'jdbc:postgresql://localhost:5432/habit_tracker'
$env:DB_USER = 'habit_user'
$env:DB_PASSWORD = Read-Host 'Senha do banco local'
# Gera 32 bytes aleatórios, codificados em Base64, apenas nesta sessão.
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:CORS_ORIGIN = 'http://localhost:5173'
$env:APP_TIMEZONE = 'America/Recife'
mvn spring-boot:run
```

Se `RandomNumberGenerator.Fill` não estiver disponível no Windows PowerShell 5.1, substitua essa linha por `$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create(); $rng.GetBytes($bytes); $rng.Dispose()`. O segredo precisa ter pelo menos 32 bytes UTF-8. Ao reiniciar com outro segredo, tokens anteriores deixam de funcionar. O exemplo `backend/.env.example` documenta variáveis; **Spring Boot não carrega `.env` automaticamente**.

Aguarde `Started HabitTrackerApplication` e deixe o terminal aberto. Erros comuns: porta 5432 ocupada, credenciais incorretas, banco inexistente, Java diferente de 21 ou segredo curto. A API usa porta 8080.

## 3. Iniciar o frontend

Em **outro** PowerShell, na raiz:

```powershell
cd frontend
npm ci
npm run dev
```

Acesse **http://localhost:5173**. O Vite encaminha `/api` para `http://localhost:8080`; `frontend/.env.example` documenta uma alternativa para o destino do proxy, que pode ser copiado para `frontend/.env`. É necessário reiniciar o Vite após alterar esse arquivo. Caso use outra porta ou origem direta, atualize CORS no backend. Não há segredos no frontend.

## 4. Testes

Execute os comandos em terminais separados, a partir da raiz do projeto. Os testes que acessam o PostgreSQL podem criar ou alterar tabelas e dados: **use um banco exclusivo e descartável, nunca produção**.

```powershell
# Backend: em um terminal, a partir da raiz
cd backend
npm ci
npm run format:check
mvn test   # testes unitários
# Configure DB_URL, DB_USER, DB_PASSWORD e JWT_SECRET para um banco de teste descartável.
mvn verify # unitários, integração com PostgreSQL, Checkstyle e empacotamento
```

```powershell
# Frontend: em outro terminal, a partir da raiz
cd frontend
npm ci
npm run lint
npm run format:check
npm run build
```

Para o smoke HTTP, inicie o backend apontando para um banco descartável e, em outro terminal na raiz, execute `python scripts/smoke_test.py`. Ele cria contas e dados de teste; cobre autenticação, isolamento, hábitos, datas futuras, idempotência, histórico e arquivamento.

Para os testes de navegador, inicie o backend apontando para um banco descartável. Em outro terminal, na pasta `frontend`, execute:

```powershell
npm ci
npx playwright install chromium
npm run test:e2e
```

O Playwright inicia seu próprio Vite em `localhost:5173`, que precisa estar livre, e usa o proxy para `localhost:8080` por padrão. Os testes criam contas e hábitos sem removê-los. A configuração do backend deve permitir a origem `http://localhost:5173`. Veja mais detalhes e cuidados com rastros em [qualidade](docs/qualidade.md#executar-o-teste-de-navegador).

A suíte de navegador cobre o fluxo principal com Chromium, verificações de teclado e regras automatizadas de acessibilidade, além de cenários de erro simulados. Ela não substitui testes manuais, nem comprova conformidade de acessibilidade. O [roteiro de validação Windows](docs/validacao-windows.md) permanece pendente de execução registrada.

## Integração contínua (CI)

O workflow `.github/workflows/ci.yml` roda em pushes e pull requests para `main` e `develop`, ou manualmente. O job de backend verifica estilo Java, executa testes unitários e de integração com PostgreSQL 16, empacota a aplicação e roda o smoke HTTP e Playwright/Chromium com o backend e banco isolados. O job de frontend verifica lint, formatação, TypeScript e build. Consulte a aba Actions para o resultado de cada execução.

O CI usa credenciais descartáveis, não faz deploy e não possui execução semanal agendada. Relatórios Java são mantidos por 7 dias; a auditoria Maven gera artefatos por 30 dias. Rastros Playwright não são enviados como artefatos. O CI também consulta advisories das dependências Maven; os avisos não bloqueiam o fluxo nem comprovam explorabilidade. Veja [gestão de dependências](docs/dependencias.md) para detalhes e limitações.

## Segurança e limites
BCrypt, JWT de 8 horas, verificação por dono no backend, validação e CORS configurável. `localStorage` não é usado; o token fica em memória. Senhas, tokens e `.env` não devem ser enviados ao Git. Antes de produção, são necessários HTTPS, limitação de tentativas, revogação/rotação de tokens, políticas de senha, auditoria, tratamento mais completo e revisão dos advisories pendentes do Spring descritos em [gestão de dependências](docs/dependencias.md). Não existe SLA ou benchmark de desempenho; veja `docs/qualidade.md`.

## Documentação e API
- [Índice da documentação](docs/README.md), [guia de contribuição](CONTRIBUTING.md) e [changelog](CHANGELOG.md).
- [Termo de abertura](docs/project-charter.md), [requisitos e regras](docs/requisitos.md), [casos de uso](docs/casos-de-uso.md), [histórias](docs/user-stories.md), [arquitetura](docs/arquitetura.md), [modelo de dados](docs/modelo-dados.md), [rastreabilidade](docs/rastreabilidade.md), [qualidade](docs/qualidade.md), [backlog de Issues](docs/issues.md) e [publicação GitHub](docs/github.md).
- API: `POST /api/auth/register`, `POST /api/auth/login`, `GET/POST /api/habits`, `GET/PUT/DELETE /api/habits/{id}` (DELETE arquiva), `PUT/DELETE /api/habits/{id}/completions/{yyyy-MM-dd}`, `GET /api/habits/{id}/completions?from=yyyy-MM-dd&to=yyyy-MM-dd`. Envie `Authorization: Bearer <token>` nas rotas de hábitos. Para arquivados, `GET /api/habits?archived=true`.

## Git e versões
Use branches de trabalho e pull requests para alterações em `main`. `develop` pode ser usada para integração quando acordado, mas deve ser comparada com `main` antes de um PR entre elas. Prefira commits como `feat:`, `fix:`, `docs:` e `test:` e referencie a issue aplicável. Confira a proteção de branches e os checks no GitHub. Guia completo em [docs/github.md](docs/github.md).

Histórico: **0.1.0 (em desenvolvimento)** — primeira implementação do MVP; ainda não é uma release publicada. SemVer: MAJOR para incompatibilidade, MINOR para função compatível e PATCH para correção compatível.
