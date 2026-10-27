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

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlin.experimental.ExperimentalNativeApi
import platform.posix.getenv

// Kotlin/Native removes assert() at binary link time unless the binary is built with -ea,
// so the side effect only runs when assertions are on.
@OptIn(ExperimentalNativeApi::class)
private val assertionsEnabled: Boolean = run {
	var enabled = false
	assert(run { enabled = true; true })
	enabled
}

@OptIn(ExperimentalNativeApi::class)
private val debugBinary: Boolean = Platform.isDebugBinary

@OptIn(ExperimentalForeignApi::class)
private fun env(name: String): String? = getenv(name)?.toKString()

/**
 * Retrieve the configuration value for [key] on Kotlin/Native platforms.
 *
 * Consults [hostSetting] first, then inspects environment variables using [envName] and [key].
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun platformSetting(key: String): String? =
	hostSetting(key) ?: env(envName(key)) ?: env(key)

/**
 * Determine if contract checks are enabled by default on Kotlin/Native platforms.
 *
 * Returns `true` if runtime assertions are enabled or if the binary is compiled in debug mode.
 *
 * @return `true` if assertions are enabled or the binary is a debug binary; `false` otherwise.
 */
internal actual fun platformDebugDefault(): Boolean = assertionsEnabled || debugBinary

/**
 * Retrieve the host operating system configuration setting for [key] before consulting environment variables.
 *
 * @param key The configuration key to read.
 * @return The host configuration setting, or `null` if not configured.
 */
internal expect fun hostSetting(key: String): String?
