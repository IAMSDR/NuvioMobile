#!/usr/bin/env node

/**
 * NuvioTV Plugin Test Simulator
 * Executes plugin functions locally to verify live responses, search, catalogs, and streams.
 *
 * Usage:
 *   node tools/simulator.js [path/to/manifest.json]
 */

const fs = require('fs');
const path = require('path');

const manifestPath = process.argv[2] || path.join(__dirname, '../template/manifest.json');

console.log(`\n\x1b[36m=== NuvioTV Plugin Simulator ===\x1b[0m`);
console.log(`Loading manifest: ${manifestPath}\n`);

if (!fs.existsSync(manifestPath)) {
  console.error(`\x1b[31mError: Manifest not found at: ${manifestPath}\x1b[0m`);
  process.exit(1);
}

const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
const baseDir = path.dirname(manifestPath);

async function runSimulation() {
  let passedTests = 0;
  let failedTests = 0;

  for (const scraper of manifest.scrapers) {
    console.log(`\x1b[35m▶ Testing Scraper: ${scraper.name} (${scraper.id})\x1b[0m`);
    const jsPath = path.isAbsolute(scraper.filename) ? scraper.filename : path.resolve(baseDir, scraper.filename);

    if (!fs.existsSync(jsPath)) {
      console.error(`  \x1b[31m✖ Scraper file not found: ${jsPath}\x1b[0m`);
      failedTests++;
      continue;
    }

    // Initialize Nuvio runtime mock globals
    globalThis.SCRAPER_ID = scraper.id;
    globalThis.SCRAPER_SETTINGS = {
      api_key: 'demo_user_key_999',
      stream_quality: '1080p',
      include_adult: false,
      enable_backup_cdn: true
    };
    globalThis.TMDB_API_KEY = 'mock_tmdb_api_key';

    let plugin;
    try {
      delete require.cache[require.resolve(jsPath)];
      plugin = require(jsPath);
    } catch (e) {
      console.error(`  \x1b[31m✖ Error importing plugin: ${e.message}\x1b[0m`);
      failedTests++;
      continue;
    }

    // 0. Test Settings
    if (typeof plugin.onSettings === 'function' || scraper.hasSettings === true) {
      process.stdout.write(`  Testing onSettings()... `);
      if (typeof plugin.onSettings !== 'function') {
        console.log(`\x1b[31mFAILED (hasSettings=true but onSettings not exported)\x1b[0m`);
        failedTests++;
      } else {
        try {
          const startTime = Date.now();
          const layout = await plugin.onSettings();
          const duration = Date.now() - startTime;

          if (!Array.isArray(layout)) {
            console.log(`\x1b[31mFAILED (onSettings returned non-array)\x1b[0m`);
            failedTests++;
          } else {
            console.log(`\x1b[32mOK (${layout.length} field(s), ${duration}ms)\x1b[0m`);
            passedTests++;
            layout.forEach(field => {
              if (field.type === 'header' || field.type === 'info') {
                console.log(`    [UI ${field.type.toUpperCase()}] "${field.label || ''}"`);
              } else {
                console.log(`    [CONFIG] ${field.key} (${field.type}) -> "${field.label}" [default: ${JSON.stringify(field.defaultValue)}]`);
              }
            });
            console.log(`    (Runtime test executing with mock SCRAPER_SETTINGS: ${JSON.stringify(globalThis.SCRAPER_SETTINGS)})`);
          }
        } catch (settingsErr) {
          console.log(`\x1b[31mERROR: ${settingsErr.message}\x1b[0m`);
          failedTests++;
        }
      }
    }

    const catalogs = scraper.catalogs || scraper.catalogues || [];

    // 1. Test Catalogs
    if (typeof plugin.getCatalog === 'function') {
      if (catalogs.length === 0) {
        console.log(`  \x1b[33m▲ getCatalog is exported but no catalogs defined in manifest\x1b[0m`);
      }
      for (const cat of catalogs) {
        process.stdout.write(`  Testing getCatalog('${cat.id}', '${cat.type || 'movie'}', '${cat.extra || ''}', 1)... `);
        try {
          const startTime = Date.now();
          const items = await plugin.getCatalog(cat.id, cat.type || 'movie', cat.extra || '', 1);
          const duration = Date.now() - startTime;

          if (!Array.isArray(items)) {
            console.log(`\x1b[31mFAILED (returned non-array)\x1b[0m`);
            failedTests++;
          } else {
            console.log(`\x1b[32mOK (${items.length} items, ${duration}ms)\x1b[0m`);
            passedTests++;

            // Check first item structure
            if (items.length > 0) {
              const first = items[0];
              const isLive = String(first.type).toLowerCase() === 'live' || String(first.type).toLowerCase() === 'channel';
              if (isLive && !first.streamUrl) {
                console.log(`    \x1b[33m▲ Warning: Live item '${first.title}' has no streamUrl provided\x1b[0m`);
              }
              console.log(`    Sample: [${first.type}] "${first.title || first.name}" (id: ${first.id})`);
            }
          }
        } catch (catErr) {
          console.log(`\x1b[31mERROR: ${catErr.message}\x1b[0m`);
          failedTests++;
        }
      }
    }

    // 2. Test Search
    if (typeof plugin.search === 'function') {
      process.stdout.write(`  Testing search('news', 'live', '', 1)... `);
      try {
        const startTime = Date.now();
        const results = await plugin.search('news', 'live', '', 1);
        const duration = Date.now() - startTime;

        if (!Array.isArray(results)) {
          console.log(`\x1b[31mFAILED (returned non-array)\x1b[0m`);
          failedTests++;
        } else {
          console.log(`\x1b[32mOK (${results.length} results, ${duration}ms)\x1b[0m`);
          passedTests++;
          if (results.length > 0) {
            console.log(`    Sample: "${results[0].title || results[0].name}" (id: ${results[0].id})`);
          }
        }
      } catch (searchErr) {
        console.log(`\x1b[31mERROR: ${searchErr.message}\x1b[0m`);
        failedTests++;
      }
    }

    // 3. Test getStreams
    if (typeof plugin.getStreams === 'function') {
      process.stdout.write(`  Testing getStreams('tt0137523', 'movie', null, null, 'sample_extra')... `);
      try {
        const startTime = Date.now();
        const streams = await plugin.getStreams('tt0137523', 'movie', null, null, 'sample_extra');
        const duration = Date.now() - startTime;

        if (!Array.isArray(streams)) {
          console.log(`\x1b[31mFAILED (returned non-array)\x1b[0m`);
          failedTests++;
        } else {
          console.log(`\x1b[32mOK (${streams.length} stream(s), ${duration}ms)\x1b[0m`);
          passedTests++;
          if (streams.length > 0) {
            console.log(`    Sample stream: [${streams[0].quality || 'auto'}] "${streams[0].title || streams[0].name || streams[0].url}"`);
          }
        }
      } catch (streamErr) {
        console.log(`\x1b[31mERROR: ${streamErr.message}\x1b[0m`);
        failedTests++;
      }
    }
  }

  console.log(`\n\x1b[36m=== Simulation Summary ===\x1b[0m`);
  console.log(`Passed: \x1b[32m${passedTests}\x1b[0m | Failed: \x1b[31m${failedTests}\x1b[0m\n`);

  if (failedTests > 0) {
    process.exit(1);
  }
}

runSimulation().catch(err => {
  console.error(`Fatal simulation error:`, err);
  process.exit(1);
});
