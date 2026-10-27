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

/**
 * Base exception for all design-by-contract violations.
 *
 * @param message The detail message that explains the contract violation.
 */
open class DesignByContractException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a precondition check fails.
 *
 * @param message The detail message that explains the precondition failure.
 */
class PreConditionException(message: String) : DesignByContractException(message)

/**
 * Exception thrown when a postcondition check fails.
 *
 * @param message The detail message that explains the postcondition failure.
 */
class PostConditionException(message: String) : DesignByContractException(message)

/**
 * Exception thrown when an invariant check fails.
 *
 * @param message The detail message that explains the invariant failure.
 */
class InvariantException(message: String) : DesignByContractException(message)
