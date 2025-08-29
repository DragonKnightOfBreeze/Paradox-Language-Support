package icu.windea.pls.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBoolean
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtFloat
import icu.windea.pls.cwt.psi.CwtInt
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isOptionBlockValue
import icu.windea.pls.cwt.psi.isOptionValue
import icu.windea.pls.model.CwtType

object CwtTypeManager {
    fun isTypedElement(element: PsiElement): Boolean {
        if (element.language !is CwtLanguage) return false
        return when {
            element is CwtExpressionElement -> {
                if (element is CwtValue && (element.isOptionValue() || element.isOptionBlockValue())) return false
                true
            }
            else -> false
        }
    }

    fun findTypedElements(elementAt: PsiElement): List<PsiElement> {
        if (elementAt.language !is CwtLanguage) return emptyList()
        val element = elementAt.parents(withSelf = true).find { isTypedElement(it) }
        if (element == null) return emptyList()
        return listOf(element)
    }

    /**
     * 基本类型 - 基于PSI的类型
     */
    fun getType(element: PsiElement): CwtType {
        return when (element) {
            is CwtPropertyKey -> CwtType.String
            is CwtBoolean -> CwtType.Boolean
            is CwtInt -> CwtType.Int
            is CwtFloat -> CwtType.Float
            is CwtString -> CwtType.String
            is CwtBlock -> CwtType.Block
            else -> CwtType.Unknown
        }
    }

    /**
     * 规则类型 - 当PSI表示一个规则时可用，基于规则的位置
     */
    fun getConfigType(element: PsiElement): CwtConfigType? {
        if (!isTypedElement(element)) return null
        return CwtConfigManager.getConfigType(element)
    }

    fun findTypeDeclarations(element: PsiElement): List<PsiElement> {
        return when (element) {
            is CwtValue -> {
                val configType = getConfigType(element) ?: return emptyList()
                when (configType) {
                    CwtConfigTypes.EnumValue -> element.parent.singleton.listOrEmpty()
                    CwtConfigTypes.DynamicValue -> element.parent.singleton.listOrEmpty()
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
