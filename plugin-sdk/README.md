# NuvioTV Plugin SDK

Developer SDK, templates, and testing tools for building, validating, and testing NuvioTV plugins.

---

## 🚀 Quickstart

### 1. Test the starter template locally
Run the simulator to verify that catalog loading, search, and stream resolution work without deploying to a device:

```bash
cd plugin-sdk
npm test
# or: node tools/simulator.js template/manifest.json
```

### 2. Validate your plugin manifest and JavaScript exports
Run the schema validator:

```bash
npm run validate
# or: node tools/validator.js path/to/your/manifest.json
```

### 3. Run the live local dev server with ADB Reverse
Start a local HTTP server that automatically binds port `8765` directly to your connected Android TV via ADB reverse:

```bash
npm run serve
# or: node tools/dev-server.js template/manifest.json
```

Add the printed URL directly into NuvioTV (**Settings -> Content Discovery -> Plugins -> Add repository**):
```
http://127.0.0.1:8765/manifest.json
```

---

## 📁 Directory Structure

```
plugin-sdk/
├── template/                      # Starter plugin boilerplate
│   ├── manifest.json              # Sample repository manifest
│   └── sample-plugin.js           # Sample scraper implementation
├── examples/
│   ├── live-iptv-plugin/          # Working Live IPTV channels plugin
│   └── catalog-vod-plugin/        # Working VOD movies/series catalog + search plugin
├── tools/
│   ├── validator.js               # Manifest & export validator CLI
│   ├── simulator.js               # Mock runner that executes plugin functions
│   └── dev-server.js              # HTTP server with auto ADB reverse forwarding
└── package.json
```

---

## 📜 Manifest Specification (`manifest.json`)

```json
{
  "name": "My Plugin Repository",
  "version": "1.0.0",
  "scrapers": [
    {
      "id": "my-plugin",
      "name": "My Plugin Name",
      "description": "Live channels and movies",
      "version": "1.0.0",
      "filename": "my-plugin.js",
      "logo": "https://example.com/logo.png",
      "supportedTypes": ["movie", "tv", "live"],
      "hasHome": true,
      "supportsSearch": true,
      "catalogs": [
        {
          "id": "live-channels",
          "name": "Live Channels",
          "type": "live",
          "extra": "news"
        },
        {
          "id": "trending-movies",
          "name": "Trending Movies",
          "type": "movie",
          "extra": "popular"
        }
      ]
    }
  ]
}
```

### Manifest Fields
| Field | Type | Required | Description |
|---|---|---|---|
| `id` | string | **Yes** | Unique identifier for the scraper |
| `name` | string | **Yes** | Display name in NuvioTV |
| `filename` | string | **Yes** | Relative filename or full URL to the JavaScript scraper |
| `supportedTypes` | array | **Yes** | `["movie", "tv", "live"]` |
| `hasHome` | boolean | No | If `true`, adds a dedicated card to the **Plugins** row directly below the hero section on Modern, Classic, and Grid home layouts. |
| `supportsSearch` | boolean | No | If `true`, enables the search bar on the plugin's dedicated page. |
| `catalogs` | array | No | List of catalog rows to display on the plugin's dedicated page. |

---

## 💻 JavaScript API Specification

Your `.js` file must export the required methods via `module.exports`:

### 1. `getStreams(tmdbId, mediaType, season, episode, extra)`
Resolves playable streams for movies or series.

```javascript
module.exports.getStreams = async function(tmdbId, mediaType, season, episode, extra) {
  // tmdbId: string (e.g. 'tt0137523' or '550')
  // mediaType: 'movie' | 'tv'
  // season: number | null
  // episode: number | null
  // extra: string (optional trailing parameter)
  return [
    {
      title: "1080p Stream",
      url: "https://example.com/stream.m3u8",
      quality: "1080p",
      headers: {
        "User-Agent": "Custom-Agent"
      }
    }
  ];
};
```

### 2. `getCatalog(catalogId, mediaType, extra, page)`
Called when opening the plugin's dedicated page to populate its catalog carousels.

```javascript
module.exports.getCatalog = async function(catalogId, mediaType, extra, page) {
  // catalogId: string (matching id in manifest)
  // mediaType: 'live' | 'movie' | 'tv'
  // extra: string
  // page: number (1-based)
  return [
    {
      id: "ch_1",
      title: "News 24",
      type: "live",               // 'live' triggers direct playback!
      poster: "https://example.com/poster.jpg",
      streamUrl: "https://example.com/live.m3u8"
    }
  ];
};
```

### 3. `search(query, mediaType, extra, page)`
Called when the user searches in the plugin's dedicated page.

```javascript
module.exports.search = async function(query, mediaType, extra, page) {
  // query: string entered by user
  return [
    {
      id: "search_1",
      title: "Matched Channel",
      type: "live",
      poster: "https://example.com/poster.jpg",
      streamUrl: "https://example.com/channel.m3u8"
    }
  ];
};
```

---

## ⚡ Direct Playback vs Scoped Playback

- **Direct Live Playback:** When an item has `type: "live"` or `type: "channel"` with a `streamUrl`, clicking it opens the player directly without opening a details screen.
- **Scoped Playback:** When a movie or TV series is opened from a plugin's page, clicking "Play" searches **only that plugin** for streams, ignoring other plugins and addons.
