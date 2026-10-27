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

class DbcWasmWasiTest {
	@AfterTest
	fun tearDown() {
		Dbc.reload()
	}

	@Test
	fun testPlatformDebugDefault() {
		assertFalse(platformDebugDefault())
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testDefaultContractsDisabledInWasi() {
		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testPlatformSettingNonExistent() {
		assertNull(platformSetting("dbc.nonexistent"))
	}

	@Test
	fun testProgrammaticOverrideAndReload() {
		Dbc.setAll(true)
		assertTrue(Dbc.invariants)
		assertTrue(Dbc.preconditions)
		assertTrue(Dbc.postconditions)

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}
}
