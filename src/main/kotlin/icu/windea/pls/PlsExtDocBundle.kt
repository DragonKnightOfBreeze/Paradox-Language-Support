package icu.windea.pls

import com.intellij.*
import icu.windea.pls.lang.model.*
import org.jetbrains.annotations.*

@NonNls
private const val BUNDLE = "messages.PlsExtDocBundle"

object PlsExtDocBundle : DynamicBundle(BUNDLE) {
	@Nls
	@JvmStatic
	fun message(name: String, type: String, gameType: ParadoxGameType? = null): String? {
		//${gameType}.${definitionType}.${definitionName}
		val key = "${gameType?.id ?: "core"}.$type.$name"
		return messageOrNull(key)
	}
}
