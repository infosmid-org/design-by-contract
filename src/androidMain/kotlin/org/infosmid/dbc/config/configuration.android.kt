/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infosmid.dbc.config

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import kotlin.concurrent.Volatile

/**
 * Configuration helper and state holder for Android applications.
 *
 * Android does not support JVM assertion flags (`-ea`). The platform reads configuration in this order:
 * 1. Application manifest `<meta-data>` tags (for example, `<meta-data android:name="dbc.invariant" android:value="false"/>`).
 * 2. Android system properties set with `adb` (for example, `adb shell setprop debug.dbc.invariant true`).
 * 3. Default debug status based on the application debuggable flag (`android:debuggable` or debug build type).
 *
 * [DbcInitProvider] invokes [configure] automatically when the process starts.
 * If you remove [DbcInitProvider] with `tools:node="remove"`, invoke [configure] manually.
 */
object DbcAndroid {
    @Volatile internal var metaData: Bundle? = null
    @Volatile internal var debuggable: Boolean = false

    /**
     * Configure design-by-contract settings using the specified Android [context].
     *
     * Reads application metadata and debug flags, then reloads [Dbc] settings.
     *
     * @param context The Android context used to inspect application information.
     */
    fun configure(context: Context) {
        val app = context.applicationContext ?: context
        val info = runCatching { applicationInfo(app) }.getOrElse { app.applicationInfo }
        debuggable = (info.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        metaData = info.metaData
        Dbc.reload()
    }

    private fun applicationInfo(context: Context): ApplicationInfo {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= 33) {
            pm.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }
    }
}

// android.os.SystemProperties is hidden API; reflection is the conventional workaround.
private val systemPropertiesGet by lazy {
    runCatching {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java, String::class.java)
    }.getOrNull()
}

private fun systemProperty(name: String): String? =
    runCatching { systemPropertiesGet?.invoke(null, name, "") as String? }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }

/**
 * Retrieve the configuration value for [key] from Android metadata, system properties, or debug properties.
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun platformSetting(key: String): String? {
    DbcAndroid.metaData?.let { bundle ->
        @Suppress("DEPRECATION") // Bundle.get(String); meta-data may be Boolean, Int or String
        bundle.get(key)?.let { return it.toString() }
    }
    return runCatching { System.getProperty(key) }.getOrNull()
        ?: systemProperty("debug.$key")
}

/**
 * Determine if contract checks are enabled by default based on the Android debuggable flag.
 *
 * @return `true` if the application is debuggable; `false` otherwise.
 */
internal actual fun platformDebugDefault(): Boolean = DbcAndroid.debuggable

/**
 * Automatic initializer for design-by-contract on Android.
 *
 * Initializes [DbcAndroid] automatically at application startup before other components run.
 */
class DbcInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let(DbcAndroid::configure)
        return true
    }
    override fun query(uri: Uri, p: Array<out String>?, s: String?, a: Array<out String>?, o: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, a: Array<out String>?): Int = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<out String>?): Int = 0
}
