package icu.windea.pls

import com.intellij.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*

private const val bundleName = "messages.PlsExtDocBundle"

object PlsExtDocBundle : DynamicBundle(bundleName) {
	@Nls
	@JvmStatic
	fun message(name: String, definitionType: String, gameType: ParadoxGameType? = null): String? {
		val key = "${gameType?.id ?: "shared"}.$definitionType.$name"
		return messageOrNull(key)
	}
}