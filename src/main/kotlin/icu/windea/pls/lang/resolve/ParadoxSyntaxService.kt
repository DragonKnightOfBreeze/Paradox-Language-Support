package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.settings.PlsConfigSettings
import icu.windea.pls.core.indicesOf
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.type.CwtSeparatorType
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxSyntaxService {
    // region Script

    /**
     * 判断 [element] 的键和值是否可以表示一个数值。
     */
    fun isNumberRepresentable(element: ParadoxScriptProperty): Boolean? {
        val propertyKey = element.propertyKey
        if (!isNumberRepresentable(propertyKey)) return false
        val propertyValue = element.propertyValue ?: return null
        if (!isNumberRepresentable(propertyValue)) return false
        return true
    }

    @Suppress("unused")
    fun isNumberRepresentable(element: ParadoxScriptPropertyKey): Boolean {
        // always true
        return true
    }

    fun isNumberRepresentable(element: ParadoxScriptValue): Boolean {
        // string literal, or number after revolution and evaluation
        val resolved = element.resolved()
        return when (resolved) {
            is ParadoxScriptInt, is ParadoxScriptFloat, is ParadoxScriptString -> true
            is ParadoxScriptInlineMath -> true
            is ParadoxScriptBlock -> true // #291 [VIC3/EU5] we can do comparisons between numbers and formula blocks
            else -> false
        }
    }

    /**
     * 判断 [element] 是否允许使用比较运算符作为属性分隔符。文法级别。
     *
     * 要求键和值可以表示一个数值。
     */
    fun isComparisonOperatorAllowed(element: ParadoxScriptProperty): Boolean? {
        return isNumberRepresentable(element)
    }

    /**
     * 判断 [element] 是否允许使用比较运算符作为属性分隔符。语义级别。
     *
     * 要求匹配的规则（[CwtPropertyConfig]）显式使用 `==` 作为属性分隔符，而不是常规的 `=`。
     */
    fun isComparisonOperatorStrictValid(element: ParadoxScriptProperty): Boolean? {
        // TODO 2.1.4+ further verification and optimization for config files (mainly `triggers.cwt`) is needed
        if (!PlsConfigSettings.getInstance().state.features.checkComparisonOperators) return null
        val configs = ParadoxConfigManager.getConfigs(element)
        if (configs.isEmpty()) return null
        return configs.any { config -> isComparisonOperatorValid(config) }
    }

    /**
     * 判断 [config] 对应的脚本属性是否允许使用比较运算符作为属性分隔符。
     *
     * 要求此规则（[CwtPropertyConfig]）显式使用 `==` 作为属性分隔符，而不是常规的 `=`。
     */
    fun isComparisonOperatorValid(config: CwtMemberConfig<*>): Boolean {
        return config is CwtPropertyConfig && config.separatorType == CwtSeparatorType.DoubleEqual
    }

    /**
     * 判断 [element] 是否允许使用安全赋值运算符作为属性分隔符。文法级别。
     *
     * 要求键的表达式类型是字符串。
     */
    fun isSafeAssignOperatorAllowed(element: ParadoxScriptProperty): Boolean {
        val expressionType = ParadoxTypeResolver.resolveExpressionType(element.propertyKey)
        return expressionType == ParadoxExpressionType.String
    }

    /**
     * 判断 [element] 是否允许使用安全调用赋值运算符作为属性分隔符。文法级别。
     *
     * 要求键的表达式类型是字符串。
     */
    fun isSafeCallAssignOperatorAllowed(element: ParadoxScriptProperty): Boolean {
        val expressionType = ParadoxTypeResolver.resolveExpressionType(element.propertyKey)
        return expressionType == ParadoxExpressionType.String
    }

    // endregion

    // region Localisation

    fun getIncorrectLeftBracketEscapeIndices(element: PsiElement, file: PsiFile? = null): List<Int> {
        if (element.elementType != TEXT_TOKEN) return emptyList()
        val file = file ?: element.containingFile ?: return emptyList()
        if (VirtualFileService.isInjectedFile(file.virtualFile)) return emptyList() // only for actual localisation files, skip injected files (e.g., in script strings)
        val text = element.text
        return text.indicesOf("\\[")
    }

    fun isDanglingColorfulTextEndMarker(element: PsiElement): Boolean {
        if (element.elementType != COLORFUL_TEXT_END) return false
        if (element.nextSibling == null && element.parent?.elementType == COLORFUL_TEXT) return false
        return true
    }

    fun isDanglingTextFormatEndMarker(element: PsiElement): Boolean {
        if (element.elementType != TEXT_FORMAT_END) return false
        if (element.nextSibling == null && element.parent?.elementType == TEXT_FORMAT) return false
        return true
    }

    // endregion
}
