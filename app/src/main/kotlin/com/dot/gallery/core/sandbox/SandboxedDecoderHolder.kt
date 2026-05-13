/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.core.sandbox

import android.content.Context
import com.dot.gallery.core.Settings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

/**
 * Global access point for the [IsolatedImageDecoder] singleton used by
 * Glide and Sketch decoders. Initialized by [com.dot.gallery.GalleryApp].
 *
 * Decoders call [isEnabled] to check the user's sandboxed-decode preference
 * and [decoder] to obtain the shared instance.
 */
object SandboxedDecoderHolder {

    @Volatile
    var decoder: IsolatedImageDecoder? = null
        private set

    fun init(decoder: IsolatedImageDecoder) {
        this.decoder = decoder
    }

    /**
     * Check if sandboxed decoding is enabled via the user's security settings.
     * This is called on Glide/Sketch decode threads so [runBlocking] is acceptable.
     */
    fun isEnabled(context: Context): Boolean {
        return runBlocking {
            Settings.Security.getSandboxedDecode(context).firstOrNull() ?: false
        }
    }
}
