package icu.windea.pls.lang.documentation

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*

/**
 * 提供对快速文档链接的支持，用于点击跳转到对应的定义/本地化等。
 * 
 * 快速文档链接也能作为html/markdown等文件中的超链接使用，从而引用和跳转到指定的定义/本地化等。
 * 
 * DDS文件路径以及符合条件的快速文档链接也能作为html/markdown等文件中的图片超链接使用，从而渲染DDS图片和本地化。
 * 
 * @see icu.windea.pls.core.references.paths.ParadoxPathReferenceProvider
 */
interface DocumentationElementLinkProvider {
    val linkPrefix: String
    
    fun resolve(link: String, contextElement: PsiElement): PsiElement?
    
    fun getUnresolvedMessage(link: String): String? = null
    
    fun create(builder: StringBuilder, element: PsiElement, plainLink: Boolean = false): Boolean
    
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
        
        /**
         * 获取嵌入PSI链接的PSI元素的HTML文本。
         */
        fun getElementText(referenceElement: PsiElement, plainLink: Boolean = false): String {
            return buildString { getElementText(this, referenceElement, plainLink) }
        }
        
        /**
         * 获取嵌入PSI链接的PSI元素的HTML文本。
         */
        fun getElementText(builder: StringBuilder, element: PsiElement, plainLink: Boolean = false) {
            val text = element.text
            val references = element.references
            if(references.isEmpty()) {
                builder.append(text.escapeXml())
                return
            }
            var i = 0
            for(reference in references) {
                val startOffset = reference.rangeInElement.startOffset
                if(startOffset != i) {
                    builder.append(text.substring(i, startOffset))
                }
                i = reference.rangeInElement.endOffset
                val resolved = reference.resolve()
                if(resolved == null) {
                    builder.append(reference.rangeInElement.substring(text))
                    continue
                }
                val r = EP_NAME.extensionList.any { it.create(builder, resolved, plainLink) }
                if(!r) {
                    builder.append(reference.rangeInElement.substring(text))
                }
            }
            val endOffset = references.last().rangeInElement.endOffset
            if(endOffset != text.length) {
                builder.append(text.substring(endOffset))
            }
        }
    }
}