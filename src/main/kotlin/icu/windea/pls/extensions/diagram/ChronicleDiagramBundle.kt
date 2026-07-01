package icu.windea.pls.extensions.diagram

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object ChronicleDiagramBundle {
    @NonNls
    private const val BUNDLE = "messages.ChronicleDiagramBundle"
    private val INSTANCE = DynamicBundle(ChronicleDiagramBundle::class.java, BUNDLE)

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
