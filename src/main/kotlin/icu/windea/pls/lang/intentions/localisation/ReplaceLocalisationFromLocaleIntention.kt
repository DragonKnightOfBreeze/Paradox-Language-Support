package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.createNotification
import icu.windea.pls.localisation.psi.*

class ReplaceLocalisationFromLocaleIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationFromLocale")

    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        //TODO 1.4.2

        val content = PlsBundle.message("intention.replaceLocalisationFromLocale.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }
}
