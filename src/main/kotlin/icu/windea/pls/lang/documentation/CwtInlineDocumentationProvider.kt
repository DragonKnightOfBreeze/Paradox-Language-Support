package icu.windea.pls.lang.documentation

import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.InlineDocumentation
import com.intellij.platform.backend.documentation.InlineDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtPsiUtil
import icu.windea.pls.lang.psi.PlsPsiManager

@Suppress("UnstableApiUsage")
class CwtInlineDocumentationProvider : InlineDocumentationProvider {
    override fun inlineDocumentationItems(file: PsiFile?): Collection<InlineDocumentation> {
        if (file == null) return emptyList()
        val result = mutableListOf<InlineDocumentation>()
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (CwtPsiUtil.canAttachComment(element)) {
                    val ownedComments = PlsPsiManager.getOwnedComments(element) { it is CwtDocComment }
                        .castOrNull<List<CwtDocComment>>().orEmpty()
                    val comments = ownedComments.optimized() // optimized to optimize memory
                    if (comments.isNotEmpty()) {
                        val inlineDocumentation = CwtInlineDocumentation(comments)
                        result.add(inlineDocumentation)
                    }
                }
                if (CwtPsiUtil.isMemberContextElement(element)) super.visitElement(element)
            }
        })
        return result.optimized() // optimized to optimize memory
    }

    override fun findInlineDocumentation(file: PsiFile, textRange: TextRange): InlineDocumentation? {
        TODO("Not yet implemented")
    }
}
