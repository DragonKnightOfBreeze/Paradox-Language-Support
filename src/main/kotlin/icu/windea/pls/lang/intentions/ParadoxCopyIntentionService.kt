package icu.windea.pls.lang.intentions

import com.intellij.modcommand.ActionContext
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextPlainRenderer

object ParadoxCopyIntentionService {
    fun getScriptedVariableName(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findScriptedVariable(context.file, context.offset) { BY_NAME or BY_REFERENCE } ?: return null
        return ParadoxPsiPresentationService.getScriptedVariableName(element)
    }

    fun getScriptedVariablePresentableName(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findScriptedVariable(context.file, context.offset) { BY_NAME or BY_REFERENCE } ?: return null
        return ParadoxPsiPresentationService.getScriptedVariablePresentableName(element)
    }

    fun getDefinitionName(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findDefinition(context.file, context.offset) { BY_NAME or BY_REFERENCE } ?: return null
        return ParadoxPsiPresentationService.getDefinitionName(element)
    }

    fun getDefinitionPresentableName(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findDefinition(context.file, context.offset) { BY_NAME or BY_REFERENCE } ?: return null
        return ParadoxPsiPresentationService.getDefinitionPresentableName(element)
    }

    fun getLocalisationName(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findLocalisation(context.file, context.offset) { BY_NAME or BY_REFERENCE } ?: return null
        return ParadoxPsiPresentationService.getLocalisationName(element)
    }

    fun getLocalisationText(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findLocalisation(context.file, context.offset) { DEFAULT or BY_REFERENCE } ?: return null
        return element.value
    }

    fun getLocalisationTextAsPlain(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findLocalisation(context.file, context.offset) { DEFAULT or BY_REFERENCE } ?: return null
        return ParadoxLocalisationTextPlainRenderer().render(element)
    }

    fun getLocalisationTextAsHtml(context: ActionContext): String? {
        val element = ParadoxPsiFileService.findLocalisation(context.file, context.offset) { DEFAULT or BY_REFERENCE } ?: return null
        return ParadoxLocalisationTextHtmlRenderer().render(element)
    }
}
