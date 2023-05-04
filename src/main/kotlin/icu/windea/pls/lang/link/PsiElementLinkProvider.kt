package icu.windea.pls.lang.link

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 提供对快速文档链接的支持，用于点击跳转到对应的定义/本地化等。（以后也许也能用在快速文档以外的其他地方）
 */
interface PsiElementLinkProvider {
    val linkPrefix: String
    
    fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<PsiElementLinkProvider>("icu.windea.pls.linkProvider")
        
        fun resolveLink(link: String, contextElement: PsiElement): PsiElement? {
            EP_NAME.extensionList.forEach {
                val shortLink = link.removePrefixOrNull(it.linkPrefix)
                if(shortLink != null) {
                    val result = it.resolveLink(shortLink, contextElement)
                    if(result != null) return result
                }
            }
            return null
        }
    }
}