package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.cwt.psi.*

class CwtPropertyUnwrapper: CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name =  if(e is CwtProperty) e.name else ""
        return PlsBundle.message("cwt.unwrap.property", name)
    }
    
    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtProperty && e.propertyValue is CwtBlock
    }
    
    override fun doUnwrap(element: PsiElement, context: Context) {
        if(element !is CwtProperty) return
        val block = element.propertyValue
        if(block !is CwtBlock) return
        context.extract(element, block)
        context.delete(element)
    }
}