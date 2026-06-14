package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxScopeManager

/**
 * 修正规则。
 *
 * 用于提供修正的相关信息（快速文档、命名、分类）。
 *
 * 修正（modifier）是一种常见的谓语，用于调整游戏数值。
 * 其名字是预定义的，也可能是动态生成的。
 * 其允许的作用域类型则来自所属的分类。
 * 可参见：`modifiers.log`。
 *
 * 规则名称在这里用于匹配（而非等同于）修正名，可以是：
 * - 常量（[Constant][CwtDataTypes.Constant]） - 匹配预定义的修正。
 * - 模板表达式（[TemplateExpression][CwtDataTypes.TemplateExpression]） - 匹配动态生成的修正。
 *
 * 路径定位：
 * - `modifiers/{name}`。其中 `{name}` 匹配规则名称。
 * - `types/type[{type}]/modifiers/{name}`。其中 `{type}` 匹配定义类型，`{name}` 匹配规则名称（其中的 `$` 会被替换为 `<{type}>`）。
 * - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`。其中 `{subtype}` 匹配定义的子类型。
 *
 * ### CWTools 兼容性
 *
 * 兼容。
 *
 * ### 示例
 *
 * ```cwt
 * # 来自 modifiers.cwt
 * modifiers = {
 *     pop_happiness = { Pops } # `Pops` is the modifier category
 *     job_<job>_add = { Planets } # for generated modifiers
 *     # ...
 * }
 *
 * # 在类型规则中声明
 * types = {
 *     type[job] = {
 *         modifiers = {
 *             job_$_add = { Planets } # will be resolved to `job_<job>_add`
 *         }
 *         # ...
 *     }
 * }
 * ```
 *
 * @property name 规则名称。
 * @property categories 分类名的集合。
 * @property categoryConfigMap 分类名到分类规则的映射。
 * @property template 名称对应的模板表达式（如 `job_<job>_add`）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 *
 * @see CwtModifierCategoryConfig
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtModifierConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    val name: String
    val categories: Set<String> // category names
    val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig>
    val template: CwtTemplateExpression
    val supportedScopes: Set<String>

    companion object {
        /** 由属性规则解析为修正规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? {
            return CwtModifierConfigResolver.resolve(config, name)
        }

        /** 从定义上下文中的 modifiers 条目解析为修正规则，可指定 [name] 与类型表达式。 */
        @JvmStatic
        fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
            return CwtModifierConfigResolver.resolveFromDefinitionModifier(config, name, typeExpression)
        }
    }
}

// region Implementations

private object CwtModifierConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? {
        // string | string[]
        val categories = config.stringValue?.let { setOf(it) } ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }?.optimized()
        if (categories == null) {
            logger.warn("Skipped invalid modifier config (name: $name): Null categories".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved modifier config (name: $name).".withLocationPrefix(config) }
        return CwtModifierConfigImpl(config, name, categories)
    }

    fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
        // string | string[]
        val categories = config.stringValue?.let { setOf(it) } ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }?.optimized()
        if (categories == null) {
            logger.debug("Skipped invalid modifier config from definition modifier (name: $name): Null categories".withLocationPrefix(config))
            return null
        }
        val modifierName = name.replace("$", "<$typeExpression>").optimized()
        logger.debug { "Resolved modifier config from definition modifier (name: $name, type expression: $typeExpression).".withLocationPrefix(config) }
        return CwtModifierConfigImpl(config, modifierName, categories)
    }
}

private class CwtModifierConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String, // template name, not actual modifier name!
    override val categories: Set<String> = emptySet() // category names
) : UserDataHolderBase(), CwtModifierConfig {
    override val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
    override val template: CwtTemplateExpression = CwtTemplateExpression.resolve(name)
    override val supportedScopes: Set<String> by lazy { computeSupportedScopes() }

    private fun computeSupportedScopes(): Set<String> {
        return when {
            categoryConfigMap.isNotEmpty() -> ParadoxScopeManager.getSupportedScopes(categoryConfigMap)
            else -> config.optionData.supportedScopes
        }
    }

    override fun toString() = "CwtModifierConfigImpl(name='$name')"
}

// endregion
