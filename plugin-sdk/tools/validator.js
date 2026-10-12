#!/usr/bin/env node

/**
 * NuvioTV Plugin Validator
 * Validates manifest.json syntax and checks for required functions in JS scraper files.
 *
 * Usage:
 *   node tools/validator.js [path/to/manifest.json]
 */

const fs = require('fs');
const path = require('path');

const manifestPath = process.argv[2] || path.join(__dirname, '../template/manifest.json');

console.log(`\n\x1b[36m=== NuvioTV Plugin Validator ===\x1b[0m`);
console.log(`Checking: ${manifestPath}\n`);

if (!fs.existsSync(manifestPath)) {
  console.error(`\x1b[31mError: Manifest file not found at: ${manifestPath}\x1b[0m`);
  process.exit(1);
}

let manifest;
try {
  const content = fs.readFileSync(manifestPath, 'utf8');
  manifest = JSON.parse(content);
} catch (err) {
  console.error(`\x1b[31mInvalid JSON in manifest: ${err.message}\x1b[0m`);
  process.exit(1);
}

const baseDir = path.dirname(manifestPath);
let errors = 0;
let warnings = 0;

function reportError(msg) {
  console.error(`  \x1b[31m✖ [ERROR]\x1b[0m ${msg}`);
  errors++;
}

function reportWarning(msg) {
  console.warn(`  \x1b[33m▲ [WARN]\x1b[0m ${msg}`);
  warnings++;
}

function reportPass(msg) {
  console.log(`  \x1b[32m✔ [PASS]\x1b[0m ${msg}`);
}

// 1. Root manifest checks
if (!manifest.name) reportError("Manifest missing required 'name' field");
else reportPass(`Repository name: ${manifest.name}`);

if (!manifest.version) reportWarning("Manifest missing 'version' field");

if (!Array.isArray(manifest.scrapers) || manifest.scrapers.length === 0) {
  reportError("Manifest 'scrapers' must be a non-empty array");
} else {
  reportPass(`Found ${manifest.scrapers.length} scraper(s)`);
}

if (errors > 0) {
  console.error(`\nValidation failed with ${errors} error(s).`);
  process.exit(1);
}

// 2. Validate each scraper
manifest.scrapers.forEach((scraper, index) => {
  console.log(`\n\x1b[34m--- Scraper [${index + 1}/${manifest.scrapers.length}]: ${scraper.name || scraper.id || 'unnamed'} ---\x1b[0m`);

  if (!scraper.id) reportError(`Scraper at index ${index} missing 'id'`);
  if (!scraper.name) reportError(`Scraper at index ${index} missing 'name'`);
  if (!scraper.filename) reportError(`Scraper at index ${index} missing 'filename'`);

  if (!Array.isArray(scraper.supportedTypes) || scraper.supportedTypes.length === 0) {
    reportWarning(`Scraper '${scraper.id}' has no 'supportedTypes' specified (defaults to ['movie', 'tv'])`);
  } else {
    reportPass(`Supported types: ${scraper.supportedTypes.join(', ')}`);
  }

  if (scraper.hasHome === true) {
    reportPass(`hasHome is enabled (will show row on home screen)`);
  }

  const catalogs = scraper.catalogs || scraper.catalogues || [];
  if (catalogs.length > 0) {
    reportPass(`Declared ${catalogs.length} catalog(s): ${catalogs.map(c => c.name || c.id).join(', ')}`);
    catalogs.forEach((cat, cIdx) => {
      if (!cat.id) reportError(`Catalog at index ${cIdx} missing 'id'`);
      if (!cat.name) reportError(`Catalog at index ${cIdx} missing 'name'`);
    });
  }

  if (scraper.supportsSearch === true) {
    reportPass(`supportsSearch is enabled`);
  }

  // 3. Inspect JS implementation
  if (scraper.filename) {
    const jsPath = path.isAbsolute(scraper.filename) ? scraper.filename : path.resolve(baseDir, scraper.filename);
    if (!fs.existsSync(jsPath)) {
      reportError(`Scraper file not found: ${jsPath}`);
      return;
    }

    try {
      delete require.cache[require.resolve(jsPath)];
      const pluginModule = require(jsPath);

      if (typeof pluginModule.getStreams !== 'function') {
        reportError(`Scraper '${scraper.filename}' does not export 'getStreams' function`);
      } else {
        reportPass(`Exports 'getStreams(tmdbId, mediaType, season, episode, extra)'`);
      }

      if (catalogs.length > 0) {
        if (typeof pluginModule.getCatalog !== 'function') {
          reportError(`Scraper declares catalogs but does not export 'getCatalog(catalogId, mediaType, extra, page)'`);
        } else {
          reportPass(`Exports 'getCatalog(catalogId, mediaType, extra, page)'`);
        }
      }

      if (scraper.supportsSearch === true) {
        if (typeof pluginModule.search !== 'function') {
          reportError(`Scraper specifies supportsSearch=true but does not export 'search(query, mediaType, extra, page)'`);
        } else {
          reportPass(`Exports 'search(query, mediaType, extra, page)'`);
        }
      }
    } catch (loadErr) {
      reportError(`Failed to load or parse '${scraper.filename}': ${loadErr.message}`);
    }
  }
});

console.log(`\n\x1b[36m=== Validation Summary ===\x1b[0m`);
if (errors === 0) {
  console.log(`\x1b[32m✔ All checks passed! (${warnings} warning(s))\x1b[0m\n`);
  process.exit(0);
} else {
  console.error(`\x1b[31m✖ Validation failed with ${errors} error(s) and ${warnings} warning(s).\x1b[0m\n`);
  process.exit(1);
}
