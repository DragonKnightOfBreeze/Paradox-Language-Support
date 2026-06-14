package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.attributes.CwtDeclarationConfigAttributes
import icu.windea.pls.config.attributes.CwtDeclarationConfigAttributesEvaluator
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则。
 *
 * 用于描述定义声明的结构，从而在定义声明中提供代码补全、代码检查等功能。
 *
 * 说明：
 * - 可在其中通过 `subtype[{expression}] = {...}` 指定需要匹配的子类型。其中 `{expression}` 为子类型表达式（[ParadoxDefinitionSubtypeExpression]）。支持嵌套使用。
 * - 可在其中引用别名规则（[CwtAliasConfig]）与单别名规则（[CwtSingleAliasConfig]），从而简化声明规则的编写。
 * - 切换类型（swapped type）的声明规则可以直接嵌套在对应的基础类型（base type）的声明规则中。
 *
 * 路径定位：
 * - `{name}`。其中 `{name}` 匹配规则名称。
 * - 对于规则文件中的顶级属性，如果未在解析其他规则的过程中被匹配到，且键是一个合法的标识符，最终都会在回退时尝试解析为声明规则。
 *
 * ### CWTools 兼容性
 *
 * 兼容。
 *
 * ### 示例
 *
 * ```cwt
 * event = {
 *     id = scalar
 *     subtype[triggered] = { # 通过子类型表达式限定定义声明的结构
 *         ## cardinality = 0..1
 *         weight_multiplier = {
 *             factor = float
 *             alias_name[modifier_rule] = alias_match_left[modifier_rule] # 引用别名规则
 *         }
 *     }
 *     ## cardinality = 0..1
 *     trigger = single_alias_right[trigger_clause] # 引用单别名规则
 *     # ...
 * }
 * ```
 *
 * @property name 规则名称（即定义的类型名）。
 * @property attributes 综合属性。
 * @property configForDeclaration 经过处理后的顶级成员规则，可以直接用于确定定义声明的结构。
 *
 * @see CwtTypeConfig
 * @see CwtSubtypeConfig
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName
    val name: String

    val attributes: CwtDeclarationConfigAttributes
    val configForDeclaration: CwtPropertyConfig

    companion object {
        /** 由属性规则解析为声明规则，可指定 [name] 以覆盖规则名称。 */
        @JvmStatic
        @JvmOverloads
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig? {
            return CwtDeclarationConfigResolver.resolve(config, name)
        }
    }
}

// region Implementations

private object CwtDeclarationConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig, inputName: String?): CwtDeclarationConfig? {
        val name = inputName ?: config.key.takeIf { it.isIdentifier() } ?: return null
        logger.debug { "Resolved declaration config (name: $name).".withLocationPrefix(config) }
        return CwtDeclarationConfigImpl(config, name)
    }
}

private class CwtDeclarationConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtDeclarationConfig {
    override val attributes: CwtDeclarationConfigAttributes by lazy { CwtDeclarationConfigAttributesEvaluator().evaluate(this) }
    override val configForDeclaration: CwtPropertyConfig by lazy { computeConfigForDeclaration() }

    private fun computeConfigForDeclaration(): CwtPropertyConfig {
        return CwtConfigManipulationService.inlineSingleAlias(config) ?: config
    }

    override fun toString() = "CwtDeclarationConfigImpl(name='$name')"
}

// endregion
