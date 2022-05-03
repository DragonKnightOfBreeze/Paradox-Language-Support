package icu.windea.pls

import com.intellij.*
import org.jetbrains.annotations.*

private const val bundleName = "messages.PlsDocBundle"

@Suppress("unused")
object PlsDocBundle : DynamicBundle(bundleName) {
	@Nls
	@JvmStatic
	fun message(@NonNls @PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String {
		return getMessage(key, *params)
	}
	
	@Nls
	@JvmStatic
	fun lazyMessage(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): () -> String {
		return { getMessage(key, *params) }
	}
}