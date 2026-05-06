/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.presentation.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.util.getUri
import com.dot.gallery.feature_node.presentation.util.GlideInvalidation
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan

/**
 * A card showing a static map preview with a circular photo thumbnail,
 * used in the Library screen to replace the plain "Locations" header.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MapPreviewCard(
    modifier: Modifier = Modifier,
    latestMedia: Media.UriMedia?,
    latitude: Double?,
    longitude: Double?,
    isDark: Boolean,
) {
    val mapTileUrl = remember(latitude, longitude, isDark) {
        val lat = latitude ?: 46.77
        val lon = longitude ?: 23.59
        val zoom = 8
        val tileX = lonToTileX(lon, zoom)
        val tileY = latToTileY(lat, zoom)
        if (isDark) {
            "https://a.basemaps.cartocdn.com/dark_all/$zoom/$tileX/$tileY@2x.png"
        } else {
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/$zoom/$tileX/$tileY@2x.png"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Map tile background
        GlideImage(
            model = mapTileUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            requestBuilderTransform = {
                it.diskCacheStrategy(DiskCacheStrategy.ALL)
            }
        )

        // Circular photo thumbnail at center
        if (latestMedia != null) {
            GlideImage(
                model = latestMedia.getUri(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop,
                requestBuilderTransform = {
                    it.signature(GlideInvalidation.signature(latestMedia))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                }
            )
        }
    }
}

// Slippy-map tile math
private fun lonToTileX(lon: Double, zoom: Int): Int {
    return ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()
}

private fun latToTileY(lat: Double, zoom: Int): Int {
    val latRad = Math.toRadians(lat)
    return ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)).toInt()
}
