# Qualidade, DevSecOps e desempenho

## Plano de testes
- Unitário: `HabitServiceTest` verifica data futura, isolamento, idempotência, intervalo inválido e hábito arquivado; `JwtServiceTest` verifica assinatura, adulteração e segredo curto.
- Integração funcional HTTP com PostgreSQL: `scripts/smoke_test.py`, em ambiente de desenvolvimento, cria contas e cobre o fluxo descrito no README; executar com servidor iniciado. Também reiniciar backend e verificar persistência manualmente.
- Interface: testar cadastro, login, campos obrigatórios, navegação por teclado, layout móvel, edição, arquivados, período e erros de rede. O build TypeScript/Vite não substitui teste no navegador.
- Testes adicionais desejáveis: integração automatizada e isolada do banco, concorrência, expiração de token, acessibilidade com leitor de tela e testes end-to-end de navegador.

## Segurança
Segredos por variáveis de ambiente; BCrypt, JWT assinado e com expiração, CORS e autorização por dono. Checklist antes da publicação: confirmar `.gitignore`, verificar histórico para vazamentos, revisar dependências, habilitar proteção de branches e secret scanning. HTTPS, rate limit, política contra brute force, auditoria, tratamento de XSS e gestão de tokens são pendências para produção. Login responde igualmente a conta inexistente/senha incorreta, mas não foi avaliado quanto a timing.

## Desempenho
Não há medição formal ou meta de desempenho validada. Medir em ambiente local informado: tempo de inicialização, latência p50/p95 por endpoint e volume de dados/concor­rência com ferramenta apropriada; documentar hardware, amostra e resultados. Índices em user_id e par habit_id/data auxiliam consultas, mas não substituem medição. Histórico não é paginado; limitar ou paginar antes de escalar.

## Registro de validação
Em ambiente Linux de desenvolvimento: build frontend aprovado; 8 testes unitários aprovados; smoke HTTP aprovado tanto diretamente na API quanto pelo proxy Vite, com PostgreSQL 17; login e histórico também foram verificados após reinício do backend. Flyway emitiu aviso de suporte para PostgreSQL 17. A interface no navegador e o Windows ainda requerem validação manual. CI ainda depende da publicação. Qualquer falha observada deve ser registrada como Issue com passos, resultado esperado e observado, logs sem segredos e correção acompanhada de teste.
