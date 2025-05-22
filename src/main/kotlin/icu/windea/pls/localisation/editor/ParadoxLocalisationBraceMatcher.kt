package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationBraceMatcher : PairedBraceMatcher {
    private val _bracePairs = arrayOf(
        BracePair(LEFT_QUOTE, RIGHT_QUOTE, true),
        BracePair(COLORFUL_TEXT_START, COLORFUL_TEXT_END, false),
        BracePair(PARAMETER_START, PARAMETER_END, true),
        BracePair(COMMAND_START, COMMAND_END, true),
        BracePair(ICON_START, ICON_END, false),
        BracePair(LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE, false),
        BracePair(TEXT_FORMAT_START, TEXT_FORMAT_END, true),
        BracePair(TEXT_ICON_START, TEXT_ICON_END, false),
    )

    override fun getPairs(): Array<BracePair> = _bracePairs

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
