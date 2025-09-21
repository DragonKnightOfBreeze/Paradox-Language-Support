package icu.windea.pls.cwt.codeInsight.template

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.util.parents
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.editor.CwtSyntaxHighlighter
import icu.windea.pls.cwt.psi.CwtMemberElement

abstract class CwtTemplateContextType(presentableName: String) : TemplateContextType(presentableName) {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        if (file.language !is CwtLanguage) return false
        return doIsInContext(templateActionContext)
    }

    abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean

    class Base : CwtTemplateContextType(PlsBundle.message("cwt.templateContextType")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            return true
        }

        override fun createHighlighter(): SyntaxHighlighter {
            return CwtSyntaxHighlighter(null)
        }
    }

    class Members : CwtTemplateContextType(PlsBundle.message("cwt.templateContextType.members")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            val startElement = start.parents(withSelf = false)
                .find { it is CwtMemberElement }
            return startElement != null
        }

        override fun createHighlighter(): SyntaxHighlighter {
            return CwtSyntaxHighlighter(null)
        }
    }
}
