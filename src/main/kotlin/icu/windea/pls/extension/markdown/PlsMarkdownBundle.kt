package icu.windea.pls.extension.markdown

import com.intellij.*
import org.jetbrains.annotations.*
import java.util.function.*

@Suppress("unused")
object PlsMarkdownBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsMarkdownBundle"
    private val INSTANCE = DynamicBundle(PlsMarkdownBundle::class.java, BUNDLE)

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
