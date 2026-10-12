/**
 * Starter Plugin for NuvioTV
 *
 * Implements:
 *   1. getCatalog(catalogId, mediaType, extra, page)
 *   2. search(query, mediaType, extra, page)
 *   3. getStreams(tmdbId, mediaType, season, episode, extra)
 */

module.exports = {
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
    if (catalogId === 'live-channels' || mediaType === 'live') {
      return [
        {
          id: 'live_stream_1',
          title: 'Big Buck Bunny 24/7',
          type: 'live',
          poster: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500',
          backdrop: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=1000',
          description: 'Non-stop animation stream test.',
          streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
          extra: extra || ''
        },
        {
          id: 'live_stream_2',
          title: 'Tears of Steel Live',
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
    return [
      {
        id: 'search_result_1',
        title: 'Matching Stream: ' + query,
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
    return [
      {
        title: 'Fast CDN 1080p (' + (extra || 'Direct') + ')',
        url: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        quality: '1080p',
        headers: {
          'User-Agent': 'NuvioTV/1.0'
        }
      }
    ];
  }
};
