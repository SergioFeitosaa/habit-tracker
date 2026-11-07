# Casos de uso

Ator principal: pessoa usuária. A autenticação é pré-condição para UC03–UC05. Erros de validação devem ser exibidos sem expor detalhes internos.

## UC01 — Cadastrar (RF01)
Pessoa informa nome, e-mail e senha; sistema valida, normaliza e-mail, verifica unicidade e salva hash. Alternativas: dados inválidos (400), e-mail existente (409). Resultado: conta criada, sem login automático.
## UC02 — Entrar (RF02)
Pessoa informa e-mail e senha; sistema valida credenciais e retorna token e nome. Alternativa: credenciais inválidas (401). Resultado: token usado nas solicitações protegidas.
## UC03 — Gerenciar hábitos (RF03–RF06)
Pessoa autenticada cria, lista, consulta e edita hábitos próprios. Ao arquivar, hábito sai da lista ativa, mas aparece na lista de arquivados e histórico é preservado. Alternativas: entrada inválida (400); hábito inexistente ou alheio (404); edição de arquivado (409).
## UC04 — Registrar realização (RF07)
Pessoa seleciona hábito ativo e data de hoje ou anterior; marca ou desmarca. Marcar repetidamente não duplica. Alternativas: data futura (400), hábito arquivado (409), hábito alheio (404).
## UC05 — Consultar histórico (RF08–RF09)
Pessoa seleciona hábito próprio e período; sistema apresenta datas e total. Inclui hábitos arquivados. Período invertido resulta em 400.
