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

class DbcJvmTest {
	@AfterTest
	fun tearDown() {
		System.clearProperty("dbc.enabled")
		System.clearProperty("dbc.precondition")
		System.clearProperty("dbc.invariant")
		// Restore gradle test default
		System.setProperty("dbc.postcondition", "false")
		Dbc.reload()
	}

	// Gradle runs test JVMs with -ea; build.gradle sets -Ddbc.postcondition=false.
	@Test
	fun eaAndSystemProperties() {
		assertTrue(Dbc.platformDefault)
		assertTrue(Dbc.invariants)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testPlatformDebugDefault() {
		assertTrue(platformDebugDefault())
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testPlatformSettingSystemProperties() {
		assertEquals("false", platformSetting("dbc.postcondition"))
		assertNull(platformSetting("dbc.nonexistent.key"))

		System.setProperty("dbc.precondition", "true")
		assertEquals("true", platformSetting("dbc.precondition"))

		System.setProperty("dbc.invariant", "false")
		assertEquals("false", platformSetting("dbc.invariant"))
	}

	@Test
	fun testDynamicSystemPropertiesReload() {
		System.setProperty("dbc.invariant", "false")
		System.setProperty("dbc.precondition", "true")
		System.setProperty("dbc.postcondition", "true")
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
		assertTrue(Dbc.postconditions)

		System.setProperty("dbc.invariant", "true")
		System.setProperty("dbc.postcondition", "false")
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testGlobalEnabledPropertyFallback() {
		System.clearProperty("dbc.invariant")
		System.clearProperty("dbc.precondition")
		System.clearProperty("dbc.postcondition")
		System.setProperty("dbc.enabled", "false")
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)

		// Per-kind setting overrides global dbc.enabled
		System.setProperty("dbc.invariant", "true")
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}
}
