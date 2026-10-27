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

/**
 * Interface for classes that support invariant checks.
 *
 * Implement this interface on any class that must validate its internal state.
 */
interface HasInvariant {
	/**
	 * Validate the internal state of the object.
	 *
	 * This function must only evaluate the condition and return a boolean value.
	 * It must not throw exceptions or modify object state.
	 *
	 * @return `true` if the state is valid; `false` otherwise.
	 */
	fun invariant(): Boolean
}

/**
 * Execute [block] and validate the invariant condition before and after execution.
 *
 * If invariant checks are disabled, this function executes [block] without validation.
 *
 * @param T The type that implements [HasInvariant].
 * @param R The return type of [block].
 * @param message The detail message for the exception if an invariant check fails.
 * @param block The operation to execute on this object.
 * @return The result of executing [block].
 * @throws InvariantException If the invariant is `false` before or after [block] runs.
 */
inline fun <reified T : HasInvariant, R> T.checkInvariant(message: String, block: T.() -> R): R {
	if (!Dbc.invariants) return block()
	if (!invariant()) {
		throw InvariantException(message)
	}
	val r = block()
	if (!invariant()) {
		throw InvariantException(message)
	}
	return r
}

/**
 * Validate the invariant condition immediately if invariant checks are enabled.
 *
 * @param T The type that implements [HasInvariant].
 * @param message The detail message for the exception if the invariant check fails.
 * @return This object instance.
 * @throws InvariantException If invariant checks are enabled and [HasInvariant.invariant] returns `false`.
 */
inline fun <reified T : HasInvariant> T.checkInvariant(message: String): T {
	if (Dbc.invariants) {
		if (!invariant()) {
			throw InvariantException(message)
		}
	}
	return this
}

/**
 * Validate the invariant condition immediately if invariant checks are enabled.
 *
 * @param T The type that implements [HasInvariant].
 * @param messageExpr A function that returns the message for the exception if the invariant check fails.
 * @return This object instance.
 * @throws InvariantException If invariant checks are enabled and [HasInvariant.invariant] returns `false`.
 */
inline fun <reified T : HasInvariant> T.checkInvariant(messageExpr: () -> String): T {
	if (Dbc.invariants) {
		if (!invariant()) {
			throw InvariantException(messageExpr())
		}
	}
	return this
}
