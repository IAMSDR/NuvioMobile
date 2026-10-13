# Nuvio Plugin SDK

Comprehensive Developer SDK, manifest specification, runtime API reference, testing tools, and boilerplate templates for building Nuvio plugins.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Quickstart](#-quickstart)
- [Directory Structure](#-directory-structure)
- [Manifest Specification (`manifest.json`)](#-manifest-specification-manifestjson)
- [User Settings System (Custom Plugin Configuration)](#-user-settings-system-custom-plugin-configuration)
  - [How User Settings Work](#how-user-settings-work)
  - [Settings Schema & Controls](#settings-schema--controls)
  - [Accessing Settings in JavaScript (`SCRAPER_SETTINGS`)](#accessing-settings-in-javascript-scraper_settings)
- [JavaScript Plugin API](#-javascript-plugin-api)
  - [1. `onSettings()`](#1-onsettings)
  - [2. `getStreams(tmdbId, mediaType, season, episode, extra)`](#2-getstreamstmdbid-mediatype-season-episode-extra)
  - [3. `getCatalog(catalogId, mediaType, extra, page)`](#3-getcatalogcatalogid-mediatype-extra-page)
  - [4. `search(query, mediaType, extra, page)`](#4-searchquery-mediatype-extra-page)
- [Runtime Environment & Built-in Globals](#-runtime-environment--built-in-globals)
  - [Globals & Context](#globals--context)
  - [Networking (`fetch`)](#networking-fetch)
  - [HTML Parsing & DOM (`cheerio`)](#html-parsing--dom-cheerio)
  - [Cryptography (`CryptoJS` & Web Crypto)](#cryptography-cryptojs--web-crypto)
  - [URL Handling (`URL` & `URLSearchParams`)](#url-handling-url--urlsearchparams)
  - [Binary & Encodings (`atob`, `btoa`, `TextEncoder`, `TextDecoder`)](#binary--encodings-atob-btoa-textencoder-textdecoder)
  - [Timers (`setTimeout`, `setInterval`)](#timers-settimeout-setinterval)
  - [Console Logging](#console-logging)
- [Content Types & Playback Modes](#-content-types--playback-modes)
- [Developer Tooling & Testing](#-developer-tooling--testing)
  - [Validator CLI](#validator-cli)
  - [Simulator CLI](#simulator-cli)
  - [Local Dev Server with ADB Reverse](#local-dev-server-with-adb-reverse)

---

## 🌟 Overview

Nuvio plugins are lightweight JavaScript scrapers executed inside an isolated, high-performance **QuickJS** runtime engine. Plugins enable:
- **Custom Catalogs / Shelves:** Expose live IPTV channels, curated lists, and on-demand video categories.
- **In-Plugin Search:** Provide instant search across live and on-demand content.
- **Stream Scraping:** Resolve direct playable streams (HLS/m3u8, MP4, MKV, torrents) with custom HTTP headers and subtitles.
- **User Settings:** Present customizable native UI forms (API keys, resolution pickers, content filters, toggles) that store preferences persistently on the device.
- **Home Screen Integration:** Render dedicated plugin cards directly beneath the Home screen Hero banner.

---

## 🚀 Quickstart

### 1. Test the starter template
Run the simulator to verify that settings, catalogs, search, and stream resolution work locally in Node.js:
```bash
cd plugin-sdk
npm test
# or: node tools/simulator.js template/manifest.json
```

### 2. Validate manifest and exports
Verify your repository structure and function signatures against the Nuvio schema:
```bash
npm run validate
# or: node tools/validator.js path/to/your/manifest.json
```

### 3. Run the live local dev server with ADB Reverse
Serve your plugin over HTTP with automatic ADB port forwarding (`adb reverse`) to test on a physical Android phone, TV, or emulator:
```bash
npm run serve
# or: node tools/dev-server.js template/manifest.json 8765
```

In Nuvio, navigate to **Settings -> Content & Discovery -> Plugins -> Add repository**, and enter:
```
http://127.0.0.1:8765/manifest.json
```

---

## 📁 Directory Structure

```
plugin-sdk/
├── template/                      # Starter plugin boilerplate
│   ├── manifest.json              # Repository manifest (with catalogs, settings & home row)
│   └── sample-plugin.js           # Reference implementation of all plugin APIs
├── examples/
│   ├── live-iptv-plugin/          # Working Live IPTV channels plugin with search
│   └── catalog-vod-plugin/        # Working VOD movies/series catalog + search plugin
├── tools/
│   ├── validator.js               # Manifest, exports, and settings schema validator
│   ├── simulator.js               # Mock runner that tests plugins with mock SCRAPER_SETTINGS
│   └── dev-server.js              # HTTP server with auto ADB reverse forwarding
└── package.json
```

---

## 📜 Manifest Specification (`manifest.json`)

Repositories host a single `manifest.json` file that defines metadata and an array of scrapers.

```json
{
  "name": "My Custom Plugin Repo",
  "version": "1.0.0",
  "description": "Custom Nuvio Plugin Repository",
  "author": "Community Developer",
  "scrapers": [
    {
      "id": "starter-plugin",
      "name": "Starter Plugin",
      "description": "Template demonstrating live channels, on-demand catalogs, settings, and search.",
      "version": "1.0.0",
      "filename": "sample-plugin.js",
      "logo": "https://example.com/logo.png",
      "supportedTypes": ["movie", "tv", "live"],
      "hasHome": true,
      "supportsSearch": true,
      "hasSettings": true,
      "catalogs": [
        {
          "id": "live-channels",
          "name": "Featured Live TV",
          "type": "live",
          "extra": "featured"
        },
        {
          "id": "curated-movies",
          "name": "Trending Movies",
          "type": "movie",
          "extra": "trending"
        }
      ]
    }
  ]
}
```

### Root Repository Fields
| Field | Type | Required | Description |
|---|---|---|---|
| `name` | string | **Yes** | Display name of the repository. |
| `version` | string | **Yes** | Repository version string (e.g. `"1.0.0"`). |
| `description` | string | No | Short description of the repository. |
| `author` | string | No | Author or organization name. |
| `scrapers` | array | **Yes** | Non-empty array of scraper objects. |

### Scraper Manifest Fields
| Field | Type | Required | Default | Description |
|---|---|---|---|---|
| `id` | string | **Yes** | — | Unique alphanumeric identifier (e.g. `"my-plugin"`). |
| `name` | string | **Yes** | — | Display name shown in Nuvio. |
| `filename` | string | **Yes** | — | Relative filename (e.g. `"my-plugin.js"`) or absolute URL to JavaScript source. |
| `version` | string | **Yes** | `"1.0.0"` | Scraper version string. |
| `description` | string | No | `""` | Description shown in the plugin settings list. |
| `logo` | string | No | `null` | URL to a square or horizontal icon/logo image. |
| `supportedTypes` | array | No | `["movie", "tv"]` | Array of supported media types: `["movie", "tv", "live"]`. (Aliases `"channel"`, `"iptv"`, `"stream"` auto-normalize to `"live"`). |
| `enabled` | boolean | No | `true` | Whether the scraper is enabled by default upon installation. |
| `hasSettings` | boolean | No | `false` | When `true`, enables a gear icon in **Settings -> Plugins** and calls `onSettings()` to generate a custom configuration dialog. |
| `hasHome` | boolean | No | `false` | When `true`, displays a card in the **Plugins** row directly below the Hero section on the Home screen. |
| `supportsSearch` | boolean | No | `false` | When `true`, displays a search bar on the plugin's dedicated browser page. |
| `catalogs` | array | No | `[]` | Array of catalog rows displayed on the plugin's dedicated page. |
| `contentLanguage` | array | No | `[]` | Supported ISO language codes (e.g. `["en", "es"]`). |
| `supportedPlatforms` | array | No | `null` | Optional platform filter (e.g. `["android"]`). |
| `formats` | array | No | `null` | Optional list of stream formats (e.g. `["hls", "mp4"]`). |

### Catalog Declaration Fields (`catalogs` / `catalogues`)
| Field | Type | Required | Default | Description |
|---|---|---|---|---|
| `id` | string | **Yes** | — | Unique identifier passed to `getCatalog(catalogId, ...)`. |
| `name` | string | **Yes** | — | Title of the horizontal shelf in the UI. |
| `type` | string | No | `"movie"` | Content type: `"live"`, `"movie"`, or `"tv"`. |
| `extra` | string | No | `""` | Custom parameter forwarded to `getCatalog`. |

---

## ⚙️ User Settings System (Custom Plugin Configuration)

Nuvio allows plugins to expose dynamic, customizable configuration dialogs without compiling any native Android code.

### How User Settings Work
1. Specify `"hasSettings": true` in your scraper's `manifest.json`.
2. Export an async function `onSettings()` in your JavaScript scraper that returns an array of UI control definitions.
3. In Nuvio, when a user taps the **Settings (gear)** icon next to the scraper in **Settings -> Plugins**, Nuvio executes `onSettings()` and renders a native Material 3 / Nuvio styled dialog.
4. When the user changes fields and taps **Save**, values are stored persistently on the device.
5. On every subsequent call (`getStreams`, `getCatalog`, `search`), Nuvio automatically injects the user's saved settings as a parsed JavaScript object into `globalThis.SCRAPER_SETTINGS`.

---

### Settings Schema & Controls

`onSettings()` returns an array of field objects. Nuvio supports 5 distinct control types:

#### 1. `header` (Section Title)
Used to divide settings into logical sections.
```javascript
{
  type: 'header',
  label: 'Account & Credentials'
}
```

#### 2. `info` (Help / Explanatory Note)
Renders secondary text providing instructions, disclaimer, or notes.
```javascript
{
  type: 'info',
  label: 'An API key provides access to premium 4K debrid mirrors.'
}
```

#### 3. `text` (Text Input)
Renders a text input box. Supports masking for passwords or sensitive API tokens.
```javascript
{
  type: 'text',
  key: 'api_key',                 // Key name in SCRAPER_SETTINGS
  label: 'Provider API Key',      // Field title
  placeholder: 'e.g. live_sec_123',// Optional placeholder
  description: 'Enter your key from provider.com', // Optional helper text
  isPassword: true                // If true, masks characters
}
```

#### 4. `select` (Dropdown Picker)
Renders a dropdown menu allowing the user to select from a predefined list of options.
```javascript
{
  type: 'select',
  key: 'stream_quality',          // Key name in SCRAPER_SETTINGS
  label: 'Preferred Quality',     // Field title
  description: 'Select default video stream resolution',
  defaultValue: '1080p',          // Initial selected value
  options: [
    { label: '4K Ultra HD', value: '4k' },
    { label: '1080p Full HD', value: '1080p' },
    { label: '720p HD', value: '720p' },
    { label: 'Auto (Best Available)', value: 'auto' }
  ]
}
```

#### 5. `toggle` (Boolean Switch)
Renders an on/off switch for enabling or disabling features.
```javascript
{
  type: 'toggle',
  key: 'include_adult',           // Key name in SCRAPER_SETTINGS
  label: 'Include Adult Content', // Field title
  description: 'Show 18+ titles in catalog results',
  defaultValue: false             // Initial boolean state
}
```

---

### Accessing Settings in JavaScript (`SCRAPER_SETTINGS`)

When any scraper function runs (`getStreams`, `getCatalog`, or `search`), the runtime injects `globalThis.SCRAPER_SETTINGS`. You can read saved values directly:

```javascript
module.exports.getStreams = async function(tmdbId, mediaType, season, episode, extra) {
  // Safe fallback if user has not yet configured settings
  const settings = globalThis.SCRAPER_SETTINGS || {};

  const apiKey = settings.api_key || "";
  const preferredQuality = settings.stream_quality || "1080p";
  const useBackup = settings.enable_backup !== false; // boolean

  // Use the settings in your HTTP requests or stream resolution
  const response = await fetch(`https://api.example.com/streams/${tmdbId}`, {
    headers: {
      'Authorization': apiKey ? `Bearer ${apiKey}` : '',
      'X-Preferred-Resolution': preferredQuality
    }
  });
  // ...
};
```

---

## 💻 JavaScript Plugin API

Plugins must export functions via `module.exports` (CommonJS) or declare them on `globalThis`.

### 1. `onSettings()`
Invoked when opening the scraper's settings dialog.

**Signature:**
```javascript
module.exports.onSettings = async function(): Promise<Array<SettingField>>
```

**Example:**
```javascript
module.exports.onSettings = async function() {
  return [
    { type: 'header', label: 'Credentials' },
    {
      type: 'text',
      key: 'api_token',
      label: 'Personal API Token',
      placeholder: 'Enter token',
      isPassword: true
    },
    {
      type: 'select',
      key: 'cdn_region',
      label: 'CDN Region',
      defaultValue: 'us',
      options: [
        { label: 'United States', value: 'us' },
        { label: 'Europe', value: 'eu' },
        { label: 'Asia', value: 'asia' }
      ]
    },
    {
      type: 'toggle',
      key: 'hls_only',
      label: 'Prefer HLS (.m3u8)',
      defaultValue: true
    }
  ];
};
```

---

### 2. `getStreams(tmdbId, mediaType, season, episode, extra)`
Resolves playable video links for a specific movie or series episode.

**Signature:**
```javascript
module.exports.getStreams = async function(
  tmdbId: string,
  mediaType: 'movie' | 'tv',
  season: number | null,
  episode: number | null,
  extra: string
): Promise<Array<StreamResult>>
```

**Parameters:**
- `tmdbId`: TMDb ID (e.g. `'550'`) or IMDb ID (e.g. `'tt0137523'`).
- `mediaType`: `'movie'` or `'tv'`.
- `season`: Season number (for TV series), or `null`/`undefined` for movies.
- `episode`: Episode number (for TV series), or `null`/`undefined` for movies.
- `extra`: String parameter forwarded if opened from a plugin catalog item.

**Return Array Item Schema (`StreamResult`):**
| Field | Type | Required | Description |
|---|---|---|---|
| `url` | string | **Yes** | Direct video stream URL (HLS `.m3u8`, MP4, MKV, etc.). |
| `title` | string | No | Primary title displayed in the streams list (e.g. `"Fast CDN 1080p"`). |
| `name` | string | No | Optional badge / provider name (e.g. `"Torrentio"` or `"MyPlugin"`). |
| `quality` | string | No | Resolution badge (e.g. `"4K"`, `"1080p"`, `"720p"`, `"480p"`, `"auto"`). |
| `size` | string | No | File size representation (e.g. `"2.4 GB"`). |
| `language` | string | No | Audio language tag (e.g. `"en"`, `"Multi"`). |
| `provider` | string | No | Host or provider source name. |
| `headers` | object | No | Key-value dictionary of HTTP headers required for playback (e.g. `User-Agent`, `Referer`, `Authorization`, `Cookie`). |
| `subtitles` | array | No | List of external subtitle tracks (see below). |
| `seeders` | number | No | Seeder count (for P2P/torrent links). |
| `peers` | number | No | Peer count (for P2P/torrent links). |
| `infoHash` | string | No | Torrent infoHash string. |

#### Subtitle Object Schema
```javascript
{
  url: "https://example.com/subs/en.vtt",
  language: "en",             // ISO code or language name
  name: "English [SDH]",      // Display label
  headers: { "Referer": "..." } // Optional download headers
}
```

**Example:**
```javascript
module.exports.getStreams = async function(tmdbId, mediaType, season, episode, extra) {
  const quality = (globalThis.SCRAPER_SETTINGS && globalThis.SCRAPER_SETTINGS.stream_quality) || '1080p';

  return [
    {
      title: `Server Alpha ${quality}`,
      name: 'Direct CDN',
      quality: quality,
      url: 'https://cdn.example.com/video.m3u8',
      size: '1.8 GB',
      headers: {
        'User-Agent': 'Mozilla/5.0',
        'Referer': 'https://example.com/'
      },
      subtitles: [
        {
          url: 'https://cdn.example.com/sub_en.vtt',
          language: 'English',
          name: 'English'
        }
      ]
    }
  ];
};
```

---

### 3. `getCatalog(catalogId, mediaType, extra, page)`
Called when opening the plugin's dedicated browser page to populate each declared catalog row.

**Signature:**
```javascript
module.exports.getCatalog = async function(
  catalogId: string,
  mediaType: 'live' | 'movie' | 'tv',
  extra: string,
  page: number
): Promise<Array<MediaItem>>
```

**Parameters:**
- `catalogId`: The `id` defined in the manifest catalog entry.
- `mediaType`: Content type declared in the manifest catalog entry (`'live'`, `'movie'`, or `'tv'`).
- `extra`: String parameter from the manifest catalog entry.
- `page`: 1-based page index (e.g. `1`, `2`).

**Return Array Item Schema (`MediaItem`):**
| Field | Type | Required | Description |
|---|---|---|---|
| `id` | string | **Yes** | Unique identifier (e.g. IMDb ID `'tt0137523'`, TMDb ID, or custom channel ID). |
| `title` | string | No | Display title (falls back to `name` or `id`). |
| `name` | string | No | Alternate title property. |
| `type` | string | No | Content type: `'live'`, `'movie'`, or `'tv'`. |
| `poster` | string | No | URL to portrait poster image. |
| `backdrop` | string | No | URL to landscape fanart/backdrop image (also accepts `background`). |
| `logo` | string | No | URL to transparent PNG logo. |
| `description` | string | No | Synopsis or overview text. |
| `releaseInfo` | string | No | Release year or broadcast label (also accepts `year`). |
| `imdbRating` | number | No | IMDb score float (e.g. `8.8`). |
| `genres` | array | No | Array of genre strings (e.g. `["Action", "Thriller"]`). |
| `streamUrl` | string | No | **Required for `'live'` items.** Direct stream URL triggered upon click. |
| `extra` | string | No | Context string forwarded to `getStreams`. |
| `headers` | object | No | HTTP headers needed to play `streamUrl` (if live). |

**Example:**
```javascript
module.exports.getCatalog = async function(catalogId, mediaType, extra, page) {
  if (catalogId === 'live-channels' || mediaType === 'live') {
    return [
      {
        id: 'news_channel_1',
        title: '24/7 Global News',
        type: 'live',
        poster: 'https://example.com/posters/news.jpg',
        streamUrl: 'https://live.example.com/news.m3u8',
        headers: { 'User-Agent': 'NuvioPlayer' }
      }
    ];
  }

  // VOD Movies
  return [
    {
      id: 'tt0137523',
      title: 'Fight Club',
      type: 'movie',
      poster: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
      releaseInfo: '1999',
      extra: 'catalog_movie'
    }
  ];
};
```

---

### 4. `search(query, mediaType, extra, page)`
Called when the user searches within the plugin's dedicated browser page.

**Signature:**
```javascript
module.exports.search = async function(
  query: string,
  mediaType: string,
  extra: string,
  page: number
): Promise<Array<MediaItem>>
```

**Example:**
```javascript
module.exports.search = async function(query, mediaType, extra, page) {
  return [
    {
      id: `live_${query}`,
      title: `Broadcast: ${query}`,
      type: 'live',
      poster: 'https://example.com/channel.jpg',
      streamUrl: 'https://example.com/live.m3u8'
    }
  ];
};
```

---

## 🌐 Runtime Environment & Built-in Globals

Nuvio runs scrapers in QuickJS. It includes a built-in standard library of web APIs, cryptography modules, and scrapers:

### Globals & Context
- `globalThis.SCRAPER_ID`: The scraper's unique ID string (matching `manifest.json`).
- `globalThis.SCRAPER_SETTINGS`: Plain JavaScript object containing user-saved settings.
- `globalThis.TMDB_API_KEY`: The TMDb API key configured in Nuvio settings (if available).
- `globalThis.global`, `globalThis.window`, `globalThis.self`: Self-referencing aliases.

---

### Networking (`fetch`)
A standard asynchronous HTTP fetch implementation backed by OkHttp on Android:
```javascript
const response = await fetch('https://api.example.com/data', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'User-Agent': 'CustomAgent/1.0'
  },
  body: JSON.stringify({ query: 'nuvio' }),
  redirect: 'follow' // 'follow' (default) or 'manual'
});

const isOk = response.ok;               // boolean
const statusCode = response.status;     // 200
const headerVal = response.headers.get('content-type');

const data = await response.json();     // parsed JSON
// or: const text = await response.text();
// or: const buf = await response.arrayBuffer();
```

---

### HTML Parsing & DOM (`cheerio`)
Available both globally as `cheerio` and via `require('cheerio')`. Fast and memory-efficient DOM traversal:
```javascript
const cheerio = require('cheerio');
const res = await fetch('https://example.com/list');
const html = await res.text();

const $ = cheerio.load(html);
const items = [];

$('div.movie-card').each((index, el) => {
  const title = $(el).find('h2.title').text().trim();
  const link = $(el).find('a').attr('href');
  const poster = $(el).find('img').attr('src');
  items.push({ title, link, poster });
});
```

Supported cheerio methods: `.find()`, `.text()`, `.html()`, `.attr()`, `.first()`, `.last()`, `.next()`, `.prev()`, `.eq()`, `.get()`, `.map()`, `.filter()`, `.children()`, `.each()`, and `$.html()`.

---

### Cryptography (`CryptoJS` & Web Crypto)

#### 1. CryptoJS (`require('crypto-js')` or global `CryptoJS`)
- **AES Encryption / Decryption:**
  ```javascript
  const CryptoJS = require('crypto-js');

  // OpenSSL-compatible AES
  const encrypted = CryptoJS.AES.encrypt('plain text', 'secret pass');
  const decrypted = CryptoJS.AES.decrypt(encrypted.toString(), 'secret pass')
    .toString(CryptoJS.enc.Utf8);

  // Custom key, IV, mode, padding
  const key = CryptoJS.enc.Hex.parse('00112233445566778899aabbccddeeff');
  const iv  = CryptoJS.enc.Hex.parse('101112131415161718191a1b1c1d1e1f');
  const cipher = CryptoJS.AES.decrypt(ciphertextWa, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  });
  ```
- **Hashing:**
  `CryptoJS.MD5(str)`, `CryptoJS.SHA1(str)`, `CryptoJS.SHA256(str)`, `CryptoJS.SHA384(str)`, `CryptoJS.SHA512(str)`
- **HMAC:**
  `CryptoJS.HmacSHA256(msg, key)`, `CryptoJS.HmacMD5(msg, key)`
- **PBKDF2:**
  `CryptoJS.PBKDF2(pass, salt, { keySize: 8, iterations: 1000 })`
- **Encodings:**
  `CryptoJS.enc.Hex`, `CryptoJS.enc.Utf8`, `CryptoJS.enc.Latin1`, `CryptoJS.enc.Base64`, `CryptoJS.enc.Base64url`

#### 2. Web Crypto API (`crypto.subtle`)
- `crypto.getRandomValues(new Uint8Array(16))`
- `crypto.randomUUID()`
- `crypto.subtle.digest('SHA-256', data)`
- `crypto.subtle.encrypt / decrypt` (AES-CBC, AES-GCM)
- `crypto.subtle.sign / verify` (HMAC)

---

### URL Handling (`URL` & `URLSearchParams`)
Complete URL construction and parameter manipulation:
```javascript
const url = new URL('/api/search?q=test', 'https://example.com');
url.searchParams.set('page', '2');
url.searchParams.append('filter', 'hd');
console.log(url.toString()); // https://example.com/api/search?q=test&page=2&filter=hd
```

---

### Binary & Encodings (`atob`, `btoa`, `TextEncoder`, `TextDecoder`)
- Base64 encoding/decoding: `atob(encoded)`, `btoa(binary)`
- UTF-8 conversions:
  ```javascript
  const bytes = new TextEncoder().encode('Hello World');
  const str = new TextDecoder('utf-8').decode(bytes);
  ```

---

### Timers (`setTimeout`, `setInterval`)
Timers are backed by Kotlin coroutine delays, guaranteeing non-blocking asynchronous timing:
```javascript
await new Promise(resolve => setTimeout(resolve, 500));
```

---

### Console Logging
Standard logging functions bridge directly into Nuvio's application log with the tag `Plugin:<scraperId>`:
- `console.log(...)`
- `console.info(...)`
- `console.warn(...)`
- `console.error(...)`
- `console.debug(...)`

---

## ⚡ Content Types & Playback Modes

| Media Type | UI Behavior | Stream Resolution |
|---|---|---|
| **`live`** | **Direct Instant Playback:** Clicking an item on the plugin page launches the video player directly, bypassing the details screen entirely. | Uses `streamUrl` directly from the catalog/search item. |
| **`movie` / `tv` (Plugin Page)** | **Scoped Playback:** Opens the details screen tagged with `scopedPluginId`. Clicking "Play" searches **only this plugin** for streams, ignoring other plugins and addons. | Calls `getStreams(tmdbId, mediaType, season, episode, extra)`. |
| **`movie` / `tv` (Home / Regular Search)** | Standard details screen. Searches all enabled plugins and addons concurrently. | Calls `getStreams(...)` across all enabled providers. |

---

## 🛠 Developer Tooling & Testing

The SDK includes tools in `tools/` for offline validation, simulation, and live testing:

### Validator CLI
Validates manifest structure, JSON syntax, required scraper files, export signatures, and the `onSettings()` schema:
```bash
node tools/validator.js [path/to/manifest.json]
```

### Simulator CLI
Executes your scraper in a Node.js simulation harness with mock `SCRAPER_SETTINGS`, checking `onSettings()`, `getCatalog()`, `search()`, and `getStreams()`:
```bash
node tools/simulator.js [path/to/manifest.json]
```

### Local Dev Server with ADB Reverse
Launches a local HTTP server and executes `adb reverse tcp:8765 tcp:8765`:
```bash
node tools/dev-server.js [path/to/manifest.json] [port]
```
Allows testing local plugin files on an emulator or connected phone using:
`http://127.0.0.1:8765/manifest.json`
