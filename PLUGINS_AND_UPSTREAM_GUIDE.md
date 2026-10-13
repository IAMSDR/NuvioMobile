# NuvioMobile Fork: Plugin System & Upstream Sync Guide

This document describes the fork additions made to NuvioMobile, the extended plugin specification, and the workflow for merging upstream changes without conflicts.

---

## 1. Plugin System Extensions

### Manifest Specification (`manifest.json`)
Plugins can declare home row presence, search capability, and catalogues:

```json
{
  "name": "My Plugin Repository",
  "version": "1.0.0",
  "scrapers": [
    {
      "id": "my-plugin",
      "name": "My Plugin",
      "description": "Live TV and on-demand content",
      "version": "1.0.0",
      "filename": "my-plugin.js",
      "logo": "https://example.com/logo.png",
      "supportedTypes": ["movie", "tv", "live"],
      "hasHome": true,
      "supportsSearch": true,
      "hasSettings": true,
      "catalogs": [
        {
          "id": "live-channels",
          "name": "Live Channels",
          "type": "live",
          "extra": ""
        },
        {
          "id": "popular-movies",
          "name": "Popular Movies",
          "type": "movie",
          "extra": ""
        }
      ]
    }
  ]
}
```

#### Manifest Fields
- `hasHome` (optional boolean, default `false`): When `true`, a card for this plugin appears in the "Plugins" row on the mobile home screen (below Hero section).
- `supportsSearch` (optional boolean, default `false`): When `true`, search is enabled on the plugin's dedicated browser page.
- `hasSettings` (optional boolean, default `false`): When `true`, enables a settings gear icon in **Settings -> Plugins** and executes `onSettings()` to construct a native user settings dialog.
- `catalogs` (optional array): Catalogues to display on the plugin's dedicated page. Each item contains:
  - `id`: Unique identifier for the catalogue.
  - `name`: Display title of the catalogue row.
  - `type`: Content type (`movie`, `tv` / `series`, or `live`).
  - `extra` (optional string): Custom argument passed to `getCatalog`.

---

### Plugin User Settings System

When `hasSettings: true`, plugins export an `onSettings()` function that returns an array of UI fields.

#### Supported Setting Controls:
1. `header`: `{ type: "header", label: "Section Title" }`
2. `info`: `{ type: "info", label: "Descriptive help note" }`
3. `text`: `{ type: "text", key: "api_key", label: "API Key", description: "...", placeholder: "...", isPassword: true }`
4. `select`: `{ type: "select", key: "quality", label: "Resolution", defaultValue: "1080p", options: [{ label: "1080p", value: "1080p" }] }`
5. `toggle`: `{ type: "toggle", key: "enable_feature", label: "Enable Feature", defaultValue: true }`

#### Reading Settings at Runtime:
Whenever `getStreams`, `getCatalog`, or `search` execute, the user's saved preferences are injected into `globalThis.SCRAPER_SETTINGS`:
```javascript
const settings = globalThis.SCRAPER_SETTINGS || {};
const apiKey = settings.api_key || "";
const quality = settings.quality || "1080p";
```

---

### Plugin JavaScript Contract (`.js`)

Plugins can export the following functions on `module.exports` or `globalThis`:

#### 1. Streams (`getStreams`)
```javascript
module.exports.getStreams = async function(tmdbId, mediaType, season, episode, extra) {
  // tmdbId: string
  // mediaType: "movie" | "tv" | "series" | "live"
  // season: number | null
  // episode: number | null
  // extra: string (defaults to "")
  return [
    {
      title: "Stream Title",
      url: "https://example.com/video.mp4",
      quality: "1080p",
      headers: { "User-Agent": "..." }
    }
  ];
};
```
*Backwards compatibility:* Existing plugins with `getStreams(tmdbId, mediaType, season, episode)` continue to work without changes because `extra` is passed as an optional trailing parameter.

#### 2. Catalogues (`getCatalog`)
```javascript
module.exports.getCatalog = async function(catalogId, mediaType, extra, page) {
  // catalogId: string
  // mediaType: "movie" | "tv" | "live"
  // extra: string
  // page: number (starts at 1)
  return [
    {
      id: "tt1234567",
      title: "Sample Movie",
      type: "movie", // "movie", "tv", or "live"
      poster: "https://example.com/poster.jpg",
      backdrop: "https://example.com/backdrop.jpg",
      description: "Sample description",
      releaseInfo: "2024",
      streamUrl: null, // For "live", providing streamUrl allows direct instant playback
      extra: ""
    }
  ];
};
```

#### 3. Search (`search`)
```javascript
module.exports.search = async function(query, mediaType, extra, page) {
  // query: string
  // mediaType: string (optional)
  // extra: string
  // page: number
  return [
    {
      id: "channel-1",
      title: "News Channel",
      type: "live",
      poster: "https://example.com/channel.png",
      streamUrl: "https://example.com/live.m3u8"
    }
  ];
};
```

---

## 2. Playback Behavior

- **`live` Items:**
  Selecting a `live` item on the plugin page opens the stream directly in the player (`PlayerRoute`), completely bypassing any details screen.
- **`movie` / `tv` Items from Plugin Page:**
  Selecting a movie or series item opens the details page scoped to that plugin (`scopedPluginId`). When the user clicks "Play", the app searches only that current plugin for streams.
- **Normal Playback (outside plugin page):**
  Unchanged. Searches all enabled plugins and installed addons as before.

---

## 3. In-App Updates

In-app updater defaults to your fork:
- Owner: `IAMSDR`
- Repo: `NuvioMobile`
- Defined in `composeApp/build.gradle.kts` via generated `GitHubConfig` object.
- Releases and APK assets published to `https://github.com/IAMSDR/NuvioMobile/releases` are automatically detected and presented in the in-app update banner.

---

## 4. Supabase & Nuvio Cloud Sync

The app is pre-configured with Nuvio's official public API endpoints:
- `SUPABASE_URL`: `https://api.nuvio.tv`
- `SUPABASE_ANON_KEY`: `sb_publishable_1Clq8rlTVACkdcZuqr6_AD__xUUC_EN`
- `DEVICE_LOGIN_WEB_BASE_URL`: `https://nuvio.tv/link`

This enables:
- Nuvio QR code / Short-link sign in (`nuvio.tv/link`)
- Cloud profile sync and avatar loading
- Cloud library and watch progress sync

---

## 5. Plugin SDK

The `plugin-sdk/` directory includes testing and boilerplate tooling:
- `plugin-sdk/template/`: Starter template with manifest and scraper JS.
- `plugin-sdk/examples/`: Working IPTV and VOD examples.
- `npm run validate`: Tests manifest syntax and JS export signatures.
- `npm run test`: Simulates `getCatalog`, `search`, and `getStreams` locally with Node.js.
- `npm run serve`: Launches a local HTTP server with automatic `adb reverse` for instant mobile testing.

---

## 6. Upstream Sync Workflow

To keep your fork in sync with upstream (`NuvioMedia/NuvioMobile`):

### 1. Add upstream remote (once):
```bash
git remote add upstream https://github.com/NuvioMedia/NuvioMobile.git
```

### 2. Fetch upstream updates:
```bash
git fetch upstream
```

### 3. Merge upstream `cmp-rewrite` into your fork branch:
```bash
git checkout cmp-rewrite
git merge upstream/cmp-rewrite
```

### 4. Why merges stay non-blocking:
- All additions to core interfaces (`StreamsRepository`, `PluginRepository`, `PluginRuntime`, `DetailRoute`, `StreamLaunch`) use default parameter values.
- New screens and components (`PluginBrowserScreen`, `PluginBrowserViewModel`, `PluginCard`, `PluginsRowSection`) are in their own dedicated files.
- The "Plugins" row integration in `HomeScreen.kt` is localized to a single check.
