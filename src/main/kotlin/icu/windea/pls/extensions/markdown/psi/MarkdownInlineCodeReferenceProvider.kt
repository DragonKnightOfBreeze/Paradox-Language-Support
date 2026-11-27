package icu.windea.pls.extensions.markdown.psi

import com.intellij.model.psi.ImplicitReferenceProvider
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.psi.PsiElement
import icu.windea.pls.base.settings.ChronicleExtensionSettings

/**
 * 用于在 Markdown 文件中，尝试将内联代码解析为匹配的目标引用（定义、本地化等）。
 */
@Suppress("UnstableApiUsage")
class MarkdownInlineCodeReferenceProvider : ImplicitReferenceProvider {
    override fun getImplicitReference(element: PsiElement, offsetInElement: Int): PsiSymbolReference? {
        if (!ChronicleExtensionSettings.getInstance().state.markdown.resolveInlineCodes) return null

        val info = getMarkdownInlineCodeInfo(element) ?: return null
        return MarkdownInlineCodeSymbolReference(element, info.rangeInElement, info.prefix, info.name)
    }
}
