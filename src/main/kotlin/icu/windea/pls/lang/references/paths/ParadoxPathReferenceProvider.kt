package icu.windea.pls.lang.references.paths

import com.intellij.openapi.paths.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.annotations.api.*
import icu.windea.pls.ep.documentation.*

/**
 * 用于支持在html/markdown等文件中通过特定的超链接引用和跳转到指定的定义/本地化/文件路径等。
 * 
 * @see icu.windea.pls.ep.documentation.ParadoxDocumentationLinkProvider
 */
@HiddenApi
class ParadoxPathReferenceProvider : PathReferenceProviderBase() {
    override fun createReferences(element: PsiElement, offset: Int, text: String?, references: MutableList<in PsiReference>, soft: Boolean): Boolean {
        val link = text ?: return true
        if(!ParadoxDocumentationLinkProvider.supports(link)) return true
        val rangeInElement = TextRange.create(offset, offset + text.length)
        val reference = ParadoxPathReference(element, rangeInElement, link)
        references.add(reference)
        return true
    }
    
    override fun getPathReference(path: String, element: PsiElement): PathReference? {
        return null
    }
}
