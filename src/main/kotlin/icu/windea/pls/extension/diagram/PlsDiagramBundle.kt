package icu.windea.pls.extension.diagram

import com.intellij.*
import org.jetbrains.annotations.*

@NonNls
private const val BUNDLE = "messages.PlsDiagramBundle"

object PlsDiagramBundle : DynamicBundle(BUNDLE) {
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
