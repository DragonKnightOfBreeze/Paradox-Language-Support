package icu.windea.pls.lang.intentions

import com.intellij.modcommand.ActionContext
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextPlainRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxCopyIntentionService {
     fun getScriptedVariableName(context: ActionContext): String? {
        val element = findScriptedVariable(context) ?: return null
        return element.name?.orNull()
    }

    fun getScriptedVariablePresentableName(context: ActionContext): String? {
        val element = findScriptedVariable(context) ?: return null
        // ParadoxHintTextProvider.getHintText(element)?.let { return it }
        return ParadoxScriptedVariableManager.getPresentableName(element)?.orNull()
    }

    fun getDefinitionName(context: ActionContext): String? {
        val element = findDefinition(context) ?: return null
        return element.definitionInfo?.name?.orNull()
    }

    fun getDefinitionPresentableName(context: ActionContext): String? {
        val element = findDefinition(context) ?: return null
        // ParadoxHintTextProvider.getHintText(element)?.let { return it }
        return ParadoxDefinitionManager.getPresentableName(element)
    }

    fun getLocalisationName(context: ActionContext): String? {
        val element = findLocalisation(context) ?: return null
        return element.name.orNull()
    }

     fun getLocalisationText(context: ActionContext): String? {
        val element = findLocalisation(context) ?: return null
        return element.value
    }

    fun getLocalisationTextAsPlain(context: ActionContext): String? {
        val element = findLocalisation(context) ?: return null
        return ParadoxLocalisationTextPlainRenderer().render(element)
    }

    fun getLocalisationTextAsHtml(context: ActionContext): String? {
        val element = findLocalisation(context) ?: return null
        return ParadoxLocalisationTextHtmlRenderer().render(element)
    }

    private fun findScriptedVariable(context: ActionContext): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFileService.findScriptedVariable(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }

    private fun findDefinition(context: ActionContext): ParadoxDefinitionElement? {
        return ParadoxPsiFileService.findDefinition(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }

    private fun findLocalisation(context: ActionContext): ParadoxLocalisationProperty? {
        return ParadoxPsiFileService.findLocalisation(context.file, context.offset) { DEFAULT or BY_REFERENCE }
    }
}
