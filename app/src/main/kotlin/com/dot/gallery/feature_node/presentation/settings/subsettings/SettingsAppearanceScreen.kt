/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.presentation.settings.subsettings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dot.gallery.R
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.Position
import com.dot.gallery.core.Settings
import com.dot.gallery.core.SettingsEntity
import com.dot.gallery.core.navigate
import com.dot.gallery.feature_node.presentation.settings.components.BaseSettingsScreen
import com.dot.gallery.feature_node.presentation.settings.components.SettingsItem
import com.dot.gallery.feature_node.presentation.settings.components.SwitchPreferenceDetailScreen
import com.dot.gallery.feature_node.presentation.settings.components.rememberSwitchPreference
import com.dot.gallery.feature_node.presentation.util.Screen
import com.dot.gallery.ui.theme.colorSchemeFromSeed
import com.dot.gallery.ui.theme.neutralColorScheme

private const val DETAIL_FOLLOW_SYSTEM = "follow_system"
private const val DETAIL_DARK_MODE = "dark_mode"
private const val DETAIL_AMOLED = "amoled"
private const val DETAIL_BLUR = "blur"
private const val DETAIL_SHARED = "shared"

@Composable
fun SettingsAppearanceScreen() {
    var detailKey by rememberSaveable { mutableStateOf<String?>(null) }

    val eventHandler = LocalEventHandler.current
    var themeColorSeed by Settings.Misc.rememberThemeColorSeed()
    var forceTheme by Settings.Misc.rememberForceTheme()
    var darkModeValue by Settings.Misc.rememberIsDarkMode()
    var amoledModeValue by Settings.Misc.rememberIsAmoledMode()
    val shouldAllowBlur = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
    var allowBlur by Settings.Misc.rememberAllowBlur()
    var sharedElements by Settings.Misc.rememberSharedElements()

    // Description strings (hoisted for reuse in detail screens)
    val followSystemDesc = stringResource(R.string.follow_system_theme_description)
    val darkModeDesc = stringResource(R.string.dark_mode_description)
    val amoledDesc = stringResource(R.string.amoled_mode_description)
    val blurDesc = stringResource(R.string.fancy_blur_description)
    val sharedDesc = stringResource(R.string.shared_elements_description)

    // Title strings
    val followSystemTitle = stringResource(R.string.settings_follow_system_theme_title)
    val darkModeTitle = stringResource(R.string.settings_dark_mode_title)
    val amoledTitle = stringResource(R.string.amoled_mode_title)
    val blurTitle = stringResource(R.string.fancy_blur)
    val sharedTitle = stringResource(R.string.shared_elements)

    when (detailKey) {
        DETAIL_FOLLOW_SYSTEM -> {
            BackHandler { detailKey = null }
            SwitchPreferenceDetailScreen(
                title = followSystemTitle,
                isChecked = !forceTheme,
                onCheckedChange = { forceTheme = !it },
                description = followSystemDesc,
            )
        }
        DETAIL_DARK_MODE -> {
            BackHandler { detailKey = null }
            SwitchPreferenceDetailScreen(
                title = darkModeTitle,
                isChecked = darkModeValue,
                onCheckedChange = { darkModeValue = it },
                description = darkModeDesc,
                preview = { checked -> DarkModePreview(checked) },
                enabled = forceTheme,
            )
        }
        DETAIL_AMOLED -> {
            BackHandler { detailKey = null }
            SwitchPreferenceDetailScreen(
                title = amoledTitle,
                isChecked = amoledModeValue,
                onCheckedChange = { amoledModeValue = it },
                description = amoledDesc,
                preview = { checked -> AmoledPreview(checked) },
            )
        }
        DETAIL_BLUR -> {
            BackHandler { detailKey = null }
            SwitchPreferenceDetailScreen(
                title = blurTitle,
                isChecked = allowBlur,
                onCheckedChange = { allowBlur = it },
                description = blurDesc,
                preview = { checked -> BlurPreview(checked) },
                enabled = shouldAllowBlur,
            )
        }
        DETAIL_SHARED -> {
            BackHandler { detailKey = null }
            SwitchPreferenceDetailScreen(
                title = sharedTitle,
                isChecked = sharedElements,
                onCheckedChange = { sharedElements = it },
                description = sharedDesc,
            )
        }
        else -> {
            AppearanceListScreen(
                themeColorSeed = themeColorSeed,
                forceTheme = forceTheme,
                onForceThemeChange = { forceTheme = !it },
                darkModeValue = darkModeValue,
                onDarkModeChange = { darkModeValue = it },
                amoledModeValue = amoledModeValue,
                onAmoledChange = { amoledModeValue = it },
                shouldAllowBlur = shouldAllowBlur,
                allowBlur = allowBlur,
                onAllowBlurChange = { allowBlur = it },
                sharedElements = sharedElements,
                onSharedElementsChange = { sharedElements = it },
                onNavigatePalette = { eventHandler.navigate(Screen.ColorPaletteScreen()) },
                onDetailClick = { detailKey = it },
            )
        }
    }
}

@Composable
private fun AppearanceListScreen(
    themeColorSeed: String,
    forceTheme: Boolean,
    onForceThemeChange: (Boolean) -> Unit,
    darkModeValue: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    amoledModeValue: Boolean,
    onAmoledChange: (Boolean) -> Unit,
    shouldAllowBlur: Boolean,
    allowBlur: Boolean,
    onAllowBlurChange: (Boolean) -> Unit,
    sharedElements: Boolean,
    onSharedElementsChange: (Boolean) -> Unit,
    onNavigatePalette: () -> Unit,
    onDetailClick: (String) -> Unit,
) {
    @Composable
    fun settings(): SnapshotStateList<SettingsEntity> {
        val paletteSummary = if (themeColorSeed == Settings.Misc.THEME_SEED_SYSTEM) {
            stringResource(R.string.color_palette_system)
        } else if (themeColorSeed == Settings.Misc.THEME_SEED_NEUTRAL) {
            stringResource(R.string.color_palette_neutral)
        } else {
            presetPalettes.find { it.hexKey == themeColorSeed }?.name
                ?: stringResource(R.string.color_palette_custom)
        }
        val paletteTitle = stringResource(R.string.color_palette_title)
        val palettePref = remember(themeColorSeed, paletteTitle, paletteSummary) {
            SettingsEntity.Preference(
                title = paletteTitle,
                summary = paletteSummary,
                icon = Icons.Outlined.Palette,
                onClick = onNavigatePalette,
                screenPosition = Position.Alone
            )
        }

        val forceThemeValuePref = rememberSwitchPreference(
            forceTheme,
            title = stringResource(R.string.settings_follow_system_theme_title),
            summary = stringResource(R.string.follow_system_theme_description),
            isChecked = !forceTheme,
            onCheck = onForceThemeChange,
            onClick = { onDetailClick(DETAIL_FOLLOW_SYSTEM) },
            screenPosition = Position.Top
        )
        val darkThemePref = rememberSwitchPreference(
            darkModeValue, forceTheme,
            title = stringResource(R.string.settings_dark_mode_title),
            summary = stringResource(R.string.dark_mode_description),
            enabled = forceTheme,
            isChecked = darkModeValue,
            onCheck = onDarkModeChange,
            onClick = { onDetailClick(DETAIL_DARK_MODE) },
            screenPosition = Position.Middle
        )
        val amoledModePref = rememberSwitchPreference(
            amoledModeValue,
            title = stringResource(R.string.amoled_mode_title),
            summary = stringResource(R.string.amoled_mode_summary),
            isChecked = amoledModeValue,
            onCheck = onAmoledChange,
            onClick = { onDetailClick(DETAIL_AMOLED) },
            screenPosition = Position.Bottom
        )

        val effectsHeader = remember {
            SettingsEntity.Header(title = "Visual Effects")
        }

        val allowBlurPref = rememberSwitchPreference(
            allowBlur,
            title = stringResource(R.string.fancy_blur),
            summary = stringResource(R.string.fancy_blur_summary),
            isChecked = allowBlur,
            onCheck = onAllowBlurChange,
            onClick = { onDetailClick(DETAIL_BLUR) },
            enabled = shouldAllowBlur,
            screenPosition = Position.Top
        )

        val sharedElementsPref = rememberSwitchPreference(
            sharedElements,
            title = stringResource(R.string.shared_elements),
            summary = stringResource(R.string.shared_elements_summary),
            isChecked = sharedElements,
            onCheck = onSharedElementsChange,
            onClick = { onDetailClick(DETAIL_SHARED) },
            screenPosition = Position.Bottom
        )

        return remember(
            palettePref, forceThemeValuePref, darkThemePref, amoledModePref,
            allowBlurPref, sharedElementsPref
        ) {
            mutableStateListOf(
                palettePref,
                forceThemeValuePref,
                darkThemePref,
                amoledModePref,
                effectsHeader,
                allowBlurPref,
                sharedElementsPref
            )
        }
    }

    val isDark = isSystemInDarkTheme()
    val swatchColors = remember(themeColorSeed, isDark) {
        if (themeColorSeed == Settings.Misc.THEME_SEED_SYSTEM) {
            null
        } else if (themeColorSeed == Settings.Misc.THEME_SEED_NEUTRAL) {
            val scheme = neutralColorScheme(isDark)
            listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.primaryContainer)
        } else {
            val seedArgb = themeColorSeed.toLongOrNull(16)?.toInt() ?: return@remember null
            val scheme = colorSchemeFromSeed(seedArgb, isDark)
            listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.primaryContainer)
        }
    }

    BaseSettingsScreen(
        title = stringResource(R.string.settings_appearance),
        settingsList = settings(),
        settingsBuilder = { item, index ->
            if (index == 0) {
                SettingsItem(
                    item = item,
                    customTrailingContent = {
                        ColorSwatch(swatchColors)
                    }
                )
            } else {
                SettingsItem(item)
            }
        }
    )
}

@Composable
private fun DarkModePreview(isChecked: Boolean) {
    val bg by animateColorAsState(if (isChecked) Color(0xFF121212) else Color(0xFFFEFBFF), label = "bg")
    val surface by animateColorAsState(if (isChecked) Color(0xFF2B2B2B) else Color(0xFFE8DEE8), label = "sf")
    val content by animateColorAsState(if (isChecked) Color(0xFFDADADA) else Color(0xFF1C1C1C), label = "ct")
    MiniPhoneFrame(backgroundColor = bg, surfaceColor = surface, contentColor = content)
}

@Composable
private fun AmoledPreview(isChecked: Boolean) {
    val bg by animateColorAsState(if (isChecked) Color.Black else Color(0xFF121212), label = "bg")
    val surface by animateColorAsState(if (isChecked) Color(0xFF0D0D0D) else Color(0xFF2B2B2B), label = "sf")
    MiniPhoneFrame(backgroundColor = bg, surfaceColor = surface, contentColor = Color(0xFFDADADA))
}

@Composable
private fun BlurPreview(isChecked: Boolean) {
    val backgroundColor = Color(0xFF121212)
    val contentColor = Color(0xFFDADADA)
    val surfaceColor = Color(0xFF2B2B2B)
    val barAlpha by animateFloatAsState(if (isChecked) 0.55f else 1f, label = "barAlpha")
    val gridColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
    )
    Box(
        modifier = Modifier
            .padding(24.dp)
            .size(width = 120.dp, height = 200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        // Media grid content (extends full height, behind bars)
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(5) { row ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(3) { col ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(gridColors[(row + col) % gridColors.size])
                        )
                    }
                }
            }
        }
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(surfaceColor.copy(alpha = barAlpha))
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(30.dp, 5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(contentColor.copy(alpha = 0.25f))
            )
        }
        // Bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(20.dp)
                .background(surfaceColor.copy(alpha = barAlpha))
        )
    }
}

@Composable
private fun MiniPhoneFrame(
    backgroundColor: Color,
    surfaceColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .size(width = 120.dp, height = 200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(24.dp).background(surfaceColor))
            Column(Modifier.weight(1f).padding(6.dp), Arrangement.spacedBy(3.dp)) {
                Box(Modifier.size(40.dp, 5.dp).clip(RoundedCornerShape(2.dp)).background(contentColor.copy(alpha = 0.25f)))
                repeat(3) {
                    Row(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                        repeat(3) {
                            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(surfaceColor))
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(20.dp).background(surfaceColor))
        }
    }
}

@Composable
private fun ColorSwatch(colors: List<Color>?) {
    if (colors == null) return
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Column(modifier = Modifier.matchParentSize()) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors[0])
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors[1])
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors[2])
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors[3])
                )
            }
        }
    }
}
