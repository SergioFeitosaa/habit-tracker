# Arquitetura

```text
Navegador React/TypeScript → Vite proxy /api → Spring MVC Controller → Service → Repository/JPA → PostgreSQL
                                                    ↑ JWT Filter / Security           ↑ Flyway
```

Monólito modular em camadas: controllers recebem DTOs validados; services aplicam regras e escopo de proprietário; repositories acessam entities; exceptions centralizam respostas conhecidas; configurations definem segurança, JWT e CORS. Spring transaciona operações de hábitos. Flyway cria o esquema e Hibernate valida mapeamentos na inicialização.

Login autentica e gera JWT assinado, com validade de oito horas. Em rotas protegidas, filtro valida assinatura e expiração e busca a conta; o service procura o hábito pelo ID **e** usuário. Resposta 404 evita revelar se um ID alheio existe. Unicidade de realização é garantida no banco; chamadas simultâneas duplicadas podem retornar 409.

Decisões e limitações: token em memória evita armazenamento persistente no navegador, mas recarregar encerra sessão. CORS é restrito à origem configurada; proxy Vite é para desenvolvimento. Fuso de referência é `America/Recife` por padrão. Sem controle de acesso baseado em papéis, rate limit, revogação ou recuperação de senha; não é configuração para produção. Ver `qualidade.md`.
