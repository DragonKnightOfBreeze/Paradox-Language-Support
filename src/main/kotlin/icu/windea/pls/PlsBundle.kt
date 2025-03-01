package icu.windea.pls

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

@NonNls
private const val BUNDLE = "messages.PlsBundle"

object PlsBundle {
    private val INSTANCE = DynamicBundle(PlsBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> {
        return INSTANCE.getLazyMessage(key, *params)
    }
}
