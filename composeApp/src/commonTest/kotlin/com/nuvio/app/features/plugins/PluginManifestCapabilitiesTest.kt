package com.nuvio.app.features.plugins

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PluginManifestCapabilitiesTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun legacy_manifest_without_hasHome_or_catalogs_parses_cleanly() {
        val rawJson = """
            {
                "name": "Legacy Repo",
                "version": "1.0.0",
                "scrapers": [
                    {
                        "id": "legacy-scraper",
                        "name": "Legacy Scraper",
                        "version": "1.0.0",
                        "filename": "legacy.js",
                        "supportedTypes": ["movie", "tv"]
                    }
                ]
            }
        """.trimIndent()

        val manifest = json.decodeFromString<PluginManifest>(rawJson)
        assertNotNull(manifest)
        assertEquals(1, manifest.scrapers.size)
        val scraper = manifest.scrapers[0]
        assertEquals("legacy-scraper", scraper.id)
        assertEquals(null, scraper.hasHome)
        assertEquals(null, scraper.supportsSearch)
        assertTrue(scraper.effectiveCatalogs.isEmpty())
    }

    @Test
    fun modern_manifest_with_hasHome_and_catalogs_parses_correctly() {
        val rawJson = """
            {
                "name": "Modern Repo",
                "version": "2.0.0",
                "scrapers": [
                    {
                        "id": "modern-scraper",
                        "name": "Modern Scraper",
                        "version": "2.0.0",
                        "filename": "modern.js",
                        "supportedTypes": ["movie", "tv", "live"],
                        "hasHome": true,
                        "supportsSearch": true,
                        "catalogs": [
                            {
                                "id": "live-sports",
                                "name": "Live Sports",
                                "type": "live",
                                "extra": "sports-filter"
                            },
                            {
                                "id": "trending-movies",
                                "name": "Trending Movies",
                                "type": "movie"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val manifest = json.decodeFromString<PluginManifest>(rawJson)
        assertNotNull(manifest)
        val scraper = manifest.scrapers[0]
        assertEquals(true, scraper.hasHome)
        assertEquals(true, scraper.supportsSearch)
        assertEquals(2, scraper.effectiveCatalogs.size)
        assertEquals("live-sports", scraper.effectiveCatalogs[0].id)
        assertEquals("sports-filter", scraper.effectiveCatalogs[0].extra)
        assertEquals("live", scraper.effectiveCatalogs[0].type)
        assertEquals("trending-movies", scraper.effectiveCatalogs[1].id)
    }

    @Test
    fun normalizePluginType_maps_channel_and_stream_to_live() {
        assertEquals("live", normalizePluginType("channel"))
        assertEquals("live", normalizePluginType("iptv"))
        assertEquals("live", normalizePluginType("stream"))
        assertEquals("tv", normalizePluginType("series"))
        assertEquals("tv", normalizePluginType("show"))
        assertEquals("movie", normalizePluginType("movie"))
    }

    @Test
    fun pluginMediaItem_displayTitle_fallback() {
        val withTitle = PluginMediaItem(id = "1", title = "Title 1")
        assertEquals("Title 1", withTitle.displayTitle)

        val withName = PluginMediaItem(id = "2", name = "Name 2")
        assertEquals("Name 2", withName.displayTitle)

        val withIdOnly = PluginMediaItem(id = "item-3")
        assertEquals("item-3", withIdOnly.displayTitle)
    }
}
