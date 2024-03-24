package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationIconRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if(e is ParadoxLocalisationIcon) e.name.orEmpty() else ""
        return PlsBundle.message("localisation.remove.icon", name)
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationIcon
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}