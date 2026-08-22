package icu.windea.pls.lang.intentions.localisation

import com.intellij.modcommand.ActionContext
import icu.windea.pls.core.intentions.CopyOnElementIntentionBase
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.lang.intentions.ParadoxCopyIntentionService

class CopyScriptedVariableNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyScriptedVariableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getScriptedVariableName(context)
}

class CopyScriptedVariablePresentableNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyScriptedVariablePresentableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getScriptedVariablePresentableName(context)
}

class CopyDefinitionNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionName(context)
}

class CopyDefinitionPresentableNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionPresentableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionPresentableName(context)
}

class CopyLocalisationNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationName(context)
}

class CopyLocalisationTextIntention :  CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationText")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationText(context)
}

class CopyLocalisationTextAsPlainIntention :  CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationTextAsPlain")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationTextAsPlain(context)
}

class CopyLocalisationTextAsHtmlIntention :  CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationTextAsHtml")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationTextAsHtml(context)
}
