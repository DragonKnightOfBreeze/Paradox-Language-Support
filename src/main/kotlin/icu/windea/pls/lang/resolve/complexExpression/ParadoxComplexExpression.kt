package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 复杂表达式。
 *
 * 对应脚本语言与本地化语言中的一段特定的表达式文本，它们可能包含数个节点，且允许嵌套包含。
 *
 * 目前支持的复杂表达式种类包括：
 * - [ParadoxTemplateExpression] - 模板表达式。规则数据类型：[CwtDataTypes.TemplateExpression]。
 * - [ParadoxScopeFieldExpression] - 作用域字段表达式。规则数据类型：[CwtDataTypeSets.ScopeField]。
 * - [ParadoxValueFieldExpression] - 值字段表达式。规则数据类型：[CwtDataTypeSets.ValueField]。
 * - [ParadoxVariableFieldExpression] - 变量字段表达式。规则数据类型：[CwtDataTypeSets.ValueField]。
 * - [ParadoxCommandExpression] - （本地化）命令表达式。规则数据类型：[CwtDataTypes.Command]。
 * - [ParadoxDynamicValueExpression] - 变量值表达式。规则数据类型：[CwtDataTypeSets.DynamicValue]。
 * - [ParadoxScriptValueReferenceExpression] - 脚本值引用表达式。
 * - [ParadoxDefineReferenceExpression] - 定值引用表达式。规则数据类型：[CwtDataTypes.DefineReference]。
 * - [ParadoxArrayDefineReferenceExpression] - 数组定值引用表达式。规则数据类型：[CwtDataTypes.ArrayDefineReference]。
 * - [ParadoxTagsExpression] - 标签集合表达式。规则数据类型：[CwtDataTypes.Tags]。
 * - [ParadoxDatabaseObjectExpression] - 数据库对象表达式。规则数据类型为 [CwtDataTypes.DatabaseObject]。
 * - [ParadoxNameFormatExpression] - 命名格式表达式。规则数据类型：[CwtDataTypes.NameFormat]。
 *
 * @see ParadoxComplexExpressionNode
 */
interface ParadoxComplexExpression : ParadoxComplexExpressionNode {
    fun getErrors(element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> = emptyList()

    fun getAllErrors(element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> = emptyList()

    fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference> = emptyList()

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
            return ParadoxComplexExpressionResolver.resolve(element, configGroup)
        }

        @JvmStatic
        fun resolveByConfig(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
            return ParadoxComplexExpressionResolver.resolveByConfig(text, range, configGroup, config)
        }
    }
}

// region Implementations

private object ParadoxComplexExpressionResolver {
    fun resolve(element: ParadoxExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
                val value = element.value
                resolveByConfig(value, null, configGroup, config)
            }
            is ParadoxLocalisationExpressionElement -> {
                if (!element.isComplexExpression()) return null
                when {
                    element.isCommandExpression() -> {
                        val value = element.value
                        ParadoxCommandExpression.resolve(value, null, configGroup)
                    }
                    element.isDatabaseObjectExpression(strict = true) -> {
                        val value = element.value
                        ParadoxDatabaseObjectExpression.resolve(value, null, configGroup)
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    fun resolveByConfig(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxComplexExpression? {
        val dataType = config.configExpression?.type ?: return null
        return when {
            dataType == CwtDataTypes.TemplateExpression -> ParadoxTemplateExpression.resolve(text, range, configGroup, config)
            dataType in CwtDataTypeSets.ScopeField -> ParadoxScopeFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeSets.ValueField -> ParadoxValueFieldExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeSets.VariableField -> ParadoxVariableFieldExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.Command -> ParadoxCommandExpression.resolve(text, range, configGroup)
            dataType in CwtDataTypeSets.DynamicValue -> ParadoxDynamicValueExpression.resolve(text, range, configGroup, config)
            dataType == CwtDataTypes.DefineReference -> ParadoxDefineReferenceExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.ArrayDefineReference -> ParadoxArrayDefineReferenceExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.Tags -> ParadoxTagsExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.DatabaseObject -> ParadoxDatabaseObjectExpression.resolve(text, range, configGroup)
            dataType == CwtDataTypes.NameFormat -> ParadoxNameFormatExpression.resolve(text, range, configGroup, config)
            else -> null
        }
    }
}

// endregion
