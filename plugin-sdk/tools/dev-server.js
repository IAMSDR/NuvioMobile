#!/usr/bin/env node

/**
 * NuvioTV Local Plugin Dev Server
 * Serves your plugin directory over HTTP and automatically sets up
 * ADB port-forwarding (adb reverse) so you can test instantly on your TV.
 *
 * Usage:
 *   node tools/dev-server.js [path/to/manifest.json] [port]
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const os = require('os');

const manifestPath = process.argv[2] || path.join(__dirname, '../template/manifest.json');
const PORT = parseInt(process.argv[3], 10) || 8765;

const baseDir = path.dirname(path.resolve(manifestPath));

if (!fs.existsSync(manifestPath)) {
  console.error(`\x1b[31mError: Manifest not found at ${manifestPath}\x1b[0m`);
  process.exit(1);
}

// Get local LAN IPs
function getLocalIPs() {
  const interfaces = os.networkInterfaces();
  const ips = [];
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        ips.push(iface.address);
      }
    }
  }
  return ips;
}

const server = http.createServer((req, res) => {
  const reqPath = decodeURI(req.url.split('?')[0]);
  let safePath = path.normalize(reqPath).replace(/^(\.\.[\/\\])+/, '');
  if (safePath === '/' || safePath === '') safePath = '/manifest.json';

  const filePath = path.join(baseDir, safePath);

  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('404 Not Found: ' + reqPath);
    console.log(`\x1b[33m[404]\x1b[0m ${req.method} ${reqPath}`);
    return;
  }

  const ext = path.extname(filePath).toLowerCase();
  const mimeTypes = {
    '.json': 'application/json',
    '.js': 'application/javascript',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.svg': 'image/svg+xml'
  };

  const contentType = mimeTypes[ext] || 'application/octet-stream';

  res.writeHead(200, {
    'Content-Type': contentType,
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-cache, no-store, must-revalidate'
  });

  const stream = fs.createReadStream(filePath);
  stream.pipe(res);
  console.log(`\x1b[32m[200]\x1b[0m ${req.method} ${reqPath} (${contentType})`);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`\n\x1b[36m====================================================\x1b[0m`);
  console.log(`\x1b[1m  NuvioTV Plugin Dev Server Running on port ${PORT}\x1b[0m`);
  console.log(`\x1b[36m====================================================\x1b[0m\n`);
  console.log(`Serving directory: \x1b[34m${baseDir}\x1b[0m\n`);

  // Attempt ADB reverse
  let adbReversed = false;
  try {
    const adbPath = process.env.ADB_PATH || 'adb';
    execSync(`${adbPath} reverse tcp:${PORT} tcp:${PORT}`, { stdio: 'ignore' });
    adbReversed = true;
    console.log(`\x1b[32m✔ ADB reverse successful!\x1b[0m The device can connect to localhost directly.`);
  } catch (e) {
    // ADB might not be in PATH or device not connected
  }

  console.log(`\n\x1b[1mPlugin Repository URLs to add in NuvioTV:\x1b[0m`);
  if (adbReversed) {
    console.log(`  📱 \x1b[32mhttp://127.0.0.1:${PORT}/manifest.json\x1b[0m (Via ADB reverse)`);
  }
  const ips = getLocalIPs();
  ips.forEach(ip => {
    console.log(`  🌐 \x1b[34mhttp://${ip}:${PORT}/manifest.json\x1b[0m (LAN / WiFi)`);
  });

  console.log(`\nPress Ctrl+C to stop the server.\n`);
});
