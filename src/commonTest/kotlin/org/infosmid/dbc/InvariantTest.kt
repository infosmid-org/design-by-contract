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
import kotlin.test.assertSame

class InvariantTest {
	private class Account(var balance: Int) : HasInvariant {
		var invariantCheckCount: Int = 0

		override fun invariant(): Boolean {
			invariantCheckCount++
			return balance >= 0
		}

		fun deposit(amount: Int): Int = checkInvariant("Balance must remain non-negative") {
			balance += amount
			balance
		}

		fun withdraw(amount: Int): Int = checkInvariant("Balance must remain non-negative") {
			balance -= amount
			balance
		}
	}

	@AfterTest
	fun tearDown() {
		Dbc.reload()
	}

	@Test
	fun testCheckInvariantBlockEnabledPositive() {
		Dbc.set(DbcKind.INVARIANT, true)
		val account = Account(100)

		val result = account.deposit(50)

		assertEquals(150, result)
		assertEquals(150, account.balance)
		assertEquals(2, account.invariantCheckCount) // Checked before and after block
	}

	@Test
	fun testCheckInvariantBlockEnabledNegativeBeforeBlock() {
		Dbc.set(DbcKind.INVARIANT, true)
		val account = Account(-10)
		var blockExecuted = false

		val ex = assertFailsWith<InvariantException> {
			account.checkInvariant("Invalid initial state") {
				blockExecuted = true
				account.balance += 20
			}
		}

		assertEquals("Invalid initial state", ex.message)
		assertFalse(blockExecuted)
		assertEquals(1, account.invariantCheckCount) // Checked before block and failed immediately
	}

	@Test
	fun testCheckInvariantBlockEnabledNegativeAfterBlock() {
		Dbc.set(DbcKind.INVARIANT, true)
		val account = Account(50)

		val ex = assertFailsWith<InvariantException> {
			account.withdraw(100)
		}

		assertEquals("Balance must remain non-negative", ex.message)
		assertEquals(-50, account.balance) // Mutation executed
		assertEquals(2, account.invariantCheckCount) // Checked before (passed) and after (failed)
	}

	@Test
	fun testCheckInvariantBlockDisabled() {
		Dbc.set(DbcKind.INVARIANT, false)
		assertFalse(Dbc.invariants)

		val account = Account(-10)
		val result = account.withdraw(50)

		assertEquals(-60, result)
		assertEquals(-60, account.balance)
		assertEquals(0, account.invariantCheckCount) // invariant() not evaluated when disabled
	}

	@Test
	fun testCheckInvariantImmediateEnabledPositive() {
		Dbc.set(DbcKind.INVARIANT, true)
		val account = Account(100)

		val returned = account.checkInvariant("Valid account")

		assertSame(account, returned)
		assertEquals(1, account.invariantCheckCount)
	}

	@Test
	fun testCheckInvariantImmediateEnabledNegative() {
		Dbc.set(DbcKind.INVARIANT, true)
		val account = Account(-5)

		val ex = assertFailsWith<InvariantException> {
			account.checkInvariant { "Account balance is negative. ${account.balance}" }
		}

		assertEquals("Account balance is negative. ${account.balance}", ex.message)
		assertEquals(1, account.invariantCheckCount)
	}

	@Test
	fun testCheckInvariantImmediateDisabled() {
		Dbc.set(DbcKind.INVARIANT, false)
		assertFalse(Dbc.invariants)

		val account = Account(-5)
		val returned = account.checkInvariant("Should be ignored when disabled")

		assertSame(account, returned)
		assertEquals(0, account.invariantCheckCount) // invariant() not evaluated when disabled
	}
}
