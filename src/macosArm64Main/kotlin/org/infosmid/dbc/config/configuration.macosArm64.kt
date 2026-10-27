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

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

/**
 * Retrieve the host configuration setting for [key] on macOS ARM64.
 *
 * Checks `NSUserDefaults` first (including Xcode scheme launch arguments such as `-dbc.invariant NO`),
 * then falls back to bundle keys in `Info.plist`.
 *
 * @param key The configuration key to read.
 * @return The configuration value, or `null` if not found.
 */
internal actual fun hostSetting(key: String): String? {
    return NSUserDefaults.standardUserDefaults.objectForKey(key)?.toString()
        ?: NSBundle.mainBundle.objectForInfoDictionaryKey(key)?.toString()
}
