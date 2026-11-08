# Changelog

Mudanças relevantes serão registradas aqui seguindo a estrutura de [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Versões e releases seguem [SemVer](https://semver.org/lang/pt-BR/); não há release publicada neste registro até que uma tag seja criada e validada.

## Não lançado

### Adicionado
- Testes Playwright/Chromium no CI para fluxo principal, troca de contas, navegação por teclado, visualização móvel básica e recuperação de erros simulados.
- Verificações automatizadas axe-core em telas selecionadas; não substituem testes manuais de acessibilidade.
- Testes unitários e de integração para validade e rejeição de JWT expirado, incluindo novo login.
- Ação para atualizar a lista de hábitos após falha de consulta.
- Checkstyle e verificação de formatação Java no backend e no CI.
- Lint e verificação de formatação do frontend no CI.
- Testes de integração da API com Spring, MockMvc e PostgreSQL para autenticação, isolamento e regras de hábitos.
- Guia de contribuição, índice de documentação e templates de issues e pull requests.

### Corrigido
- Estado da interface é reiniciado ao sair, evitando manter rascunhos e filtros ao trocar de conta.
- Mensagens distinguem credenciais inválidas, sessão inválida e falhas de conexão; rascunhos não são descartados automaticamente ao receber erro.
- Alterações confirmadas são distinguidas de falhas ao atualizar a lista ou o histórico; dados conhecidos são refletidos na interface.
- Ajustados contrastes e nome acessível do controle de consulta de hábito.
- Alterações de um hábito são serializadas para evitar registros ou remoções após arquivamento concorrente.
- Marcações simultâneas da mesma realização usam inserção atômica no PostgreSQL.

### Alterado
- Matriz de rastreabilidade vinculada às issues do GitHub e às evidências de implementação e teste.
- Documentação de Git atualizada para refletir um repositório já existente.
