package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptPsiService

@Optimized
object ParadoxParameterContextService {
    fun resolveArguments(element: ParadoxScriptBlock): List<Tuple2<String, String>> {
        return buildList {
            for (p in element.properties()) {
                val k = p.propertyKey.name.orNull() ?: continue
                if (!ParadoxNameValidators.checkParameterName(k)) continue
                val v = p.propertyValue?.text ?: continue
                this += tupleOf(k, v)
            }
        }
    }

    fun replaceTextWithArgs(element: PsiElement, args: List<Tuple2<String, String>>, direct: Boolean): String {
        if (direct) {
            val oldText = element.text
            var newText = oldText
            args.forEachFast { (k, v) ->
                newText = newText.replace("$$k$", v.unquote())
            }
            return newText
        } else {
            val offset = element.startOffset
            val argMap = args.toMap()
            val replacements = mutableListOf<Tuple2<TextRange, String>>()

            element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun elementFinished(element: PsiElement) {
                    run {
                        if (element !is ParadoxScriptConditionalBlock) return@run
                        val conditionalExpression = element.conditionalExpression ?: return@run
                        val parameter = conditionalExpression.conditionalParameter
                        val name = parameter.name
                        val v = argMap[name] ?: return@run
                        val revert = v.equals("no", true)
                        val operator = conditionalExpression.children().oneBy(ParadoxScriptElementTypes.NOT_SIGN) == null
                        if ((!revert && operator) || (revert && !operator)) {
                            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
                            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
                            if (start != null && end != null) {
                                element.parent.addRangeAfter(start, end, element)
                            }
                        }
                        element.delete()
                    }
                }
            })

            element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    run {
                        if (element !is ParadoxScriptParameter) return@run
                        val n = element.name ?: return@run
                        val v0 = argMap[n] ?: return@run
                        val v = v0
                        replacements.add(tupleOf(element.textRange.shiftLeft(offset), v))
                        return
                    }
                    super.visitElement(element)
                }
            })

            var newText = element.text
            replacements.forEachReversedFast { (range, v) ->
                newText = newText.replaceRange(range.startOffset, range.endOffset, v)
            }
            return newText
        }
    }
}
