package icu.windea.pls.localisation.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

abstract class ParadoxLocalisationTemplateContextType(presentableName: String) : TemplateContextType(presentableName) {
    final override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        if (file.language !is ParadoxLocalisationLanguage) return false
        return doIsInContext(templateActionContext)
    }

    abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean

    class Base : ParadoxLocalisationTemplateContextType(PlsBundle.message("localisation.templateContextType")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            return true
        }

        override fun createHighlighter(): SyntaxHighlighter? {
            return ParadoxLocalisationSyntaxHighlighter(null, null)
        }
    }

    class LocalisationText : ParadoxLocalisationTemplateContextType(PlsBundle.message("localisation.templateContextType.localisationText")) {
        override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
            val file = templateActionContext.file
            val startOffset = templateActionContext.startOffset
            val start = file.findElementAt(startOffset) ?: return false
            if (start.elementType == LEFT_QUOTE) return false
            val startElement = start.parentOfType<ParadoxLocalisationPropertyValue>()
            return startElement != null
        }

        override fun createHighlighter(): SyntaxHighlighter? {
            return ParadoxLocalisationTextSyntaxHighlighter(null, null)
        }
    }
}
