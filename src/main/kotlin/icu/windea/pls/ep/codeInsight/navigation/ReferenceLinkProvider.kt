package icu.windea.pls.ep.codeInsight.navigation

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle

/**
 * 用于将特定的链接（快速文档中的PSI链接，或是html/markdown等文件中的链接）解析为匹配的目标引用（定义、本地化等）。
 *
 * @see icu.windea.pls.lang.references.paths.ParadoxPathReferenceProvider
 */
interface ReferenceLinkProvider {
    val linkPrefix: String

    fun resolve(link: String, contextElement: PsiElement): PsiElement?

    fun getUnresolvedMessage(link: String): String? = null

    fun createPsiLink(element: PsiElement, plainLink: Boolean = true): String? = null

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ReferenceLinkProvider>("icu.windea.pls.referenceLinkProvider")

        fun supports(link: String): Boolean {
            return EP_NAME.extensionList.any { ep ->
                link.startsWith(ep.linkPrefix)
            }
        }

        fun resolve(link: String, contextElement: PsiElement): PsiElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!link.startsWith(ep.linkPrefix)) return@f null
                ep.resolve(link, contextElement)
            }
        }

        fun getUnresolvedMessage(link: String): String {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!link.startsWith(ep.linkPrefix)) return@f null
                ep.getUnresolvedMessage(link)
            } ?: PlsBundle.message("path.reference.unresolved", link)
        }

        fun createPsiLink(element: PsiElement, plainLink: Boolean = true): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.createPsiLink(element, plainLink)
            }
        }
    }
}
