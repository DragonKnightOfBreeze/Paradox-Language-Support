package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

// com.intellij.json.editor.selection.JsonStringLiteralSelectionHandler

class ParadoxLocalisationWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxLocalisationLanguage) return false
        return false
    }

    // no specific situations here
}
