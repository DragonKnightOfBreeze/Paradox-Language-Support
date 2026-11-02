package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import icu.windea.pls.core.intentions.CopyToClipboardIntention
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.lang.intentions.ParadoxCopyIntentionService

class CopyScriptedVariableNameIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyScriptedVariableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getScriptedVariableName(context)
}

class CopyScriptedVariablePresentableNameIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyScriptedVariablePresentableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getScriptedVariablePresentableName(context)
}

class CopyDefinitionNameIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionName(context)
}

class CopyDefinitionPresentableNameIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionPresentableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionPresentableName(context)
}

class CopyLocalisationNameIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationName(context)
}

class CopyLocalisationTextIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationText")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationText(context)
}

class CopyLocalisationTextAsPlainIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationTextAsPlain")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationTextAsPlain(context)
}

class CopyLocalisationTextAsHtmlIntention : CopyToClipboardIntention() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyLocalisationTextAsHtml")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getLocalisationTextAsHtml(context)
}
