# Habit Tracker — requisitos do MVP

## Objetivo
Permitir que cada pessoa mantenha hábitos diários e acompanhe suas realizações em uma aplicação web executada localmente.

## Escopo e regras de negócio
- Cadastro com nome, e-mail e senha; login com e-mail e senha. Não há recuperação de senha nem envio de e-mail no MVP.
- E-mail único, normalizado e armazenado sem distinção de maiúsculas; senha armazenada com BCrypt, nunca em texto puro.
- Cada hábito pertence a um usuário e tem nome obrigatório, descrição opcional e estado ativo ou arquivado.
- Hábitos são diários. Cada hábito admite no máximo uma realização por data, protegida também por restrição de unicidade no banco.
- Permitir marcar e desmarcar datas atuais ou passadas; proibir datas futuras. Validar a data também no backend.
- Arquivar preserva o histórico. A lista principal mostra hábitos ativos; o histórico permanece consultável. Restauração fica fora do MVP.
- O usuário só acessa ou altera os próprios hábitos e realizações. Identificadores de outro usuário não devem revelar dados.
- Progresso básico: quantidade de dias concluídos no período consultado. Uma data usa calendário local configurado no servidor; fuso padrão recomendado: America/Recife.

## Requisitos funcionais
- RF01: cadastrar usuário.
- RF02: autenticar usuário e emitir JWT.
- RF03: criar hábito ativo.
- RF04: listar hábitos ativos e consultar detalhes próprios.
- RF05: editar nome e descrição de hábito próprio.
- RF06: arquivar hábito próprio e preservar histórico.
- RF07: marcar e desmarcar realização por data.
- RF08: consultar histórico e quantidade de realizações no período.
- RF09: consumir a API pela interface web.

## Requisitos não funcionais
- RNF01: Java 21, Spring Boot 3, Maven, React, TypeScript, Vite e PostgreSQL.
- RNF02: autenticação JWT, autorização por usuário, BCrypt, validação de entrada e CORS restrito à origem configurada.
- RNF03: persistência PostgreSQL e migrations Flyway versionadas.
- RNF04: instalação local no Windows documentada, exemplos de configuração sem segredos reais.
- RNF05: interface responsiva e utilizável por teclado, com rótulos e mensagens de erro.
- RNF06: testes automatizados e verificação manual do fluxo vertical; não afirmar cobertura ou desempenho sem medição.
- RNF07: Git Flow simplificado, Conventional Commits e SemVer; alterações em main apenas por PR.

## Aceite da demonstração
1. Cadastrar e autenticar uma conta.
2. Criar, listar, consultar e editar um hábito.
3. Marcar e desmarcar uma data, consultar histórico e progresso.
4. Arquivar o hábito e verificar que seu histórico foi preservado.
5. Reiniciar a aplicação e verificar a persistência no PostgreSQL.
6. Autenticar outro usuário e verificar que não consegue acessar dados do primeiro.

## Fora do escopo inicial
Recuperação de senha, e-mails, múltiplas frequências, notificações, deploy público e indicadores avançados. Swagger, Docker Compose e CI/CD são complementações P1.
