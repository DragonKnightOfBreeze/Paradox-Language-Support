package icu.windea.pls.script.codeInsight.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

class ParadoxScriptBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(ParadoxScriptElementTypes.LEFT_BRACE, ParadoxScriptElementTypes.RIGHT_BRACE, true),
        BracePair(ParadoxScriptElementTypes.PARAMETER_START, ParadoxScriptElementTypes.PARAMETER_END, false),
        BracePair(ParadoxScriptElementTypes.LEFT_BRACKET, ParadoxScriptElementTypes.RIGHT_BRACKET, false), // cannot be structural
        BracePair(ParadoxScriptElementTypes.NESTED_LEFT_BRACKET, ParadoxScriptElementTypes.NESTED_RIGHT_BRACKET, false), // cannot be structural
        BracePair(ParadoxScriptElementTypes.INLINE_MATH_START, ParadoxScriptElementTypes.INLINE_MATH_END, true),
        BracePair(ParadoxScriptElementTypes.LABS_SIGN, ParadoxScriptElementTypes.RABS_SIGN, false),
        BracePair(ParadoxScriptElementTypes.LP_SIGN, ParadoxScriptElementTypes.RP_SIGN, true),
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
