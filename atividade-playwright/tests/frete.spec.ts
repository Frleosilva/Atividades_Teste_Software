import { expect, test, type Page } from '@playwright/test';

async function consultar(page: Page, cep: string, valor: string) {
  await page.getByLabel('CEP', { exact: true }).fill(cep);
  await page.getByLabel('Valor do pedido').fill(valor);
  await page.getByRole('button', { name: 'Calcular frete' }).click();
}

test.describe('Simulação de entrega', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/frete'); });

  test('aguarda uma consulta antes de exibir o resultado', async ({ page }) => {
    await expect(page.locator('#resultado')).toBeHidden();
    await expect(page.getByLabel('CEP', { exact: true })).toBeEmpty();
  });

  test('recalcula a tarifa ao trocar o destino', async ({ page }) => {
    await consultar(page, '88015000', '75,50');
    await expect(page.getByRole('status')).toHaveText('Frete: R$ 15,00');
    await consultar(page, '30140071', '75,50');
    await expect(page.getByRole('status')).toHaveText('Frete: R$ 25,00');
  });

  for (const [cep, tarifa] of [['89010000', '15,00'], ['50030000', '25,00']]) {
    test(`verifica a fronteira da gratuidade para ${cep}`, async ({ page }) => {
      await consultar(page, cep, '199,99');
      await expect(page.getByRole('status')).toHaveText(`Frete: R$ ${tarifa}`);
      await consultar(page, cep, '200,00');
      await expect(page.getByRole('status')).toHaveText('Frete grátis');
      await consultar(page, cep, '200,01');
      await expect(page.getByRole('status')).toHaveText('Frete grátis');
    });
  }

  test('aceita ponto decimal e espaços nas extremidades', async ({ page }) => {
    await consultar(page, ' 88015000 ', ' 42.90 ');
    await expect(page.getByRole('status')).toHaveText('Frete: R$ 15,00');
  });

  for (const cep of ['', '1234567', '123456789', '8801A000', '88015-000']) {
    test(`recusa CEP ${JSON.stringify(cep)} mesmo em pedido acima do limite`, async ({ page }) => {
      await consultar(page, cep, '350');
      await expect(page.getByRole('alert')).toHaveText('Dados inválidos');
    });
  }

  for (const valor of ['', '0', '-25', 'vinte', '42,901', '1.000,00']) {
    test(`recusa valor ${JSON.stringify(valor)}`, async ({ page }) => {
      await consultar(page, '88015000', valor);
      await expect(page.getByRole('alert')).toHaveText('Dados inválidos');
    });
  }

  test('substitui sucesso por erro e permite corrigir o pedido', async ({ page }) => {
    await consultar(page, '30140071', '250');
    await expect(page.getByRole('status')).toHaveText('Frete grátis');
    await consultar(page, '30140071', '0');
    await expect(page.getByRole('alert')).toHaveText('Dados inválidos');
    await expect(page.getByRole('status')).toHaveCount(0);
    await consultar(page, '30140071', '0,01');
    await expect(page.getByRole('status')).toHaveText('Frete: R$ 25,00');
    await expect(page.getByRole('alert')).toHaveCount(0);
  });
});
