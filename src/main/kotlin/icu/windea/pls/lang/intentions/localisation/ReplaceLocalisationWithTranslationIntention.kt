package icu.windea.pls.lang.intentions.localisation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.localisation.psi.*

class ReplaceLocalisationWithTranslationIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (PlsTranslationManager.findTool() == null) return false
        return super.isAvailable(project, editor, file)
    }

    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        //TODO 1.4.2
    }
}
