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

// In Kotlin/Wasm, js() must be the entire body of a top-level function.
@ExperimentalWasmJsInterop
private fun jsSetting(key: String, env: String): String? = js(
    """(() => {
        const cfg = globalThis.DBC_CONFIG;
        if (cfg != null && cfg[key] != null) return String(cfg[key]);
        const e = globalThis.process?.env;
        const v = e?.[env] ?? e?.[key];
        return v == null ? null : String(v);
    })()"""
)

@ExperimentalWasmJsInterop
private fun jsNodeEnv(): String? = js(
    """(() => { try { return process.env.NODE_ENV ?? null; } catch (e) { return null; } })()"""
)

/**
 * Retrieve the configuration value for [key] in WebAssembly JavaScript environments.
 *
 * Checks `globalThis.DBC_CONFIG` first, then inspects `process.env` in Node.js environments.
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
@ExperimentalWasmJsInterop
internal actual fun platformSetting(key: String): String? = jsSetting(key, envName(key))

/**
 * Determine if contract checks are enabled by default in WebAssembly JavaScript environments.
 *
 * Inspects `process.env.NODE_ENV`. Returns `true` when `NODE_ENV` is defined and does not equal `"production"`.
 *
 * @return `true` if running in a non-production environment; `false` otherwise.
 */
@ExperimentalWasmJsInterop
internal actual fun platformDebugDefault(): Boolean {
    val nodeEnv = jsNodeEnv() ?: return false
    return nodeEnv != "production"
}
