package com.hlju.funlinkbluetooth.feature.center

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.hlju.funlinkbluetooth.core.designsystem.navigation.PageScaffold
import com.hlju.funlinkbluetooth.core.designsystem.token.Corners
import com.hlju.funlinkbluetooth.core.designsystem.token.Spacing
import com.hlju.funlinkbluetooth.core.designsystem.token.adaptivePageHorizontalPadding
import com.hlju.funlinkbluetooth.core.designsystem.widget.StateMessageCard
import com.hlju.funlinkbluetooth.core.designsystem.widget.StatusBadge
import com.hlju.funlinkbluetooth.core.designsystem.widget.SurfaceTone
import com.hlju.funlinkbluetooth.core.plugin.api.GamePlugin
import com.hlju.funlinkbluetooth.core.plugin.api.NearbyAppAvailability
import com.hlju.funlinkbluetooth.core.plugin.api.NearbyAppAvailabilityState
import com.hlju.funlinkbluetooth.core.plugin.api.NearbyAppLaunchContext
import com.hlju.funlinkbluetooth.core.plugin.api.resolveNearbyAppAvailability
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private val ItemHorizontalPadding = Spacing.LargePlus
private val ItemVerticalPadding = Spacing.PageBase12
private val ItemGap = Spacing.PageBase12
private val ItemIconSize = Spacing.IconExtraLarge

@Composable
fun CenterPage(
    plugins: List<GamePlugin>,
    nearbyAppLaunchContext: NearbyAppLaunchContext,
    bottomInset: Dp,
    onPluginClick: (GamePlugin) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    val filteredPlugins by remember(plugins, query) {
        derivedStateOf {
            if (query.isBlank()) {
                plugins
            } else {
                plugins.filter { plugin ->
                    plugin.displayName.contains(query, ignoreCase = true) ||
                        plugin.name.contains(query, ignoreCase = true) ||
                        plugin.id.contains(query, ignoreCase = true)
                }
            }
        }
    }

    val scrollBehavior = MiuixScrollBehavior()
    val pageHorizontalPadding = adaptivePageHorizontalPadding(Spacing.PageOuterInset)

    PageScaffold(
        title = "游戏中心",
        scrollBehavior = scrollBehavior,
    ) { innerPadding, contentModifier ->
        LazyColumn(
            modifier = contentModifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = pageHorizontalPadding),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + Spacing.PageSectionGap,
                bottom = innerPadding.calculateBottomPadding() + bottomInset + Spacing.PageSectionGap
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.PageSectionGap),
            overscrollEffect = null
        ) {
            item {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = DpSize(Spacing.Zero, Spacing.Zero),
                    inputField = {
                        InputField(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = {},
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            label = "输入程序名称或标识"
                        )
                    },
                    expanded = searchExpanded,
                    onExpandedChange = { searchExpanded = it }
                ) {}
            }

            if (filteredPlugins.isEmpty()) {
                item {
                    StateMessageCard(
                        title = if (query.isBlank()) "暂无可用程序" else "未找到匹配程序",
                        summary = if (query.isBlank()) {
                            "当前还没有注册可展示的插件。"
                        } else {
                            "试试更短的关键词。"
                        },
                        tone = if (query.isBlank()) SurfaceTone.Neutral else SurfaceTone.Warning
                    )
                }
            } else {
                items(filteredPlugins, key = { it.id }) { plugin ->
                    val availability = plugin.resolveNearbyAppAvailability(nearbyAppLaunchContext)
                    PluginRow(
                        plugin = plugin,
                        availability = availability,
                        onClick = { onPluginClick(plugin) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginRow(
    plugin: GamePlugin,
    availability: NearbyAppAvailability,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Corners.PageShape),
        showIndication = availability.canOpen,
        onClick = if (availability.canOpen) onClick else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ItemHorizontalPadding, vertical = ItemVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemGap)
        ) {
            plugin.AppIcon(modifier = Modifier.size(ItemIconSize))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
            ) {
                Text(
                    text = plugin.displayName,
                    style = MiuixTheme.textStyles.main,
                    color = if (availability.canOpen) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.onBackgroundVariant
                    },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "程序标识：${plugin.id}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                    maxLines = 1
                )
                Text(
                    text = availability.summaryText,
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (availability.state == NearbyAppAvailabilityState.QUALITY_INSUFFICIENT) {
                        MiuixTheme.colorScheme.error
                    } else {
                        MiuixTheme.colorScheme.onBackgroundVariant
                    },
                    maxLines = 2
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.SmallPlus)
            ) {
                StatusBadge(
                    text = availability.badgeText,
                    tone = availability.badgeTone()
                )
                if (availability.canOpen) {
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "进入程序",
                        tint = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                }
            }
        }
    }
}

private fun NearbyAppAvailability.badgeTone(): SurfaceTone = when (state) {
    NearbyAppAvailabilityState.READY -> SurfaceTone.Primary
    NearbyAppAvailabilityState.WAITING_FOR_CONNECTION -> SurfaceTone.Neutral
    NearbyAppAvailabilityState.HOST_NEEDS_ROOM,
    NearbyAppAvailabilityState.CLIENT_NEEDS_CONNECTION,
    NearbyAppAvailabilityState.QUALITY_INSUFFICIENT -> SurfaceTone.Warning
}
