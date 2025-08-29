package icu.windea.pls.cwt.codeInsight.surroundWith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.findElementsBetween
import icu.windea.pls.cwt.psi.CwtBlockElement

//com.intellij.json.surroundWith.JsonSurroundDescriptor
//com.intellij.json.surroundWith.JsonSurrounderBase
//com.intellij.json.surroundWith.JsonWithObjectLiteralSurrounder

class CwtSurroundDescriptor : SurroundDescriptor {
    private val _surrounders = arrayOf(
        CwtPropertySurrounder(),
        CwtBlockSurrounder(),
    )

    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
        return file.findElementsBetween(startOffset, endOffset, { it.parentOfType<CwtBlockElement>() }) {
            it.takeIf { it !is PsiWhiteSpace }
        }.toTypedArray()
    }

    override fun getSurrounders(): Array<Surrounder> {
        return _surrounders
    }

    override fun isExclusive(): Boolean {
        return false
    }
}
