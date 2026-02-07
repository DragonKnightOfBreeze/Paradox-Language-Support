package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.cwt.psi.CwtTokenSets

object CwtPsiFileManager {
    // region Find Extensions (from elementOffset)

    fun findStringExpressionElementFromStartOfffset(file: PsiFile, offset: Int): CwtStringExpressionElement? {
        if (offset < 0) return null
        if (file.language != CwtLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType in CwtTokenSets.STRING_EXPRESSION_TOKENS }
            ?.parentOfType<CwtStringExpressionElement>()
    }

    fun findPropertyFromStartOffset(file: PsiFile, offset: Int): CwtProperty? {
        if (offset < 0) return null
        if (file.language != CwtLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType == CwtElementTypes.PROPERTY_KEY_TOKEN }
            ?.parentOfType<CwtProperty>()
    }

    // endregion
}
