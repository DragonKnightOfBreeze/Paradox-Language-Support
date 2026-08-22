package icu.windea.pls.lang.intentions.csv

import com.intellij.modcommand.ActionContext
import icu.windea.pls.core.intentions.CopyOnElementIntentionBase
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.lang.intentions.ParadoxCopyIntentionService

class CopyDefinitionNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionName(context)
}

class CopyDefinitionPresentableNameIntention : CopyOnElementIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.copyDefinitionPresentableName")
    override fun getText(context: ActionContext) = ParadoxCopyIntentionService.getDefinitionPresentableName(context)
}
