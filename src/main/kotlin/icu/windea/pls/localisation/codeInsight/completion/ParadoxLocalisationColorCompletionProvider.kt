package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供颜色ID的代码补全。
 */
class ParadoxLocalisationColorCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val insertHandler = InsertHandler<LookupElement> { context, _ ->
        //delete existing colorId after press enter
        if (context.completionChar == '\n' || context.completionChar == '\r') {
            val editor = context.editor
            val offset = editor.caretModel.offset
            editor.document.deleteString(offset, offset + 1)
        }
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val file = parameters.originalFile
        val originalColorId = file.findElementAt(parameters.offset)
            ?.takeIf { it.elementType == ParadoxLocalisationElementTypes.COLOR_TOKEN }
        val project = file.project
        val colorConfigs = ParadoxTextColorManager.getInfos(project, file)
        val lookupElements = mutableListOf<LookupElement>()
        for (colorConfig in colorConfigs) {
            ProgressManager.checkCanceled()
            val element = colorConfig.pointer.element ?: continue
            val name = colorConfig.name
            val icon = colorConfig.icon
            val tailText = " from <textcolor>"
            val typeFile = colorConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .letIf(originalColorId != null) {
                    it.withInsertHandler(insertHandler)
                }
            lookupElements.add(lookupElement)
        }
        result.addAllElements(lookupElements)
    }
}
