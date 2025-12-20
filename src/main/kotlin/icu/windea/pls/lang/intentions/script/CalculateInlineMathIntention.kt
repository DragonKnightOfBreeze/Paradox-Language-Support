package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.PsiBasedModCommandAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.ui.calculators.ParadoxInlineMathCalculatorDialog
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalculateInlineMathIntention : PsiBasedModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.calculateInlineMath")

    override fun perform(context: ActionContext, element: ParadoxScriptInlineMath): ModCommand {
        val project = context.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                val dialog = ParadoxInlineMathCalculatorDialog(project, element)
                dialog.show()
            }
        }
        return ModCommand.nop()
    }

    override fun generatePreview(context: ActionContext?, element: ParadoxScriptInlineMath?): IntentionPreviewInfo {
        return IntentionPreviewInfo.Html(PlsBundle.message("intention.calculateInlineMath.desc"))
    }
}
