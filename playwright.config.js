const { defineConfig, devices } = require('@playwright/test');
const path = require('path');

module.exports = defineConfig({
    testDir: './tests/e2e',
    timeout: 30000,
    retries: 0,
    use: {
        baseURL: `file://${path.resolve(__dirname, 'www')}/`,
        headless: true,
    },
    projects: [
        { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    ],
    webServer: undefined, // using file:// — no server needed
});
