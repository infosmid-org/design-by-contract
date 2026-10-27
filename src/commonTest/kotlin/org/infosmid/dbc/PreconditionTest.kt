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
package org.infosmid.dbc

import org.infosmid.dbc.config.Dbc
import org.infosmid.dbc.config.DbcKind
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreconditionTest {
	@AfterTest
	fun tearDown() {
		Dbc.reload()
	}

	@Test
	fun testPreconditionBooleanWithMessageExprPositive() {
		var messageEvaluated = false
		precondition(true) {
			messageEvaluated = true
			"Should not be evaluated"
		}
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPreconditionBooleanWithMessageExprNegative() {
		var messageEvaluated = false
		val ex = assertFailsWith<PreConditionException> {
			precondition(false) {
				messageEvaluated = true
				"Precondition boolean failed"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("Precondition boolean failed", ex.message)
	}

	@Test
	fun testPreconditionBooleanWithMessageExprEnforcedRegardlessOfConfiguration() {
		Dbc.set(DbcKind.PRECONDITION, false)
		assertFalse(Dbc.preconditions)

		val ex = assertFailsWith<PreConditionException> {
			precondition(false) { "Still enforced when disabled" }
		}
		assertEquals("Still enforced when disabled", ex.message)
	}

	@Test
	fun testPreconditionLambdaWithMessageExprEnabledPositive() {
		Dbc.set(DbcKind.PRECONDITION, true)
		var checkEvaluated = false
		var messageEvaluated = false

		precondition({
			checkEvaluated = true
			true
		}) {
			messageEvaluated = true
			"Should not be evaluated"
		}

		assertTrue(checkEvaluated)
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPreconditionLambdaWithMessageExprEnabledNegative() {
		Dbc.set(DbcKind.PRECONDITION, true)
		var checkEvaluated = false
		var messageEvaluated = false

		val ex = assertFailsWith<PreConditionException> {
			precondition({
				checkEvaluated = true
				false
			}) {
				messageEvaluated = true
				"Precondition lambda failed"
			}
		}

		assertTrue(checkEvaluated)
		assertTrue(messageEvaluated)
		assertEquals("Precondition lambda failed", ex.message)
	}

	@Test
	fun testPreconditionLambdaWithMessageExprDisabled() {
		Dbc.set(DbcKind.PRECONDITION, false)
		var checkEvaluated = false
		var messageEvaluated = false

		precondition({
			checkEvaluated = true
			false
		}) {
			messageEvaluated = true
			"Should not be evaluated"
		}

		assertFalse(checkEvaluated)
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPreconditionMessageWithLambdaEnabledPositive() {
		Dbc.set(DbcKind.PRECONDITION, true)
		var checkEvaluated = false

		precondition("No failure expected") {
			checkEvaluated = true
			true
		}

		assertTrue(checkEvaluated)
	}

	@Test
	fun testPreconditionMessageWithLambdaEnabledNegative() {
		Dbc.set(DbcKind.PRECONDITION, true)
		var checkEvaluated = false

		val ex = assertFailsWith<PreConditionException> {
			precondition("Precondition msg-lambda failed") {
				checkEvaluated = true
				false
			}
		}

		assertTrue(checkEvaluated)
		assertEquals("Precondition msg-lambda failed", ex.message)
	}

	@Test
	fun testPreconditionMessageWithLambdaDisabled() {
		Dbc.set(DbcKind.PRECONDITION, false)
		var checkEvaluated = false

		precondition("Should not fail when disabled") {
			checkEvaluated = true
			false
		}

		assertFalse(checkEvaluated)
	}

	@Test
	fun testPreconditionBooleanWithExceptionStringPositive() {
		precondition(true, "No failure expected")
	}

	@Test
	fun testPreconditionBooleanWithExceptionStringNegative() {
		val ex = assertFailsWith<PreConditionException> {
			precondition(false, "Precondition string check failed")
		}
		assertEquals("Precondition string check failed", ex.message)
	}

	@Test
	fun testPreconditionBooleanWithExceptionStringEnforcedRegardlessOfConfiguration() {
		Dbc.set(DbcKind.PRECONDITION, false)
		val ex = assertFailsWith<PreConditionException> {
			precondition(false, "Always checked")
		}
		assertEquals("Always checked", ex.message)
	}

	@Test
	fun testPreconditionNotNullWithStringPositive() {
		val value: String? = "valid"
		val result: String = preconditionNotNull(value, "Must not be null")
		assertEquals("valid", result)
	}

	@Test
	fun testPreconditionNotNullWithStringNegative() {
		val value: String? = null
		val ex = assertFailsWith<PreConditionException> {
			preconditionNotNull(value, "Value is required")
		}
		assertEquals("Value is required", ex.message)
	}

	@Test
	fun testPreconditionNotNullWithMessageExprPositive() {
		var messageEvaluated = false
		val value: Int? = 42
		val result: Int = preconditionNotNull(value) {
			messageEvaluated = true
			"Should not evaluate"
		}
		assertEquals(42, result)
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPreconditionNotNullWithMessageExprNegative() {
		var messageEvaluated = false
		val value: Int? = null
		val ex = assertFailsWith<PreConditionException> {
			preconditionNotNull(value) {
				messageEvaluated = true
				"Int value must not be null"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("Int value must not be null", ex.message)
	}

	@Test
	fun testPreconditionFailedWithMessageExpr() {
		var messageEvaluated = false
		val ex = assertFailsWith<PreConditionException> {
			preconditionFailed {
				messageEvaluated = true
				"Precondition explicitly failed with expr"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("Precondition explicitly failed with expr", ex.message)
	}

	@Test
	fun testPreconditionFailedWithString() {
		val ex = assertFailsWith<PreConditionException> {
			preconditionFailed("Precondition explicitly failed with string")
		}
		assertEquals("Precondition explicitly failed with string", ex.message)
	}
}
