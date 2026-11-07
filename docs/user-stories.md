# Histórias de usuário

| ID | História | Requisitos | Aceite resumido |
|---|---|---|---|
| US01 | Como visitante quero criar conta para ter dados próprios. | RF01 | Nome, e-mail único e senha válidos; senha protegida. |
| US02 | Como pessoa cadastrada quero entrar para acessar meus dados. | RF02 | Credenciais válidas geram JWT; inválidas são recusadas. |
| US03 | Como usuário quero criar e visualizar hábitos para organizar minha rotina. | RF03, RF04 | Nome obrigatório; lista e detalhes próprios. |
| US04 | Como usuário quero editar e arquivar hábitos para ajustar minha rotina. | RF05, RF06 | Dados alterados persistem; histórico arquivado permanece. |
| US05 | Como usuário quero marcar e desmarcar dias para registrar realizações e corrigir enganos. | RF07 | Uma realização por dia; datas futuras proibidas. |
| US06 | Como usuário quero consultar histórico e progresso para acompanhar minha consistência. | RF08, RF09 | Filtro por período, datas e total. |
| US07 | Como usuário quero privacidade para que outras contas não vejam meus dados. | RF02, RNF02 | Requisições anônimas negadas e IDs de outros usuários retornam 404. |
