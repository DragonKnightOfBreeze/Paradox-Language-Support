package com.windea.plugin.idea.paradox

import com.intellij.*
import org.jetbrains.annotations.*

object ParadoxBundle : DynamicBundle(paradoxBundleName) {
	fun message(@PropertyKey(resourceBundle = paradoxBundleName) key: String, vararg params: Any): String {
		return ParadoxBundle.getMessage(key, *params)
	}
}
