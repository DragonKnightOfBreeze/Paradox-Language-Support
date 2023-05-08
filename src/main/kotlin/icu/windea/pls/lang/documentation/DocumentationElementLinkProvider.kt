package icu.windea.pls.lang.documentation

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 提供对快速文档链接的支持，用于点击跳转到对应的定义/本地化等。
 * 
 * 快速文档链接也能作为html/markdown等文件中的超链接使用，从而引用和跳转到指定的定义/本地化等。
 * 
 * TODO DDS文件路径以及符合条件的快速文档链接也能作为html/markdown等文件中的图片超链接使用，从而渲染DDS图片和本地化。
 * 
 * @see icu.windea.pls.core.references.paths.ParadoxPathReferenceProvider
 */
interface DocumentationElementLinkProvider {
    val linkPrefix: String
    
    fun resolve(link: String, contextElement: PsiElement): PsiElement?
    
    fun getUnresolvedMessage(link: String): String? = null
    
    fun create(element: PsiElement, plainLink: Boolean = true): String? = null
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<DocumentationElementLinkProvider>("icu.windea.pls.documentationElementLinkProvider")
        
        fun supports(link: String): Boolean {
            return EP_NAME.extensionList.any { link.startsWith(it.linkPrefix) }
        }
        
        fun resolve(link: String, contextElement: PsiElement): PsiElement? {
            EP_NAME.extensionList.forEach {
                if(link.startsWith(it.linkPrefix)) {
                    val result = it.resolve(link, contextElement)
                    if(result != null) return result
                }
            }
            return null
        }
        
        fun getUnresolvedMessage(link: String): String {
            EP_NAME.extensionList.forEach {
                if(link.startsWith(it.linkPrefix)) {
                    val result = it.getUnresolvedMessage(link)
                    if(result != null) return result
                }
            }
            return PlsBundle.message("path.reference.unresolved", link)
        }
        
        fun create(element: PsiElement, plainLink: Boolean = true): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.create(element, plainLink) }
        }
    }
}