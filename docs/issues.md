# Backlog de Issues

Este documento preserva o backlog original I01–I16. Os identificadores correspondem às issues GitHub [#1](https://github.com/SergioFeitosaa/habit-tracker/issues/1)–[#16](https://github.com/SergioFeitosaa/habit-tracker/issues/16), respectivamente. Consulte o GitHub para o estado atual; as caixas abaixo são o registro original dos critérios, não um painel de status. Veja a [matriz de rastreabilidade](rastreabilidade.md) e confirme a evidência de cada validação.

## I01 — Configurar repositório e proteção de branches

**Descrição:** Configurar repositório e proteção de branches.

**Contexto:** O projeto precisa de histórico rastreável e fluxo de revisão.

**Requisitos relacionados:** RNF07.

**Critérios de aceite:**
- [ ] Criar repositório público e branches; documentar proteção e publicar sem segredos.

**Labels:** P0, devops.

## I02 — Documentar termo de abertura e requisitos

**Descrição:** Documentar termo de abertura e requisitos.

**Contexto:** O escopo deve ser acordado antes da implementação.

**Requisitos relacionados:** RF01–RF09, RNF01–RNF07.

**Critérios de aceite:**
- [ ] Registrar objetivo, regras, escopo, critérios e riscos.

**Labels:** P0, documentação.

## I03 — Inicializar backend Spring Boot e frontend Vite

**Descrição:** Inicializar backend Spring Boot e frontend Vite.

**Contexto:** As aplicações precisam de dependências e comandos reproduzíveis.

**Requisitos relacionados:** RNF01, RNF04.

**Critérios de aceite:**
- [ ] Compilar backend Java 21 e frontend TypeScript e documentar comandos.

**Labels:** P0, backend, frontend.

## I04 — Modelar PostgreSQL e migration Flyway

**Descrição:** Modelar PostgreSQL e migration Flyway.

**Contexto:** Os dados devem persistir com integridade.

**Requisitos relacionados:** RNF03, RF01, RF03, RF07.

**Critérios de aceite:**
- [ ] Criar tabelas, chaves, índice e unicidade de realização; executar migration.

**Labels:** P0, banco.

## I05 — Implementar cadastro e BCrypt

**Descrição:** Implementar cadastro e BCrypt.

**Contexto:** Pessoas precisam de contas isoladas.

**Requisitos relacionados:** RF01, RNF02.

**Critérios de aceite:**
- [ ] Validar campos; normalizar e-mail; rejeitar duplicado; armazenar hash.

**Labels:** P0, backend, segurança.

## I06 — Implementar login e emissão JWT

**Descrição:** Implementar login e emissão JWT.

**Contexto:** Usuários cadastrados precisam iniciar sessão.

**Requisitos relacionados:** RF02, RNF02.

**Critérios de aceite:**
- [ ] Credenciais válidas geram token com validade; inválidas recebem 401.

**Labels:** P0, backend, segurança.

## I07 — Proteger API e isolar dados por usuário

**Descrição:** Proteger API e isolar dados por usuário.

**Contexto:** Dados privados não podem ser acessados por outra conta.

**Requisitos relacionados:** RF02, RNF02.

**Critérios de aceite:**
- [ ] Exigir JWT em hábitos; restringir CORS; consultar recurso por proprietário; negar acesso cruzado.

**Labels:** P0, backend, segurança.

## I08 — Implementar gerenciamento de hábitos

**Descrição:** Implementar gerenciamento de hábitos.

**Contexto:** Usuários devem gerenciar sua rotina.

**Requisitos relacionados:** RF03, RF04, RF05, RF06.

**Critérios de aceite:**
- [ ] Criar, listar, detalhar, editar e arquivar; preservar histórico.

**Labels:** P0, backend.

## I09 — Implementar realizações e histórico

**Descrição:** Implementar realizações e histórico.

**Contexto:** A rotina exige registro por dia e consulta por período.

**Requisitos relacionados:** RF07, RF08.

**Critérios de aceite:**
- [ ] Marcar e desmarcar; impedir futuro; garantir unicidade; consultar datas e total.

**Labels:** P0, backend, banco.

## I10 — Construir telas de cadastro e login

**Descrição:** Construir telas de cadastro e login.

**Contexto:** O fluxo deve ser acessível pelo navegador.

**Requisitos relacionados:** RF01, RF02, RNF05.

**Critérios de aceite:**
- [ ] Formulários com validação e mensagens; cadastro e login conectados à API.

**Labels:** P0, frontend.

## I11 — Construir interface de hábitos e acompanhamento

**Descrição:** Construir interface de hábitos e acompanhamento.

**Contexto:** O usuário deve utilizar as operações principais visualmente.

**Requisitos relacionados:** RF03–RF09, RNF05.

**Critérios de aceite:**
- [ ] Criar, editar, arquivar, selecionar, marcar e consultar histórico; layout responsivo.

**Labels:** P0, frontend.

## I12 — Integrar frontend e backend

**Descrição:** Integrar frontend e backend.

**Contexto:** A demonstração precisa cobrir o fluxo vertical.

**Requisitos relacionados:** RF09, RNF01, RNF02.

**Critérios de aceite:**
- [ ] Proxy /api configurado; enviar token; demonstrar fluxo e tratar erros.

**Labels:** P0, integração.

## I13 — Testar regras e segurança

**Descrição:** Testar regras e segurança.

**Contexto:** Falhas de regra e isolamento precisam ser detectadas.

**Requisitos relacionados:** RNF02, RNF06.

**Critérios de aceite:**
- [ ] JUnit cobre regras e JWT; executar smoke HTTP com duas contas.

**Labels:** P0, testes, segurança.

## I14 — Validar persistência e demonstração local

**Descrição:** Validar persistência e demonstração local.

**Contexto:** O MVP precisa funcionar no computador da apresentação.

**Requisitos relacionados:** RNF03, RNF04, RNF06.

**Critérios de aceite:**
- [ ] Executar roteiro completo no Windows e verificar dados após reiniciar.

**Labels:** P0, testes.

## I15 — Documentar execução e rastreabilidade

**Descrição:** Documentar execução e rastreabilidade.

**Contexto:** O projeto deve ser reproduzível e explicável.

**Requisitos relacionados:** RNF04, RNF07.

**Critérios de aceite:**
- [ ] README contém pré-requisitos, passos e testes; mapear requisitos, histórias e código.

**Labels:** P0, documentação.

## I16 — Configurar CI e plano de qualidade

**Descrição:** Configurar CI e plano de qualidade.

**Contexto:** A equipe precisa de feedback automatizado e riscos explícitos.

**Requisitos relacionados:** RNF06, RNF07.

**Critérios de aceite:**
- [ ] Executar testes e build no Actions; registrar plano de segurança e desempenho.

**Labels:** P1, devops, testes.
