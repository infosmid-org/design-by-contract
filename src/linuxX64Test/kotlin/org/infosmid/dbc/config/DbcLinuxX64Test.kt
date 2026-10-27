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

import platform.posix.setenv
import platform.posix.unsetenv
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DbcLinuxX64Test {
	@AfterTest
	fun tearDown() {
		unsetenv("DBC_ENABLED")
		unsetenv("DBC_PRECONDITION")
		unsetenv("DBC_POSTCONDITION")
		unsetenv("DBC_INVARIANT")
		unsetenv("dbc.enabled")
		unsetenv("dbc.precondition")
		unsetenv("dbc.postcondition")
		unsetenv("dbc.invariant")
		Dbc.reload()
	}

	@Test
	fun testPlatformDebugDefault() {
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testHostSettingIsNull() {
		assertNull(hostSetting("dbc.invariant"))
		assertNull(hostSetting("dbc.enabled"))
		assertNull(hostSetting("dbc.precondition"))
		assertNull(hostSetting("dbc.postcondition"))
	}

	@Test
	fun testEnvironmentVariablesPlatformSetting() {
		setenv("DBC_INVARIANT", "false", 1)
		setenv("dbc.precondition", "true", 1)

		assertEquals("false", platformSetting("dbc.invariant"))
		assertEquals("true", platformSetting("dbc.precondition"))
		assertNull(platformSetting("dbc.nonexistent"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
	}

	@Test
	fun testEnvironmentGlobalEnabledFallback() {
		setenv("DBC_ENABLED", "false", 1)
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)

		// Kind-specific setting overrides global setting
		setenv("DBC_INVARIANT", "true", 1)
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testProgrammaticOverrideAndReload() {
		Dbc.setAll(true)
		assertTrue(Dbc.invariants)
		assertTrue(Dbc.preconditions)
		assertTrue(Dbc.postconditions)

		Dbc.reload()
		assertEquals(platformDebugDefault(), Dbc.invariants)
		assertEquals(platformDebugDefault(), Dbc.preconditions)
		assertEquals(platformDebugDefault(), Dbc.postconditions)
	}
}
