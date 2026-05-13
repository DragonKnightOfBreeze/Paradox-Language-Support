package icu.windea.pls.lang.codeInsight.type

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.model.type.CwtTypeResolver

object CwtTypeManager {
    fun isTypedElement(element: PsiElement): Boolean {
        if (element.language !is CwtLanguage) return false
        return when {
            element is CwtExpressionElement -> true
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
     * 表达式 - 如果 [element] 表示一个表达式则可用。
     */
    fun getExpression(element: PsiElement): String? {
        return when (element) {
            is CwtExpressionElement -> CwtTypeResolver.resolveExpression(element)
            else -> null
        }
    }

    /**
     * 类型 - 如果 [element] 表示一个表达式则可用。
     */
    fun getType(element: PsiElement): String? {
        return when (element) {
            is CwtExpressionElement -> CwtTypeResolver.resolveExpressionType(element).id
            else -> null
        }
    }

    /**
     * 规则类型 - 如果 [element] 表示一个规则则可用。
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
                    CwtConfigTypes.EnumValue -> element.parent.to.singletonListOrEmpty()
                    CwtConfigTypes.DynamicValue -> element.parent.to.singletonListOrEmpty()
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
