package icu.windea.pls.localisation.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class TooLongScopeLinkInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxLocalisationCommand) visitCommand(element)
            }
            
            private fun visitCommand(element: ParadoxLocalisationCommand) {
                ProgressManager.checkCanceled()
                if(element.hasSyntaxError()) return //skip if any syntax error
                var firstScope: ParadoxLocalisationCommandScope? = null
                var lastScope: ParadoxLocalisationCommandScope? = null
                var size = 0
                element.processChild {
                    when {
                        it is ParadoxLocalisationCommandScope -> {
                            if(firstScope == null) firstScope = it
                            lastScope = it
                            size++
                            true
                        }
                        it is ParadoxLocalisationCommandField -> false
                        else -> true
                    }
                }
                if(size > ParadoxScopeHandler.maxScopeLinkSize) {
                    val startOffset = firstScope?.textRangeInParent?.startOffset ?: return
                    val endOffset = lastScope?.textRangeInParent?.endOffset ?: return
                    val range = TextRange.create(startOffset, endOffset)
                    val description = PlsBundle.message("inspection.localisation.scope.tooLongScopeLink.description")
                    holder.registerProblem(element, range, description)
                }
            }
        }
    }
}