import { randomUUID } from "node:crypto";
import { expect, test, type Page } from "@playwright/test";

const password = "Senha-teste-123!";

async function register(page: Page, email: string) {
  await page.getByRole("button", { name: "Cadastre-se" }).click();
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
  await register(page, first);
  await login(page, first);

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
  await page.getByRole("button", { name: "Ver arquivados" }).click();
  await page
    .getByRole("button", { name: "Consultar Leitura editada E2E" })
    .click();
  await page.getByLabel("De", { exact: true }).fill("2020-01-01");
  await page.getByLabel("Até", { exact: true }).fill("2020-12-31");
  await page.getByRole("button", { name: "Consultar período" }).click();
  await expect(page.getByText("1 dia concluído no período")).toBeVisible();
  await page.getByRole("button", { name: "Sair" }).click();

  await page.reload();
  await register(page, second);
  await login(page, second);
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
