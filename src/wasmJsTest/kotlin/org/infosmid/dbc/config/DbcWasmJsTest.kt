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

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalWasmJsInterop::class)
private fun setGlobalDbcConfig(key: String, value: String): Unit = js(
	"""(() => {
		if (typeof globalThis.DBC_CONFIG === 'undefined' || globalThis.DBC_CONFIG == null) {
			globalThis.DBC_CONFIG = {};
		}
		globalThis.DBC_CONFIG[key] = value;
	})()"""
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun clearGlobalDbcConfig(): Unit = js(
	"""(() => {
		if (typeof globalThis !== 'undefined') {
			delete globalThis.DBC_CONFIG;
		}
	})()"""
)
@ExperimentalWasmJsInterop
class DbcWasmJsTest {
	@AfterTest
	fun tearDown() {
		clearGlobalDbcConfig()
		Dbc.reload()
	}

	@Test
	fun testPlatformDebugDefault() {
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testGlobalDbcConfig() {
		setGlobalDbcConfig("dbc.invariant", "false")
		setGlobalDbcConfig("dbc.precondition", "true")

		assertEquals("false", platformSetting("dbc.invariant"))
		assertEquals("true", platformSetting("dbc.precondition"))
		assertNull(platformSetting("dbc.nonexistent"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
	}

	@Test
	fun testGlobalEnabledFallback() {
		setGlobalDbcConfig("dbc.enabled", "false")
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)

		setGlobalDbcConfig("dbc.invariant", "true")
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}
}
