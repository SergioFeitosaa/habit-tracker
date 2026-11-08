# Preparação da entrega do MVP

Este checklist organiza a preparação de uma versão de demonstração local e de portfólio. **Não é uma aprovação de release nem uma autorização para publicar**. A versão `0.1.0` nos manifests é de desenvolvimento; este documento não cria tag, release ou deploy.

Para cada entrega, registre o commit exato e a evidência correspondente. Não marque um item por uma execução em outro commit, por uma issue fechada ou apenas porque o procedimento existe. Se uma verificação for adiada, registre-a como **não executada**, com seus limites, em vez de assinalá-la como concluída.

## 1. Identificar a candidata

- [ ] Registrar commit da `main`, data, responsável pela validação e finalidade da entrega (demonstração local ou portfólio).
- [ ] Confirmar que o CI do commit da candidata terminou com sucesso: backend (Java 21, PostgreSQL, smoke e navegador) e frontend. Registrar o link da execução em Actions.
- [ ] Revisar o [changelog](../CHANGELOG.md), [README](../README.md) e [rastreabilidade](rastreabilidade.md) contra o código da candidata.
- [ ] Verificar se há mudanças não integradas, falhas ou riscos conhecidos que afetem o conteúdo da entrega; não assumir que um PR aberto já está na `main`.

## 2. Demonstrar o fluxo do MVP

- [ ] Registrar o resultado da demonstração dos seis critérios em [requisitos.md](requisitos.md#aceite-da-demonstração), incluindo marcar **e desmarcar**, arquivamento, isolamento e persistência após reinício.
- [ ] Quando a validação Windows for retomada, executar o [roteiro Windows](validacao-windows.md) em banco descartável e registrar versão do ambiente, commit e resultados. Até então, manter como pendente; testes no CI não comprovam o roteiro Windows.
- [ ] Registrar a avaliação manual da interface, incluindo teclado, leitor de tela, responsividade e mensagens de erro, no ambiente testado. Playwright e axe-core ajudam a detectar problemas, mas não comprovam acessibilidade completa.
- [ ] Registrar falhas e limitações observadas, com passos para reproduzir e sem dados pessoais, senhas, tokens ou logs sensíveis.

## 3. Revisar riscos e escopo

- [ ] Conferir os avisos da auditoria de dependências da candidata e revisar os riscos residuais em [dependências](dependencias.md); avisos no CI não equivalem à avaliação de explorabilidade.
- [ ] Manter explícito que o MVP é local e não está pronto para produção. Antes de qualquer publicação para uso real, avaliar os advisories Spring pendentes, suporte da linha usada, HTTPS, limitação de tentativas, gestão de tokens e demais itens de [segurança](qualidade.md#segurança).
- [ ] Não afirmar métricas de desempenho, cobertura completa ou conformidade de acessibilidade sem medições e evidências apropriadas.
- [ ] Se a entrega exigir identificação institucional, preencher autor e instituição com dados fornecidos pela pessoa responsável, sem presumir informações.

## 4. Preparar a versão, sem publicá-la automaticamente

- [ ] Com os critérios acordados concluídos ou limitações explicitamente aceitas para o objetivo da entrega, decidir a versão SemVer e conferir compatibilidade com os manifests.
- [ ] Preparar notas a partir de **Não lançado** no changelog, descrevendo funcionalidades, verificações efetivamente executadas, limitações e instruções de execução local.
- [ ] Revisar os links e confirmar que não há dados sensíveis ou artefatos de teste nos materiais de entrega.
- [ ] Solicitar decisão explícita antes de criar tag ou release; não publicar ou fazer deploy como parte deste checklist.

## Registro de evidências

Copie esta seção para o local acordado da entrega e preencha apenas após cada verificação. Não inclua segredos nem dados pessoais.

| Item | Resultado (passou / falhou / não executado) | Commit, ambiente e link da evidência | Observações e limitações |
|---|---|---|---|
| CI da candidata | Não executado | — | — |
| Demonstração dos critérios | Não executado | — | — |
| Roteiro Windows | Não executado | — | Adiado por decisão do responsável. |
| Acessibilidade manual | Não executado | — | Testes automatizados não a substituem. |
| Revisão de dependências e riscos | Não executado | — | Avaliar antes da entrega; uso em produção exige revisão adicional. |
| Documentação e notas da versão | Não executado | — | — |
