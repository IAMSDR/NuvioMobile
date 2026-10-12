package com.nuvio.app.features.plugins

import kotlinx.coroutines.flow.StateFlow

expect object PluginRepository {
    val uiState: StateFlow<PluginsUiState>

    fun initialize()

    fun onProfileChanged(profileId: Int)

    fun clearLocalState()

    suspend fun pullFromServer(profileId: Int)

    suspend fun addRepository(rawUrl: String): AddPluginRepositoryResult

    fun removeRepository(manifestUrl: String)

    fun refreshAll()

    fun refreshRepository(manifestUrl: String, pushAfterRefresh: Boolean = false)

    fun toggleScraper(scraperId: String, enabled: Boolean)

    fun setPluginsEnabled(enabled: Boolean)

    fun setGroupStreamsByRepository(enabled: Boolean)

    fun setLocalPluginSearchPaused(paused: Boolean)

    fun getEnabledScrapersForType(type: String): List<PluginScraper>

    suspend fun testScraper(scraperId: String): Result<List<PluginRuntimeResult>>

    suspend fun executeScraper(
        scraper: PluginScraper,
        tmdbId: String,
        mediaType: String,
        season: Int?,
        episode: Int?,
        extra: String = "",
    ): Result<List<PluginRuntimeResult>>

    suspend fun getCatalog(
        scraperId: String,
        catalogId: String,
        mediaType: String,
        extra: String? = null,
        page: Int = 1,
    ): Result<List<PluginMediaItem>>

    suspend fun search(
        scraperId: String,
        query: String,
        mediaType: String? = null,
        extra: String? = null,
        page: Int = 1,
    ): Result<List<PluginMediaItem>>
}