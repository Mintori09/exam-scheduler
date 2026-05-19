// @ts-check
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests-e2e',
  timeout: 360000,
  expect: {
    timeout: 20000
  },
  use: {
    baseURL: 'http://127.0.0.1:8080',
    trace: 'on-first-retry'
  },
  reporter: [['list']]
});
