package com.nuvio.app.features.plugins.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.NuvioCardDepthSurface
import com.nuvio.app.core.ui.NuvioInputField
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.nuvioCardDepth
import com.nuvio.app.features.plugins.PluginMediaItem

@Composable
fun PluginBrowserScreen(
    scraperId: String,
    onBackPress: () -> Unit,
    onNavigateToDetail: (itemId: String, itemType: String) -> Unit,
    onPlayLiveStream: (streamUrl: String, title: String, headers: Map<String, String>?, logo: String?, poster: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember(scraperId) { PluginBrowserViewModel(scraperId) }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NuvioLoadingIndicator()
            }
            return
        }

        val scraper = uiState.scraper
        if (scraper == null || uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error ?: "Plugin unavailable",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Header: Back button + Plugin Logo & Title
            item(key = "plugin_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    NuvioBackButton(onClick = onBackPress)
                    Spacer(modifier = Modifier.width(12.dp))
                    if (!scraper.logo.isNullOrBlank()) {
                        AsyncImage(
                            model = scraper.logo,
                            contentDescription = scraper.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Extension,
                            contentDescription = scraper.name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scraper.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (scraper.description.isNotBlank()) {
                            Text(
                                text = scraper.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Search Bar (if scraper.supportsSearch)
            if (scraper.supportsSearch) {
                item(key = "plugin_search_bar") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        NuvioInputField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = "Search in ${scraper.name}...",
                            trailingContent = if (uiState.searchQuery.isNotBlank()) {
                                {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            // Live Resolving Error Banner (if any)
            if (uiState.liveErrorMessage != null) {
                item(key = "live_error") {
                    Text(
                        text = uiState.liveErrorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // Search Results Mode
            if (uiState.searchQuery.isNotBlank()) {
                if (uiState.isSearching) {
                    item(key = "search_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            NuvioLoadingIndicator()
                        }
                    }
                } else if (uiState.searchResults.isEmpty()) {
                    item(key = "search_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No results found for \"${uiState.searchQuery}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    item(key = "search_results_grid") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = "Search Results",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 0.dp),
                            ) {
                                items(
                                    items = uiState.searchResults,
                                    key = { "search_${it.id}" },
                                ) { item ->
                                    PluginMediaCard(
                                        item = item,
                                        onClick = {
                                            handleMediaItemClick(
                                                item = item,
                                                viewModel = viewModel,
                                                onNavigateToDetail = onNavigateToDetail,
                                                onPlayLiveStream = onPlayLiveStream,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Catalogs Mode: Horizontal Shelves
                if (uiState.sections.isEmpty()) {
                    item(key = "no_catalogs") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No catalogues available in this plugin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.sections,
                        key = { "section_${it.catalog.id}" },
                    ) { section ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = section.catalog.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                            if (section.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    NuvioLoadingIndicator()
                                }
                            } else if (section.items.isEmpty()) {
                                Text(
                                    text = section.error ?: "No items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                ) {
                                    items(
                                        items = section.items,
                                        key = { "item_${section.catalog.id}_${it.id}" },
                                    ) { item ->
                                        PluginMediaCard(
                                            item = item,
                                            onClick = {
                                                handleMediaItemClick(
                                                    item = item,
                                                    viewModel = viewModel,
                                                    onNavigateToDetail = onNavigateToDetail,
                                                    onPlayLiveStream = onPlayLiveStream,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loading overlay for live stream resolution
        if (uiState.isLiveResolving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                NuvioLoadingIndicator()
            }
        }
    }
}

private fun handleMediaItemClick(
    item: PluginMediaItem,
    viewModel: PluginBrowserViewModel,
    onNavigateToDetail: (itemId: String, itemType: String) -> Unit,
    onPlayLiveStream: (streamUrl: String, title: String, headers: Map<String, String>?, logo: String?, poster: String?) -> Unit,
) {
    if (item.type.equals("live", ignoreCase = true)) {
        viewModel.resolveLiveStream(item, onPlayLiveStream)
    } else {
        onNavigateToDetail(item.id, item.type)
    }
}

@Composable
private fun PluginMediaCard(
    item: PluginMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val isLive = item.type.equals("live", ignoreCase = true)

    val cardWidth = if (isLive) 160.dp else 120.dp
    val cardHeight = if (isLive) 100.dp else 175.dp

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .nuvioCardDepth(
                shape = shape,
                surface = NuvioCardDepthSurface.Posters,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = item.poster ?: item.backdrop ?: item.logo
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.displayTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Title label overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // LIVE badge in top-left corner
            if (isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        ),
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }
    }
}
