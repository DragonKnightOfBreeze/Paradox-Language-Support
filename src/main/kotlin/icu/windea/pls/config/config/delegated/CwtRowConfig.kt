@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromProperty
import icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * @property columns 各个列对应的CWT规则。
 * @property endColumn 如果匹配最后一列的列名，则该列可以省略。
 */
interface CwtRowConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtFilePathMatchableConfig {
    @FromKey("row[$]")
    val name: String
    @FromProperty("columns: ColumnConfigs")
    val columns: Map<String, CwtPropertyConfig>
    @FromProperty("end_column: string?")
    val endColumn: String?

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtRowConfig?
    }

    companion object : Resolver by CwtRowConfigResolverImpl()
}
