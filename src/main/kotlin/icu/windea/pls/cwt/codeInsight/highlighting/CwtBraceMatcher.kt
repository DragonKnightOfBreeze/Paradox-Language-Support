package icu.windea.pls.cwt.codeInsight.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.psi.CwtElementTypes

class CwtBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(CwtElementTypes.LEFT_BRACE, CwtElementTypes.RIGHT_BRACE, true),
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
