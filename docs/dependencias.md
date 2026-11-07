# Gestão de dependências e riscos

## Inventário e análise

O job de backend no CI exporta `dependency-tree.json` com a árvore resolvida do Maven, incluindo dependências transitivas e de teste. O script `scripts/audit_maven.py` consulta a API pública do GitHub Advisory Database para cada coordenada e versão resolvida, gera `dependency-audit.md` e registra avisos quando encontra advisories. Os dois arquivos são publicados no artefato `backend-dependency-audit`, com retenção de 30 dias. A execução ocorre em PRs para `main`/`develop`, pushes nessas branches, manualmente e semanalmente na branch padrão.

Os achados **não falham o job automaticamente**: precisam ser triados para determinar escopo, condições de exploração e atualização segura. Falha na obtenção da árvore ou na consulta à API falha o job, para não confundir erro de consulta com análise limpa. A ausência de advisories não garante segurança: o catálogo pode não conter todos os problemas, e não cobre configuração, código próprio ou necessariamente todo componente distribuído fora da árvore Maven. Dependências de teste são incluídas e devem ser avaliadas no contexto do CI.

Para repetir localmente, com Java 21, Maven e Python 3 instalados e acesso ao GitHub e ao Maven Central:

```powershell
cd backend
mvn --batch-mode --no-transfer-progress org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree -DoutputType=json -DoutputFile=target/dependency-tree.json
python ../scripts/audit_maven.py target/dependency-tree.json target/dependency-audit.md
```

Consultas não autenticadas têm limite menor na API. Opcionalmente, use autenticação já disponível no ambiente, sem incluir credenciais no código ou nos relatórios. Para o frontend, execute `npm ci` e `npm audit` dentro de `frontend`.

## Acompanhamento contínuo

`.github/dependabot.yml` solicita propostas semanais de atualização para Maven, npm e GitHub Actions, após entrar na branch padrão. PRs devem ser revisados e testados; atualizações automáticas não garantem correção nem substituem a triagem de alertas. Em **Settings → Code security** habilite o dependency graph e os alertas de segurança do Dependabot; permissões administrativas podem ser necessárias. A configuração de version updates não os habilita automaticamente. Confira a aba Security e os resultados do CI periodicamente.

## Suporte e risco residual

O PR #23 propõe Spring Boot 3.5.16, uma versão posterior à correção do advisory GHSA-wwpq-f5c3-7hvx na linha 3.5. A consulta pontual ao GitHub Advisory Database para `spring-boot:3.5.16` não substitui a análise da árvore inteira. A [política oficial de suporte](https://github.com/spring-projects/spring-boot/wiki/Supported-Versions) recomenda usar uma versão suportada e verificar também os ciclos de vida das bibliotecas de terceiros. A situação de suporte gratuito atual de 3.5.16 deve ser confirmada na [página oficial](https://spring.io/projects/spring-boot#support); não foi possível verificá-la neste ambiente. Planeje a migração para uma linha suportada e valide alterações incompatíveis antes de uso em produção.

A análise de dependências não substitui testes de segurança, proteção de branches, revisão de configurações e validação no navegador. Nunca publique senhas, tokens ou logs contendo segredos nos relatórios.

### Triagem inicial do PR #23

Na execução de CI para o commit `1216a82`, após atualizar Jackson, Tomcat, PostgreSQL JDBC e Log4j, a análise retornou dois advisories para `org.springframework:spring-webmvc:6.2.19`: [GHSA-j9f9-w8pj-32f8](https://github.com/advisories/GHSA-j9f9-w8pj-32f8) e [GHSA-pc63-qcmh-9cmg](https://github.com/advisories/GHSA-pc63-qcmh-9cmg), ambos classificados como críticos no GitHub. O primeiro descreve uso de Server-Sent Events com fragmentos de view; o segundo depende de `XsltView` e mapeamento de renderização específico. Não foram encontradas referências explícitas a esses recursos em `backend/src`, mas isso não é prova definitiva de ausência de exposição. Os advisories não indicam correção pública na linha 6.2; avalie migração de Spring Boot/Framework para uma linha suportada, com testes e revisão de compatibilidade, antes de produção. O resultado é um retrato da execução, não uma garantia permanente; consulte sempre a execução mais recente.
