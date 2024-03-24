package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationReferenceRemover : ParadoxLocalisationUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if(e is ParadoxLocalisationPropertyReference) e.name else ""
        return PlsBundle.message("localisation.remove.reference", name)
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxLocalisationPropertyReference
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}