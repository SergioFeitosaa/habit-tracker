# Como contribuir

Obrigado pelo interesse no Habit Tracker. Mudanças pequenas, com objetivo e critérios claros, facilitam a revisão e a evolução do projeto.

## Antes de começar

1. Consulte o [README](README.md), a [documentação](docs/README.md) e as issues existentes para evitar trabalho duplicado.
2. Para mudanças significativas, descreva o problema e os critérios de aceite em uma issue antes da implementação. Não inclua credenciais, tokens, dados pessoais ou arquivos `.env`.
3. Crie uma branch de trabalho a partir da versão de destino atualizada. A `main` é a versão principal; use `develop` como destino apenas quando o fluxo e sua sincronização forem acordados.

## Implementação e testes

- Faça commits coesos no formato Conventional Commits, por exemplo `fix: valida data futura` ou `docs: atualiza rastreabilidade`. Referencie a issue relevante quando existir.
- Atualize os testes e a documentação afetada. Para comandos completos e pré-requisitos, consulte o [README](README.md).
- No backend, `npm ci` instala a ferramenta de formatação Java; `npm run format` aplica o formato e `npm run format:check` o verifica. `mvn test` executa o Checkstyle e os testes unitários; `mvn verify` inclui integração com PostgreSQL. Use **um banco isolado de teste**, com variáveis `DB_*` e `JWT_SECRET` configuradas; Flyway pode alterar seu schema. Nunca use produção.
- No frontend, execute `npm ci`, `npm run lint`, `npm run format:check` e `npm run build`. Use `npm run format` para aplicar o formato. O build não substitui o teste da interface no navegador.
- Quando alterar requisitos, comportamento ou fluxos, atualize as histórias, a [rastreabilidade](docs/rastreabilidade.md) e demais documentos impactados.

## Pull request

Abra um PR para a branch de destino e preencha o template: objetivo, mudanças, testes e riscos. Use `Closes #n` somente quando o PR de fato satisfizer todos os critérios da issue; caso contrário, apenas referencie `#n`. Confira os resultados do CI, incluindo os avisos de dependências, antes de solicitar o merge. O CI pode ficar verde mesmo quando a auditoria apresenta avisos que exigem triagem.

Não crie tags ou releases antes de validar a versão. Para configurações do repositório e revisão de segurança, consulte [docs/github.md](docs/github.md) e [docs/dependencias.md](docs/dependencias.md).
