package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.select.*
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则。
 *
 * 用于描述定义声明的结构，从而在定义声明中提供代码补全、代码检查等功能。
 *
 * 说明：
 * - 可在其中通过子类型表达式（[ParadoxDefinitionSubtypeExpression]）限定定义声明的结构。
 * - 可在其中引用别名规则（[CwtAliasConfig]）与单别名规则（[CwtSingleAliasConfig]），从而简化声明规则的编写。
 * - 切换类型（swapped type）的声明规则可以直接嵌套在对应的基础类型（base type）的声明规则中。
 *
 * 路径定位：`{name}`，`{name}` 匹配规则名称（定义类型）。
 * - 任何无法在解析其他规则的过程中被匹配到的顶级属性，如果键是一个合法的标识符，最终都会在回退时尝试解析为声明规则。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
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
 * @property name 名称。
 * @property configForDeclaration 可直接用于检查定义声明的结构，经过处理后的属性规则。
 * @property subtypesUsedInDeclaration 其中的子类型表达式（[ParadoxDefinitionSubtypeExpression]）中使用到的子类型的集合。
 *
 * @see CwtTypeConfig
 * @see CwtSubtypeConfig
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String

    val configForDeclaration: CwtPropertyConfig
    val subtypesUsedInDeclaration: Set<String>

    interface Resolver {
        /** 由属性规则解析为声明规则，可指定 [name] 以覆盖规则名称。 */
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig?
    }

    companion object : Resolver by CwtDeclarationConfigResolverImpl()
}

// region Implementations

private class CwtDeclarationConfigResolverImpl : CwtDeclarationConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig, name: String?): CwtDeclarationConfig? = doResolve(config, name)

    private fun doResolve(config: CwtPropertyConfig, inputName: String?): CwtDeclarationConfig? {
        val name = inputName ?: config.key.takeIf { it.isIdentifier() } ?: return null
        logger.debug { "Resolved declaration config (name: $name).".withLocationPrefix(config) }
        return CwtDeclarationConfigImpl(config, name)
    }
}

private class CwtDeclarationConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtDeclarationConfig {
    override val configForDeclaration: CwtPropertyConfig by lazy {
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    override val subtypesUsedInDeclaration: Set<String> by lazy {
        val result = sortedSetOf<String>()
        selectConfigScope {
            config.walkDown().asProperty().forEach { c ->
                val subtypeExpression = c.key.removeSurroundingOrNull("subtype[", "]")
                if (subtypeExpression != null) {
                    val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
                    resolved.subtypes.forEach { (subtype, _) -> result.add(subtype) }
                }
            }
        }
        result.optimized()
    }

    override fun toString() = "CwtDeclarationConfigImpl(name='$name')"
}


// endregion
