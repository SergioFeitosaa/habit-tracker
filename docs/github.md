# Publicação e Git Flow simplificado

A integração GitHub não está disponível nesta conversa; os passos precisam ser feitos por você. Não envie senhas ou tokens no chat. Primeiro inspecione `git status` e confirme que nenhum segredo está incluído. O repositório público será visível a todos.

1. No GitHub, clique **New repository**, nome `habit-tracker`, visibilidade Public. Para evitar históricos divergentes, não inicialize README, licença ou .gitignore pelo site.
2. Na pasta raiz, no PowerShell, configure seu nome e e-mail do Git caso ainda não os tenha configurado. Então:

```powershell
git init -b develop
git add .
git status
git commit -m "chore: inicializa MVP habit tracker"
git remote add origin https://github.com/SEU_USUARIO/habit-tracker.git
git push -u origin develop
```

Substitua `SEU_USUARIO`; use autenticação segura do GitHub (por exemplo Git Credential Manager). Se a pasta já tiver `.git`, não repita `git init`; confira `git branch` e `git remote -v` antes de prosseguir.
3. No GitHub, crie a branch `main` a partir de `develop` pela interface; isso cria a referência inicial, sem novo commit direto na main. Em Settings → Branches / Rules, defina `main` como branch padrão e proteja `main` e, quando possível, `develop`: exigir PR, checks e impedir pushes diretos. Disponibilidade de proteções depende do plano e das configurações.
4. Crie as labels mencionadas em `docs/issues.md`, depois abra as 16 Issues copiando título e corpo. Atualize os IDs locais com os números reais. Não marque como concluídas sem validar o aceite.
5. Para trabalho posterior: `git switch develop; git pull; git switch -c feature/NUMERO-descricao`. Faça commits, por exemplo `feat: registra conclusão diária (#NUMERO)`. Envie `git push -u origin feature/NUMERO-descricao`; abra PR para develop com `Closes #NUMERO` no corpo quando o PR realmente concluir a Issue. Após aprovação/testes, faça PR de develop para main. Para correções use `fix/NUMERO-descricao` e `fix:`. Evite fechamento automático prematuro quando várias Issues dependem da mesma entrega.
6. Quando validado, crie uma release/tag SemVer pela interface a partir de main, por exemplo `v0.1.0`. O projeto ainda não possui release publicada; não apresente o número de versão do código como uma tag já criada.

## Cadastrar backlog pelo GitHub CLI (alternativa ao cadastro manual)

Instale [GitHub CLI](https://cli.github.com/), abra um novo PowerShell e execute `gh auth login` para autenticar no navegador. Não compartilhe tokens no chat. Após publicar o repositório conforme acima, execute na raiz `powershell -ExecutionPolicy Bypass -File .\scripts\cadastrar-issues.ps1`. O script pede confirmação do repositório, cria labels ausentes e cadastra as Issues; se interrompido, pode ser executado novamente e ignora títulos existentes (consulta até 1.000 Issues). Revise os números reais e atualize `rastreabilidade.md`. Após modificar arquivos, faça commit numa feature branch e abra PR para develop; não altere diretamente main.
