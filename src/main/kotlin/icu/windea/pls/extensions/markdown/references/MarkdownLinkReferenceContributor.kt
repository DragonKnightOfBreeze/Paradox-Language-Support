@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.references

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.references.paths.ParadoxPathReference
import icu.windea.pls.lang.references.paths.ParadoxPathReferenceProvider
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestination

/**
 * 为 Markdown 链接目标提供 [ParadoxPathReference]。
 *
 * 从 263 基线开始，[MarkdownLinkDestination] 的引用不再通过 `PathReferenceManager` 构建，
 * 因此这里显式接入 [ParadoxPathReferenceProvider]。
 */
class MarkdownLinkReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            psiElement(MarkdownLinkDestination::class.java),
            object : PsiReferenceProvider() {
                private val pathReferenceProvider = ParadoxPathReferenceProvider()

                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    val references = mutableListOf<PsiReference>()
                    pathReferenceProvider.createReferences(element, references, false)
                    return references.toTypedArray()
                }
            }
        )
    }
}
