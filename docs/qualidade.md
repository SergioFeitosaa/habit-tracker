# Qualidade, DevSecOps e desempenho

## Plano de testes
- Unitário: `HabitServiceTest` verifica data futura, isolamento, uso da inserção atômica, intervalo inválido e hábito arquivado; `JwtServiceTest` verifica assinatura, adulteração e segredo curto.
- Integração Spring com PostgreSQL: `ApiIntegrationIT` usa MockMvc, autenticação e repositórios reais; `mvn verify` executa os testes em transações revertidas. Use banco exclusivo de teste; Flyway pode alterar o schema.
- Concorrência: `ConcurrentCompletionIT` usa transações independentes no PostgreSQL para verificar duas marcações simultâneas da mesma data e o resultado HTTP. `ConcurrentArchiveIT` cobre marcação e desmarcação aguardando um arquivamento e arquivamento aguardando marcação; outras interações simultâneas requerem avaliação adicional.
- Integração funcional HTTP com PostgreSQL: `scripts/smoke_test.py`, em ambiente de desenvolvimento, cria contas e cobre o fluxo descrito no README; executar com servidor iniciado. Também reiniciar backend e verificar persistência manualmente.
- Navegador: Playwright/Chromium cobre cadastro, login, criação e edição de hábito, marcação, histórico, arquivamento, nova sessão e isolamento entre contas, incluindo a limpeza dos rascunhos e filtros ao sair sem recarregar a página. Uma verificação básica usa largura móvel na tela de entrada. Não substitui validação manual de campos obrigatórios, teclado, leitores de tela, erros de rede ou outros navegadores/dispositivos.
- O CI executa os testes de integração e o smoke test HTTP com PostgreSQL 16 isolado. Testes adicionais desejáveis: mais combinações de operações concorrentes, expiração de token, acessibilidade com leitor de tela e testes adicionais de navegador.

## Segurança
Segredos por variáveis de ambiente; BCrypt, JWT assinado e com expiração, CORS e autorização por dono. Checklist antes da publicação: confirmar `.gitignore`, verificar histórico para vazamentos, revisar dependências, habilitar proteção de branches e secret scanning. HTTPS, rate limit, política contra brute force, auditoria, tratamento de XSS e gestão de tokens são pendências para produção. Login responde igualmente a conta inexistente/senha incorreta, mas não foi avaliado quanto a timing.

## Desempenho
Não há medição formal ou meta de desempenho validada. Medir em ambiente local informado: tempo de inicialização, latência p50/p95 por endpoint e volume de dados/concor­rência com ferramenta apropriada; documentar hardware, amostra e resultados. Índices em user_id e par habit_id/data auxiliam consultas, mas não substituem medição. Histórico não é paginado; limitar ou paginar antes de escalar.

## Registro de validação
Em ambiente Linux de desenvolvimento: build frontend aprovado; 8 testes unitários aprovados; smoke HTTP aprovado tanto diretamente na API quanto pelo proxy Vite, com PostgreSQL 17; login e histórico também foram verificados após reinício do backend. Flyway emitiu aviso de suporte para PostgreSQL 17. A validação manual da interface e do Windows ainda está pendente; há também testes automatizados de navegador no CI. O workflow de CI automatiza o build, os testes unitários e o smoke test HTTP; consulte a aba Actions para o resultado de cada execução. Qualquer falha observada deve ser registrada como Issue com passos, resultado esperado e observado, logs sem segredos e correção acompanhada de teste.

## Executar o teste de navegador

Use **somente um backend e banco PostgreSQL descartáveis**, nunca dados reais: o teste cria duas contas e hábitos e não os remove. Inicie o backend conforme o README, apontando para esse banco, e aguarde-o estar pronto. Em outro terminal, no diretório `frontend`, execute:

```bash
npm ci
npx playwright install chromium
npm run test:e2e
```

O Playwright inicia e encerra seu próprio Vite em `localhost:5173`; a porta deve estar livre. O proxy padrão aponta ao backend em `localhost:8080` (ou use `VITE_PROXY_TARGET` para o backend descartável). No Linux podem ser necessárias bibliotecas do navegador (`npx playwright install --with-deps chromium`). O CI executa o fluxo completo com PostgreSQL 16 isolado e credenciais descartáveis. Os rastros locais em `frontend/test-results/` podem conter dados de sessão: são ignorados pelo Git e não são enviados como artefatos no CI. O teste não valida reinício do backend, Windows, acessibilidade completa nem segurança de produção.
