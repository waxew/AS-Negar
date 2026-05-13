/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.room.Room
import androidx.work.WorkManager
import com.dot.gallery.core.DefaultEventHandler
import com.dot.gallery.core.EditBackupManager
import com.dot.gallery.core.encryption.EncryptedDatabaseFactory
import com.dot.gallery.core.sandbox.IsolatedImageDecoder
import com.dot.gallery.core.sandbox.IsolatedMetadataParser
import com.dot.gallery.core.sandbox.PrivateFolderRepository
import com.dot.gallery.core.MediaDistributor
import com.dot.gallery.core.MediaDistributorImpl
import com.dot.gallery.core.MediaHandler
import com.dot.gallery.core.MediaHandlerImpl
import com.dot.gallery.core.MediaSelector
import com.dot.gallery.core.MediaSelectorImpl
import com.dot.gallery.feature_node.data.data_source.InternalDatabase
import com.dot.gallery.feature_node.data.data_source.KeychainHolder
import com.dot.gallery.feature_node.data.data_source.migration.MIGRATION_12_13
import com.dot.gallery.feature_node.data.repository.MediaRepositoryImpl
import com.dot.gallery.feature_node.domain.repository.MediaRepository
import com.dot.gallery.feature_node.domain.util.EventHandler
import com.dot.gallery.core.ml.ModelManager
import com.dot.gallery.feature_node.presentation.search.SearchHelper
import com.dot.gallery.feature_node.presentation.search.SearchHelperImpl
import com.dot.gallery.core.decryption.DecryptManager
import com.dot.gallery.core.decryption.MediaMetadataSidecarCache
import com.dot.gallery.core.memory.AdaptiveDecryptConfig
import com.dot.gallery.core.metrics.MetricsCollector
import com.dot.gallery.core.memory.ByteArrayPool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    @Singleton
    fun provideDatabase(app: Application): InternalDatabase {
        return try {
            EncryptedDatabaseFactory.create(app)
        } catch (_: Exception) {
            // Device doesn't support SQLCipher or hardware-backed keystore —
            // fall back to plaintext database silently.
            Room.databaseBuilder(app, InternalDatabase::class.java, InternalDatabase.NAME)
                .addMigrations(MIGRATION_12_13)
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideKeychainHolder(@ApplicationContext context: Context): KeychainHolder =
        KeychainHolder(context)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideEventHandler(): EventHandler = DefaultEventHandler()

    @Provides
    @Singleton
    fun provideMediaDistributor(
        @ApplicationContext context: Context,
        workManager: WorkManager,
        repository: MediaRepository,
        eventHandler: EventHandler,
        database: InternalDatabase
    ): MediaDistributor = MediaDistributorImpl(context, repository, eventHandler, workManager, database.getScannedMediaDao())

    @Provides
    @Singleton
    fun provideMediaSelector(): MediaSelector = MediaSelectorImpl()

    @Provides
    @Singleton
    fun provideMediaHandler(
        @ApplicationContext context: Context,
        mediaRepository: MediaRepository,
        workManager: WorkManager,
    ): MediaHandler = MediaHandlerImpl(mediaRepository, context, workManager)

    @Provides
    @Singleton
    fun provideIsolatedMetadataParser(@ApplicationContext context: Context): IsolatedMetadataParser =
        IsolatedMetadataParser(context)

    @Provides
    @Singleton
    fun provideIsolatedImageDecoder(@ApplicationContext context: Context): IsolatedImageDecoder =
        IsolatedImageDecoder(context)

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context,
        workManager: WorkManager,
        database: InternalDatabase,
        keychainHolder: KeychainHolder,
        geocoder: Geocoder?,
        isolatedParser: IsolatedMetadataParser,
    ): MediaRepository = MediaRepositoryImpl(context, workManager, database, keychainHolder, geocoder, isolatedParser)

    @Provides
    @Singleton
    fun provideModelManager(@ApplicationContext context: Context): ModelManager = ModelManager(context)

    @Provides
    @Singleton
    fun provideSearchHelper(modelManager: ModelManager): SearchHelper = SearchHelperImpl(modelManager)

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Geocoder.isPresent()) Geocoder(context) else null

    @Provides
    @Singleton
    fun provideDecryptManager(@ApplicationContext context: Context, metrics: MetricsCollector): DecryptManager = DecryptManager(context, metrics)

    @Provides
    @Singleton
    fun provideMediaMetadataSidecarCache(@ApplicationContext context: Context): MediaMetadataSidecarCache = MediaMetadataSidecarCache(context)

    @Provides
    @Singleton
    fun provideAdaptiveDecryptConfig(app: Application): AdaptiveDecryptConfig = AdaptiveDecryptConfig(app)

    @Provides
    @Singleton
    fun provideMetricsCollector(): MetricsCollector = MetricsCollector()

    @Provides
    @Singleton
    fun provideByteArrayPool(): ByteArrayPool = ByteArrayPool()

    @Provides
    @Singleton
    fun providePrivateFolderRepository(@ApplicationContext context: Context): PrivateFolderRepository =
        PrivateFolderRepository(context)

    @Provides
    @Singleton
    fun provideEditBackupManager(
        @ApplicationContext context: Context,
        database: InternalDatabase
    ): EditBackupManager = EditBackupManager(context, database.getEditHistoryDao())

}
