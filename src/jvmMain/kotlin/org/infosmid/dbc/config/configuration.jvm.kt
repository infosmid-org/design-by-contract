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

// -ea, -ea:org.example..., -ea:org.example.dbc.Dbc and -da: are all honoured.
private val assertionsEnabled: Boolean = Dbc::class.java.desiredAssertionStatus()

/**
 * Retrieve the configuration value for [key] on the JVM platform.
 *
 * Checks Java system properties first, then checks system environment variables using [envName].
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun platformSetting(key: String): String? = runCatching {
	System.getProperty(key) ?: System.getenv(envName(key))
}.getOrNull()

/**
 * Determine if contract checks are enabled by default on the JVM platform.
 *
 * Returns `true` if assertions are enabled for [Dbc] (for example, using the `-ea` JVM argument).
 *
 * @return `true` if JVM assertions are enabled; `false` otherwise.
 */
internal actual fun platformDebugDefault(): Boolean = assertionsEnabled
