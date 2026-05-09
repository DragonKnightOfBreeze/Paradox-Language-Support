package icu.windea.pls.model

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.model.CwtSeparatorType

enum class CwtSeparatorType(val text: String) {
    EQUAL("="), // logic equal
    NOT_EQUAL("!="), // logic not equal
    DOUBLE_EQUAL("=="), // matches comparison operators
    ;

    override fun toString() = text

    companion object {
        @JvmStatic
        fun resolve(text: String): CwtSeparatorType? {
            return when (text) {
                "=" -> EQUAL
                "!=", "<>" -> NOT_EQUAL
                "==" -> DOUBLE_EQUAL
                else -> null
            }
        }

        @JvmStatic
        fun resolve(element: PsiElement): CwtSeparatorType? {
            val elementType = element.elementType
            return when (elementType) {
                CwtElementTypes.EQUAL_SIGN -> EQUAL
                CwtElementTypes.NOT_EQUAL_SIGN -> NOT_EQUAL
                CwtElementTypes.DOUBLE_EQUAL_SIGN -> DOUBLE_EQUAL
                else -> null
            }
        }
    }
}
