package icu.windea.pls.localisation.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_BRACKET
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_BRACKET
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_SINGLE_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_START

class ParadoxLocalisationBraceMatcher : PairedBraceMatcher {
    private val _bracePairs = arrayOf(
        BracePair(LEFT_QUOTE, RIGHT_QUOTE, true),
        BracePair(COLORFUL_TEXT_START, COLORFUL_TEXT_END, false),
        BracePair(PARAMETER_START, PARAMETER_END, true),
        BracePair(LEFT_BRACKET, RIGHT_BRACKET, true),
        BracePair(ICON_START, ICON_END, false),
        BracePair(LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE, false),
        BracePair(TEXT_FORMAT_START, TEXT_FORMAT_END, true),
        BracePair(TEXT_ICON_START, TEXT_ICON_END, false),
    )

    override fun getPairs(): Array<BracePair> = _bracePairs

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
