package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

class ParadoxLocalisationWordSelectionHandler: ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if(e.language != ParadoxLocalisationLanguage) return false
        return false
    }
    
    //no specific situations here
}
