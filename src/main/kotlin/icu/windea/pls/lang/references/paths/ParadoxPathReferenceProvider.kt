package icu.windea.pls.lang.references.paths

import com.intellij.openapi.paths.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider

//org.intellij.plugins.markdown.lang.references.headers.HeaderAnchorPathReferenceProvider

/**
 * 用于支持在html/markdown等文件中，将特定的链接解析为匹配的目标引用（定义、本地化等）。
 *
 * @see ParadoxPathReference
 * @see icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider
 */
class ParadoxPathReferenceProvider : PathReferenceProviderBase() {
    override fun createReferences(element: PsiElement, offset: Int, text: String?, references: MutableList<in PsiReference>, soft: Boolean): Boolean {
        val link = text ?: return true
        if (!ReferenceLinkProvider.supports(link)) return true
        val rangeInElement = TextRange.create(offset, offset + text.length)
        val reference = ParadoxPathReference(element, rangeInElement, link)
        references.add(reference)
        return true
    }

    override fun getPathReference(path: String, element: PsiElement): PathReference? {
        return null
    }
}
