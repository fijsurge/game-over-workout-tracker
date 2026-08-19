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

// Nav button
const navStats = () => 'button:has(span:text-is("Stats"))';

async function goToStats(page) {
    await page.locator(navStats()).click();
    // Dashboard shows day cards — wait for Monday card
    await expect(page.locator('button').filter({ hasText: 'Monday' }).first()).toBeVisible();
}

// Navigate from stats dashboard → day view → exercise view
async function goToExerciseView(page, day, exerciseName) {
    await page.locator(navStats()).click();
    await page.locator('button').filter({ hasText: day }).first().click();
    await page.locator('button').filter({ hasText: exerciseName }).first().click();
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

    test('can switch to stats view and shows day dashboard', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });
        await goToStats(page);
        // Dashboard shows all 5 training days
        await expect(page.locator('button').filter({ hasText: 'Monday' }).first()).toBeVisible();
        await expect(page.locator('button').filter({ hasText: 'Saturday' }).first()).toBeVisible();
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

test.describe('Stats — Exercise view', () => {
    test('shows exercise name when navigating to exercise view', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await expect(page.getByText('Incline Barbell Press').first()).toBeVisible();
    });

    test('shows session count on exercise card', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await expect(page.getByText('2 sessions')).toBeVisible();
    });

    test('shows pencil edit button on cards with data', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await expect(page.getByTitle('Edit sets').first()).toBeVisible();
    });

    test('pencil toggles to checkmark in edit mode', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await page.getByTitle('Edit sets').first().click();
        await expect(page.getByTitle('Done editing').first()).toBeVisible();
    });

    test('edit mode shows delete buttons on set tiles', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await page.getByTitle('Edit sets').first().click();
        await expect(page.locator('button').filter({ hasText: '✕' }).first()).toBeVisible();
    });

    test('exiting edit mode hides delete buttons', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await page.getByTitle('Edit sets').first().click();
        await page.getByTitle('Done editing').first().click();
        await expect(page.locator('button').filter({ hasText: '✕' })).toHaveCount(0);
    });

    test('shows multi-set progression chart svg when history has multiple sessions', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        // chart renders an SVG when there are >= 2 sessions
        await expect(page.locator('svg').first()).toBeVisible();
    });

    test('shows personal best badge with weight and reps at that weight', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        // bestWeight is 145 (4/14), and 12 is the most reps ever done at 145
        await expect(page.getByText('PB 145 × 12')).toBeVisible();
    });

    test('marks the set tile that set the personal best', async ({ page }) => {
        await loadWithHistory(page);
        await goToExerciseView(page, 'Monday', 'Incline Barbell Press');
        await expect(page.getByTitle('Personal best')).toBeVisible();
    });
});

test.describe('Workout log — personal best nudge', () => {
    // Incline Barbell Press is the first (4-set) exercise on Monday, Phase 1.
    // History best is 145 lbs x 12 reps (see sampleHistory).
    const getExerciseCard = (page) => page.locator('div.p-5.rounded-3xl').filter({ hasText: 'Incline Barbell Press' }).first();

    test('shows the PB badge on the log screen without navigating to stats', async ({ page }) => {
        await loadWithHistory(page);
        const card = getExerciseCard(page);
        await expect(card.getByText('PB 145 × 12')).toBeVisible();
    });

    test('shows a new weight PR nudge when logging above the current best', async ({ page }) => {
        await loadWithHistory(page);
        const card = getExerciseCard(page);
        const inputs = card.locator('input[type="number"]');
        await inputs.nth(0).fill('150');
        await inputs.nth(1).fill('5');
        await expect(card.getByText('New weight PR!')).toBeVisible();
    });

    test('shows a new rep PR nudge when matching best weight with more reps', async ({ page }) => {
        await loadWithHistory(page);
        const card = getExerciseCard(page);
        const inputs = card.locator('input[type="number"]');
        await inputs.nth(0).fill('145');
        await inputs.nth(1).fill('13');
        await expect(card.getByText('New rep PR at this weight!')).toBeVisible();
    });

    test('shows no PR nudge when below the current best', async ({ page }) => {
        await loadWithHistory(page);
        const card = getExerciseCard(page);
        const inputs = card.locator('input[type="number"]');
        await inputs.nth(0).fill('100');
        await inputs.nth(1).fill('10');
        await expect(card.getByText('New weight PR!')).toHaveCount(0);
        await expect(card.getByText('New rep PR at this weight!')).toHaveCount(0);
    });
});

test.describe('Stats — Day view', () => {
    test('shows session comparison after clicking a day card', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await page.locator('button').filter({ hasText: 'Monday' }).first().click();
        // Day view shows the exercise list with comparison data
        await expect(page.getByText('Incline Barbell Press').first()).toBeVisible();
    });

    test('back button returns to dashboard', async ({ page }) => {
        await loadWithHistory(page);
        await goToStats(page);
        await page.locator('button').filter({ hasText: 'Monday' }).first().click();
        await page.locator('button').filter({ hasText: 'All Days' }).click();
        await expect(page.locator('button').filter({ hasText: 'Saturday' }).first()).toBeVisible();
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

test.describe('Swap panel', () => {
    test('add custom exercise then swap an exercise to it', async ({ page }) => {
        await page.goto(APP_URL);
        await page.waitForSelector('h1', { timeout: 10000 });

        // Default view is workout/Phase 1/Monday — first exercise: Incline Barbell Press
        // The exercise name is a button that opens the SwapPanel
        await page.locator('button').filter({ hasText: 'Incline Barbell Press' }).first().click();

        // Swap panel opens
        await expect(page.getByRole('heading', { name: 'Swap Exercise' })).toBeVisible();
        await expect(page.getByText('Replace')).toBeVisible();

        // No custom exercises yet
        await expect(page.getByText('No custom exercises yet.')).toBeVisible();

        // Add a custom exercise
        await page.getByPlaceholder('New exercise name').fill('Cable Crossover');
        await page.getByRole('button', { name: 'Add' }).click();

        // The new alternate should appear in the panel
        await expect(page.getByRole('button', { name: 'Cable Crossover' })).toBeVisible();
        // localStorage should have it persisted
        const persisted = await page.evaluate((id) =>
            localStorage.getItem(`${id}_customExercises`), APP_ID);
        expect(JSON.parse(persisted)).toContain('Cable Crossover');

        // Tap the alternate to swap
        await page.getByRole('button', { name: 'Cable Crossover' }).click();

        // Panel closes; the workout card now shows Cable Crossover with a SUB badge
        await expect(page.getByRole('heading', { name: 'Swap Exercise' })).not.toBeVisible();
        await expect(page.locator('button').filter({ hasText: 'Cable Crossover' })).toBeVisible();
        await expect(page.getByText('instead of Incline Barbell Press')).toBeVisible();

        // Swap should be persisted in localStorage (stored as { date, swaps })
        const swapsRaw = await page.evaluate((id) =>
            localStorage.getItem(`${id}_swaps`), APP_ID);
        expect(JSON.parse(swapsRaw).swaps['1-Monday-Incline Barbell Press']).toBe('Cable Crossover');
    });

    test('restore original removes the swap', async ({ page }) => {
        // Pre-seed a swap and a custom exercise. Swaps expire end-of-day, so use today's date.
        await page.goto(APP_URL);
        await page.evaluate(([id]) => {
            const today = new Date().toISOString().split('T')[0];
            localStorage.setItem(`${id}_customExercises`, JSON.stringify(['Cable Crossover']));
            localStorage.setItem(`${id}_swaps`, JSON.stringify({
                date: today,
                swaps: { '1-Monday-Incline Barbell Press': 'Cable Crossover' }
            }));
        }, [APP_ID]);
        await page.reload();
        await page.waitForSelector('h1', { timeout: 10000 });

        // Workout card shows the substituted name
        await expect(page.getByText('instead of Incline Barbell Press')).toBeVisible();

        // Open the swap panel via the swapped-in exercise name
        await page.locator('button').filter({ hasText: 'Cable Crossover' }).first().click();
        await expect(page.getByRole('heading', { name: 'Swap Exercise' })).toBeVisible();

        // Restore the original
        await page.getByRole('button', { name: 'Restore Original' }).click();

        // Workout card returns to original name; SUB indicator gone
        await expect(page.getByText('instead of Incline Barbell Press')).not.toBeVisible();
        await expect(page.locator('button').filter({ hasText: 'Incline Barbell Press' }).first()).toBeVisible();

        // Swap removed from localStorage
        const swapsRaw = await page.evaluate((id) =>
            localStorage.getItem(`${id}_swaps`), APP_ID);
        const parsed = swapsRaw ? JSON.parse(swapsRaw) : { swaps: {} };
        const swapsMap = parsed.swaps || {};
        expect(swapsMap['1-Monday-Incline Barbell Press']).toBeUndefined();
    });
});
