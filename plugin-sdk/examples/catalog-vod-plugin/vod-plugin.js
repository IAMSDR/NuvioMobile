/**
 * Example VOD Catalog Plugin
 */

module.exports = {
  getCatalog: async function(catalogId, mediaType, extra, page) {
    if (mediaType === 'movie' || catalogId === 'top-movies') {
      return [
        {
          id: 'tt0137523',
          title: 'Fight Club',
          type: 'movie',
          poster: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
          backdrop: 'https://image.tmdb.org/t/p/original/hZkgoQYus5vegHoetLkCJzb17zJ.jpg',
          description: 'A depressed office worker looking for a way to change his life crosses paths with an eccentric soap maker.',
          releaseInfo: '1999',
          extra: extra
        },
        {
          id: 'tt0111161',
          title: 'The Shawshank Redemption',
          type: 'movie',
          poster: 'https://image.tmdb.org/t/p/w500/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg',
          backdrop: 'https://image.tmdb.org/t/p/original/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg',
          description: 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.',
          releaseInfo: '1994',
          extra: extra
        }
      ];
    }

    return [
      {
        id: 'tt0903747',
        title: 'Breaking Bad',
        type: 'tv',
        poster: 'https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg',
        description: 'A chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.',
        releaseInfo: '2008',
        extra: extra
      }
    ];
  },

  search: async function(query, mediaType, extra, page) {
    return [
      {
        id: 'tt0137523',
        title: 'Fight Club (' + query + ')',
        type: 'movie',
        poster: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
        description: 'Matched search query for: ' + query,
        extra: extra || ''
      }
    ];
  },

  getStreams: async function(tmdbId, mediaType, season, episode, extra) {
    return [
      {
        title: 'VOD Stream 1080p (' + (extra || 'Default') + ')',
        url: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
        quality: '1080p'
      }
    ];
  }
};
