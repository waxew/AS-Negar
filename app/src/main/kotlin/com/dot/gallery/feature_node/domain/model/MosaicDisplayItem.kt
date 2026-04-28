/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.domain.model

/**
 * Tile size variants for the mosaic layout.
 *
 * TILE_2x2: 2 columns, aspect 1:1   → height = 2w. Companion: QuadTile (span 2)
 * TILE_2x1: 2 columns, aspect 2:1   → height = w.  Companion: 2 singles (span 1 each)
 * TILE_3x2: 3 columns, aspect 3:2   → height = 2w. Companion: PairTile (span 1)
 */
enum class BigTileSize(val colSpan: Int, val aspectRatio: Float) {
    TILE_2x2(2, 1f),
    TILE_2x1(2, 2f),
    TILE_3x2(3, 1.5f),
}

sealed class MosaicDisplayItem<T : Media> {
    abstract val key: String

    data class HeaderItem<T : Media>(val header: MediaItem.Header<T>) : MosaicDisplayItem<T>() {
        override val key = header.key
    }

    data class BigTileItem<T : Media>(
        val mediaItem: MediaItem.MediaViewItem<T>,
        val tileSize: BigTileSize
    ) : MosaicDisplayItem<T>() {
        override val key = "mosaic_big_${tileSize.name}_${mediaItem.key}"
    }

    data class QuadTileItem<T : Media>(val mediaItems: List<MediaItem.MediaViewItem<T>>) : MosaicDisplayItem<T>() {
        override val key = "mosaic_quad_${mediaItems.first().key}"
    }

    data class PairTileItem<T : Media>(val mediaItems: List<MediaItem.MediaViewItem<T>>) : MosaicDisplayItem<T>() {
        override val key = "mosaic_pair_${mediaItems.first().key}"
    }

    data class SingleItem<T : Media>(val mediaItem: MediaItem.MediaViewItem<T>) : MosaicDisplayItem<T>() {
        override val key = mediaItem.key
    }
}
