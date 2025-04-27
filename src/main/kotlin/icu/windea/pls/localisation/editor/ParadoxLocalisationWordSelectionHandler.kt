package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxLocalisationLanguage) return false
        return false
    }

    //no specific situations here
}
