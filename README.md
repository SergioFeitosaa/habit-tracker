# Habit Tracker

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.13-green) ![React](https://img.shields.io/badge/React-18-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

MVP acadêmico para criar hábitos diários e registrar realizações. A API usa autenticação JWT e separa dados por usuário. Este é um projeto de estudo para execução local, não um serviço pronto para produção.

## Funcionalidades
Implementadas no código: cadastro, login, criação/listagem/detalhes/edição/arquivamento de hábitos, registro e remoção de realizações, histórico por período e total de dias. Hábitos arquivados permanecem consultáveis com seu histórico. Sessão fica apenas na memória do navegador e termina ao recarregar a página.

Planejadas: recuperação de senha, restauração de hábitos, testes de integração adicionais, documentação OpenAPI, indicadores avançados e endurecimento de segurança. O CI valida backend e frontend no GitHub Actions; não há implantação automática.

## Tecnologias e pré-requisitos
- Windows 10/11, PowerShell, Git, Java **21** (JDK), Maven **3.9+**, Node.js **22 LTS** e npm.
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

```powershell
# Em backend; os testes unitários não exigem banco
mvn test
# Em frontend
npm run build
# Opcional: com Python 3 instalado e backend rodando; cria duas contas de teste no banco de desenvolvimento
cd ..
python scripts/smoke_test.py
```

O smoke test usa HTTP real, grava dados no banco e cobre cadastro/login, acesso não autorizado, isolamento, hábitos, datas futuras, idempotência, histórico e arquivamento. Não o execute em banco de produção. Roteiro manual: cadastre, entre, crie e edite um hábito, registre e desmarque uma data, consulte o histórico, arquive e consulte a aba arquivados; reinicie os processos e faça login para verificar persistência. Teste também com uma segunda conta. A consulta de histórico é limitada ao período selecionado.

## Integração contínua (CI)

O workflow `.github/workflows/ci.yml` executa em pushes e pull requests para `main` e `develop`, e também pode ser iniciado manualmente na aba Actions. Ele roda os testes e empacota o backend com Java 21, executa o smoke test HTTP contra um PostgreSQL 16 isolado e compila o frontend com Node.js 22. Os relatórios dos testes unitários ficam disponíveis como artefato por 7 dias. Não são necessários secrets: as credenciais usadas no CI são temporárias e exclusivas do ambiente de teste.

O CI não faz deploy nem substitui testes de interface no navegador. Para impedir merges com falhas, configure uma regra de proteção para exigir os checks **Backend (Java 21, PostgreSQL 16)** e **Frontend (Node.js 22)** no GitHub; a disponibilidade depende do plano e das configurações do repositório.

## Segurança e limites
BCrypt, JWT de 8 horas, verificação por dono no backend, validação e CORS configurável. `localStorage` não é usado; o token fica em memória. Senhas, tokens e `.env` não devem ser enviados ao Git. Em produção seriam necessários HTTPS, limitação de tentativas, revogação/rotação de tokens, políticas de senha, auditoria, tratamento mais completo e avaliação de dependências. Não existe SLA ou benchmark de desempenho; veja `docs/qualidade.md`.

## Documentação e API
- [Termo de abertura](docs/project-charter.md), [requisitos e regras](docs/requisitos.md), [casos de uso](docs/casos-de-uso.md), [histórias](docs/user-stories.md), [arquitetura](docs/arquitetura.md), [modelo de dados](docs/modelo-dados.md), [rastreabilidade](docs/rastreabilidade.md), [qualidade](docs/qualidade.md), [backlog de Issues](docs/issues.md) e [publicação GitHub](docs/github.md).
- API: `POST /api/auth/register`, `POST /api/auth/login`, `GET/POST /api/habits`, `GET/PUT/DELETE /api/habits/{id}` (DELETE arquiva), `PUT/DELETE /api/habits/{id}/completions/{yyyy-MM-dd}`, `GET /api/habits/{id}/completions?from=yyyy-MM-dd&to=yyyy-MM-dd`. Envie `Authorization: Bearer <token>` nas rotas de hábitos. Para arquivados, `GET /api/habits?archived=true`.

## Git e versões
Use `develop` para integração, `main` para versões estáveis e branches `feature/<numero>-descricao` ou `fix/<numero>-descricao`. Faça PR para develop; após validação, PR develop → main, sem commits diretos em main. Use `feat:`, `fix:`, `docs:`, `test:` e referência à Issue. Após publicação, proteja main e develop. Guia completo em [docs/github.md](docs/github.md).

Histórico: **0.1.0 (em desenvolvimento)** — primeira implementação do MVP; ainda não é uma release publicada. SemVer: MAJOR para incompatibilidade, MINOR para função compatível e PATCH para correção compatível.
