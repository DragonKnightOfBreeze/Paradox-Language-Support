package icu.windea.pls.lang.intentions.localisation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ReplaceLocalisationWithTranslationIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithTranslation")

    override fun doInvoke(project: Project, editor: Editor?, file: PsiFile?, elements: List<ParadoxLocalisationProperty>) {
        TODO("Not yet implemented")
    }
}
