package icu.windea.pls

import com.intellij.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*

@NonNls
private const val BUNDLE = "messages.PlsExtDocBundle"

object PlsExtDocBundle : DynamicBundle(BUNDLE) {
	@Nls
	@JvmStatic
	fun message(name: String, definitionType: String, gameType: ParadoxGameType? = null): String? {
		val key = "${gameType?.id ?: "shared"}.$definitionType.$name"
		return messageOrNull(key)
	}
}