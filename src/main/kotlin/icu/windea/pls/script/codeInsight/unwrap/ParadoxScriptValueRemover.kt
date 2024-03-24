package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if(e is ParadoxScriptValue) e.name else ""
        return PlsBundle.message("script.remove.value", name)
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return (e is ParadoxScriptValue && e !is ParadoxScriptBlock) && e.isBlockValue()
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}