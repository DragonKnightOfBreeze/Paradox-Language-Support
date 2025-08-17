package icu.windea.pls.localisation.codeInsight.editorActions.moveUpDown

import com.intellij.codeInsight.editorActions.moveUpDown.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationMover : LineMover() {
    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (file.language !is ParadoxLocalisationLanguage) return false
        if (!super.checkAvailable(editor, file, info, down)) return false
        return true
    }
}
