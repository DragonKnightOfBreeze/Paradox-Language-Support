package icu.windea.pls.lang.references.paths

import com.intellij.openapi.paths.PathReference
import com.intellij.openapi.paths.PathReferenceProviderBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.extensions.settings.PlsExtensionsSettings
import icu.windea.pls.lang.codeInsight.ReferenceLinkService

// org.intellij.plugins.markdown.lang.references.headers.HeaderAnchorPathReferenceProvider

/**
 * 用于支持在 html/markdown 等文件中，将特定的链接解析为匹配的目标引用（定义、本地化等）。
 *
 * @see ParadoxPathReference
 * @see icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider
 */
class ParadoxPathReferenceProvider : PathReferenceProviderBase() {
    override fun createReferences(element: PsiElement, offset: Int, text: String?, references: MutableList<in PsiReference>, soft: Boolean): Boolean {
        // 如果 Markdown 相关扩展功能未启用，仅忽略 Markdown 或 HTML 文件
        val languageId = element.language.id
        if (languageId.equals("markdown", true) || languageId.equals("html", true)) {
            if (!PlsExtensionsSettings.getInstance().state.markdown.resolveLinks) return false
        }

        val link = text ?: return true
        if (!ReferenceLinkService.supports(link)) return true
        val rangeInElement = TextRange.create(offset, offset + text.length)
        val reference = ParadoxPathReference(element, rangeInElement, link)
        references.add(reference)
        return true
    }

    override fun getPathReference(path: String, element: PsiElement): PathReference? {
        return null
    }
}
