package com.thedavelopers.eventqr.features

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * Robolectric cannot provide the platform AndroidKeyStore, so SessionManager's
 * EncryptedSharedPreferences-backed storage throws in unit tests. These shadows swap
 * in plain SharedPreferences with the same file name so guard tests can exercise the
 * real activities. Test-only: production code is untouched.
 */
@Implements(MasterKeys::class)
object ShadowMasterKeys {
    @JvmStatic
    @Implementation
    fun getOrCreate(keyGenParameterSpec: android.security.keystore.KeyGenParameterSpec): String = "test-master-key"
}

@Implements(EncryptedSharedPreferences::class)
object ShadowEncryptedSharedPreferences {
    @JvmStatic
    @Implementation
    fun create(
        fileName: String,
        masterKeyAlias: String,
        context: Context,
        keyEncryptionScheme: EncryptedSharedPreferences.PrefKeyEncryptionScheme,
        valueEncryptionScheme: EncryptedSharedPreferences.PrefValueEncryptionScheme,
    ): SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
}
