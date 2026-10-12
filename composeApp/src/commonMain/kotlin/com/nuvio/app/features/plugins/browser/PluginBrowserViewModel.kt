package com.nuvio.app.features.plugins.browser

import co.touchlab.kermit.Logger
import com.nuvio.app.features.plugins.PluginCatalogManifest
import com.nuvio.app.features.plugins.PluginMediaItem
import com.nuvio.app.features.plugins.PluginRepository
import com.nuvio.app.features.plugins.PluginScraper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PluginCatalogSection(
    val catalog: PluginCatalogManifest,
    val items: List<PluginMediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class PluginBrowserUiState(
    val scraper: PluginScraper? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val sections: List<PluginCatalogSection> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<PluginMediaItem> = emptyList(),
    val isLiveResolving: Boolean = false,
    val liveErrorMessage: String? = null,
)

class PluginBrowserViewModel(
    val scraperId: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("PluginBrowser")
    private val _uiState = MutableStateFlow(PluginBrowserUiState())
    val uiState: StateFlow<PluginBrowserUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadScraperAndCatalogs()
    }

    fun loadScraperAndCatalogs() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            PluginRepository.initialize()
            val scrapers = PluginRepository.uiState.value.scrapers
            val scraper = scrapers.find { it.id == scraperId }
            if (scraper == null) {
                _uiState.update { it.copy(isLoading = false, error = "Plugin not found") }
                return@launch
            }

            _uiState.update { it.copy(scraper = scraper, isLoading = false) }

            val catalogs = scraper.catalogs
            if (catalogs.isNotEmpty()) {
                val initialSections = catalogs.map {
                    PluginCatalogSection(catalog = it, isLoading = true)
                }
                _uiState.update { it.copy(sections = initialSections) }

                catalogs.forEachIndexed { index, catalog ->
                    launch {
                        val result = PluginRepository.getCatalog(
                            scraperId = scraper.id,
                            catalogId = catalog.id,
                            mediaType = catalog.type,
                            extra = catalog.extra,
                        )
                        result.fold(
                            onSuccess = { items ->
                                _uiState.update { current ->
                                    val updated = current.sections.toMutableList()
                                    if (index < updated.size) {
                                        updated[index] = updated[index].copy(
                                            items = items,
                                            isLoading = false,
                                        )
                                    }
                                    current.copy(sections = updated)
                                }
                            },
                            onFailure = { error ->
                                log.e(error) { "Failed to load catalog ${catalog.id}" }
                                _uiState.update { current ->
                                    val updated = current.sections.toMutableList()
                                    if (index < updated.size) {
                                        updated[index] = updated[index].copy(
                                            isLoading = false,
                                            error = error.message,
                                        )
                                    }
                                    current.copy(sections = updated)
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        performSearch(query)
    }

    fun performSearch(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = scope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val scraper = _uiState.value.scraper ?: return@launch
            val result = PluginRepository.search(
                scraperId = scraper.id,
                query = trimmed,
            )
            result.fold(
                onSuccess = { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                },
                onFailure = { error ->
                    log.e(error) { "Plugin search failed for query: $trimmed" }
                    _uiState.update { it.copy(isSearching = false) }
                },
            )
        }
    }

    fun resolveLiveStream(
        item: PluginMediaItem,
        onResolved: (streamUrl: String, title: String, headers: Map<String, String>?, logo: String?, poster: String?) -> Unit,
    ) {
        val scraper = _uiState.value.scraper ?: return
        if (!item.streamUrl.isNullOrBlank()) {
            onResolved(
                item.streamUrl,
                item.displayTitle,
                item.headers,
                item.logo ?: scraper.logo,
                item.poster,
            )
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLiveResolving = true, liveErrorMessage = null) }
            val result = PluginRepository.executeScraper(
                scraper = scraper,
                tmdbId = item.id,
                mediaType = "live",
                season = null,
                episode = null,
                extra = item.extra.orEmpty(),
            )
            _uiState.update { it.copy(isLiveResolving = false) }
            result.fold(
                onSuccess = { streams ->
                    val firstStream = streams.firstOrNull()
                    if (firstStream != null) {
                        onResolved(
                            firstStream.url,
                            firstStream.title.takeIf { it.isNotBlank() } ?: item.displayTitle,
                            firstStream.headers,
                            item.logo ?: scraper.logo,
                            item.poster,
                        )
                    } else {
                        _uiState.update { it.copy(liveErrorMessage = "No live stream found for ${item.displayTitle}") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(liveErrorMessage = error.message ?: "Failed to resolve live stream") }
                },
            )
        }
    }
}
