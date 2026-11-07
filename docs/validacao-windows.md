# Roteiro de validação no Windows

Este roteiro complementa o CI e cobre as issues [#10](https://github.com/SergioFeitosaa/habit-tracker/issues/10), [#11](https://github.com/SergioFeitosaa/habit-tracker/issues/11), [#12](https://github.com/SergioFeitosaa/habit-tracker/issues/12) e [#14](https://github.com/SergioFeitosaa/habit-tracker/issues/14). Os itens são um plano de teste: não marquem uma issue como concluída sem registrar o resultado real.

## Preparação

- Use uma cópia atualizada da `main`, sem alterações locais pendentes que possam ser sobrescritas. Confira o commit com `git rev-parse --short HEAD`.
- No Windows, siga os pré-requisitos e as seções de PostgreSQL, backend e frontend do [README](../README.md). São necessários Java 21, Maven, Node.js 22.12 ou superior e PostgreSQL.
- Use exclusivamente banco e contas de **teste**. O roteiro cria e altera dados; não use banco de produção.
- Não compartilhe senhas, tokens, dados pessoais, arquivos `.env` ou logs contendo segredos. Use endereços de teste fictícios.
- Deixe o backend e o frontend rodando em terminais separados. Acesse `http://localhost:5173` no navegador da própria máquina.

## Cadastro e login — #10 e #12

- [ ] Abra **Cadastre-se**, tente enviar os campos obrigatórios vazios ou inválidos e confirme que a interface impede o envio ou informa o problema.
- [ ] Cadastre uma conta de teste com e-mail ainda não usado e senha de pelo menos oito caracteres. Confirme a mensagem de sucesso.
- [ ] Tente entrar com senha incorreta e confirme que uma mensagem de erro aparece. Entre com as credenciais corretas e confirme que a lista de hábitos aparece.
- [ ] Use **Sair**, confirme que retorna à tela de login e entre novamente.

## Hábitos e acompanhamento — #11 e #12

- [ ] Crie um hábito com nome e descrição e confirme que aparece na lista. Edite-o e confirme que os novos dados aparecem.
- [ ] Selecione o hábito, marque uma data válida, consulte um período que inclua essa data e confirme a data e o total. Marque a mesma data novamente e confirme que o total não duplicou.
- [ ] Desmarque a data e confirme que ela desaparece do histórico. Marque novamente para o teste de persistência.
- [ ] Arquive o hábito, confirme que desapareceu dos ativos e aparece em **Ver arquivados**. Selecione-o e confirme que seu histórico permanece disponível.
- [ ] Em uma segunda conta de teste, confirme que o hábito da primeira conta não aparece. Essa verificação pela interface complementa, mas não substitui, testes de autorização da API.
- [ ] Teste em janela larga e estreita (por exemplo, 375 px no modo responsivo do navegador). Confirme que campos e botões ficam visíveis e utilizáveis sem conteúdo essencial cortado. Teste também a navegação com a tecla Tab.

## Persistência após reinício — #14

- [ ] Encerre backend e frontend com Ctrl+C, sem apagar o banco ou o volume Docker. Se estiver usando Docker, **não** execute `docker compose down -v`, pois isso apaga o volume.
- [ ] Inicie novamente os serviços conforme o README. Entre com a primeira conta (a sessão em memória termina quando a página é recarregada).
- [ ] Confirme que o hábito arquivado, os dados editados e a realização marcada continuam presentes no período consultado.

## Registrar resultados

Para cada issue, registre em comentário: data, commit, versão do Windows, navegador e versões de Java, Maven, Node.js e PostgreSQL; indique quais itens passaram, falharam ou não foram executados. Para falhas, descreva passos, resultado esperado e resultado observado, incluindo apenas capturas ou logs sem segredos. Feche cada issue somente quando todos os seus critérios forem atendidos; a #14 exige execução no computador Windows da demonstração.
