package com.dot.gallery.feature_node.presentation.vault

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dot.gallery.R
import com.dot.gallery.core.Constants.Animation.enterAnimation
import com.dot.gallery.core.Constants.Animation.exitAnimation
import com.dot.gallery.core.Constants.Animation.navigateInAnimation
import com.dot.gallery.core.Constants.Animation.navigateUpAnimation
import com.dot.gallery.core.DefaultEventHandler
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.Settings.Misc.rememberForceTheme
import com.dot.gallery.core.Settings.Misc.rememberIsDarkMode
import com.dot.gallery.core.navigate
import com.dot.gallery.core.navigateUp
import com.dot.gallery.feature_node.domain.model.UIEvent
import com.dot.gallery.feature_node.presentation.mediaview.MediaViewScreenRoute
import androidx.compose.runtime.rememberCoroutineScope
import com.dot.gallery.feature_node.presentation.util.SecureWindow
import com.dot.gallery.feature_node.presentation.vault.components.VaultPasswordUnlockDialog
import com.dot.gallery.feature_node.presentation.vault.utils.VaultAuthType
import com.dot.gallery.feature_node.presentation.vault.utils.VaultPasswordManager
import com.dot.gallery.feature_node.presentation.vault.utils.rememberBiometricState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VaultScreen(
    paddingValues: PaddingValues,
    toggleRotate: () -> Unit,
    shouldSkipAuth: MutableState<Boolean>
) = SecureWindow {
    val globalEventHandler = LocalEventHandler.current
    val viewModel = hiltViewModel<VaultViewModel>()
    val navController = rememberNavController()
    var addNewVault by remember { mutableStateOf(false) }

    val localEventHandler = remember { DefaultEventHandler() }
    LaunchedEffect(localEventHandler) {
        localEventHandler.navigateAction = {
            navController.navigate(it) {
                launchSingleTop = true
                restoreState = true
            }
        }
        localEventHandler.navigateUpAction = navController::navigateUp
    }
    LaunchedEffect(localEventHandler) {
        localEventHandler.updaterFlow.collectLatest { event ->
            when (event) {
                is UIEvent.NavigationRouteEvent -> localEventHandler.navigateAction(event.route)
                UIEvent.NavigationUpEvent -> localEventHandler.navigateUpAction()
                is UIEvent.SetFollowThemeEvent -> globalEventHandler.setFollowThemeAction(event.followTheme)
                is UIEvent.ToggleNavigationBarEvent -> globalEventHandler.toggleNavigationBarAction(event.isVisible)
                UIEvent.UpdateDatabase -> {}
            }
        }
    }
    CompositionLocalProvider(
        LocalEventHandler provides localEventHandler
    ) {
        val context = LocalContext.current
        val albumState = viewModel.albumsState.collectAsStateWithLifecycle()
        val metadataState = viewModel.metadataState.collectAsStateWithLifecycle()
        val vaultState = viewModel.vaultState.collectAsStateWithLifecycle()
        val startDestination by remember(vaultState.value) {
            derivedStateOf { vaultState.value.getStartScreen() }
        }

        var isAuthenticated by remember { mutableStateOf(shouldSkipAuth.value) }
        var showPasswordDialog by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var detectedAuthType by remember { mutableStateOf<VaultAuthType?>(null) }
        val wrongPasswordStr = stringResource(R.string.vault_wrong_password)
        val scope = rememberCoroutineScope()
        fun navigateAfterAuth() {
            val vaults = vaultState.value.vaults
            if (vaults.size > 1) {
                localEventHandler.navigate(VaultScreens.VaultSelect())
            } else {
                vaults.firstOrNull()?.let { viewModel.currentVault.value = it }
                localEventHandler.navigate(VaultScreens.VaultDisplay())
            }
        }

        val biometricState = rememberBiometricState(
            title = stringResource(R.string.biometric_authentication),
            subtitle = stringResource(R.string.unlock_your_vault),
            onSuccess = {
                isAuthenticated = true
                navigateAfterAuth()
            },
            onFailed = {
                isAuthenticated = false
                globalEventHandler.navigateUp()
            }
        )
        /** Check for custom password and either show password dialog or biometric prompt. */
        fun startAuth() {
            val firstVault = vaultState.value.vaults.firstOrNull()
            if (firstVault != null) {
                scope.launch {
                    val authType = VaultPasswordManager.getAuthType(context, firstVault.uuid)
                    if (authType != null) {
                        detectedAuthType = authType
                        showPasswordDialog = true
                    } else if (biometricState.isSupported) {
                        biometricState.authenticate()
                    } else {
                        localEventHandler.navigateUp()
                    }
                }
            }
        }

        if (showPasswordDialog) {
            VaultPasswordUnlockDialog(
                authType = detectedAuthType,
                onDismiss = {
                    globalEventHandler.navigateUp()
                },
                onSubmit = { secret ->
                    val firstVault = vaultState.value.vaults.firstOrNull()
                    if (firstVault != null) {
                        scope.launch {
                            if (VaultPasswordManager.verifyPassword(context, firstVault.uuid, secret)) {
                                showPasswordDialog = false
                                passwordError = null
                                isAuthenticated = true
                                navigateAfterAuth()
                            } else {
                                passwordError = wrongPasswordStr
                            }
                        }
                    }
                },
                errorMessage = passwordError
            )
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val systemBarFollowThemeState = rememberSaveable(navBackStackEntry) {
            mutableStateOf(
                navBackStackEntry?.destination?.route?.contains(VaultScreens.EncryptedMediaViewScreen()) == false
            )
        }
        val forcedTheme by rememberForceTheme()
        val localDarkTheme by rememberIsDarkMode()
        val systemDarkTheme = isSystemInDarkTheme()
        val darkTheme by remember(forcedTheme, localDarkTheme, systemDarkTheme) {
            mutableStateOf(if (forcedTheme) localDarkTheme else systemDarkTheme)
        }
        LaunchedEffect(darkTheme, systemBarFollowThemeState.value) {
            (context as? ComponentActivity)?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ) { darkTheme || !systemBarFollowThemeState.value },
                navigationBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ) { darkTheme || !systemBarFollowThemeState.value }
            )
        }

        SharedTransitionLayout {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = startDestination,
                enterTransition = { navigateInAnimation },
                exitTransition = { navigateUpAnimation },
                popEnterTransition = { navigateInAnimation },
                popExitTransition = { navigateUpAnimation }
            ) {
                composable(VaultScreens.LoadingScreen()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                composable(VaultScreens.VaultSelect()) {
                    val itemCounts by viewModel.vaultItemCounts.collectAsStateWithLifecycle()
                    VaultSelectScreen(
                        vaultState = vaultState,
                        vaultItemCounts = itemCounts,
                        onVaultSelected = { vault ->
                            viewModel.currentVault.value = vault
                            navController.navigate(VaultScreens.VaultDisplay()) {
                                popUpTo(VaultScreens.VaultSelect()) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onCreateVault = {
                            addNewVault = true
                            localEventHandler.navigate(VaultScreens.VaultSetup())
                        },
                        onNavigateUp = globalEventHandler::navigateUp
                    )
                }

                composable(VaultScreens.VaultSetup()) {
                    VaultSetup(
                        navigateUp = {
                            addNewVault = false
                            if (vaultState.value.vaults.isEmpty()) globalEventHandler.navigateUp() else localEventHandler.navigateUp()
                        },
                        onCreate = {
                            val wasAddingNewVault = addNewVault
                            addNewVault = false
                            if (wasAddingNewVault && isAuthenticated) {
                                // Already authenticated, just go back to vault display
                                localEventHandler.navigateUp()
                            } else {
                                isAuthenticated = false
                                biometricState.authenticate()
                            }
                        },
                        vm = viewModel
                    )
                }
                composable(VaultScreens.VaultDisplay()) {
                    LaunchedEffect(isAuthenticated, biometricState.isSupported, vaultState) {
                        if (!isAuthenticated && !addNewVault && vaultState.value.vaults.isNotEmpty()) {
                            startAuth()
                        }
                    }
                    AnimatedVisibility(
                        visible = isAuthenticated,
                        enter = enterAnimation,
                        exit = exitAnimation
                    ) {
                        VaultDisplay(
                            globalNavigateUp = globalEventHandler::navigateUp,
                            vaultState = vaultState,
                            currentVault = viewModel.currentVault,
                            createMediaState = viewModel::createMediaState,
                            deleteLeftovers = viewModel::deleteLeftovers,
                            deleteVault = viewModel::deleteVault,
                            setVault = { vault -> viewModel.setVault(vault) {} },
                            onCreateVaultClick = {
                                addNewVault = true
                                localEventHandler.navigate(VaultScreens.VaultSetup())
                            },
                            restoreVault = viewModel::restoreVault,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            workerProgress = viewModel.progress,
                            workerIsRunning = viewModel.isRunning,
                            metadataState = metadataState,
                            encryptAndRequestDeletion = viewModel::encryptAndRequestDeletion,
                            addMediaKeepOriginals = viewModel::addMediaKeepOriginals,
                            pendingDeletions = viewModel.pendingDeletions,
                            userMessage = viewModel.userMessage,
                        )
                    }
                }

                composable(
                    route = VaultScreens.EncryptedMediaViewScreen.id(),
                    arguments = listOf(
                        navArgument("mediaId") {
                            type = NavType.LongType
                        }
                    )
                ) { backStackEntry ->
                    val mediaId = remember(backStackEntry) {
                        backStackEntry.arguments?.getLong("mediaId") ?: -1
                    }
                    val mediaState = remember(viewModel.currentVault.value) {
                        viewModel.createMediaState(viewModel.currentVault.value)
                    }.collectAsStateWithLifecycle()
                    MediaViewScreenRoute(
                        toggleRotate = toggleRotate,
                        paddingValues = paddingValues,
                        mediaId = mediaId,
                        mediaState = mediaState,
                        vaultState = vaultState,
                        albumsState = albumState,
                        metadataState = metadataState,
                        currentVault = viewModel.currentVault.value,
                        restoreMedia = viewModel::restoreMedia,
                        deleteMedia = viewModel::deleteMedia,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable
                    )
                }
            }
        }
    }
}

