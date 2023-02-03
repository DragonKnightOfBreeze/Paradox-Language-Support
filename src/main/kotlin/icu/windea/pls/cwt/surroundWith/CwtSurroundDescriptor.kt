package icu.windea.pls.cwt.surroundWith

import com.intellij.lang.surroundWith.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

//com.intellij.json.surroundWith.JsonSurroundDescriptor
//com.intellij.json.surroundWith.JsonSurrounderBase
//com.intellij.json.surroundWith.JsonWithObjectLiteralSurrounder

class CwtSurroundDescriptor: SurroundDescriptor {
    companion object {
        private val defaultSurrounders = arrayOf(
            CwtClausePropertySurrounder(),
            CwtClauseSurrounder(),
        )
    }
    
    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
        return file.findElementsBetween(startOffset, endOffset, { it.parentOfType<CwtBlockElement>() }) {
            it.takeIf { it !is PsiWhiteSpace }
        }.toTypedArray()
    }
    
    override fun getSurrounders(): Array<Surrounder> {
        return defaultSurrounders
    }
    
    override fun isExclusive(): Boolean {
        return false
    }
}