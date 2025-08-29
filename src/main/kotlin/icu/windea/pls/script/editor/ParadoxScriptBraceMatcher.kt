package icu.windea.pls.script.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_END
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_START
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LABS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LEFT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LEFT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LP_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NESTED_LEFT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NESTED_RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_END
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_START
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RABS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RP_SIGN

class ParadoxScriptBraceMatcher : PairedBraceMatcher {
    private val _bracePairs = arrayOf(
        BracePair(LEFT_BRACE, RIGHT_BRACE, true),
        BracePair(PARAMETER_START, PARAMETER_END, false),
        BracePair(LEFT_BRACKET, RIGHT_BRACKET, false), //cannot be structural
        BracePair(NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET, false), //cannot be structural
        BracePair(INLINE_MATH_START, INLINE_MATH_END, true),
        BracePair(LABS_SIGN, RABS_SIGN, false),
        BracePair(LP_SIGN, RP_SIGN, true),
    )

    override fun getPairs(): Array<BracePair> = _bracePairs

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
