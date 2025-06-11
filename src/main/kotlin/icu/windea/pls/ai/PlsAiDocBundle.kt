package icu.windea.pls.ai

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

object PlsAiDocBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsAiDocBundle"
    private val INSTANCE = DynamicBundle(PlsAiDocBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> {
        return INSTANCE.getLazyMessage(key, *params)
    }
}
