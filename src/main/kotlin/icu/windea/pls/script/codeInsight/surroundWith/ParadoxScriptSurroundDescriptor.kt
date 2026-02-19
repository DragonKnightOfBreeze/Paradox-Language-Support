package icu.windea.pls.script.codeInsight.surroundWith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import icu.windea.pls.lang.psi.PlsPsiFileManager
import icu.windea.pls.script.psi.ParadoxScriptBoundMemberContainer

// com.intellij.json.surroundWith.JsonSurroundDescriptor
// com.intellij.json.surroundWith.JsonSurrounderBase
// com.intellij.json.surroundWith.JsonWithObjectLiteralSurrounder

class ParadoxScriptSurroundDescriptor : SurroundDescriptor {
    private val _surrounders = arrayOf(
        ParadoxScriptPropertySurrounder(),
        ParadoxScriptBlockSurrounder(),
        ParadoxScriptParameterConditionSurrounder()
    )

    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<out PsiElement> {
        return PlsPsiFileManager.findElementsBetween(file, startOffset, endOffset) { getContainer(it, startOffset, endOffset) }
            .filter { it !is PsiWhiteSpace }
            .toList()
            .toTypedArray<PsiElement>()
    }

    private fun getContainer(element: PsiElement, startOffset: Int, endOffset: Int): ParadoxScriptBoundMemberContainer? {
        val container = element.parentOfType<ParadoxScriptBoundMemberContainer>() ?: return null
        if (container.leftBound?.takeIf { it.endOffset <= startOffset } == null) return null
        if (container.rightBound?.takeIf { it.startOffset >= endOffset } == null) return null
        return container
    }

    override fun getSurrounders(): Array<Surrounder> {
        return _surrounders
    }

    override fun isExclusive(): Boolean {
        return false
    }
}
