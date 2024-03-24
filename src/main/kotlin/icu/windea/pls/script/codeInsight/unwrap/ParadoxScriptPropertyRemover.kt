package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if(e is ParadoxScriptProperty) e.name else ""
        return PlsBundle.message("script.remove.property", name)
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptProperty
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}