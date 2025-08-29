package icu.windea.pls.cwt.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.psi.CwtElementTypes.LEFT_BRACE
import icu.windea.pls.cwt.psi.CwtElementTypes.RIGHT_BRACE

class CwtBraceMatcher : PairedBraceMatcher {
    private val _bracePairs = arrayOf(
        BracePair(LEFT_BRACE, RIGHT_BRACE, true),
    )

    override fun getPairs(): Array<BracePair> = _bracePairs

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
