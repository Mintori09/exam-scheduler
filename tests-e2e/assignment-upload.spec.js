const path = require('path');
const { test, expect } = require('@playwright/test');

test('upload 2 excel files and create assignment with 1000 sessions', async ({ page }) => {
  test.setTimeout(1500000);

  const staffFile = path.resolve(__dirname, '..', 'CANBOCOITHI.XLSX');
  const roomFile = path.resolve(__dirname, '..', 'PHONGTHI.XLSX');

  await page.goto('/');

  await page.setInputFiles('input[name="staffFile"]', staffFile);
  await page.setInputFiles('input[name="roomFile"]', roomFile);
  await page.fill('input[name="sessionCount"]', '1000');

  await page.click('button[type="submit"]');

  const detailUrlPattern = /\/assignments\/[0-9a-fA-F-]+$/;
  const outcome = await Promise.race([
    page.waitForURL(detailUrlPattern, { timeout: 1400000 }).then(() => 'success'),
    page.locator('.error').waitFor({ state: 'visible', timeout: 1400000 }).then(() => 'error')
  ]);

  if (outcome === 'success') {
    await expect(page.locator('h1')).toContainText('Chi tiết lần chạy');
    await expect(page.getByText('Trạng thái: SUCCESS')).toBeVisible({ timeout: 1400000 });
    await expect(page.getByText('Tải DANHSACH_PHANCONG.xlsx')).toBeVisible();
    await expect(page.getByText('Tải DANHSACH_GIAMSAT.xlsx')).toBeVisible();
    return;
  }

  const errorText = await page.locator('.error').innerText();
  throw new Error(`Create assignment returned error page: ${errorText}`);
});
