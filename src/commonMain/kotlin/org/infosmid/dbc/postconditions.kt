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
@file:OptIn(ExperimentalContracts::class)
@file:Suppress("NOTHING_TO_INLINE")

package org.infosmid.dbc

import org.infosmid.dbc.config.Dbc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Validate that [check] is true.
 *
 * Declares a contract that implies [check] is true when this function returns.
 *
 * @param check The boolean condition to validate.
 * @param messageExpr A function that returns the exception message.
 * @throws PostConditionException If [check] is `false`.
 */
@Throws(PostConditionException::class)
inline fun postcondition(
	check: Boolean,
	messageExpr: () -> String,
) {
	contract { returns() implies check }
	if (!check) throw PostConditionException(messageExpr())
}

/**
 * Validate that [checkExpr] returns true when postcondition checks are enabled.
 *
 * @param checkExpr A function that returns the boolean condition to validate.
 * @param messageExpr A function that returns the exception message.
 * @throws PostConditionException If postcondition checks are enabled and [checkExpr] returns `false`.
 */
@Throws(PostConditionException::class)
inline fun postcondition(
	checkExpr: () -> Boolean,
	messageExpr: () -> String,
) {
	if (Dbc.postconditions && !checkExpr()) throw PostConditionException(messageExpr())
}

/**
 * Validate that [check] is true.
 *
 * Declares a contract that implies [check] is true when this function returns.
 *
 * @param message The detail message for the exception if the check fails.
 * @param check The boolean condition to validate.
 * @throws PostConditionException If [check] is `false`.
 */
@Throws(PostConditionException::class)
inline fun postcondition(
	message: String,
	check: Boolean,
) {
	contract { returns() implies check }
	if (!check) throw PostConditionException(message)
}

/**
 * Validate that [checkExpr] returns true when postcondition checks are enabled.
 *
 * @param message The detail message for the exception if the check fails.
 * @param checkExpr A function that returns the boolean condition to validate.
 * @throws PostConditionException If postcondition checks are enabled and [checkExpr] returns `false`.
 */
@Throws(PostConditionException::class)
inline fun postcondition(
	message: String,
	checkExpr: () -> Boolean,
) {
	if (Dbc.postconditions && !checkExpr()) throw PostConditionException(message)
}

/**
 * Validate that [obj] is not null.
 *
 * Declares a contract that implies [obj] is not null when this function returns.
 *
 * @param T The type of the object.
 * @param obj The nullable object to validate.
 * @param messageExpr A function that returns the exception message.
 * @return The non-null object.
 * @throws PostConditionException If [obj] is null.
 */
@Throws(PostConditionException::class)
inline fun <T> postconditionNotNull(
	obj: T?,
	messageExpr: () -> String,
): T {
	contract { returns() implies (obj != null) }
	if (obj == null) throw PostConditionException(messageExpr())
	return obj
}

/**
 * Validate that [obj] is not null.
 *
 * Declares a contract that implies [obj] is not null when this function returns.
 *
 * @param T The type of the object.
 * @param obj The nullable object to validate.
 * @param exception The detail message for the exception if [obj] is null.
 * @return The non-null object.
 * @throws PostConditionException If [obj] is null.
 */
@Throws(PostConditionException::class)
inline fun <T> postconditionNotNull(
	obj: T?,
	exception: String,
): T {
	contract { returns() implies (obj != null) }
	if (obj == null) throw PostConditionException(exception)
	return obj
}

/**
 * Throw a [PostConditionException] unconditionally.
 *
 * @param messageExpr A function that returns the exception message.
 * @throws PostConditionException Always throws this exception.
 */
@Throws(PostConditionException::class)
inline fun postconditionFailed(messageExpr: () -> String): Nothing = throw PostConditionException(messageExpr())

/**
 * Throw a [PostConditionException] unconditionally.
 *
 * @param message The detail message for the exception.
 * @throws PostConditionException Always throws this exception.
 */
@Throws(PostConditionException::class)
inline fun postconditionFailed(message: String): Nothing = throw PostConditionException(message)
