package icu.windea.pls.model

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

enum class ParadoxSeparatorType(val text: String) {
    EQUAL("="),
    SAFE_EQUAL("?="), // supported in ck3, vic3 and eu5, see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/86
    NOT_EQUAL("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    ;

    override fun toString() = text

    companion object {
        @JvmStatic
        fun resolve(text: String): ParadoxSeparatorType? {
            return when (text) {
                "=" -> EQUAL
                "?=" -> SAFE_EQUAL
                "!=", "<>" -> NOT_EQUAL
                "<" -> LT
                ">" -> GT
                "<=" -> LE
                ">=" -> GE
                else -> null
            }
        }

        @JvmStatic
        fun resolve(element: PsiElement): ParadoxSeparatorType? {
            val elementType = element.elementType
            return when (elementType) {
                ParadoxScriptElementTypes.EQUAL_SIGN -> EQUAL
                ParadoxScriptElementTypes.SAFE_EQUAL_SIGN -> SAFE_EQUAL
                ParadoxScriptElementTypes.NOT_EQUAL_SIGN -> NOT_EQUAL
                ParadoxScriptElementTypes.LT_SIGN -> LT
                ParadoxScriptElementTypes.GT_SIGN -> GT
                ParadoxScriptElementTypes.LE_SIGN -> LE
                ParadoxScriptElementTypes.GE_SIGN -> GE
                else -> null
            }
        }
    }
}
