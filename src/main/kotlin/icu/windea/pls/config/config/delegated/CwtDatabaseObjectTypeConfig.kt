package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression

/**
 * 数据库对象类型规则。
 *
 * 用于描述数据库对象表达式（[ParadoxDatabaseObjectExpression]）的类型与格式。
 * 这种表达式可以在本地化文件中作为概念名称使用（如 `['civic:some_civic', ...]`）。
 * 目前，它们最终会引用一个定义或本地化。
 *
 * 路径定位：`database_object_types/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * database_object_types = {
 *     civic = {
 *         type = civic_or_origin
 *         swap_type = swapped_civic
 *     }
 * }
 * ```
 *
 * @property name 名称。匹配类型前缀（如 `civic:some_civic` 中的 `civic:`）。
 * @property type 如果存在，则将对象节点（如 `civic:some_civic` 中的 `some_civic`）视为该类型的定义引用。
 * @property swapType 如果存在，则将接续的对象节点（如 `civic:some_civic:some_swapped_civic` 中的 `some_swapped_civic`）视为该切换类型的定义引用。
 * @property localisation 如果存在，则将对象节点（如 `job:job_soldier` 中的 `job_soldier`）视为匹配该前缀的本地化引用。
 *
 * @see ParadoxDatabaseObjectExpression
 * @see CwtDataTypes.DatabaseObject
 * @see icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
 */
interface CwtDatabaseObjectTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("type: string?")
    val type: String?
    @FromProperty("swap_type: string?")
    val swapType: String?
    @FromProperty("localisation: string?")
    val localisation: String?

    /** 根据 [isBase]（基础/替换）返回对应的值规则。 */
    fun getConfigForType(isBase: Boolean): CwtValueConfig?

    interface Resolver {
        /** 由属性规则解析为数据库对象类型规则。 */
        fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig?
    }

    companion object : Resolver by CwtDatabaseObjectTypeConfigResolverImpl()
}

// region Implementations

private class CwtDatabaseObjectTypeConfigResolverImpl : CwtDatabaseObjectTypeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid database object type config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propElements.groupBy { it.key }
        val type = propGroup.getOne("type")?.stringValue
        val swapType = propGroup.getOne("swap_type")?.stringValue
        val localisation = propGroup.getOne("localisation")?.stringValue
        if (type == null && localisation == null) {
            logger.warn("Skipped invalid database object type config (name: $name): Missing type or localisation property.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved database object type config (name: $name).".withLocationPrefix(config) }
        return CwtDatabaseObjectTypeConfigImpl(config, name, type, swapType, localisation)
    }
}

private class CwtDatabaseObjectTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String?,
    override val swapType: String?,
    override val localisation: String?
) : UserDataHolderBase(), CwtDatabaseObjectTypeConfig {
    override fun getConfigForType(isBase: Boolean): CwtValueConfig? {
        val expressionString = when {
            localisation != null -> "localisation"
            isBase -> type?.let { "<$it>" }
            else -> swapType?.let { "<$it>" }
        }
        if (expressionString == null) return null
        return CwtValueConfig.createMock(configGroup, expressionString)
    }

    override fun toString() = "CwtDatabaseObjectTypeConfigImpl(name='$name')"
}

// endregion
