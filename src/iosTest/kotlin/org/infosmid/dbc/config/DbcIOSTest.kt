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

import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DbcIOSTest {
	@AfterTest
	fun tearDown() {
		val defaults = NSUserDefaults.standardUserDefaults
		defaults.removeObjectForKey("dbc.enabled")
		defaults.removeObjectForKey("dbc.precondition")
		defaults.removeObjectForKey("dbc.postcondition")
		defaults.removeObjectForKey("dbc.invariant")
		Dbc.reload()
	}

	@Test
	fun testPlatformDebugDefault() {
		assertEquals(platformDebugDefault(), Dbc.platformDefault)
	}

	@Test
	fun testUserDefaultsPlatformSetting() {
		val defaults = NSUserDefaults.standardUserDefaults
		defaults.setObject("false", forKey = "dbc.invariant")
		defaults.setObject("true", forKey = "dbc.precondition")

		assertEquals("false", platformSetting("dbc.invariant"))
		assertEquals("true", platformSetting("dbc.precondition"))
		assertNull(platformSetting("dbc.nonexistent"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
	}

	@Test
	fun testUserDefaultsGlobalEnabledFallback() {
		val defaults = NSUserDefaults.standardUserDefaults
		defaults.setObject("false", forKey = "dbc.enabled")
		Dbc.reload()

		assertFalse(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)

		// Kind-specific setting overrides global setting
		defaults.setObject("true", forKey = "dbc.invariant")
		Dbc.reload()

		assertTrue(Dbc.invariants)
		assertFalse(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}
}