package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtDatabaseObjectTypeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 扩展：数据库对象类型规则。
 *
 * 概述：
 * - 为某类“数据库对象”（如 UI/图片/音频等外部资源的条目）声明类型与本地化信息，并支持在“基础/替换”两种视角下取值。
 * - 由 `database_object_type[name] = { ... }` 或相关扩展写法声明。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `database_object_types` 下的每个成员属性。
 * - 规则名取自成员属性键，即 `name`（如 `ascension_perk`、`technology`）。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/cwtools-stellaris-config/config/database_object_types.cwt
 * database_object_types = {
 *     technology = {
 *         type = technology
 *         swap_type = swapped_technology
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property type 类型标识（可选）。
 * @property swapType 替换时使用的类型标识（可选）。
 * @property localisation 本地化文本键（可选）。
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

    /** 根据 [isBase]（基础/替换）返回对应的值规则。*/
    fun getConfigForType(isBase: Boolean): CwtValueConfig?

    interface Resolver {
        /** 由成员属性规则解析为“数据库对象类型规则”。*/
        fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig?
    }

    companion object : Resolver by CwtDatabaseObjectTypeConfigResolverImpl()
}
