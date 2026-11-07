# Rastreabilidade

Os números I01–I16 são identificadores locais de planejamento, **não** números de Issues já criadas no GitHub. Atualize-os após o cadastro real.

| Requisito | História | Backlog | Implementação / verificação |
|---|---|---|---|
| RF01 | US01 | I05, I06 | AuthController, AuthService; smoke HTTP |
| RF02, RNF02 | US02, US07 | I06, I07, I13 | JwtService, JwtFilter, SecurityConfig, HabitService; JwtServiceTest e smoke |
| RF03–RF06 | US03, US04 | I08, I10 | HabitController, HabitService, App.tsx (`src/main.tsx`); smoke |
| RF07 | US05 | I09, I11 | HabitService, CompletionRepository, frontend; HabitServiceTest e smoke |
| RF08–RF09 | US06 | I09, I11, I12 | HabitService, HabitController, frontend; smoke |
| RNF01, RNF03 | US03 | I03, I04 | pom.xml, package.json, V1 migration; inicialização |
| RNF04, RNF07 | — | I01, I02, I15 | README, .env.example, .gitignore, docs/github.md |
| RNF05 | US06 | I10, I11 | frontend CSS e labels; revisão manual pendente |
| RNF06 | US07 | I13, I14, I16 | JUnit, smoke, docs/qualidade.md, workflow CI |
