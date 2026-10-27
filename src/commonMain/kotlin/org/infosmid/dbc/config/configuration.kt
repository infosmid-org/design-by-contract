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

import kotlin.concurrent.Volatile

/**
 * Categories of contract checks that you can configure independently.
 *
 * @property key The configuration property key name for this check category.
 */
enum class DbcKind(val key: String) {
	PRECONDITION("dbc.precondition"),
	POSTCONDITION("dbc.postcondition"),
	INVARIANT("dbc.invariant"),
}

/**
 * Global configuration and switches for design-by-contract checks.
 *
 * The system determines the state of each check category using this priority order:
 * 1. The category setting (for example, `dbc.invariant` or `DBC_INVARIANT`).
 * 2. The global setting (`dbc.enabled` or `DBC_ENABLED`).
 * 3. The platform debug default ([platformDebugDefault]).
 *
 * Settings originate from platform configuration or programmatic changes.
 */
object Dbc {
	/**
	 * Configuration key for the global switch that controls all contract checks.
	 */
	const val ENABLED_KEY: String = "dbc.enabled"
	
	/**
	 * Status of precondition checks. Set to `true` to enable or `false` to disable.
	 */
	@Volatile var preconditions: Boolean = resolve(DbcKind.PRECONDITION)

	/**
	 * Status of postcondition checks. Set to `true` to enable or `false` to disable.
	 */
	@Volatile var postconditions: Boolean = resolve(DbcKind.POSTCONDITION)

	/**
	 * Status of invariant checks. Set to `true` to enable or `false` to disable.
	 */
	@Volatile var invariants: Boolean = resolve(DbcKind.INVARIANT)
	
	/**
	 * Default debug status provided by the platform.
	 */
	val platformDefault: Boolean get() = platformDebugDefault()
	
	/**
	 * Check if checks for the specified [kind] are enabled.
	 *
	 * @param kind The category of contract check to inspect.
	 * @return `true` if checks for [kind] are enabled; `false` otherwise.
	 */
	fun isEnabled(kind: DbcKind): Boolean = when (kind) {
		DbcKind.PRECONDITION -> preconditions
		DbcKind.POSTCONDITION -> postconditions
		DbcKind.INVARIANT -> invariants
	}
	
	/**
	 * Enable or disable contract checks for the specified [kind].
	 *
	 * @param kind The category of contract check to update.
	 * @param enabled `true` to enable checks; `false` to disable checks.
	 */
	fun set(kind: DbcKind, enabled: Boolean) {
		when (kind) {
			DbcKind.PRECONDITION -> preconditions = enabled
			DbcKind.POSTCONDITION -> postconditions = enabled
			DbcKind.INVARIANT -> invariants = enabled
		}
	}
	
	/**
	 * Enable or disable contract checks for all categories.
	 *
	 * @param enabled `true` to enable checks; `false` to disable checks.
	 */
	fun setAll(enabled: Boolean) {
		DbcKind.entries.forEach { set(it, enabled) }
	}
	
	/**
	 * Reload configuration settings from the platform and reset all categories.
	 *
	 * This operation discards previous programmatic changes.
	 */
	fun reload() {
		DbcKind.entries.forEach { set(it, resolve(it)) }
	}
	
	private fun resolve(kind: DbcKind): Boolean =
		resolveFlag(kind, ::platformSetting, platformDebugDefault())
}

/**
 * Resolve the boolean configuration flag for a specified check category.
 *
 * @param kind The contract check category to resolve.
 * @param setting Function to read a configuration key from the platform.
 * @param debugDefault Default boolean value when no configuration key exists.
 * @return The resolved boolean configuration value.
 */
internal fun resolveFlag(kind: DbcKind, setting: (String) -> String?, debugDefault: Boolean): Boolean =
	parseFlag(setting(kind.key)) ?: parseFlag(setting(Dbc.ENABLED_KEY)) ?: debugDefault

/**
 * Parse a string value into a boolean flag.
 *
 * Valid true values include: `true`, `1`, `yes`, `on`, and `enabled`.
 * Valid false values include: `false`, `0`, `no`, `off`, and `disabled`.
 *
 * @param value The string to parse.
 * @return `true` or `false` if the value matches a known token; `null` otherwise.
 */
internal fun parseFlag(value: String?): Boolean? = when (value?.trim()?.lowercase()) {
	"true", "1", "yes", "on", "enabled" -> true
	"false", "0", "no", "off", "disabled" -> false
	else -> null
}

/**
 * Convert a dot-separated configuration key into an uppercase environment variable name.
 *
 * For example, converts `dbc.invariant` to `DBC_INVARIANT`.
 *
 * @param key The property key to convert.
 * @return The uppercase environment variable name with dots replaced by underscores.
 */
internal fun envName(key: String): String = key.uppercase().replace('.', '_')

/**
 * Retrieve the raw configuration value for [key] from the platform environment.
 *
 * @param key The configuration key to read.
 * @return The raw configuration string, or `null` if not configured.
 */
internal expect fun platformSetting(key: String): String?

/**
 * Determine if contract checks are enabled by default on the platform when no configuration exists.
 *
 * @return `true` if checks are enabled by default; `false` otherwise.
 */
internal expect fun platformDebugDefault(): Boolean
