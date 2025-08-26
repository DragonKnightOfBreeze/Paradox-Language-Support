package icu.windea.pls

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

object PlsBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsBundle"
    private val INSTANCE = DynamicBundle(PlsBundle::class.java, BUNDLE)

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
