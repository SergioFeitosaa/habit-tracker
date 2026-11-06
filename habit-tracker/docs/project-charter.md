# Termo de abertura — Habit Tracker

**Natureza:** projeto acadêmico, identificação do autor e da instituição a preencher. **Prazo informado:** 06/11/2026. **Patrocinador/interessado:** participante da formação; usuários demonstrativos. **Equipe:** participante com assistência de IA; decisões e validação são responsabilidade humana.

## Problema e objetivo
Manter hábitos é difícil quando o acompanhamento é disperso. Entregar um MVP local, com contas isoladas, hábitos diários e histórico, para exercitar análise, arquitetura, implementação, qualidade e segurança.

## Escopo
Cadastro e login, gestão de hábitos ativos/arquivados, registro diário, histórico, PostgreSQL, documentação e instruções. Não inclui recuperação de senha, envio de e-mail, notificações, múltiplas frequências nem publicação em produção.

## Sucesso
Os critérios de aceite da demonstração constam em `requisitos.md`; o projeto deve iniciar em Windows com instruções reproduzíveis, e a rastreabilidade deve conectar requisitos ao backlog e código.

## Premissas, restrições e riscos
- Prazo de dois dias e instalação local de Java, Node e PostgreSQL já disponíveis; Maven e Git precisam ser conferidos.
- Riscos: configuração do banco, falta de tempo, vazamento de segredos, vulnerabilidades de dependências e falhas de integração. Mitigação: fluxo vertical primeiro, variáveis locais, testes e revisão antes da publicação.
- Mudanças passam por análise de impacto em requisitos, backlog e critérios de aceite. P1 só entra após P0.
