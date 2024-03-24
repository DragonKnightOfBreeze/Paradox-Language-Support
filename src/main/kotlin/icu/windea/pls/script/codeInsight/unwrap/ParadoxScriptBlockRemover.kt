package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptBlockRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message("script.remove.block")
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptBlock && e.isBlockValue()
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}