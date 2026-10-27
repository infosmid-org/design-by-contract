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

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DbcJsTest {
	@AfterTest
	fun tearDown() {
		js(
			"""(function() {
				if (typeof globalThis !== 'undefined') {
					delete globalThis.DBC_CONFIG;
				}
				if (typeof process !== 'undefined' && process.env) {
					delete process.env['DBC_POSTCONDITION'];
					delete process.env['dbc.postcondition'];
					delete process.env['DBC_PRECONDITION'];
					delete process.env['dbc.precondition'];
					delete process.env['DBC_INVARIANT'];
					delete process.env['dbc.invariant'];
					delete process.env['DBC_ENABLED'];
					delete process.env['dbc.enabled'];
				}
			})()"""
		)
		Dbc.reload()
	}

	@Test
	fun testPlatformDebugDefault() {
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testGlobalDbcConfig() {
		js(
			"""(function() {
				globalThis.DBC_CONFIG = {
					'dbc.invariant': 'false',
					'dbc.precondition': 'true'
				};
			})()"""
		)

		assertEquals("false", platformSetting("dbc.invariant"))
		assertEquals("true", platformSetting("dbc.precondition"))
		assertNull(platformSetting("dbc.nonexistent"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
	}

	@Test
	fun testGlobalEnabledFallback() {
		js(
			"""(function() {
				globalThis.DBC_CONFIG = {
					'dbc.enabled': 'false'
				};
			})()"""
		)
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)

		// Kind-specific setting overrides global setting
		js(
			"""(function() {
				globalThis.DBC_CONFIG = {
					'dbc.enabled': 'false',
					'dbc.invariant': 'true'
				};
			})()"""
		)
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testProcessEnvFallback() {
		val isNode: Boolean = js("typeof process !== 'undefined' && process.env != null") as Boolean
		if (!isNode) return

		js("process.env['DBC_POSTCONDITION'] = 'false';")
		assertEquals("false", platformSetting("dbc.postcondition"))

		Dbc.reload()
		assertFalse(Dbc.postconditions)
	}
}
