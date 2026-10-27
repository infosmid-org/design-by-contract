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

import kotlin.wasm.WasmImport
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmImport("wasi_snapshot_preview1", "environ_sizes_get")
@ExperimentalWasmInterop
private external fun wasiEnvironSizesGet(countPtr: Int, bufSizePtr: Int): Int

@WasmImport("wasi_snapshot_preview1", "environ_get")
@ExperimentalWasmInterop
private external fun wasiEnvironGet(environPtr: Int, environBufPtr: Int): Int

/**
 * Snapshot of the WASI environment variables.
 *
 * Populated from WASI runtime environment variables (for example, `wasmtime run --env DBC_ENABLED=1`).
 */
@OptIn(UnsafeWasmMemoryApi::class)
private val environment: Map<String, String> by lazy {
    withScopedMemoryAllocator { allocator ->
        val countPtr = allocator.allocate(4)
        val sizePtr = allocator.allocate(4)
        if (wasiEnvironSizesGet(countPtr.address.toInt(), sizePtr.address.toInt()) != 0) {
            return@withScopedMemoryAllocator emptyMap()
        }
        val count = countPtr.loadInt()
        val size = sizePtr.loadInt()
        if (count == 0 || size == 0) return@withScopedMemoryAllocator emptyMap()
        
        val pointers = allocator.allocate(count * 4)
        val buffer = allocator.allocate(size)
        if (wasiEnvironGet(pointers.address.toInt(), buffer.address.toInt()) != 0) {
            return@withScopedMemoryAllocator emptyMap()
        }
        // The buffer holds count NUL-terminated "NAME=value" entries back to back.
        val bytes = ByteArray(size) { (buffer + it).loadByte() }
        val result = HashMap<String, String>(count)
        var start = 0
        for (i in bytes.indices) {
            if (bytes[i] == 0.toByte()) {
                val entry = bytes.decodeToString(start, i)
                val eq = entry.indexOf('=')
                if (eq > 0) result[entry.substring(0, eq)] = entry.substring(eq + 1)
                start = i + 1
            }
        }
        result
    }
}

/**
 * Retrieve the configuration value for [key] in WebAssembly WASI environments.
 *
 * Inspects WASI environment variables using [envName] and [key].
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun platformSetting(key: String): String? =
    environment[envName(key)] ?: environment[key]

/**
 * Determine if contract checks are enabled by default in WebAssembly WASI environments.
 *
 * WASI environments do not provide a standard debug flag. Checks are disabled by default unless configured.
 *
 * @return Always returns `false`.
 */
internal actual fun platformDebugDefault(): Boolean = false
