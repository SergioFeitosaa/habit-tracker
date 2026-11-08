# Arquitetura

```text
Navegador React/TypeScript → Vite proxy /api → Spring MVC Controller → Service → Repository/JPA → PostgreSQL
                                                    ↑ JWT Filter / Security           ↑ Flyway
```

Monólito modular em camadas: controllers recebem DTOs validados; services aplicam regras e escopo de proprietário; repositories acessam entities; exceptions centralizam respostas conhecidas; configurations definem segurança, JWT e CORS. Spring transaciona operações de hábitos. Flyway cria o esquema e Hibernate valida mapeamentos na inicialização.

Login autentica e gera JWT assinado, com validade de oito horas. Em rotas protegidas, filtro valida assinatura e expiração e busca a conta; o service procura o hábito pelo ID **e** usuário. Resposta 404 evita revelar se um ID alheio existe. A marcação usa inserção atômica no PostgreSQL com `ON CONFLICT DO NOTHING`: marcações simultâneas da mesma data são idempotentes, mantendo a restrição de unicidade no banco. Alterações no mesmo hábito (edição, arquivamento, marcação e desmarcação) são serializadas por bloqueio pessimista por proprietário durante a transação. Assim, uma alteração que aguarda um arquivamento confirmado recebe 409; se ocorrer primeiro, é concluída antes do arquivamento. Leituras não usam esse bloqueio.

Decisões e limitações: token em memória evita armazenamento persistente no navegador, mas recarregar encerra sessão. CORS é restrito à origem configurada; proxy Vite é para desenvolvimento. Fuso de referência é `America/Recife` por padrão. Sem controle de acesso baseado em papéis, rate limit, revogação ou recuperação de senha; não é configuração para produção. Ver `qualidade.md`.
