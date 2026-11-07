# Fluxo Git e GitHub

O repositório já existe e a branch padrão é `main`. Consulte o estado real das branches, pull requests e configurações no GitHub antes de agir; não execute `git init` nem recrie o repositório para contribuir.

## Fluxo de alteração

1. Confira o estado local com `git status` e atualize a branch de destino sem sobrescrever mudanças locais. Crie uma branch de trabalho a partir dela.
2. Implemente uma mudança coesa, inclua testes e documentação e revise o diff para evitar envio de segredos ou arquivos gerados. Nunca envie senhas ou tokens no chat.
3. Use commits claros, preferencialmente Conventional Commits, e referencie uma issue quando aplicável. Faça push da branch de trabalho e abra um PR para a branch de destino.
4. Revise as alterações e os checks de backend e frontend. A auditoria de dependências gera avisos para triagem e não bloqueia automaticamente o CI. Vincule `Closes #n` somente se a entrega satisfizer os critérios da issue.
5. Após validação, integre por PR. `main` deve permanecer protegida; confirme a configuração em **Settings → Branches / Rules** e exija os checks de backend e frontend. Disponibilidade de regras depende do plano e das permissões.

`develop` pode ser usada como branch de integração quando acordado, mas sua existência não garante que esteja sincronizada com `main`. Antes de abrir um PR entre elas, compare os históricos e as alterações para evitar reintroduzir arquivos antigos ou desfazer correções. Consulte [CONTRIBUTING.md](../CONTRIBUTING.md) para detalhes dos testes.

## Issues e versões

As issues do backlog original já existem no GitHub e estão vinculadas em [rastreabilidade.md](rastreabilidade.md). Antes de criar uma issue, procure tarefas existentes; status fechado não substitui evidência de validação. O script `scripts/cadastrar-issues.ps1` é histórico: antes de usá-lo, verifique seu impacto e evite duplicação.

Os manifests indicam a versão de desenvolvimento `0.1.0`; isso não significa que uma tag ou release tenha sido publicada. Só crie uma versão SemVer após validar os critérios de entrega e registrar as alterações no [CHANGELOG](../CHANGELOG.md).
