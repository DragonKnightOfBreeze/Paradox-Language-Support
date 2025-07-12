package icu.windea.pls.extension.markdown

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

object PlsMarkdownBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsMarkdownBundle"
    private val INSTANCE = DynamicBundle(PlsMarkdownBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> {
        return INSTANCE.getLazyMessage(key, *params)
    }
}
