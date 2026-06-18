/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.presentation.standalone

import android.app.KeyguardManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.gallery.core.DefaultEventHandler
import com.dot.gallery.core.MediaDistributor
import com.dot.gallery.core.MediaHandler
import com.dot.gallery.core.MediaSelector
import com.dot.gallery.core.Settings.Misc.rememberAllowBlur
import com.dot.gallery.core.util.SetupMediaProviders
import com.dot.gallery.feature_node.domain.model.UIEvent
import com.dot.gallery.feature_node.domain.util.EventHandler
import com.dot.gallery.feature_node.presentation.mediaview.MediaViewScreenRoute
import com.dot.gallery.feature_node.presentation.util.LocalHazeState
import com.dot.gallery.feature_node.presentation.util.toggleOrientation
import com.dot.gallery.ui.theme.GalleryTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

@AndroidEntryPoint
open class StandaloneActivity : AppCompatActivity() {

    private val eventHandler: EventHandler = DefaultEventHandler()

    @Inject
    lateinit var mediaDistributor: MediaDistributor

    @Inject
    lateinit var mediaHandler: MediaHandler

    @Inject
    lateinit var mediaSelector: MediaSelector

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalHazeMaterialsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        val action = intent.action.toString()
        // A review can be "secure" in two ways:
        //  - an explicit ACTION_REVIEW_SECURE action, or
        //  - a plain ACTION_REVIEW launched while the device is locked. Google
        //    Camera's secure session uses the latter: it sends
        //    com.android.camera.action.REVIEW (no "secure" in the action) from
        //    over the keyguard, so action-name matching alone misses it and the
        //    keyguard would prompt for unlock before showing the photo.
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        val isSecure = action.lowercase().contains("secure") ||
                keyguardManager?.isKeyguardLocked == true
        val clipData = intent.clipData
        val uriList = mutableSetOf<Uri>()
        intent.data?.let(uriList::add)
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                uriList.add(clipData.getItemAt(i).uri)
            }
        }
        // Occlude (and wake over) the lock screen when the review is secure.
        // This is only enabled when launched while locked, so a normal review
        // that is later locked mid-viewing never bypasses the keyguard.
        setShowWhenLocked(isSecure)
        setTurnScreenOn(isSecure)
        setContent {
            GalleryTheme {
                val allowBlur by rememberAllowBlur()
                val hazeState = rememberHazeState(
                    blurEnabled = allowBlur
                )
                val viewModel =
                    hiltViewModel<StandaloneViewModel, StandaloneViewModel.Factory> { factory ->
                        factory.create(
                            reviewMode = action.contains("REVIEW", true),
                            isSecure = isSecure,
                            dataList = uriList.toList()
                        )
                    }
                CompositionLocalProvider(
                    LocalHazeState provides hazeState,
                    LocalHazeStyle provides HazeMaterials.thin(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    LaunchedEffect(Unit) {
                        eventHandler.navigateUpAction = { finish() }
                    }
                    LaunchedEffect(eventHandler) {
                        eventHandler.updaterFlow.collect {
                            if (it == UIEvent.NavigationUpEvent) {
                                finish()
                            }
                        }
                    }
                    SetupMediaProviders(
                        eventHandler = eventHandler,
                        mediaDistributor = mediaDistributor,
                        mediaHandler = mediaHandler,
                        mediaSelector = mediaSelector
                    ) {
                        Scaffold { paddingValues ->
                            val vaults = viewModel.vaults.collectAsStateWithLifecycle()
                            val mediaState = viewModel.mediaState.collectAsStateWithLifecycle()
                            val albumsState = viewModel.albumsState.collectAsStateWithLifecycle()
                            val metadataState =
                                viewModel.metadataState.collectAsStateWithLifecycle()
                            val mediaId by viewModel.mediaId.collectAsStateWithLifecycle()
                            val staticState by remember { mutableStateOf(true) }
                            SharedTransitionLayout {
                                AnimatedContent(
                                    targetState = staticState,
                                    label = "standalone"
                                ) { staticState ->
                                    if (staticState) {
                                        MediaViewScreenRoute(
                                            toggleRotate = ::toggleOrientation,
                                            paddingValues = paddingValues,
                                            isStandalone = true,
                                            mediaId = mediaId,
                                            mediaState = mediaState,
                                            vaultState = vaults,
                                            albumsState = albumsState,
                                            metadataState = metadataState,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedContentScope = this
                                        )
                                    }
                                }
                            }
                        }
                        BackHandler {
                            finish()
                        }
                    }
                }
            }
        }
    }

}