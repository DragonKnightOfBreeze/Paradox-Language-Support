package icu.windea.pls.script.codeInsight.template

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.editor.ParadoxScriptSyntaxHighlighter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionExpression

abstract class ParadoxScriptTemplateContextType(presentableName: String) : TemplateContextType(presentableName) {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        if (file.language !is ParadoxScriptLanguage) return false
        return doIsInContext(templateActionContext)
    }

    abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean

    class Base : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            return true
        }

        override fun createHighlighter(): SyntaxHighlighter {
            return ParadoxScriptSyntaxHighlighter(null, null)
        }
    }

    class Members : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.members")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            val startElement = start.parents(withSelf = false)
                .find {
                    if (it is ParadoxScriptInlineMath && it.startOffset != startOffset) return false
                    if (it is ParadoxScriptParameterConditionExpression && it.startOffset != startOffset) return false
                    it is ParadoxScriptMemberElement
                }
            return startElement != null
        }

        override fun createHighlighter(): SyntaxHighlighter {
            return ParadoxScriptSyntaxHighlighter(null, null)
        }
    }

    class InlineMathExpressions : ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.inlineMathExpressions")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file.originalFile
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            if (start.elementType == ParadoxScriptElementTypes.INLINE_MATH_START) return false
            val startElement = start.parentOfType<ParadoxScriptInlineMath>()
            return startElement != null
        }
    }
}
