package com.nuvio.app.features.plugins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val PLUGIN_REPOSITORY_REFRESH_INTERVAL_MS = 6 * 60 * 60 * 1_000L

internal fun isPluginRepositoryRefreshDue(
    lastUpdatedEpochMs: Long,
    nowEpochMs: Long,
): Boolean = lastUpdatedEpochMs <= 0L ||
    nowEpochMs - lastUpdatedEpochMs >= PLUGIN_REPOSITORY_REFRESH_INTERVAL_MS

@Serializable
data class PluginManifest(
    val name: String,
    val version: String,
    val description: String? = null,
    val author: String? = null,
    val scrapers: List<PluginManifestScraper> = emptyList(),
)

@Serializable
data class PluginCatalogManifest(
    val id: String,
    val name: String,
    val type: String = "movie",
    val extra: String? = null,
)

@Serializable
data class PluginManifestScraper(
    val id: String,
    val name: String,
    val description: String? = null,
    val version: String,
    val filename: String,
    @SerialName("supportedTypes") val supportedTypes: List<String> = listOf("movie", "tv"),
    val enabled: Boolean = true,
    val hasSettings: Boolean = false,
    val logo: String? = null,
    @SerialName("contentLanguage") val contentLanguage: List<String>? = null,
    @SerialName("supportedPlatforms") val supportedPlatforms: List<String>? = null,
    @SerialName("disabledPlatforms") val disabledPlatforms: List<String>? = null,
    val formats: List<String>? = null,
    @SerialName("supportedFormats") val supportedFormats: List<String>? = null,
    @SerialName("supportsExternalPlayer") val supportsExternalPlayer: Boolean? = null,
    val limited: Boolean? = null,
    val hasHome: Boolean? = null,
    val supportsSearch: Boolean? = null,
    val catalogs: List<PluginCatalogManifest>? = null,
    val catalogues: List<PluginCatalogManifest>? = null,
) {
    val effectiveCatalogs: List<PluginCatalogManifest>
        get() = catalogs ?: catalogues ?: emptyList()
}

data class PluginRepositoryItem(
    val manifestUrl: String,
    val name: String,
    val description: String? = null,
    val version: String? = null,
    val scraperCount: Int = 0,
    val lastUpdated: Long = 0L,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

data class PluginScraper(
    val id: String,
    val repositoryUrl: String,
    val name: String,
    val description: String,
    val version: String,
    val filename: String,
    val supportedTypes: List<String>,
    val enabled: Boolean,
    val manifestEnabled: Boolean,
    val hasSettings: Boolean = false,
    val logo: String? = null,
    val contentLanguage: List<String> = emptyList(),
    val formats: List<String>? = null,
    val code: String,
    val hasHome: Boolean = false,
    val supportsSearch: Boolean = false,
    val catalogs: List<PluginCatalogManifest> = emptyList(),
) {
    fun supportsType(type: String): Boolean {
        val normalizedType = normalizePluginType(type)
        return supportedTypes.map { normalizePluginType(it) }.contains(normalizedType)
    }
}

@Serializable
data class PluginMediaItem(
    val id: String,
    val title: String? = null,
    val name: String? = null,
    val type: String = "movie",
    val poster: String? = null,
    val backdrop: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val year: String? = null,
    val imdbRating: Float? = null,
    val genres: List<String> = emptyList(),
    val streamUrl: String? = null,
    val extra: String? = null,
    val headers: Map<String, String>? = null,
) {
    val displayTitle: String
        get() = title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: id
}

data class PluginRuntimeResult(
    val title: String,
    val name: String? = null,
    val url: String,
    val quality: String? = null,
    val size: String? = null,
    val language: String? = null,
    val provider: String? = null,
    val type: String? = null,
    val seeders: Int? = null,
    val peers: Int? = null,
    val infoHash: String? = null,
    val headers: Map<String, String>? = null,
    val subtitles: List<PluginSubtitleResult>? = null,
)

@Serializable
data class PluginSubtitleResult(
    val url: String,
    val language: String,
    val name: String? = null,
    val headers: Map<String, String>? = null
)

data class PluginsUiState(
    val pluginsEnabled: Boolean = true,
    val groupStreamsByRepository: Boolean = false,
    val repositories: List<PluginRepositoryItem> = emptyList(),
    val scrapers: List<PluginScraper> = emptyList(),
)

sealed interface AddPluginRepositoryResult {
    data class Success(val repository: PluginRepositoryItem) : AddPluginRepositoryResult
    data class Error(val message: String) : AddPluginRepositoryResult
}

@Serializable
internal data class StoredPluginsState(
    val pluginsEnabled: Boolean = true,
    val groupStreamsByRepository: Boolean = false,
    val repositories: List<StoredPluginRepository> = emptyList(),
    val scrapers: List<StoredPluginScraper> = emptyList(),
)

@Serializable
internal data class StoredPluginRepository(
    val manifestUrl: String,
    val name: String,
    val description: String? = null,
    val version: String? = null,
    val scraperCount: Int = 0,
    val lastUpdated: Long = 0L,
)

@Serializable
internal data class StoredPluginScraper(
    val id: String,
    val repositoryUrl: String,
    val name: String,
    val description: String,
    val version: String,
    val filename: String,
    val supportedTypes: List<String>,
    val enabled: Boolean,
    val manifestEnabled: Boolean,
    val hasSettings: Boolean = false,
    val logo: String? = null,
    val contentLanguage: List<String> = emptyList(),
    val formats: List<String>? = null,
    val code: String? = null,
    val hasHome: Boolean = false,
    val supportsSearch: Boolean = false,
    val catalogs: List<PluginCatalogManifest> = emptyList(),
)

internal data class RestoredPluginScraper(
    val scraper: PluginScraper,
    val requiresMigration: Boolean,
)

internal fun PluginsUiState.toStoredPluginsState(): StoredPluginsState =
    StoredPluginsState(
        pluginsEnabled = pluginsEnabled,
        groupStreamsByRepository = groupStreamsByRepository,
        repositories = repositories.map { repository ->
            StoredPluginRepository(
                manifestUrl = repository.manifestUrl,
                name = repository.name,
                description = repository.description,
                version = repository.version,
                scraperCount = repository.scraperCount,
                lastUpdated = repository.lastUpdated,
            )
        },
        scrapers = scrapers.map(PluginScraper::toStoredPluginScraper),
    )

internal fun PluginScraper.toStoredPluginScraper(): StoredPluginScraper =
    StoredPluginScraper(
        id = id,
        repositoryUrl = repositoryUrl,
        name = name,
        description = description,
        version = version,
        filename = filename,
        supportedTypes = supportedTypes,
        enabled = enabled,
        manifestEnabled = manifestEnabled,
        hasSettings = hasSettings,
        logo = logo,
        contentLanguage = contentLanguage,
        formats = formats,
        code = null,
        hasHome = hasHome,
        supportsSearch = supportsSearch,
        catalogs = catalogs,
    )

internal fun StoredPluginScraper.restorePluginScraper(
    loadCachedCode: (String) -> String?,
): RestoredPluginScraper? {
    val cachedCode = loadCachedCode(id)
    val resolvedCode = cachedCode ?: code ?: return null
    return RestoredPluginScraper(
        scraper = PluginScraper(
            id = id,
            repositoryUrl = repositoryUrl,
            name = name,
            description = description,
            version = version,
            filename = filename,
            supportedTypes = supportedTypes,
            enabled = enabled,
            manifestEnabled = manifestEnabled,
            hasSettings = hasSettings,
            logo = logo,
            contentLanguage = contentLanguage,
            formats = formats,
            code = resolvedCode,
            hasHome = hasHome,
            supportsSearch = supportsSearch,
            catalogs = catalogs,
        ),
        requiresMigration = code != null,
    )
}

internal fun normalizePluginType(value: String): String =
    when (value.lowercase()) {
        "series", "show", "other" -> "tv"
        "channel", "iptv", "stream" -> "live"
        else -> value.lowercase()
    }
