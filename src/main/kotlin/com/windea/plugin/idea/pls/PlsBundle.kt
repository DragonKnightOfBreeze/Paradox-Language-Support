package com.windea.plugin.idea.pls

import com.intellij.*
import org.jetbrains.annotations.*

object PlsBundle : DynamicBundle(bundleName) {
	fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String {
		return PlsBundle.getMessage(key, *params)
	}
}
