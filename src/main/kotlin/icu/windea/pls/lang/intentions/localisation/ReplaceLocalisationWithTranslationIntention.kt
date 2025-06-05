package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.createNotification
import icu.windea.pls.localisation.psi.*

class ReplaceLocalisationWithTranslationIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (PlsTranslationManager.findTool() == null) return false
        return super.isAvailable(project, editor, file)
    }

    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        //TODO 1.4.2

        val content = PlsBundle.message("intention.replaceLocalisationWithTranslation.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }
}
