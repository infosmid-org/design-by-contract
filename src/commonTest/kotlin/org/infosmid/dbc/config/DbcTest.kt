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

import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.*

class DbcTest {
	@Test fun parsesCommonSpellings() {
		listOf("true", "1", "YES", " on ").forEach { assertEquals(true, parseFlag(it), it) }
		listOf("false", "0", "NO", "off").forEach { assertEquals(false, parseFlag(it), it) }
		assertNull(parseFlag(null)); assertNull(parseFlag("maybe"))
	}
	
	@Test fun perKindBeatsGlobalBeatsDefault() {
		val cfg = mapOf("dbc.enabled" to "true", "dbc.invariant" to "false")
		assertFalse(resolveFlag(DbcKind.INVARIANT, cfg::get, debugDefault = true))
		assertTrue(resolveFlag(DbcKind.PRECONDITION, cfg::get, debugDefault = false))
		assertTrue(resolveFlag(DbcKind.POSTCONDITION, { null }, debugDefault = true))
	}
	
	@Test fun envNameMapping() = assertEquals("DBC_INVARIANT", envName("dbc.invariant"))
	
	@Test fun readsPlatformEnvironment() {
		// Set by the test tasks (or not supported by the runner) -> report what we saw.
		println("platform: DBC_PROBE=${platformSetting("dbc.probe")} default=${Dbc.platformDefault} " +
				"pre=${Dbc.preconditions} post=${Dbc.postconditions} inv=${Dbc.invariants}")
	}
	
	@Test fun programmaticOverride() {
		Dbc.setAll(false); assertFalse(Dbc.isEnabled(DbcKind.INVARIANT))
		Dbc.set(DbcKind.INVARIANT, true); assertTrue(Dbc.invariants)
		Dbc.reload()
	}
}
