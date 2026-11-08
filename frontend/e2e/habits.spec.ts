import AxeBuilder from "@axe-core/playwright";
import { randomUUID } from "node:crypto";
import { expect, test, type Page } from "@playwright/test";

async function checkAccessibility(page: Page) {
  const results = await new AxeBuilder({ page })
    .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"])
    .analyze();
  expect(results.violations).toEqual([]);
}

const password = "Senha-teste-123!";

async function register(page: Page, email: string) {
  await page.getByRole("button", { name: "Cadastre-se" }).click();
  await checkAccessibility(page);
  await page.getByLabel("Nome", { exact: true }).fill("Pessoa Teste");
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill(password);
  await page.getByRole("button", { name: "Criar conta" }).click();
  await expect(page.getByRole("status")).toContainText("Conta criada!");
}

async function login(page: Page, email: string) {
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill(password);
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(
    page.getByRole("heading", { name: "Seus hábitos" }),
  ).toBeVisible();
}

test("usuário acompanha hábito e histórico isolado após nova sessão", async ({
  page,
}) => {
  const first = `e2e-${randomUUID()}@example.com`;
  const second = `e2e-${randomUUID()}@example.com`;
  await page.goto("/");
  await checkAccessibility(page);
  await register(page, first);
  await login(page, first);
  await checkAccessibility(page);

  await page.getByLabel("Nome do hábito").fill("Leitura E2E");
  await page.getByLabel("Descrição").fill("Ler diariamente");
  await page.getByRole("button", { name: "Adicionar hábito" }).click();
  const initial = page.locator("article").filter({ hasText: "Leitura E2E" });
  await expect(initial).toBeVisible();
  await initial.getByRole("button", { name: "Editar" }).click();
  await page.getByLabel("Nome do hábito").fill("Leitura editada E2E");
  await page.getByRole("button", { name: "Salvar alterações" }).click();
  await page
    .getByRole("button", { name: "Consultar Leitura editada E2E" })
    .click();
  await page.getByLabel("Data da realização").fill("2020-01-01");
  await page.getByLabel("De", { exact: true }).fill("2020-01-01");
  await page.getByLabel("Até", { exact: true }).fill("2020-12-31");
  await page.getByRole("button", { name: "Marcar ✓", exact: true }).click();
  await expect(page.getByText("1 dia concluído no período")).toBeVisible();
  await expect(page.getByText("01/01/2020")).toBeVisible();
  await checkAccessibility(page);

  page.once("dialog", (dialog) => dialog.accept());
  await page
    .locator("article")
    .filter({ hasText: "Leitura editada E2E" })
    .getByRole("button", { name: "Arquivar" })
    .click();
  await expect(
    page.getByRole("button", { name: "Consultar Leitura editada E2E" }),
  ).toHaveCount(0);
  await page.reload();
  await expect(
    page.getByRole("heading", { name: "Acesse sua conta" }),
  ).toBeVisible();
  await login(page, first);
  await page.getByLabel("Nome do hábito").fill("Rascunho anterior");
  await page.getByLabel("Descrição").fill("Descrição anterior");
  await page.getByRole("button", { name: "Adicionar hábito" }).click();
  await page
    .locator("article")
    .filter({ hasText: "Rascunho anterior" })
    .getByRole("button", { name: "Editar" })
    .click();
  await page.getByLabel("Nome do hábito").fill("Alteração não salva");
  await page.getByRole("button", { name: "Ver arquivados" }).click();
  await page
    .getByRole("button", { name: "Consultar Leitura editada E2E" })
    .click();
  await page.getByLabel("De", { exact: true }).fill("2020-01-01");
  await page.getByLabel("Até", { exact: true }).fill("2020-12-31");
  await page.getByRole("button", { name: "Consultar período" }).click();
  await expect(page.getByText("1 dia concluído no período")).toBeVisible();
  await checkAccessibility(page);
  await page.getByRole("button", { name: "Sair" }).click();

  await expect(page.getByLabel("E-mail")).toHaveValue("");
  await register(page, second);
  await login(page, second);
  await expect(
    page.getByRole("heading", { name: "Novo hábito" }),
  ).toBeVisible();
  await expect(page.getByLabel("Nome do hábito")).toHaveValue("");
  await expect(page.getByLabel("Descrição")).toHaveValue("");
  await expect(
    page.getByRole("heading", { name: "Hábitos ativos" }),
  ).toBeVisible();
  await page.getByLabel("Nome do hábito").fill("Hábito da segunda conta");
  await page.getByRole("button", { name: "Adicionar hábito" }).click();
  await page
    .getByRole("button", { name: "Consultar Hábito da segunda conta" })
    .click();
  const today = await page.evaluate(() =>
    new Intl.DateTimeFormat("en-CA", {
      timeZone: "America/Recife",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(new Date()),
  );
  await expect(page.getByLabel("De", { exact: true })).toHaveValue(
    today.slice(0, 8) + "01",
  );
  await page.getByRole("button", { name: "Ver arquivados" }).click();
  await expect(
    page.getByRole("button", { name: "Consultar Leitura editada E2E" }),
  ).toHaveCount(0);
});

test("tela de entrada é utilizável em largura de celular", async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 });
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Acesse sua conta" }),
  ).toBeVisible();
  await expect(page.getByLabel("E-mail")).toBeVisible();
  await expect(page.getByRole("button", { name: "Entrar" })).toBeVisible();
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth),
  ).toBeLessThanOrEqual(375);
});
