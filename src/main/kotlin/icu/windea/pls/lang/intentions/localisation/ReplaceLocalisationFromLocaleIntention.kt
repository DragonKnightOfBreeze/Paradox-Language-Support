package icu.windea.pls.lang.intentions.localisation

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.localisation.psi.*

class ReplaceLocalisationFromLocaleIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationFromLocale")

    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        //TODO 1.4.2
    }
}
