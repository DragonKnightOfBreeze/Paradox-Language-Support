package icu.windea.pls.lang.codeInsight.hints

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

object ParadoxHintsPreviewBundle {
    @NonNls
    private const val BUNDLE = "inlayProviders.preview"
    private val INSTANCE = DynamicBundle(ParadoxHintsPreviewBundle::class.java, BUNDLE)

    @JvmStatic
    @Nls
    fun get(providerId: String, offset: Int): String? {
        return INSTANCE.messageOrNull("$providerId.$offset")
    }
}
