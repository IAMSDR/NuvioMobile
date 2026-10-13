/**
 * Starter Plugin for Nuvio
 *
 * Implements:
 *   1. onSettings()                                         - Dynamic settings UI
 *   2. getCatalog(catalogId, mediaType, extra, page)        - On-demand shelves
 *   3. search(query, mediaType, extra, page)                - In-plugin search
 *   4. getStreams(tmdbId, mediaType, season, episode, extra) - Stream scraper
 */

module.exports = {
  /**
   * Returns the configuration schema for this plugin.
   * When manifest.json specifies "hasSettings": true, Nuvio displays a gear icon
   * in Settings -> Plugins and invokes this method to build the settings form.
   *
   * Supported types:
   *   - 'header': Section title
   *   - 'info': Informational note
   *   - 'text': String input (supports placeholder, description, isPassword)
   *   - 'select': Dropdown picker (options: [{ label, value }], defaultValue)
   *   - 'toggle': Boolean switch (defaultValue: true|false)
   *
   * Saved values are accessible at runtime via globalThis.SCRAPER_SETTINGS[key].
   *
   * @returns {Promise<Array<Object>>}
   */
  onSettings: async function() {
    return [
      {
        type: 'header',
        label: 'Account & Credentials'
      },
      {
        type: 'text',
        key: 'api_key',
        label: 'Provider API Key',
        description: 'Optional API key for premium bandwidth and sources.',
        placeholder: 'e.g. key_live_12345',
        isPassword: true
      },
      {
        type: 'header',
        label: 'Playback & Quality'
      },
      {
        type: 'select',
        key: 'stream_quality',
        label: 'Default Stream Resolution',
        description: 'Filters or prioritizes available qualities.',
        defaultValue: '1080p',
        options: [
          { label: '4K Ultra HD', value: '4k' },
          { label: '1080p Full HD', value: '1080p' },
          { label: '720p HD', value: '720p' },
          { label: 'Auto (Best)', value: 'auto' }
        ]
      },
      {
        type: 'toggle',
        key: 'enable_backup_cdn',
        label: 'Enable Fallback Backup CDNs',
        description: 'Attempts alternative mirror links if primary stream fails.',
        defaultValue: true
      },
      {
        type: 'info',
        label: 'Settings are stored locally on your device and applied on the next search.'
      }
    ];
  },

  /**
   * Fetches items for a catalog declared in manifest.json.
   *
   * @param {string} catalogId - The id declared in manifest catalogs (e.g. 'live-channels')
   * @param {string} mediaType - Content type ('live', 'movie', or 'tv')
   * @param {string} extra     - Optional extra filter or category parameter
   * @param {number} page      - Page number (1-based)
   * @returns {Promise<Array<Object>>}
   */
  getCatalog: async function(catalogId, mediaType, extra, page) {
    const settings = globalThis.SCRAPER_SETTINGS || {};
    const preferredQuality = settings.stream_quality || '1080p';

    if (catalogId === 'live-channels' || mediaType === 'live') {
      return [
        {
          id: 'live_stream_1',
          title: 'Big Buck Bunny 24/7 (' + preferredQuality + ')',
          type: 'live',
          poster: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500',
          backdrop: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=1000',
          description: 'Non-stop animation stream test.',
          streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
          extra: extra || ''
        },
        {
          id: 'live_stream_2',
          title: 'Tears of Steel Live (' + preferredQuality + ')',
          type: 'live',
          poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500',
          backdrop: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1000',
          description: 'Sci-fi short film stream.',
          streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
          extra: extra || ''
        }
      ];
    }

    // Curated movies catalog
    return [
      {
        id: 'tt0137523',
        title: 'Fight Club',
        type: 'movie',
        poster: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
        backdrop: 'https://image.tmdb.org/t/p/original/hZkgoQYus5vegHoetLkCJzb17zJ.jpg',
        description: 'An insomniac office worker and a devil-may-care soap maker form an underground fight club.',
        releaseInfo: '1999',
        streamUrl: null,
        extra: extra || ''
      }
    ];
  },

  /**
   * Searches the plugin for matching items.
   *
   * @param {string} query     - Search term entered by the user
   * @param {string} mediaType - Optional content type filter ('live', 'movie', 'tv')
   * @param {string} extra     - Optional extra filter
   * @param {number} page      - Page number (1-based)
   * @returns {Promise<Array<Object>>}
   */
  search: async function(query, mediaType, extra, page) {
    const settings = globalThis.SCRAPER_SETTINGS || {};
    const qualityTag = settings.stream_quality || 'Auto';

    return [
      {
        id: 'search_result_1',
        title: 'Matching Stream: ' + query + ' [' + qualityTag + ']',
        type: 'live',
        poster: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500',
        description: 'Live broadcast matching: ' + query,
        streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        extra: extra || ''
      }
    ];
  },

  /**
   * Resolves playable video streams for a specific title.
   *
   * @param {string} tmdbId    - TMDB ID or IMDb ID (e.g. 'tt0137523')
   * @param {string} mediaType - 'movie' or 'tv'
   * @param {number?} season   - Season number (for TV series)
   * @param {number?} episode  - Episode number (for TV series)
   * @param {string?} extra    - Optional parameter passed from catalog item
   * @returns {Promise<Array<Object>>}
   */
  getStreams: async function(tmdbId, mediaType, season, episode, extra) {
    // Read user settings configured through the app dialog
    const settings = globalThis.SCRAPER_SETTINGS || {};
    const quality = settings.stream_quality || '1080p';
    const apiKey = settings.api_key ? 'Auth: ' + settings.api_key.substring(0, 5) + '...' : 'Guest';

    return [
      {
        title: 'Fast CDN ' + quality + ' (' + (extra || 'Direct') + ') [' + apiKey + ']',
        url: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        quality: quality,
        headers: {
          'User-Agent': 'Nuvio/1.0'
        }
      }
    ];
  }
};
