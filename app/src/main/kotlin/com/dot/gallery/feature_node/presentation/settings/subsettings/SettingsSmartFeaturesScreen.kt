package com.dot.gallery.feature_node.presentation.settings.subsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.dot.gallery.R
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.Position
import com.dot.gallery.core.Settings
import com.dot.gallery.core.SettingsEntity
import com.dot.gallery.core.navigate
import com.dot.gallery.core.workers.forceMetadataCollect
import com.dot.gallery.feature_node.presentation.settings.components.BaseSettingsScreen
import com.dot.gallery.feature_node.presentation.settings.components.rememberPreference
import com.dot.gallery.feature_node.presentation.settings.components.rememberSwitchPreference
import com.dot.gallery.feature_node.presentation.util.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import kotlinx.coroutines.flow.map

@Composable
fun SettingsSmartFeaturesScreen() {
    @Composable
    fun settings(): SnapshotStateList<SettingsEntity> {
        val context = LocalContext.current
        val handler = LocalEventHandler.current
        var noClassification by Settings.Misc.rememberNoClassification()
        val noClassificationPref = rememberPreference(
            noClassification,
            title = stringResource(R.string.categories),
            summary = stringResource(R.string.categorise_your_media),
            onClick = {
                handler.navigate(Screen.CategoriesScreen())
            },
            screenPosition = Position.Alone
        )

        val databaseHeader = remember(context) {
            SettingsEntity.Header(
                title = context.getString(R.string.database)
            )
        }

        // Observe MetadataCollectionWorker state
        val workManager = remember(context) { WorkManager.getInstance(context) }
        val workInfos by remember(workManager) {
            workManager.getWorkInfosFlow(
                WorkQuery.fromUniqueWorkNames("MetadataCollection")
            ).map { infos ->
                infos.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
        }.collectAsStateWithLifecycle(emptyList())

        val isMetadataWorkerRunning = workInfos.isNotEmpty()
        val metadataProgress = workInfos
            .firstOrNull { it.state == WorkInfo.State.RUNNING }
            ?.progress?.getInt("progress", -1) ?: -1

        val metadataSummary = when {
            isMetadataWorkerRunning && metadataProgress in 1..99 ->
                stringResource(R.string.metadata_collecting_progress, metadataProgress)
            isMetadataWorkerRunning ->
                stringResource(R.string.metadata_collecting)
            else ->
                stringResource(R.string.metadata_idle)
        }

        val refreshMetadataPref = rememberPreference(
            isMetadataWorkerRunning, metadataProgress,
            title = stringResource(R.string.refresh_metadata),
            summary = metadataSummary,
            enabled = !isMetadataWorkerRunning,
            onClick = {
                workManager.forceMetadataCollect()
            },
            screenPosition = Position.Alone
        )

        val storageHeader = remember(context) {
            SettingsEntity.Header(
                title = context.getString(R.string.edit_backups_storage)
            )
        }

        val editBackupsPref = rememberPreference(
            title = stringResource(R.string.edit_backups),
            summary = stringResource(R.string.edit_backups_summary),
            icon = Icons.Outlined.SettingsBackupRestore,
            onClick = {
                handler.navigate(Screen.EditBackupsViewerScreen())
            },
            screenPosition = Position.Alone
        )

        return remember(
            noClassificationPref, databaseHeader, refreshMetadataPref,
            storageHeader, editBackupsPref
        ) {
            mutableStateListOf(
                noClassificationPref,
                databaseHeader, refreshMetadataPref,
                storageHeader, editBackupsPref
            )
        }
    }

    BaseSettingsScreen(
        title = stringResource(R.string.ai_category),
        settingsList = settings(),
    )
}