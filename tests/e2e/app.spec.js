const { test, expect } = require('@playwright/test');
const path = require('path');

const APP_ID = 'game-over-tracker-v5';
const APP_URL = `file://${path.resolve(__dirname, '../../www/index.html')}`;

const sampleHistory = [
    { key: '1-Monday-Incline Barbell Press-s1', date: '2026-04-07', w: '135', r: '12' },
    { key: '1-Monday-Incline Barbell Press-s2', date: '2026-04-07', w: '140', r: '10' },
    { key: '1-Monday-Incline Barbell Press-s3', date: '2026-04-07', w: '135', r: '11' },
    { key: '1-Monday-Incline Barbell Press-s1', date: '2026-04-14', w: '145', r: '12' },
    { key: '1-Monday-Flat Barbell Press-s1',    date: '2026-04-07', w: '155', r: '12' },
];

// Nav buttons: "Log" (workout view) | "Stats" (stats view)
// Stats sub-tabs: "Summary" | "Chart" | "Table" | "Log" (data log)
const navStats = () => 'button:has(span:text-is("Stats"))';
const statsTabLog = () => 'button:text-is("Log")'; // stats tab — not the nav button

async function goToStats(page) {
    await page.locator(navStats()).click();
    await expect(page.getByText('Summary')).toBeVisible();
}

async function goToLogTab(page) {
    // The stats "Log" tab button comes before the nav "Log" button in DOM order
    await page.locator(statsTabLog()).first().click();
}

async function loadWithHistory(page, history = sampleHistory) {
    await page.goto(APP_URL);
    await page.evaluate(([id, hist]) => {
        localStorage.setItem(`${id}_history`, JSON.stringify(hist));
    }, [APP_ID, history]);
    await page.reload();
    await page.waitForSelector('h1', { timeout: 10000 });
}

test.describe('App shell', () => {
    test('loads and renders the header', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        const heading = await page.locator('h1').textContent();
        expect(heading).toContain('Game Over');
    });

    test('shows Log and Stats nav buttons', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        await expect(page.locator('button:has(span:text-is("Stats"))')).toBeVisible();
        await expect(page.locator('button:has(span:text-is("Log"))')).toBeVisible();
    });

    test('can switch to stats view', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        await goToStats(page);
        await expect(page.getByText('Summary')).toBeVisible();
        await expect(page.getByText('Chart')).toBeVisible();
        await expect(page.getByText('Table')).toBeVisible();
    });
});

test.describe('Phase selector', () => {
    test('shows three phase buttons', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        await expect(page.getByText('Phase 1')).toBeVisible();
        await expect(page.getByText('Phase 2')).toBeVisible();
        await expect(page.getByText('Phase 3')).toBeVisible();
    });

    test('can switch phases', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        await page.getByText('Phase 2').click();
        await expect(page.getByText('Phase 2')).toBeVisible();
    });
});

test.describe('Stats — Log view', () => {
    test('shows exercise cards when history exists', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await expect(page.getByText('Incline Barbell Press')).toBeVisible();
    });

    test('shows session count on exercise cards', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await expect(page.getByText('2 sessions')).toBeVisible();
    });

    test('shows pencil edit button on cards with data', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await expect(page.getByTitle('Edit sets').first()).toBeVisible();
    });

    test('pencil toggles to checkmark in edit mode', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await page.getByTitle('Edit sets').first().click();
        await expect(page.getByTitle('Done editing').first()).toBeVisible();
    });

    test('edit mode shows delete buttons on set tiles', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await page.getByTitle('Edit sets').first().click();
        await expect(page.locator('button').filter({ hasText: '✕' }).first()).toBeVisible();
    });

    test('exiting edit mode hides delete buttons', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await goToLogTab(page);
        await page.getByTitle('Edit sets').first().click();
        await page.getByTitle('Done editing').first().click();
        await expect(page.locator('button').filter({ hasText: '✕' })).toHaveCount(0);
    });
});

test.describe('Stats — Average view', () => {
    test('shows chart svg when history exists', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        // Default is Summary (A) view — Recharts renders an svg
        await expect(page.locator('svg').first()).toBeVisible();
    });
});

test.describe('Day selector', () => {
    test('shows expected day options', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        const daySelect = page.locator('select');
        await expect(daySelect).toBeVisible();
        const options = await daySelect.locator('option').allTextContents();
        expect(options).toContain('Monday');
        expect(options).toContain('Saturday');
    });
});
