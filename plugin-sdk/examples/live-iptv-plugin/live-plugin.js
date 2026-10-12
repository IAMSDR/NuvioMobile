/**
 * Example Live IPTV Plugin
 */

module.exports = {
  getCatalog: async function(catalogId, mediaType, extra, page) {
    if (extra === 'sports') {
      return [
        {
          id: 'sport_1',
          title: 'Sports HD 1',
          type: 'live',
          poster: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=500',
          streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
          extra: extra
        },
        {
          id: 'sport_2',
          title: 'Sports Arena 2',
          type: 'live',
          poster: 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500',
          streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
          extra: extra
        }
      ];
    }

    return [
      {
        id: 'news_1',
        title: 'Global 24 News',
        type: 'live',
        poster: 'https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=500',
        streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        extra: extra
      }
    ];
  },

  search: async function(query, mediaType, extra, page) {
    return [
      {
        id: 'search_channel_' + query,
        title: 'Channel: ' + query,
        type: 'live',
        poster: 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=500',
        streamUrl: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        extra: extra || ''
      }
    ];
  },

  getStreams: async function(tmdbId, mediaType, season, episode, extra) {
    return [];
  }
};
