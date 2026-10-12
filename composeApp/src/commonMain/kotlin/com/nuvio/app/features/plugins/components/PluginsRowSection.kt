package com.nuvio.app.features.plugins.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.nuvio.app.core.ui.NuvioShelfSection
import com.nuvio.app.features.home.components.homeSectionHorizontalPaddingForWidth
import com.nuvio.app.features.plugins.PluginScraper

@Composable
fun PluginsRowSection(
    plugins: List<PluginScraper>,
    modifier: Modifier = Modifier,
    sectionPadding: Dp? = null,
    onPluginClick: (String) -> Unit,
) {
    if (plugins.isEmpty()) return

    if (sectionPadding != null) {
        PluginsRowSectionContent(
            plugins = plugins,
            modifier = modifier.fillMaxWidth(),
            sectionPadding = sectionPadding,
            onPluginClick = onPluginClick,
        )
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            PluginsRowSectionContent(
                plugins = plugins,
                modifier = Modifier.fillMaxWidth(),
                sectionPadding = homeSectionHorizontalPaddingForWidth(maxWidth.value),
                onPluginClick = onPluginClick,
            )
        }
    }
}

@Composable
private fun PluginsRowSectionContent(
    plugins: List<PluginScraper>,
    modifier: Modifier,
    sectionPadding: Dp,
    onPluginClick: (String) -> Unit,
) {
    NuvioShelfSection(
        title = "Plugins",
        entries = plugins,
        modifier = modifier,
        headerHorizontalPadding = sectionPadding,
        rowContentPadding = PaddingValues(horizontal = sectionPadding),
        key = { plugin -> "plugin_${plugin.id}" },
    ) { plugin ->
        PluginCard(
            plugin = plugin,
            onClick = { onPluginClick(plugin.id) },
        )
    }
}
