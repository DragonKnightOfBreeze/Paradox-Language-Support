package icu.windea.pls.localisation.codeInsight.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes

class ParadoxLocalisationBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(ParadoxLocalisationElementTypes.LEFT_QUOTE, ParadoxLocalisationElementTypes.RIGHT_QUOTE, true),
        BracePair(ParadoxLocalisationElementTypes.COLORFUL_TEXT_START, ParadoxLocalisationElementTypes.COLORFUL_TEXT_END, false),
        BracePair(ParadoxLocalisationElementTypes.PARAMETER_START, ParadoxLocalisationElementTypes.PARAMETER_END, true),
        BracePair(ParadoxLocalisationElementTypes.LEFT_BRACKET, ParadoxLocalisationElementTypes.RIGHT_BRACKET, true),
        BracePair(ParadoxLocalisationElementTypes.ICON_START, ParadoxLocalisationElementTypes.ICON_END, false),
        BracePair(ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE, ParadoxLocalisationElementTypes.RIGHT_SINGLE_QUOTE, false),
        BracePair(ParadoxLocalisationElementTypes.TEXT_FORMAT_START, ParadoxLocalisationElementTypes.TEXT_FORMAT_END, true),
        BracePair(ParadoxLocalisationElementTypes.TEXT_ICON_START, ParadoxLocalisationElementTypes.TEXT_ICON_END, false),
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
