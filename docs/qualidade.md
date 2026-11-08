# Qualidade, DevSecOps e desempenho

## Plano de testes
- Unitário: `HabitServiceTest` verifica data futura, isolamento, uso da inserção atômica, intervalo inválido e hábito arquivado; `JwtServiceTest` verifica assinatura, adulteração e segredo curto.
- Integração Spring com PostgreSQL: `ApiIntegrationIT` usa MockMvc, autenticação e repositórios reais; `mvn verify` executa os testes em transações revertidas. Use banco exclusivo de teste; Flyway pode alterar o schema.
- Concorrência: `ConcurrentCompletionIT` usa transações independentes no PostgreSQL para verificar duas marcações simultâneas da mesma data e o resultado HTTP. Não cobre corrida com arquivamento ou desmarcação.
- Integração funcional HTTP com PostgreSQL: `scripts/smoke_test.py`, em ambiente de desenvolvimento, cria contas e cobre o fluxo descrito no README; executar com servidor iniciado. Também reiniciar backend e verificar persistência manualmente.
- Interface: testar cadastro, login, campos obrigatórios, navegação por teclado, layout móvel, edição, arquivados, período e erros de rede. O build TypeScript/Vite não substitui teste no navegador.
- O CI executa os testes de integração e o smoke test HTTP com PostgreSQL 16 isolado. Testes adicionais desejáveis: concorrência com arquivamento e desmarcação, expiração de token, acessibilidade com leitor de tela e testes end-to-end de navegador.

## Segurança
Segredos por variáveis de ambiente; BCrypt, JWT assinado e com expiração, CORS e autorização por dono. Checklist antes da publicação: confirmar `.gitignore`, verificar histórico para vazamentos, revisar dependências, habilitar proteção de branches e secret scanning. HTTPS, rate limit, política contra brute force, auditoria, tratamento de XSS e gestão de tokens são pendências para produção. Login responde igualmente a conta inexistente/senha incorreta, mas não foi avaliado quanto a timing.

## Desempenho
Não há medição formal ou meta de desempenho validada. Medir em ambiente local informado: tempo de inicialização, latência p50/p95 por endpoint e volume de dados/concor­rência com ferramenta apropriada; documentar hardware, amostra e resultados. Índices em user_id e par habit_id/data auxiliam consultas, mas não substituem medição. Histórico não é paginado; limitar ou paginar antes de escalar.

## Registro de validação
Em ambiente Linux de desenvolvimento: build frontend aprovado; 8 testes unitários aprovados; smoke HTTP aprovado tanto diretamente na API quanto pelo proxy Vite, com PostgreSQL 17; login e histórico também foram verificados após reinício do backend. Flyway emitiu aviso de suporte para PostgreSQL 17. A interface no navegador e o Windows ainda requerem validação manual. O workflow de CI automatiza o build, os testes unitários e o smoke test HTTP; consulte a aba Actions para o resultado de cada execução. Qualquer falha observada deve ser registrada como Issue com passos, resultado esperado e observado, logs sem segredos e correção acompanhada de teste.
