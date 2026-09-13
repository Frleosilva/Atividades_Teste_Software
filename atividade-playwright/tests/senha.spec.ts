import { expect, test, type Page } from '@playwright/test';

async function cadastrar(page: Page, senha: string, confirmacao = senha) {
  await page.getByLabel('Nova senha', { exact: true }).fill(senha);
  await page.getByLabel('Confirmar senha', { exact: true }).fill(confirmacao);
  await page.getByRole('button', { name: 'Cadastrar senha' }).click();
}

test.describe('Regras e correções no cadastro de senha', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/senha'); });

  test('inicia sem mensagem e com campos protegidos', async ({ page }) => {
    await expect(page.locator('#resultado')).toBeHidden();
    for (const rotulo of ['Nova senha', 'Confirmar senha']) {
      await expect(page.getByLabel(rotulo, { exact: true })).toHaveAttribute('type', 'password');
      await expect(page.getByLabel(rotulo, { exact: true })).toBeEmpty();
    }
  });

  for (const tamanho of [7, 8, 20, 21]) {
    test(`verifica o limite com ${tamanho} caracteres`, async ({ page }) => {
      await cadastrar(page, 'Zr9' + 'm'.repeat(tamanho - 3));
      if (tamanho === 8 || tamanho === 20) {
        await expect(page.getByRole('status')).toHaveText('Senha cadastrada');
      } else {
        await expect(page.getByRole('alert')).toHaveText('Senha fora do padrão');
      }
    });
  }

  const restricoes = [
    ['sem maiúscula', 'montanha47'],
    ['sem minúscula', 'MONTANHA47'],
    ['sem algarismo', 'MontanhaAzul'],
    ['espaço interno', 'Monte 47Az'],
    ['espaço inicial', ' Monte47Az'],
    ['espaço final', 'Monte47Az '],
    ['campo vazio', ''],
  ];
  for (const [regra, entrada] of restricoes) {
    test(`impede cadastro: ${regra}`, async ({ page }) => {
      await cadastrar(page, entrada);
      await expect(page.getByRole('alert')).toHaveText('Senha fora do padrão');
      await expect(page.getByLabel('Nova senha', { exact: true })).toHaveValue(entrada);
    });
  }

  test('aceita símbolos e limpa os campos após o sucesso', async ({ page }) => {
    await cadastrar(page, 'Trilha#72!');
    await expect(page.getByRole('status')).toHaveText('Senha cadastrada');
    await expect(page.getByLabel('Nova senha', { exact: true })).toBeEmpty();
    await expect(page.getByLabel('Confirmar senha', { exact: true })).toBeEmpty();
  });

  test('permite corrigir uma confirmação que difere apenas na capitalização', async ({ page }) => {
    await cadastrar(page, 'Caminho82', 'caminho82');
    await expect(page.getByRole('alert')).toHaveText('As senhas não coincidem');
    await expect(page.getByLabel('Nova senha', { exact: true })).toHaveValue('Caminho82');
    await page.getByLabel('Confirmar senha', { exact: true }).fill('Caminho82');
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();
    await expect(page.getByRole('status')).toHaveText('Senha cadastrada');
    await expect(page.getByRole('alert')).toHaveCount(0);
  });

  test('exige o preenchimento da confirmação', async ({ page }) => {
    await cadastrar(page, 'Caminho82', '');
    await expect(page.getByRole('alert')).toHaveText('As senhas não coincidem');
  });

  test('prioriza o formato e atualiza a resposta nas próximas tentativas', async ({ page }) => {
    await cadastrar(page, 'curta', 'diferente');
    await expect(page.getByRole('alert')).toHaveText('Senha fora do padrão');
    await cadastrar(page, 'Horizonte56');
    await expect(page.getByRole('status')).toHaveText('Senha cadastrada');
    await cadastrar(page, '', '');
    await expect(page.getByRole('alert')).toHaveText('Senha fora do padrão');
    await expect(page.getByRole('status')).toHaveCount(0);
  });
});
