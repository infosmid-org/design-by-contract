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

class PostConditionTest {
	@AfterTest
	fun tearDown() {
		Dbc.reload()
	}

	@Test
	fun testPostconditionBooleanWithMessageExprPositive() {
		var messageEvaluated = false
		postcondition(true) {
			messageEvaluated = true
			"Should not be evaluated"
		}
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPostconditionBooleanWithMessageExprNegative() {
		var messageEvaluated = false
		val ex = assertFailsWith<PostConditionException> {
			postcondition(false) {
				messageEvaluated = true
				"Postcondition boolean failed"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("Postcondition boolean failed", ex.message)
	}

	@Test
	fun testPostconditionBooleanWithMessageExprEnforcedRegardlessOfConfiguration() {
		Dbc.set(DbcKind.POSTCONDITION, false)
		assertFalse(Dbc.postconditions)

		val ex = assertFailsWith<PostConditionException> {
			postcondition(false) { "Still enforced when disabled" }
		}
		assertEquals("Still enforced when disabled", ex.message)
	}

	@Test
	fun testPostconditionLambdaWithMessageExprEnabledPositive() {
		Dbc.set(DbcKind.POSTCONDITION, true)
		var checkEvaluated = false
		var messageEvaluated = false

		postcondition({
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
	fun testPostconditionLambdaWithMessageExprEnabledNegative() {
		Dbc.set(DbcKind.POSTCONDITION, true)
		var checkEvaluated = false
		var messageEvaluated = false

		val ex = assertFailsWith<PostConditionException> {
			postcondition({
				checkEvaluated = true
				false
			}) {
				messageEvaluated = true
				"Postcondition lambda failed"
			}
		}

		assertTrue(checkEvaluated)
		assertTrue(messageEvaluated)
		assertEquals("Postcondition lambda failed", ex.message)
	}

	@Test
	fun testPostconditionLambdaWithMessageExprDisabled() {
		Dbc.set(DbcKind.POSTCONDITION, false)
		var checkEvaluated = false
		var messageEvaluated = false

		postcondition({
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
	fun testPostconditionMessageWithBooleanPositive() {
		postcondition("No failure expected", true)
	}

	@Test
	fun testPostconditionMessageWithBooleanNegative() {
		val ex = assertFailsWith<PostConditionException> {
			postcondition("Postcondition msg-boolean failed", false)
		}
		assertEquals("Postcondition msg-boolean failed", ex.message)
	}

	@Test
	fun testPostconditionMessageWithBooleanEnforcedRegardlessOfConfiguration() {
		Dbc.set(DbcKind.POSTCONDITION, false)
		val ex = assertFailsWith<PostConditionException> {
			postcondition("Still enforced when disabled", false)
		}
		assertEquals("Still enforced when disabled", ex.message)
	}

	@Test
	fun testPostconditionMessageWithLambdaEnabledPositive() {
		Dbc.set(DbcKind.POSTCONDITION, true)
		var checkEvaluated = false

		postcondition("No failure expected") {
			checkEvaluated = true
			true
		}

		assertTrue(checkEvaluated)
	}

	@Test
	fun testPostconditionMessageWithLambdaEnabledNegative() {
		Dbc.set(DbcKind.POSTCONDITION, true)
		var checkEvaluated = false

		val ex = assertFailsWith<PostConditionException> {
			postcondition("Postcondition msg-lambda failed") {
				checkEvaluated = true
				false
			}
		}

		assertTrue(checkEvaluated)
		assertEquals("Postcondition msg-lambda failed", ex.message)
	}

	@Test
	fun testPostconditionMessageWithLambdaDisabled() {
		Dbc.set(DbcKind.POSTCONDITION, false)
		var checkEvaluated = false

		postcondition("Should not fail when disabled") {
			checkEvaluated = true
			false
		}

		assertFalse(checkEvaluated)
	}

	@Test
	fun testPostconditionNotNullWithMessageExprPositive() {
		var messageEvaluated = false
		val value: String = "valid"
		val result: String = postconditionNotNull(value) {
			messageEvaluated = true
			"Should not evaluate"
		}
		assertEquals("valid", result)
		assertFalse(messageEvaluated)
	}

	@Test
	fun testPostconditionNotNullWithMessageExprNegative() {
		var messageEvaluated = false
		val value: String? = null
		val ex = assertFailsWith<PostConditionException> {
			postconditionNotNull(value) {
				messageEvaluated = true
				"String value must not be null"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("String value must not be null", ex.message)
	}

	@Test
	fun testPostconditionNotNullWithStringPositive() {
		val value: Int? = 100
		val result: Int = postconditionNotNull(value, "Must not be null")
		assertEquals(100, result)
	}

	@Test
	fun testPostconditionNotNullWithStringNegative() {
		val value: Int? = null
		val ex = assertFailsWith<PostConditionException> {
			postconditionNotNull(value, "Value is required")
		}
		assertEquals("Value is required", ex.message)
	}

	@Test
	fun testPostconditionFailedWithMessageExpr() {
		var messageEvaluated = false
		val ex = assertFailsWith<PostConditionException> {
			postconditionFailed {
				messageEvaluated = true
				"Postcondition explicitly failed with expr"
			}
		}
		assertTrue(messageEvaluated)
		assertEquals("Postcondition explicitly failed with expr", ex.message)
	}

	@Test
	fun testPostconditionFailedWithString() {
		val ex = assertFailsWith<PostConditionException> {
			postconditionFailed("Postcondition explicitly failed with string")
		}
		assertEquals("Postcondition explicitly failed with string", ex.message)
	}
}
