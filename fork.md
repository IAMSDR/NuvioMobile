# NuvioMobile Fork Overview

This fork brings extended plugin capabilities to NuvioMobile while maintaining seamless compatibility with future upstream updates:

1. **Non-blocking Upstream Sync**: All changes use default parameter values on shared interfaces and isolate new features in dedicated modules so future merges from upstream `cmp-rewrite` stay conflict-free.
2. **Android Focus**: Configured for Android builds and testing.
3. **Extended Plugin Capabilities**:
   - `catalogs` and `supportsSearch` support in `manifest.json`.
   - `extra` argument in `getStreams(tmdbId, mediaType, season, episode, extra)` for custom routing.
   - `hasHome` manifest flag: displays a "Plugins" row on the home screen immediately below the Hero section (omitted when 0 plugins have `hasHome: true`).
   - Dedicated `PluginBrowserScreen` for browsing catalogs and searching within a plugin.
   - `live` media type: instant playback in player, bypassing the details screen.
   - Scoped stream resolution: movie/series playback opened from a plugin searches only that plugin.
4. **In-App Updater**:
   - Checks `https://github.com/IAMSDR/NuvioMobile/releases` for updates.
5. **Nuvio Cloud Sync**:
   - Defaults configured for official Nuvio public API endpoints (`https://api.nuvio.tv`).
6. **Plugin SDK**:
   - Full developer kit with simulator, validator, and dev server in `plugin-sdk/`.
