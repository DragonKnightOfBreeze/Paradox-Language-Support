package icu.windea.pls.extension.diagram

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

object PlsDiagramBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsDiagramBundle"
    private val INSTANCE = DynamicBundle(PlsDiagramBundle::class.java, BUNDLE)

    @JvmStatic
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    @Nls
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> {
        return INSTANCE.getLazyMessage(key, *params)
    }
}
