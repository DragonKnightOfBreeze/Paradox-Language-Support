package icu.windea.pls.gameTypes.stellaris

import com.intellij.*
import org.jetbrains.annotations.*

@NonNls
private const val BUNDLE = "messages.gameTypes.StellarisBundle"

object StellarisBundle : DynamicBundle(BUNDLE) {
	@Nls
	@JvmStatic
	fun message(@NonNls @PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
		return getMessage(key, *params)
	}
	
	@Nls
	@JvmStatic
	fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): () -> String {
		return { getMessage(key, *params) }
	}
}