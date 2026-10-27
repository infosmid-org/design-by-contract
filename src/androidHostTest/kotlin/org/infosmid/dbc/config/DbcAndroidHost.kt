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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
class DbcAndroidHostTest {
	@AfterTest
	fun tearDown() {
		DbcAndroid.metaData = null
		DbcAndroid.debuggable = false
		System.clearProperty("dbc.enabled")
		System.clearProperty("dbc.precondition")
		System.clearProperty("dbc.postcondition")
		System.clearProperty("dbc.invariant")
		Dbc.reload()
	}

	@Test
	fun testDebuggableDefault() {
		DbcAndroid.debuggable = false
		Dbc.reload()
		assertFalse(DbcAndroid.debuggable)
		assertFalse(platformDebugDefault())
		assertFalse(Dbc.platformDefault)
		assertFalse(Dbc.invariants)

		DbcAndroid.debuggable = true
		Dbc.reload()
		assertTrue(DbcAndroid.debuggable)
		assertTrue(platformDebugDefault())
		assertTrue(Dbc.platformDefault)
		assertTrue(Dbc.invariants)
	}

	@Test
	fun testMetaDataSettings() {
		val bundle = mockk<Bundle>()
		every { bundle.get("dbc.invariant") } returns "false"
		every { bundle.get("dbc.precondition") } returns "true"
		every { bundle.get("dbc.enabled") } returns "false"
		every { bundle.get(match { it !in listOf("dbc.invariant", "dbc.precondition", "dbc.enabled") }) } returns null

		DbcAndroid.metaData = bundle
		DbcAndroid.debuggable = true

		assertEquals("false", platformSetting("dbc.invariant"))
		assertEquals("true", platformSetting("dbc.precondition"))
		assertEquals("false", platformSetting("dbc.enabled"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
		assertTrue(Dbc.preconditions)
		// postconditions is not in bundle, so it falls back to dbc.enabled ("false")
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testMetaDataOverridesSystemProperties() {
		val bundle = mockk<Bundle>()
		every { bundle.get("dbc.invariant") } returns "false"
		every { bundle.get(neq("dbc.invariant")) } returns null

		DbcAndroid.metaData = bundle

		System.setProperty("dbc.invariant", "true")
		assertEquals("false", platformSetting("dbc.invariant"))

		Dbc.reload()
		assertFalse(Dbc.invariants)
	}

	@Test
	fun testSystemPropertyFallback() {
		DbcAndroid.metaData = null
		DbcAndroid.debuggable = false

		System.setProperty("dbc.precondition", "true")
		assertEquals("true", platformSetting("dbc.precondition"))
		assertNull(platformSetting("dbc.nonexistent"))

		Dbc.reload()
		assertTrue(Dbc.preconditions)
		assertFalse(Dbc.postconditions)
	}

	@Test
	fun testConfigureWithContext() {
		val context = mockk<Context>()
		val appInfo = ApplicationInfo().apply {
			flags = ApplicationInfo.FLAG_DEBUGGABLE
		}
		val bundle = mockk<Bundle>()
		every { bundle.get("dbc.invariant") } returns "false"
		every { bundle.get(neq("dbc.invariant")) } returns null
		appInfo.metaData = bundle

		val pm = mockk<PackageManager>()
		every { context.applicationContext } returns context
		every { context.packageName } returns "org.infosmid.dbc.test"
		every { context.packageManager } returns pm
		every { context.applicationInfo } returns appInfo
		every { pm.getApplicationInfo(any<String>(), any<Int>()) } returns appInfo

		DbcAndroid.configure(context)

		assertTrue(DbcAndroid.debuggable)
		assertEquals(bundle, DbcAndroid.metaData)
		assertFalse(Dbc.invariants)
	}
}

