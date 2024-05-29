package icu.windea.pls.ep.documentation

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 提供对快速文档链接的支持，用于点击跳转到对应的定义/本地化等。
 */
interface ParadoxDocumentationLinkProvider {
    val linkPrefix: String
    
    fun resolve(link: String, contextElement: PsiElement): PsiElement?
    
    fun getUnresolvedMessage(link: String): String? = null
    
    fun create(element: PsiElement, plainLink: Boolean = true): String? = null
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDocumentationLinkProvider>("icu.windea.pls.documentationLinkProvider")
        
        fun supports(link: String): Boolean {
            return EP_NAME.extensionList.any { ep ->
                link.startsWith(ep.linkPrefix)
            }
        }
        
        fun resolve(link: String, contextElement: PsiElement): PsiElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!link.startsWith(ep.linkPrefix)) return@f null
                ep.resolve(link, contextElement)
            }
        }
        
        fun getUnresolvedMessage(link: String): String {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!link.startsWith(ep.linkPrefix)) return@f null
                ep.getUnresolvedMessage(link)
            } ?: PlsBundle.message("path.reference.unresolved", link)
        }
        
        fun create(element: PsiElement, plainLink: Boolean = true): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.create(element, plainLink)
            }
        }
    }
}