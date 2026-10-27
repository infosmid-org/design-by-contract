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

// Lookup order: globalThis.DBC_CONFIG["dbc.invariant"], then process.env (Node).
private fun jsSetting(key: String, env: String): dynamic = js(
    """(function () {
        var g = typeof globalThis !== 'undefined' ? globalThis : {};
        var cfg = g.DBC_CONFIG;
        if (cfg != null && cfg[key] != null) return String(cfg[key]);
        var p = g.process;
        if (p != null && p.env != null) {
            if (p.env[env] != null) return p.env[env];
            if (p.env[key] != null) return p.env[key];
        }
        return null;
    })()"""
)

// Written literally as process.env.NODE_ENV so bundlers can substitute it at build time.
private fun jsNodeEnv(): dynamic = js(
    """(function () { try { return process.env.NODE_ENV; } catch (e) { return undefined; } })()"""
)

/**
 * Retrieve the configuration value for [key] in JavaScript environments.
 *
 * Checks `globalThis.DBC_CONFIG` first, then inspects `process.env` in Node.js environments.
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun platformSetting(key: String): String? =
    jsSetting(key, envName(key))?.toString()

/**
 * Determine if contract checks are enabled by default in JavaScript environments.
 *
 * Inspects `process.env.NODE_ENV`. Returns `true` when `NODE_ENV` is defined and does not equal `"production"`.
 *
 * @return `true` if running in a non-production environment; `false` otherwise.
 */
internal actual fun platformDebugDefault(): Boolean {
    val nodeEnv = jsNodeEnv()?.toString() ?: return false
    return nodeEnv != "production"
}
